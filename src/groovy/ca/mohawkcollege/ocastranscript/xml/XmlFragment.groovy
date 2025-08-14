package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.xml.pesc.Format
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.Node
import groovy.xml.StreamingMarkupBuilder

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

class XmlFragment implements Buildable {
    String tag
    Map<String, String> attributes = [:]
    List<XmlFragment> children = []

    @Deprecated
    static final SimpleDateFormat FORMAT_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")
    @Deprecated
    static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd")

    // Default constructor
    XmlFragment() {
    }

    XmlFragment(Object stringLike) {
        try{
            parseText(stringLike.toString())
        }
        catch(Exception e){
            log.error("Prolog error: " + e.toString())
        }
    }

    XmlFragment(GPathResult gPathResult) {
        parseGPath(gPathResult)
    }

    XmlFragment(Node node) {
        parseNode(node)
    }

    XmlFragment(XmlFragment xmlFragment) {
        try{
            tag = xmlFragment.tag
            attributes = xmlFragment.attributes
            children = xmlFragment.children
        }catch(Exception parseException){
            log.error("Possible tag error: " + parseException.toString())
        }
    }

    XmlFragment(String tagName, XmlFragment content) {
        tag = tagName
        children = [content]
    }

    XmlFragment(String tagName, Object content) {
        this(tagName, new XmlFragment(content))
    }

    /**
     * Get a copy of the current XmlFragment, with its root tag changed to `tag`
     *
     * @param tag
     * @return
     */
    XmlFragment withTag(String tag) {
        new XmlFragment(this).with {
            it.setTag(tag)
            it
        }
    }

    /**
     * Get a new XmlFragment that contains this one, with a root tag of `tag`
     *
     * @param tag
     * @return
     */
    XmlFragment wrapWith(String tag) {
        new XmlFragment(tag, this)
    }

    void build(GroovyObject builder) {
        String thisTag = getTag()
        if (thisTag) {
            builder."$thisTag"(getAttributes()) { buildHelper(builder, getChildren()) }
        } else {
            buildHelper(builder, getChildren())
        }
    }

    List<String> getNoteMessages() {
        this.getChildren("NoteMessage")*.toString().findAll()
    }

    /**
     * Return the first child whose tag name matches the requested property. If none is found, return an empty
     * XmlFragment
     */
    def propertyMissing(String name) {
        if (name.contains(".")) {
            // split and iterate
            def (String head, String tail) = name.split(/\./, 2)
            this."$head"."$tail"
        } else if (name.startsWith("all_")) {
            // We want to return all the matching children
            getChildren(name.replaceFirst(/^all_/, ""))
        } else {
            // Just return the first child
            getChild(name)
        }
    }

    /**
     * Explicitly return a child XmlFragment
     */
    protected XmlFragment getChild(String tagName) {
        if (tagName.contains(".")) {
            // split and iterate
            def (String head, String tail) = tagName.split(/\./, 2)
            this.getChild(head)?.getChild(tail)
        } else {
            children.find { it.tag == tagName } ?: new XmlFragment()
        }
    }

    /**
     * Explicitly return a child XmlFragment list
     */
    protected List<XmlFragment> getChildren(String tagName) {
        if (tagName.contains(".")) {
            // split and iterate
            def (String head, String tail) = tagName.split(/\./, 2)
            this.getChildren(head)*.getChildren(tail).flatten() as List<XmlFragment>
        } else {
            children.findAll { it.tag == tagName }
        }
    }

    /**
     * Helper method to get a String XML element value
     *
     * @param childName
     * @return String representation of element found
     */
    protected String getString(String childName) { this.getChild(childName).with { it ? it.toString() : null } }

    /**
     * Set a string value in our object
     *
     * @param tagName
     * @param value
     */
    protected void setString(String tagName, String value) {
        XmlFragment valueFragment = new XmlCData(value as String)
        def childTag = children.find { it.tag == tagName }
        if (childTag) {
            childTag.children[0] = valueFragment
        } else {
            // Create new child element
            XmlFragment newChild = new XmlFragment()
            newChild.tag = tagName
            newChild.children = [valueFragment] as List<XmlFragment>
            children.add(newChild)
        }
    }

    /**
     * Helper method to get a LocalDate XML element value
     *
     * @param childName
     * @return LocalDate representation of element found, or null if empty
     * @throws DateTimeParseException if the XML value is in an invalid format
     */
    protected LocalDate getLocalDate(String childName) throws DateTimeParseException {
        getString(childName).with { it ? LocalDate.parse(it, Format.DATE.dateTimeFormatter) : null }
    }

    /**
     * Helper method to get a Date XML element value
     *
     * @param childName
     * @return Date representation of element found, or null if empty
     * @throws ParseException if the XML value is in an invalid format
     */
    protected Date getDate(String childName) throws ParseException {
        getString(childName).with { it ? Format.DATE.simpleDateFormat.parse(it) : null }
    }

    /**
     * Helper method to get a YearMonth XML element value
     *
     * The expected string format is as described by the primitive XSD type gYearMonth:
     * https://www.w3.org/TR/xmlschema11-2/#gYearMonth
     *
     * @param childName
     * @return LocalDate representation of element found, or null if empty
     * @throws DateTimeParseException if the XML value is in an invalid format
     */
    protected YearMonth getYearMonth(String childName) throws DateTimeParseException {
        getString(childName).with { it ? YearMonth.parse(it, Format.YEAR_MONTH.dateTimeFormatter) : null }
    }

    /**
     * Helper method to get a BigDecimal XML element value
     *
     * @param childName
     * @return BigDecimal representation of element found, if numeric, or null otherwise
     */
    protected BigDecimal getBigDecimal(String childName) {
        getString(childName).with { it?.isNumber() ? it.toBigDecimal() : null }
    }

    /**
     * Helper method to get a BigInteger XML element value
     *
     * @param childName
     * @return BigInteger representation of element found, if numeric, or null otherwise
     */
    protected BigInteger getBigInteger(String childName) {
        getString(childName).with { it?.isNumber() ? it.toBigInteger() : null }
    }

    protected XmlFragment parseText(String xmlString) { parseGPath(XmlSlurper.newInstance().parseText(xmlString)) }

    protected XmlFragment parseGPath(GPathResult gPathResult) { parseNode(gPathResult.nodeIterator().next() as Node) }

    protected XmlFragment parseNode(Node node) {
        setTag node.name()
        setChildren convertToList(node)
        this
    }

    protected static void buildHelper(GroovyObject builder, List children) {
        children.each { child ->
            (child instanceof XmlFragment) ? child.build(builder) : builder.mkp.yield(child)
        }
    }

    protected static List<XmlFragment> convertToList(GPathResult gPathResult) {
        // if node is text, add a XmlCData to the list. Otherwise add a generic XmlFragment
        gPathResult.nodeIterator().collect { child ->
            (child instanceof Node) ? new XmlFragment(child) : new XmlCData(child.toString())
        }
    }

    protected static List<XmlFragment> convertToList(Node node) {
        // if node is text, add a XmlCData to the list. Otherwise add a generic XmlFragment
        node?.children()?.collect { child ->
            (child instanceof Node) ? new XmlFragment(child) : new XmlCData(child.toString())
        }
    }

    String toXml() {
        this.with { thiz -> new StreamingMarkupBuilder().bind { GroovyObject builder -> thiz.build(builder) }.toString() }
    }

    String toString() {
        this.withTag(null).toXml()
    }

    boolean isEmpty() { !children.size() }

    /**
     * In Boolean context, an XmlFragment is true if and only if it has a nonempty tag name or children.
     *
     * @return
     */
    Boolean asBoolean() { tag || !empty }
}