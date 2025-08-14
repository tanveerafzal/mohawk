package ca.mohawkcollege.ocastranscript.xml

import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
abstract class XmlSpecification extends Specification {
    // Helper code

    protected static int maxNestingDepth = 10
    protected static int maxNodeChildren = 3
    protected static int maxTagNameLength = 8
    protected static int maxTextContentLength = 12

    /**
     * Generate some random XML
     *
     * @return
     */
    protected static String getRandomXml() { getRandomXmlContent(0) }

    protected static String getRandomXmlContent(int depth) {
        depth > maxNestingDepth ? randomText : buildTag(randomTag, ([true] * RandomUtils.nextInt(maxNodeChildren)).collect {
            RandomUtils.nextBoolean() ? randomText : getRandomXmlContent(depth + 1)
        })
    }

    protected static String getRandomTag() {
        RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(maxTagNameLength) + 1)
    }

    protected static String getRandomText() {
        RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(maxTextContentLength) + 1)
    }

    protected static String buildTag(String tagName, String content) { buildTag([:], tagName, content) }

    protected static String buildTag(String tagName, List content) { buildTag([:], tagName, content) }

    protected static String buildTag(String tagName, Map content) {
        buildTag([:], tagName, content.collect { (it.value != null) ? buildTag(it.key, it.value) : null }.findAll())
    }

    protected static String buildTag(Map attributes, String tagName, List content) {
        buildTag(attributes, tagName, content.flatten().findAll().join() as String)
    }

    protected static String buildTag(Map attributes, String tagName, String content) {
        String attributeString = attributes.collect { entry -> " ${entry.key}='${entry.value}'" }.join()
        "<$tagName$attributeString>$content</$tagName>"
    }
}
