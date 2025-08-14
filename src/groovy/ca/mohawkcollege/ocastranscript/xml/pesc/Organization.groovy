package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import grails.util.Holders
import groovy.transform.InheritConstructors
import org.springframework.context.annotation.Lazy

/**
 * Corresponds to PESC definition "AcRec:OrganizationType"
 */
@InheritConstructors
class Organization extends XmlFragment {
    final String tag = "Organization"

    @Lazy
    static Organization MOHAWK = { createMohawkOrganization() }()

    String getCsis() { getString("CSIS") }

    void setCsis(String identifier) { setString("CSIS", identifier) }

    String getUsis() { getString("USIS") }

    void setUsis(String identifier) { setString("USIS", identifier) }

    String getName() { getString("OrganizationName") }

    void setName(String organizationName) { setString("OrganizationName", organizationName) }

    List<XmlFragment> getChildren() {
        [
                csis?.with { new XmlCData(it).withTag("CSIS") },
                usis?.with { new XmlCData(it).withTag("USIS") },
                localOrganizationId?.with { it.withTag("LocalOrganizationID") },
                name?.with { new XmlCData(it).withTag("OrganizationName") },
                noteMessages.collect { new XmlCData(it)?.withTag("NoteMessage") }
        ].flatten().findAll() as List<XmlFragment>
    }

    String getIdentifier() { csis ?: usis ?: null }

    LocalOrganizationID getLocalOrganizationId() {
        getChild("LocalOrganizationID").with { it ? new LocalOrganizationID(it) : null }
    }

    List<String> getAddressLines() {
        XmlFragment addressFragment = this.Contacts.Address

        [addressFragment.all_AddressLine,
         [addressFragment.City,
          addressFragment.StateProvince,
          addressFragment.StateProvinceCode,
          addressFragment.PostalCode,
          addressFragment.CountryCode].findAll().join(" ")
        ].flatten().findAll()*.toString()
    }

    String getFormattedPhone() {
        formatPhoneFragment(this.Contacts.Phone as XmlFragment)
    }

    String getFormattedFax() {
        formatPhoneFragment(this.Contacts.FaxPhone as XmlFragment)
    }

    String getEmail() {
        this.Contacts.Email.EmailAddress
    }

    List<String> getContactNoteMessages() {
        this.Contacts.noteMessages
    }

    private static String formatPhoneNumber(String phoneString) {
        if (!phoneString) {
            return null
        }
        // strip out punctuation and pad to a known length
        phoneString = phoneString.replaceAll("[^a-zA-Z0-9]", "").padLeft(12)

        // Split the string into chunks, remove the empty ones, and join the rest with dashes
        [phoneString[0..3],
         phoneString[4..7],
         phoneString[8..11]]*.trim().findAll().join("-")
    }

    private static String formatPhoneFragment(XmlFragment phoneFragment) {
        [
                (phoneFragment.CountryPrefixCode.toString() ? "+${phoneFragment.CountryPrefixCode}" : null),
                (phoneFragment.AreaCityCode.toString() ? "(${phoneFragment.AreaCityCode})" : null),
                formatPhoneNumber(phoneFragment.PhoneNumber.toString()),
                (phoneFragment.PhoneNumberExtension.toString() ? "x${phoneFragment.PhoneNumberExtension}" : null)
        ].findAll().join(" ")
    }

    private static createMohawkOrganization() {
        def organizationConfig = Holders.grailsApplication.config.mohawkcollege.ocastranscript.Organization
        Organization organization = new Organization()
        organization.csis = organizationConfig.CSIS
        organization.name = organizationConfig.OrganizationName
        return organization
    }
}