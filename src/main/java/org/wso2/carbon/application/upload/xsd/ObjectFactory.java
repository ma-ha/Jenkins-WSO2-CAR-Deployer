
package org.wso2.carbon.application.upload.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.wso2.carbon.application.upload.xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _UploadedFileItemFileType_QNAME = new QName("http://upload.application.carbon.wso2.org/xsd", "fileType");
    private final static QName _UploadedFileItemFileName_QNAME = new QName("http://upload.application.carbon.wso2.org/xsd", "fileName");
    private final static QName _UploadedFileItemDataHandler_QNAME = new QName("http://upload.application.carbon.wso2.org/xsd", "dataHandler");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon.application.upload.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UploadedFileItem }
     * 
     */
    public UploadedFileItem createUploadedFileItem() {
        return new UploadedFileItem();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://upload.application.carbon.wso2.org/xsd", name = "fileType", scope = UploadedFileItem.class)
    public JAXBElement<String> createUploadedFileItemFileType(String value) {
        return new JAXBElement<String>(_UploadedFileItemFileType_QNAME, String.class, UploadedFileItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://upload.application.carbon.wso2.org/xsd", name = "fileName", scope = UploadedFileItem.class)
    public JAXBElement<String> createUploadedFileItemFileName(String value) {
        return new JAXBElement<String>(_UploadedFileItemFileName_QNAME, String.class, UploadedFileItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://upload.application.carbon.wso2.org/xsd", name = "dataHandler", scope = UploadedFileItem.class)
    public JAXBElement<byte[]> createUploadedFileItemDataHandler(byte[] value) {
        return new JAXBElement<byte[]>(_UploadedFileItemDataHandler_QNAME, byte[].class, UploadedFileItem.class, ((byte[]) value));
    }

}
