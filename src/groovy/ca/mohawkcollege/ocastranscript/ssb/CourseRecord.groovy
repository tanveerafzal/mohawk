package ca.mohawkcollege.ocastranscript.ssb

abstract class CourseRecord implements HasBannerTerm {
    String creditBasis = "Regular"
    BigDecimal numericGrade = null
    String gradeCode = null

    abstract BigDecimal getPidm()

    abstract Boolean getIncludeInGpa()

    abstract BigDecimal getCreditHours()

    abstract BigDecimal getCreditHoursCompleted()

    abstract String getGradeScale()

    abstract String getSubject()

    abstract String getCourseNumber()

    abstract String getCourseTitle()

    abstract String getProgramCode()

    abstract String getProgramName()

    BigDecimal getWeightedGrade() { creditHours * numericGrade }
}
