package ca.mohawkcollege.ocastranscript

class TranscriptDigestJob extends AbstractQuartzJob {
    static jobName = "ocasTranscriptDigest"
    // Define triggers
    static triggers = getTriggers(jobName)

    def printTranscriptService

    def execute() {
        log.info("Job $jobName beginning")
        try {
            printTranscriptService.generatePdfDigest()
        }
        catch (Exception exception) {
            log.error("Job $jobName failed with exception:", exception)
        }
        log.info("Job $jobName complete")
    }
}
