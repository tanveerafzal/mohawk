package ca.mohawkcollege.ocastranscript.ssb

import grails.test.mixin.support.SkipMethod
import grails.test.spock.IntegrationSpec
import spock.lang.Shared


class TranscriptRequestIntegrationSpec extends IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = TranscriptRequest

    @SkipMethod
    void "load and validate TranscriptRequest records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by $sqlRand", [max: sampleLimit] as Map)
        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }

}
