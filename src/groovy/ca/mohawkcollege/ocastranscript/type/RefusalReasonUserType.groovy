package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.RefusalReason

import java.sql.Types

class RefusalReasonUserType  extends MappedEnumType<RefusalReason> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["REASON_CDE"]
    }

    @Override
    int[] typeLengths() {
        return [3]
    }
}
