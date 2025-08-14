package ca.mohawkcollege.ocastranscript.ssb

import grails.test.mixin.support.SkipMethod
import grails.test.spock.IntegrationSpec
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Timeout

class AcademicHistoryIntegrationSpec extends  IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    Class testedClass = AcademicHistory


    void "load and validate AcademicHistory records"() {
        given:
        def aList = testedClass.findAll("from ${testedClass.simpleName} order by pidm desc", [max: sampleLimit] as Map)
        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }


}
