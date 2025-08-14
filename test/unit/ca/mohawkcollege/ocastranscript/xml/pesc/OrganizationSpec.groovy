package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlSpecification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.lang.math.RandomUtils
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class OrganizationSpec extends XmlSpecification {

    void "test that getName returns the name as set in xml"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the name string to be as expected"
        organization.name == name

        where:
        name = randomText
        xmlString = "<Organization><OrganizationName>$name</OrganizationName></Organization>"
    }

    @Unroll
    void "test that getIdentifier prefers CSIS over USIS with #comment"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the identifier string to be as expected"
        organization.identifier == expected

        where:
        testValues << [[true, false], [true, false]].combinations()

        csis = randomText
        usis = randomText
        xmlCsis = (testValues[0] ? buildTag("CSIS", csis) : "")
        xmlUsis = (testValues[1] ? buildTag("USIS", usis) : "")
        xmlString = "<Organization>$xmlCsis$xmlUsis</Organization>"

        expected = (testValues[0] ? csis : testValues[1] ? usis : null)

        presence = testValues.collect { it ? "present" : "missing" }
        comment = "CSIS ${presence[0]}, USIS ${presence[1]}"
    }

    void "test that a simple address is formatted properly"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        when: "we ask for the address lines"
        def addressLines = organization.addressLines

        then: "the result is what we expected"
        addressLines == expectedResult

        where:
        xmlString = "<Organization><Contacts><Address><AddressLine>A</AddressLine><AddressLine>B</AddressLine><AddressLine>C</AddressLine>" +
                "<City>D</City>" +
                "<StateProvince>E</StateProvince>" +
                "<StateProvinceCode>F</StateProvinceCode>" +
                "<PostalCode>G</PostalCode>" +
                "<CountryCode>H</CountryCode>" +
                "</Address></Contacts></Organization>"
        expectedResult = ["A", "B", "C", "D E F G H"]
    }

    @Unroll
    void "test that a #comment number is formatted properly"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the organization's formatted number is what we expected"
        organization."$propertyName" == expectedResult

        where:
        propertyName << ["formattedPhone", "formattedFax"]
        xmlTag << ["Phone", "FaxPhone"]
        comment << ["phone", "fax"]

        prefix = (RandomUtils.nextInt(10) as String)
        areaCode = "905"
        exchange = "123"
        local = "4567"
        extension = "54321"

        xmlString = [
                "<Organization><Contacts><$xmlTag>",
                buildTag("CountryPrefixCode", prefix),
                buildTag("AreaCityCode", areaCode),
                buildTag("PhoneNumber", "$exchange$local"),
                buildTag("PhoneNumberExtension", extension),
                "</$xmlTag></Contacts></Organization>"
        ].join()
        expectedResult = "+$prefix ($areaCode) $exchange-$local x$extension"
    }

    void "test that getEmail returns the email as set in xml"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the email string to be as expected"
        organization.email == email

        where:
        email = "steve@msn.com"
        xmlString = "<Organization><Contacts><Email><EmailAddress>$email</EmailAddress></Email></Contacts></Organization>"
    }

    @Unroll
    void "test that getContactNoteMessages returns the note messages with #comment"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the note message list to match our expectations"
        organization.contactNoteMessages == expectedList

        where:
        messageList << [
                [],
                [""],
                ["abc"],
                ["def", "ghi", "jkl"],
                ["def", "", "jkl"],
                ["", "def", ""],
                ["", "", ""],
                ["", "", "def", "ghi"],
                ["", "def"]
        ]
        expectedList = messageList.findAll()
        xmlMessages = messageList.collect { buildTag("NoteMessage", it) }.join()
        xmlString = "<Organization><Contacts>$xmlMessages</Contacts></Organization>"

        countMessages = expectedList.size()
        countBlanks = messageList.size() - countMessages
        commentMessages = countMessages ? countMessages > 1 ? "multiple messages" : "one message" : "no messages"
        commentBlanks = countBlanks ? countBlanks > 1 ? "multiple blanks" : "one blank" : "no blanks"
        comment = "$commentMessages and $commentBlanks"
    }

    @Unroll
    void "test that getNoteMessages returns the note messages with #comment"() {
        given: "an organization built from an XML string"
        def organization = new Organization(xmlString)

        expect: "the note message list to match our expectations"
        organization.noteMessages == expectedList

        where:
        messageList << [
                [],
                [""],
                ["abc"],
                ["def", "ghi", "jkl"],
                ["def", "", "jkl"],
                ["", "def", ""],
                ["", "", ""],
                ["", "", "def", "ghi"],
                ["", "def"]
        ]
        expectedList = messageList.findAll()
        xmlMessages = messageList.collect {
            "<NoteMessage>$it</NoteMessage>"
        }.join()
        xmlString = "<Organization>$xmlMessages</Organization>"

        countMessages = expectedList.size()
        countBlanks = messageList.size() - countMessages
        commentMessages = countMessages ? countMessages > 1 ? "multiple messages" : "one message" : "no messages"
        commentBlanks = countBlanks ? countBlanks > 1 ? "multiple blanks" : "one blank" : "no blanks"
        comment = "$commentMessages and $commentBlanks"
    }

    @Unroll
    void "test that getChildren fetches all expected objects in order with #comment"() {
        given: "an organization built from an XML string"
        println "XML:"
        println xmlString
        def organization = new Organization(xmlString)

        when: "we fetch all the children of the object"
        def children = organization.children

        and: "selectively pop them out"
        def (foundCsis, foundUsis, foundLocalId, foundOrgName) = [null, null, null, null]
        if (csis) foundCsis = children.remove(0).toString()
        if (usis) foundUsis = children.remove(0).toString()
        if (localId) foundLocalId = children.remove(0).toString()
        if (name) foundOrgName = children.remove(0).toString()
        def foundNotes = children*.toString()

        then: "the children of the organization match up, in order, to the expected objects"
        foundCsis == csis
        foundUsis == usis
        foundLocalId == localIdContent
        foundOrgName == name
        foundNotes == noteMessageList

        where:
        testValues << [
                [true, false], // is CSIS present
                [true, false],  // is USIS present
                [true, false],  // is org name present
                [true, false],  // is local ID present
                [0, 1, 3] // how many note messages are there
        ].combinations()

        csis = (testValues[0] ? randomText : null)
        usis = (testValues[1] ? randomText : null)
        name = (testValues[2] ? randomText : null)

        localId = (testValues[3] ?
                GroovySpy(LocalOrganizationID) {
                    getCode() >> randomText
                    getQualifier() >> randomText
                    getEmpty() >> false // HACK
                } :
                null)
        localIdContent = localId?.toString()

        noteMessageList = ([true] * testValues[4]).collect { randomText }

        xmlParts = [
                csis ? buildTag("CSIS", csis) : null,
                usis ? buildTag("USIS", usis) : null,
                localIdContent ? buildTag("LocalOrganizationID", localIdContent) : null,
                name ? buildTag("OrganizationName", name) : null,
                noteMessageList.collect { buildTag("NoteMessage", it) }
        ].findAll().flatten()
        xmlString = buildTag("Organization", xmlParts.join())

        comment = testValues*.toString().join("/")
    }
}
