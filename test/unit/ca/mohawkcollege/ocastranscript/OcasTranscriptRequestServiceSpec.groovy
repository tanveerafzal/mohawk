package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.baseline.*
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import ca.mohawkcollege.ocastranscript.ssb.TranscriptSchedule
import ca.mohawkcollege.ocastranscript.xml.XmlResponse
import ca.mohawkcollege.ocastranscript.xml.XmlTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.transform.NotYetImplemented
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(OcasTranscriptRequestService)
@Mock([TranscriptRequest, TranscriptSchedule, HoldCode, Svrtreq, Svrtnte, StudentHold])
@ConfineMetaClassChanges([TranscriptRequest])
class OcasTranscriptRequestServiceSpec extends Specification {
    static mockBaseUri = 'Hello'

    static doWithConfig(ConfigObject c) {
        c.baseURL = mockBaseUri
        c.mohawkcollege.ocastranscript.api.baseURL = mockBaseUri
    }

    def setup() {
        // mock up injected beans
        service.xmlTransmissionService = GroovyMock(XmlTransmissionService) { send(_) >> true }
        service.ocasAuthenticationService = GroovyMock(OcasAuthenticationService) { getAccessToken() >> "token" }
        service.xmlValidationService = GroovyMock(XmlValidationService) { getError(_) >> null }
        service.svktediService = GroovyMock(SvktediService) {}
        service.rest = GroovyMock(RestBuilder)
    }

    def cleanup() {
    }


    @Unroll
    void "test, for status [#requestStatus/#responseStatus], that we send #expectSend"() {
        given: "a mock response object"
        def mockResponse = GroovyMock(XmlResponse)

        and: "a mock transcript object"
        def mockTranscript = GroovyMock(XmlTranscript)

        and: "a mock request with certain statuses"
        GroovyMock(TranscriptRequest, global: true)
        TranscriptRequest./findAllBy.*/(*_) >> (responseStatuses.collect { ResponseStatus response ->
            GroovyMock(TranscriptRequest) {
                getRequestStatus() >> (requestStatus ? RequestStatus.valueOf(requestStatus) : null)
                getResponseStatus() >> response
                getOriginalXml() >> "<ok/>"
                generateResponse() >> mockResponse
                generateTranscript() >> mockTranscript
            }
        })

        when: "we process transcript requests"
        service.processTranscriptRequests()

        then: "the response is sent"
        noExceptionThrown()
        expectResponseCount * service.xmlTransmissionService.send(mockResponse) >> true
        expectTranscriptCount * service.xmlTransmissionService.send(mockTranscript) >> true

        where:
        requestStatus  | responseStatus | expectResponse | expectTranscript
        "Validating"   | "*"            | false          | false
        "Refusal"      | "Canceled"     | true           | false
        "Refusal"      | "NoRecord"     | true           | false
        "Refusal"      | "Deceased"     | true           | false
        "Refusal"      | "Hold"         | true           | false
        "ManualVerify" | "*"            | false          | false

        responseStatuses = responseStatus == "*" ? ResponseStatus.values() as List : [ResponseStatus.valueOf(responseStatus)]
        expectResponseCount = expectResponse ? responseStatuses.size() : 0
        expectTranscriptCount = expectTranscript ? responseStatuses.size() : 0
        expectSend = [(expectResponse ? "response(s)" : null), (expectTranscript ? "transcript(s)" : null)].findAll().join(" and ") ?: "nothing"
    }

    void "test that if the request status is ManualVerify then we flag it"() {
        given: "a mock request in ManualVerify status"
        GroovyMock(TranscriptRequest, global: true)
        TranscriptRequest./findAllBy.*/(*_) >> [GroovyMock(TranscriptRequest) {
            getRequestStatus() >> RequestStatus.ManualVerify
            1 * flagForManualIntervention()
        }]

        when: "we process transcript requests"
        service.processTranscriptRequests()

        then: "the flag method is called"
        noExceptionThrown()
    }


    @NotYetImplemented
    void "test that after sending, if there are unsent schedules the request is in the correct state"() {
        false
    }

    @NotYetImplemented
    void "test that after sending, if there are no unsent schedules the request is in the correct state"() { false }

    @Unroll
    void "test that for a transcript in status #status, verification is #outcome"() {
        given: "a request in the specified status"
        GroovyMock(TranscriptRequest, global: true)
        TranscriptRequest./findAll.*/(*_) >> [
                GroovyMock(TranscriptRequest) {
                    asBoolean() >> true
                    getRequestTrackingId() >> requestId
                    getRequestStatus() >> RequestStatus.valueOf(status)
                }
        ]

        when: "we process requests"
        service.processTranscriptRequests()

        then: "verification is triggered"
        expectCount * service.svktediService.processSvrtreq(_)

        where:
        status                        | expectCall
        "New"                         | true
        "Validating"                  | false
        "Deferred"                    | false
        "OnHold"                      | false
        "Refusal"                     | false
        "ReadyToSendResponse"         | false
        "ReadyToSendDeferredResponse" | false
        "ReadyToSendTranscript"       | false
        "ResponseSendFailed"          | false
        "ResponseSent"                | false
        "TranscriptSendFailed"        | false
        "TranscriptSent"              | false
        "ManualVerify"                | false
        "Error"                       | false
        requestId = "abc123"
        expectCount = expectCall ? 1 : 0
        outcome = expectCall ? "triggered" : "not triggered"
    }

    @Unroll
    @ConfineMetaClassChanges([OcasTranscriptRequestService])
    void "test attemptSendTranscript with different response types"() {
        given: "a mock transcript request with a specific response type"
        def transcriptRequest = GroovyMock(TranscriptRequest) {
            asBoolean() >> true
            getRequestTrackingId() >> 'C123'
            getRequestStatus() >> RequestStatus.Validating
            getSvrtreq() >> GroovyMock(Svrtreq) {
                getResponseType() >> responseType
            }
        }
        GroovyMock(TranscriptRequest, global: true)
        TranscriptRequest./findAll.*/(*_) >> [transcriptRequest]
        when: "processTranscriptRequests is called"
        service.processTranscriptRequests()
        then: "the correct methods are called based on the response type"
        sendTranscript * transcriptRequest.setRequestStatus(RequestStatus.ReadyToSendTranscript)
        sendResponse * transcriptRequest.setRequestStatus(RequestStatus.ReadyToSendResponse)
        where:
        responseType            | sendTranscript | sendResponse
        ResponseType.Transcript | 1              | 0
        ResponseType.Refusal    | 0              | 1
    }
}
