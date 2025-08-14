package ca.mohawkcollege.ocastranscript.baseline

class Term {

    String stvtermCode
    String stvtermDesc
    Date stvtermStartDate
    Date stvtermEndDate
    String stvtermFaProcYr
    Date stvtermActivityDate
    String stvtermFaTerm
    BigDecimal stvtermFaPeriod
    BigDecimal stvtermFaEndPeriod
    String stvtermAcyrCode
    Date stvtermHousingStartDate
    Date stvtermHousingEndDate
    String stvtermSystemReqInd
    String stvtermTrmtCode
    String stvtermFaSummerInd
    String stvtermUserId
    String stvtermDataOrigin
    String stvtermVpdiCode

    static mapping = {
        table schema: "SATURN", name: "STVTERM"
        id column: "STVTERM_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "STVTERM_VERSION"
    }
    static constraints = {
        stvtermCode unique: true
        stvtermFaProcYr nullable: true
        stvtermFaTerm nullable: true
        stvtermFaPeriod nullable: true
        stvtermFaEndPeriod nullable: true
        stvtermSystemReqInd nullable: true
        stvtermTrmtCode nullable: true
        stvtermFaSummerInd nullable: true
        stvtermUserId nullable: true
        stvtermDataOrigin nullable: true
        stvtermVpdiCode nullable: true
    }
}
