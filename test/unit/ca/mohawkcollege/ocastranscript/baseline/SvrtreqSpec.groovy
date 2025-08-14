package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.ocastranscript.xml.SendTrigger
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(Svrtreq)
class SvrtreqSpec extends Specification {

    def testObject

    @Unroll
    void "test object validation #outcome when #testCase"() {

        when:
        testObject = new Svrtreq(
                svrtreqBgn02: requestId,
                svrtreqTransDate: transDate,
                sendTrigger: sendTrigger as SendTrigger,
                verificationStage: stage ? VerificationStage.fromValue(stage) as VerificationStage : null,
                holdStatus: HoldStatus.fromValue(hold) as HoldStatus,
                refusalReason: reason ? RefusalReason.fromValue(reason) as RefusalReason : null,
                responseType: responseType as ResponseType,
                svrtreqActivityDate: activityDate,
                userMatchResult: match ? UserMatchResult.fromValue(match) as UserMatchResult : null,
                requestPurpose: purpose ? RequestPurpose.fromValue(purpose) as RequestPurpose : null,
                svrtreqDataOrigin: dataOrigin,
                svrtreqUserId: userId
        )


        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors

        then:
        (foundValid == expectValid) || (errors.each { println(it) } && false)

        where:
        testCase                       | requestId | transDate  | trigger | stage | hold | reason | response | match | purpose | expectValid
        'all properties are populated' | 'abc'     | new Date() | 'R2'    | 'C'   | 'H'  | '01'   | '130'    | 'X'   | '13'    | true
        'request ID is null'           | null      | new Date() | 'R2'    | 'C'   | 'H'  | '01'   | '130'    | 'X'   | '13'    | false
        'transaction date is null'     | 'abc'     | null       | 'R2'    | 'C'   | 'H'  | '01'   | '130'    | 'X'   | '13'    | false
        'send trigger is null'         | 'abc'     | new Date() | null    | 'C'   | 'H'  | '01'   | '130'    | 'X'   | '13'    | false
        'verification stage is null'   | 'abc'     | new Date() | 'R2'    | null  | 'H'  | '01'   | '130'    | 'X'   | '13'    | false
        'hold is null'                 | 'abc'     | new Date() | 'R2'    | 'C'   | null | '01'   | '130'    | 'X'   | '13'    | true
        'refusal reason is null'       | 'abc'     | new Date() | 'R2'    | 'C'   | 'H'  | null   | '130'    | 'X'   | '13'    | true
        'completion status is null'    | 'abc'     | new Date() | 'R2'    | 'C'   | 'H'  | '01'   | null     | 'X'   | '13'    | false
        'user match status is null'    | 'abc'     | new Date() | 'R2'    | 'C'   | 'H'  | '01'   | '147'    | null  | '13'    | true
        'request purpose is null'      | 'abc'     | new Date() | 'R2'    | 'C'   | 'H'  | '01'   | '147'    | 'X'   | null    | false
        'all properties are null'      | null      | null       | null    | null  | null | null   | null     | null  | null    | false
        sendTrigger = trigger ? SendTrigger.fromValue(trigger) : null
        responseType = response ? ResponseType.fromValue(response) : null
        activityDate = new Date()
        dataOrigin = 'ABC'
        userId = 'DEF'
        outcome = expectValid ? "passes" : "fails"
    }

    void "test that purpose code is valid by default"() {
        expect: "in a default-initialized Svrtreq object, request purpose is valid"
        domain.validate(['requestPurpose'])

        and: "its value is Create"
        domain.requestPurpose == RequestPurpose.Create
    }
}