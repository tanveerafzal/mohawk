package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition "Core:AgencyIdentifierType"
 */
@InheritConstructors
class AgencyIdentifier extends XmlFragment {
    static final String CODE_MUTUALLY_DEFINED = "MutuallyDefined"
    static final String CODE_STATE = "State"
    static final String NAME_OCAS = "OCAS Application Number"
    static final String NAME_OEN = "Ontario Education Number"

    List<XmlFragment> getChildren() {
        [
                new XmlCData(getAssignedId()).wrapTag("AgencyAssignedID"),
                new XmlCData(getCode()).wrapTag("AgencyCode"),
                new XmlCData(getName()).wrapTag("AgencyName"),
                new XmlCData(getCountryCode()).wrapTag("CountryCode"),
                new XmlCData(getStateProvinceCode()).wrapTag("StateProvinceCode"),
                getNoteMessages()*.collect { new XmlCData(it).wrapTag("NoteMessage") }
        ].flatten() as List<XmlFragment>
    }

    String getAssignedId() { getString("AgencyAssignedID") }

    String getCode() { getString("AgencyCode") }

    String getName() { getString("AgencyName") }

    String getCountryCode() { getString("CountryCode") }

    String getStateProvinceCode() { getString("StateProvinceCode") }

    boolean isOcas() { code == CODE_MUTUALLY_DEFINED && name == NAME_OCAS }

    boolean isOen() { code == CODE_STATE && name == NAME_OEN }
}
