package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.RequestPurpose

import java.sql.Types

class RequestPurposeUserType extends MappedEnumType<RequestPurpose> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["PURPOSE_CDE"]
    }

    @Override
    int[] typeLengths() {
        return [2]
    }
}
