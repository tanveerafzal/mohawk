package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberRange

import java.time.YearMonth

/**
 * Corresponds to PESC definition acRec:AcademicSessionType
 */
@InheritConstructors
class AcademicSession extends XmlFragment {
    // required, YearMonth
    YearMonth getDesignator() { getYearMonth("AcademicSessionDetail.SessionDesignator") }

    String getName() { getString("AcademicSessionDetail.SessionName") }

    String getType() { getString("AcademicSessionDetail.SessionType") }

    Date getBegin() { getDate("AcademicSessionDetail.SessionBeginDate") }

    Date getEnd() { getDate("AcademicSessionDetail.SessionEndDate") }

    String getStudentLevel() { getString("StudentLevel.StudentLevelCode") }

    String getProgramName() { getString("AcademicProgram.AcademicProgramName") }

    CourseCreditLevel getCreditLevel() {
        academicSummary.getString("AcademicSummaryLevel").with { it ? CourseCreditLevel.valueOf(it) : null }
    }

    BigInteger getClassRank() { academicSummary.getBigInteger("ClassRank") }

    BigInteger getClassSize() { academicSummary.getBigInteger("ClassSize") }

    String getCreditUnit() { gpa.creditUnit }

    Number getCreditHoursForGPA() { gpa.creditHoursForGpa }

    Number getGradePointAverage() { gpa.gradePointAverage }

    Number getGpaRangeMinimum() { gpa.rangeMinimum }

    Number getGpaRangeMaximum() { gpa.rangeMaximum }

    NumberRange getGpaRange() { gpa.range }

    // Calculated properties
    // AHRS
    Number getCreditHoursAttempted() {
        gpa.getBigDecimal("CreditHoursAttempted") ?: courses*.creditValue.findAll().sum() as Number
    }
    // EHRS
    Number getCreditHoursEarned() {
        gpa.getBigDecimal("CreditHoursEarned") ?: courses*.creditEarned.findAll().sum() as Number
    }
    // QPTS
    Number getQualityPointsEarned() {
        gpa.getBigDecimal("TotalQualityPoints") ?: courses*.qualityPointsEarned.findAll().sum() as Number
    }

    // TODO what is qhrs? is it needed?
    // Number qhrs

    // Lazy-loaded properties

    @Lazy
    XmlFragment academicSummary = { this.AcademicSummary as XmlFragment }()

    @Lazy
    Gpa gpa = { new Gpa(academicSummary.getChild("GPA")) }()

    @Lazy
    List<Award> awards = {
        this.all_AcademicAward.collect { XmlFragment xmlFragment ->
            new Award(xmlFragment)
        }
    }()

    @Lazy
    List<Course> courses = {
        this.all_Course.collect { XmlFragment xmlFragment ->
            new Course(xmlFragment)
        }
    }()

    // Get messages from academic summary as well
    List<String> getNoteMessages() {
        super.noteMessages + academicSummary.noteMessages
    }

}
