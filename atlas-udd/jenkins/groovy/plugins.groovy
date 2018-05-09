import hudson.model.*
import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*
import org.csanchez.jenkins.plugins.kubernetes.volumes.workspace.EmptyDirWorkspaceVolume
import org.csanchez.jenkins.plugins.kubernetes.volumes.HostPathVolume
import org.yaml.snakeyaml.Yaml

// since kubernetes-1.0
// import org.csanchez.jenkins.plugins.kubernetes.model.KeyValueEnvVar
// import org.csanchez.jenkins.plugins.kubernetes.PodEnvVar
// change after testing
// ConfigObject conf = new ConfigSlurper().parse(new File(System.getenv("JENKINS_HOME") + '/config.yaml').text)


String configText = new File(System.getenv("JENKINS_HOME") + '/config.yaml').text
Yaml yaml = new Yaml()
properties = yaml.load(configText)

def jenkins = Jenkins.getInstance()
def kubernetesTemplate
try {
	println("Configuring k8s")

	properties.PLUGINS.KUBERNETES.each { k8sPodTemplate ->
    	
    	kubernetesTemplate = new KubernetesCloud(k8sPodTemplate.NAME)	
    	kubernetesTemplate.setContainerCapStr(k8sPodTemplate.CONTAINER_CAP_STR)
    	kubernetesTemplate.setServerUrl(k8sPodTemplate.SERVER_URL)
    	kubernetesTemplate.setSkipTlsVerify(k8sPodTemplate.SKIP_TLS_VERIFY)
    	// kc.setNamespace(k8sPodTemplate.NAMESPACE)
    	kubernetesTemplate.setJenkinsUrl(k8sPodTemplate.JENKINS_URL)
    	// kc.setCredentialsId(k8sPodTemplate)
    	kubernetesTemplate.setRetentionTimeout(k8sPodTemplate.RETENTION_TIMEOUT)
    	kubernetesTemplate.setConnectTimeout(k8sPodTemplate.CONNECT_TIMEOUT)
    	kubernetesTemplate.setReadTimeout(k8sPodTemplate.READ_TIMEOUT)

      	jenkins.clouds.add(kubernetesTemplate)
        println "cloud added: ${k8sPodTemplate.NAME}"
      
      	println "set templates"
        // kubernetesTemplate.templates.clear()

    	k8sPodTemplate.POD_TEMPLATES.each { podTemplateConfig ->
        	def podTemplate = new PodTemplate()
        	podTemplate.setLabel(podTemplateConfig.label)
			podTemplate.setName(podTemplateConfig.name)  
        }

           
    }
