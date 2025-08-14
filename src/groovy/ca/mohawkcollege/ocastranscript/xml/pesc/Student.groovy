package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition "acRec:StudentType"
 */
@InheritConstructors
class Student extends XmlFragment {
    static final String AGENCY_CODE_MUTUALLY_DEFINED = "MutuallyDefined"
    static final String AGENCY_CODE_STATE = "State"
    static final String AGENCY_NAME_OCAS = "OCAS Application Number"
    static final String AGENCY_NAME_OEN = "Ontario Education Number"

    String getFullyQualifiedName() {
        XmlFragment name = this.Person.Name
        [name.NamePrefix, name.FirstName, name.all_MiddleName, name.LastName, name.NameSuffix, name.NameTitle].flatten()*.toString().findAll().join(" ")
    }

    String getSchoolAssignedPersonId() { getString("Person.SchoolAssignedPersonID") }

    String getOcasApplicationNumber() {
        this.Person.all_AgencyIdentifier.find {
            it.AgencyCode.toString() == AGENCY_CODE_MUTUALLY_DEFINED && it.AgencyName.toString() == AGENCY_NAME_OCAS
        }?.AgencyAssignedID?.toString() ?: this.Person?.AgencyAssignedID?.toString() ?: null
    }

    String getOntarioEducationNumber() {
        this.Person.all_AgencyIdentifier.find {
            it.AgencyCode.toString() == AGENCY_CODE_STATE
        }?.AgencyAssignedID?.toString() ?: null
    }

    Date getDateOfBirth() { getDate("Person.Birth.BirthDate") }

    @Lazy
    List<AcademicSession> academicSessions = {
        academicRecord.getChildren("AcademicSession").collect { XmlFragment xmlFragment ->
            new AcademicSession(xmlFragment)
        }
    }()

    @Lazy
    XmlFragment academicRecord = { getChild("AcademicRecord") }()

    @Lazy
    XmlFragment academicSummary = { academicRecord.getChild("AcademicSummary") }()

    @Lazy
    Gpa gpa = { new Gpa(this.academicSummary.GPA as XmlFragment) }()

    List<Award> getPDFAwards() {

        List<Award> awardListParent = this.all_AcademicAward?.findAll{!it.isEmpty()}.collect { XmlFragment xmlFragment ->
            new Award(xmlFragment)
        }

        List<Award> awardListChild = this.academicSessions?.awards

        def awardListJoined = awardListChild.findAll(){!it.isEmpty()} + awardListParent.findAll(){!it.isEmpty()}
        return awardListJoined
    }
}
