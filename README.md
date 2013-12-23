Jenkins-WSO2-CAR-Deployer
=========================

Jenkins Plugin: Deploy Carbon Apps (CAR) to WSO2 ESB or other WSO2 Server Types


Prepare Jenkins:
----------------
1. You should start the WSO2 Server and open the carbon console in the browser. 
2. Copy the certificate to a local file, e.g. wso2-as.cert
3. Load the certificate into your keystore (it will go to ~/.keystore by default):<br>
   <tt>keytool -import -trustcacerts -alias wso2as-key -file wso2-as.cert</tt>
4. Add parameter in Jenkins for Maven to trust the certificate of the WSO2 server:<br> Manage Jenkins > Configure System > Global MAVEN_OPTS:<br>
  -Djavax.net.ssl.trustStore=/home/mh/.keystore -Djavax.net.ssl.trustStorePassword=changeit

Build the Plugin
----------------
Get the sources:<br>
<tt>git clone https://github.com/ma-ha/Jenkins-WSO2-CAR-Deployer.git</tt>
<p>
To build the Jenkins plugin, simply call:<br>
<tt>mvn clean install -Dmaven.test.skip=true</tt>

Use the Jenkins Plugin:
----------------------
1. Load the Plugin (target/Jenkins-WSO2-CAR-Deployer.hpi) into Jenkins (Manage Jenkins > Manage Plugins)
2. Restart Jenkins
3. Configure your project to use "Deploy CAR to WSO2 Server" as "Post-build Actions"
4. Fill out the configuration form

Remark: You will get an exception on the Jenkins console:<br>
<tt>org.apache.cxf.interceptor.Fault: Message part {http://upload.application.carbon.wso2.org}uploadAppResponse was not recognized.  (Does it exist in service WSDL?)</tt><br>
This is ignored by the plugin, since it is a problem of the WSO2 WSDL generated code.


