package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.ocastranscript.type.GenderUserType
import ca.mohawkcollege.ocastranscript.type.RefusalReasonUserType
import ca.mohawkcollege.ocastranscript.type.RequestPurposeUserType
import ca.mohawkcollege.ocastranscript.type.ResponseTypeUserType
import ca.mohawkcollege.ocastranscript.type.HoldStatusUserType
import ca.mohawkcollege.ocastranscript.type.SendTriggerUserType
import ca.mohawkcollege.ocastranscript.type.UserMatchResultUserType
import ca.mohawkcollege.ocastranscript.type.VerificationStageUserType
import ca.mohawkcollege.ocastranscript.xml.SendTrigger

class Svrtreq {
    String svrtreqBgn02
    Date svrtreqTransDate
    Date svrtreqBirthDate
    Date svrtreqExitDate
    Gender gender
    String svrtreqOcasAppnum
    String svrtreqSin
    String svrtreqStudentNo_1
    String svrtreqStudentNo_2
    String svrtreqStudentNo_3
    String svrtreqDataOrigin = "OCAS"
    String svrtreqUserId = "OcasSync"
    Date svrtreqActivityDate
    String svrtreqSurname
    String svrtreqFirstmidname
    String svrtreqFirstname
    String svrtreqFormersurname
    String svrtreqSecondmidname
    String svrtreqId
    String svrtreqPrefix
    String svrtreqDateInd
    Date svrtreqSendDate
    //H = holds exist, $ = balance owing, N = no holds exist, O = hold override
    HoldStatus holdStatus
    // The reason why we sent back a refusal
    RefusalReason refusalReason
    // The action to be taken as a result of this request
    RequestPurpose requestPurpose = RequestPurpose.Create
    // What the final outcome of the request was
    ResponseType responseType
    // when the transcript should be sent
    SendTrigger sendTrigger
    // the result of a user matching process, if any
    UserMatchResult userMatchResult
    // What stage of verification we are in
    VerificationStage verificationStage

    static mapping = {
        table schema: "SATURN", name: "SVRTREQ"
        id column: "SVRTREQ_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "SVRTREQ_VERSION"
        gender type: GenderUserType, column: "SVRTREQ_GENDER"
        holdStatus type: HoldStatusUserType, column: "SVRTREQ_HOLD_IND"
        refusalReason type: RefusalReasonUserType, column: "SVRTREQ_REASON_CDE"
        requestPurpose type: RequestPurposeUserType, column: "SVRTREQ_PURPOSE_CDE"
        responseType type: ResponseTypeUserType, column: "SVRTREQ_COMPLETION_IND", defaultValue: ResponseType.Incomplete
        sendTrigger type: SendTriggerUserType, column: "SVRTREQ_ACTION_CDE"
        userMatchResult type: UserMatchResultUserType, column: "SVRTREQ_MATCH_IND"
        verificationStage type: VerificationStageUserType, column: "SVRTREQ_STATE_IND"
        svrtreqDateInd column: "SVRTREQ_DATE_IND"
    }

    static constraints = {
        svrtreqBgn02 unique: true
        svrtreqSendDate nullable: true
        svrtreqBirthDate nullable: true
        svrtreqExitDate nullable: true
        gender nullable: true
        svrtreqOcasAppnum nullable: true
        svrtreqSin nullable: true
        svrtreqStudentNo_1 nullable: true
        svrtreqStudentNo_2 nullable: true
        svrtreqStudentNo_3 nullable: true
        svrtreqSurname nullable: true
        svrtreqFirstmidname nullable: true
        svrtreqFirstname nullable: true
        svrtreqFormersurname nullable: true
        svrtreqSecondmidname nullable: true
        svrtreqId nullable: true
        svrtreqPrefix nullable: true
        svrtreqDateInd nullable: true
        holdStatus nullable: true
        userMatchResult nullable: true
        refusalReason nullable: true
    }

    String toString() {
        [
                this.class.simpleName,
                svrtreqBgn02,
                [
                        st: sendTrigger,
                        vs: verificationStage,
                        um: userMatchResult,
                        hs: holdStatus,
                        rt: responseType,
                        rr: refusalReason
                ].collect { [it.key, it.value].join(":") }.join("/")
        ].join(" ")
    }
}
