package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.OcasApi
import ca.mohawkcollege.ocastranscript.XmlTransmissionService
import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.Organization
import ca.mohawkcollege.ocastranscript.xml.pesc.TranscriptAcknowledgment
import grails.validation.Validateable

@Validateable
class XmlAcknowledgment extends AbstractXmlTransmission {

    OcasTranscript ocasTranscript

    @Lazy
    TranscriptAcknowledgment transcriptAcknowledgment = {
        this.ocasTranscript.with { transcript ->
            TranscriptAcknowledgment.newInstance().with { ack ->
                ack.transmissionData.with { tData ->
                    tData.destination = transcript?.source
                    tData.source = Organization.MOHAWK
                    tData.documentId = XmlTransmissionService.generateDocumentId()
                    tData.requestTrackingId = transcript?.requestTrackingId
                    tData.createdDateTime = DateUtils.now
                }
                ack.academicAwardTotal = transcript?.collegeTranscript?.academicAwardTotal
                ack.courseTotal = transcript?.collegeTranscript?.courseTotal

                ack
            }
        }
    }()

    static constraints = {
        transcriptRequest nullable: true
        errorType nullable: true
        errorMessage nullable: true
        rawXml nullable: true
        xml nullable: true
    }

    String getRequestId() { transcriptAcknowledgment?.requestTrackingId }

    @Override
    String getXml() { transcriptAcknowledgment?.toXml() }

    @Override
    List<String> getExpectedResponseFields() { ["RequestID"] }

    @Override
    Map<String, String> getPayload() { [PESCXML: xml] }

    @Override
    String getApiEndpoint() { OcasApi.ENDPOINT_POST_ACKNOWLEDGMENT }
}
