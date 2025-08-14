package ca.mohawkcollege.ocastranscript.ssb

class InstitutionalHonor extends AcademicAward implements Serializable {

    BigDecimal pidm
    BigDecimal sequenceNumber
    String termCode
    String description
    Date effectiveDate

    static mapping = {
        table schema: "GROOVY_GRAILS", name: "V_AWARD"
//        version false
//        id composite: ["pidm", "sequenceNumber"], generator: "assigned"
        id column: "surrogate_id" //, "generator": "assigned"
    }

    static constraints = {
        pidm unique: "sequenceNumber"
    }

    static transients = ['level', 'date', 'title', 'programCode', 'programName', 'programDescription', 'term']

    @Override
    String getLevel() { '0.0' }

    @Override
    Date getDate() { effectiveDate }

    @Override
    String getTitle() { description }
}
