package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import ca.mohawkcollege.ocastranscript.xml.pesc.Organization
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import ca.mohawkcollege.ocastranscript.xml.pesc.TranscriptAcknowledgment
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class XmlAcknowledgmentSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    @Unroll
    @ConfineMetaClassChanges(TranscriptAcknowledgment)
    void "test validation when #comment"() {
        given: "an instance of this class"
        def xmlAcknowledgment = new XmlAcknowledgment()

        and: "a mock transcript transcript"
        def mockTranscript = GroovySpy(OcasTranscript) {
            getSource() >> GroovyMock(Organization)
            getRequestTrackingId() >> mockRequestId
        }

        and: "a mock on TranscriptAcknowledgment"
        def mockTranscriptAcknowledgment = GroovyMock(TranscriptAcknowledgment) {
            asBoolean() >> true
            getRequestTrackingId() >> mockRequestId
        }
        mockTranscriptAcknowledgment.with(*_) >> mockTranscriptAcknowledgment
        GroovyMock(TranscriptAcknowledgment, global: true)
        TranscriptAcknowledgment."<init>"(*_) >> mockTranscriptAcknowledgment
        TranscriptAcknowledgment.newInstance(*_) >> mockTranscriptAcknowledgment

        and: "a mock transcript request"
        def mockRequest = GroovyMock(TranscriptRequest) {
            asBoolean() >> true
            getRequestTrackingId() >> "x"
            getResponseStatus() >> ResponseStatus.Hold
            getOriginalXml() >> "<a>b</a>"
        }

        when: "we set the object's properties"
        xmlAcknowledgment.ocasTranscript = transcript ? mockTranscript : null
        xmlAcknowledgment.transcriptRequest = request ? mockRequest : null
        xmlAcknowledgment.errorType = errorType ? ErrorType.valueOf(errorType) : null
        xmlAcknowledgment.errorMessage = errorMessage
        xmlAcknowledgment.rawXml = rawXml

        and: "we validate the object"
        def foundValid = xmlAcknowledgment.validate()

        then: "validation result matches our expectations"
        noExceptionThrown()
        foundValid == expectValid || (xmlAcknowledgment.errors.allErrors.each { println(it) } && false)

        where:
        transcript | request | errorType       | errorMessage | rawXml     | expectValid | comment
        true       | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "all properties populated"
        null       | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | false       | "transcript object null"
        true       | null    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "request object null"
        true       | true    | null            | "who?"       | "<a>b</a>" | true        | "error type null"
        true       | true    | "OcasIdUnknown" | null         | "<a>b</a>" | true        | "error message null"
        true       | true    | "OcasIdUnknown" | "who?"       | null       | true        | "raw XML null"
        null       | null    | null            | null         | null       | false       | "all properties null"

        mockRequestId = RandomStringUtils.randomAlphanumeric(12)
    }

    @ConfineMetaClassChanges(TranscriptAcknowledgment)
    void "test that the xml property is delegated to the transcriptAcknowledgment"() {
        given: "an instance of our class"

        and: "a mock TranscriptAcknowledgment with a fake XML value"
        GroovySpy(TranscriptAcknowledgment, global: true)
        TranscriptAcknowledgment.newInstance(*_) >> GroovySpy(TranscriptAcknowledgment) {
            1 * toXml() >> testValue
        }

        and: "a mock OcasTranscript whose XML explicitly does not match the mock TranscriptAcknowledgment"
        def instance = new XmlAcknowledgment()
        instance.ocasTranscript = Spy(OcasTranscript, constructorArgs: [requestTrackingId: dummyValue]) {
            getSource() >> GroovyMock(Organization)
        }

        when: "we access the xml property"
        def found = instance.xml

        then: "the result comes from the transcriptAcknowledgment"
        noExceptionThrown()
        found == testValue

        where:
        dummyValue = RandomStringUtils.randomAscii(63)
        testValue = RandomStringUtils.randomAscii(127)
    }
}
