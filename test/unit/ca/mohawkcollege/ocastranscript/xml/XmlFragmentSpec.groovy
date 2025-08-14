package ca.mohawkcollege.ocastranscript.xml

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class XmlFragmentSpec extends XmlSpecification {

    @Unroll
    void "test that output XML matches input when creating fragment from String with #comment"() {
        println "[$xml]"

        when: "we create an Xml Fragment from an XML string"
        def fragment = new XmlFragment(xml)

        then: "the resultant XML matches the input"
        fragment.toXml() == xml

        where:
        xml                                                             | comment
        "<rootnode><a><b>foo</b><b><c>bar</c>baz</b></a></rootnode>"    | "complex structure"
        "<rootnode>foo</rootnode>"                                      | "simple structure"
        "<rootnode><a><b>foo</b><b><c>bar</c></b></a></rootnode>"       | "single child node with complex structure"
        "<rootnode><a><b>foo</b>baz</a>quz<b></b><c>bar</c></rootnode>" | "multiple child nodes"
        "<rootnode>foo <strong>bar</strong> baz</rootnode>"             | "mixed structure"
        "<rootnode></rootnode>"                                         | "empty root node"
    }

    @Unroll
    void "test that output XML matches input when creating fragment from GPathResult with #comment"() {
        when: "we create an Xml Fragment from a parser result"
        def fragment = new XmlFragment(XmlSlurper.newInstance().parseText(xml))

        then: "the resultant XML matches the input"
        fragment.toXml() == xml

        where:
        xml                                                             | comment
        "<rootnode><a><b>foo</b><b><c>bar</c></b></a></rootnode>"       | "complex structure"
        "<rootnode>foo</rootnode>"                                      | "simple structure"
        "<rootnode><a><b>foo</b><b><c>bar</c></b></a></rootnode>"       | "single child node with complex structure"
        "<rootnode><a><b>foo</b>baz</a>quz<b></b><c>bar</c></rootnode>" | "multiple child nodes"
        "<rootnode>foo <strong>bar</strong> baz</rootnode>"             | "mixed structure"
        "<rootnode></rootnode>"                                         | "empty root node"
    }

    void "test that tag and children are populated when creating fragment from String with #comment"() {
        when: "we create an Xml Fragment from an XML string"
        def person = new XmlFragment(xml)

        then: "the property map and tag name are populated"
        person.tag == "rootnode"
        person.children.size() == xpCount

        where:
        xml                                                             | expectedTag | xpCount | comment
        "<rootnode><a><b>foo</b><b><c>bar</c></b></a></rootnode>"       | "rootnode"  | 1       | "single child node with complex structure"
        "<rootnode><a><b>foo</b>baz</a>quz<b></b><c>bar</c></rootnode>" | "rootnode"  | 4       | "multiple child nodes"
        "<rootnode>foo <strong>bar</strong> baz</rootnode>"             | "rootnode"  | 3       | "mixed structure"
        "<rootnode></rootnode>"                                         | "rootnode"  | 0       | "empty root node"
    }

    void "test that tag and children are populated when creating fragment from GPathResult"() {
        given: "a GPathResult created from an XML string"
        def gPathResult = XmlSlurper.newInstance().parseText(xml)

        when: "we create an Xml Fragment from the GPathResult"
        def person = new XmlFragment(gPathResult)

        then: "the property map and tag name are populated"
        person.tag == "rootnode"
        person.children.size() == expectedChildren

        where:
        xml                                                             | expectedTag | expectedChildren | comment
        "<rootnode><a><b>foo</b><b><c>bar</c></b></a></rootnode>"       | "rootnode"  | 1                | "single child node with complex structure"
        "<rootnode><a><b>foo</b>baz</a>quz<b></b><c>bar</c></rootnode>" | "rootnode"  | 4                | "multiple child nodes"
        "<rootnode>foo <strong>bar</strong> baz</rootnode>"             | "rootnode"  | 3                | "mixed structure"
        "<rootnode></rootnode>"                                         | "rootnode"  | 0                | "empty root node"
    }

    void "test that the tag+string constructor produces correct XML"() {
        when: "we call the constructor with an XML content string and a tag name"
        def fragment = new XmlFragment(xmlTag, innerXml)

        and: "get the resultant object's XML representation"
        def foundXml = fragment.toXml()
        println "foundXml $foundXml"

        then: "the XML is properly nested"
        foundXml == expectXml

        where:
        iteration << (1..5)

        innerXml = randomXml
        xmlTag = randomTag
        expectXml = buildTag(xmlTag, innerXml)
    }

    void "test that the tag+XmlFragment constructor produces correct XML"() {
        when: "we call the constructor with an XML content string and a tag name"
        def fragment = new XmlFragment(xmlTag, innerXml)

        and: "get the resultant object's XML representation"
        def foundXml = fragment.toXml()
        println "foundXml $foundXml"

        then: "the XML is properly nested"
        foundXml == expectXml

        where:
        iteration << (1..5)

        innerXml = randomXml
        xmlTag = randomTag
        expectXml = buildTag(xmlTag, innerXml)
    }

    void "test that setting a missing child to a string appends the child"() {
        given: "a starter XML fragment"
        def xmlFragment = new XmlFragment(initialXml)

        when: "we set a string child to a given value"
        xmlFragment.setString(newTag, newValue)

        then: "the XML contains the string in the right place"
        xmlFragment.toXml() == expectXml

        where:
        iteration << (1.5)

        xmlTag = randomTag
        xmlContent = randomXml
        initialXml = buildTag(xmlTag, xmlContent)
        newTag = randomTag
        newValue = randomText
        newXml = buildTag(newTag, newValue)
        expectXml = buildTag(xmlTag, [xmlContent, newXml])
    }

    void "test that setting an existing child to a string updates the child in place"() {
        given: "a starter XML fragment"
        println "starter: $initialXml"
        def xmlFragment = new XmlFragment(initialXml)

        when: "we set a string child to a given value"
        xmlFragment.setString(newTag, newValue)

        and: "get the resultant XML string"
        def foundXml = xmlFragment.toXml()

        then: "the XML contains the string in the right place"
        foundXml == expectXml

        where:
        iteration << (1..5)

        xmlTag = randomTag
        xmlContent = randomXml
        initialXml = buildTag(xmlTag, xmlContent)
        newTag = randomTag
        newValue = randomText
        newXml = buildTag(newTag, newValue)
        expectXml = buildTag(xmlTag, [xmlContent, newXml])
    }

    void "test that withTag changes only the root tag"() {
        given: "a simple XML fragment"
        def simpleFragment = new XmlFragment(oldXml)

        when: "we create a new object with a new tag"
        def newFragment = simpleFragment.withTag(newTag)

        then: "the two objects are different"
        simpleFragment != newFragment

        and: "their xml differs only in root tag"
        newFragment.toXml() == newXml

        where:
        oldTag = randomTag
        newTag = randomTag
        innerXml = randomXml
        oldXml = buildTag(oldTag, innerXml)
        newXml = buildTag(newTag, innerXml)
    }

    void "test that we can access children via property calls"() {
        given: "an XmlFragment made from some XML"
        def aFragment = new XmlFragment(outerXml)

        when: "we get a particular property of the object"
        def innerProperty = aFragment."$xmlTag"

        then: "the result is a child XmlFragment"
        noExceptionThrown()
        innerProperty instanceof XmlFragment
        innerProperty.toXml() == expectedChild

        where:
        xmlTag = randomTag
        expectedChild = buildTag(xmlTag, randomText)

        outerXml = ((1..5).collect { randomXml } + [expectedChild]).with {
            Collections.shuffle(it)
            buildTag(randomTag, it)
        }
    }

    void "test that we can access descendants via property calls"() {
        given: "an XmlFragment made from a nested XML string"
        def aFragment = new XmlFragment(nestedXml)

        when: "we get a particular property of the object"
        def innerProperty = aFragment.b.c.d

        then: "the result is a child XmlFragment"
        noExceptionThrown()
        innerProperty instanceof XmlFragment
        innerProperty.toXml() == innerXml

        where:
        innerXml = buildTag("d", randomText)
        nestedXml = buildTag("a", buildTag("b", buildTag("c", innerXml)))
    }

    void "test that a property call fetches only the first corresponding child element"() {
        given: "an XML string with some identically named children"
        def xml = "<a><b>foo</b><b>bar</b><b>baz</b></a>"

        and: "an XmlFragment made from that XML"
        def aFragment = new XmlFragment(xml)

        when: "we get a particular property of the object"
        def innerProperty = aFragment.b

        then: "the result is a child XmlFragment"
        noExceptionThrown()
        innerProperty instanceof XmlFragment
        innerProperty.toXml() == "<b>foo</b>"
    }

    void "test that the 'all_' prefix for property accessors fetches a list of fragments"() {
        given: "an XML string with some identically named children"
        def xml = "<a><b>foo</b><b>bar</b><b>baz</b></a>"

        and: "an XmlFragment made from that XML"
        def aFragment = new XmlFragment(xml)

        when: "we get a particular property of the object"
        def innerProperty = aFragment.all_b

        then: "the result is a list of XmlFragment"
        noExceptionThrown()
        innerProperty instanceof List<XmlFragment>
        innerProperty.size() == 3
    }

    @Unroll
    void "test that getNoteMessages returns the note messages with #comment"() {
        given: "an XML fragment built from an XML string"
        def xmlFragment = new XmlFragment(xmlString)

        expect: "the note message list to match our expectations"
        xmlFragment.noteMessages == expectedList

        where:
        messageList << [
                [],
                [""],
                ["abc"],
                ["def", "ghi", "jkl"],
                ["def", "", "jkl"],
                ["", "def", ""],
                ["", "", ""],
                ["", "", "def", "ghi"],
                ["", "def"]
        ]
        expectedList = messageList.findAll()
        xmlMessages = messageList.collect {
            "<NoteMessage>$it</NoteMessage>"
        }.join()
        xmlString = "<a>$xmlMessages</a>"

        countMessages = expectedList.size()
        countBlanks = messageList.size() - countMessages
        commentMessages = countMessages ? countMessages > 1 ? "multiple messages" : "one message" : "no messages"
        commentBlanks = countBlanks ? countBlanks > 1 ? "multiple blanks" : "one blank" : "no blanks"
        comment = "$commentMessages and $commentBlanks"
    }

    @Unroll
    void "test that a setString call properly #comment"() {
        given: "an XML string with a string value"
        def aFragment = new XmlFragment(xmlString)

        when: "we set the element text to be something else"
        aFragment.setString("a", "bar")

        then: "the result has been saved as expected"
        noExceptionThrown()
        aFragment.getString("a") == "bar"
        if (create) {
            aFragment.getString("b") == "foo"
        }

        where:
        create | xmlString                         | comment
        false  | "<rootnode><a>foo</a></rootnode>" | "modifies text of an existing tag element"
        true   | "<rootnode><b>foo</b></rootnode>" | "creates a missing tag element"
    }

    void "test that attributes get rendered in XML"() {
        given: "an XmlFragment with a populated attribute map"
        def xmlFragment = new XmlFragment()
        xmlFragment.tag = xmlTag
        xmlFragment.attributes = attributeMap
        xmlFragment.children.add(new XmlCData(xmlContent))

        when: "we render the XML"
        def foundXml = xmlFragment.toXml()

        then: "the XML contains the attributes"
        foundXml == expectXml

        where:
        xmlTag = randomTag
        attributeMap = [foo: "bar", baz: 42]
        xmlContent = randomText

        expectXml = buildTag(attributeMap, xmlTag, xmlContent)
    }
}
