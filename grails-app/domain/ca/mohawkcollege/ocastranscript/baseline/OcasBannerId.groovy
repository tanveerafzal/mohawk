package ca.mohawkcollege.ocastranscript.baseline

class OcasBannerId implements Serializable {

	String      svrobidOcasApplNum
	String      svrobidYear
	BigDecimal  svrobidPidm
	String      svrobidDuplInd
	String      svrobidUser
	Date        svrobidActivityDate
	BigDecimal  svrobidSurrogateId
	BigDecimal  svrobidVersion
	String      svrobidUserId
	String      svrobidDataOrigin
	String      svrobidVpdiCode
	
	static mapping={
		table	schema: "SATURN", name: "SVROBID"
		version	false
		id 		composite:["svrobidOcasApplNum", "svrobidYear"], generator:"assigned"
		
	}
    static constraints = {
		svrobidOcasApplNum nullable:false
		svrobidYear nullable:false
		svrobidUser nullable:false
		svrobidActivityDate nullable:false

		svrobidPidm nullable:true
		svrobidDuplInd nullable:true
		svrobidSurrogateId nullable:true
		svrobidVersion nullable:true
		svrobidUserId nullable:true
		svrobidDataOrigin nullable:true
		svrobidVpdiCode nullable:true
    }
}
