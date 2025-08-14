package ca.mohawkcollege.ocastranscript.xml.pesc

import ca.mohawkcollege.ocastranscript.xml.XmlFragment
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberRange

/**
 * Corresponds to PESC definition "CoreMain:GPAType"
 */
@InheritConstructors
class Gpa extends XmlFragment {

    // AHRS
    Number getCreditHoursAttempted() { getBigDecimal("CreditHoursAttempted") }

    // EHRS
    Number getCreditHoursEarned() { getBigDecimal("CreditHoursEarned") }

    Number getCreditHoursRequired() { getBigDecimal("CreditHoursRequired") }

    Number getCreditHoursDeficient() { getBigDecimal("CreditHoursDeficient") }

    String getCreditUnit() { getString("CreditUnit") }

    Number getGradePointAverage() { getBigDecimal("GradePointAverage") }

    // QPTS
    Number getTotalQualityPoints() { getBigDecimal("TotalQualityPoints") }

    Number getCreditHoursForGpa() { getBigDecimal("CreditHoursforGPA") }

    Number getRangeMinimum() { getBigDecimal("GPARangeMinimum") }

    Number getRangeMaximum() { getBigDecimal("GPARangeMaximum") }

    NumberRange getRange() {
        (rangeMinimum == null || rangeMaximum == null) ? null : new NumberRange(rangeMinimum, rangeMaximum)
    }

    /**
     * The default String representation of this object is the grade point average value
     * @return
     */
    String toString() { gradePointAverage }
}
