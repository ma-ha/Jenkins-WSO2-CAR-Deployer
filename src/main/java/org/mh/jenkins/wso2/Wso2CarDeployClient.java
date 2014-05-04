package org.mh.jenkins.wso2;

import hudson.model.BuildListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.wso2.carbon.application.upload.CarbonAppUploaderPortType;
import org.wso2.carbon.application.upload.UploadApp;
import org.wso2.carbon.application.upload.xsd.UploadedFileItem;


public class Wso2CarDeployClient {

	private CarbonAppUploaderPortType uploadSvc;
	private BuildListener listener;
	
	/** File read buffer size. */
    private static final int READ_BUFFER_SIZE = 4096;
	
	/** Constructor sets up the web service proxy client 
	 * @param listener */
	public Wso2CarDeployClient(  String serviceUrl, String adminUser, String adminPwd, BuildListener listener ) {
		this.listener = listener;
		listener.getLogger().println("[WSO2 CAR Deployer] Set up SOAP admin client...");   	
		
		Properties properties = System.getProperties();
		properties.put( "org.apache.cxf.stax.allowInsecureParser", "1" );
		System.setProperties( properties ); 
		
	    JaxWsProxyFactoryBean clientFactory = new JaxWsProxyFactoryBean(); 
        clientFactory.setAddress( serviceUrl+"CarbonAppUploader.CarbonAppUploaderHttpsEndpoint/" );
		clientFactory.setServiceClass( CarbonAppUploaderPortType.class );
		clientFactory.setUsername( adminUser );
		clientFactory.setPassword( adminPwd );
		
		uploadSvc =	(CarbonAppUploaderPortType) clientFactory.create();
		
		Client clientProxy = ClientProxy.getClient( uploadSvc );
		
		HTTPConduit conduit = (HTTPConduit) clientProxy.getConduit();
		HTTPClientPolicy httpClientPolicy = conduit.getClient();
		httpClientPolicy.setAllowChunking(false);
		
		String targetAddr = conduit.getTarget().getAddress().getValue();
		if ( targetAddr.toLowerCase().startsWith("https:") ) {
			TrustManager[] simpleTrustManager = new TrustManager[] { 
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}
		
						public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
						}
		
						public void checkServerTrusted(
								java.security.cert.X509Certificate[] certs, String authType) {
						}
					} 
			};
			TLSClientParameters tlsParams = new TLSClientParameters();
			tlsParams.setTrustManagers(simpleTrustManager);
			tlsParams.setDisableCNCheck(true); //TODO enable CN check
			conduit.setTlsClientParameters(tlsParams);
		}
	}
	
	
	/**
	 * Upload carbon abb artifact to WSO2 ESB or other WSO2 Server via WSO2 SOAP service
	 * @param appType 
	 * @param artifactFileName Filename for CAR artifact to upload to WSO2 Server
	 * @return true, if upload is OK
	 */
	public boolean uploadCAR(  InputStream fin, String targetFileName, String appType ) {
		boolean result = true;
		try {
			
	        UploadedFileItem fileItem = new UploadedFileItem();
	        
	        org.wso2.carbon.application.upload.xsd.ObjectFactory fieldFactory = new org.wso2.carbon.application.upload.xsd.ObjectFactory();
	        //byte[] fileContent = readFile( fin );
	        final byte fileContent[] = readFully(fin);
	        final int cnt = fileContent.length;
	        listener.getLogger().println( "[WSO2 CAR Deployer] Read CAR with "+cnt+" bytes" );

	        fileItem.setDataHandler( fieldFactory.createUploadedFileItemDataHandler( fileContent ) );
	        fileItem.setFileName( fieldFactory.createUploadedFileItemFileName( targetFileName ) );
	        fileItem.setFileType( fieldFactory.createUploadedFileItemFileType( appType ) );

	        UploadApp req = new UploadApp();
	        req.getFileItems().add( fileItem );
			
	        listener.getLogger().println( "[WSO2 CAR Deployer] Invoking uploadService for "+targetFileName+" ...");
			uploadSvc.uploadApp( req );
			
		} catch ( Exception e) {
			if ( e.getMessage().indexOf("uploadAppResponse was not recognized") > 0 ) {
				// TODO: Why is the response empty in WSDL?
				listener.getLogger().println( "[WSO2 CAR Deployer] WARNING: response ignored ;-)" );	
			} else {
				listener.getLogger().println( "[WSO2 CAR Deployer] ERROR: "+e.getMessage() );
				result = false;
				e.printStackTrace();
			}
		}
		
		return result ;
	}

   /**
     * Read all data available in this input stream and return it as byte[].
     * Take care for memory on large files!
     * <p>
     * Imported from org.jcoderz.commons.util.IoUtil</p>
     *
     * @param is the input stream to read from (will not be closed).
     * @return a byte array containing all data read from the is.
     */
    private byte[] readFully(InputStream is) throws IOException {
        final byte[] buffer = new byte[READ_BUFFER_SIZE];
        int read = 0;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((read = is.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }

        return out.toByteArray();
    }
	
}
