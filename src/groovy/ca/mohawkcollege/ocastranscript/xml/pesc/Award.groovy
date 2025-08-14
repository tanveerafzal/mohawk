package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors

/**
 * Corresponds to PESC definition acRec:AcademicAwardType
 */
@InheritConstructors
class Award extends XmlFragment {

    String getTitle() { getString("AcademicAwardTitle") }

    Date getDate() { getDate("AcademicAwardDate") }

    String getProgramType() { getString("AcademicAwardProgram.AcademicProgramType") }

    String getProgramName() { getString("AcademicAwardProgram.AcademicProgramName") }
}
