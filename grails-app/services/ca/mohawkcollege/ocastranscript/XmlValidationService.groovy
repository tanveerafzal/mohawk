package ca.mohawkcollege.ocastranscript

import com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory
import grails.util.Holders
import org.apache.commons.io.FileUtils
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Validator

class XmlValidationService {
    protected Validator validator

    // This doesn't work as a lazy getter because the file handles close.
    private static Source[] getXsdFiles() {
        Source[] xsdFiles

        String xsdDirname = Holders.config.mohawkcollege.ocastranscript.xsdDirectory
        File xsdDirectory = new File(xsdDirname)
        if (!xsdDirectory.isDirectory()) {
            throw new RuntimeException("Invalid XSD directory provided: $xsdDirname")
        }
        List<File> orderedFiles = [
                "CoreMain_v1.10.0.xsd",
                "AcademicRecord_v1.6.0.xsd"
        ].collect {
            new File(xsdDirectory, it)
        }
        List<File> unorderedFiles = FileUtils.listFiles(xsdDirectory, ["xsd"] as String[], false) - orderedFiles

        xsdFiles = (orderedFiles + unorderedFiles).collect { File file ->
            if (!file.canRead()) {
                throw new RuntimeException("Could not read file: ${file.path}")
            }
            return new StreamSource(new FileInputStream(file))
        }.findAll() as Source[]

        return xsdFiles
    }

    protected Validator getValidator() {
        if (!this.validator) {
            try {
                this.validator = XMLSchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                        newSchema(getXsdFiles()).
                        newValidator()
            } catch (SAXException e) {
                log.error("Error creating XML validator object (${e.localizedMessage})")
            }
        }

        return this.validator
    }

    /**
     * Validates and returns false if validation fails
     *
     * @param XmlContent current parsing xml content
     * @throws IOException
     */
    String getError(String XmlContent) throws IOException {
        try {
            getValidator().validate(new StreamSource(new StringReader(XmlContent)))
        } catch (SAXParseException e) {
            log.error("Invalid XML Content (${e.localizedMessage})")
            log.debug(XmlContent)
            return e.getLocalizedMessage()
        } catch (SAXException e) {
            log.error("Error validating XML Content:  ${e.localizedMessage}")
            log.debug(XmlContent)
            return e.getLocalizedMessage()
        }

        log.debug("Successfully validated XML")
        return null
    }

    static XmlValidationService getInstance() { Holders.grailsApplication.mainContext.xmlValidationService ?: new XmlValidationService() }
}
