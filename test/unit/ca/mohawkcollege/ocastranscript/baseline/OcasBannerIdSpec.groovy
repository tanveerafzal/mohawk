package ca.mohawkcollege.ocastranscript.baseline


import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(OcasBannerId)
class OcasBannerIdSpec extends Specification {

    def testObject

    @Unroll('create OcasBannerId with #testCase  ')
    void "Test if OcasBannerId Creation"() {

        when:
        testObject = new OcasBannerId(
                svrobidOcasApplNum:svrobidOcasApplNum,
                svrobidYear:svrobidYear,
                svrobidActivityDate: new Date(),
                svrobidUser:'ROTHK'
        )



        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
     //   noExceptionThrown()
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   svrobidOcasApplNum |svrobidYear|isValid
        'TC#1'                                   |   '062032554'        | '2006'    |   true
        'TC#2 svrobidOcasApplNum is null'        |   null               | '2006'    |   false
        'TC#3 svrobidYear is null'               |   '062032554'        | null      |   false
    }

}