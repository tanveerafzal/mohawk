package ca.mohawkcollege.ocastranscript.ssb

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@TestFor(InstitutionalHonor)
class InstitutionalHonorSpec extends Specification {

    def testObject


    def cleanup() {
        testObject = null
    }

    @Unroll('create InstitutionalHonor with #pidm and #message ')
    void "Test if AcademicAwards Creation"() {
        given:
        testObject = new InstitutionalHonor(pidm:pidm,termCode:termCode,description:award, effectiveDate:activityDate);

        where:
        pidm | termCode | award | activityDate
        165286  | 'Honours'| 'Honours'|'2006-05-12'
        165283  | 'Honours'| 'Honours'|null
        null | null| null| null
    }

    @Unroll('create InstitutionalHonor with #pidm have rows ')
    void "Test if AcademicAwards have data"() {

        given:
        testObject = new InstitutionalHonor(pidm:165286,termCode:'201209',description:'Honours', effectiveDate:'2006-05-12');

        expect:
        // InstitutionalHonor.findByPidm(pidm:pidm).termCode.equals('165286')  == awardListSize
        testObject.termCode  == termCode
        where:
        pidm | termCode
        165286  |  '201209'
        165286  |  '201209'
    }

}
