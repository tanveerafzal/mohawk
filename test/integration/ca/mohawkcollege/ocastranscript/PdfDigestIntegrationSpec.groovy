package ca.mohawkcollege.ocastranscript

import grails.test.spock.IntegrationSpec

class PdfDigestIntegrationSpec extends IntegrationSpec {

    // inject bean
    def printTranscriptService

    def setup() {
    }

    def cleanup() {
    }

    void "test generating the PDF digest"() {
        when: "we run the service method"
        printTranscriptService.generatePdfDigest()

        then: "there were no exceptions"
        noExceptionThrown()
    }

    // TODO test that the template is actually being rendered correctly
}
