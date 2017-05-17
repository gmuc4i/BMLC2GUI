/*----------------------------------------------------------------*
|   Copyright 2009-2011 Networking and Simulation Laboratory      |
|         George Mason University, Fairfax, Virginia              |
|                                                                 |
| Permission to use, copy, modify, and distribute this            |
| software and its documentation for all purposes is hereby       |
| granted without fee, provided that the above copyright notice   |
| and this permission appear in all copies and in supporting      |
| documentation, and that the name of George Mason University     |
| not be used in advertising or publicity pertaining to           |
| distribution of the software without specific, written prior    |
| permission. GMU makes no representations about the suitability  |
| of this software for any purposes.  It is provided "AS IS"      |
| without express or implied warranties.  All risk associated     |
| with use of this software is expressly assumed by the user.     |
*----------------------------------------------------------------*/

package edu.gmu.netlab;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaxfront.core.dom.DOMBuilder;
import com.jaxfront.core.schema.ValidationException;
import com.jaxfront.core.type.Type;
import com.jaxfront.core.util.URLHelper;
import com.jaxfront.core.util.io.cache.XUICache;

import edu.gmu.c4i.sbml.Exception_Exception;
import edu.gmu.c4i.sbmlclientlib.SBMLClient;

/**
 * MSDL Support Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	5/15/2011
 */ 
public class MSDL {
	
	/**
	 * Clears cache
	 */
	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
	
	/**
	 * Creates and displays a new Opord DOM from an XML file
	 */
	void openMSDL_FS() { 
		releaseXUICache();	
		BMLC2GUI.bml.bmlDocumentType = "MSDL";	
		BMLC2GUI.bml.documentTypeLabel.setText(BMLC2GUI.bml.bmlDocumentType);	
		//BMLC2GUI.bml.xsdUrl = URLHelper.getUserURL("c:\\BMLC2GUI\\MSDL\\MilitaryScenario_1.0.0.xsd");//BMLC2GUI.bml.opord_C2CoreSchemaFile); 
		BMLC2GUI.bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation +
        "\\BMLC2GUI\\Schema\\MSDL\\MilitaryScenario_1.0.0.xsd"
    );
		
    // XML file
		JFileChooser xmlFc = new JFileChooser(BMLC2GUI.bml.guiFolderLocation);
		xmlFc.setDialogTitle("Enter the MSDL file name");
		xmlFc.showOpenDialog(BMLC2GUI.bml);
    if(xmlFc.getSelectedFile() == null)return;
		BMLC2GUI.bml.xmlUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		BMLC2GUI.bml.tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		BMLC2GUI.bml.xuiUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation +
          "\\BMLC2GUI\\XUIView\\MSDLView.xui");
		BMLC2GUI.bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		BMLC2GUI.printDebug(BMLC2GUI.bml.xuiUrl.toString());
		BMLC2GUI.bml.root = "MilitaryScenario";
			//BMLC2GUI.bml.opord_C2CoreRoot; //"C-BML OrderPush"; // NATO OPORD"OperationsOrder";
		BMLC2GUI.bml.drawMSDL(BMLC2GUI.bml.xsdUrl, BMLC2GUI.bml.xmlUrl, BMLC2GUI.bml.xuiUrl, BMLC2GUI.bml.root);
	}

	/**
	 * Create a new MSDL file
	 * 
	 * 11/28/2011
	 * mababneh
	 */
	void newMSDL() {
		BMLC2GUI.bml.root = null;
		BMLC2GUI.bml.bmlDocumentType = "MSDL";
		BMLC2GUI.bml.documentTypeLabel.setText(BMLC2GUI.bml.bmlDocumentType);
		releaseXUICache();
		
		// Order Schema C://BMLC2GUI//IBML-MSG048-09//IBMLOrderPushPulls.xsd
		BMLC2GUI.bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation +
        "\\BMLC2GUI\\Schema\\MSDL\\MilitaryScenario_1.0.0.xsd"
      );
		BMLC2GUI.bml.xmlUrl = null;		//Empty XML
		BMLC2GUI.bml.xuiUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation +
        "\\BMLC2GUI\\XUIView\\MSDLView.xui"
      );
		
		BMLC2GUI.printDebug(BMLC2GUI.bml.xuiUrl.toString());
		BMLC2GUI.bml.root = "MilitaryScenario";
		BMLC2GUI.bml.initDom(
      "default-context", 
      BMLC2GUI.bml.xsdUrl, 
      BMLC2GUI.bml.xmlUrl, 
      BMLC2GUI.bml.xuiUrl, 
      BMLC2GUI.bml.root);
	}
	/**
	 * Push MSDL file to web service
	 * 
	 * 11/28/2011
	 * mababneh
	 */
	void pushMSDL() {		
		String pushResultString ="";		// String to hold the result of the execution of the SBML XML query
		String pushOrderInputString ="";		// String to hold the input to the SBML XML query

		try {
			// assign the text of the XML document to pushOrderInputString
			pushOrderInputString = BMLC2GUI.bml.currentDom.serialize().toString();
		} catch (ValidationException e) {
			e.printStackTrace();
		}		

		//Running the SBML Query through the SBMLClient
		//SBMLClient - Secure or non-secure depending on library implementation
    	SBMLClient sbmlClient = null;
		try {
			sbmlClient = new SBMLClient(BMLC2GUI.bml.sbmlOrderServerName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}	
    	
    	try {
    		BMLC2GUI.printDebug("Starting the Web Service query ");
    		
    		// call the callSBML method to execute the query
    		pushResultString = sbmlClient.sbmlProcessBML(pushOrderInputString, BMLC2GUI.bml.sbmlOrderDomainName,BMLC2GUI.bml.orderBMLType);			
		} catch (Exception_Exception e2) {
			System.err.println("The query execution was unsuccessful....... ");	
			JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "Order Push Message",JOptionPane.INFORMATION_MESSAGE);
		}
 
    	BMLC2GUI.printDebug("The query result is : " + pushResultString);  
    	JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "Order Push Message",JOptionPane.INFORMATION_MESSAGE);	       
	}
} //End of class MSDL
