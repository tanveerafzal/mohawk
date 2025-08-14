package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum Gender implements MappedEnum<String> {
    Female("F"), Male("M"), Unreported("U")

    Gender(String value) { setValue(value) }
}