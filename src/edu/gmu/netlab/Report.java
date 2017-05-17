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
 * @since	10/28/2010
 */ 
public class Report {
	
	String root="";
  BMLC2GUI bml = BMLC2GUI.bml;
	
	/**
	 * Create a new BML Report (XML Document)
	 * 
	 * @since	10/17/2009
	 */
	void newReport(String reportType) {	
		releaseXUICache();
		String schemaLocation = "";
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";
		
		//set xsdUrl and xuiUrl
		root = bml.setUrls(reportType);	
        
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.initDom("default-context", bml.xsdUrl, bml.xmlUrl, bml.xuiUrl, root);
	}
	
	/**
	 * Open an existing CBML Report (XML Document) from The File System
	 * 
	 * @deprecated	Uses the old schema testing. To be removed when done
	 * @since	10/15/2009
	 */
	void openReport() {	
		releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";
		bml.documentTypeLabel.setText("General Status Report");

		//XML file
		bml.xsdUrl = 
      URLHelper.getUserURL(
        bml.guiFolderLocation + bml.cbmlReportSchemaLocation);
		bml.xmlUrl = 
      URLHelper.getUserURL(bml.guiFolderLocation + "/BMLC2GUI/CBML_Reports.xml");
		bml.xuiUrl = 
      URLHelper.getUserURL(bml.xuiFolderLocation + "/TaskStatusReportView09.xui");
		bml.bmlDocumentType = "GeneralStatusReport";
		root = "MultipleReportPull";
		
    //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      root, 
      bml.bmlDocumentType,
      "GeneralStatusReport",
      (new String[]{"UnitID","Hostility"}),
      (new String[]{"Latitude","Longitude"}),
      bml.cbmlns,
      null
    );
	}
	
	/**
	 * Open an existing CBML Report (XML Document) from The File System
	 * 
	 * @since	02/03/2009
	 */
	void openReportFS() throws IOException {		
		releaseXUICache();
    String reportString ="";
    String reportType ="";
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";
		bml.bmlDocumentType = "GeneralStatusReport";
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.xsdUrl = 
      URLHelper.getUserURL(
        bml.guiFolderLocation + bml.cbmlReportSchemaLocation);

		// XML file
		JFileChooser xmlFc = new JFileChooser(bml.guiFolderLocation + "//");
		xmlFc.setDialogTitle("Open CBML Report XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;            
		bml.xmlUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
		bml.xuiUrl = 
        URLHelper.getUserURL(BMLC2GUI.xuiFolderLocation + 
          "/GeneralStatusReportView09.xui");		// Jaxfront XUI file
		bml.root = "CBMLReport";

    // determine report type by scanning XML file
    File reportFile = new File(bml.xmlUrl.getFile());
    Scanner reportpullScanner = new Scanner(reportFile);
    while (reportpullScanner.hasNext()){
      reportString = reportpullScanner.next();
      reportType = bml.getBmlDocumentType(reportString);
      if(reportType != "UNKNOWN"){break;}
    }
    bml.bmlDocumentType = reportType;
 
    //Step 6 the report type now is known
    BMLC2GUI.printDebug("======== Report Type is  : " + reportType);
    bml.documentTypeLabel.setText(reportType);
        
    // Step 7 : Display the Report Document
		// set xsdUrl and xuiUrl
		// debugx bml.root = bml.setUrls(reportType);
    
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
      bml.cbmlns,
      null
    );			
	}
	
	/**
	 * Open an existing BML Report (XML Document) from The File System
	 * 
	 * @since	10/28/2010
	 */
	void openReportFSDemo(String fileString) throws IOException {
		releaseXUICache();
    String reportString ="";
    String reportType ="";
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";

		//XML file		
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
    BMLC2GUI.printDebug("======== Report Type is  : " + reportType);
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
      bml.cbmlns,
      null
    );
	}
	
	/**
	 * Open an existing BML Report (XML Document) from The File System
	 * 
	 * Used for demonstrations
	 * 
	 * @since	10/28/2010
	 */
	void openReportDemo() throws IOException{
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";
		long x, y = 0;	
		String fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\GeneralStatusReport1.xml"; //BRIDGEREP1		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000;		// 	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  // Do Nothing		//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\GeneralStatusReport2.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000; 		//	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  // Do Nothing//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\GeneralStatusReport3.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);

		x = System.currentTimeMillis();
		y = x + 1000;		//	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  	// Do Nothing	//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\GeneralStatusReport4.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);	
		
		x = System.currentTimeMillis();
		y = x + 1000;		//	Wait for 5 seconds	
		while (System.currentTimeMillis() < y ){}  	// Do Nothing	//Just wait

		fileStringDemo = bml.guiFolderLocation + "\\Samples\\Reports\\Demo\\GeneralStatusReport5.xml"; //GeneralStatusReport1
		openReportFSDemo(fileStringDemo);	
	}

	/**
	 * Send an Edited (optionally validated) BML Report (XML Document) to the Web Services through the SBML Client
	 * 
	 * @since	1/30/2010
	 */
	void pushReport() {
		String pushResultString ="";	// String to hold the result of the execution of the SBML XML query
		String pushReportInputString ="";	// String to hold the input to the SBML XML query
    BMLC2GUI.sbmlOrderDomainName = "CBML";
    BMLC2GUI.generalBMLFunction = "CBML";
    BMLC2GUI.reportBMLType = "CBML";

		try {
			// assign the text of the XML document to pushOrderInputString
			pushReportInputString = bml.currentDom.serialize().toString();
		} catch (ValidationException e) {
			e.printStackTrace();
		}			

    //Running the SBML Query through the SBMLClient
		pushResultString = 
      BMLC2GUI.ws.processBML(
        pushReportInputString, 
        BMLC2GUI.sbmlOrderDomainName, 
        BMLC2GUI.sbmlOrderDomainName, 
        "IBML Report Push");

		BMLC2GUI.printDebug("The query result is : " + pushResultString);  
		JOptionPane.showMessageDialog(
      null, 
      pushResultString.substring(38), 
      "Report Push Message",
      JOptionPane.INFORMATION_MESSAGE);
    
	} // end pushReport

	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
} // End of Report Class
