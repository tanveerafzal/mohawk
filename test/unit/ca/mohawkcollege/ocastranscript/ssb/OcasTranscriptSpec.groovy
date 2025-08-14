package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionType
import ca.mohawkcollege.ocastranscript.baseline.Goradid
import ca.mohawkcollege.ocastranscript.baseline.Spriden
import ca.mohawkcollege.ocastranscript.xml.XmlAcknowledgment
import ca.mohawkcollege.ocastranscript.xml.pesc.CollegeTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.Organization
import ca.mohawkcollege.ocastranscript.xml.pesc.Student
import ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionData
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(OcasTranscript)
@Mock([Spriden, Goradid])
@ConfineMetaClassChanges([CollegeTranscript, TransmissionData, Student, XmlAcknowledgment])
class OcasTranscriptSpec extends Specification {

    String basicXml = "<CollegeTranscript><TransmissionData></TransmissionData><Student></Student></CollegeTranscript>"

    CollegeTranscript mockCollegeTranscript
    TransmissionData mockTransmissionData
    Student mockStudent

    def setup() {
        mockTransmissionData = GroovyMock(TransmissionData) {
            asBoolean() >> true
        }
        mockTransmissionData.with(_) >> { Closure closure -> super.with(mockTransmissionData, closure) }
        mockStudent = GroovyMock(Student) { asBoolean() >> true }
        mockCollegeTranscript = GroovyMock(CollegeTranscript) {
            asBoolean() >> true
            getTransmissionData() >> mockTransmissionData
            getStudent() >> mockStudent
        }

        GroovyMock(CollegeTranscript, global: true)
        CollegeTranscript."<init>"(*_) >> mockCollegeTranscript
    }

    @Unroll
    void "test that with #comment and no known corresponding student, the student identifiers validate"() {
        mockStudent.getOntarioEducationNumber() >> (oen ?: null)
        mockStudent.getOcasApplicationNumber() >> (ocas ?: null)

        given: "no additional mocks, simulating student not found in Goradid or Spriden"

        when: "we create a transcript object based on sample XML content"
        domain.parseText(basicXml)

        then: "the created object validates on its student identifiers"
        assert domain.pidm == null
        assert domain.oen == (oen ?: null)
        assert domain.ocasApplicationNumber == (ocas ?: null)
        domain.validate(['pidm', 'oen', 'ocasApplicationNumber']) || (domain.errors.allErrors.each { println it } && false)
        noExceptionThrown()

        where:
        ids << (0..1).collect { [null, "", RandomStringUtils.randomAlphanumeric(9)] }.combinations()
        oen = ids[0]
        ocas = ids[1]

        // method name comment
        comment = [["OEN", "OCAS number"], ids].transpose().collect {
            "${it[0]} ${it[1] == null ? "null" : it[1] ? "present" : "empty"}"
        }.join(", ")
    }

    void "test that a CollegeTranscript object gets parsed from XML"() {
        when: "we populate our OcasTranscript with XML"
        domain.parseText(basicXml)

        then: "the CollegeTranscript constructor has been passed the same XML"
        1 * CollegeTranscript."<init>"(basicXml) >> mockCollegeTranscript
    }

    void "test that transmission type gets inherited from the TransmissionData object"() {
        mockTransmissionData.getTransmissionType() >> testTransmissionType

        when: "we populate our OcasTranscript with XML"
        domain.parseText(basicXml)

        then: "the transmissionType property has been populated"
        domain.transmissionType == testTransmissionType

        where:
        testTransmissionType << TransmissionType.values()
    }

    void "test that the testOnly flag gets inherited from the TransmissionData object"() {
        mockTransmissionData.getTest() >> isTest

        when: "we populate our OcasTranscript with XML"
        domain.parseText(basicXml)

        then: "the transmissionType property has been populated"
        domain.testOnly == isTest

        where:
        isTest << [true, false]
    }

    void "test that a source organization object gets inherited from the TransmissionData object"() {
        def mockSource = GroovyMock(Organization)
        mockTransmissionData.getSource() >> mockSource

        when: "we populate our OCAS transcript with given XML"
        domain.parseText(basicXml)

        then: "it contains a source organization object with data from the XML"
        domain.source == mockSource
    }

    @Unroll
    void "test that getNoteMessages returns #messageCount note messages from the CollegeTranscript object"() {
        mockCollegeTranscript.getNoteMessages() >> messageList

        when: "we populate our OcasTranscript with the given XML"
        domain.parseText(basicXml)

        then: "the note message list matches our expectations"
        domain.notes == messageList

        where:
        messageCount << [0, 1, 5]

        messageList = ([null] * messageCount).collect { RandomStringUtils.randomAlphanumeric(5) }
    }

    void "test that generating an acknowledgment calls the constructor correctly"() {
        given: "a mock constructor for XmlAcknowledgment"
        GroovyMock(XmlAcknowledgment, global: true)
        XmlAcknowledgment."<init>"(_ as Map) >> { Map args ->
            GroovyMock(XmlAcknowledgment) {
                getOcasTranscript() >> args.ocasTranscript
            }
        }

        when: "we generate an Xml Acknowledgment"
        XmlAcknowledgment xmlAcknowledgement = domain.generateAcknowledgment()

        then: "it received the current object in its constructor"
        xmlAcknowledgement.ocasTranscript == domain
    }
}
