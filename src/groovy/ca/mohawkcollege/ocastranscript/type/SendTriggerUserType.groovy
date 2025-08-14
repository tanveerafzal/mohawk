package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.xml.SendTrigger

import java.sql.Types

class SendTriggerUserType extends MappedEnumType<SendTrigger> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["ACTION_CDE"]
    }

    @Override
    int[] typeLengths() {
        return [2]
    }
}
