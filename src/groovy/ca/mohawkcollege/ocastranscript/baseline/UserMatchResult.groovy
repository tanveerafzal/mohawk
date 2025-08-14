package ca.mohawkcollege.ocastranscript.baseline

import ca.mohawkcollege.baselib.util.MappedEnum

enum UserMatchResult implements MappedEnum<String> {
    MatchFound('X'), DuplicatesFound('D'), NoMatchFound('N')

    // Constructors
    UserMatchResult(String value) { setValue(value) }
}