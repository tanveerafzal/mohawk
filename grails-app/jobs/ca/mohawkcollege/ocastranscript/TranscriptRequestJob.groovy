package ca.mohawkcollege.ocastranscript

class TranscriptRequestJob  extends AbstractQuartzJob {
    static jobName = "ocasTranscriptRequest"
    // Define triggers
    static triggers = getTriggers(jobName)

    def ocasTranscriptRequestService

    def execute() {
        log.info("Job $jobName beginning")
        try {
            log.debug("1/2: Checking for new incoming transcript requests...")
            ocasTranscriptRequestService.checkForNewRequests()

            // will send transcripts
            log.debug("2/2: Processing transcript requests...")
            ocasTranscriptRequestService.processTranscriptRequests()

        }
        catch (Exception exception) {
            log.error("Job $jobName failed with exception:", exception)
        }
        log.info("Job $jobName complete")
    }
}
