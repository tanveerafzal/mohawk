package ca.mohawkcollege.ocastranscript.ssb


import ca.mohawkcollege.ocastranscript.baseline.HoldCode
import ca.mohawkcollege.ocastranscript.baseline.OcasBannerId
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class OcasBannerIdIntegrationSpec extends  IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = OcasBannerId



    void "load and validate OcasBannerId records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by $sqlRand", [max: sampleLimit] as Map)
       // print(aList.size())
        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }


}
