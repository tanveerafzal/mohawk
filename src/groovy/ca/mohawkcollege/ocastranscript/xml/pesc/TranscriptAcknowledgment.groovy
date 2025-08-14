package ca.mohawkcollege.ocastranscript.xml.pesc


import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors
import org.springframework.context.annotation.Lazy

@InheritConstructors
class TranscriptAcknowledgment extends XmlFragment {
    final String tag = "TrnAck:Acknowledgment"

    Map<String, String> getAttributes() {
        [
                'xmlns:xsi'   : "http://www.w3.org/2001/XMLSchema-instance",
                'xmlns:TrnAck': "urn:org:pesc:message:TranscriptAcknowledgment:v1.1.0"
        ]
    }

    List<XmlFragment> getChildren() {
        [
                transmissionData.withTag("TransmissionData"),
                person.withTag("Person"),
                new XmlCData(academicAwardTotal as String).withTag("AcademicAwardTotal"),
                new XmlCData(courseTotal as String).withTag("CourseTotal"),
                getNoteMessages()*.collect { new XmlCData(it).withTag("NoteMessage") }
        ].flatten().findAll() as List<XmlFragment>
    }

    @Lazy
    @Delegate(includes = ["getRequestTrackingId", "setRequestTrackingId", "getCreatedDateTime", "setCreatedDateTime"])
    TransmissionData transmissionData = {
        XmlFragment tDataFragment = getChild("TransmissionData")
        TransmissionData tData = (tDataFragment ? new TransmissionData(tDataFragment) : new TransmissionData())
        tData.documentType = DocumentType.Acknowledgment
        tData.transmissionType = TransmissionType.Original
        tData
    }()

    @Lazy
    AcknowledgmentPerson person = { getChild("Person")?.with { new AcknowledgmentPerson(it) } }()

    XmlFragment getAcademicSummary() { getChild("AcademicSummary") }

    Integer getAcademicAwardTotal() { getBigInteger("AcademicAwardTotal") }

    void setAcademicAwardTotal(Integer value) { setString("AcademicAwardTotal", value as String) }

    Integer getCourseTotal() { getBigInteger("CourseTotal") }

    void setCourseTotal(Integer value) { setString("CourseTotal", value as String) }
}
