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

	// job environment
	EnvVars env;

	// job params
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
		env = build.getEnvironment( listener ); 	
		
		// deployment only, if build is successfully 
		if ( build.getResult().isWorseOrEqualTo( Result.FAILURE) ) {
			listener.getLogger().println( "[WSO2 CAR Deployer] WSO2 CAR upload: STOP, due to worse build result!" );
			return true; // nothing to do
		}
		listener.getLogger().println( "[WSO2 CAR Deployer] WSO2 CAR upload initiated (baseDir="+build.getArtifactsDir().getPath()+")" );

		try {
			// validate input and get variable values
			String xCarSource         = checkParam( carSource, "AAR source", listener );
			String xCarTargetFileName = checkParam( carTargetFileName, "AAR target file name", listener );
			String xWso2URL           = checkParam( wso2URL, "WSO2 Server URL", listener );
			String xWso2AdminUser     = checkParam( wso2AdminUser, "WSO2 admin user", listener );
			String xWso2AdminPwd      = checkParam( wso2AdminPwd, "WSO2 admin password", listener );
			
			if ( ! xWso2URL.endsWith("/") ) {
				xWso2URL += "/";
			}

			boolean result = true;
	
			FilePath[] aarList = build.getWorkspace().list( xCarSource );
			if ( aarList.length == 0 ) {
				listener.error( "[WSO2 CAR Deployer] No CAR file found for '"+xCarSource+"'" );   
				return false;
			} else if ( aarList.length != 1  ) {
				listener.error( "[WSO2 CAR Deployer] Multiple CAR files found for '"+xCarSource+"'" );   
				for ( FilePath aarFile : aarList ) {
					listener.getLogger().println( "AAR is n="+aarFile.toURI() );
				}
				return false;
			} else {
				for ( FilePath aarFile : aarList ) {
					listener.getLogger().println( "[WSO2 CAR Deployer] CAR file is   = "+ aarFile.toURI() );
					listener.getLogger().println( "[WSO2 CAR Deployer] CAR file size = "+ aarFile.length() );
					listener.getLogger().println( "[WSO2 CAR Deployer] CAR target is = "+xCarTargetFileName );
	
					InputStream fileIs = aarFile.read();
	
					Wso2CarDeployClient deployer = new Wso2CarDeployClient( xWso2URL, xWso2AdminUser, xWso2AdminPwd, listener );
					result = deployer.uploadCAR( fileIs, xCarTargetFileName, appType );
				}
			}
			return result;
			
		} catch ( Exception e ) {
			return false;
		}

	}

	// --------------------------------------------------------------------------------------------
	/** Validate input and get variable values (if set) */
	private String checkParam( String param, String logName, BuildListener listener ) throws Exception {
		String result = param;
		if ( StringUtils.isBlank( param ) ) {
			listener.error( "[WSO2 Deployer] "+logName+" must be set!" ); 
			throw new Exception("param is blanc");
		} else {
			if ( param.startsWith( "$" ) ) {
				String envVar = param.substring( 1 );
				listener.getLogger().println( "[WSO2 Deployer] '"+logName+"' from env var: $"+envVar );
				result = env.get( envVar );
				if ( result == null ) {
					listener.error( "[WSO2 Deployer] $"+envVar+" is null (check parameter names and settings)" ); 
					throw new Exception("var is null");					
				}
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
