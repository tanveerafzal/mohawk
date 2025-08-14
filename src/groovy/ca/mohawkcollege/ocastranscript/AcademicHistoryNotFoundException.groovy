package ca.mohawkcollege.ocastranscript

class AcademicHistoryNotFoundException extends RuntimeException {
    String getMessage() { "No academic history found" }
}
