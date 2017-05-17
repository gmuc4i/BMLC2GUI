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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.jaxfront.core.schema.ValidationException;
import com.jaxfront.core.util.URLHelper;
import com.jaxfront.core.util.io.cache.XUICache;
import java.io.*;

/**
 * Order09  Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	11/28/2011
 */ 
public class Order09 {
	private String bmlDocumentType = "";
  BMLC2GUI bml = BMLC2GUI.bml;
	
	/**
	 * Create a new IBML09 Order (XML Document)
	 * 
	 * mababneh 11/28/2011
	 */
	void newOrder09() {
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
		bml.root = "OrderPushIBML";
		bmlDocumentType = "IBML Order09";
		bml.documentTypeLabel.setText(bmlDocumentType);
		releaseXUICache();
		
		// Order Schema C://BMLC2GUI//IBML-MSG048-09//IBMLOrderPushPulls.xsd
		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.xuiFolderLocation + 
          BMLC2GUI.ibml09OrderSchemaLocation);
		bml.xmlUrl = null;		//Empty XML
		bml.xuiUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.xuiFolderLocation + 
          "/TabStyleOrder09.xui"); 		// XUI Style
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
	 * Open an existing IBML09 Order (XML Document)
	 * 
	 * mababneh 11/28/2011
	 */
	void openOrderFS09() {
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
		bml.bmlDocumentType = "IBML Order09";	
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.xsdUrl = //Schema File XSD
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + 
          BMLC2GUI.ibml09OrderSchemaLocation);	
		JFileChooser xmlFc = //XML file
      new JFileChooser(BMLC2GUI.guiFolderLocation + "//");	
		xmlFc.setDialogTitle("Enter the Order XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;
		bml.xmlUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		bml.xuiUrl = // Jaxfront XUI file
      URLHelper.getUserURL(BMLC2GUI.xuiFolderLocation + "/TabStyleOrder09.xui");
		bml.root = "OrderPushIBML";
		
    //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      "GroundTask",
      (new String[]{
        "WhereClass",
        "WhereCategory",
        "WhereLabel",
        "WhereQualifier",
        "TaskeeWho",
        "DateTime"}),
      (new String[]{
        "Latitude",
        "Longitude"}),
      bml.ibmlns,
      null
    );
	}
	
	/**
	 * Push an IBML09 Order (XML Document)
	 * 
	 * mababneh 11/28/2011
	 */
	void pushOrder09() {		
		String pushResultString ="";		// String to hold the result of the execution of the SBML XML query
		String pushOrderInputString ="";		// String to hold the input to the SBML XML query
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";

//		try {
//			// assign the text of the XML document to pushOrderInputString
//			pushOrderInputString = bml.currentDom.serialize().toString();
//		} catch (ValidationException e) {
//			e.printStackTrace();
//debugx		}		
    ///debugx should push BML from memory...
    FileReader xmlFile;
    try{
      xmlFile=new FileReader(new File(bml.xmlUrl.getFile()));
      int charBuf; 
      while((charBuf = xmlFile.read())>0) {
        pushOrderInputString += (char)charBuf;
      }
    }
    catch(Exception e) {
      System.err.println("Exception in reading XML file:"+e);
      e.printStackTrace();
      return;
    }
    BMLC2GUI.printDebug("***PUSHXML:"+pushOrderInputString);

    // hack by JMP 2Aug2013 to remove extraneous stuff (including
    // unmatched quote) occurring in root-level element
    int hackIndex = pushOrderInputString.indexOf("IBMLOrderPushPulls.xsd\"");
    if(hackIndex > -1)
    {
      String part1 = pushOrderInputString.substring(0,hackIndex);
      int hackOutLength = ("IBMLOrderPushPulls.xsd\"").length();
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
    BMLC2GUI.printDebug("The IBML Order push result is : " + pushResultString);
    JOptionPane.showMessageDialog(
      null, 
      pushResultString , 
      "Order Push Message",
      JOptionPane.INFORMATION_MESSAGE);
    
	}// end pushOrder09()
}//End of class Order09
