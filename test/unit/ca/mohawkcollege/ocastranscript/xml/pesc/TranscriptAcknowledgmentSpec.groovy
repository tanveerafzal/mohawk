package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@TestMixin(GrailsUnitTestMixin)
@ConfineMetaClassChanges(TransmissionData)
class TranscriptAcknowledgmentSpec extends XmlSpecification {
    static Map xmlPropertyTags = [person: "Person", academicSummary: "AcademicSummary"]
    static Map xmlExampleMap = [
            simple    : "<body>text</body>",
            complex   : "<a><b>c</b><b><c>d</c></b></a>",
            multichild: "<a><b>c</b></a><b>c</b><d>e</d>",
            mixed     : "abc<a>b</a><b></b><c>haha</c>",
            empty     : ""
    ]
    static Map integerPropertyTags = [academicAwardTotal: "AcademicAwardTotal", courseTotal: "CourseTotal"]
    static List integerExampleList = [0, 1, 5]

    @Unroll
    void "test that #propertyName output XML matches the input with #comment structure"() {
        GroovySpy(AcknowledgmentPerson, global: true) { toXml() >> xmlSubstring }

        given: 'a transcript acknowledgment object built from an XML string'
        def transcriptAcknowledgment = new TranscriptAcknowledgment(xmlString)

        expect: "the property XML to match the input string"
        transcriptAcknowledgment."$propertyName".toXml() == xmlSubstring

        where:
        testValue << [xmlPropertyTags.entrySet(), xmlExampleMap.entrySet()].combinations()

        propertyTag = testValue[0] as Map.Entry<String, String>
        xmlExample = testValue[1] as Map.Entry<String, String>
        propertyName = propertyTag.key
        xmlTag = propertyTag.value
        xmlContent = xmlExample.value
        xmlSubstring = buildTag(xmlTag, xmlContent)
        xmlString = buildTag("Acknowledgment", xmlSubstring)

        comment = xmlExample.key
    }

    @Unroll
    void "test that integer #propertyName is parsed from XML with value #count"() {
        given: 'a transcript acknowledgment object built from an XML string'
        def transcriptAcknowledgment = new TranscriptAcknowledgment(xmlString)

        expect: "the property value is as expected"
        transcriptAcknowledgment."$propertyName" == count

        where:
        testValue << [integerPropertyTags.entrySet(), integerExampleList].combinations()

        propertyTag = testValue[0] as Map.Entry<String, String>
        count = testValue[1] as Integer
        propertyName = propertyTag.key
        xmlTag = propertyTag.value

        xmlSubstring = buildTag(xmlTag, count.toString())
        xmlString = buildTag("Acknowledgment", xmlSubstring)
    }

    void "test that accessing transmissionData property creates a new object"() {
        given: "an XML string containing a substring fragment"
        GroovySpy(TransmissionData, global: true)

        when: "a TransmissionAcknowledgment created from that XML"
        def transcriptAcknowledgment = new TranscriptAcknowledgment(xmlString)

        and: "the TransmissionData object is accessed"
        transcriptAcknowledgment.transmissionData

        then: "a new TransmissionData object is created with the original fragment"
        1 * TransmissionData."<init>"({ it.toXml() == xmlSubstring } as XmlFragment)

        where:
        xmlSubstring = buildTag("TransmissionData", randomXml)
        xmlString = buildTag("Acknowledgment", xmlSubstring)
    }
}
