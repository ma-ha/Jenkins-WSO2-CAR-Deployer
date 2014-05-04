package org.mh.jenkins.wso2;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


/**
 * Jenkins Plug-In to deploy a Cabon Application (CAR) artifact to a WSO2 Server or WSO2 ESB via SOAP admin API
 * 
 * @author mh
 *
 */
public class Wso2CarPublisher extends Recorder {

	public  String carSource;
	public  String carTargetFileName;
	public  String appType;
	public  String wso2URL;
	public  String wso2AdminUser;
	public  String wso2AdminPwd;

	/** Constructor using fields */
	@DataBoundConstructor
	public Wso2CarPublisher( String carSource, String carTargetFileName, String wso2URL, String wso2AdminUser, String wso2AdminPwd, String appType ){
		super();
		this.carSource = carSource.trim();
		this.carTargetFileName = carTargetFileName.trim();
		this.wso2URL = wso2URL.trim();
		this.wso2AdminUser = wso2AdminUser.trim();
		this.wso2AdminPwd  = wso2AdminPwd.trim();
		this.appType = appType.trim();
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}


	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	// --------------------------------------------------------------------------------------------

	/** Check input params and tart the deployment */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform( AbstractBuild build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
		EnvVars env = build.getEnvironment( listener ); 	
		String xcarSource = carSource;
		String xcarTargetFileName = carTargetFileName;
		String xwso2URL = wso2URL;
		String xwso2AdminUser = wso2AdminUser;
		String xwso2AdminPwd = wso2AdminPwd;

		if ( build.getResult().isWorseOrEqualTo( Result.FAILURE) ) {
			listener.getLogger().println( "[WSO2 CAR Deployer] WSO2 CAR upload: STOP, due to worse build result!" );
			return true; // nothing to do
		}
		listener.getLogger().println( "[WSO2 CAR Deployer] WSO2 CAR upload initiated (baseDir="+build.getArtifactsDir().getPath()+")" );

		if ( StringUtils.isBlank( xcarTargetFileName ) ) {
			listener.error( "[WSO2 CAR Deployer] CAR file name must be set!" ); 
			return false;
		} else 	if ( ! xcarTargetFileName.toLowerCase().endsWith( ".car" ) ) {
			listener.error( "[WSO2 CAR Deployer] CAR target file name must has .car ending!" ); 
			return false;
		} else {
			if ( xcarTargetFileName.startsWith( "$" ) ) {
				String envVar = xcarTargetFileName.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] 'CAR File Name' from env var: "+envVar );
				xcarTargetFileName = env.get( envVar );
			}
		}
		
		if ( StringUtils.isBlank( xcarSource ) ) {
			listener.error( "[WSO2 CAR Deployer] CAR source name must be set!" ); 
			return false;
		} else 	if ( ! xcarSource.toLowerCase().endsWith( ".car" ) ) {
			listener.error( "[WSO2 CAR Deployer] CAR source file name must has .car ending!" ); 
			return false;
		} else {
			if ( xcarSource.startsWith( "$" ) ) {
				String envVar = xcarSource.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] 'CAR Source File' from env var: "+envVar );
				xcarSource = env.get( envVar );
			}
		}
		
		if ( StringUtils.isBlank( xwso2URL ) ) {
			listener.error( "[WSO2 CAR Deployer] WSO2 server URL must be set!" ); 
			return false;
		} else {
			if ( xwso2URL.startsWith( "$" ) ) {
				String envVar = xwso2URL.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] 'WSO2 Server URL' from env var: "+envVar );
				xwso2URL = env.get( envVar );
			}
			if ( ! xwso2URL.endsWith("/") ) {
				xwso2URL += "/";
			}
		}
		
		// Validates that the organization token is filled in the project configuration.
		if ( StringUtils.isBlank( xwso2AdminUser ) ) {
			listener.error( "[WSO2 CAR Deployer] Admin user name must be set!" ); 
			return false;
		} else {
			if ( xwso2AdminUser.startsWith( "$" ) ) {
				String envVar = xwso2AdminUser.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] 'Admin User' from env var: "+envVar );
				xwso2AdminUser = env.get( envVar );
			}
		}
		
		// Validates that the organization token is filled in the project configuration.
		if ( StringUtils.isBlank( xwso2AdminPwd ) ) {
			listener.error( "[WSO2 CAR Deployer] Admin password must be set!" ); 
			return false;
		} else {
			if ( xwso2AdminPwd.startsWith( "$" ) ) {
				String envVar = xwso2AdminPwd.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] 'Admin Password' from env var: "+envVar );
				xwso2AdminPwd = env.get( envVar );
			}
		}

		boolean result = true;

		FilePath[] aarList = build.getWorkspace().list( xcarSource );
		if ( aarList.length == 0 ) {
			listener.error( "[WSO2 CAR Deployer] No CAR file found for '"+xcarSource+"'" );   
			return false;
		} else if ( aarList.length != 1  ) {
			listener.error( "[WSO2 CAR Deployer] Multiple CAR files found for '"+xcarSource+"'" );   
			for ( FilePath aarFile : aarList ) {
				listener.getLogger().println( "AAR is n="+aarFile.toURI() );
			}
			return false;
		} else {
			for ( FilePath aarFile : aarList ) {
				listener.getLogger().println( "[WSO2 CAR Deployer] CAR file is   = "+ aarFile.toURI() );
				listener.getLogger().println( "[WSO2 CAR Deployer] CAR file size = "+ aarFile.length() );

				InputStream fileIs = aarFile.read();

				Wso2CarDeployClient deployer = new Wso2CarDeployClient( xwso2URL, xwso2AdminUser, xwso2AdminPwd, listener );
				result = deployer.uploadCAR( fileIs, xcarTargetFileName, appType );
			}
		}
		return result;
	}

	// --------------------------------------------------------------------------------------------

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super( Wso2CarPublisher.class );
			load();
		}

		@SuppressWarnings("rawtypes")
		public boolean isApplicable( Class<? extends AbstractProject> aClass ) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		@Override
		public boolean configure( StaplerRequest req, JSONObject json ) throws FormException {
			req.bindJSON(this, json);
			save();
			return true;
		}

		/** This human readable name is used in the configuration screen. */
		public String getDisplayName() {
			return "Deploy Carbon App (CAR) to WSO2 Server or ESB"; 
		}
	}




}
