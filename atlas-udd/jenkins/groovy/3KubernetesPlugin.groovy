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


String configText = new File('/tmp/config.yaml').text
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
        kubernetesTemplate.setMaxRequestsPerHostStr(k8sPodTemplate.MAX_REQUESTS_PER_HOST_STR)
      	println "set templates"
        // kubernetesTemplate.templates.clear()

    	k8sPodTemplate.POD_TEMPLATES.each { podTemplateConfig ->
        	def podTemplate = new PodTemplate()
        	podTemplate.setLabel(podTemplateConfig.LABEL)
			podTemplate.setName(podTemplateConfig.NAME)
          	podTemplate.setNamespace(podTemplateConfig.NAMESPACE)
         	//podTemplate.setMaxRequestsPerHostStr(podTemplateConfig.MAX_REQUESTS_PER_HOST_STR)
          
          	// Containers
        	if (podTemplateConfig.CONTAINER_TEMPLATE) {
            	def ctrTempaltes = []
				podTemplateConfig.CONTAINER_TEMPLATE.each { ctr ->
                	println "containerTemplate: ${ctr.NAME}"
                	ContainerTemplate ct = new ContainerTemplate(ctr.NAME, ctr.IMAGE)
                    
                    ct.setAlwaysPullImage(ctr.ALWAYS_PULL_IMAGE)
            		ct.setPrivileged(ctr.PRIVILIGIED)
            		ct.setTtyEnabled(ctr.TTY_ENABLED)
            		ct.setWorkingDir(ctr.WORKING_DIR)
            		ct.setArgs(ctr.ARGS)
            		ct.setCommand(ctr.COMMAND)
                  		      INITIAL_DELAY_SECONDS: 0
		     // TIMEOUT_SECONDS: 0
		     // FAILURE_THRESHOLD: 0
		     // PERIOD_SECONDS: 0
		     // SUCCESS_THRESHOLD: 0
                  	ctrTempaltes << ct
                }
              	podTemplate.setContainers(ctrTempaltes)
            }
          
          	// Volumes
        	if (podTemplateConfig.VOLUMES) {
            	def volumes = []
				podTemplateConfig.VOLUMES.each { volume ->
                  	println "volume "
                	if (volume.TYPE == 'HostPathVolume') {
                    	volumes << new HostPathVolume(volume.HOST_PATH, volume.MOUNT_PATH)
					}
                }
              	podTemplate.setVolumes(volumes)
            }
          	kubernetesTemplate.templates << podTemplate
		}
		jenkins.clouds.add(kubernetesTemplate)
    	println "cloud added: ${k8sPodTemplate.NAME}"
    }
  	
	println("Configuring k8s completed")
} finally {
	//if we don't null kc, jenkins will try to serialise k8s objects and that will fail, so we won't see actual error
  	kubernetesTemplate = null
}
