package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum VerificationStage implements MappedEnum<String> {
    NotStarted("P"),
    UserMatchCheck("M"),
    HoldCheck("H"),
    DateCheck("D"),
    Complete("C"),
    Cancelled("X")
    //One of: M = pending match check, H = pending hold check, D = pending date check, P = ???, C = verification process completed, X = cancelled

    VerificationStage(String value) { setValue(value) }
}