package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

/**
 * Our best guess at the meaning of the SVRTREQ.SVRTREQ_PURPOSE_CDE column comes from the source
 * code of the C program student/c/svptreq.c
 */
enum RequestPurpose implements MappedEnum<String> {
    Cancel("1"),
    Create("13"),
    Resubmit("15")

    // Constructor
    RequestPurpose(String value) { setValue(value) }
}