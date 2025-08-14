package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.baselib.util.MappedEnum

enum CourseCreditLevel implements MappedEnum<String> {
    SeventhGrade("Z"),
    EighthGrade("Z"),
    NinthGrade("Z"),
    TenthGrade("Z"),
    EleventhGrade("Z"),
    TwelfthGrade("Z"),
    Dual("Z"),
    DualGraduateUndergraduate("Z"),
    DualHighSchoolCollege("Z"),
    Graduate("G"),
    GraduateProfessional("Z"),
    LowerDivision("Z"),
    Professional("P"),
    Secondary("Z"),
    TechnicalPreparatory("Z"),
    Undergraduate("U"),
    UpperDivision("Z"),
    Vocational("Z")

    CourseCreditLevel(String value) { setValue(value) }

    String getCode() { value }
}