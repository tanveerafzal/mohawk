package ca.mohawkcollege.ocastranscript

class IllegalEnumValueException extends RuntimeException {
    Class<? extends Enum> enumType
    Object illegalValue
    private boolean valueSpecified = false

    IllegalEnumValueException() {
        super()
    }

    IllegalEnumValueException(Class<? extends Enum> type) {
        super()
        this.enumType = type
    }

    IllegalEnumValueException(Class<? extends Enum> type, Object value) {
        super()
        this.enumType = type
        this.setIllegalValue(value)
    }

    IllegalEnumValueException(Throwable cause) {
        super(cause)
    }

    IllegalEnumValueException(Class<? extends Enum> type, Throwable cause) {
        super(cause)
        this.enumType = type
    }

    IllegalEnumValueException(Class<? extends Enum> type, Object value, Throwable cause) {
        super(cause)
        this.enumType = type
        this.setIllegalValue(value)
    }

    String getLocalizedMessage() {
        return "Illegal value${valueSpecified ? " [${illegalValue}]" : ""} " +
                "found for enumeration${enumType ? " with type ${enumType.name}" : ""}"
    }

    void setIllegalValue(Object value) {
        illegalValue = value
        valueSpecified = true
    }
}
