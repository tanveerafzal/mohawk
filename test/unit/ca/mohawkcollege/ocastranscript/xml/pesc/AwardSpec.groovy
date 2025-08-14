package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class AwardSpec extends XmlSpecification {
    @Unroll
    void "test that award title is parsed properly from XML with tag #comment"() {
        given: "an award object built from an XML string"
        def award = new Award(xmlString)

        expect: "the award's title to be as predicted"
        award.title == expectedValue

        where:
        xmlValue << [null, randomText]

        expectedValue = xmlValue ?: null

        xmlTitle = xmlValue ? buildTag("AcademicAwardTitle", xmlValue) : ""
        xmlString = buildTag("AcademicAward", xmlTitle)

        comment = xmlValue ? "populated" : "missing"
    }

    @Unroll
    void "test that award date is parsed properly from XML with tag #comment"() {
        given: "an award object built from an XML string"
        def award = new Award(xmlString)

        expect: "the award's date to be as predicted"
        award.date == expectedValue

        where:
        xmlValue     | expectedValue
        null         | null
        ""           | null
        "2018-05-22" | new Date(118, 4, 22)

        xmlDate = (xmlValue == null ? "" : "<AcademicAwardDate>$xmlValue</AcademicAwardDate>")
        xmlString = "<AcademicAward>$xmlDate</AcademicAward>"

        comment = (xmlValue == null ? "missing" : xmlValue ? "populated" : "empty")
    }

    @Unroll
    void "test that award #propertyName is parsed properly from XML with tag #comment"() {
        given: "an award object built from an XML string"
        def award = new Award(xmlString)

        expect: "the award's program type to be as predicted"
        award."$propertyName" == expectedValue

        where:
        testValue << [["programType", "programName"], [true, false]].combinations()

        innerTagMap = [programType: "AcademicProgramType", programName: "AcademicProgramName"]
        propertyName = testValue[0]
        xmlValue = (testValue[1] ? randomText : null)
        expectedValue = xmlValue

        xmlSubstring = xmlValue ? buildTag("AcademicAwardProgram", buildTag(innerTagMap[propertyName], xmlValue)) : ""
        xmlString = buildTag("AcademicAward", xmlSubstring)

        comment = xmlValue ? "populated" : "missing"
    }
}
