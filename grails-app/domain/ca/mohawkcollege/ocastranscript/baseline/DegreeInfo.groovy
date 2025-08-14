package ca.mohawkcollege.ocastranscript.baseline

class DegreeInfo implements Serializable {

    BigDecimal shrdgmrPidm
    BigDecimal shrdgmrSeqNo
    String shrdgmrDegcCode
    String shrdgmrDegsCode
    String shrdgmrLevlCode
    String shrdgmrCollCode_1
    String shrdgmrMajrCode_1
    String shrdgmrMajrCodeMinr_1
    String shrdgmrMajrCodeConc_1
    String shrdgmrCollCode_2
    String shrdgmrMajrCode_2
    String shrdgmrMajrCodeMinr_2
    String shrdgmrMajrCodeConc_2
    Date shrdgmrApplDate
    Date shrdgmrGradDate
    String shrdgmrAcyrCodeBulletin
    Date shrdgmrActivityDate
    String shrdgmrMajrCodeMinr_1_2
    String shrdgmrMajrCodeConc_1_2
    String shrdgmrMajrCodeConc_1_3
    String shrdgmrMajrCodeMinr_2_2
    String shrdgmrMajrCodeConc_2_2
    String shrdgmrMajrCodeConc_2_3
    String shrdgmrTermCodeSturec
    String shrdgmrMajrCode_1_2
    String shrdgmrMajrCode_2_2
    String shrdgmrCampCode
    String shrdgmrTermCodeGrad
    String shrdgmrAcyrCode
    String shrdgmrGrstCode
    String shrdgmrFeeInd
    Date shrdgmrFeeDate
    String shrdgmrAuthorized
    String shrdgmrTermCodeCompleted
    String shrdgmrDegcCodeDual
    String shrdgmrLevlCodeDual
    String shrdgmrDeptCodeDual
    String shrdgmrCollCodeDual
    String shrdgmrMajrCodeDual
    String shrdgmrDeptCode
    String shrdgmrDeptCode_2
    String shrdgmrProgram
    String shrdgmrTermCodeCtlg_1
    String shrdgmrDeptCode_1_2
    String shrdgmrDeptCode_2_2
    String shrdgmrMajrCodeConc_121
    String shrdgmrMajrCodeConc_122
    String shrdgmrMajrCodeConc_123
    String shrdgmrTermCodeCtlg_2
    String shrdgmrCampCode_2
    String shrdgmrMajrCodeConc_221
    String shrdgmrMajrCodeConc_222
    String shrdgmrMajrCodeConc_223
    BigDecimal shrdgmrCurrRule_1
    BigDecimal shrdgmrCmjrRule_1_1
    BigDecimal shrdgmrCconRule_11_1
    BigDecimal shrdgmrCconRule_11_2
    BigDecimal shrdgmrCconRule_11_3
    BigDecimal shrdgmrCmjrRule_1_2
    BigDecimal shrdgmrCconRule_12_1
    BigDecimal shrdgmrCconRule_12_2
    BigDecimal shrdgmrCconRule_12_3
    BigDecimal shrdgmrCmnrRule_1_1
    BigDecimal shrdgmrCmnrRule_1_2
    BigDecimal shrdgmrCurrRule_2
    BigDecimal shrdgmrCmjrRule_2_1
    BigDecimal shrdgmrCconRule_21_1
    BigDecimal shrdgmrCconRule_21_2
    BigDecimal shrdgmrCconRule_21_3
    BigDecimal shrdgmrCmjrRule_2_2
    BigDecimal shrdgmrCconRule_22_1
    BigDecimal shrdgmrCconRule_22_2
    BigDecimal shrdgmrCconRule_22_3
    BigDecimal shrdgmrCmnrRule_2_1
    BigDecimal shrdgmrCmnrRule_2_2
    String shrdgmrDataOrigin
    String shrdgmrUserId
    BigDecimal shrdgmrStspKeySequence
    String shrdgmrVpdiCode

    static mapping = {
        table schema: "SATURN", name: "SHRDGMR"
        id column: "SHRDGMR_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "SHRDGMR_VERSION"
    }
    static constraints = {
        shrdgmrPidm unique: ['shrdgmrSeqNo']
        shrdgmrAcyrCode nullable: true
        shrdgmrAuthorized nullable: true
        shrdgmrCampCode nullable: true
        shrdgmrCampCode_2 nullable: true
        shrdgmrCconRule_11_1 nullable: true
        shrdgmrCconRule_11_2 nullable: true
        shrdgmrCconRule_11_3 nullable: true
        shrdgmrCconRule_12_1 nullable: true
        shrdgmrCconRule_12_2 nullable: true
        shrdgmrCconRule_12_3 nullable: true
        shrdgmrCconRule_21_1 nullable: true
        shrdgmrCconRule_21_2 nullable: true
        shrdgmrCconRule_21_3 nullable: true
        shrdgmrCconRule_22_1 nullable: true
        shrdgmrCconRule_22_2 nullable: true
        shrdgmrCconRule_22_3 nullable: true
        shrdgmrCmjrRule_1_1 nullable: true
        shrdgmrCmjrRule_1_2 nullable: true
        shrdgmrCmjrRule_2_1 nullable: true
        shrdgmrCmjrRule_2_2 nullable: true
        shrdgmrCmnrRule_1_1 nullable: true
        shrdgmrCmnrRule_1_2 nullable: true
        shrdgmrCmnrRule_2_1 nullable: true
        shrdgmrCmnrRule_2_2 nullable: true
        shrdgmrCollCodeDual nullable: true
        shrdgmrCollCode_2 nullable: true
        shrdgmrCurrRule_1 nullable: true
        shrdgmrCurrRule_2 nullable: true
        shrdgmrDataOrigin nullable: true
        shrdgmrDegcCodeDual nullable: true
        shrdgmrDeptCode nullable: true
        shrdgmrDeptCodeDual nullable: true
        shrdgmrDeptCode_1_2 nullable: true
        shrdgmrDeptCode_2 nullable: true
        shrdgmrDeptCode_2_2 nullable: true
        shrdgmrFeeDate nullable: true
        shrdgmrFeeInd nullable: true
        shrdgmrGrstCode nullable: true
        shrdgmrLevlCodeDual nullable: true
        shrdgmrMajrCodeConc_1 nullable: true
        shrdgmrMajrCodeConc_121 nullable: true
        shrdgmrMajrCodeConc_122 nullable: true
        shrdgmrMajrCodeConc_123 nullable: true
        shrdgmrMajrCodeConc_1_2 nullable: true
        shrdgmrMajrCodeConc_1_3 nullable: true
        shrdgmrMajrCodeConc_2 nullable: true
        shrdgmrMajrCodeConc_221 nullable: true
        shrdgmrMajrCodeConc_222 nullable: true
        shrdgmrMajrCodeConc_223 nullable: true
        shrdgmrMajrCodeConc_2_2 nullable: true
        shrdgmrMajrCodeConc_2_3 nullable: true
        shrdgmrMajrCodeDual nullable: true
        shrdgmrMajrCodeMinr_1 nullable: true
        shrdgmrMajrCodeMinr_1_2 nullable: true
        shrdgmrMajrCodeMinr_2 nullable: true
        shrdgmrMajrCodeMinr_2_2 nullable: true
        shrdgmrMajrCode_1_2 nullable: true
        shrdgmrMajrCode_2 nullable: true
        shrdgmrMajrCode_2_2 nullable: true
        shrdgmrStspKeySequence nullable: true
        shrdgmrTermCodeCompleted nullable: true
        shrdgmrTermCodeCtlg_2 nullable: true
        shrdgmrTermCodeGrad nullable: true
        shrdgmrVpdiCode nullable: true
        shrdgmrGradDate nullable: true
        shrdgmrUserId nullable: true
        shrdgmrProgram nullable: true
        shrdgmrAcyrCodeBulletin nullable: true
        shrdgmrApplDate nullable: true
        shrdgmrCollCode_1 nullable: true
        shrdgmrMajrCode_1 nullable: true
        shrdgmrTermCodeCtlg_1 nullable: true
    }
}
