
package org.wso2.carbon.application.upload;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.wso2.carbon.application.upload.xsd.UploadedFileItem;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileItems" type="{http://upload.application.carbon.wso2.org/xsd}UploadedFileItem" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileItems"
})
@XmlRootElement(name = "uploadApp")
public class UploadApp {

    @XmlElement(nillable = true)
    protected List<UploadedFileItem> fileItems;

    /**
     * Gets the value of the fileItems property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileItems property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UploadedFileItem }
     * 
     * 
     */
    public List<UploadedFileItem> getFileItems() {
        if (fileItems == null) {
            fileItems = new ArrayList<UploadedFileItem>();
        }
        return this.fileItems;
    }

}
