package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.bannerlib.base.UserHistory
import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.*
import ca.mohawkcollege.ocastranscript.baseline.*
import ca.mohawkcollege.ocastranscript.type.BooleanYNType
import ca.mohawkcollege.ocastranscript.xml.SendTrigger
import ca.mohawkcollege.ocastranscript.xml.XmlResponse
import ca.mohawkcollege.ocastranscript.xml.XmlTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.DocumentType
import ca.mohawkcollege.ocastranscript.xml.pesc.HoldReason
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import groovy.util.slurpersupport.GPathResult
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * This class corresponds to the XML type TranscriptRequest, currently in version 1.1.0. The XMLNS for this element is:
 * urn:org:pesc:message:TranscriptRequest:v1.1.0
 */
@Transactional
class TranscriptRequest implements Serializable {

    static dataOrigin = "OCAS"
    static userId = 'ocas-app'

    String requestTrackingId
    @Enumerated(EnumType.STRING)
    DocumentType requestType = DocumentType.Request
    Date requestDate
    BigDecimal pidm
    @Enumerated(EnumType.STRING)
    RequestStatus requestStatus
    @Enumerated(EnumType.STRING)
    ErrorType errorType
    String oen
    String ocasApplNumber
    String originalXml
    String responseXml
    Boolean responseSent
    @Enumerated(EnumType.STRING)
    ResponseStatus responseStatus
    // Grails autofilled timestamps
    Date dateCreated
    Date lastUpdated

    // Derived
    Svrtreq svrtreq

    // Transient
    HoldReason holdReason

    @Cascade(CascadeType.ALL)
    static hasMany = [schedules: TranscriptSchedule]

    static fetchMode = [schedules: 'eager']

    static transients = ['xml', 'holdReason']

    String xml

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "OCAS_TRANSCRIPT_REQUEST"
        originalXml type: "materialized_clob"
        responseXml type: "materialized_clob"
        responseSent type: BooleanYNType
    }

    static constraints = {
        requestTrackingId maxSize: 35
        pidm nullable: true
        requestStatus nullable: true
        errorType nullable: true
        oen nullable: true, maxSize: 30
        ocasApplNumber nullable: true, maxSize: 30
        originalXml nullable: true
        responseXml nullable: true
        responseSent nullable: true
        responseStatus nullable: true

        svrtreq nullable: true
    }

    /**
     * Added property setter functionality. When we set the Svrtreq property, we populate some of the request
     * properties from it
     *
     * @param newSvrtreq
     */
    void setSvrtreq(Svrtreq newSvrtreq) {
        try {
            this.svrtreq = newSvrtreq

            def possibleSvrtreqId = this.svrtreq?.svrtreqId
            // Get the pidm if necessary
            try {
                if (possibleSvrtreqId != null) {
                    if (!pidm && svrtreq?.svrtreqId) {
                        pidm = UserHistory.findByBannerId(svrtreq.svrtreqId)?.getPidm()
                    }
                } else {
                    println("The Svrtreq object is null")
                }
            }
            catch (Exception svrtreqEx) {
                log.error("Svrtreq fetching exception: " + svrtreqEx.toString())
            }

            requestStatus = extractRequestStatusFromSvrtreq()
            // Needed because the DateCheck flag is usually set for this request status
            if (requestStatus == RequestStatus.ReadyToSendDeferredResponse) {
                responseStatus = ResponseStatus.Hold
            } else {
                responseStatus = extractResponseStatusFromSvrtreq()
            }
            holdReason = extractHoldReasonFromSvrtreq()
        }

        catch (Exception ex) {
            log.error("Svrtreq-related exception: " + ex.toString())
        }
    }

    private void saveSvrtreq() {
        if (!svrtreq) {
            try {
                def svrtreqObj = Svrtreq.findOrCreateBySvrtreqBgn02(requestTrackingId)
                setSvrtreq(svrtreqObj)
            }
            catch (Exception e) {
                log.error("Error saving Svrtreq object: " + e.toString())
            }

        }

        // populate Svrtreq from XML for new records only
        if (!svrtreq.id && originalXml) {
            GPathResult xmlParse = XmlSlurper.newInstance().parseText(originalXml)

            // find the person this request is for
            GPathResult personXml = xmlParse.Request.RequestedStudent.Person
            Map<String, String> person =
                    [bannerId         : personXml.SchoolAssignedPersonID,
                     namePrefix       : personXml.Name.NamePrefix,
                     firstName        : personXml.Name.FirstName,
                     middleName       : personXml.Name.MiddleName,
                     lastName         : personXml.Name.LastName,
                     birthDate        : personXml.Birth.BirthDate,
                     genderCode       : personXml.Gender.GenderCode,
                     sin              : personXml.SIN,
                     alternateNameCode: personXml.AlternateName.NameCode,
                     alternateLastName: personXml.AlternateName.LastName,
                     exitDateString   : xmlParse.Request.RequestedStudent.Attendance.ExitDate
                    ].collectEntries { key, value -> [(key): value.toString()] } as Map<String, String>

            svrtreq.svrtreqBirthDate = Date.parse('yyyy-MM-dd', person.birthDate)
            svrtreq.svrtreqStudentNo_1 = person.bannerId
            svrtreq.svrtreqSurname = person.lastName
            svrtreq.svrtreqFirstmidname = person.middleName
            svrtreq.svrtreqFirstname = person.firstName
            svrtreq.svrtreqPrefix = person.namePrefix
            svrtreq.gender = Gender.valueOf(person.genderCode)
            svrtreq.svrtreqSin = person.sin
            if (person.exitDateString) {
                svrtreq.svrtreqExitDate = Date.parse('yyyy-MM-dd', person.exitDateString)
            }

            if (person.alternateNameCode == 'Former') {
                svrtreq.svrtreqFormersurname = person.alternateLastName
            }
        }

        if (!svrtreq.svrtreqTransDate) {
            svrtreq.svrtreqTransDate = requestDate
        }
        if (!svrtreq.svrtreqOcasAppnum) {
            svrtreq.svrtreqOcasAppnum = ocasApplNumber
        }
        if (!svrtreq.responseType) {
            svrtreq.responseType = ResponseType.Incomplete
        }
        if (!svrtreq.verificationStage) {
            svrtreq.verificationStage = VerificationStage.NotStarted
        }
        svrtreq.svrtreqActivityDate = DateUtils.now

        svrtreq.sendTrigger = schedules ? SendTrigger.valueOf(schedules?.find()?.sendTrigger?.toString()) : SendTrigger.Now
        if (svrtreq.sendTrigger == SendTrigger.AfterDegreeAwarded) {
            svrtreq.svrtreqDateInd = "R4"
        }

        // Check for a send date first and make sure to apply that
        getSendDate(originalXml)

        TranscriptSchedule schedule = null
        if (schedules) {
            schedule = schedules.first()
        }

        schedule?.setSendDate(svrtreq.svrtreqSendDate)

        // Get the MohawkId if possible
        if (pidm) {
            svrtreq.svrtreqId = Spriden.findBySpridenPidmAndSpridenChangeIndIsNull(pidm).spridenId
        }

        svrtreq.save(flush: false)
    }

    /**
     * Generate and return an XML response object from this request. This will also set the responseXml property in
     * the current request object
     */
    XmlResponse generateResponse() {
        try {
            XmlTranscript xmlTranscript = new XmlTranscript(transcriptRequest: this)

            try {
                // Check conditions that might prevent sending a transcript
                if (!xmlTranscript.validate()) {
                    requestStatus = RequestStatus.Error
                    errorType = ErrorType.XmlGenerationError
                    xmlTranscript.errors.allErrors.each { log.error("Validation error: $it") }
                    throw new RuntimeException("XML Transcript object failed validation")
                } else if (!xmlTranscript.hasAcademicHistory) {
                    if (requestStatus != RequestStatus.ReadyToSendDeferredResponse) {
                        requestStatus = RequestStatus.ReadyToSendResponse
                    }
                    throw new AcademicHistoryNotFoundException()
                } else {
                    try {
                        XmlResponse response = new XmlResponse(transcriptRequest: this)
                        if (requestStatus == RequestStatus.ReadyToSendDeferredResponse) {
                            response.setOcasResponseStatus(ResponseStatus.Hold)
                            response.setNoteMessage("Suspended")
                        } else {
                            response.setOcasResponseStatus(extractResponseStatusFromSvrtreq())
                            if (extractResponseNoteFromSvrtreq() != null) {
                                response.setNoteMessage(extractResponseNoteFromSvrtreq())
                            }
                        }
                        response.setHoldReason(extractHoldReasonFromSvrtreq())
                        responseXml = response.xml

                        response

                    }
                    catch (Exception respHoldException) {
                        log.error("transcript response hold error: " + respHoldException.toString())
                    }
                }
            }
            catch (AcademicHistoryNotFoundException academicException) {
                XmlResponse response = new XmlResponse(transcriptRequest: this)
                if (requestStatus == RequestStatus.ReadyToSendDeferredResponse) {
                    response.setOcasResponseStatus(ResponseStatus.Hold)
                    response.setNoteMessage("Suspended")
                } else {
                    response.setOcasResponseStatus(extractResponseStatusFromSvrtreq())
                    response.setNoteMessage("Verify")
                }
                response.setHoldReason(HoldReason.Other)
                response.getXml()
                response.getRawXml()
                def modifiedResponse = response
                responseXml = response.xml
                modifiedResponse

            }
            catch (Exception respAcademicException) {
                log.error("transcript response academic history error: " + respAcademicException.toString())
            }
        }
        catch (Exception respGeneralException) {
            log.error("transcript response general error: " + respGeneralException.toString())
        }
    }

    /**
     * Generate and return an XML transcript object from this request.
     */
    XmlTranscript generateTranscript() {
        XmlTranscript xmlTranscript = new XmlTranscript(transcriptRequest: this)

        // Check conditions that might prevent sending a transcript
        if (!xmlTranscript.validate()) {
            requestStatus = RequestStatus.Error
            errorType = ErrorType.XmlGenerationError
            xmlTranscript.errors.allErrors.each { log.error("Validation error: $it") }
            throw new RuntimeException("XML Transcript object failed validation")
        } else if (!xmlTranscript.hasAcademicHistory) {
            requestStatus = RequestStatus.ReadyToSendResponse
            throw new AcademicHistoryNotFoundException()
        }

        responseXml = xmlTranscript.xml

        xmlTranscript
    }

    /**
     * If we need human eyes on this request, we can flag it as such.
     */
    void flagForManualIntervention() {
        svrtreq.responseType = ResponseType.Incomplete
        svrtreq.verificationStage = VerificationStage.NotStarted
        svrtreq.save()
    }

    /**
     * Helper method. Translate the information in the Svrtreq object into the corresponding request status
     *
     * @return
     */
    private RequestStatus extractRequestStatusFromSvrtreq() {
        if (!svrtreq) return null

        // If a response type is set, return the corresponding result.
        switch (svrtreq.responseType) {
            case ResponseType.Transcript:
                return RequestStatus.ReadyToSendTranscript
            case ResponseType.Refusal:
                return svrtreq.refusalReason ? RequestStatus.Refusal : RequestStatus.ManualVerify
            case ResponseType.Incomplete:
            case null:
                break
            default:
                throw new IllegalEnumValueException(ResponseType, svrtreq.responseType)
        }

        if (requestStatus == RequestStatus.Deferred && responseSent == false) {
            getSendDate(originalXml)


            svrtreq.setVerificationStage(VerificationStage.Complete)
            if (svrtreq.svrtreqDateInd == 'IT') {
                svrtreq.svrtreqDateInd == ''
            }

            return RequestStatus.ReadyToSendDeferredResponse
        }

        // Otherwise check to see if verification has finished
        switch (svrtreq.verificationStage) {
            case VerificationStage.NotStarted:
                if (!requestStatus || (requestStatus == RequestStatus.New)){
                    return RequestStatus.New
                }
                else{
                    return RequestStatus.Validating
                }
            case VerificationStage.Cancelled:
            case null:
                return RequestStatus.New
            case VerificationStage.DateCheck:
                // Term could be missing from GTVSDAX in this case
                log.info("Possible term codes in Banner issue")
            case VerificationStage.HoldCheck:
            case VerificationStage.UserMatchCheck:
                // no response
                return RequestStatus.Validating
            case VerificationStage.Complete:
                // Some sort of extra processing required
                // This might be cause for concern in the future
                return RequestStatus.Deferred
            default:
                throw new IllegalEnumValueException(VerificationStage, svrtreq.verificationStage)
        }
    }

    private void getSendDate(String originalXml) {
        GPathResult xmlParse = XmlSlurper.newInstance().parseText(originalXml)
        GPathResult recipient = xmlParse.Request.Recipient.TranscriptHold.HoldType[0]
        if (recipient == RecipientSchedule.AfterSpecifiedDate) {
            Date sendDate = TranscriptSchedule.parseSendDate(xmlParse.Request.Recipient.TranscriptHold.ReleaseDate[0].toString())
            this.svrtreq.setSvrtreqSendDate(sendDate)
            this.svrtreq.setRefusalReason(RefusalReason.Suspended)
            this.requestStatus = RequestStatus.ReadyToSendDeferredResponse
        } else if (recipient == RecipientSchedule.AfterSpecifiedTerm) {
            def sendTerm = TranscriptSchedule.parseSendTerm(xmlParse.Request.Recipient.TranscriptHold.SessionName[0].toString())
            def possibleDate
            switch (sendTerm.substring(4, 5)) {
                case '1':
                    // Winter
                    possibleDate = sendTerm.substring(0, 4) + "-04-30"
                    break
                case '2':
                    // Spring
                    possibleDate = sendTerm.substring(0, 4) + "-08-31"
                    break
                case '3':
                    // Fall
                    possibleDate = sendTerm.substring(0, 4) + "-12-31"
                    break
                default:
                    log.error("Missing term code season")
                    possibleDate = null
                    break
            }
            if (possibleDate != null) {
                Date finalDate = Date.parse('yyyy-MM-dd', possibleDate)
                this.svrtreq.svrtreqActivityDate = DateUtils.now
                this.svrtreq.setSvrtreqSendDate(finalDate)
                this.svrtreq.setRefusalReason(RefusalReason.Suspended)
                this.requestStatus = RequestStatus.ReadyToSendDeferredResponse
            } else {
                log.error("Send date fetching failed")
            }

        } else if (recipient != RecipientSchedule.AfterDegreeAwarded) {
            // For AfterDegreeAwarded, the RO will set this manually
            // When we send back to the RO, they still need a send date
            this.svrtreq.setSvrtreqSendDate(DateUtils.now)
        }
    }

    /**
     * Helper method. Translate the information in the Svrtreq object into the corresponding response status
     *
     * @return
     */
    private ResponseStatus extractResponseStatusFromSvrtreq() {
        if (!svrtreq) return null

        // If a response type is specified, return the corresponding response status
        switch (svrtreq.responseType) {
            case ResponseType.Transcript:
                return ResponseStatus.TranscriptSent
            case ResponseType.Refusal:
                // We're sending a refusal. Check the reason code
                if (svrtreq.refusalReason?.isHold()) return ResponseStatus.Hold

                switch (svrtreq.refusalReason) {
                    case RefusalReason.Cancellation:
                        return ResponseStatus.Canceled
                    case RefusalReason.NotFound:
                        return ResponseStatus.NoRecord
                    case RefusalReason.Deceased:
                        return ResponseStatus.Deceased
                    default:
                        return ResponseStatus.Hold
                }
            case ResponseType.Incomplete:
            case null:
                // continue on to check verification status
                break
            default:
                throw new IllegalEnumValueException(ResponseType, svrtreq.responseType)
        }

        // Check to see if verification has finished
        switch (svrtreq.verificationStage) {
        // Consider changing this later, it could prevent proper response types from being generated (esp with DateCheck)
            case VerificationStage.HoldCheck:
            case VerificationStage.DateCheck:
            case VerificationStage.NotStarted:
            case VerificationStage.Cancelled:
            case VerificationStage.UserMatchCheck:
            case null:
                // Verification incomplete; no response yet
                return null
            case VerificationStage.Complete:
                // Request verification is complete; continue on to check results
                break
            default:
                throw new IllegalEnumValueException(VerificationStage, svrtreq.verificationStage)
        }

        // Have we matched the user?
        switch (svrtreq.userMatchResult) {
            case UserMatchResult.DuplicatesFound:
                return ResponseStatus.MultipleMatch
            case UserMatchResult.NoMatchFound:
                return ResponseStatus.NoRecord
            case UserMatchResult.MatchFound:
            case null:
                // No user match issues; continue on
                break
            default:
                throw new IllegalEnumValueException(UserMatchResult, svrtreq.userMatchResult)
        }

        // Are there any holds?
        switch (svrtreq.holdStatus) {
            case HoldStatus.Hold:
            case HoldStatus.BalanceOwingHold:
                return ResponseStatus.Hold
            case HoldStatus.HoldOverridden:
            case HoldStatus.NoHold:
            case null:
                // No hold found; continue on
                break
            default:
                throw new IllegalEnumValueException(HoldStatus, svrtreq.holdStatus)
        }

        return ResponseStatus.TranscriptRequestReceived
    }

    private HoldReason extractHoldReasonFromSvrtreq() {
        if (!svrtreq) return null

        // Even without a hold status, there might be a relevant refusal reason
        if (svrtreq.responseType == ResponseType.Refusal && svrtreq.refusalReason?.isHold())
            return HoldReason.Financial
        //  This special case is to prevent accidental financial holds being assigned  2023-06-26

        switch (svrtreq.holdStatus) {
            case HoldStatus.BalanceOwingHold:
                return HoldReason.Financial
            case HoldStatus.Hold:
                // An incoming OCAS Hold should always return financial 2022-09-01
                // Unless it's handled in a separate method (ex: No Academic History)
                return HoldReason.Other
            default:
                if (svrtreq.refusalReason?.isHold()) return HoldReason.Other
                // something else; continue on
                break
        }

        return null
    }

    private String extractResponseNoteFromSvrtreq() {
        if (getRequestStatus() == RequestStatus.Refusal && getSvrtreq()?.refusalReason) {
            if (svrtreq.refusalReason == RefusalReason.NoAcademicData)
                return OcasApi.NOTEMESSAGE_NO_ACADEMIC_HISTORY
        }
        return null
    }

    void setXml(String pescXml) {
        def thiz = this
        // Populate object
        thiz.originalXml = pescXml

        XmlSlurper.newInstance().parseText(pescXml).with { xmlRoot ->
            thiz.ocasApplNumber = xmlRoot.Request.RequestedStudent.Person.AgencyAssignedID.toString()
            xmlRoot.Request.RequestedStudent.Person.AgencyIdentifier.each { identifier ->

                if (identifier.AgencyName == 'Ontario Education Number')
                    thiz.oen = identifier.AgencyAssignedID.toString()

                if (identifier.AgencyName == 'OCAS Application Number') {
                    thiz.ocasApplNumber = identifier.AgencyAssignedID.toString()
                }
            }

            // Create schedule objects
            createSchedules(xmlRoot)
        }
    }

    private void createSchedules(GPathResult xmlObject) {
        // Remove any existing send schedules so we can enter the new ones
        schedules*.delete()

        // there may be more than one schedule, but there may also be duplicates with different recipients
        // So we use toSet() to eliminate duplicates
        schedules = xmlObject.Request.Recipient.collect { GPathResult recipient ->
            new TranscriptSchedule(transcriptRequest: this, xml: recipient)
        }.toSet()
    }

    /**
     * Before creating the request record for the first time, generate a Svrtreq record for it
     */
    void beforeInsert() { saveSvrtreq() }
}
