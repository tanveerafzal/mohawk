package ca.mohawkcollege.ocastranscript.xml.pesc


import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class LocalOrganizationIDSpec extends Specification {

    @Unroll
    void "test that getChildren fetches all expected objects with #comment"() {
        given: "a local organization ID built from an XML string"
        def localOrganizationID = new LocalOrganizationID(xmlString)

        expect: "the children of the local organization ID match up, in order, to the expected objects"
        localOrganizationID.getChildren().get(0).toString() == localOrganizationIDCode.toString()
        localOrganizationID.getChildren().get(1).toString() == localOrganizationIDQualifier.toString()

        where:
        localOrganizationIDCode | localOrganizationIDQualifier | comment
        "123"                   | "ON"                         | "everything present"
        "123"                   | null                         | "code present"
        null                    | "ON"                         | "qualifier present"
        null                    | null                         | "nothing present"

        xmlLocalOrganizationIDCode = ("<LocalOrganizationIDCode>$localOrganizationIDCode</LocalOrganizationIDCode>")
        xmlLocalOrganizationIDQualifier = ("<LocalOrganizationIDQualifier>$localOrganizationIDQualifier</LocalOrganizationIDQualifier>")

        xmlString = "<LocalOrganizationID>$xmlLocalOrganizationIDCode$xmlLocalOrganizationIDQualifier</LocalOrganizationID>"
    }
}
