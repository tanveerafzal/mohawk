package ca.mohawkcollege.ocastranscript.baseline

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(Svrtnte)
class SvrtnteSpec extends Specification {

    def testObject

    @Unroll('create Svrtnte with #testCase  ')
    void "Test if Svrtnte Creation"() {

        when:
        testObject = new Svrtnte(
                svrtnteBgn02:svrtnteBgn02,
                svrtnteNote:svrtnteNote,
                svrtnteDataOrigin:svrtnteDataOrigin,
                svrtnteUserId:svrtnteUserId,
                svrtnteActivityDate : new Date()
        )



        def foundValid = testObject.validate()
        def errors = testObject.errors.allErrors
        then:
        isValid == !(errors)
        foundValid == isValid

        where:
        testCase                           | svrtnteBgn02       |svrtnteNote                |svrtnteDataOrigin  |svrtnteUserId   |isValid
        'TC#1'                             | 'C20060721177019'  | 'inst=353100/Cambrian'    |   'EDI'           |   'BEATTIM'    |true
        'TC#2 svrtnteBgn02 is null'        | null               | 'inst=353100/Cambrian'    |   'EDI'           |   'BEATTIM'    |false
        'TC#3 svrtnteNote is null'         | 'C20060721177019'  | null                      |   'EDI'           |   'BEATTIM'    |false
        'TC#4 svrtnteDataOrigin is null'   | 'C20060721177019'  | 'inst=353100/Cambrian'    |  null             |   'BEATTIM'    |false
        'TC#5 svrtnteUserId is null'       | 'C20060721177019'  | 'inst=353100/Cambrian'    |   'EDI'           |  null          |false

    }

}