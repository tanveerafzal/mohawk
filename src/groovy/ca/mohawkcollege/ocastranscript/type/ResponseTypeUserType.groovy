package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.ResponseType

import java.sql.Types

class ResponseTypeUserType extends MappedEnumType<ResponseType> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["COMPLETION_IND"]
    }

    @Override
    int[] typeLengths() {
        return [3]
    }
}
