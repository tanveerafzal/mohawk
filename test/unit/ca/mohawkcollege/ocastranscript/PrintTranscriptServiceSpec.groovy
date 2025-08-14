package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.baselib.util.DateUtils
import ca.mohawkcollege.ocastranscript.ssb.OcasTranscript
import grails.gsp.PageRenderer
import grails.plugin.rendering.pdf.PdfRenderingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(PrintTranscriptService)
@Mock([OcasTranscript])
class PrintTranscriptServiceSpec extends Specification {

    static doWithConfig(ConfigObject c) {
        c.mohawkcollege.ocastranscript.pdf.outputDirectory = "testOutput"
    }

    def setup() {
        service.groovyPageRenderer = GroovyMock(PageRenderer)
        service.pdfRenderingService = GroovyMock(PdfRenderingService) {
            render(*_) >> GroovyMock(ByteArrayOutputStream) {
                asType(_) >> it
                toByteArray() >> ([65, 33, 65] as byte[])
            }
        }
    }

    @ConfineMetaClassChanges([DateUtils])
    void "test that a new digest date is generated on every run"() {
        GroovySpy(DateUtils, global: true)
        def anyFile = GroovyMock(File, global: true) {
            exists() >> true
            mkdirs() >> true
            isDirectory() >> true
            canWrite() >> true
            newOutputStream() >> Mock(BufferedOutputStream)
        }
        File."<init>"(*_) >> anyFile

        when: "we call the service method multiple times"
        trialCount.times { service.generatePdfDigest() }

        then: "we see a single call to DateUtils.now per time"
        trialCount * DateUtils.getNow()

        then: "no more calls to DateUtils.now"
        0 * DateUtils.getNow()

        where:
        trialCount << [0, 1, 5]
    }
}
