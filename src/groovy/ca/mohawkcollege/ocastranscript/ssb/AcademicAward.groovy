package ca.mohawkcollege.ocastranscript.ssb

abstract class AcademicAward implements HasBannerTerm, HasBannerProgram {
    abstract String getLevel()

    abstract Date getDate()

    abstract String getTitle()
}