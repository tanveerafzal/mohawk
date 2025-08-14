package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.Gender

import java.sql.Types

class GenderUserType extends MappedEnumType<Gender> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["GENDER"]
    }

    @Override
    int[] typeLengths() {
        return [1]
    }
}
