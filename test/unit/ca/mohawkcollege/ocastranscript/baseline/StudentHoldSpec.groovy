package ca.mohawkcollege.ocastranscript.baseline

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(StudentHold)
class StudentHoldSpec extends Specification {

    def testObject

    @Unroll
    void "test validation when #testCase"() {
        when:
        testObject = new StudentHold(
                pidm: pidm,
                holdCode: holdCode ? GroovyMock(HoldCode) : null,
                sprholdUser: user,
                sprholdFromDate: fromDate ? new Date() : null,
                sprholdToDate: toDate ? new Date() : null,
                sprholdReleaseInd: release,
                sprholdActivityDate: activity ? new Date() : null
        )
        def foundValid = testObject.validate()

        then:
        (foundValid == expectValid) || (testObject.errors.allErrors.each { println(it) } && false)

        where:
        testCase                | pidm | holdCode | user | activity | fromDate | toDate | release | expectValid
        'all fields populated'  | 123  | 'CL'     | 'su' | true     | true     | true   | 'N'     | true
        'pidm is null'          | null | 'CL'     | 'ed' | true     | true     | true   | 'N'     | false
        'holdCode is null'      | 123  | null     | 'ed' | true     | true     | true   | 'N'     | false
        'user is null'          | 123  | 'CL'     | null | true     | true     | true   | 'N'     | false
        'activity date is null' | 123  | 'CL'     | 'ed' | false    | true     | true   | 'N'     | false
        'fromDate is null'      | 123  | 'CL'     | 'ed' | true     | false    | true   | 'N'     | false
        'toDate is null'        | 123  | 'CL'     | 'ed' | true     | true     | false  | 'N'     | false
        'release is null'       | 123  | 'CL'     | 'ed' | true     | true     | true   | null    | false
    }
}