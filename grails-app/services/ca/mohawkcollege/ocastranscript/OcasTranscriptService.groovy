package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import org.codehaus.groovy.grails.web.json.JSONElement
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException

class OcasTranscriptService extends OcasApiService {

    def xmlTransmissionService

    /**
     * Query OCAS for new transcripts without any response, and save each one
     */
    void fetchNewTranscripts() {
        log.info("Checking for new incoming transcripts")

        List<String> requestIds = fetchPendingTranscriptIds()
        log.debug("Transcript request IDs received: ${requestIds?.size()}")

        requestIds.each { requestId ->
            log.debug("Getting transcript for request ID ${requestId}")
            fetchTranscriptByRequestId(requestId)
        }

        log.info("Successfully retrieved ${requestIds?.size()} new incoming transcripts")
    }

    private List<String> fetchPendingTranscriptIds() {
        try {
            return (jsonGet(OcasApi.ENDPOINT_NEW_TRANSCRIPTS)?.RequestID as List<String>)?.findAll()
        }
        catch (RestClientException restClientException) {
            // When no transcripts are found a 400 status is returned
            restClientException.cause.with { cause ->
                if (cause instanceof HttpClientErrorException && cause.statusCode == HttpStatus.BAD_REQUEST) {
                    return [] as List<String>
                }
                throw restClientException
            }
        }
    }

    private fetchTranscriptByRequestId(String requestId) {
        try {

            JSONElement json = jsonGet(OcasApi.ENDPOINT_GET_TRANSCRIPT, requestId: requestId)
            assert requestId == json?.RequestID
            log.debug("RequestID $requestId: Received transcript from OCAS")

            // Having passed the sanity tests, let's parse the return XML and save it to the database
            OcasTranscript transcript = OcasTranscript.createByJsonElement(json)
            transcript.save(failOnError: true)
            log.debug("RequestID $requestId: Saved to database")

            // Now that it's saved, we send an acknowledgment of receipt
            xmlTransmissionService.send(transcript.generateAcknowledgment())
            log.info("RequestID $requestId: Transcript saved and acknowledgment sent to OCAS")
        }
        catch (Exception ex) {
            log.error("Processing incoming transcript for request ID ${requestId} failed with exception: ${ex.message}")
        }
    }
}

