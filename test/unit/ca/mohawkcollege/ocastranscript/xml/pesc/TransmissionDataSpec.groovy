package ca.mohawkcollege.ocastranscript.xml.pesc


import ca.mohawkcollege.ocastranscript.XmlTransmissionService
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
class TransmissionDataSpec extends Specification {

//    * Requirements:
//    *
//    * 1. documentId property, string 1-35, from DocumentID, required
//    * 2. createdDateTime, Date, from CreatedDateTime, required
//    * 3. documentType, type DocumentType, from DocumentType, required
//    * 4. transmissionType, type TransmissionType, from TransmissionType, required
//    * 5. source, xml.pesc.Organization, from Source, required
//    * 6. destination, xml.pesc.Organization, from Destination, required
//    * 7. test, Boolean, from DocumentProcessCode (assumption: documents are from Prod unless specified)
//    * 8. official, Boolean, from DocumentOfficialCode (assumption: documents are unofficial unless specified)
//    * 9. partial, Boolean, from DocumentCompleteCode (assumption: documents are complete unless specified)
//    * 10. requestTrackingId, String 1-35, from RequestTrackingID
//    *
//    * 11. helper method replyToSender

    // The XML tag name associated with each property
    @Shared
    Map propertyTagMap = [documentId       : "DocumentID",
                          createdDateTime  : "CreatedDateTime",
                          documentType     : "DocumentTypeCode",
                          requestTrackingId: "RequestTrackingID",
                          source           : "Source.Organization",
                          destination      : "Destination.Organization",
                          transmissionType : "TransmissionType",
                          test             : "DocumentProcessCode",
                          official         : "DocumentOfficialCode",
                          partial          : "DocumentCompleteCode"]

    // The XML value that should be interpreted as true for each property
    @Shared
    Map propertyTrueMap = [test: "TEST", official: "Official", partial: "Partial"]

    TransmissionData transmissionData

    def setup() {
        GroovyMock(XmlTransmissionService, global: true)
    }

    @Unroll
    void "test that #propertyName is parsed as String from XML with tag #comment"() {
        given: "a transmission data object built from an XML string"
        transmissionData = new TransmissionData(xmlString)
        println "xmlString: $xmlString"

        expect: "the property value to be as predicted"
        transmissionData."$propertyName" == expectedValue

        where:
        testArray << [["requestTrackingId"], [true, false]].combinations()

        propertyName = testArray[0]
        tagExists = testArray[1]
        xmlValue = tagExists ? RandomStringUtils.randomAlphanumeric(5) : null
        xmlTag = propertyTagMap[propertyName]
        expectedValue = xmlValue ?: null

        xmlContent = (xmlValue ? "<$xmlTag>$xmlValue</$xmlTag>" : "")
        xmlString = "<blah>$xmlContent</blah>"

        comment = (xmlValue ? "populated" : "missing")
    }

    @Unroll
    void "test that #propertyName is parsed as Date from XML with tag #comment"() {
        given: "a transmission data object built from an XML string"
        transmissionData = new TransmissionData(xmlString)

        expect: "the property value to be as predicted"
        transmissionData."$propertyName" == expectedValue

        where:
        testArray << [["createdDateTime"], [true, false]].combinations()

        propertyName = testArray[0] as String
        tagExists = testArray[1] as Boolean
        xmlTag = propertyTagMap[propertyName]

        // Random date:
        rand = Random.newInstance()
        expectedValue = tagExists ? new Date(rand.nextInt(9999), rand.nextInt(11), rand.nextInt(27) + 1) : null
        xmlValue = expectedValue ? Format.DATE.simpleDateFormat.format(expectedValue) : null

        xmlContent = (xmlValue ? "<$xmlTag>$xmlValue</$xmlTag>" : "")
        xmlString = "<blah>$xmlContent</blah>"

        comment = (xmlValue == null ? "missing" : xmlValue ? "populated" : "empty")
    }

    @Unroll
    void "test that documentType is parsed as enum from XML with tag #comment"() {
        given: "a transmission data object built from an XML string"
        transmissionData = new TransmissionData(xmlString)

        expect: "the property value to be as predicted"
        transmissionData.documentType == expectedValue

        where:
        testValue << ([null] + DocumentType.values()).flatten()

        tagExists = testValue as Boolean
        xmlTag = propertyTagMap["documentType"]
        xmlValue = testValue?.toString() ?: ""
        expectedValue = testValue

        xmlContent = (xmlValue ? "<$xmlTag>$xmlValue</$xmlTag>" : "")
        xmlString = "<blah>$xmlContent</blah>"

        comment = (testValue == null ? "missing" : "populated")
    }

    @Unroll
    void "test that transmissionType is parsed as enum from XML with tag #comment"() {
        given: "a transmission data object built from an XML string"
        transmissionData = new TransmissionData(xmlString)

        expect: "the property value to be as predicted"
        transmissionData.transmissionType == expectedValue

        where:
        testValue << ([null] + TransmissionType.values()).flatten()

        tagExists = testValue as Boolean
        xmlTag = propertyTagMap["transmissionType"]
        xmlValue = testValue?.toString() ?: ""
        expectedValue = testValue

        xmlContent = (xmlValue ? "<$xmlTag>$xmlValue</$xmlTag>" : "")
        xmlString = "<blah>$xmlContent</blah>"

        comment = (testValue == null ? "missing" : "populated")
    }

    @Unroll
    void "test that #comment Organization property #propertyName is parsed from XML"() {
        given: "mocks on the Organization class"
        println "XML string: [$xmlString]"
        def organizationMock = GroovyMock(Organization)
        GroovyMock(Organization, global: true)

        //the constructor of Organization is called or not
        (tagExists ? 1 : 0) * Organization."<init>"(*_) >> organizationMock

        when: "we create a transmission data object from XML"
        transmissionData = new TransmissionData(xmlString)

        and: "we fetch the organization object in question"
        def theOrganization = transmissionData."$propertyName"

        then: "the property contains the resultant instance of Organization"
        noExceptionThrown()
        theOrganization == (tagExists ? organizationMock : null)

        where:
        testArray << [["source", "destination"], [true, false]].combinations()
        propertyName = testArray[0] as String
        xmlTags = propertyTagMap[propertyName]
        tagExists = testArray[1] as Boolean
        xmlValue = RandomStringUtils.randomAlphanumeric(5)

        xmlContent = tagExists ? buildXml(xmlTags, xmlValue) : ""
        xmlString = "<bleh>$xmlContent</bleh>"

        comment = tagExists ? "populated" : "missing"
    }

    @Unroll
    void "test that boolean #propertyName is parsed as a Boolean from XML with value #comment"() {
        given: "a transmission data object built from an XML string"
        transmissionData = new TransmissionData(xmlString)

        expect: "the property value to be as predicted"
        transmissionData."$propertyName" == expectedValue

        where:
        testArray << [["test", "official", "partial"], [true, false]].combinations()
        expectedValue = testArray[1]
        propertyName = testArray[0] as String
        xmlTag = propertyTagMap[propertyName]
        tagExists = expectedValue != null
        xmlValue = expectedValue ? propertyTrueMap[propertyName] : RandomStringUtils.randomAlphanumeric(5)
        xmlContent = tagExists ? "<$xmlTag>$xmlValue</$xmlTag>" : ""
        xmlString = "<blah>$xmlContent</blah>"

        comment = (expectedValue == null ? "missing" : expectedValue)
    }

    void "test that if present, the document ID is populated from the initial XML"() {
        0 * XmlTransmissionService.generateDocumentId()

        given: "a TransmissionData with an empty document ID"
        def tData = new TransmissionData(initialXml)

        when: "we query the documentId"
        def foundId = tData.documentId

        then: "the result comes from a call to XmlTransmissionService"
        foundId == providedId

        where:
        providedId = "provided"
        initialXml = "<TransmissionData><DocumentID>$providedId</DocumentID></TransmissionData>"
    }

    void "test that if the XML element is empty, we get our documentId from the service"() {
        1 * XmlTransmissionService.generateDocumentId() >> generatedId

        given: "a TransmissionData with an empty document ID"
        def tData = new TransmissionData(initialXml)

        when: "we query the documentId"
        def foundId = tData.documentId

        then: "the result comes from a call to XmlTransmissionService"
        foundId == generatedId

        where:
        generatedId = "okay"
        initialXml = "<TransmissionData><DocumentID></DocumentID></TransmissionData>"
    }

    private static String buildXml(String joinedTags, String value) {
        def tags = joinedTags.split(/\./)
        [
                tags.collect { "<$it>" },
                value,
                tags.reverse().collect { "</$it>" }
        ].flatten().join()
    }
}