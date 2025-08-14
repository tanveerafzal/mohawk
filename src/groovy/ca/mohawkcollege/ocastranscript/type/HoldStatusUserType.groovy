package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.HoldStatus

import java.sql.Types

class HoldStatusUserType extends MappedEnumType<HoldStatus>{
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["HOLD_IND"]
    }

    @Override
    int[] typeLengths() {
        return [1]
    }
}
