package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

import java.time.LocalDate

/**
 * Corresponds to PESC definition acRec:CourseType
 */
@InheritConstructors
class Course extends XmlFragment {

    static final String GRADE_NONE = "ZZZ"

    String getNumber() { getString("CourseNumber") }

    String getCourseAbbreviation() { getString("CourseSubjectAbbreviation")}

    String getTitle() { getString("CourseTitle") }

    Date getBeginDate() { getDate("CourseBeginDate") }

    Date getEndDate() { getDate("CourseEndDate") }

    CourseCreditLevel getCreditLevel() {
        getString("CourseCreditLevel").with { it ? CourseCreditLevel.valueOf(it) : null }
    }

    String getRepeatCode() { getString("CourseRepeatCode") }

    String getCreditLevelCode() { creditLevel?.code }

    String getGradeScale() { getString("CourseAcademicGradeScaleCode") }

    String getCreditUnits() { getString("CourseCreditUnits") }

    // aka AHRS
    Number getCreditValue() { getBigDecimal("CourseCreditValue") }

    // AKA EHRS
    Number getCreditEarned() { getBigDecimal("CourseCreditEarned") }

    // aka QPTS
    Number getQualityPointsEarned() { getBigDecimal("CourseQualityPointsEarned") }

    String getAcademicGrade() { getString("CourseAcademicGrade") }

    String getSupplementalAcademicGrade() {
        getString("CourseSupplementalAcademicGrade.CourseSupplementalGrade.CourseAcademicSupplementalGrade")
    }

    String getGrade() {
        academicGrade == GRADE_NONE ? supplementalAcademicGrade : academicGrade
    }
}
