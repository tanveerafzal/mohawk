package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.type.BooleanYNType

class AcademicHistory extends CourseRecord implements Serializable {

    BigDecimal pidm
    String programCode
    String programName
    String termCode
    String subject
    String courseNumber
    String courseTitle
    String gradeCode
    String gradeScale = null
    BigDecimal creditHours
    BigDecimal creditHoursCompleted
    Boolean includeInGpa

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "V_COURSE_HISTORY"
        version false
        id composite: ["pidm", "termCode", "subject", "courseNumber"], generator: "assigned"
        pidm column: "PIDM"
        programCode column: "MAJOR"
        programName column: "MAJOR_DESC"
        termCode column: "TERM_CODE"
        subject column: "SUBJ_CODE"
        courseNumber column: "CRSE_NUMB"
        courseTitle column: "COURSE_TITLE"
        gradeCode column: "GRADE"
        numericGrade column: "NUMERIC_VALUE"
        creditHours column: "CREDIT_HOURS"
        creditHoursCompleted column: "CREDIT_HOURS_EARN"
        includeInGpa type: BooleanYNType
    }

    static constraints = {
        numericGrade nullable: true
        courseTitle nullable: true
    }

    static transients = ['gradeScale', 'term', 'creditBasis', 'programDescription']
}
