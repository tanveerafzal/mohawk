package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.DateUtils

class StudentHold {

    BigDecimal pidm
    HoldCode holdCode
    String sprholdUser
    Date sprholdFromDate
    Date sprholdToDate
    String sprholdReleaseInd
    String sprholdReason
    BigDecimal sprholdAmountOwed
    String sprholdOrigCode
    Date sprholdActivityDate
    String sprholdDataOrigin
    String sprholdUserId
    String sprholdVpdiCode

    static mapping = {

        table schema: "SATURN", name: "SPRHOLD"
        id column: "SPRHOLD_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "SPRHOLD_VERSION"
        pidm column: "SPRHOLD_PIDM"
        holdCode column: "SPRHOLD_HLDD_CODE"
    }
    static constraints = {
        pidm unique: ['holdCode', 'sprholdUser', 'sprholdFromDate', 'sprholdToDate']
        sprholdDataOrigin nullable: true
        sprholdReason nullable: true
        sprholdAmountOwed nullable: true
        sprholdOrigCode nullable: true
        sprholdUserId nullable: true
        sprholdVpdiCode nullable: true
    }

    static StudentHold findActiveByPidm(BigDecimal pidm) {
        return createCriteria().get {
            eq("pidm", pidm)
            lt("sprholdFromDate", DateUtils.now)
            ge("sprholdToDate", DateUtils.now)
            holdCode {
                eq("stvhlddTransHoldInd", 'Y')
            }
        } as StudentHold
    }

    static List<StudentHold> findAllActiveByPidm(BigDecimal pidm) {
        return createCriteria().list {
            eq("pidm", pidm)
            lt("sprholdFromDate", DateUtils.now)
            ge("sprholdToDate", DateUtils.now)
            holdCode {
                eq("stvhlddTransHoldInd", 'Y')
            }
        } as List<StudentHold>
    }
}
