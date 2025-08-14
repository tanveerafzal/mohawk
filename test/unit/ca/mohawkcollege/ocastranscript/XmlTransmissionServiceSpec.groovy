package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.xml.XmlResponse
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(XmlTransmissionService)
class XmlTransmissionServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test that we fail to send a response with validation errors"() {
        given: "a response with errors"
        def mockResponse = GroovyMock(XmlResponse) {
            validate() >> false
        }

        when: "we try to send it"
        def result = service.send(mockResponse)

        then: "we get false back"
        noExceptionThrown()
        !result
    }
}
