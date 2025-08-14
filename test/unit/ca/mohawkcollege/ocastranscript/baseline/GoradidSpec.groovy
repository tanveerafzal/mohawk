package ca.mohawkcollege.ocastranscript.baseline


import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(Goradid)
class GoradidSpec extends Specification {

    def testObject

    @Unroll('create Goradid with #testCase  ')
    void "Test if Goradid Creation"() {

        when:
        testObject = new Goradid(
                goradidPidm:goradidPidm,
                goradidAdditionalId:goradidAdditionalId,
                goradidAdidCode:goradidAdidCode,
                goradidUserId:goradidUserId,
                goradidActivityDate : new Date(),
                goradidDataOrigin:'200115'
        )

        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
     //   noExceptionThrown()
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   goradidPidm |goradidAdditionalId| goradidAdidCode|goradidUserId          |isValid
        'TC#1'                                   |   226937       | 868718818         |'OEN'          |'CRON_ST'             |true
        'TC#2 goradidUserId is null'             |   226937       | 868718818         |'OEN'          |null                  |false
        'TC#3 goradidAdidCode is null'           |   226937       | 868718818         |null           |'CRON_ST'            |false
        'TC#4 goradidAdditionalId is null'       |   226937       | null              |'OEN'          |'CRON_ST'             |false
    }

}