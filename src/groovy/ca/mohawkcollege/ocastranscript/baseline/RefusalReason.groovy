package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum RefusalReason implements MappedEnum<String> {

    Cancellation("01"), // Not used
    NotFound("10"), // Commonly used
    Deceased("12"), // Rarely used
    ReIssue("18"), // Not used
    // "on hold"
    Hold("21"), // Financial Hold specifically
    //22 - Information copy   (hard copy to be sent)*************** need to confirm
    InformationCopy("22"), // Rarely used
    NoAcademicData("27"), // Commonly used
    Rejection("44"), // Not used
    Suspended("48") // Not used

    static final RefusalReason[] holdReasons = [ReIssue, Hold, InformationCopy, NoAcademicData, Rejection, Suspended]

    // Constructor
    RefusalReason(String value) { setValue(value) }

    boolean isHold() { return holdReasons.contains(this) }
}