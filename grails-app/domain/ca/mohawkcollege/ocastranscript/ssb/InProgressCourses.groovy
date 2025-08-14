package ca.mohawkcollege.ocastranscript.ssb

class InProgressCourses extends CourseRecord implements Serializable {
    BigDecimal pidm
    String programCode
    String programName
    String termCode
    String gradeScale = '500'
    String subject
    String courseNumber
    String courseTitle
    Boolean includeInGpa = false
    BigDecimal creditHours
    BigDecimal creditHoursCompleted = 0
    String levelCode

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "V_COURSE_CURRENT"
        version false
        id composite: ["pidm", "termCode", "subject", "courseNumber"], generator: "assigned"
        pidm column: "PIDM"
        levelCode sqlType: 'char'
        creditHours column: "CREDITS"
        subject column: "SUBJ_CODE"
        courseNumber column: "CRSE_NUMB"
        programCode column: "MAJOR"
        programName column: "MAJOR_DESC"
    }

    static constraints = {
    }

    static transients = ['creditHoursCompleted', 'gradeCode', 'gradeScale', 'term', 'creditBasis', 'includeInGpa', 'numericGrade', 'programDescription']
}
