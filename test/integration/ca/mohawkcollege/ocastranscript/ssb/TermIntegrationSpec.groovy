package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.baseline.Svrtnte
import ca.mohawkcollege.ocastranscript.baseline.Term
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class TermIntegrationSpec extends  IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = Term



    void "load and validate Term records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by $sqlRand", [max: sampleLimit] as Map)
       // print(aList.size())
        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }


}
