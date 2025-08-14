package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import ca.mohawkcollege.ocastranscript.xml.pesc.HoldReason
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class XmlResponseSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    @Unroll
    void "test validation #outcome when #comment"() {
        given: "an instance of this class"
        def xmlResponse = new XmlResponse()

        and: "a mock transcript request"
        def mockRequest = GroovyMock(TranscriptRequest) {
            getRequestTrackingId() >> "x"
        }

        when: "we set the object's properties"
        xmlResponse.transcriptRequest = request ? mockRequest : null
        xmlResponse.errorType = errorType ? ErrorType.valueOf(errorType) : null
        xmlResponse.errorMessage = errorMessage
        xmlResponse.rawXml = rawXml
        //
        xmlResponse.holdReason = hold ? HoldReason.valueOf(hold) : null
        xmlResponse.noteMessage = note
        xmlResponse.ocasResponseStatus = response ? ResponseStatus.valueOf(response) : null

        and: "we validate the object"
        def foundValid = xmlResponse.validate()

        then: "validation result matches our expectations"
        noExceptionThrown()
        foundValid == expectValid || (xmlResponse.errors.allErrors.each { println(it) } && false)

        where:
        hold        | note  | response | request | errorType       | errorMessage | rawXml     | expectValid | comment
        "Financial" | 'abc' | "Hold"   | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "all properties populated"
        null        | 'abc' | "Hold"   | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "hold reason null"
        "Financial" | null  | "Hold"   | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "note null"
        "Financial" | 'abc' | null     | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | false       | "response status null"
        "Financial" | 'abc' | "Hold"   | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | true        | "access token null"
        "Financial" | 'abc' | "Hold"   | null    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | false       | "request object null"
        "Financial" | 'abc' | "Hold"   | true    | null            | "who?"       | "<a>b</a>" | true        | "error type null"
        "Financial" | 'abc' | "Hold"   | true    | "OcasIdUnknown" | null         | "<a>b</a>" | true        | "error message null"
        "Financial" | 'abc' | "Hold"   | true    | "OcasIdUnknown" | "who?"       | null       | true        | "raw XML null"
        null        | null  | null     | null    | null            | null         | null       | false       | "all properties null"
        "Other"     | null  | "Hold"   | true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | false       | "hold reason Other and note null"
        outcome = expectValid ? "passes" : "fails"
    }
}
