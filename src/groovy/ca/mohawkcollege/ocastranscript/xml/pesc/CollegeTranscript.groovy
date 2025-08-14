package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

@InheritConstructors
class CollegeTranscript extends XmlFragment {
    @Lazy
    TransmissionData transmissionData = { new TransmissionData(this.getChild("TransmissionData")) }()

    @Lazy
    Student student = { new Student(getChild("Student")) }()

    @Lazy
    XmlFragment academicSummary = { this.student.academicSummary }()

    @Lazy
    Gpa gpa = { this.student.gpa }()

    Organization getSource() { this.transmissionData.source }

    Organization getDestination() { this.transmissionData.destination }

    Date getCreatedDateTime() { this.transmissionData.createdDateTime }

    CourseCreditLevel getCreditLevel() {
        academicSummary.getString("AcademicSummaryLevel").with { it ? CourseCreditLevel.valueOf(it) : null }
    }

    Integer getAcademicAwardTotal() { student.academicRecord.getChildren("AcademicAward").size() }

    Integer getCourseTotal() { student.academicRecord.getChildren("Course").size() }

    // Get messages from transmission data as well
    List<String> getNoteMessages() {
        super.noteMessages + transmissionData.noteMessages
    }
}
