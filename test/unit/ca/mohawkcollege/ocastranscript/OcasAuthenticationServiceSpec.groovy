package ca.mohawkcollege.ocastranscript

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.TestFor
import groovy.time.TimeCategory
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONElement
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(OcasAuthenticationService)
class OcasAuthenticationServiceSpec extends Specification {
    static mockBaseUri = 'Hello'

    static doWithConfig(ConfigObject c) {
        c.mohawkcollege.ocastranscript.api.tokenLifetimeSeconds = 300
        c.mohawkcollege.ocastranscript.api.baseURL = mockBaseUri
    }

    @Shared
    RestResponse mockResponse
    @Shared
    String authUrl

    def setup() {
        authUrl = "$mockBaseUri/${OcasApi.ENDPOINT_AUTHENTICATE}"

        service.rest = GroovyMock(RestBuilder)

        // A mock API response that returns a random pair of values each time
        mockResponse = GroovyMock(RestResponse) {
            getStatusCode() >> HttpStatus.OK
            getJson() >> GroovyMock(JSONElement) {
                asBoolean() >> true
                getAccess_token() >> RandomStringUtils.random(32)
                getToken_type() >> RandomStringUtils.random(6)
            }
        }
    }

    def cleanup() {
    }

    @Unroll
    void "test that #callCount fetches of accessToken result in only one API call"() {
        given: "a mock on the RestBuilder"
        1 * service.rest.post(authUrl, _) >> mockResponse
        0 * service.rest._

        when: "we call getAccessToken repeatedly"
        callCount.times { service.accessToken }

        then: "we only called the rest service once"
        noExceptionThrown()

        where:
        callCount << [1, 2, 5]
    }

    @Unroll
    void "test that #callCount subsequent fetches of accessToken return the same value"() {
        given: "random values from a mock Rest service"
        service.rest.post(authUrl, _) >> mockResponse

        when: "we call getAccessToken() multiple times"
        def allTokens = (1..callCount).collect { service.accessToken }

        then: "we got several tokens, all the same"
        allTokens.size() == callCount
        allTokens.toUnique().size() == 1

        where:
        callCount << [1, 2, 5]
    }

    // We're accessing some private members here
    @SuppressWarnings('GroovyAccessibility')
    void "test that when the token has timed out, we fetch a new one"() {
        given: "mocks on the rest service"
        callCount * service.rest.post(authUrl, _) >> mockResponse
        0 * service.rest._

        and: "an old expiry date"
        Date definitelyExpired = TimeCategory.minus(new Date(), service.tokenLifetime).minus(1)

        when: "we get the accessToken n times, expiring it each time"
        callCount.times {
            service.tokenFetchTime = definitelyExpired
            assert !service.tokenValid
            service.accessToken
        }

        then: "the API was called each time"
        noExceptionThrown()

        where:
        callCount << [2, 5, 13]
    }
}
