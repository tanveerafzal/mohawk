package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.baselib.domain.annotation.OneToOne
import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.RecipientSchedule
import ca.mohawkcollege.ocastranscript.baseline.Svrtnte
import ca.mohawkcollege.ocastranscript.baseline.Term
import ca.mohawkcollege.ocastranscript.xml.pesc.Format
import groovy.transform.EqualsAndHashCode
import groovy.util.slurpersupport.GPathResult
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import groovy.util.logging.Slf4j

import javax.persistence.EnumType
import javax.persistence.Enumerated
import java.text.ParseException

/**
 * A TranscriptSchedule represents a time at which a given TranscriptRequest wants a transcript to be sent.
 *
 * There are four primary ways in which a transcript may be scheduled to send:
 *  1. Immediately
 *  2. After a specific date
 *  3. After a specific term
 *  4. After a credential has been awarded
 *
 * Two TranscriptSchedule objects are considered equal if they refer to the same transcript request, and
 * also specify the same schedule for sending.
 */
@Slf4j
@EqualsAndHashCode(includes = ["transcriptRequest", "sendTrigger", "sendDate", "sendTermCode"])
class TranscriptSchedule {

    static final String RE_OCAS_SESSION = /^([FWSA])\s(\d{4})$/
    static final String RE_MOHAWK_TERM = /^(\d{4})([123])([035])$/

    @Cascade(CascadeType.ALL)
    TranscriptRequest transcriptRequest
    Boolean sent = false
    @Enumerated(EnumType.STRING)
    RecipientSchedule sendTrigger
    Date sendDate
    String sendTermCode
    // Grails autofilled timestamps
    Date dateCreated
    Date lastUpdated

    // lazy property
    private Term privateSendTerm

    static transients = ['privateSendTerm', 'sendTerm', 'triggerParameter', 'xml', 'readyToSend']

    static belongsTo = [TranscriptRequest]

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "OCAS_TRANSCRIPT_SCHEDULE"
        sendTermCode column: "SEND_TERM"
    }

    static constraints = {
        sendDate nullable: true
        sendTermCode nullable: true
    }

    Term getSendTerm() {
        if (!privateSendTerm && sendTermCode) {
            privateSendTerm = Term.findByStvtermCode(sendTermCode)
        }
        privateSendTerm
    }

    void setSendTerm(Term term) {
        privateSendTerm = term
        sendTermCode = term?.stvtermCode
    }

    /**
     * Return one of the parameter fields of this schedule (sessionName or sendDate) or null, depending
     * on the schedule trigger set
     *
     * @return
     */
    String getTriggerParameter() {
        switch (sendTrigger) {
            case RecipientSchedule.AfterSpecifiedTerm:
                return sendTermCode
            case RecipientSchedule.AfterSpecifiedDate:
                return sendDate ? Format.DATE.simpleDateFormat.format(sendDate) : null
            default:
                return null
        }
    }

    Svrtnte generateSvrtnte() {
        // If this has already been saved, and we haven't changed the send parameters
        if (id && ["sendTrigger", "sendTermCode"].every { !isDirty(it) }) {
            return null
        }

        log.trace("Creating SVRTNTE entry for request: ${transcriptRequest.requestTrackingId}")
        if (sendTrigger == RecipientSchedule.AfterSpecifiedTerm) {
            def sessionSymbol = "X"
            switch (triggerParameter?.substring(4, 5)) {
                case '1':
                    // Winter
                    sessionSymbol = "W"
                    break
                case '2':
                    // Spring
                    sessionSymbol = "A"
                    break
                case '3':
                    // Fall
                    sessionSymbol = "F"
                    break
                default:
                    log.error("Missing Term letter")
                    break
            }
            def modifiedTriggerParameter = sessionSymbol + triggerParameter?.substring(0, 4)
            new Svrtnte(
                    svrtnteBgn02: transcriptRequest.requestTrackingId,
                    svrtnteActivityDate: DateUtils.now,
                    svrtnteNote: ["TERM", modifiedTriggerParameter].findAll()*.toString().join("=")
            )
        } else {
            new Svrtnte(
                    svrtnteBgn02: transcriptRequest.requestTrackingId,
                    svrtnteActivityDate: DateUtils.now,
                    svrtnteNote: [sendTrigger, triggerParameter].findAll()*.toString().join("=")
            )
        }

    }

    /**
     * Populate the object's properties using a GPathResult. Note that {@code xml} is a transient property,
     * and the original XML cannot necessarily be recreated from this TranscriptSchedule object.
     *
     * TranscriptSchedule properties obtained from XML are:
     *  - sendTrigger
     *  - sendTerm
     *  - sendDate
     *
     * @param recipient
     */
    void setXml(GPathResult recipient) {
        sendTrigger = RecipientSchedule.fromValue(recipient.TranscriptHold.HoldType[0].toString()) as RecipientSchedule
        sendDate = null
        privateSendTerm = null
        try {
            switch (sendTrigger) {
                case RecipientSchedule.AfterSpecifiedDate:
                    sendDate = parseSendDate(recipient.TranscriptHold.ReleaseDate[0].toString())
                    break
                case RecipientSchedule.AfterSpecifiedTerm:
                    sendTermCode = parseSendTerm(recipient.TranscriptHold.SessionName[0].toString())
                    break
                default:
                    break
            }
        } catch (ParseException ignored) {
            log.error("Send date parsing error for: " + transcriptRequest?.requestTrackingId)
            sendDate = null
        }
    }

    static Date parseSendDate(String dateString) {
        try {
            dateString ? Format.DATE.simpleDateFormat.parse(dateString) : null
        } catch (ParseException ignored) {
            null
        }
    }

    static String parseSendTerm(String termCode) {
        termCode ? ocasSessionToBannerTerm(termCode) : null
    }

    /**
     * According to the requested schedule, is the transcript ready to send?
     */
    boolean isReadyToSend(TranscriptRequest request) {
        // If we've already sent it, don't resend
        if (sent) {
            return false
        }

        switch (sendTrigger) {
            case RecipientSchedule.Now:
                // Now means send now
                return true
                // Using sendDate as the sendTerm gets converted to a date in svrtreq anyway (hardcoded per request)
            case RecipientSchedule.AfterDegreeAwarded:
            case RecipientSchedule.AfterSpecifiedTerm:
            case RecipientSchedule.AfterSpecifiedDate:
                // See if the date has passed to now send a deferred transcript
                if (sendDate != null) {
                    return DateUtils.previousMidnight >= sendDate
                    // See if the sendDate should be there
                } else {
                    if (request?.svrtreq?.svrtreqSendDate != null) {
                        sendDate = request?.svrtreq?.svrtreqSendDate
                        save()
                        return DateUtils.previousMidnight >= sendDate
                    }
                }
                return false
            default:
                return false
        }
    }

    /**
     * Given an OCAS session in the form "W 2014", convert to a Mohawk term code such as "201510". For these purposes
     * we use the CE channel indicator "0" so that sorting and comparisons by term code work out.
     *
     * If the passed string is already in the form of a Mohawk term code, return it unchanged except for the channel
     * indicator. Otherwise, if the expected format is not found, return null.
     *
     * @param session
     * @return a string in the form of a Mohawk term code
     */
    private static String ocasSessionToBannerTerm(String session) {
        if (!session) return null

        // If it's already in Mohawk format, make sure it has the CE channel and return it
        session?.replaceFirst(/\d$/, "0")

        def match = (session =~ RE_OCAS_SESSION)
        if (!match) {
            log.error("Unrecognized session string: $session")
            throw new IllegalArgumentException("Unrecognized session string: $session")
        }

        def season = [F: 3, W: 1, S: 2, A: 2][match.group(1)]
        def year = match.group(2).toInteger()
        if (season != 3) {
            year++
        }

        // Use CE channel here because of how we do comparisons
        def channel = 0

        return [year, season, channel]*.toString().join("")
    }

    /**
     * After saving, save our Svrtnte object as well
     */
    def beforeInsert() { generateSvrtnte()?.save(flush: false) }

    /**
     * After saving, save our Svrtnte object as well
     */
    def beforeUpdate() { generateSvrtnte()?.save(flush: false) }
}