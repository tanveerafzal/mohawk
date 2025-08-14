package ca.mohawkcollege.ocastranscript

import grails.test.spock.IntegrationSpec

class OcasAuthenticationServiceIntegrationSpec extends IntegrationSpec {

    def ocasAuthenticationService

    def setup() {
    }

    def cleanup() {
    }

    void "test API access token retrieval"() {
        when: "we query the service for a token"
        def token = ocasAuthenticationService.accessToken

        then:
        noExceptionThrown()
        token
    }
}
