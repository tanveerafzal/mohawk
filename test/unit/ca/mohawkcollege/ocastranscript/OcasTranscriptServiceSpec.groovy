package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import ca.mohawkcollege.ocastranscript.xml.XmlAcknowledgment
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONElement
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(OcasTranscriptService)
class OcasTranscriptServiceSpec extends Specification {
    static mockBaseUri = 'Hello'
    static String endpointTranscriptIds = "$mockBaseUri/transcripts/no_response"
    static String endpointTranscripts = "$mockBaseUri/transcripts/"

    static doWithConfig(ConfigObject c) {
        c.baseURL = mockBaseUri
        c.mohawkcollege.ocastranscript.api.baseURL = mockBaseUri
    }

    def setup() {
        // mock up injected beans
        service.rest = GroovyMock(RestBuilder)
        service.ocasAuthenticationService = GroovyMock(OcasAuthenticationService) {
            getAccessToken() >> "OK"
        }
        service.xmlTransmissionService = GroovyMock(XmlTransmissionService)
    }

    void "test that if the API returns no tracking IDs we exit cleanly"() {
        given: "an API mock that will return an empty list of IDs"
        def mockResponse = GroovyMock(RestResponse) {
            getStatusCode() >> HttpStatus.OK
            getJson() >> GroovyMock(JSONElement) {
                getRequestID() >> []
            }
        }

        when: "we call the service method"
        service.fetchNewTranscripts()

        then: "no additional API or database calls are made"
        noExceptionThrown()
        1 * service.rest.get(endpointTranscriptIds, _) >> mockResponse
        0 * service.rest._
    }

    void "test that if the API throws HTTPS status 400 we exit cleanly"() {
        given: "an API mock that will throw an exception"

        when: "we call the service method"
        service.fetchNewTranscripts()

        then: "no additional API or database calls are made"
        noExceptionThrown()
        1 * service.rest.get(endpointTranscriptIds, _) >> { throw new HttpClientErrorException(HttpStatus.BAD_REQUEST) }
        0 * service.rest._
    }

    @Unroll
    @ConfineMetaClassChanges([OcasTranscript])
    void "test that for every ID received from the API, the corresponding transcript is saved and acknowledged (internal exception thrown: #sendThrowsException)"() {
        given: "a list of ID's from the API"
        def mockAcknowledgement = GroovyMock(XmlAcknowledgment)
        def anyOCASTranscript = GroovyMock(OcasTranscript, global: true) {
            generateAcknowledgment() >> mockAcknowledgement
        }
        OcasTranscript.createByJsonElement(*_) >> anyOCASTranscript
        service.rest.get(endpointTranscriptIds, _ as Closure) >> GroovyMock(RestResponse) {
            getStatusCode() >> HttpStatus.OK
            getJson() >> GroovyMock(JSONElement) {
                getRequestID() >> iDList
            }
        }

        when: "we call FetchNewTranscripts"
        service.fetchNewTranscripts()

        then: "each one generates a fetch from the API, a save to the database, and a response to the API"
        noExceptionThrown()

        iDList.each { String id ->
            and:
            1 * service.rest.get(endpointTranscripts + id, _ as Closure) >> GroovyMock(RestResponse) {
                getStatusCode() >> HttpStatus.OK
                getJson() >> GroovyMock(JSONElement) {
                    getRequestID() >> id
                }
            }
            1 * anyOCASTranscript.save(*_)
            1 * service.xmlTransmissionService.send(mockAcknowledgement) >> {
                if (sendThrowsException) {
                    throw new Exception()
                }
                true
            }
        }

        where:
        sendThrowsException << [false, true]
        iDList = [1..5].collect { RandomStringUtils.random(10) } as List<String>
    }
}














