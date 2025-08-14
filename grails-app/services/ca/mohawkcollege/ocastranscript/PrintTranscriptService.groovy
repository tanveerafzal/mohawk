package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import com.lowagie.text.DocumentException

import java.text.SimpleDateFormat

class PrintTranscriptService {
    def pdfRenderingService
    def grailsApplication
    def groovyPageRenderer

    static String dateToFileNameFormat = "'OCAS_Transcripts'_yyyy-MM-dd.'pdf'"

    private Date privateDigestDate

    def generatePdfDigest() {
        // The date of the transcripts to be included is the day before the run date
        resetDigestDate()

        log.info("Generating PDF digest for transcripts received on: [${SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(digestDate)}]")

        List<OcasTranscript> transcripts = getDigestTranscripts()
        transcripts*.parseXml()

        String templatePath = "/pdf/transcript_template"
        Map templateModel = [transcripts: transcripts, digestDate: digestDate]
        File pdfOutput = getOutputFile(digestDate)

        if (pdfConfig.get("DEVL_exportHtml")) {
            try {
                log.debug("saving HTML to file...")
                def htmlFilename = pdfOutput.absolutePath + ".html"
                groovyPageRenderer.renderTo(new FileWriter(htmlFilename), template: templatePath, model: templateModel)
            } catch (IOException e) {
                throw new RuntimeException("Failed to save HTML to file", e)
            }
        }

        try {
            log.debug("rendering PDF content...")
            byte[] byteArray = renderToPdfBytes(templatePath, templateModel)

            log.debug("saving to file...")
            OutputStream outputStream = pdfOutput.newOutputStream()
            outputStream.write(byteArray)
            outputStream.close()
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to render PDF from template", e)
        } catch (IOException e) {
            throw new RuntimeException("Failed to save PDF to file", e)
        }
    }

    private ConfigObject getPdfConfig() { grailsApplication.config.mohawkcollege.ocastranscript.pdf }

    private resetDigestDate() { privateDigestDate = null }

    private Date getDigestDate() {
        if (!privateDigestDate) {
            // Get a fixed timestamp for this run of the application
            Date jobRunDate = DateUtils.now

            if (pdfConfig.containsKey("DEVL_FakeRunDate")) {
                def processDate = pdfConfig.get("DEVL_FakeRunDate")

                if (processDate instanceof Date) {
                    jobRunDate = processDate
                } else if (processDate instanceof String) {
                    jobRunDate = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).parse(processDate)
                } else {
                    log.error("Config override date had bad type: [${processDate.class.name}]")
                }

                log.debug("Config override for run date: " + jobRunDate)
            }

            if (pdfConfig.containsKey("DEVL_runDateOffset")) {
                def runDateOffset = pdfConfig.get("DEVL_runDateOffset")

                if (runDateOffset instanceof Number) {
                    jobRunDate = jobRunDate + runDateOffset.toInteger()
                } else {
                    log.error("Invalid config override date offset: [${runDateOffset}]")
                }

                log.debug("Config date offset override: " + jobRunDate)
            }

            privateDigestDate = jobRunDate.previous()
        }

        privateDigestDate
    }

    private File getOutputFile(Date fileDate) {
        // create File object for use in export

        // Parent directory comes from config
        if (!pdfConfig.containsKey("outputDirectory")) {
            throw new RuntimeException("Configuration key [pdf.outputDirectory] not specified")
        }
        String pdfDirectory = pdfConfig.get("outputDirectory")
        if (!pdfDirectory) {
            throw new RuntimeException("Configuration value for key [pdf.outputDirectory] is empty")
        }

        File parentDirectory = new File(pdfDirectory)
        try {
            if (!(parentDirectory.exists() || parentDirectory.mkdirs())) {
                throw new RuntimeException("Could not create PDF output directory $pdfDirectory")
            }
            if (!parentDirectory.isDirectory()) {
                throw new RuntimeException("Requested PDF output directory $pdfDirectory exists, but is not a directory")
            }
            if (!parentDirectory.canWrite()) {
                throw new RuntimeException("PDF output directory $pdfDirectory exists, but is not writable")
            }
        } catch (SecurityException securityException) {
            throw new RuntimeException("PDF output directory setup failed due to security exception", securityException)
        }

        File outputFile = new File(parentDirectory, fileDate.format(dateToFileNameFormat))
        log.debug("saving to file: [${outputFile.path}]")

        outputFile
    }

    private byte[] renderToPdfBytes(String template, Map model) {
        (pdfRenderingService.render(template: template, model: model) as ByteArrayOutputStream).toByteArray()
    }

    private List<OcasTranscript> getDigestTranscripts() {
        // establish the bounds of our transcript query
        Date startDate = DateUtils.getPreviousMidnight(digestDate)
        Date endDate = DateUtils.getNextMidnight(digestDate)

        log.debug(" - date boundaries are : [$startDate, $endDate]")

        OcasTranscript.findAllByDateCreatedBetween(startDate, endDate)
    }
}
