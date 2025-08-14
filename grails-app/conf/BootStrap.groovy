
class BootStrap {

    def init = { servletContext ->
        System.setProperty("com.ibm.jsse2.overrideDefaultProtocol","TLSv12")
        System.setProperty("https.protocols","TLSv1.2")
        println "Security protocol used in bootstrap is "+System.getProperty("https.protocols")
    }
    def destroy = {
    }
}
