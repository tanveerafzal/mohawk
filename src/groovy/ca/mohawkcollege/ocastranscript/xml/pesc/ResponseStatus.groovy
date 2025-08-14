package ca.mohawkcollege.ocastranscript.xml.pesc

/**
 * Per AcademicRecord_v1.6.0.xsd -- AcRec:ResponseStatusType
 */
enum ResponseStatus {
    TranscriptSent,
    TranscriptRequestReceived,
    Hold,
    NoRecord,
    MultipleMatch,
    Canceled,
    OfflineRecordSearch,
    OfflineRecordSent,
    Deceased
}
