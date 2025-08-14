package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class CourseSpec extends XmlSpecification {

    @Unroll
    void "test that #propertyName is parsed as String from XML with tag #comment"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's property value to be as predicted"
        course."$propertyName" == expectedValue

        where:
        testArray << [["number", "title", "creditUnits", "repeatCode", "gradeScale", "academicGrade"],
                      [true, false]].combinations()

        propertyMap = [number       : "CourseNumber", title: "CourseTitle", creditUnits: "CourseCreditUnits",
                       repeatCode   : "CourseRepeatCode", gradeScale: "CourseAcademicGradeScaleCode",
                       academicGrade: "CourseAcademicGrade"]
        propertyName = testArray[0]
        xmlValue = (testArray[1] ? randomText : null)
        xmlTag = propertyMap[propertyName]
        expectedValue = xmlValue ?: null

        xmlString = buildTag("Course", xmlValue ? buildTag(xmlTag, xmlValue) : "")

        comment = (xmlValue ? "populated" : "missing")
    }

    @Unroll
    void "test that supplemental grade is parsed as String from XML with tag #comment"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's property value to be as predicted"
        course.supplementalAcademicGrade == expectedValue

        where:
        xmlValue << [null, randomText]

        xmlTags = ["CourseSupplementalAcademicGrade", "CourseSupplementalGrade", "CourseAcademicSupplementalGrade"]
        xmlOpening = xmlTags.collect { "<$it>" }.join()
        xmlClosing = xmlTags.reverse().collect { "</$it>" }.join()
        expectedValue = xmlValue ?: null

        xmlString = buildTag("Course", xmlValue ? "$xmlOpening$xmlValue$xmlClosing" : "")

        comment = (xmlValue ? "populated" : "missing")
    }

    @Unroll
    void "test that credit level and code are parsed properly from XML with value #comment"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's credit level and corresponding code to be as predicted"
        course.creditLevel == expectedValue
        course.creditLevelCode == expectedCode

        where:
        testValue << CourseCreditLevel.values() + [null, ""]

        propertyName = "creditLevel"
        xmlValue = testValue?.toString()
        xmlTag = "CourseCreditLevel"
        expectedValue = testValue ?: null
        expectedCode = (expectedValue as CourseCreditLevel)?.code

        xmlContent = (xmlValue == null ? "" : "<$xmlTag>$xmlValue</$xmlTag>")
        xmlString = "<Course>$xmlContent</Course>"

        comment = (xmlValue == null ? "missing" : xmlValue ?: "empty")
    }

    @Unroll
    void "test that #propertyName is parsed numerically from XML with tag #comment"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's property value to be as predicted"
        course."$propertyName" == expectedValue

        where:
        testArray << [["creditValue", "creditEarned", "qualityPointsEarned"], [null, "", Random.newInstance().nextDouble()]].combinations()

        propertyMap = [creditValue: "CourseCreditValue", creditEarned: "CourseCreditEarned", qualityPointsEarned: "CourseQualityPointsEarned"]
        propertyName = testArray[0]
        xmlValue = testArray[1]
        xmlTag = propertyMap[propertyName]
        expectedValue = xmlValue ?: null

        xmlContent = (xmlValue == null ? "" : "<$xmlTag>$xmlValue</$xmlTag>")
        xmlString = "<Course>$xmlContent</Course>"

        comment = (xmlValue == null ? "missing" : xmlValue ? "populated" : "empty")
    }

    @Unroll
    void "test that #propertyName is parsed as date from XML with tag #comment"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's property value to be as predicted"
        course."$propertyName" == expectedValue

        where:
        testArray << [["beginDate", "endDate"], [null, false, true]].combinations()

        propertyMap = [beginDate: "CourseBeginDate", endDate: "CourseEndDate"]
        propertyName = testArray[0]
        rand = Random.newInstance()
        expectedValue = testArray[1] ? new Date(rand.nextInt(9999), rand.nextInt(11), rand.nextInt(27) + 1) : null
        xmlValue = expectedValue ? Format.DATE.simpleDateFormat.format(expectedValue) : ""
        xmlTag = propertyMap[propertyName]

        xmlContent = (testArray[1] == null ? "" : "<$xmlTag>$xmlValue</$xmlTag>")
        xmlString = "<Course>$xmlContent</Course>"

        comment = (testArray[1] == null ? "missing" : xmlValue ? "populated" : "empty")
    }

    @Unroll
    void "test that grade falls back to supplemental grade (#xmlSupplementalValue) if academic grade is ZZZ (#xmlAcademicValue)"() {
        given: "a course object built from an XML string"
        def course = new Course(xmlString)

        expect: "the course's property value to be as predicted"
        course.grade == (course.academicGrade == Course.GRADE_NONE ? course.supplementalAcademicGrade : course.academicGrade)

        where:
        testArray << [[true, false, null], [true, false]].combinations()

        testAcademic = testArray[0]
        testSupplemental = testArray[1]

        xmlAcademicValue = testAcademic ? Course.GRADE_NONE : RandomStringUtils.randomAlphanumeric(3)
        xmlAcademic = testAcademic == null ? "" : "<CourseAcademicGrade>$xmlAcademicValue</CourseAcademicGrade>"

        xmlSupplementalValue = testSupplemental ? RandomStringUtils.randomAlphanumeric(3) : ""
        xmlTags = ["CourseSupplementalAcademicGrade", "CourseSupplementalGrade", "CourseAcademicSupplementalGrade"]
        xmlOpening = xmlTags.collect { "<$it>" }.join()
        xmlClosing = xmlTags.reverse().collect { "</$it>" }.join()
        xmlSupplemental = (testSupplemental ? "" : "$xmlOpening$xmlSupplementalValue$xmlClosing")

        xmlString = "<Course>$xmlAcademic$xmlSupplemental</Course>"
    }
}
