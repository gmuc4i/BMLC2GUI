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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Scanner;

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
 * Report Support Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	12/1/2011
 */ 
public class Report09 {
	
	String root="";
  BMLC2GUI bml = BMLC2GUI.bml;
	
	/**
	 * Create a new IBML09 Report (XML Document)
	 * 
	 * @mababneh
	 */
	void newReport(String reportType) {	
		releaseXUICache();
		String schemaLocation = "";
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
    BMLC2GUI.reportBMLType = "IBML";
		
		//set xsdUrl and xuiUrl
    bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + BMLC2GUI.ibml09ReportSchemaLocation);
		root = bml.setUrls(reportType);	
        
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.initDom(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      root);
	}
	
	
	
	/**
	 * Open an existing IBML09 Report (XML Document) from The File System
	 * 
	 * @since	
	 */
	void openReportFS_General09() throws IOException {		
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
    BMLC2GUI.reportBMLType = "IBML";
		bml.bmlDocumentType = "GeneralStatusReport";	
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + BMLC2GUI.ibml09ReportSchemaLocation);//Schema File XSD

    JFileChooser xmlFc = new JFileChooser(BMLC2GUI.guiFolderLocation + "//");//XML file
		xmlFc.setDialogTitle("Enter the Report XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;

    bml.xmlUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		bml.xuiUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.xuiFolderLocation + "/GeneralStatusReportView09.xui");// Jaxfront XUI file
		bml.root = "BMLReport";//debugx "BMLReport");
		
    //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      "GeneralStatusReport",
      (new String[]{"UnitID","Hostility"}),
      (new String[]{"Latitude","Longitude"}),
      bml.ibmlns,
      null
    );
	}
	
	/**
	 * Open an existing IBML09 Report (XML Document) from The File System
	 * 
	 * @since	
	 */
	void openReportFS_Task09() throws IOException {		
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
    BMLC2GUI.reportBMLType = "IBML";
		bml.bmlDocumentType = "TaskStatusReport";	
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + BMLC2GUI.ibml09ReportSchemaLocation);//Schema File XSD
		JFileChooser xmlFc = new JFileChooser(BMLC2GUI.guiFolderLocation + "//");//XML file
		xmlFc.setDialogTitle("Enter the Report XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;
		bml.xmlUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = 
      URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		bml.xuiUrl = // Jaxfront XUI file
      URLHelper.getUserURL(BMLC2GUI.xuiFolderLocation + "/TaskStatusReportView09.xui");
		bml.root = "BMLReport";	
		
    // Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      "GeneralStatusReport",
      (new String[]{"UnitID","Hostility"}),
      (new String[]{"Latitude","Longitude"}),
      bml.ibmlns,
      null
    );
	}
	/**
	 * Open an existing IBML09 Report (XML Document) from The File System
	 * 
	 * @since	10/28/2010
	 */
	private void openReportFSDemo(String fileString) throws IOException {
		releaseXUICache();
    String reportString ="";
    String reportType ="";
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
    BMLC2GUI.reportBMLType = "IBML";

	  // XML file		
    bml.xmlUrl = URLHelper.getUserURL(fileString);
    File reportFile = new File(fileString);
    Scanner reportpullScanner = new Scanner(reportFile);
        
    // Build the editor using the w3c Dom document instead of the XML file
    bml.w3cBmlDom = SBMLSubscriber.listenerDocument;
        
    while (reportpullScanner.hasNext()){
      reportString = reportpullScanner.next();
      reportType = bml.getBmlDocumentType(reportString);
			if(reportType != "UNKNOWN"){break;}
    }  
    bml.bmlDocumentType = reportType;
             
    //Step 6 the report type now is known
    bml.printDebug("======== Report Type is  : " + reportType);
    bml.documentTypeLabel.setText(reportType);
               
    // Step 7 : Display the Report Document  
		//set xsdUrl and xuiUrl
		bml.root = bml.setUrls(reportType);
           
    //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      "GeneralStatusReport",
      (new String[]{"UnitID","Hostility"}),
      (new String[]{"Latitude","Longitude"}),
      bml.ibmlns,
      null
    );
	}
	
	/**
	 * Open an existing IBML09 Report (XML Document) from The File System
	 * 
	 * Used for demonstrations
	 * 
	 * @since	10/28/2010
	 */
	void openReportDemo() throws IOException{
		long x, y = 0;	
		String fileStringDemo = 
      bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\BRIDGEREP.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000;		// 	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  // Do Nothing		//Just wait

		fileStringDemo = 
      bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\BRIDGEREP2.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000; 		//	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  // Do Nothing//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\BRIDGEREP3.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000;		//	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  	// Do Nothing	//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\BRIDGEREP4.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);	
	}

	/**
	 * Send an Edited (optionally validated) IBML09 Report 
   * (XML Document) to the Web Services through the SBML Client
	 * 
	 * @since	1/30/2010
	 */
	void pushReport09() {
		String pushResultString ="";	// String to hold the result of the execution of the SBML XML query
		String pushReportInputString ="";	// String to hold the input to the SBML XML query
    BMLC2GUI.sbmlOrderDomainName = "IBML";
    BMLC2GUI.generalBMLFunction = "IBML";
    BMLC2GUI.reportBMLType = "IBML";
 
    if(bml.currentDom == null)
      pushReportInputString = bml.currentXmlString;
    else {
  		try {
		  	// assign the text of the XML document to pushOrderInputString
			  pushReportInputString = bml.currentDom.serialize().toString();
		  } catch (ValidationException e) {
			  e.printStackTrace();
		  }		
    }

    // hsck by JMP 2Aug2013 to remove extraneous stuff (including
    // unmatched quote) occurring in root-level element
    int hackIndex = pushReportInputString.indexOf("IBMLReports.xsd\"");
    if(hackIndex > -1)
    {
      String part1 = pushReportInputString.substring(0,hackIndex);
      int hackOutLength = ("IBMLReports.xsd\"").length();
      String part2 = pushReportInputString.substring(hackIndex+hackOutLength);
      pushReportInputString = part1 + part2;
    }

    //Running the SBML Query through the SBMLClient
		pushResultString = 
      BMLC2GUI.ws.processBML(
        pushReportInputString, 
        BMLC2GUI.sbmlOrderDomainName, 
        BMLC2GUI.sbmlOrderDomainName, 
        "IBML Report Push");
		bml.printDebug("The query result is : " + pushResultString);  
		JOptionPane.showMessageDialog(
      null, 
      pushResultString.substring(38) , 
      "Report Push Message",
      JOptionPane.INFORMATION_MESSAGE);
	
  } // end pushReport()

	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
} // End of Report Class
