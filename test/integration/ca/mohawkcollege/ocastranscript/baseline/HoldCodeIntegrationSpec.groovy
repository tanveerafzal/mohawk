package ca.mohawkcollege.ocastranscript.baseline


import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class HoldCodeIntegrationSpec extends IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    void "load and validate HoldCode records"() {
        given:
        def aList = HoldCode.findAll("from HoldCode order by $sqlRand", [max: sampleLimit] as Map)

        expect: "random list of objects all validate"
        aList
        aList.every({ it?.validate() || (it?.errors?.allErrors?.each { println(it) } && false) })
    }
}
