package ca.mohawkcollege.ocastranscript.ssb

import grails.test.spock.IntegrationSpec
import spock.lang.Shared

class DegreeIntegrationSpec extends IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 5

    Class testedClass = Degree

    void "load and validate GradAwards records"() {
        given:
        def aList = Degree.findAll("from ${testedClass.simpleName} where termCode is not null  order by $sqlRand desc ", [max: sampleLimit] as Map)

        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it.errors) }) })
    }
}
