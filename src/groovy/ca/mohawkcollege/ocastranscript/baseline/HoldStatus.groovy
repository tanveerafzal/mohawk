package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum HoldStatus implements MappedEnum<String> {
    NoHold('N'), Hold('H'), BalanceOwingHold('$'), HoldOverridden('O')

    HoldStatus(String value) { setValue(value) }
}