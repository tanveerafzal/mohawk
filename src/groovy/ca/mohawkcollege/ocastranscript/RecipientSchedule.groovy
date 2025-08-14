package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.baselib.util.MappedEnum

enum RecipientSchedule implements MappedEnum<String> {
    Now("Now"),
    AfterDegreeAwarded(["AfterDegreeAwarded", "AfterCertificateAwarded", "AfterGradesPosted", "AfterDegreeCompletionStatement", "AfterHonorsStatement"]),
    AfterSpecifiedDate("AfterSpecifiedDate"),
    AfterSpecifiedTerm(["AfterSpecifiedCourseGrade", "AfterGradesChanged", "AfterSpecifiedTerm", "AfterCorrespondenceCourseCompleted"]),
    Other(["AfterCurrentTermEnrollment", "Other"])

    // Constructors
    RecipientSchedule(String value) { setValue(value) }

    RecipientSchedule(List<String> values) { setValues(values) }
}