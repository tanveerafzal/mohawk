package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlCData
import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition "Core:LocalOrganizationIDType"
 */
@InheritConstructors
class LocalOrganizationID extends XmlFragment {

    List<XmlFragment> getChildren() {
        [
                new XmlCData(code as String).withTag("LocalOrganizationIDCode"),
                new XmlCData(qualifier as String).withTag("LocalOrganizationIDQualifier")
        ].findAll() as List<XmlFragment>
    }

    String getCode() { getString("LocalOrganizationIDCode") }

    String getQualifier() { getString("LocalOrganizationIDQualifier") }

}
