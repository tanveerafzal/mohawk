package ca.mohawkcollege.ocastranscript.baseline


import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(HoldCode)
class HoldCodeSpec extends Specification {

    def testObject

    @Unroll('create HoldCode with #testCase  ')
    void "Test if HoldCode Creation"() {

        when:
        testObject = new HoldCode(
                stvhlddCode:stvhlddCode,
                stvhlddDispWebInd:stvhlddDispWebInd,
                stvhlddApplicationHoldInd:stvhlddApplicationHoldInd,
                stvhlddComplianceHoldInd:stvhlddComplianceHoldInd,
                stvhlddActivityDate : new Date(),
                stvhlddSurrogateId:200115,
                stvhlddVersion:1
        )


        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
     //   noExceptionThrown()
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   stvhlddCode |stvhlddDispWebInd |stvhlddApplicationHoldInd|stvhlddComplianceHoldInd |isValid
        'TC#1'                                   |   'MS'        | 'Y'              |   'Y'                   |'Y'                      |true
        'TC#2 stvhlddApplicationHoldInd is null' |   'MS'        | 'Y'              |   null                  |'Y'                      |false
        'TC#3 stvhlddComplianceHoldInd is null'  |   'MS'        | 'Y'              |   'Y'                   |null                     |false
        'TC#4 stvhlddDispWebInd is null'         |   'MS'        | null             |   'Y'                   |'Y'                      |false
    }

}