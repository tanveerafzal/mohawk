package ca.mohawkcollege.ocastranscript.xml

import ca.mohawkcollege.ocastranscript.xml.pesc.Format

class XmlCData extends XmlFragment {
    String content

    XmlCData(String contentString) {
        content = contentString
    }

    XmlCData(Number number) {
        this(number?.toString() as String)
    }

    XmlCData(Date date, Format format) {
        this(date ? format.simpleDateFormat.format(date) : null as String)
    }

    XmlFragment withTag(String tag) { wrapWith(tag) }

    void build(final GroovyObject builder) { builder.mkp.yield(content) }

    String toString() { content }

    String toXml() { content }

    /**
     * In Boolean context, an XmlCData is true if and only if it has nonempty content.
     *
     * @return
     */
    Boolean asBoolean() { content }
}
