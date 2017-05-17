/*----------------------------------------------------------------*
|   Copyright 2009-2017 Networking and Simulation Laboratory      |
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

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.jaxfront.core.dom.DOMBuilder;
import com.jaxfront.core.schema.ValidationException;
import com.jaxfront.core.type.Type;
import com.jaxfront.core.util.URLHelper;
import com.jaxfront.core.util.io.cache.XUICache;

import edu.gmu.c4i.sbml.Exception_Exception;
import edu.gmu.c4i.sbmlclientlib.SBMLClient;

/**
 * Order Support Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 *              repurposed to CBML by JMP 2Aug2013
 * @since	02/03/2010
 */ 
public class Order {
	private String root = null;
	private String bmlDocumentType = "";
  BMLC2GUI bml = BMLC2GUI.bml;
	
	/**
	 * Create a new BML Order (XML Document)
	 * 
	 * @since	4/17/2009
	 */
	void newOrder() {
		bml.root = null;
		bmlDocumentType = "CBML Light Order";
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
		bml.documentTypeLabel.setText(bmlDocumentType);
		releaseXUICache();

		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + BMLC2GUI.cbmlOrderSchemaLocation);
		bml.xmlUrl = null;		//Empty XML
		bml.xuiUrl = 
      URLHelper.getUserURL(
        bml.xuiFolderLocation + "/TabStyleOrder.xui"); 		// XUI Style
		bml.initDom(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root);
	}
	
	/**
	 * Clears cache
	 */
	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
	
	/**
	 * Open an existing BML Order (XML Document) From the file System
	 *
	 * @deprecated	Only for testing old schema.  To be removed when everything is OK.
	 * @since	02/03/2010
	 */
	void openOrder() {
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
		bmlDocumentType = "CBML Light Order";
		bml.documentTypeLabel.setText(bmlDocumentType);
		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + BMLC2GUI.cbmlOrderSchemaLocation);//Schema File XSD
		bml.xmlUrl = 
      URLHelper.getUserURL(BMLC2GUI.guiFolderLocation + "/BMLC2GUI/CBML_Order.xml");		//XML file
		bml.xuiUrl = 
      URLHelper.getUserURL(
        bml.xuiFolderLocation + "/TabStyleOrder.xui");		// Jaxfront XUI file
		root = "CBMLOrder";
		
    // Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      root, 
      bmlDocumentType,
      "Task",
      (new String[]{
        "OID",
        "PointLight",
        "Line",
        "Surface",
        "CorridorArea",
        "TaskeeWhoRef",
        "DateTime"}),
      (new String[]{
        "Latitude",
        "Longitude"}),
      bml.cbmlns,
      null
    );
	}

	/**
	 * Open an existing BML Order (XML Document) from the file System
	 * 
	 * @since	02/03/2010
	 */
	void openOrderFS() {
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
		bml.bmlDocumentType = "CBML Light Order";
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
    String xsdFileLocation = 
      bml.guiFolderLocation + BMLC2GUI.cbmlOrderSchemaLocation;	
		bml.xsdUrl = URLHelper.getUserURL(xsdFileLocation);
		JFileChooser xmlFc = new JFileChooser(bml.guiFolderLocation + "//");//XML file
		xmlFc.setDialogTitle("Enter the Order XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;
		bml.xmlUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = 
      xmlFc.getSelectedFile().toString() + "(tmp)";
		bml.xuiUrl = 
      URLHelper.getUserURL(
        bml.xuiFolderLocation + "/TabStyleOrder.xui");// Jaxfront XUI file
		bml.root = "CBMLOrder";
    
    // Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      "Task",
      (new String[]{
        "TaskeeWhoRef",
        "Datetime",
        "OID",
        "PointLight",
        "Line",
        "Surface",
        "CorridorArea"}),
      (new String[]{
        "Latitude",
        "Longitude"}),
      bml.cbmlns,
      null
    );
	}
	
	/**
	 * Send an Edited (optionally validated) BML Order (XML Document) to the Web Services through the SBML Client
	 * 
	 * @since	10/25/2009
	 */
	void pushOrder() {		
		String pushResultString ="";		// String to hold the result of the execution of the SBML XML query
		String pushOrderInputString ="";// String to hold the input to the SBML XML query
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";

//		try {
//			// assign the text of the XML document to pushOrderInputString
//			pushOrderInputString = bml.currentDom.serialize().toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    
    pushOrderInputString="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<cbml:CBMLOrder xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
" xsi:schemaLocation=\"http://www.sisostds.org/schemas/c-bml/1.0 /Users/jmarkpullen/Desktop/SandboxTech/workingGUI/BMLC2GUI/IITSEC/Schema/C_BML/OPORD/CBML_Order.xsd\"\n" +
" xmlns=\"http://www.sisostds.org/schemas/c-bml/1.0\"\n" +
" xmlns:cbml=\"http://www.sisostds.org/schemas/c-bml/1.0\"></cbml:CBMLOrder>";
    System.out.println("XML:"+pushOrderInputString);//debugx

    // hack by JMP 2Aug2013 to remove extraneous stuff (including
    // unmatched quote) occurring in root-level element
    int hackIndex = pushOrderInputString.indexOf("CBML_Order.xsd\"");
    if(hackIndex > -1)
    {
      String part1 = pushOrderInputString.substring(0,hackIndex);
      int hackOutLength = ("CBML_Order.xsd\"").length();
      String part2 = pushOrderInputString.substring(hackIndex+hackOutLength);
      pushOrderInputString = part1 + part2;
    }
    
    // Running the SBML Query through the SBMLClient
		pushResultString = 
      BMLC2GUI.ws.processBML(
        pushOrderInputString, 
        BMLC2GUI.sbmlOrderDomainName, 
        BMLC2GUI.sbmlOrderDomainName, 
        "IBML Order Push");
    BMLC2GUI.printDebug("The CBML Order push result is : " + pushResultString);
    JOptionPane.showMessageDialog(
      null, 
      pushResultString , 
      "Order Push Message",
      JOptionPane.INFORMATION_MESSAGE);
    
  }// end pushOrder()
}//End of class Order
