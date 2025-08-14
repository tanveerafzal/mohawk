package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.ErrorType
import ca.mohawkcollege.ocastranscript.OcasApi
import ca.mohawkcollege.ocastranscript.RequestStatus
import ca.mohawkcollege.ocastranscript.baseline.Term
import ca.mohawkcollege.ocastranscript.ssb.*
import ca.mohawkcollege.ocastranscript.xml.pesc.Format
import grails.util.Holders
import grails.validation.Validateable
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

import java.text.DecimalFormat

@Validateable
class XmlTranscript extends AbstractXmlTransmission {

    static constraints = {
        importFrom AbstractXmlTransmission

    }

    @Lazy
    List<InProgressCourses> coursesInProgress = {
        InProgressCourses.findAllByPidm(transcriptRequest?.pidm)
    }()

    @Lazy
    List<AcademicHistory> pastCourses = {
        AcademicHistory.findAllByPidm(transcriptRequest?.pidm)
    }()

    Boolean getHasAcademicHistory() { !pastCourses?.empty }

    @Lazy
    List<CourseRecord> allCourses = {
        (coursesInProgress + pastCourses) as List<CourseRecord>
    }()

    // generate the xml transcript for this student
    @Override
    String getXml() {
        if (!rawXml) {
            if (!transcriptRequest) return null
            if (transcriptRequest.requestStatus != RequestStatus.ReadyToSendTranscript) return null

            ConfigObject organizationConfig = Holders.grailsApplication.config.mohawkcollege.ocastranscript.Organization

            // Get the student's MohawkId
            String mohawkId = transcriptRequest.svrtreq.svrtreqId

            if (!mohawkId) {
                // We can't do anything with this
                errorMessage = "Unknown PIDM associated with transcript request"
                errorType = ErrorType.XmlGenerationError
                return null
            }

            if (!hasAcademicHistory) {
                errorMessage = "No academic history found"
                errorType = ErrorType.AcademicHistoryNotFound
                return null
            }

            List<InstitutionalHonor> institutionalHonors = InstitutionalHonor.findAllByPidm(transcriptRequest.pidm)
            List<Degree> degrees = Degree.findAllByPidm(transcriptRequest.pidm)
            List<AcademicAward> allAwards = (institutionalHonors + degrees as List<AcademicAward>)

            List<AcademicHistory> gpaIncludedCourses = pastCourses.findAll { it.includeInGpa }
            BigDecimal weightedGradePointTotal = gpaIncludedCourses*.weightedGrade.sum() as BigDecimal ?: 0
            BigDecimal totalCreditHours = gpaIncludedCourses*.creditHours.sum() as BigDecimal ?: 0
            BigDecimal totalCreditHoursCompleted = gpaIncludedCourses*.creditHoursCompleted.sum() as BigDecimal ?: 0

            BigDecimal weightedGpa = (weightedGradePointTotal && totalCreditHours) ? (weightedGradePointTotal / totalCreditHours) : 0
            BigDecimal roundedGpa = Math.round(weightedGpa * 10) / 10
            String institutionalGpa = ((totalCreditHours == 0) ? '0.0' : (new DecimalFormat("#0.0").format(roundedGpa)))

            GPathResult xmlParse = new XmlSlurper().parseText(transcriptRequest.originalXml)
            GPathResult transData = xmlParse.TransmissionData // orig trans data
            GPathResult destination = xmlParse.TransmissionData.Source.Organization.MutuallyDefined
            GPathResult personData = xmlParse.Request.RequestedStudent.Person // orig person

            String destinationCode = (destination.size() > 0 && destination[0].toString() == 'OUAC') ? 'OUAC' : 'OCAS'
            String destinationName = (destination.size() > 0 && destination[0].toString() == 'OUAC') ? 'OUAC' : 'OCAS Application Services Inc.'


            if (personData.SchoolAssignedPersonID.size() > 0 && personData.SchoolAssignedPersonID[0] != mohawkId) {
                personData.SchoolAssignedPersonID[0] = mohawkId
            }

            DecimalFormat gpaFormat = new DecimalFormat("#0.0")

            Date today = new Date()
            def generatedXml = new StreamingMarkupBuilder().bind {
                'ns0:CollegeTranscript'('xmlns:ns0':"urn:org:pesc:message:CollegeTranscript:v1.3.0") {
                    TransmissionData() {
                        DocumentID() {
                            mkp.yield transData?.DocumentID?.toString()?.replace('-','')
                        }
                        CreatedDateTime() {
                            mkp.yield XmlFragment.FORMAT_DATE_TIME.format(today)
                        }
                        DocumentTypeCode() {
                            mkp.yield 'RequestedRecord'
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

                    Student() {
                        mkp.yield personData
                        AcademicRecord() {
                            // Awards not associated with any specific term
                            allAwards.findAll { !it.term }.each { gradAwardItem ->
                                AcademicAward() {
                                    AcademicAwardLevel() {
                                        mkp.yield gradAwardItem.level
                                    }
                                    if (gradAwardItem.date != null) {
                                        AcademicAwardDate() {
                                            mkp.yield Format.DATE.simpleDateFormat.format(gradAwardItem.date)
                                        }
                                    }
                                    AcademicAwardTitle() {
                                        mkp.yield gradAwardItem.title
                                    }
                                    AcademicAwardProgram() {
                                        AcademicProgramName() {
                                            mkp.yield(gradAwardItem.programCode + ' ' + gradAwardItem.programName.replaceAll("&", "&amp;").take(60))
                                        }
                                    }

                                }
                            }

                            AcademicSummary() {
                                GPA() {
                                    CreditHoursAttempted {
                                        mkp.yield '0'
                                    }
                                    CreditHoursEarned() {
                                        mkp.yield totalCreditHoursCompleted.toString()
                                    }
                                    CreditUnit() {
                                        mkp.yield 'Semester'
                                    }
                                    GradePointAverage() {
                                        mkp.yield institutionalGpa
                                    }
                                    CreditHoursforGPA() {
                                        mkp.yield totalCreditHours.toString()
                                    }
                                }
                            }

                            // collect all terms
                            (([pastCourses, coursesInProgress, allAwards].flatten() as List<HasBannerTerm>)*.term.findAll().toSet().sort {
                                it.stvtermCode
                            }).each { Term termObj ->

                                List<CourseRecord> termCourses = allCourses.findAll {
                                    it.termCode == termObj.stvtermCode
                                }.sort { it.courseTitle }

                                // Cannot use null-safe here due to Groovy bug: https://stackoverflow.com/questions/3807280/cannot-access-first-element-from-an-empty-list-when-using-grails-sortedset
                                CourseRecord firstCourse = null
                                if (!termCourses.isEmpty()){
                                    firstCourse = termCourses.first()
                                }
                                //CourseRecord firstCourse = termCourses?.first()

                                String majorDesc = firstCourse?.programCode + ' ' + firstCourse?.programName

                                List<AcademicAward> termAwards = allAwards.findAll { it.term == termObj }

                                // Collection of course histories that should contribute to GPA
                                List<AcademicHistory> includeInGpa = pastCourses.findAll {
                                    it.includeInGpa && it.termCode == termObj.stvtermCode
                                }.sort { it.courseTitle }

                                BigDecimal totalPoints = includeInGpa.collect { it.creditHours * it.numericGrade }.sum() as BigDecimal
                                BigDecimal totalCredits = includeInGpa*.creditHours.sum() as BigDecimal

                                Date startDate = termObj.stvtermStartDate

                                AcademicSession() {
                                    AcademicSessionDetail() {
                                        SessionDesignator() {            // SCHEMA NOT Validated
                                            mkp.yield termObj.stvtermCode[0..3] + '-' + startDate.format("MM")
                                        }
                                        SessionName() {
                                            mkp.yield termObj.stvtermDesc
                                        }
                                        SessionType() {
                                            mkp.yield 'Semester'
                                        }
                                        SessionBeginDate() {
                                            mkp.yield XmlFragment.FORMAT_DATE.format(startDate)
                                        }
                                        SessionEndDate() {
                                            mkp.yield XmlFragment.FORMAT_DATE.format(termObj.stvtermEndDate)
                                        }
                                    }
                                    AcademicProgram() {
                                        AcademicProgramName() {
                                            mkp.yield majorDesc?.replaceAll("&", "&amp;")?.take(60)
                                        }
                                    }

                                    // All awards for this term
                                    termAwards.each { AcademicAward award ->
                                        AcademicAward() {
                                            AcademicAwardLevel() {
                                                mkp.yield award.level
                                            }
                                            AcademicAwardDate() {
                                                mkp.yield Format.DATE.simpleDateFormat.format(award.date)
                                            }
                                            AcademicAwardTitle() {
                                                mkp.yield award.title
                                            }
                                            if (award.programDescription) {
                                                AcademicAwardProgram() {
                                                    AcademicProgramName() {
                                                        mkp.yield(award.programDescription.replaceAll("&", "&amp;").take(60))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    termCourses.each { courseRecord ->
                                        Course() {
                                            CourseCreditBasis() {
                                                mkp.yield courseRecord.creditBasis
                                            }
                                            CourseCreditValue() {
                                                mkp.yield courseRecord.creditHours.toString()
                                            }
                                            CourseCreditEarned() {
                                                mkp.yield courseRecord.creditHoursCompleted.toString()
                                            }
                                            if (courseRecord.creditHoursCompleted) {
                                                CourseAcademicGrade() {
                                                    mkp.yield courseRecord.gradeCode
                                                }
                                            } else if (courseRecord.gradeScale) {
                                                CourseAcademicGradeScaleCode() {
                                                    mkp.yield courseRecord.gradeScale
                                                }
                                            }
                                            else{
                                                // We still need to print a status
                                                CourseAcademicGrade() {
                                                    mkp.yield courseRecord.gradeCode
                                                }
                                            }
                                            CourseSubjectAbbreviation() {
                                                mkp.yield courseRecord.subject
                                            }
                                            CourseNumber() {
                                                mkp.yield courseRecord.courseNumber
                                            }
                                            CourseTitle() {
                                                mkp.yield courseRecord.courseTitle.replaceAll("&", "&amp;").take(60)
                                            }
                                        }
                                    }

                                    AcademicSummary() {
                                        GPA() {
                                            CreditUnit() {
                                                mkp.yield 'Semester'
                                            }
                                            GradePointAverage() {
                                                mkp.yield(gpaFormat.format((totalCredits && totalPoints) ? (totalPoints / totalCredits) : 0))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            String tmpXML = generatedXml.toString()
            int declarationEnd = tmpXML.indexOf('?>')

            if (declarationEnd != -1)
                tmpXML = tmpXML.substring(declarationEnd + 2)

            rawXml = tmpXML
        }

        return rawXml
    }

    // Note that capitalization is inconsistent from the OCAS side
    @Override
    List<String> getExpectedResponseFields() { ["RequestId", "PESCXml"] }

    @Override
    Map<String, String> getPayload() { [PESCXML: xml] as Map<String, String> }

    @Override
    String getApiEndpoint() { OcasApi.ENDPOINT_POST_TRANSCRIPT }
}
