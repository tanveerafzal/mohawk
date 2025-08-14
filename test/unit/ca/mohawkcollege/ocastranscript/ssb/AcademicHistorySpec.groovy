package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.baseline.Term
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(AcademicHistory)
@Mock([Term])
class AcademicHistorySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "check that term property works"(){
//        given: "an instance of the tested class"
//        when: "we call "
    }

    void "test that term property access calls the trait's getTerm properly"() {
        given: "an instance of AcademicHistory"
        HasBannerTerm hasTerm = new AcademicHistory(termCode: "abc") as HasBannerTerm
        GroovyMock(Term, global: true)
        Term.findAllByStvtermCode(_) >> null

        when: "we access the term property"
        def theTerm = hasTerm.term

        then: "the Term finder method is called"
        1 * Term.findByStvtermCode(_)
    }
}
