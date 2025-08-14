package ca.mohawkcollege.ocastranscript

import grails.util.Holders

abstract class AbstractQuartzJob {

    static List integerProperties = ["startDelay", "repeatInterval", "repeatCount"]

    // Define triggers
    static Closure getTriggers(String jobName) {
        return {
            // Some properties need to be turned into integers
            log.debug("Setting triggers for job $jobName")
            Properties jobProps =
                    Holders.config.mohawkcollege.ocastranscript.job."${jobName}".toProperties().collectEntries { key, value ->
                        [(key): integerProperties.contains(key) ? value.toString().toInteger() : value.toString()]
                    }
            if (jobProps.containsKey('repeatInterval')) {
                log.debug("Running job on an interval")
                simple(jobProps)
            } else if (jobProps.containsKey('cronExpression')) {
                log.debug("Running job on a schedule")
                cron(jobProps)
            } else {
                log.info("Job $jobName is disabled as it has no valid trigger configuration")
            }
        }
    }

    abstract def execute()
}
