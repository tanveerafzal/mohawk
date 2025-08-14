package ca.mohawkcollege.ocastranscript.xml.pesc


import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import ca.mohawkcollege.ocastranscript.xml.XmlAcknowledgment
import grails.test.spock.IntegrationSpec
import spock.lang.Unroll

class TranscriptAcknowledgmentIntegrationSpec extends IntegrationSpec {

    def xmlValidationService

    def setup() {
    }

    def cleanup() {
    }

    void "test that TranscriptAcknowledgment XML output validates against the schema"() {
        given: "a populated transcript acknowledgment"
        TranscriptAcknowledgment ack = new TranscriptAcknowledgment()
        ack.requestTrackingId = requestId
        ack.createdDateTime = new Date()
        ack.transmissionData.transmissionType = TransmissionType.Original
        ack.transmissionData.source = Organization.MOHAWK
        ack.transmissionData.destination = Organization.MOHAWK
        ack.person.schoolAssignedPersonId = "ID"
        ack.person.dateOfBirth = new Date()
        ack.academicAwardTotal = 42
        ack.courseTotal = 1

        when: "we get the object's XML representation"
        def foundXml = ack.toXml()
        println "Found XML:"
        println foundXml

        then: "nothing bad happens"
        noExceptionThrown()

        when: "we run the XML against schema validation"
        def result = xmlValidationService.getError(foundXml)

        then: "the result is a pass"
        result == null

        where:
        requestId = "requestId"
        documentId = "generatedDocumentId"
        xmlString = "<Acknowledgment></Acknowledgment>"
    }

    @Unroll
    void "test that an acknowledgment generated from transcript #transcriptId produces valid XML"() {
        given: "a transcript received from OCAS"
        assert transcript
        println "Transcript XML:"
        println transcript.xml

        when: "we produce an acknowledgment"
        XmlAcknowledgment ack = transcript.generateAcknowledgment()

        and: "we get the object's XML representation"
        def foundXml = ack.xml
        println "Acknowledgment XML:"
        println foundXml

        and: "we run the XML against schema validation"
        def result = xmlValidationService.getError(foundXml)

        then: "the resultant XML passes validation"
        result == null

        where:
        transcript << OcasTranscript.listOrderByDateCreated(order: "desc", max: 5)

        transcriptId = transcript.requestTrackingId
    }

    // OCASSYNC-122
    @Unroll
    void "OCASSYNC-122 test that an acknowledgment generated from transcript #transcriptId has the right source and destination"() {
        given: "a transcript received from OCAS"
        assert transcript
        println "Transcript XML:"
        println transcript.xml

        when: "we produce an acknowledgment"
        def transcriptAcknowledgment = transcript.generateAcknowledgment().transcriptAcknowledgment

        and: "we get the object's source and destination"
        def acknowledgmentSource = transcriptAcknowledgment.transmissionData.source
        println "Transcript source XML:"
        println transcript.source.toXml()
        println "Acknowledgment source XML:"
        println acknowledgmentSource.toXml()
        def acknowledgmentDestination = transcriptAcknowledgment.transmissionData.destination
        println "Transcript destination XML:"
        println transcript.destination.toXml()
        println "Acknowledgment destination XML:"
        println acknowledgmentDestination.toXml()

        then: "the acknowledgment's destination is the same as the transcript's source"
        acknowledgmentDestination
        acknowledgmentDestination == transcript.source

        and: "the acknowledgment's source is Mohawk"
        acknowledgmentSource
        acknowledgmentSource == Organization.MOHAWK

        where:
        transcript << OcasTranscript.listOrderByDateCreated(order: "desc", max: 5)

        transcriptId = transcript.requestTrackingId
    }

    // OCASSYNC-121
    @Unroll
    void "OCASSYNC-121 test that an acknowledgment generated from transcript #transcriptId has the right document type"() {
        given: "a transcript received from OCAS"
        assert transcript
        println "Transcript XML:"
        println transcript.xml

        when: "we produce an acknowledgment"
        def transcriptAcknowledgment = transcript.generateAcknowledgment().transcriptAcknowledgment
        println "Found XML:"
        println transcriptAcknowledgment.toXml()

        and: "we get the object's document type"
        def foundDocType = transcriptAcknowledgment.transmissionData.documentType

        then: "the acknowledgment's document type is correct"
        foundDocType == DocumentType.Acknowledgment

        where:
        transcript << OcasTranscript.listOrderByDateCreated(order: "desc", max: 5)

        transcriptId = transcript.requestTrackingId
    }
}
