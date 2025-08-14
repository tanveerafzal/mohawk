package ca.mohawkcollege.ocastranscript.xml


import ca.mohawkcollege.ocastranscript.xml.pesc.DocumentType
import ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionData
import ca.mohawkcollege.ocastranscript.xml.pesc.TransmissionType
import groovy.transform.InheritConstructors
import groovy.xml.Namespace
import org.springframework.context.annotation.Lazy

/**
 * An XmlDocument represents the top-level object in an XML tree. This is the parent to the various XML documents
 * sent to and from the OCAS api.
 */
@InheritConstructors
abstract class XmlDocument extends XmlFragment {
    /**
     * The format of the document's tag is always identical
     *
     * @return
     */
    final String getTag() { [namespace.prefix, rootTag].join(":") }

    /**
     * Make sure we can't modify the tag once set.
     *
     * @param tag
     */
    final void setTag(String tag) {}

    /**
     * A concrete class inheriting from this class must define the XML tag it will use, not including namespace
     *
     * @return
     */
    abstract String getRootTag()

    /**
     * A concrete class inheriting from this class must define the XSD namespace it will use
     *
     * @return
     */
    abstract Namespace getNamespace()

    // The general XSI namespace is always included
    static Namespace xsiNamespace = new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi")

    /**
     * Concrete subclasses must define a document type
     *
     * @return
     */
    abstract DocumentType getDocumentType()

    // Default transmission type may be overridden
    TransmissionType transmissionType = TransmissionType.Original

    /**
     * By default, the attributes of the top-level XML tag are just the ones required to set the appropriate XML
     * namespaces. This may be overridden by subclasses.
     *
     * @return
     */
    Map<String, String> getAttributes() {
        [
                ("xmlns:${xsiNamespace.prefix}" as String): xsiNamespace.uri,
                ("xmlns:${namespace.prefix}" as String)   : namespace.uri
        ]
    }

    /**
     * Every subclass has a TransmissionData element. This lazy getter may be overridden if needed.
     */
    @Lazy
    @Delegate(includes = ["getRequestTrackingId", "setRequestTrackingId", "getCreatedDateTime", "setCreatedDateTime"])
    TransmissionData transmissionData = {
        XmlFragment tDataFragment = getChild("TransmissionData")
        TransmissionData tData = (tDataFragment ? new TransmissionData(tDataFragment) : new TransmissionData())
        tData.documentType = getDocumentType()
        tData.transmissionType = transmissionType
        tData
    }()
}
