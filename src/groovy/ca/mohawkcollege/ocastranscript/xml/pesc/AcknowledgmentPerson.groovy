package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import grails.validation.Validateable
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition "acRec:AcknowledgmentPersonType"
 */
@Validateable
@InheritConstructors
class AcknowledgmentPerson extends XmlFragment {
    static constraints = {
        schoolAssignedPersonId nullable: true, minSize: 1, maxSize: 20
    }

    List<XmlFragment> getChildren() {
        [
                getSchoolAssignedPersonId()?.with { it ? new XmlCData(it).wrapWith("SchoolAssignedPersonID") : null },
                agencies*.withTag("AgencyIdentifier"),
                getDateOfBirth()?.with { it ? new XmlCData(it, Format.DATE).withTag("BirthDate").wrapWith("Birth") : null },
                getName()?.withTag("Name"),
                getNoteMessages()*.collect { new XmlCData(it).withTag("NoteMessage") }
        ].flatten().findAll() as List<XmlFragment>
    }

    Date dateOfBirth
    String schoolAssignedPersonId

    String getSchoolAssignedPersonId() { schoolAssignedPersonId ?: getString("SchoolAssignedPersonID") }

    @Lazy
    List<AgencyIdentifier> agencies = {
        getChildren("AgencyIdentifier").collect { new AgencyIdentifier(it) }
    }()

    String getOcasApplicationNumber() { agencies.find { it.isOcas() }?.assignedId }

    String getOntarioEducationNumber() { agencies.find { it.isOen() }?.assignedId }

    XmlFragment getName() { getChild("Name") }

    Date getDateOfBirth() { dateOfBirth ?: getDate("Birth.BirthDate") }
}
