package ca.mohawkcollege.ocastranscript.baseline

class Goradid implements Serializable {

    BigDecimal goradidPidm
    String goradidAdditionalId
    String goradidAdidCode
    String goradidUserId
    Date goradidActivityDate
    String goradidDataOrigin
    String goradidVpdiCode

    static mapping = {
        table schema: "GENERAL", name: 'GORADID'
        id column: 'GORADID_SURROGATE_ID', generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: 'GORADID_VERSION'
    }
    static constraints = {

        goradidPidm unique: ['goradidAdditionalId', 'goradidAdidCode']
        goradidVpdiCode nullable: true
    }
}
