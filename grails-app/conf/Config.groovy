/* Externalized config

   Application configuration is stored in two files:
   * /opt/tomcat/files/config/BaseLib-config.groovy
   * /opt/tomcat/files/config/OcasTranscriptSync-config.groovy

   The BaseLib config file is used to store database connection information
   and other more global configuration, while the OcasTranscriptSync config
   file is used for application-specific settings.
 */

System.properties['jdk.tls.client.protocols'] = 'TLSv1.2'

grails.config.defaults.locations = [BaseLibConfig]

if (!grails.config.locations)
	grails.config.locations = []
grails.config.locations << "file:${-> grails.config.dir}/BaseLib-config.groovy"
grails.config.locations << "file:${-> grails.config.dir}/BannerLib-config.groovy"
grails.config.locations << "file:${-> grails.config.dir}/${appName}-config.groovy"

// Change this to alter the default package name and Maven publishing destination
grails.project.groupId = 'ca.mohawkcollege.ocastranscript'
