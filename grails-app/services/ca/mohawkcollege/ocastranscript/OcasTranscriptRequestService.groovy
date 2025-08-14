package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.baseline.ResponseType
import ca.mohawkcollege.ocastranscript.baseline.StudentHold
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import ca.mohawkcollege.ocastranscript.ssb.TranscriptSchedule
import ca.mohawkcollege.ocastranscript.xml.XmlResponse
import ca.mohawkcollege.ocastranscript.xml.XmlTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.DocumentType
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import org.codehaus.groovy.grails.web.json.JSONElement

import java.text.ParseException

class OcasTranscriptRequestService extends OcasApiService {

    def svktediService
    def xmlTransmissionService
    def xmlValidationService

    /**
     * get the Transcript Requests from OCAS. Check if Request Already Exist in MOHAWK_TRANS_XML Table. if not exist
     * the save it in MOHAWK_TRANS_XML, MOHAWK_TRANS_XML_PROC, SVRTREQ and svrtnte Tables.
     *
     * @return null
     */
    def checkForNewRequests() {
        log.info("Checking for new transcript requests")

        JSONElement jsonRequestList
        try {
            jsonRequestList = jsonGet(OcasApi.ENDPOINT_NEW_REQUESTS)
            if (jsonRequestList?.isEmpty()) {
                throw new RuntimeException("No response data from API")
            }
        } catch (IOException e) {
            log.error("Failed to fetch transcript request list due to IO error: ${e.message}")
            throw e
        } catch (RuntimeException e) {
            log.error("Failed to fetch transcript request list: ${e.message}")
            throw e
        }

        List<String> requestIds = (jsonRequestList.RequestID as List<String>).findAll()
        log.info("Loading ${requestIds?.size()} new transcript requests...")

        jsonRequestList?.Message?.findAll()?.each {
            log.debug("Incoming JSON message: ${jsonRequestList.Message.toString()}")
        }

        requestIds.each { String requestId ->
            log.debug("getting transcript request ${requestId}")

            JSONElement jsonTranscriptRequest
            try {
                jsonTranscriptRequest = jsonGet(OcasApi.ENDPOINT_GET_REQUEST, requestId: requestId)
                if (!jsonTranscriptRequest) {
                    throw new RuntimeException("No response data from API")
                }
                if (jsonTranscriptRequest.error) {
                    throw new RuntimeException(jsonTranscriptRequest.error.toString())
                }
                assert requestId == jsonTranscriptRequest.RequestID
                log.debug("Request $requestId received from OCAS")
            } catch (IOException e) {
                log.error("Failed to retrieve transcript request with ID $requestId due to IO error: ${e.message}")
                return
            } catch (RuntimeException e) {
                log.error("Failed to retrieve transcript request with ID $requestId: ${e.message}")
                return
            }
            try {
                TranscriptRequest transcriptRequest = TranscriptRequest.findOrCreateByRequestTrackingId(requestId)

                if (transcriptRequest.id) {
                    log.debug("Found existing incoming request: $requestId")
                    return
                }

                log.info("Received new request [$requestId]")

                transcriptRequest.xml = jsonTranscriptRequest.PESCXml
                transcriptRequest.requestStatus = RequestStatus.New
                transcriptRequest.responseSent = false
                String dateString = jsonTranscriptRequest.RequestDate.toString()
                try {
                    transcriptRequest.requestDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.S", dateString)
                } catch (ParseException ignored) {
                    log.error("Request ID [$requestId]: Got bad date string [${dateString}] from API")
                    return
                }

                if (!transcriptRequest.save()) {
                    log.error("Failed to save transcript request: [$requestId]")
                    return
                }

                log.debug("RequestID $requestId: Transcript request saved successfully")
            } catch (Exception e) {
                log.error("Possible Transcript Request error: " + e.toString())
            }
        }

        log.info("Finished check for new requests")
    }

    /**
     * Process transcript requests received from OCAS. we need to send Transcript or Response back to OCAS.
     */
    def processTranscriptRequests() {
        log.info("Starting sending transcripts")

        try {
            List unsentRequests = TranscriptRequest.findAllByRequestTypeAndResponseSent(DocumentType.Request, false)
            // For the deferred end-of-term transcripts, they are in the TranscriptRequestReceived state instead of Hold due to when and where they get marked and saved in the DB
            // The response to OCAS shows proper holding XML data
            List unsentOnHoldRequests = TranscriptRequest.findAllByRequestStatusAndResponseSentAndResponseStatusInList(RequestStatus.Deferred, true, [ResponseStatus.TranscriptRequestReceived, ResponseStatus.Hold])
            log.debug("Found ${unsentRequests.size()} transcript requests awaiting processing")
            log.debug("Found ${unsentOnHoldRequests.size()} on hold transcript requests awaiting processing")
            unsentRequests.each { transcriptRequest ->
                log.debug("Processing transcript request: ${transcriptRequest.requestTrackingId}")
                try {
                    // Trigger population of values from Svrtreq object
                    log.debug("Updating info from SVRTREQ...")
                    transcriptRequest.getSvrtreq()

                    if (transcriptRequest?.svrtreq?.responseType && transcriptRequest?.svrtreq?.responseType != ResponseType.Incomplete){
                        switch (transcriptRequest.svrtreq.responseType){
                            case ResponseType.Transcript:
                                log.info("Transcript completion manually completed for request " + transcriptRequest.requestTrackingId + "...")
                                transcriptRequest.setRequestStatus(RequestStatus.ReadyToSendTranscript)
                                attemptSendTranscript(transcriptRequest)
                                break
                            case ResponseType.Refusal:
                                log.info("Transcript refusal manually completed for request " + transcriptRequest.requestTrackingId + "...")
                                transcriptRequest.setRequestStatus(RequestStatus.ReadyToSendResponse)
                                sendResponse(transcriptRequest)
                                break
                        }
                    }
                    else{
                        switch (transcriptRequest.requestStatus) {
                            case RequestStatus.New:
                                // If the request is new, run database processing for verification and cross-referencing
                                log.debug("Running SVKTEDI processing...")
                                svktediService.processSvrtreq(transcriptRequest)
                                transcriptRequest.requestStatus = RequestStatus.ReadyToSendTranscriptt
                                    attemptSendTranscript(transcriptRequest)
                                break
                            case RequestStatus.ReadyToSendDeferredResponse:
                            case RequestStatus.ReadyToSendResponse:
                            case RequestStatus.ResponseSendFailed:
                                // send response
                                log.debug("Sending response to OCAS...")
                                sendResponse(transcriptRequest)
                                break
                            case RequestStatus.OnHold:
                                // on hold, check holds. If the hold is lifted then mark for send, otherwise wait
                                // determine whether hold is expired
                                if (StudentHold.findAciveByPidm(transcriptRequest.pidm)) {
                                    log.debug("Found existing hold. Sending response to OCAS...")
                                    sendResponse(transcriptRequest)
                                } else {
                                    // mark as new, it will get verified again on the next pass
                                    log.debug("Marking hold as lifted...")
                                    transcriptRequest.requestStatus = RequestStatus.New
                                }
                                break
                            case RequestStatus.TranscriptSendFailed:
                            case RequestStatus.Deferred:
                            case RequestStatus.ReadyToSendTranscript:
                            case RequestStatus.Validating:
                                attemptSendTranscript(transcriptRequest)
                                break
                            case RequestStatus.Refusal:
                                log.debug("Sending refusal to OCAS...")
                                sendResponse(transcriptRequest)
                                break
                            case RequestStatus.ManualVerify:
                                log.debug("Flagging for manual verification...")
                                transcriptRequest.flagForManualIntervention()
                                // send nothing
                                break
                            case RequestStatus.ResponseSent:
                                // We've attempted to send a transcript
                            case RequestStatus.TranscriptSent:
                                // Something is broken
                            case RequestStatus.Error:
                                // figure this out
                                break
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to process transcript with request tracking ID ${transcriptRequest.requestTrackingId}: ${ex.message}")
                    transcriptRequest.requestStatus = RequestStatus.Error
                }

                log.debug("Finished processing transcript request: ${transcriptRequest.requestTrackingId}")
            }

            unsentOnHoldRequests.each { transcriptRequest ->
                log.debug("Processing deferred transcript request: ${transcriptRequest.requestTrackingId}")
                try {
                    // Trigger population of values from Svrtreq object
                    log.debug("Updating info from SVRTREQ...")
                    transcriptRequest.getSvrtreq()
                    attemptSendTranscript(transcriptRequest)
                } catch (Exception ex) {
                    log.error("Failed to process on hold transcript with request tracking ID ${transcriptRequest.requestTrackingId}: ${ex.message}")
                    transcriptRequest.requestStatus = RequestStatus.Error
                }
                log.debug("Finished processing on hold transcript request: ${transcriptRequest.requestTrackingId}")
            }
        } catch (Exception e) {
            log.error("General sending transcript error: " + e.toString())
        }
        log.info("Finished sending transcripts")
    }

    /**
     * Attempts to send a transcript request to OCAS if there are any schedules that are ready to be sent.
     *
     * @param transcriptRequest the transcript request to send
     */
    private void attemptSendTranscript(TranscriptRequest transcriptRequest) {
        // Merge the transcript request to ensure that the object is in the current Hibernate session
        transcriptRequest = transcriptRequest.merge()

        // Find all schedules that are ready to be sent
        Set<TranscriptSchedule> sendableSchedules = transcriptRequest.schedules.findAll { it.isReadyToSend(transcriptRequest) }

        // If there are any schedules that are ready to be sent
        if (sendableSchedules) {
            // Update the request status to indicate that the transcript is ready to be sent
            transcriptRequest.requestStatus = RequestStatus.ReadyToSendTranscript

            // Log that the transcript is being sent
            log.debug("Sending transcript to OCAS...")

            try {
                // Construct and send an XML transcript
                // If the transcript is successfully sent, update the schedules to indicate that they have been sent
                if (sendTranscript(transcriptRequest)) {
                    sendableSchedules.each { transcriptSchedule ->
                        transcriptSchedule.sent = true
                        transcriptSchedule = transcriptSchedule.merge()
                        transcriptSchedule.save()
                    }
                }
            // If there is an exception during the date conversion, log the error
            } catch (Exception dateEx) {
                log.error("Possible date conversion error: " + dateEx.toString())
            }
        // If there are no schedules that are ready to be sent
            // Log that the request is being marked as deferred
        } else {
            log.debug("Marking as deferred...")
            // Update the request and response statuses to indicate that the transcript should not be sent yet
            transcriptRequest.requestStatus = RequestStatus.Deferred
            transcriptRequest.responseStatus = ResponseStatus.Hold
        }
    }

    private void sendResponse(TranscriptRequest transcriptRequest) {
        log.debug("Sending response for request ID: ${transcriptRequest.requestTrackingId}")

        XmlResponse response

        try {
            response = transcriptRequest.generateResponse()
        } catch (RuntimeException exception) {
            transcriptRequest.requestStatus = RequestStatus.Error
            transcriptRequest.errorType = ErrorType.XmlGenerationError
            log.error("Generating XML Response [${transcriptRequest.requestTrackingId}] failed: ${exception.message}")
            return
        }

        if (!xmlTransmissionService.send(response)) {
            // There was a problem sending
            transcriptRequest.requestStatus = RequestStatus.ResponseSendFailed
            transcriptRequest.errorType = response.errorType
        } else if (transcriptRequest.responseStatus == ResponseStatus.Hold) {
            // Check if deferred hold is ready to be released first
            // Mark as deferred for the deferred holds
            if (transcriptRequest.requestStatus == RequestStatus.ReadyToSendDeferredResponse) {
                // Now we wait for the deferred hold to be lifted
                transcriptRequest.requestStatus = RequestStatus.Deferred
            } else {
                // Now we wait for the hold to be lifted
                transcriptRequest.requestStatus = RequestStatus.OnHold
            }

            transcriptRequest.responseSent = true
            log.info("Sent hold response for request ID: ${transcriptRequest.requestTrackingId}")
        } else {
            // response sent successfully.
            transcriptRequest.responseSent = true
            transcriptRequest.requestStatus = RequestStatus.ResponseSent
            log.info("Sent acknowledgment response for request ID: ${transcriptRequest.requestTrackingId}")
        }
    }

    private boolean sendTranscript(TranscriptRequest transcriptRequest) {
        log.debug("Sending transcript for request ID: ${transcriptRequest.requestTrackingId}")

        XmlTranscript xmlTranscript

        try {
            xmlTranscript = transcriptRequest.generateTranscript()
        } catch (AcademicHistoryNotFoundException exception) {
            log.debug("sendTranscript failed: ${exception.message}")
            transcriptRequest.errorType = ErrorType.AcademicHistoryNotFound
            transcriptRequest.flagForManualIntervention()
            log.info("Flagged manual intervention for request ID: ${transcriptRequest.requestTrackingId}")
            return false
        } catch (RuntimeException exception) {
            log.error("Generating XML Transcript [${transcriptRequest.requestTrackingId}] failed: ${exception.message}")
            return false
        }

        // If we've gotten here we're ready to send
        log.debug("Attempting to send Transcript [${transcriptRequest.requestTrackingId}]")
        if (xmlTransmissionService.send(xmlTranscript)) {
            log.info("Sent transcript for request ID: ${transcriptRequest.requestTrackingId}")
            // XML transcript was sent okay
            transcriptRequest.requestStatus = RequestStatus.TranscriptSent
            transcriptRequest.svrtreq.setResponseType(ResponseType.Transcript)
            transcriptRequest.responseSent = true
            return true
        } else {
            log.info("Failed to send transcript for request ID: ${transcriptRequest.requestTrackingId}")
            // There was a problem sending the XML transcript
            transcriptRequest.requestStatus = RequestStatus.TranscriptSendFailed
            transcriptRequest.errorType = xmlTranscript.errorType
            return false
        }
    }
}