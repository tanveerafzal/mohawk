package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import grails.validation.Validateable
import org.apache.commons.logging.LogFactory

@Validateable
abstract class AbstractXmlTransmission {

    protected static final log = LogFactory.getLog(this)
    TranscriptRequest transcriptRequest
    ErrorType errorType
    String errorMessage
    String rawXml

    static constraints = {
        // This is required because another class wants it nullable in specific situations
        transcriptRequest nullable: false

        errorType nullable: true
        errorMessage nullable: true
        rawXml nullable: true
        xml nullable: true
    }

    protected static final Map<String, String> xsiNamespace = [
            'xsi': "http://www.w3.org/2001/XMLSchema-instance"
    ]

    protected static final Map<String, String> optionalNamespaces = [
            'TrnAck' : "urn:org:pesc:message:TranscriptAcknowledgment:v1.1.0",
            "TrnResp": "urn:org:pesc:message:TranscriptResponse:v1.1.0",
            "CollTrn": "urn:org:pesc:message:CollegeTranscript:v1.3.0",
            'AcRec'  : "urn:org:pesc:sector:AcademicRecord:v1.6.0",
            'core'   : "urn:org:pesc:core:CoreMain:v1.10.0",
            "ext"    : "urn:ca:ocas:useextensions"
    ]

    void setXml(String xml) { this.rawXml = xml}
    abstract String getXml()

    abstract List<String> getExpectedResponseFields()

    abstract Map<String, String> getPayload()

    abstract String getApiEndpoint()

    String getRequestId() { transcriptRequest?.requestTrackingId }

    /**
     * Given a list of short namespace names, return a map of namespaces suitable for passing into a root XML element.
     * This will include the XMLSchema namespace that is always required, as well as whichever namespaces appear in the
     * given list.
     *
     * @param names
     * @return
     */
    protected static Map<String, String> getNamespaces(List<String> names) {
        (xsiNamespace + (optionalNamespaces.findAll { names.contains(it.key) })).
                collectEntries {
                    [("xmlns:${it.key}" as String): it.value]
                } as Map<String, String>
    }
}
