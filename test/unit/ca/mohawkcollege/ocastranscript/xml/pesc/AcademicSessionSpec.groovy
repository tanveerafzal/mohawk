package ca.mohawkcollege.ocastranscript.xml.pesc

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class AcademicSessionSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    @Shared
    Map propertyTagMap = [designator  : "AcademicSessionDetail.SessionDesignator",
                          name        : "AcademicSessionDetail.SessionName",
                          type        : "AcademicSessionDetail.SessionType",
                          studentLevel: "StudentLevel.StudentLevelCode",
                          programName : "AcademicProgram.AcademicProgramName"]

    @Unroll
    void "test that string property #propertyName is parsed properly from XML when #comment"() {
        given: "a course object built from an XML string"
        println xmlString
        def academicSession = new AcademicSession(xmlString)

        expect: "the course's property value to be as predicted"
        academicSession."$propertyName" == expectedValue

        where:
        testArray << [["name", "type", "studentLevel", "programName"],
                      [true, false]].combinations()

        propertyName = testArray[0]
        tagPresent = testArray[1]
        xmlValue = RandomStringUtils.randomAlphanumeric(5)
        xmlTags = propertyTagMap[propertyName].split(/\./)
        expectedValue = tagPresent ? xmlValue : null

        xmlParts = tagPresent ?  [
                xmlTags.collect { "<$it>" },
                xmlValue,
                xmlTags.reverse().collect { "</$it>" }
        ].flatten() : []
        xmlString = "<root>${xmlParts.join()}</root>"

        comment = (tagPresent ? "missing" : "populated")
    }
}
