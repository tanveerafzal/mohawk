package ca.mohawkcollege.ocastranscript

class TranscriptJob extends AbstractQuartzJob {
    static jobName = "ocasTranscript"
    // Define triggers
    static triggers = getTriggers(jobName)

    def ocasTranscriptService

    def execute() {
        log.info("Job $jobName beginning")
        try {
            log.debug("Fetching new incoming transcripts...")
            ocasTranscriptService.fetchNewTranscripts()
        }
        catch (Exception exception) {
            log.error("Job $jobName failed with exception:", exception)
        }
        log.info("Job $jobName complete")
    }
}
