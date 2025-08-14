package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum ResponseType implements MappedEnum<String> {
    Incomplete("~"),
    Transcript("130"),
    Refusal("147")

    ResponseType(String value) { setValue(value) }
}