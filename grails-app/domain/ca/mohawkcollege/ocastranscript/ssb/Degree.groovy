package ca.mohawkcollege.ocastranscript.ssb

class Degree extends AcademicAward implements Serializable {
    BigDecimal pidm
    BigDecimal sequenceNumber
    String termCode
    String programCode
    String programName
    String description
    Date gradDate

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "V_DEGREE"
        id column: "surrogate_id" //, "generator": "assigned"
    }

    static constraints = {
        pidm unique: "sequenceNumber"
        termCode nullable: true
        gradDate nullable: true
    }

    static transients = ['level', 'date', 'title', 'programDescription', 'term']

    @Override
    String getLevel() { '2.2' }

    @Override
    Date getDate() { gradDate }

    @Override
    String getTitle() { description }
}
