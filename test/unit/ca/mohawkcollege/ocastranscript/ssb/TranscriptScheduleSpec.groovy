package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.RecipientSchedule
import ca.mohawkcollege.ocastranscript.baseline.Svrtnte
import ca.mohawkcollege.ocastranscript.baseline.Term
import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Shared
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import java.text.SimpleDateFormat

@TestFor(TranscriptSchedule)
@Mock([Term])
@ConfineMetaClassChanges([Term])
class TranscriptScheduleSpec extends XmlSpecification {

    @Shared
    TranscriptRequest aRequest
    @Shared
    TranscriptRequest bRequest

    @Shared
    String pastTerm = "201515"
    @Shared
    String endingTerm = "202015"
    @Shared
    String currentTerm = "202515"
    @Shared
    String newTerm = "203025"
    @Shared
    String futureTerm = "203525"

    @Shared
    Date past
    @Shared
    Date yesterday
    @Shared
    Date today
    @Shared
    Date tomorrow
    @Shared
    Date future

    @Shared
    Map<Date, String> termComments = [
            (pastTerm)   : "term ending yesterday",
            (endingTerm) : "term ending today",
            (currentTerm): "current term",
            (newTerm)    : "term starting today",
            (futureTerm) : "term starting tomorrow"
    ]
    @Shared
    Map<Date, String> dateComments
    @Shared
    Map<Boolean, String> sendComments = [(true): "sends", (false): "does not send"]
    @Shared
    Map<Boolean, String> degreeComments = [(true): "degree awarded", (false): "degree not awarded"]
    @Shared
    Map<Boolean, String> courseComments = [(true): "courses in progress", (false): "no courses in progress"]

    void setupSpec() {
        // define some useful values
        past = DateUtils.getPreviousMidnight(10)
        yesterday = DateUtils.getPreviousMidnight(1)
        today = DateUtils.previousMidnight
        tomorrow = DateUtils.nextMidnight
        future = DateUtils.getNextMidnight(10)
        dateComments = [
                (past)     : "past date",
                (yesterday): "yesterday's date",
                (today)    : "current date",
                (tomorrow) : "tomorrow's date",
                (future)   : "future date"]
    }

    void setup() {
        aRequest = GroovyMock(TranscriptRequest) {
            asBoolean() >> true
            getId() >> 1L
        }
        bRequest = GroovyMock(TranscriptRequest) {
            asBoolean() >> true
            getId() >> 3L
        }

        GroovyMock(Term, global: true)
        Term.findByStvtermCode(pastTerm) >> GroovyMock(Term) {
            getStvtermCode() >> pastTerm
            getStvtermStartDate() >> past
            getStvtermEndDate() >> yesterday
            toString() >> termComments[pastTerm]
            asBoolean() >> true
        }
        Term.findByStvtermCode(newTerm) >> GroovyMock(Term) {
            getStvtermCode() >> newTerm
            getStvtermStartDate() >> today
            getStvtermEndDate() >> future
            toString() >> termComments[newTerm]
            asBoolean() >> true
        }
        Term.findByStvtermCode(currentTerm) >> GroovyMock(Term) {
            getStvtermCode() >> currentTerm
            getStvtermStartDate() >> past
            getStvtermEndDate() >> future
            toString() >> termComments[currentTerm]
            asBoolean() >> true
        }
        Term.findByStvtermCode(endingTerm) >> GroovyMock(Term) {
            getStvtermCode() >> endingTerm
            getStvtermStartDate() >> past
            getStvtermEndDate() >> today
            toString() >> termComments[endingTerm]
            asBoolean() >> true
        }
        Term.findByStvtermCode(futureTerm) >> GroovyMock(Term) {
            getStvtermCode() >> futureTerm
            getStvtermStartDate() >> tomorrow
            getStvtermEndDate() >> future
            toString() >> termComments[futureTerm]
            asBoolean() >> true
        }
    }

    @Unroll
    void "test TranscriptRequestSchedule validation when #testCase"() {
        given: "a domain object populated with test values"
        domain.transcriptRequest = hasRequest ? GroovyMock(TranscriptRequest) : null
        domain.sent = sent
        domain.sendTrigger = processSchedule

        when: "we run validation on the object"
        def foundValid = domain.validate()
        def errors = domain.errors.allErrors

        then: "we get the validation result we were expecting"
        noExceptionThrown()
        expectValid == !(errors)
        foundValid == expectValid

        where:
        hasRequest | sent  | processScheduleString | expectValid | testCase
        false      | false | 'Now'                 | false       | 'transcriptRequest is null'
        1          | true  | null                  | false       | 'sendTrigger is null'
        1          | false | 'Now'                 | true        | 'All Valid'
        processSchedule = processScheduleString ? RecipientSchedule.valueOf(processScheduleString) : null
    }

//    @Unroll
//    void "test validation for trigger #triggerString and #testCase"() {
//        given: "a domain object populated with test values"
//        domain.transcriptRequest = GroovyMock(TranscriptRequest)
//        domain.sendTrigger = processSchedule
//        domain.sendDate = sendDate
//        domain.sendTermCode = sendTermCode
//
//        when: "we run validation on the object"
//        def foundValid = domain.validate()
//        def errors = domain.errors.allErrors
//
//        then: "we get the validation result we were expecting"
//        noExceptionThrown()
//        expectError == (errors as boolean)
//        foundValid == !expectError
//
//        where:
//        testArray << [[true, false], [true, false], RecipientSchedule.values()].combinations()
//        sendDate = testArray[0] ? new Date() : null as Date
//        sendTermCode = testArray[1] ? "200010" : null as String
//        processSchedule = testArray[2] as RecipientSchedule
//        // There are a limited number of error conditions
//        expectError = (processSchedule == RecipientSchedule.AfterSpecifiedDate && !sendDate) || (processSchedule == RecipientSchedule.AfterSpecifiedTerm && !sendTermCode)
//    }

    @Unroll
    void "test that setting the xml property with holdType=#holdType populates other properties accordingly"() {
        given: "an XML fragment representing an AcRec:RecipientType element"
        def xmlParse = XmlSlurper.newInstance().parseText(xmlRecipient)
        Term.findByStvtermStartDateLessThanEqualsAndStvtermEndDateGreaterThanEquals(today, today) >> GroovyMock(Term) {
            getStvtermCode() >> currentTerm
            toString() >> currentTerm
            asBoolean() >> true
        }

        when: "we set the schedule's xml property"
        domain.transcriptRequest = GroovyMock(TranscriptRequest) { getRequestDate() >> today }
        domain.xml = xmlParse

        then: "its other properties are as we expect"
        noExceptionThrown()
        domain.sendTrigger == expectSendTrigger
        domain.triggerParameter == expectTriggerParameter

        where:
        holdType             | session  | releaseDate  | expectSendTriggerText | expectTriggerParameter
        "Now"                | null     | null         | "Now"                 | null
        "AfterDegreeAwarded" | null     | null         | "AfterDegreeAwarded"  | null
        "AfterSpecifiedDate" | null     | '2020-10-31' | "AfterSpecifiedDate"  | "2020-10-31"
        "AfterSpecifiedTerm" | 'F 2020' | null         | "AfterSpecifiedTerm"  | "202030"
        expectSendTrigger = RecipientSchedule.valueOf(expectSendTriggerText) as RecipientSchedule

        holdChildren = [
                buildTag("HoldType", holdType),
                (session ? buildTag("SessionName", session) : null),
                (releaseDate ? buildTag("ReleaseDate", releaseDate) : null),
                buildTag("NoteMessage", randomText)
        ].findAll()
        xmlTranscriptHold = buildTag("TranscriptHold", holdChildren)
        xmlRecipient = buildTag("Recipient", xmlTranscriptHold)
    }

    void "test that if already sent, readyToSend is always false regardless of trigger type"() {
        given: "a schedule that has already been sent"
        makeDomainValid()
        domain.sent = true
        domain.sendTrigger = trigger

        expect: "it is not ready to be sent"
        !domain.isReadyToSend(GroovyMock(TranscriptRequest))

        where:
        trigger << (RecipientSchedule.values() as List<RecipientSchedule>)
    }

    @Unroll
    void "test that readyToSend #sendComment when unsent and with trigger type #triggerText"() {
        given: "a set of conditions affecting the sending of a schedule"
        makeDomainValid()
        domain.sent = false
        domain.sendTrigger = sendTrigger

        expect: "the isReadyToSend property as predicted"
        domain.isReadyToSend(GroovyMock(TranscriptRequest)) == expectedResult

        where:
        triggerText | expectedResult
        "Now"       | true
        "Other"     | false

        sendComment = sendComments[expectedResult]
        sendTrigger = RecipientSchedule.fromValue(triggerText) as RecipientSchedule
    }


    @Unroll
    @ConfineMetaClassChanges([InProgressCourses])
    void "test that readyToSend after #termComment #sendComment with #courseComment"() {
        GroovyMock(InProgressCourses, global: true)
        InProgressCourses./findBy.*/(*_) >> GroovyMock(InProgressCourses) { asBoolean() >> inProgress }

        given: "a set of conditions affecting the sending of a schedule"
        makeDomainValid()
        domain.sent = false
        domain.sendTrigger = RecipientSchedule.AfterSpecifiedTerm
        domain.sendTermCode = parameter
        domain.sendDate = dateParameter

        expect: "the isReadyToSend property as predicted"
        domain.isReadyToSend(GroovyMock(TranscriptRequest)) == expectedResult

        where:
        parameter   | inProgress | expectedResult | dateParameter
        pastTerm    | true       | false          | tomorrow
        pastTerm    | false      | true           | yesterday
        newTerm     | true       | false          | tomorrow
        newTerm     | false      | false          | tomorrow
        currentTerm | true       | false          | tomorrow
        currentTerm | false      | false          | tomorrow
        endingTerm  | true       | false          | tomorrow
        endingTerm  | false      | false          | tomorrow
        futureTerm  | true       | false          | tomorrow
        futureTerm  | false      | false          | tomorrow
        sendComment = sendComments[expectedResult]
        courseComment = courseComments[inProgress]
        termComment = parameter
    }

    @Unroll
    void "test that readyToSend after #dateComment #sendComment"() {
        given: "a set of conditions affecting the sendability of a schedule"
        makeDomainValid()
        domain.sent = false
        domain.sendTrigger = RecipientSchedule.AfterSpecifiedDate
        domain.sendDate = parameter

        expect: "the isReadyToSend property as predicted"
        domain.isReadyToSend(GroovyMock(TranscriptRequest)) == expectedResult

        where:
        parameter | expectedResult
        past      | true
        yesterday | true
        today     | true
        tomorrow  | false
        future    | false
        sendComment = sendComments[expectedResult]
        dateComment = dateComments[parameter]
    }

    @Unroll
    void "test that equality test fails when compared to #comment"() {
        expect: "inequality between variables"
        (domain as Object) != otherObject

        where:
        otherObject      | comment
        null             | "null"
        "bad comparison" | "string"
    }

    @Unroll
    void "test that equality #expectComment when #comment"() {
        given: "an object to compare with"
        def other = new TranscriptSchedule()

        when: "domain properties set to test values"
        domain.transcriptRequest = myRequest
        domain.sent = mySent
        domain.sendTrigger = myTrigger

        and: "other object properties set"
        other.transcriptRequest = urRequest
        other.sent = urSent
        other.sendTrigger = urTrigger

        and: "we test equality"
        def foundEqual = (domain.equals(other))

        then: "equality is as expected"
        expectEqual == foundEqual

        where:
        myRequest | urRequest | mySent | urSent | myTriggerText | urTriggerText        | expectEqual | comment
        aRequest  | aRequest  | true   | true   | "Now"         | "Now"                | true        | "all properties match"
        aRequest  | bRequest  | true   | true   | "Now"         | "Now"                | false       | "request mismatch"
        aRequest  | null      | true   | true   | "Now"         | "Now"                | false       | "first request null"
        null      | aRequest  | true   | true   | "Now"         | "Now"                | false       | "second request null"
        null      | null      | true   | true   | "Now"         | "Now"                | true        | "both requests null"
        aRequest  | aRequest  | true   | false  | "Now"         | "Now"                | true        | "mismatch, sent/unsent"
        aRequest  | aRequest  | false  | true   | "Now"         | "Now"                | true        | "mismatch, unsent/sent"
        aRequest  | aRequest  | true   | true   | "Now"         | "AfterDegreeAwarded" | false       | "trigger mismatch"
        aRequest  | aRequest  | true   | true   | null          | "Now"                | false       | "first trigger null"
        aRequest  | aRequest  | true   | true   | "Now"         | null                 | false       | "second trigger null"
        aRequest  | aRequest  | true   | true   | null          | null                 | true        | "both triggers null"

        myTrigger = myTriggerText ? RecipientSchedule.valueOf(myTriggerText) : null
        urTrigger = urTriggerText ? RecipientSchedule.valueOf(urTriggerText) : null
        expectComment = expectEqual ? "passes" : "fails"
    }

    @Unroll
    @ConfineMetaClassChanges([Svrtnte])
    void "a Svrtnte object is created and populated correctly for trigger=[#scheduleTrigger]"() {
        GroovyMock(Svrtnte, global: true)
        1 * Svrtnte."<init>"({ it ->
            it.svrtnteBgn02 == fakeRequestTrackingId && it.svrtnteNote == expectedNoteString
        } as Map) >> { GroovyMock(Svrtnte) { asBoolean() >> true } }
        def pattern = "yyyy-MM-dd"
        def input = "2019-02-28"
        def dateTimeNow = new SimpleDateFormat(pattern).parse(input)

        when: "we populate a TranscriptSchedule"
        //domain.triggerParameter = "202230"
        domain.sendTrigger = scheduleTrigger
        domain.sendTermCode = futureTerm
        domain.sendDate = dateTimeNow
        domain.transcriptRequest = GroovyMock(TranscriptRequest) { getRequestTrackingId() >> fakeRequestTrackingId }

        and: "we get the Svrtnte property"
        domain.generateSvrtnte()

        then: "all required Svrtnte properties have been set properly"
        noExceptionThrown()

        where:
        trigger              | expectedNoteString
        "Now"                | "Now"
        "AfterDegreeAwarded" | "AfterDegreeAwarded"
        "AfterSpecifiedTerm" | "TERM=A2035"
        "AfterSpecifiedDate" | "AfterSpecifiedDate=2019-02-28"

        fakeRequestTrackingId = RandomStringUtils.randomAlphanumeric(6)
        scheduleTrigger = RecipientSchedule.valueOf(trigger)
    }

    @Unroll
    void "valid OCAS session #ocasSession is converted to Mohawk term #mohawkTerm (#comment)"() {
        given: "an XML fragment representing an AcRec:RecipientType element"
        when: "we set the schedule's xml property"
        domain.xml = XmlSlurper.newInstance().parseText(xmlRecipient)

        then: "the resultant object has the expected Mohawk term"
        noExceptionThrown()
        domain.triggerParameter == mohawkTerm

        where:
        ocasSession | mohawkTerm | comment
        "W 2019"    | "202010"   | "End of Winter May 2020"
        "A 2019"    | "202020"   | "End of Spring/Summer Sept 2020"
        "S 2019"    | "202020"   | "End of Spring Sept 2020"
        "F 2019"    | "201930"   | "End of Fall January 2020"

        holdChildren = [
                buildTag("HoldType", "AfterSpecifiedTerm"),
                buildTag("SessionName", ocasSession)
        ]
        xmlTranscriptHold = buildTag("TranscriptHold", holdChildren)
        xmlRecipient = buildTag("Recipient", xmlTranscriptHold)
    }

    void "an invalid incoming OCAS session causes an exception to be thrown"() {
        given: "an XML fragment representing an AcRec:RecipientType element"
        when: "we set the schedule's xml property"
        domain.xml = XmlSlurper.newInstance().parseText(xmlRecipient)

        then: "a runtime exception is thrown"
        thrown(RuntimeException)

        where:
        ocasSession << ["Q 1234", "F_2345", "S-9876", "W 789", "I'm a little teapot"]

        holdChildren = [
                buildTag("HoldType", "AfterSpecifiedTerm"),
                buildTag("SessionName", ocasSession)
        ]
        xmlTranscriptHold = buildTag("TranscriptHold", holdChildren)
        xmlRecipient = buildTag("Recipient", xmlTranscriptHold)
    }

    /**
     * Given a domain object, set minimal property values such that it will pass validation
     */
    private void makeDomainValid() {
        domain.transcriptRequest = GroovyMock(TranscriptRequest)
        domain.sendTrigger = RecipientSchedule.Other
        assert domain.validate()
    }
}
