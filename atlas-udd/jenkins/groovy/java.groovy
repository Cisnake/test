import hudson.model.JDK
import hudson.tools.InstallSourceProperty
import hudson.tools.ZipExtractionInstaller

def descriptor = new JDK.DescriptorImpl();


def List<JDK> installations = []

javaTool=['name':'jdk8', 'javaHome':'/tools/jdk1.8.0_144']

println("Setting up tool: ${javaTool.name}")

def jdk = new JDK(javaTool.name as String, javaTool.javaHome as String)
installations.add(jdk)

descriptor.setInstallations(installations.toArray(new JDK[installations.size()]))
descriptor.save()