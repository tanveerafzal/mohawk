package ca.mohawkcollege.ocastranscript

import grails.plugins.rest.client.RequestCustomizer
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.TestFor
import spock.lang.Shared
import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(OcasApiService)
class OcasApiServiceSpec extends Specification {
    static mockBaseUri = 'Hello'

    static doWithConfig(c) {
        c.mohawkcollege.ocastranscript.api.baseURL = mockBaseUri
    }

    @Shared
    def restBuilder
    @Shared
    def requestCustomizer
    def mockAccessToken = "mockToken"

    def setup() {
        restBuilder = GroovyMock(RestBuilder)
        requestCustomizer = GroovyMock(RequestCustomizer)
        service.rest = restBuilder
        service.ocasAuthenticationService = GroovyMock(OcasAuthenticationService) { getAccessToken() >> mockAccessToken }
    }

    def cleanup() {
    }

    void "test that JSON get method passes the right arguments into the RestBuilder"() {
        given: "appropriate arguments"
        def relativeUrl = "foo/bar/baz"
        1 * restBuilder.get(_ as CharSequence, _) >> { CharSequence url, closure ->
            closure.setDelegate(requestCustomizer)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.run()
            GroovyMock(RestResponse) {
                asBoolean() >> true
                getStatusCode() >> HttpStatus.OK
            }
        }
        requestCustomizer.getOcasAuthenticationService() >> service.getOcasAuthenticationService()

        when: "we call jsonGet"
        service.jsonGet(relativeUrl)

        then: "rest.get is called with the appropriate arguments"
        noExceptionThrown()
        1 * requestCustomizer.contentType(_ as CharSequence)
        1 * requestCustomizer.header("Authorization" as CharSequence, mockAccessToken as CharSequence)
    }

    void "test JSON post method"() {
        when: "we call jsonPost"
        //service.jsonPost()

        then: "rest.post is called with the appropriate arguments"
    }

    void "test form post method"() {
        when: "we call formPost"
        //service.formPost()

        then: "rest.post is called with the appropriate arguments"
    }

    void "test that URL is built properly from template and params"() {
        given: "fake URL parts"
        def fakeRelativeUrl = 'fake/$arg1/$arg2/path'
        def arg1 = 'foo'
        def arg2 = 'bar'
        def expectedUrl = "$mockBaseUri/fake/foo/bar/path"
        1 * restBuilder.get(expectedUrl as CharSequence, _) >> GroovyMock(RestResponse) {
            asBoolean() >> true
            getStatusCode() >> HttpStatus.OK
        }

        when: "we call the post method"
        service.jsonGet(fakeRelativeUrl, arg1: arg1, arg2: arg2)

        then: "rest.post is called with the expected url"
        noExceptionThrown()
    }
}
