package ca.mohawkcollege.ocastranscript.xml

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.xml.StreamingMarkupBuilder
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class XmlCDataSpec extends XmlSpecification {

    @Unroll
    void "test building with #comment"() {
        given: "a Cdata with some content"
        def cdata = new XmlCData(originalContent)

        when: "we get the result of building the object"
        def result = new StreamingMarkupBuilder().bind { builder -> cdata.build(builder) }.toString()

        then: "the result is the same as the original content"
        result == originalContent

        where:
        originalContent              | comment
        "hello"                      | "plain string"
        "  playing  with  \n spaces" | "embedded spaces"
        ""                           | "empty content"
    }

    void "test that building with null content produces an empty string"() {
        given: "a Cdata with null content"
        def cdata = new XmlCData(null as String)

        when: "we get the result of building the object"
        def result = new StreamingMarkupBuilder().bind { builder -> cdata.build(builder) }.toString()

        then: "the result is empty"
        result == ""
    }

    void "test that using withTag results in an enclosing XML tag"() {
        given: "a Cdata with some content"
        def cdata = new XmlCData(content)

        when: "we get the result of building the object"
        def result = cdata.withTag(xmlTag).toXml()

        then: "the original content is nested in a tag"
        result == expectXml

        where:
        content = randomText
        xmlTag = randomTag
        expectXml = buildTag(xmlTag, content)
    }
}
