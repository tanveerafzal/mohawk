package ca.mohawkcollege.ocastranscript.baseline

class Svrtnte {

    String svrtnteBgn02
    String svrtnteNote
    String svrtnteDataOrigin = "OCAS"
    String svrtnteUserId = "OcasSync"
    Date svrtnteActivityDate
    String svrtnteVpdiCode

    static mapping = {
        table schema: "SATURN"
        id column: "SVRTNTE_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "SVRTNTE_VERSION"
    }

    static constraints = {
        svrtnteVpdiCode nullable: true
    }

}
