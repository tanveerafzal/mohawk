package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(DegreeInfo)
class DegreeInfoSpec extends Specification {

    def testObject

    @Unroll('create DegreeInfo with #testCase  ')
    void "Test if DegreeInfo Creation"() {

        when:
        testObject = new DegreeInfo(
                shrdgmrPidm:shrdgmrPidm,
                shrdgmrSeqNo:shrdgmrSeqNo,
                shrdgmrDegcCode:shrdgmrDegcCode,
                shrdgmrDegsCode:shrdgmrDegsCode,
                shrdgmrLevlCode:shrdgmrLevlCode,
                shrdgmrActivityDate : new Date(),
                shrdgmrTermCodeSturec:'200115',
                shrdgmrSurrogateId:10,
                shrdgmrVersion:0
        )

        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
     //   noExceptionThrown()
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                                 |   shrdgmrPidm |shrdgmrSeqNo| shrdgmrDegcCode|shrdgmrDegsCode| shrdgmrLevlCode          |isValid
        'TC#1'                                   |   82180       | 1         |'PSDC'         |'AW'             |'00'                      |true
        'TC#2-shrdgmrDegsCode is null'           |   82180       | 1         |'PSDC'         |null            | '00'                     |false
        'TC#3-shrdgmrDegcCode is null'           |   82180       | 1         |null           |'AW'             | '00'                     |false
        'TC#4 - shrdgmrLevlCode is null'         |   82180       | 1         |'PSDC'         |'AW'             | null                     |false
    }

}