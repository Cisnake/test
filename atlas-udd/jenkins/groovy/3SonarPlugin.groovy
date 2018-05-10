import jenkins.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.TriggersConfig
import hudson.tools.*
import org.yaml.snakeyaml.Yaml

// String configText = new File(System.getenv("JENKINS_HOME") + '/config.yaml').text
String configText = new File('/tmp/config.yaml').text
Yaml yaml = new Yaml()
properties = yaml.load(configText)
try {
  
  println("Configuring sonarQube")
  properties.PLUGINS.SONARS.each { sonar ->
    
  	def jenkins = Jenkins.getInstance()
	def sonar_conf = jenkins.getDescriptor("SonarGlobalConfiguration")
	def sonar_installations = sonar_conf.getInstallations()
  	def sonarServer = new SonarInstallation(sonar.NAME, sonar.SERVER_URL, '',
    	'', "", new TriggersConfig(), '')
  
  	sonar_installations += sonarServer
    sonar_conf.setInstallations((SonarInstallation[]) sonar_installations)
	sonar_conf.save()
  	
	println("Configuring sonarQube completed")
  }
} finally {
	//if we don't null kc, jenkins will try to serialise k8s objects and that will fail, so we won't see actual error
  	sonar_conf = null
  	sonar_installations = null
	  
}
