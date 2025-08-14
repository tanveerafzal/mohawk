package ca.mohawkcollege.ocastranscript.ssb

trait HasBannerProgram {
    String programCode
    String programName

    String getProgramDescription() { [programCode, programName].findAll().join(' ') }
}