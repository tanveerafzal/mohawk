grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = 'target/work'
grails.project.target.level = 1.8
grails.project.source.level = 1.8
grails.project.war.file = "target/ocas-transcript-sync##${appVersion}.war"

grails.project.fork = [
        // Configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        // compile: [maxMemory: 512, minMemory: 128, debug: false, maxPerm: 256],

        // Configure settings for the test-app JVM, uses the daemon by default
        test: false,
        // Configure settings for the run-app JVM
        run: [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 512, forkReserve: false,
              jvmArgs: ['-Djdk.tls.ephemeralDHKeySize=2048', '-Doracle.net.tns_admin=\\\\mcfs01\\tnsnames$']],
        // Configure settings for the run-war JVM
        war: [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 512, forkReserve: false,
              jvmArgs: ['-Djdk.tls.ephemeralDHKeySize=2048', '-Doracle.net.tns_admin=\\\\mcfs01\\tnsnames$']],
        // Configure settings for the Console UI JVM
        console: [maxMemory: 1024, minMemory: 256, debug: false, maxPerm: 512,
                  jvmArgs: ['-Djdk.tls.ephemeralDHKeySize=2048', '-Doracle.net.tns_admin=\\\\mcfs01\\tnsnames$']]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits "global"
    // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    log "error"
    // Whether to verify checksums on resolve
    checksums true
    // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    legacyResolve false

    repositories {
        // Whether to inherit repository definitions from plugins
        inherits "true"

        // Local Artifactory repos
        mavenRepo('http://svn.mohawkcollege.ca:8081/artifactory/plugins-snapshot-local/') { updatePolicy "always" }
        mavenRepo('http://svn.mohawkcollege.ca:8081/artifactory/plugins-release-local/')

        // Remote repos
        grailsCentral()
        grailsPlugins()
        mavenCentral() 
        mavenLocal()
        grailsHome()
    }
    dependencies {
        // Specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes
        //build "org.apache.maven.wagon:wagon-http:jar:1.0-beta-2"

        runtime "org.springframework:spring-test:4.2.1.RELEASE"
        test "org.spockframework:spock-core:1.3-groovy-2.4"
    }

    plugins {
        // plugins for the build system only
        build ':tomcat:8.0.50'

        // Needed for scheduled jobs:
        runtime(':quartz:1.0.2')

        compile ":rest-client-builder:2.1.1"
        compile('ca.mohawkcollege:banner-lib:3.3.4')
        runtime ':rendering:1.0.0'
    }
}
