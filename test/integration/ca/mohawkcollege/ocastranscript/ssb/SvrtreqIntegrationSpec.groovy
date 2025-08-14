package ca.mohawkcollege.ocastranscript.ssb


import ca.mohawkcollege.ocastranscript.baseline.Svrtreq
import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class SvrtreqIntegrationSpec extends IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 100

    void "load and validate Svrtreq records"() {
        given:
        def aList = Svrtreq.findAll("from ${Svrtreq.simpleName} order by $sqlRand", [max: sampleLimit] as Map)

        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }
}
