package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.baselib.util.MappedEnum

/**
 * Per AcademicRecord_v1.6.0.xsd -- AcRec:HoldTypeType
 *
 * These are the local codes we save in SVRTREQ.
 *
 * Note that our database procedure mentions some other codes, but these are never used:
 * AfterSpecifiedDate("R5")
 * Other("OT")
 */
enum SendTrigger implements MappedEnum<String> {
    Now("R2"),
    AfterDegreeAwarded("R4"),
    AfterSpecifiedTerm("R3")

    // Constructors
    SendTrigger(String value) { setValue(value) }
}