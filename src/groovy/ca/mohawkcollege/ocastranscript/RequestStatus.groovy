package ca.mohawkcollege.ocastranscript

enum RequestStatus {
    // Freshly received from OCAS
    New,
    // We've triggered database-side validation
    Validating,
    // We've determined we're not going to send a transcript
    Deferred,
    OnHold,
    Refusal,
    // We've determined we will send some kind of response
    ReadyToSendResponse,
    // We've determined we will send a deferred response (for AfterDegreeAwarded or AfterSpecifiedDate situations)
    ReadyToSendDeferredResponse,
    // We've determined we will send a transcript
    ReadyToSendTranscript,
    // We've attempted to send a response
    ResponseSendFailed,
    ResponseSent,
    // We've attempted to send a transcript
    TranscriptSendFailed,
    TranscriptSent,
    // Sent back for manual handling
    ManualVerify,
    // Something is broken
    Error
}
