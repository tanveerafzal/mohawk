package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.OcasApi
import ca.mohawkcollege.ocastranscript.xml.pesc.HoldReason
import ca.mohawkcollege.ocastranscript.xml.pesc.ResponseStatus
import grails.validation.Validateable
import groovy.util.slurpersupport.NodeChildren
import groovy.xml.StreamingMarkupBuilder
import grails.util.Holders

@Validateable
class XmlResponse extends AbstractXmlTransmission {

    HoldReason holdReason
    String noteMessage
    ResponseStatus ocasResponseStatus

    static constraints = {
        importFrom AbstractXmlTransmission

        holdReason nullable: true
        noteMessage nullable: true, validator: requiredIfOther
    }

    static requiredIfOther = { String value, XmlResponse object ->
        if (!value && object.holdReason == HoldReason.Other) {
            return 'requiredIfOther'
        }
    }

    /**
     * Return the xml to send out. This lazy loader generates the XML on first access, and saves it for later use.
     *
     * @return String XML output
     */
    @Override
    String getXml() {
        if (!rawXml) {
            if (!transcriptRequest) return null

            def organizationConfig = Holders.grailsApplication.config.mohawkcollege.ocastranscript.Organization

            def xmlParse = new XmlSlurper().parseText(transcriptRequest.originalXml)
            def transData = xmlParse.TransmissionData

            Date today = new Date()
            NodeChildren destination = xmlParse.TransmissionData.Source.Organization.MutuallyDefined as NodeChildren
            def destinationCode = (destination.size() > 0 && destination[0].toString() == 'OUAC') ? 'OUAC' : 'OCAS'
            def destinationName = (destination.size() > 0 && destination[0].toString() == 'OUAC') ? 'OUAC' : 'OCAS Application Services Inc.'
            def studentInfo = xmlParse.Request.RequestedStudent

            def generatedXml = new StreamingMarkupBuilder().bind {
                'TrnResp:TranscriptResponse'(getNamespaces(['TrnResp'])) {
                    'TrnResp:TransmissionData'() {
                        DocumentID() {
                            mkp.yield transData?.DocumentID?.toString()?.replace('-','')
                        }
                        CreatedDateTime() {
                            mkp.yield today.format("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                        }
                        DocumentTypeCode() {
                            mkp.yield 'Response'
                        }
                        TransmissionType() {
                            mkp.yield 'Original'
                        }
                        Source() {
                            Organization() {
                                CSIS() {
                                    mkp.yield organizationConfig.CSIS
                                }
                                OrganizationName() {
                                    mkp.yield organizationConfig.OrganizationName
                                }
                                Contacts() {
                                    Address() {
                                        AddressLine() {
                                            mkp.yield organizationConfig.AddressLine
                                        }
                                        City() {
                                            mkp.yield organizationConfig.City
                                        }
                                        StateProvinceCode() {
                                            mkp.yield organizationConfig.StateProvinceCode
                                        }
                                        PostalCode() {
                                            mkp.yield organizationConfig.PostalCode
                                        }
                                    }
                                    Phone() {
                                        PhoneNumber() {
                                            mkp.yield organizationConfig.PhoneNumber
                                        }
                                    }
                                    Email() {
                                        EmailAddress() {
                                            mkp.yield organizationConfig.EmailAddress
                                        }
                                    }
                                }
                            }
                        }
                        Destination() {
                            Organization() {
                                MutuallyDefined() {
                                    mkp.yield destinationCode
                                }
                                OrganizationName() {
                                    mkp.yield destinationName
                                }

                            }
                        }
                        DocumentProcessCode() {
                            mkp.yield organizationConfig.DocumentProcessCode
                        }
                        DocumentCompleteCode() {
                            mkp.yield organizationConfig.DocumentCompleteCode
                        }
                        RequestTrackingID() {
                            mkp.yield transData.RequestTrackingID.toString()
                        }
                        NoteMessage() {
                            mkp.yield organizationConfig.NoteMessage
                        }
                    }

                    'TrnResp:Response'() {
                        CreatedDateTime() {
                            mkp.yield today.format("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                        }
                        RequestTrackingID() {
                            mkp.yield transData.RequestTrackingID.toString()
                        }
                        ResponseStatus() {
                            mkp.yield ocasResponseStatus
                        }
                        if (holdReason != null) {
                            ResponseHold() {
                                HoldReason() {
                                    mkp.yield holdReason
                                }
                                if (noteMessage != null) {
                                    NoteMessage() {
                                        mkp.yield noteMessage
                                    }
                                }
                            }
                        }

                        mkp.yield studentInfo
                    }
                }
            }

            def tmpXML = generatedXml.toString()
            def declarationEnd = tmpXML.indexOf('?>')

            if (declarationEnd != -1)
                tmpXML = tmpXML.substring(declarationEnd + 2)

            rawXml = tmpXML
        }

        rawXml
    }

    @Override
    List<String> getExpectedResponseFields() { ["RequestID"] }

    @Override
    Map<String, String> getPayload() { [PESCXML: xml] }

    @Override
    String getApiEndpoint() { OcasApi.ENDPOINT_POST_RESPONSE }
}
