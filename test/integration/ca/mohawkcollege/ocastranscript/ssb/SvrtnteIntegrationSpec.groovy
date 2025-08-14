package ca.mohawkcollege.ocastranscript.ssb

import ca.mohawkcollege.ocastranscript.baseline.Svrtnte
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class SvrtnteIntegrationSpec extends  IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = Svrtnte



    void "load and validate Svrtnte records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by svrtnteBgn02 desc", [max: sampleLimit] as Map)

        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }


}
