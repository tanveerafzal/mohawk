package ca.mohawkcollege.ocastranscript.baseline

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(Spriden)
class SpridenSpec extends Specification {

    def testObject

    @Unroll('create Spriden with #testCase  ')
    void "Test if Spriden Creation"() {

        when:
        testObject = new Spriden(
                spridenPidm:spridenPidm,
                spridenId:spridenId,
                spridenLastName:spridenLastName,
                spridenActivityDate : new Date(),
                spridenSurrogateId:200115,
                spridenVersion:0
        )

        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   spridenPidm |spridenId     |spridenLastName |isValid
        'TC#1'                                   |   1226124     | '001226051'  |   'Potato'     |true
        'TC#3 spridenLastName is null'           |   1226124     | '001226051'  |   null         |false
        'TC#2 spridenPidm is null'               |   null        | '001226051'  |   'Potato'     |false
        'TC#4 spridenId is null'               |   1226124     | null         |   'Potato'     |false
    }

}