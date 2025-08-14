package ca.mohawkcollege.ocastranscript.type

import ca.mohawkcollege.baselib.util.MappedEnumType
import ca.mohawkcollege.ocastranscript.baseline.VerificationStage

import java.sql.Types

class VerificationStageUserType extends MappedEnumType<VerificationStage> {
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    @Override
    String[] columnNames() {
        return ["STATE_IND"]
    }

    @Override
    int[] typeLengths() {
        return [1]
    }
}
