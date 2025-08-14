package ca.mohawkcollege.ocastranscript.baseline

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(Term)
class TermSpec extends Specification {

    def testObject

    @Unroll('create Term with #testCase  ')
    void "Test if Term Creation"() {

        when:
        testObject = new Term(
                stvtermCode:stvtermCode,
                stvtermDesc:stvtermDesc,
                stvtermStartDate:stvtermStartDate,
                stvtermEndDate:stvtermEndDate,
                stvtermAcyrCode:stvtermAcyrCode,
                stvtermHousingStartDate:stvtermHousingStartDate,
                stvtermHousingEndDate:stvtermHousingEndDate,
                stvtermActivityDate : new Date(),
                stvtermSurrogateId:200115,
                stvtermVersion:0
        )




        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   stvtermCode|stvtermDesc  |stvtermStartDate |stvtermEndDate |stvtermAcyrCode |stvtermHousingStartDate   |stvtermHousingEndDate|isValid
        'TC#1'                                   |   198533     | 'Fall 1985' |new Date()       |new Date()     |'8586'          |new Date()                |new Date()    |true
        'TC#2 stvtermCode is null'               |   null       | 'Fall 1985' |new Date()       |new Date()     |'8586'          |new Date()                |new Date()    |false
        'TC#3 stvtermDesc is null'               |   198533     |null         |new Date()       |new Date()     |'8586'          |new Date()                |new Date()    |false
        'TC#4 stvtermStartDate is null'          |   198533     | 'Fall 1985' |null             |new Date()     |'8586'          |new Date()                |new Date()    |false
        'TC#5 stvtermEndDate is null'            |   198533     | 'Fall 1985' |new Date()       |  null         |'8586'          |new Date()                |new Date()    |false
        'TC#6 stvtermAcyrCode is null'           |   198533     | 'Fall 1985' |new Date()       |new Date()     |null            |new Date()                |new Date()    |false
        'TC#7 stvtermHousingStartDate is null'   |   198533     | 'Fall 1985' |new Date()       |new Date()     |'8586'          | null                     |new Date()    |false
        'TC#8 stvtermHousingEndDate is null'     |   198533     | 'Fall 1985' |new Date()       |new Date()     |'8586'          |new Date()                |null          |false
    }

}