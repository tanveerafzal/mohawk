package ca.mohawkcollege.ocastranscript.baseline

import grails.test.spock.IntegrationSpec
import spock.lang.Shared
import spock.lang.Unroll

class StudentHoldIntegrationSpec extends IntegrationSpec {

    @Shared
    def sqlRand = "DBMS_RANDOM.value()"

    @Shared
    Integer sampleLimit = 10

    void "load and validate StudentHold records"() {
        given:
        def aList = StudentHold.findAll("from StudentHold order by $sqlRand", [max: sampleLimit] as Map)

        expect: "random list of objects all validate"
        aList
        aList.every({ it && (it.validate() || it.errors.allErrors.any { println(it) }) })
    }

    @Unroll
    void "test finders for active transcript holds when pidm has #comment"() {
        when: "we check for results returned from the two finders"
        boolean found1 = StudentHold.findAllActiveByPidm(pidm as BigDecimal)?.size() > 0
        boolean found2 = StudentHold.findActiveByPidm(pidm as BigDecimal) != null

        then: "all the results match our expectation"
        found1 == expectResults
        found2 == expectResults

        where:
        pidm    | expectResults | comment
        1160013 | true          | "holds"
        779305  | false         | "no holds"
    }
}
