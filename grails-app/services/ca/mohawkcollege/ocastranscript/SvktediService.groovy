package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.ocastranscript.baseline.Svrtreq
import ca.mohawkcollege.ocastranscript.ssb.TranscriptRequest
import grails.transaction.Transactional
import groovy.sql.Sql

@Transactional
class SvktediService {

    def dataSource

    def serviceMethod() {

    }

    def processSvrtreq(TranscriptRequest request) {
        Sql sql = new Sql(dataSource)
        sql.call("{call svktedi.p_process_svrtreq(?,?)}", [request.requestTrackingId, Sql.VARCHAR]) { errMessage ->
            if (errMessage == "~") {
                // This is the "success" return string
                log.debug("procedure call OK")
            } else {
                log.error("procedure call returned an error message: $errMessage")
            }
        }
    }

    BigDecimal getSvrtreqPidm(Svrtreq svrtreq) {
        BigDecimal pidm = null
        Sql sql = new Sql(dataSource)
        sql.call("{call svktedi.p_get_spriden_pidm(?,?)}", [svrtreq.svrtreqId, Sql.VARCHAR]) { String spriden_pidm ->
            log.debug("spriden_pidm : found $spriden_pidm for svrtreqId ${svrtreq.svrtreqId}")
            pidm = new BigDecimal(spriden_pidm)
        }
        return pidm
    }
}
