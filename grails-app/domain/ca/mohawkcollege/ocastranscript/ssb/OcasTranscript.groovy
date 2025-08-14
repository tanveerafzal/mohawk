package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionType
import ca.mohawkcollege.ocastranscript.baseline.Goradid
import ca.mohawkcollege.ocastranscript.baseline.Spriden
import ca.mohawkcollege.ocastranscript.type.BooleanYNType
import ca.mohawkcollege.ocastranscript.xml.XmlAcknowledgment
import ca.mohawkcollege.ocastranscript.xml.pesc.CollegeTranscript
import ca.mohawkcollege.ocastranscript.xml.pesc.Organization
import ca.mohawkcollege.ocastranscript.xml.pesc.Student
import org.codehaus.groovy.grails.web.json.JSONElement

class OcasTranscript {

    String documentId
    String requestTrackingId
    TransmissionType transmissionType
    Boolean testOnly
    Boolean partialContent
    String oen
    String ocasApplicationNumber
    BigInteger pidm
    String xml
    // Grails autofilled timestamps
    Date dateCreated
    Date lastUpdated
    // transient properties
    String mohawkId
    CollegeTranscript collegeTranscript

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "OCAS_TRANSCRIPT"
        testOnly type: BooleanYNType
        partialContent type: BooleanYNType
        xml type: "materialized_clob"
    }

    static constraints = {
        documentId unique: true
        requestTrackingId nullable: true
        oen nullable: true
        ocasApplicationNumber nullable: true
        mohawkId nullable: true
        pidm nullable: true
    }

    static transients = ['student', 'mohawkId', 'collegeTranscript', 'notes', 'source', 'destination']

    /**
     * Generate and return an XML response object from this request. This will also set the responseXml property in
     * the current request object
     */
    XmlAcknowledgment generateAcknowledgment() { new XmlAcknowledgment(ocasTranscript: this) }

    CollegeTranscript getCollegeTranscript() {
        if (!this.@collegeTranscript && xml) {
            parseXml()
        }
        this.@collegeTranscript
    }

    Student getStudent() { getCollegeTranscript().student }

    Organization getSource() { getCollegeTranscript().transmissionData.source }

    Organization getDestination() { getCollegeTranscript().transmissionData.destination }

    List<String> getNotes() { getCollegeTranscript().noteMessages }

    void parseXml() { parseText(xml) }

    void parseText(String xmlString) {
        if (!xml) xml = xmlString

        collegeTranscript = new CollegeTranscript(xml)

        collegeTranscript.transmissionData.with { td ->
            this.documentId = td.documentId
            this.transmissionType = td.transmissionType
            this.testOnly = td.test
            this.partialContent = td.partial
            this.requestTrackingId = td.requestTrackingId
        }

        oen = student.ontarioEducationNumber
        ocasApplicationNumber = student.ocasApplicationNumber
        if (oen) {
            // get pidm from OEN
            pidm = (BigInteger) Goradid.findByGoradidAdidCodeAndGoradidAdditionalId('OEN', oen)?.goradidPidm
        }
        if (ocasApplicationNumber && !pidm) {
            // get pidm from OCAS number
            pidm = (BigInteger) Spriden.findBySpridenId(ocasApplicationNumber)?.spridenPidm
        }

        this
    }

    /**
     * Given a JSONElement of the type returned by the OCAS API, return an OcasTranscript object populated with all
     * necessary data from the JSON XML
     *
     * 1. Given a JSONElement
     * 2. of the type returned by the OCAS API
     * 3. return an OcasTranscript
     * 4. populated with all necessary data
     *
     * @param json
     * @return
     */
    static OcasTranscript createByJsonElement(JSONElement json) {
        String originalXml = json.PESCXml.toString()
        String requestId = json.RequestID.toString()

        // We may have already received this transcript
        OcasTranscript transcript = findOrCreateByRequestTrackingId(requestId)
        if (!transcript.id) {
            // This is a new object. Let's populate it.
            transcript.xml = originalXml
            transcript.parseXml()
        }

        transcript
    }
}