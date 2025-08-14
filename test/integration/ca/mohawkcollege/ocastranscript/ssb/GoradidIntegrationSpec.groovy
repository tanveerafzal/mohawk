package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.baseline.DegreeInfo
import ca.mohawkcollege.ocastranscript.baseline.Goradid
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class GoradidIntegrationSpec extends  IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = Goradid



    void "load and validate Goradid records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by $sqlRand", [max: sampleLimit] as Map)
       // print(aList.size())
        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }


}
