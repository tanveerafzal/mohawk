package ca.mohawkcollege.ocastranscript.baseline

class HoldCode {

    String id
    String stvhlddCode
    String stvhlddRegHoldInd
    String stvhlddTransHoldInd
    String stvhlddGradHoldInd
    String stvhlddGradeHoldInd
    String stvhlddDesc
    Date stvhlddActivityDate
    String stvhlddArHoldInd
    String stvhlddEnvHoldInd
    BigDecimal stvhlddVrMsgNo
    String stvhlddDispWebInd
    String stvhlddApplicationHoldInd
    String stvhlddComplianceHoldInd
    String stvhlddUserId
    String stvhlddDataOrigin
    String stvhlddVpdiCode

    static mapping = {

        table schema: "SATURN", name: "STVHLDD"
        id name: "stvhlddCode", generator: 'assigned'
        version column: "STVHLDD_VERSION"
        stvhlddCode column: "STVHLDD_CODE"
    }
    static constraints = {
        stvhlddRegHoldInd nullable: true
        stvhlddTransHoldInd nullable: true
        stvhlddGradHoldInd nullable: true
        stvhlddGradeHoldInd nullable: true
        stvhlddDesc nullable: true
        stvhlddArHoldInd nullable: true
        stvhlddEnvHoldInd nullable: true
        stvhlddVrMsgNo nullable: true
        stvhlddUserId nullable: true
        stvhlddDataOrigin nullable: true
        stvhlddVpdiCode nullable: true
    }
}
