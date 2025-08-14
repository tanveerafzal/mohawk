package ca.mohawkcollege.ocastranscript.xml.pesc


import ca.mohawkcollege.ocastranscript.XmlTransmissionService
import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition "AcRec:TransmissionData"
 *
 * Requirements:
 *
 * 1. documentId property, string 1-35, from DocumentID, required
 * 2. createdDateTime, dateTime, from CreatedDateTime, required
 * 3. documentType, type DocumentType, from DocumentType, required
 * 4. transmissionType, type TransmissionType, from TransmissionType, required
 * 5. source, xml.pesc.Organization, from Source, required
 * 6. destination, xml.pesc.Organization, from Destination, required
 * 7. test, Boolean, from DocumentProcessCode (assumption: documents are from Prod unless specified)
 * 8. official, Boolean, from DocumentOfficialCode (assumption: documents are unofficial unless specified)
 * 9. partial, Boolean, from DocumentCompleteCode (assumption: documents are complete unless specified)
 * 10. requestTrackingId, String 1-35, from RequestTrackingID
 * 11. noteMessages, List<String>, length 1-80 chars each, from NoteMessage
 *
 * 12. helper method replyToSender
 *
 * Note that UserDefinedExtensions is treated as unused
 */
@InheritConstructors
class TransmissionData extends XmlFragment {
    final String tag = "TransmissionData"

    List<XmlFragment> getChildren() {
        [
                new XmlCData(getDocumentId() as String).wrapWith("DocumentID"),
                new XmlCData(getCreatedDateTime(), Format.TIMESTAMP).wrapWith("CreatedDateTime"),
                new XmlCData(getDocumentType() as String).wrapWith("DocumentTypeCode"),
                new XmlCData(getTransmissionType() as String).wrapWith("TransmissionType"),
                getSource()?.withTag("Organization")?.wrapWith("Source"),
                getDestination()?.withTag("Organization")?.wrapWith("Destination"),
                new XmlCData(getRequestTrackingId() as String).wrapWith("RequestTrackingID"),
                getNoteMessages()*.collect { new XmlCData(it).wrapWith("NoteMessage") }
        ].flatten().findAll() as List<XmlFragment>
    }

    static final String DOCUMENT_PROCESS_CODE_TEST = "TEST"
    static final String DOCUMENT_OFFICIAL_CODE_OFFICIAL = "Official"
    static final String DOCUMENT_COMPLETE_CODE_PARTIAL = "Partial"

    String documentId
    Date createdDateTime
    DocumentType documentType
    TransmissionType transmissionType
    Organization source
    Organization destination
    Boolean test
    Boolean official
    Boolean partial
    String requestTrackingId

    String getDocumentId() {
        if (!documentId) {
            documentId = getString("DocumentID") ?: XmlTransmissionService.generateDocumentId()
        }
        documentId
    }

    Date getCreatedDateTime() {
        createdDateTime ?: getDate("CreatedDateTime")
    }

    DocumentType getDocumentType() {
        documentType ?: getString("DocumentTypeCode").with { it ? DocumentType.valueOf(it) : null }
    }

    TransmissionType getTransmissionType() {
        transmissionType ?: getString("TransmissionType").with { it ? TransmissionType.valueOf(it) : null }
    }

    Organization getSource() {
        if (!source) source = getChild("Source.Organization")?.with { it ? new Organization(it) : null }
        source
    }

    Organization getDestination() {
        if (!destination) destination = getChild("Destination.Organization")?.with { it ? new Organization(it) : null }
        destination
    }

    Boolean getTest() {
        (test != null) ? test : (getString("DocumentProcessCode") == DOCUMENT_PROCESS_CODE_TEST)
    }

    Boolean getOfficial() {
        (official != null) ? official : (getString("DocumentOfficialCode") == DOCUMENT_OFFICIAL_CODE_OFFICIAL)
    }

    Boolean getPartial() {
        (partial != null) ? partial : (getString("DocumentCompleteCode") == DOCUMENT_COMPLETE_CODE_PARTIAL)
    }

    String getRequestTrackingId() {
        requestTrackingId ?: getString("RequestTrackingID")
    }

    String getUserDefinedExtensions() {
        this.getString("UserDefinedExtensions")
    }

    TransmissionData createReply() {
        TransmissionData reply = new TransmissionData()
        reply.destination = this.source
        reply.source = Organization.MOHAWK
        reply
    }
}