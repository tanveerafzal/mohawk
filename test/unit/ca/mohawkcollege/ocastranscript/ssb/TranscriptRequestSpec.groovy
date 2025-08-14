package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.bannerlib.base.UserHistory
import ca.mohawkcollege.ocastranscript.AcademicHistoryNotFoundException
import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.RequestStatus
import ca.mohawkcollege.ocastranscript.baseline.*
import ca.mohawkcollege.ocastranscript.xml.XmlResponse
import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import ca.mohawkcollege.ocastranscript.xml.XmlTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.DocumentType
import ca.mohawkcollege.ocastranscript.xml.pesc.HoldReason
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.math.RandomUtils
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@TestFor(TranscriptRequest)
@Mock([Svrtreq, Spriden, TranscriptSchedule, AcademicHistory, UserHistory])
@ConfineMetaClassChanges([XmlResponse])
class TranscriptRequestSpec extends XmlSpecification {

    def setup() {
        // general Spriden mock
        GroovyMock(Spriden, global: true)
        Spriden./findBy.*/(*_) >> GroovyMock(Spriden)
    }

    @Unroll
    void "test TranscriptRequest validation #outcome when #testCase"() {
        given: "a domain object populated with test values"
        domain.requestTrackingId = requestTrackingId

        and: "a Svrtreq mock with various properties set"
        domain.svrtreq = GroovyMock(Svrtreq) { asBoolean() >> true }
        domain.requestType = requestType
        domain.requestDate = requestDate
        domain.pidm = pidm
        domain.requestStatus = requestStatus
        domain.errorType = errorType
        domain.oen = oen
        domain.ocasApplNumber = ocasNo
        domain.originalXml = originalXml
        domain.responseXml = responseXml
        domain.responseSent = responseSent
        domain.responseStatus = responseStatus

        when: "we run validation on the object"
        def foundValid = domain.validate()
        def foundErrors = domain.errors.allErrors

        then: "we get the validation result we were expecting"
        noExceptionThrown()
        (expectValid == foundValid) || (foundErrors.each { println(it) } && false)

        where:
        testCase                   | requestTypeString | requestTrackingId | pidm | requestStatusString | errorTypeString | oen   | ocasNo | originalXml | responseXml | responseStatusText | expectValid
        'all properties populated' | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'request type is null'     | null              | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | false
        'request Id is null'       | 'Request'         | null              | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | false
        'pidm is null'             | 'Request'         | 'C20201209011800' | null | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'request status is null'   | 'Request'         | 'C20201209011800' | 234  | null                | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'error type is null'       | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | null            | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'oen is null'              | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | null  | '123'  | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'OCAS number is null'      | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | null   | '<xml/>'    | '<xml/>'    | 'Hold'             | true
        'original XML is null'     | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | null        | '<xml/>'    | 'Hold'             | true
        'response XML is null'     | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | null        | 'Hold'             | true
        'response status is null'  | 'Request'         | 'C20201209011800' | 234  | 'ResponseSent'      | 'OcasIdUnknown' | 'WWW' | '123'  | '<xml/>'    | '<xml/>'    | null               | true
        responseSent = false
        requestDate = new Date()
        recordDate = new Date()
        requestType = requestTypeString ? DocumentType.valueOf(requestTypeString) : null
        requestStatus = requestStatusString ? RequestStatus.valueOf(requestStatusString) : null
        errorType = errorTypeString ? ErrorType.valueOf(errorTypeString) : null
        responseStatus = responseStatusText ? ResponseStatus.valueOf(responseStatusText) : null
        outcome = expectValid ? "passes" : "fails"
    }

    @Unroll
    void "the setter for svrtreq populates properties correctly when #comment"() {
        given: "a domain object with nulls in various properties"
        domain.requestStatus = null
        domain.responseStatus = null
        domain.holdReason = null

        and: "a Svrtreq mock with various properties set"
        def mockSvrtreq = GroovyMock(Svrtreq) {
            asBoolean() >> true
            getSvrtreqId() >> mohawkId
            getVerificationStage() >> (verification ? VerificationStage.valueOf(verification) : null)
            getUserMatchResult() >> (userMatch ? UserMatchResult.valueOf(userMatch) : null)
            getHoldStatus() >> (hold ? HoldStatus.valueOf(hold) : null)
            getResponseType() >> (response ? ResponseType.valueOf(response) : null)
            getRefusalReason() >> (reason ? RefusalReason.valueOf(reason) : null)
        }

        when: "we set the svrtreq property"
        domain.svrtreq = mockSvrtreq

        def foundRequestStatus = domain.requestStatus
        def foundResponseStatus = domain.responseStatus
        def foundHoldReason = domain.holdReason

        then: "various properties have been set"
        foundRequestStatus == (xpRequest ? RequestStatus.valueOf(xpRequest) : null)
        foundResponseStatus == (xpResponse ? ResponseStatus.valueOf(xpResponse) : null)
        foundHoldReason == (xpHold ? HoldReason.valueOf(xpHold) : null)

        where:
        hold             | response     | reason            | xpRequest               | xpResponse                  | xpHold      | comment
        "NoHold"         | "Transcript" | null              | "ReadyToSendTranscript" | "TranscriptSent"            | null        | "no hold - transcript sent"
        "HoldOverridden" | "Transcript" | null              | "ReadyToSendTranscript" | "TranscriptSent"            | null        | "hold overridden - transcript sent"
        "NoHold"         | "Incomplete" | null              | "Deferred"              | "TranscriptRequestReceived" | null        | "response incomplete"
        "NoHold"         | "Refusal"    | "Cancellation"    | "Refusal"               | "Canceled"                  | null        | "refusal, cancellation"
        "NoHold"         | "Refusal"    | "NotFound"        | "Refusal"               | "NoRecord"                  | null        | "refusal, not found"
        "NoHold"         | "Refusal"    | "Deceased"        | "Refusal"               | "Deceased"                  | null        | "refusal, deceased"
        "NoHold"         | "Refusal"    | "ReIssue"         | "Refusal"               | "Hold"                      | "Financial" | "refusal, reissue"
        "NoHold"         | "Refusal"    | "Hold"            | "Refusal"               | "Hold"                      | "Financial" | "refusal, hold"
        "NoHold"         | "Refusal"    | "InformationCopy" | "Refusal"               | "Hold"                      | "Financial" | "refusal, information copy"
        "NoHold"         | "Refusal"    | "NoAcademicData"  | "Refusal"               | "Hold"                      | "Financial" | "refusal, no academic data"
        "NoHold"         | "Refusal"    | "Rejection"       | "Refusal"               | "Hold"                      | "Financial" | "refusal, rejection"
        "NoHold"         | "Refusal"    | "Suspended"       | "Refusal"               | "Hold"                      | "Financial" | "refusal, suspended"
        "NoHold"         | "Refusal"    | null              | "ManualVerify"          | "Hold"                      | null        | "refusal without reason"
        mohawkId = "abc"
        verification = "Complete"
        userMatch = "MatchFound"
    }

    void "no matter the verification stage is or its results, if there's a completion indicator of Transcript, then that overrides it"() {
        given: "a persisted request whose Svrtreq is of type 'Transcript'"
        domain.id = 1
        def mockSvrtreq = GroovyMock(Svrtreq) {
            asBoolean() >> true
            getSvrtreqId() >> "123"
            getHoldStatus() >> testValues[0]
            getRefusalReason() >> testValues[1]
            getResponseType() >> ResponseType.Transcript
        }
        domain.svrtreq = mockSvrtreq

        when: "we get the responseStatus and requestStatus from a transcript request"
        def responseStatus = domain.responseStatus
        def requestStatus = domain.requestStatus

        then: "the result depends only on the response type of the Svrtreq"
        0 * mockSvrtreq.getVerificationStage()
        0 * mockSvrtreq.getUserMatchResult()
        responseStatus == ResponseStatus.TranscriptSent
        requestStatus == RequestStatus.ReadyToSendTranscript

        where:
        testValues << [(HoldStatus.values() as List) + [null],
                       (RefusalReason.values() as List) + [null],].combinations()
    }

    void "no matter the verification stage is or its results, if there's a completion indicator of Refusal, then that overrides it"() {
        given: "a Svrtreq with a completion indicator of Refusal"
        def mockSvrtreq = GroovyMock(Svrtreq) {
            asBoolean() >> true
            getSvrtreqId() >> "123"
            getRefusalReason() >> testRefusalReason
            getResponseType() >> ResponseType.Refusal
        }

        and: "a persisted request with the Svrtreq"
        domain.id = 1
        domain.svrtreq = mockSvrtreq

        when: "we get the responseStatus and requestStatus from a transcript request"
        def responseStatus = domain.responseStatus
        def requestStatus = domain.requestStatus

        then: "the result depends only on the response type of the Svrtreq"
        0 * mockSvrtreq.getVerificationStage()
        0 * mockSvrtreq.getUserMatchResult()
        0 * mockSvrtreq.getHoldStatus()
        responseStatus == expectedResponseStatus
        requestStatus == RequestStatus.Refusal

        where:
        testRefusalReason << RefusalReason.values()

        expectedResponseStatus = [
                (RefusalReason.Cancellation): ResponseStatus.Canceled,
                (RefusalReason.NotFound)    : ResponseStatus.NoRecord,
                (RefusalReason.Deceased)    : ResponseStatus.Deceased
        ].get(testRefusalReason, ResponseStatus.Hold)
    }

    void "for response type = Incomplete, properties are derived from Svrtreq correctly"() {
        given: "a persisted request with an appropriate Svrtreq"
        domain.id = 1
        domain.svrtreq = GroovyMock(Svrtreq) {
            asBoolean() >> true
            getSvrtreqId() >> mohawkId
            getVerificationStage() >> verification
            getUserMatchResult() >> match
            getHoldStatus() >> hold
            getResponseType() >> ResponseType.Incomplete
            getRefusalReason() >> reason
        }

        expect:
        domain.requestStatus == expected["requestStatus"]
        domain.responseStatus == expected["responseStatus"]
        domain.holdReason == expected["holdReason"]

        where:
        testValues << [(VerificationStage.values() as List) - ["Complete" as VerificationStage],
                       (UserMatchResult.values() as List) + [null],
                       (HoldStatus.values() as List) + [null],
                       (RefusalReason.values() as List) + [null],
        ].combinations()

        mohawkId = "id"
        verification = testValues[0] as VerificationStage
        match = testValues[1] as UserMatchResult
        hold = testValues[2] as HoldStatus
        reason = testValues[3] as RefusalReason

        xpRequestStatus = [
                NotStarted    : RequestStatus.New,
                UserMatchCheck: RequestStatus.Validating,
                HoldCheck     : RequestStatus.Validating,
                DateCheck     : RequestStatus.Validating,
                Cancelled     : RequestStatus.New
        ][verification.toString()]
        xpResponseStatus = null
        // Even without an explicit hold, a refusal reason might exist
        xpHoldReason = ([
                BalanceOwingHold: HoldReason.Financial,
                Hold            : HoldReason.Other
        ].get(hold.toString())) ?: (reason?.isHold() ? HoldReason.Other : null)
        expected = [
                requestStatus : [
                        NotStarted    : RequestStatus.New,
                        UserMatchCheck: RequestStatus.Validating,
                        HoldCheck     : RequestStatus.Validating,
                        DateCheck     : RequestStatus.Validating,
                        Cancelled     : RequestStatus.New
                ][verification.toString()],
                responseStatus: xpResponseStatus as ResponseStatus,
                holdReason    : xpHoldReason as HoldReason
        ]
    }

    void "an existing request with a refusal in Svrtreq will always have refusal status"() {
        given: "an existing request"
        domain.id = 1

        and: "a Svrtreq mock with refusal response type"
        domain.svrtreq = GroovyMock(Svrtreq) {
            asBoolean() >> true
            getSvrtreqId() >> "id"
            getVerificationStage() >> VerificationStage.Complete
            getUserMatchResult() >> match
            getHoldStatus() >> hold
            getResponseType() >> response
            getRefusalReason() >> reason
        }

        expect: "request status matches expectations"
        domain.requestStatus == xpRequestStatus

        where: "reasons and other properties run the gamut"
        combo << ([UserMatchResult.values(), HoldStatus.values(), RefusalReason.values()].combinations())
        match = (combo as List)[0]
        hold = (combo as List)[1]
        response = ResponseType.Refusal
        reason = (combo as List)[2]

        xpRequestStatus = RequestStatus.Refusal
    }

    void "test that if we flag for manual verification then the Svrtreq object gets updated accordingly"() {
        given: "an Svrtreq mock"
        domain.svrtreq = GroovyMock(Svrtreq) { asBoolean() >> true }

        when: "we have our domain process the Svrtreq object"
        domain.flagForManualIntervention()

        then: "the Svrtreq gets its properties changed"
        noExceptionThrown()
        1 * domain.svrtreq.setResponseType(ResponseType.Incomplete)
    }

    @ConfineMetaClassChanges([XmlTranscript])
    void "test that when no academic history is found, generating a transcript throws an exception"() {
        given: "a request that says it's ready to send"
        domain.requestStatus = RequestStatus.ReadyToSendTranscript

        and: "it has no academic history"
        GroovyMock(XmlTranscript, global: true)
        new XmlTranscript(*_) >> GroovyMock(XmlTranscript) {
            asBoolean() >> true
            validate() >> true
            getHasAcademicHistory() >> false
        }

        when: "we call generateTranscript"
        domain.generateTranscript()

        then: "our exception is thrown"
        thrown(AcademicHistoryNotFoundException)
    }

    @ConfineMetaClassChanges([XmlTranscript])
    void "test that when academic history is found, generating a transcript succeeds"() {
        given: "a request that says it's ready to send"
        domain.requestStatus = RequestStatus.ReadyToSendTranscript

        and: "it has academic history"
        GroovyMock(XmlTranscript, global: true)
        new XmlTranscript(*_) >> GroovyMock(XmlTranscript) {
            asBoolean() >> true
            validate() >> true
            getHasAcademicHistory() >> true
        }

        when: "we call generateTranscript"
        def transcript = domain.generateTranscript()

        then: "no exception is thrown and transcript is generated"
        noExceptionThrown()
        transcript != null
    }

    @ConfineMetaClassChanges([XmlTranscript])
    void "test that when academic history is found, generating a transcript succeeds"() {
        given: "a request that says it's ready to send"
        domain.requestStatus = RequestStatus.ReadyToSendTranscript

        and: "it has academic history"
        GroovyMock(XmlTranscript, global: true)
        new XmlTranscript(*_) >> GroovyMock(XmlTranscript) {
            asBoolean() >> true
            validate() >> true
            getHasAcademicHistory() >> true
        }

        when: "we call generateTranscript"
        def transcript = domain.generateTranscript()

        then: "no exception is thrown and transcript is generated"
        noExceptionThrown()
        transcript != null
    }

    @Unroll
    void "test TranscriptRequest validation fails when #testCase"() {
        given: "a domain object populated with test values"
        domain.requestTrackingId = requestTrackingId
        domain.svrtreq = GroovyMock(Svrtreq) { asBoolean() >> true }
        domain.requestType = requestType
        domain.requestDate = new Date()
        domain.pidm = pidm
        domain.requestStatus = RequestStatus.ResponseSent
        domain.errorType = ErrorType.OcasIdUnknown
        domain.oen = oen
        domain.ocasApplNumber = ocasNo
        domain.originalXml = '<xml/>'
        domain.responseXml = '<xml/>'
        domain.responseSent = false
        domain.responseStatus = ResponseStatus.Hold

        when: "we run validation on the object"
        def foundValid = domain.validate()
        def foundErrors = domain.errors.allErrors

        then: "validation fails"
        !foundValid
        foundErrors.size() > 0

        where:
        testCase               | requestType          | requestTrackingId | pidm  | oen   | ocasNo
        'request type is null' | null                 | 'C20201209011800' | 234  | 'WWW'     | '123'
        'request Id is null'   | DocumentType.Request | null              | 234  | 'WWW'     | '123'
    }

    @ConfineMetaClassChanges([Svrtreq])
    @Unroll
    void "saving a new record populates the Svrtreq via XML [#testGender/#testAlternate/#testAttendance]"() {
        GroovyMock(Svrtreq, global: true)

        given: "a new unsaved request"
        domain.id = null
        domain.originalXml = domainXml

        when: "we prepare to save the domain"
        domain.beforeInsert()

        then: "we populated the Svrtreq from the domain's XML"
        1 * Svrtreq./find(OrCreate)?By.*/(*_) >> GroovyMock(Svrtreq) {
            asBoolean() >> true
            1 * setSvrtreqBirthDate(_ as Date)
            1 * setSvrtreqStudentNo_1(_ as String)
            1 * setSvrtreqSurname(_ as String)
            1 * setSvrtreqFirstmidname(_ as String)
            1 * setSvrtreqFirstname(_ as String)
            1 * setSvrtreqPrefix(_ as String)
            1 * setGender(testGender)
            1 * setSvrtreqSin(_ as String)
            (testAttendance ? 1 : 0) * setSvrtreqExitDate(!null)
            (testAlternate == "Former" ? 1 : 0) * setSvrtreqFormersurname(_ as String)
        }

        where:
        testArray << [Gender.values(), ["Former", randomText, null], [true, false]].combinations()
        testGender = testArray[0] as Gender
        testAlternate = testArray[1] as String
        testAttendance = testArray[2] as boolean

        domainXml = buildTag("TranscriptRequest", [Request: [RequestedStudent: [
                Person    : [
                        SchoolAssignedPersonID: randomText,
                        Name                  : [
                                NamePrefix: randomText,
                                FirstName : randomText,
                                MiddleName: randomText,
                                LastName  : randomText
                        ],
                        Birth                 : [BirthDate: "1234-01-23"],
                        Gender                : [GenderCode: testGender.toString()],
                        SIN                   : randomText,
                        AlternateName         : testAlternate ? [NameCode: testAlternate, LastName: randomText] : null
                ],
                Attendance: testAttendance ? [ExitDate: "2055-04-05"] : null
        ]]])
    }

    @ConfineMetaClassChanges([TranscriptSchedule])
    void "test that when parsing XML, TranscriptSchedule object(s) are created"() {
        GroovyMock(TranscriptSchedule, global: true)
        TranscriptSchedule."<init>"(*_) >> { argList ->
            GroovyMock(TranscriptSchedule) {
                asBoolean() >> true
            }
        }

        when: "we feed a valid XML string into our request"
        domain.xml = xmlString

        then: "schedules are created but not saved"
        domain.schedules.size() == scheduleCount

        where:
        scheduleCount << [0, 1, 5]

        xmlString = buildTag("TranscriptRequest", [
                buildTag("TransmissionData",
                        buildTag("DocumentTypeCode", DocumentType.Request.toString())),
                buildTag("Request", ([true] * scheduleCount).collect {
                    buildTag("Recipient", randomXml)
                })
        ])
    }

    @ConfineMetaClassChanges([TranscriptSchedule])
    void "test that when parsing XML, duplicate schedules get discarded"() {
        // Use multiple references to the same mock to simulate duplicates
        Map<String, TranscriptSchedule> tsMap = [:]

        GroovyMock(TranscriptSchedule, global: true)
        totalCount * TranscriptSchedule."<init>"(*_) >> { argList ->
            def xmlKey = argList.xml.toString()
            if (!tsMap.containsKey(xmlKey)) {
                tsMap[xmlKey] = GroovyMock(TranscriptSchedule) {
                    asBoolean() >> true
                }
            }
            tsMap[xmlKey]
        }

        when: "we feed a valid XML string into our request"
        println xmlString
        domain.xml = xmlString

        then: "the right number of schedules are saved in the parent object"
        domain.schedules.size() == expectedScheduleCount

        where:
        duplicateCounts << [
                [1, 1, 1, 1, 1], // no duplicates
                [3, 1, 1, 1], // one set of duplicates
                [4, 1, 5, 1, 7, 1, 2] // several sets of duplicates
        ]
        totalCount = duplicateCounts.sum()
        expectedScheduleCount = duplicateCounts.size()

        scheduleXmls = duplicateCounts.collect { Integer count ->
            // make sure duplicates are identical
            [buildTag("Recipient", RandomUtils.nextInt().toString())] * (count)
        }.flatten()

        xmlString = buildTag("TranscriptRequest", [
                buildTag("TransmissionData",
                        buildTag("DocumentTypeCode", DocumentType.Request.toString())),
                buildTag("Request", scheduleXmls)
        ])
    }
}
