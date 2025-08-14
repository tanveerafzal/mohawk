package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.UserMatchResult

import java.sql.Types

class UserMatchResultUserType extends MappedEnumType<UserMatchResult> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["MATCH_IND"]
    }

    @Override
    int[] typeLengths() {
        return [1]
    }
}
