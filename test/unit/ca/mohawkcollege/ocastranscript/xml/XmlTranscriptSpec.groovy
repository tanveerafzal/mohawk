package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.RequestStatus
import ca.mohawkcollege.ocastranscript.baseline.Spriden
import ca.mohawkcollege.ocastranscript.baseline.Svrtreq
import ca.mohawkcollege.ocastranscript.ssb.AcademicHistory
import ca.mohawkcollege.ocastranscript.ssb.Degree
import ca.mohawkcollege.ocastranscript.ssb.InProgressCourses
import ca.mohawkcollege.ocastranscript.ssb.InstitutionalHonor
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Spriden, InProgressCourses, AcademicHistory, InstitutionalHonor, Degree])
@ConfineMetaClassChanges([AcademicHistory, InProgressCourses])
class XmlTranscriptSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    @Unroll
//    @NotYetImplemented
    void "test validation when #comment"() {

        given: "a mock transcript request"
        def mockRequest = mockTranscriptRequest(responseStatus: ResponseStatus.Hold)

        when: "we create static mocks"
        def inProgressList = ([GroovyMock(InProgressCourses)] * inProgress)
        def historicalList = ([GroovyMock(AcademicHistory)] * historical)

        GroovyStub(InProgressCourses, global: true)
        GroovyStub(AcademicHistory, global: true)

        AcademicHistory.findAllByPidm(*_) >> historicalList

        InProgressCourses.findAllByPidm(*_) >> inProgressList

        and: "we set the object's properties"
        def xmlTranscript = new XmlTranscript()
//        xmlTranscript.hasAcademicHistory = hasAcademicHistory
        xmlTranscript.transcriptRequest = request ? mockRequest : null
        xmlTranscript.errorType = errorType ? ErrorType.valueOf(errorType) : null
        xmlTranscript.errorMessage = errorMessage
        xmlTranscript.rawXml = rawXml
//        xmlTranscript.allCourses = [GroovyMock(InProgressCourses)] * courses

        and: "we validate the object"
        def foundInProgress = xmlTranscript.coursesInProgress.size()
        def foundHistorical = xmlTranscript.pastCourses.size()
        def foundValid = xmlTranscript.validate()

        then: "validation result matches our expectations"
        noExceptionThrown()
        foundInProgress == inProgress
        foundHistorical == historical
        foundValid == expectValid || (xmlTranscript.errors.allErrors.each { println(it) } && false)

        where:
        request | errorType       | errorMessage | rawXml     | inProgress | historical | expectValid | comment
        true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | 2          | 3          | true        | "all properties populated"
        null    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | 2          | 3          | false       | "request object null"
        true    | null            | "who?"       | "<a>b</a>" | 2          | 3          | true        | "error type null"
        true    | "OcasIdUnknown" | null         | "<a>b</a>" | 2          | 3          | true        | "error message null"
        true    | "OcasIdUnknown" | "who?"       | null       | 2          | 3          | true        | "raw XML null"
        true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | 0          | 3          | true        | "no current courses"
        true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | 2          | 0          | true        | "no past courses"
        true    | "OcasIdUnknown" | "who?"       | "<a>b</a>" | 0          | 0          | true        | "no courses"
        null    | null            | null         | null       | 0          | 0          | false       | "all properties null"
    }

    @Unroll
    void "test that a GPA of #gpa gets sent as #rounded"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (grades.collect { Number grade ->
            mockAcademicHistory(grade: grade)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlGpa = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.GradePointAverage

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlGpa == rounded

        where:
        grades           | rounded
        [60, 70, 80]     | "70.0"
        [70, 70, 80]     | "73.3"
        [70, 80, 80]     | "76.7"
        [70, 70, 70, 71] | "70.3"
        [70, 70, 70, 72] | "70.5"
        [70, 70, 70, 73] | "70.8"
        [7, 7, 7, 8]     | "7.3" // rounded from 7.25
        [100, 100, 101]  | "100.3"
        gpa = grades.sum() / grades.size()
        weight = 1
        included = true
    }

    @Unroll
    void "test that GPA calculation takes omissions into account when #comment"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (([grades, includeds].transpose()).collect { Number grade, Boolean included ->
            mockAcademicHistory(grade: grade, included: included)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlGpa = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.GradePointAverage

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlGpa == rounded

        where:
        grades       | includeds             | rounded | comment
        [60, 70, 80] | [true, true, true]    | "70.0"  | "none of the grades is omitted"
        [60, 70, 80] | [true, true, false]   | "65.0"  | "one of the grades is omitted"
        [60, 70, 80] | [false, false, false] | "0.0"   | "all of the grades are omitted"
    }

    @Unroll
    void "test that GPA calculation takes course weight into account when #comment"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (([grades, weights].transpose()).collect { Number grade, Number weight ->
            mockAcademicHistory(grade: grade, creditHours: weight)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlGpa = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.GradePointAverage

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlGpa == rounded

        where:
        grades       | weights         | rounded | comment
        [60, 70, 80] | [1, 1, 1]       | "70.0"  | "all courses have weight 1"
        [60, 70, 80] | [3, 3, 3]       | "70.0"  | "all courses have the same weight"
        [60, 70, 80] | [1, 2, 3]       | "73.3"  | "all of the weights are different"
        [60, 70, 80] | [1, 2.72, 3.14] | "73.1"  | "some of the weights are not integers"
        [60, 70, 80] | [1, 1, 0]       | "65.0"  | "one of the weights is zero"
        [60, 70, 80] | [0, 0, 0]       | "0.0"   | "all of the weights are zero"
    }

    @Unroll
    void "test that with 0 completed and #inProgressCount in progress courses, no XML is generated"() {
        given: "a student with in progress courses and none completed"
        GroovyMock(InProgressCourses, global: true)
        InProgressCourses.findAllByPidm(_) >> (([true] * inProgressCount).collect { GroovyMock(InProgressCourses) })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        expect: "no XML generated"
        xmlTranscript.xml == null

        where:
        inProgressCount << [0, 1, 5]
    }

    @Unroll
    void "test that with #comment, CreditHoursAttempted is rendered as #expected in XML"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (completedValues.collect { Number completed ->
            mockAcademicHistory(hoursCompleted: completed)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlValue = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.CreditHoursAttempted

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlValue == expected

        where:
        completedValues    | comment
        [0]                | "a course with zero value"
        [1]                | "a course with integer value"
        [3.14]             | "a course with decimal value"
        [0, 0, 0, 0, 0]    | "multiple courses, value zero"
        [1, 1, 1, 1, 1]    | "multiple courses, same integer value"
        [4, 3, 1, 2, 5]    | "multiple courses, multiple integer values"
        [4, 3.14, 0, 12.0] | "multiple courses, mixed values"
        expected = "0"
    }

    @Unroll
    void "test that with #comment, CreditHoursEarned is rendered as #expected in XML"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> ([completedValues, includedValues].transpose().collect { Number completed, boolean included ->
            mockAcademicHistory(hoursCompleted: completed, included: included)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlValue = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.CreditHoursEarned

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlValue == expected

        where:
        completedValues    | includedValues                    | expectedValue | comment
        [0]                | [true]                            | 0             | "a course with zero value"
        [0]                | [false]                           | 0             | "an excluded course with zero value"
        [1]                | [true]                            | 1             | "a course with positive integer value"
        [1]                | [false]                           | 0             | "an excluded course with positive value"
        [3.14]             | [true]                            | 3.14          | "a course with decimal value"
        [0, 0, 0, 0, 0]    | [true, false, true, false, true]  | 0             | "multiple courses, value zero"
        [1, 1, 1, 1, 1]    | [false, true, false, true, false] | 2             | "multiple courses, same integer value"
        [4, 3, 1, 2, 5]    | [true, false, true, false, true]  | 10            | "multiple courses, multiple integer values"
        [4, 3.14, 0, 12.0] | [true, true, true, true]          | 19.14         | "multiple courses, mixed values"
        [4, 3.14, 0, 12.0] | [false, false, false, false]      | 0             | "multiple courses, all excluded"
        expected = expectedValue.toString()
    }

    void "test that CreditUnit is always rendered as 'Semester' in XML"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (completedValues.collect { Number completed ->
            mockAcademicHistory(hoursCompleted: completed)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlValue = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.CreditUnit

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlValue == expected.toString()

        where:
        completedValues    | comment
        [0]                | "a course with zero value"
        [1]                | "a course with integer value"
        [3.14]             | "a course with decimal value"
        [0, 0, 0, 0, 0]    | "multiple courses, value zero"
        [1, 1, 1, 1, 1]    | "multiple courses, same integer value"
        [4, 3, 1, 2, 5]    | "multiple courses, multiple integer values"
        [4, 3.14, 0, 12.0] | "multiple courses, mixed values"
        expected = "Semester"
    }

    @Unroll
    void "test that with #comment, CreditHoursforGPA is rendered as #expected in XML"() {
        given: "a list of courses totalling a specified GPA"
        GroovyMock(AcademicHistory, global: true)
        AcademicHistory.findAllByPidm(_) >> (creditValues.collect { Number creditValue ->
            mockAcademicHistory(creditHours: creditValue)
        })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        when: "we generate the transcript XML"
        def xml = xmlTranscript.xml

        and: "we parse out the GPA"
        def xmlValue = new XmlSlurper().parseText(xml).
                Student.AcademicRecord.AcademicSummary.GPA.CreditHoursforGPA

        then: "the GPA in the transcript is rounded and formatted properly"
        noExceptionThrown()
        xmlValue == expected

        where:
        creditValues       | comment
        [0]                | "a course with zero weight"
        [1]                | "a course with integer weight"
        [3.14]             | "a course with decimal weight"
        [0, 0, 0, 0, 0]    | "multiple courses, weight zero"
        [1, 1, 1, 1, 1]    | "multiple courses, same integer weight"
        [4, 3, 1, 2, 5]    | "multiple courses, multiple integer weights"
        [4, 3.14, 0, 12.0] | "multiple courses, mixed weights"
        expected = (creditValues.sum() ?: 0).toString()
    }

    void "test that in progress courses are ignored in academic history search"() {
        given: "a student with in progress courses and none completed"
        GroovyMock(InProgressCourses, global: true)
        InProgressCourses.findAllByPidm(_) >> ((1..5).collect { GroovyMock(InProgressCourses) })

        and: "an XML transcript with a mock transcript request"
        def xmlTranscript = new XmlTranscript(transcriptRequest: mockTranscriptRequest())

        expect: "the XmlTranscript object has no academic history"
        !xmlTranscript.hasAcademicHistory
    }

    private mockAcademicHistory(Map<String, Object> params) {
        boolean included = params?.containsKey("included") ? params.included : true
        BigDecimal creditHours = params?.containsKey("creditHours") ? params.creditHours.toString().toBigDecimal() : 1
        BigDecimal hoursCompleted = params?.containsKey("hoursCompleted") ? params.hoursCompleted.toString().toBigDecimal() : creditHours
        BigDecimal numericGrade = params?.containsKey("grade") ? params.grade.toString().toBigDecimal() : 75
        GroovyMock(AcademicHistory) {
            asBoolean() >> true
            getIncludeInGpa() >> included
            getCreditHours() >> creditHours
            getCreditHoursCompleted() >> hoursCompleted
            getWeightedGrade() >> (numericGrade * creditHours)
        }
    }

    private mockTranscriptRequest(Map<String, Object> params) {
        def requestStatus = params?.containsKey("requestStatus") ? params.requestStatus : RequestStatus.ReadyToSendTranscript
        GroovyMock(TranscriptRequest) {
            asBoolean() >> true
            getRequestTrackingId() >> "x"
            getRequestStatus() >> requestStatus
            getSvrtreq() >> GroovyMock(Svrtreq) { getSvrtreqId() >> "mohawkId" }
            getOriginalXml() >> "<empty/>"
        }
    }
}
