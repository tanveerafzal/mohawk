package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.GrailsNameUtils
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@ConfineMetaClassChanges([TransmissionData, Student])
class CollegeTranscriptSpec extends Specification {

    static def propertyTypeMap = [academicSummary: XmlFragment, gpa: Gpa]

    @Unroll
    void "test that accessing the #propertyName property creates a new object"() {
        GroovyMock(propertyClass, global: true)
        def mockPropertyValue = GroovyMock(propertyClass)

        given: "an XML string containing a substring fragment"
        def xmlString = "<CollegeTranscript>$xmlSubstring</CollegeTranscript>"

        and: "a CollegeTranscript created from that XML"
        def collegeTranscript = new CollegeTranscript(xmlString)

        when: "we get the property value"
        def foundPropertyValue = collegeTranscript."$propertyName"

        then: "a new TransmissionData object is created from the original fragment"
        1 * propertyClass."<init>"({ it.toXml() == xmlSubstring } as XmlFragment) >> mockPropertyValue
        foundPropertyValue == mockPropertyValue

        where:
        property << [
                [name: "transmissionData", klass: TransmissionData, tag: "TransmissionData"],
                [name: "student", klass: Student, tag: "Student"]
        ]
        propertyName = property.name
        propertyClass = property.klass
        xmlTag = property.tag
        xmlContent = RandomStringUtils.randomAlphanumeric(12)
        xmlSubstring = "<$xmlTag>$xmlContent</$xmlTag>"
    }

    @Unroll
    void "test that the #propertyName property is delegated to the student"() {
        GroovyMock(Student, global: true)
        def mockPropertyValue = GroovyMock(propertyType)
        Student."<init>"(*_) >> GroovyMock(Student) {
            "$propertyGetter"() >> mockPropertyValue
        }

        given: "an object from XML containing a student substring"
        def instance = new CollegeTranscript(xmlString)

        when: "we access the requested property"
        def found = instance."$propertyName"

        then: "the value obtained came from the student object"
        found == mockPropertyValue

        where:
        propertyName << ["academicSummary", "gpa"]
        propertyGetter = GrailsNameUtils.getGetterName(propertyName)
        propertyType = propertyTypeMap[propertyName]

        xmlString = "<CollegeTranscript><Student></Student></CollegeTranscript>"
    }

    void "test that note messages include #transcriptNoteCount from the transcript and #transmissionNoteCount from the TransmissionData"() {
        GroovyMock(TransmissionData, global: true)
        TransmissionData."<init>"(*_) >> GroovyMock(TransmissionData) {
            getNoteMessages() >> transmissionNotes
        }

        given: "a CollegeTranscript with a TransmissionData inside it"
        def instance = new CollegeTranscript(xmlString)

        when: "we get the note messages for the CollegeTranscript"
        def foundNotes = instance.noteMessages

        then: "the resultant list includes the TransmissionData notes at the end"
        foundNotes == transcriptNotes + transmissionNotes

        where:
        noteCounts << [[0, 1, 5], [0, 1, 5]].combinations()

        transcriptNoteCount = noteCounts[0]
        transmissionNoteCount = noteCounts[1]
        transcriptNotes = ([null] * transcriptNoteCount).collect { RandomStringUtils.randomAlphanumeric(12) }
        transmissionNotes = ([null] * transmissionNoteCount).collect { RandomStringUtils.randomAlphanumeric(12) }

        xmlNotes = transcriptNotes.collect { "<NoteMessage>$it</NoteMessage>" }.join()
        xmlString = "<CollegeTranscript><TransmissionData></TransmissionData>$xmlNotes</CollegeTranscript>"
    }

    void "test that award and course totals are obtained from the student's academic record"() {
        given: "a CollegeTranscript with a mock Student object"
        GroovyMock(Student, global: true)
        Student."<init>"(*_) >> GroovyMock(Student) {
            getAcademicRecord() >> GroovyMock(XmlFragment) {
                getChildren("AcademicAward") >> (([true] * awardTotal).collect { GroovyMock(XmlFragment) })
                getChildren("Course") >> (([true] * courseTotal).collect { GroovyMock(XmlFragment) })
            }
        }
        def instance = new CollegeTranscript()

        when: "we get the property totals from the transcript"
        def foundAwards = instance.academicAwardTotal
        def foundCourses = instance.courseTotal

        then: "the resultant values come from the student's academic record object"
        foundAwards == awardTotal
        foundCourses == courseTotal

        where:
        testValues << [[0, 1, 5], [0, 1, 3]].combinations()
        awardTotal = testValues[0]
        courseTotal = testValues[1]
    }
}
