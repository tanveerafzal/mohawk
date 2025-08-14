package ca.mohawkcollege.ocastranscript.baseline

class Spriden {

    BigDecimal spridenPidm
    String spridenId
    String spridenLastName
    String spridenFirstName
    String spridenMi
    String spridenChangeInd
    String spridenEntityInd
    Date spridenActivityDate
    String spridenUser
    String spridenOrigin
    String spridenSearchLastName
    String spridenSearchFirstName
    String spridenSearchMi
    String spridenSoundexLastName
    String spridenSoundexFirstName
    String spridenNtypCode
    String spridenCreateUser
    Date spridenCreateDate
    String spridenDataOrigin
    String spridenCreateFdmnCode
    String spridenSurnamePrefix
    String spridenUserId
    String spridenVpdiCode

    static mapping = {
        table schema: "SATURN"
        id column: "SPRIDEN_SURROGATE_ID", generator: 'jpl.hibernate.util.TriggerAssignedIdentityGenerator'
        version column: "SPRIDEN_VERSION"
        spridenSoundexLastName sqlType: "char", length: 4
        spridenSoundexFirstName sqlType: "char", length: 4
        //cache usage: 'read-only'
    }
    static constraints = {

        spridenFirstName nullable: true
        spridenMi nullable: true
        spridenChangeInd nullable: true
        spridenEntityInd nullable: true
        spridenUser nullable: true
        spridenOrigin nullable: true
        spridenSearchLastName nullable: true
        spridenSearchFirstName nullable: true
        spridenSearchMi nullable: true
        spridenSoundexLastName nullable: true
        spridenSoundexFirstName nullable: true
        spridenNtypCode nullable: true
        spridenCreateUser nullable: true
        spridenCreateDate nullable: true
        spridenDataOrigin nullable: true
        spridenCreateFdmnCode nullable: true
        spridenSurnamePrefix nullable: true
        spridenUserId nullable: true
        spridenVpdiCode nullable: true

    }
}
