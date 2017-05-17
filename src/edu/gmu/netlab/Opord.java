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
 * Opord Support Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	5/15/2011
 */ 
public class Opord {
	
	/**
	 * Clears cache
	 */
	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
	
	/**
	 * Creates a new Opord DOM from an XML file
	 */
	void newOPORD() {
		BMLC2GUI.bml.root = null;
		BMLC2GUI.bml.bmlDocumentType = "OPORD";
		BMLC2GUI.bml.documentTypeLabel.setText(BMLC2GUI.bml.bmlDocumentType);
		releaseXUICache();
		
		URL url = URLHelper.getUserURL(BMLC2GUI.bml.schemaFolderLocation + "/OPORD/NATO_OPORD.xsd");		// OPORD Schema 
		URL xmlUrl = null;		//Empty XML
		URL xuiUrl = URLHelper.getUserURL(BMLC2GUI.bml.xuiFolderLocation + "/OPORD.xui"); 		// XUI Style
		BMLC2GUI.bml.initDom("default-context", url, xmlUrl, xuiUrl, BMLC2GUI.bml.root);
	}
	
	/**
	 * Creates and displays a new Opord DOM from an XML file
	 */
	void openOPORD_FS() { 
		releaseXUICache();	
		BMLC2GUI.bml.bmlDocumentType = "OPORD";//IBML_CBMLOrder"; // Temp for array method of location graphics - "OPORD"; 5/13/2011	
		BMLC2GUI.bml.documentTypeLabel.setText(BMLC2GUI.bml.bmlDocumentType);	
		BMLC2GUI.bml.xsdUrl = URLHelper.getUserURL(BMLC2GUI.bml.schemaFolderLocation + "/OPORD/NATO_OPORD.xsd");
                //BMLC2GUI.bml.opord_C2CoreSchemaFile); 

		// XML file
		JFileChooser xmlFc = new JFileChooser(BMLC2GUI.bml.guiFolderLocation);
		xmlFc.setDialogTitle("Enter the OPORD XML file name");
		xmlFc.showOpenDialog(BMLC2GUI.bml);
    if(xmlFc.getSelectedFile() == null)return;
		BMLC2GUI.bml.xmlUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		BMLC2GUI.bml.tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		BMLC2GUI.bml.xuiUrl = URLHelper.getUserURL(BMLC2GUI.bml.xuiFolderLocation + "/OPORD.xui"); //xuiFolderLocation); // + "/OPORD.xui");
		BMLC2GUI.bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		BMLC2GUI.printDebug(BMLC2GUI.bml.xuiUrl.toString());
		BMLC2GUI.bml.root = "OperationsOrder";// "C-BML OrderPush"; //BMLC2GUI.bml.opord_C2CoreRoot; //; // NATO OPORD;
		BMLC2GUI.bml.drawOPORD_FS(BMLC2GUI.bml.xsdUrl, BMLC2GUI.bml.xmlUrl, BMLC2GUI.bml.xuiUrl, BMLC2GUI.bml.root);
	}
		


	/*
	 * Send an Edited (optionally validated) OPORD (XML Document) to the Web Services through the SBML Client
     *
     * Used for demostrations
     *
	 * @since	10/28/2010
	 */
	private void pushOPORD() {
		String pushResultString ="";		// String to hold the result of the execution of the SBML XML query
		String pushOrderInputString ="";		// String to hold the input to the SBML XML query

		try {
			// assign the text of the XML document to pushOrderInputString
			pushOrderInputString = BMLC2GUI.bml.currentDom.serialize().toString();
		
			// See if the serialization to a file is different ? - Answer: No
			File ibml_CBMLFile = new File(BMLC2GUI.bml.guiFolderLocation + "//SBMLClient//dist//IBML_C_BML.xml");
			try {
				BMLC2GUI.bml.currentDom.saveAs(ibml_CBMLFile);
			} catch (ValidationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			BMLC2GUI.printDebug(pushOrderInputString.substring(0, 3000));
			pushOrderInputString = pushOrderInputString.replace(" C_BML-light-Order.xsd\"","");		
			BMLC2GUI.printDebug(pushOrderInputString.substring(0, 2000));
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
			JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "OPORD Push Message",JOptionPane.INFORMATION_MESSAGE);
		}

		BMLC2GUI.printDebug("The query result is : " + pushResultString);  
		JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "OPORD Push Message",JOptionPane.INFORMATION_MESSAGE);      
	}

	/*
	 * Send an Edited (optionally validated) OPORD (XML Document) to the Web Services through the SBML Client
	 *
	 * @since	5/15/2011
	 */
	void pushOPORD_C2Core() {
		String pushResultString ="";	// String to hold the result of the execution of the SBML XML query
		String pushOrderInputString ="";	// String to hold the input to the SBML XML query

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
			BMLC2GUI.printDebug("The query execution was unsuccessful....... ");	
			JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "OPORD Push Message",JOptionPane.INFORMATION_MESSAGE);
		}

		BMLC2GUI.printDebug("The query result is : " + pushResultString);  
		JOptionPane.showMessageDialog(null, pushResultString.substring(38) , "OPORD Push Message",JOptionPane.INFORMATION_MESSAGE);
	}

} //End of class Opord
