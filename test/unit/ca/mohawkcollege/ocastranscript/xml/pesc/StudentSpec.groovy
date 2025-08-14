package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class StudentSpec extends XmlSpecification {

    @Unroll
    void "test that OEN is correctly parsed from XML when #comment"() {
        given: "a student built from XML string"
        def student = new Student(xmlString)

        expect: "the OEN is as expected"
        student.ontarioEducationNumber == expected

        where:
        agencyFound | oen   | comment
        false       | null  | "agency not present"
        true        | null  | "agency contains no ID"
        true        | ""    | "agency ID is empty"
        true        | "abc" | "agency ID is found"

        expected = oen ?: null

        xmlOen = (oen == null ? "" : "<AgencyAssignedID>$oen</AgencyAssignedID>")
        xmlAgencyCode = "<AgencyCode>${Student.AGENCY_CODE_STATE}</AgencyCode>"
        xmlAgencyName = "<AgencyName>${Student.AGENCY_NAME_OEN}</AgencyName>"
        xmlAgencyIdentifier = agencyFound ? "<AgencyIdentifier>$xmlOen$xmlAgencyCode$xmlAgencyName</AgencyIdentifier>" : ""
        xmlString = "<Student><Person>$xmlAgencyIdentifier</Person></Student>"
    }

    @Unroll
    void "test that OCAS application number is correctly parsed from XML when #comment"() {
        given: "a student built from XML string"
        def student = new Student(xmlString)

        expect: "the OCAS number is as expected"
        student.ocasApplicationNumber == expected

        where:
        testValues << [[true, false], [true, false], [true, false]].combinations()

        agencyIdPresent = testValues[0]
        otherAgencyPresent = testValues[1]
        deprecatedIdPresent = testValues[2]

        agencyId = randomText
        deprecatedId = randomText

        xmlIdParts = [
                buildTag("AgencyAssignedID", agencyId),
                buildTag("AgencyCode", Student.AGENCY_CODE_MUTUALLY_DEFINED),
                buildTag("AgencyName", Student.AGENCY_NAME_OCAS),
        ]
        xmlAgencyIdentifier = agencyIdPresent ? buildTag("AgencyIdentifier", xmlIdParts) : ""

        xmlOtherParts = [
                buildTag("AgencyAssignedID", randomText),
                buildTag("AgencyCode", randomText),
                buildTag("AgencyName", randomText),
        ]
        xmlOtherAgency = otherAgencyPresent ? buildTag("AgencyIdentifier", xmlOtherParts) : ""

        xmlDeprecatedId = deprecatedIdPresent ? buildTag("AgencyAssignedID", deprecatedId) : ""

        xmlParts = [xmlDeprecatedId, xmlOtherAgency, xmlAgencyIdentifier].findAll()
        xmlString = buildTag("Student", buildTag("Person", xmlParts))

        expected = agencyIdPresent ? agencyId : deprecatedIdPresent ? deprecatedId : null

        comment = [
                "agency ID ${agencyIdPresent ? "present" : "absent"}",
                "other agency ${otherAgencyPresent ? "present" : "absent"}",
                "old ID ${deprecatedIdPresent ? "present" : "absent"}"
        ].join("; ")
    }

    @Unroll
    void "test that String #propertyName is parsed correctly from XML when #comment"() {
        given: "a Student object created from an XML string"
        def student = new Student(xmlString)

        expect: "the object's property to be as predicted"
        student."$propertyName" == expectedValue

        where:
        testList << [["schoolAssignedPersonId"], [true, false]].combinations()

        propertyName = testList[0]
        testValue = testList[1]
        tagMap = [schoolAssignedPersonId: "Person.SchoolAssignedPersonID"]
        tags = tagMap[propertyName].split(/\./)

        expectedValue = testValue ? randomText : null

        xmlParts = testValue ? [
                tags.collect { "<$it>" },
                expectedValue,
                tags.reverse().collect { "</$it>" }
        ].flatten() : []
        xmlString = buildTag(randomTag, xmlParts.join())

        comment = testValue ? "populated" : "missing"
    }

    @Unroll
    void "test that we can get the student's #comment via property accessors"() {
        given: "an XML string"
        def nameXml = nameElements.findAll { it.value }.collect { key, value -> "<$key>$value</$key>" }.join()
        def xmlString = "<Student><Person><Name>$nameXml</Name></Person></Student>"

        and: "a Student built from it"
        def student = new Student(xmlString)

        when: "we ask for the student name"
        def foundName = student.fullyQualifiedName

        then: "the result is what we expected"
        foundName == expectedName

        where:
        prefix | first   | middle | last   | suffix | title  | expectedName               | comment
        null   | null    | null   | null   | null   | null   | ""                         | "empty name"
        null   | "Bluto" | null   | null   | null   | null   | "Bluto"                    | "first name only"
        "Mx"   | "Earl"  | "QQ"   | "Wall" | "Esq"  | "King" | "Mx Earl QQ Wall Esq King" | "many-part name"

        nameElements = [NamePrefix: prefix, FirstName: first, MiddleName: middle, LastName: last, NameSuffix: suffix, NameTitle: title]
    }

    void "test that we can get the student's birthdate"() {
        given: "a student built from XML string"
        def student = new Student(xmlString)

        expect: "the birthdate is as expected"
        student.dateOfBirth == expected

        where:
        birthFound | birthDate    | comment
        false      | null         | "birth element is not present"
        true       | null         | "birthdate element is not present"
        true       | ""           | "birth date is empty"
        true       | "2021-01-28" | "birth date is present"

        parsedBirthDate = birthDate ? Format.DATE.simpleDateFormat.parse(birthDate) : null
        expected = parsedBirthDate ?: null

        xmlBirthDate = (birthDate == null ? "" : "<BirthDate>$birthDate</BirthDate>")
        xmlBirth = (birthFound ? "<Birth>$xmlBirthDate</Birth>" : "")
        xmlString = "<Student><Person>$xmlBirth</Person></Student>"
    }
}
