package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.xml.AbstractXmlTransmission
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONElement

class XmlTransmissionService extends OcasApiService {

    def xmlValidationService

    Boolean send(AbstractXmlTransmission xmlTransmission) {
        log.debug("Sending ${xmlTransmission.class} response for requestId: ${xmlTransmission.requestId}")

        if (!xmlTransmission.validate()) {
            xmlTransmission.errorType = ErrorType.XmlGenerationError
            xmlTransmission.errorMessage = "Object failed validation. Aborting transmission"
            logError(xmlTransmission)
            log.debug(xmlTransmission.errors?.allErrors*.toString()?.join("\n"))
            return false
        }

        String xmlError = xmlValidationService.getError(xmlTransmission.xml)
        if (xmlError) {
            xmlTransmission.errorType = ErrorType.XmlGenerationError
            xmlTransmission.errorMessage = "Invalid outgoing XML detected ($xmlError). Aborting transmission"
            logError(xmlTransmission)
            return false
        }

        JSONElement json
        try {
            json = jsonPost(xmlTransmission.apiEndpoint, xmlTransmission.payload, requestId: xmlTransmission.requestId)
        } catch (Exception e) {
            xmlTransmission.errorType = ErrorType.ApiCommunication
            xmlTransmission.errorMessage = e.message
            logError(xmlTransmission)
            return false
        }

        // Check for an empty or missing JSON block
        if (!json) {
            xmlTransmission.errorType = ErrorType.ApiCommunication
            xmlTransmission.errorMessage = "No response content from API endpoint ${xmlTransmission.apiEndpoint}"
            logError(xmlTransmission)
            return false
        }

        // Make sure no error message was returned
        String errorMessage = json.Message?.toString() ?: json.Error?.toString()
        if (errorMessage) {
            xmlTransmission.errorType = ErrorType.ApiCommunication
            xmlTransmission.errorMessage = errorMessage
            logError(xmlTransmission)
            return false
        }

        // Make sure required fields are present
        String missingFields = xmlTransmission.expectedResponseFields.findAll { !(json."$it") }?.join(", ")
        if (missingFields) {
            xmlTransmission.errorType = ErrorType.ApiCommunication
            xmlTransmission.errorMessage = "Missing expected response field(s): $missingFields"
            logError(xmlTransmission)
            log.debug("API JSON response fields: ${json.keySet().join(", ")}")
            return false
        }

        // Success: We got a valid JSON response with no error message
        log.debug("Success: sent ${xmlTransmission.class} transmission for requestId: ${xmlTransmission.requestId}")
        return true
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    protected logError(AbstractXmlTransmission response) {
        log.error("Failed to send transmission for requestId ${response.requestId}: ${response.errorMessage}")
    }

    static String generateDocumentId() {
        return RandomStringUtils.randomAlphanumeric(32)
    }

}
