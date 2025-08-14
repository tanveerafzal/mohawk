package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.RequestStatus
import ca.mohawkcollege.ocastranscript.ssb.AcademicAward
import ca.mohawkcollege.ocastranscript.ssb.Degree
import ca.mohawkcollege.ocastranscript.ssb.InstitutionalHonor
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import grails.test.spock.IntegrationSpec

class XmlTranscriptIntegrationSpec extends IntegrationSpec {

    def xmlValidationService

    void "test that a TranscriptRequest's XML transcript validates against the schema"() {
        given: "an existing transcript request with a degree"
        assert transcriptRequest
        List<InstitutionalHonor> institutionalHonors = InstitutionalHonor.findAllByPidm(transcriptRequest.pidm)
        List<Degree> degrees = Degree.findAllByPidm(transcriptRequest.pidm)
        List<AcademicAward> allAwards = (institutionalHonors + degrees as List<AcademicAward>)
        def termAwardCount = allAwards.count { it.term }
        def nonTermAwardCount = allAwards.count { !it.term }

        when: "we generate a transcript"
        transcriptRequest.requestStatus = RequestStatus.ReadyToSendTranscript
        def xmlTranscript = transcriptRequest.generateTranscript()

        and: "we get the XML string"
        def foundXml = xmlTranscript.getXml()

        and: "print it out"
        def stringWriter = new StringWriter()
        def node = new XmlParser().parseText(foundXml)
        new XmlNodePrinter(new PrintWriter(stringWriter)).print(node)

        println "XML:"
        println stringWriter.toString()

        then: "the XML is not empty"
        foundXml

        when: "we run the XML through validation"
        def result = xmlValidationService.getError(foundXml)

        then: "no error is produced and an AcademicAward exists"
        !result

        when: "we look for AcademicAward elements in the parsed output"
        def xmlParse = XmlSlurper.newInstance().parseText(foundXml)
        def nonTermAwards = xmlParse.Student.AcademicRecord.AcademicAward
        def termAwards = xmlParse.Student.AcademicRecord.AcademicSession.AcademicAward

        then: "we have the right number of non-term awards"
        nonTermAwards.size() == nonTermAwardCount

        then: "we have the right number of term-specific awards"
        termAwards.size() == termAwardCount

        where:
        transcriptRequest << transcriptListFilter()
    }

    List<TranscriptRequest> transcriptListFilter() {
        List<TranscriptRequest> requestList = []
        int max = 20
        int page = 0

        while (requestList.size() < max) {
            def transcriptRequestList = TranscriptRequest.withCriteria {
                isNotNull("pidm")
                maxResults max
                firstResult page * max
            }
            if (!transcriptRequestList.size()) break

            def requestsWithDegrees = transcriptRequestList.findAll {
                Degree.countByPidm(it.pidm) && it.svrtreq?.svrtreqId
            }

            requestList.addAll(requestsWithDegrees)
            page++
        }

        println 'Found ' + requestList.size() + ' eligible students'

        return requestList
    }

}
