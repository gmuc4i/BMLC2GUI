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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import sun.misc.Regexp;

import com.jaxfront.core.dom.DOMBuilder;
import com.jaxfront.core.type.Type;
import com.jaxfront.core.util.URLHelper;
import com.jaxfront.core.util.io.cache.XUICache;

import edu.gmu.c4i.sbmlclientlib.SBMLClient;
import edu.gmu.c4i.sbml.Exception_Exception;


/**
 * Web Services Support Methods
 *
 * These methods support the BMLC2GUI object
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	5/11/2011
 * 
 * @author	Eric Popelka, C4I Center, George Mason University
 * @since	6/29/2011
 */ 
public class webservices {
	BMLC2GUI bml;
	String root;
	private static final String xmlPreamble = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  
  public webservices(BMLC2GUI bmlRef)
  {
    bml = bmlRef;
  }

	/**
	 * Clears cache
	 */
	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}
	
	/**
	 * Open an existing BML Order (XML Document) from a web service
	 */
	void openOrderWS() throws IOException {	
		String root = null;
		String orderID = "";
		bml.bmlDocumentType = "Order";	
		bml.documentTypeLabel.setText(bml.bmlDocumentType);	
		releaseXUICache();
		
	    // pull the order of a given ReportID
	    orderID = JOptionPane.showInputDialog(null, "Enter Order ID ", "Open an Order from Web Service",JOptionPane.QUESTION_MESSAGE);
    	File orderPullResultFile = new File(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\orderpullresult.xml");
    	PrintWriter orderPullPWXml = new PrintWriter(orderPullResultFile);
 
    	// The result of the query returned by the SBMLClient
    	String orderPullResultString = "";
 
    	//The SBML Query Syntax string
    	String xmlString = xmlPreamble + "<bml:OrderPullIBML xmlns:bml=\"http://netlab.gmu.edu/IBML\"><bml:OrderID>" + orderID + "</bml:OrderID></bml:OrderPullIBML>"; 
    	    	
    	String orderResultPullString = processBML(xmlString, BMLC2GUI.sbmlOrderDomainName, 
    			BMLC2GUI.orderBMLType, "Pull Order");
    	
    	if (orderPullResultString.contains("Error")){
    		JOptionPane.showMessageDialog(null, orderPullResultString.substring(38) , "Pull Message",JOptionPane.INFORMATION_MESSAGE);
    	}
    	
      // Write to the orderpullresult.xml
      orderPullPWXml.println(orderPullResultString);
      orderPullPWXml.close();
    	BMLC2GUI.printDebug("Finished creating the orderpullresult.xml file");
 
    	// Input from Web Service is valid 
      if (orderPullResultString.length() == 0){
        JOptionPane.showMessageDialog(null, "No Order was returned from the server");
      }
      else
      { // Display the Report Document      
        root = "OrderPushIBML";
    		bml.xsdUrl = URLHelper.getUserURL(bml.schemaFolderLocation + "/Orders/IBMLOrderPushPulls.xsd");        //Schema File XSD
    		bml.xuiUrl = URLHelper.getUserURL(bml.xuiFolderLocation + "/TabStyleOrder.xui");    // Jaxfront XUI file
        bml.xmlUrl = URLHelper.getUserURL(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\orderpullresult.xml");
       
        // Generate the swing GUI
    		bml.drawFromXML(
          "default-context", 
          bml.xsdUrl, 
          bml.xmlUrl, 
          bml.xuiUrl, 
          root, 
          bml.bmlDocumentType,
          null,
          null,
          null,
          bml.ibmlns,
          null
        );
      }// end of else // Valid web service input
	}

	/**
 	* Open a report coming through the Web Service Subscriber /Listener
 	*
 	* @since	3/17/2010
        * 11/24/2014
 	*/
	void openReportSub() throws IOException {
		releaseXUICache();
		String reportString ="";
		String reportType ="";

		// XML file		
		bml.xmlUrl = URLHelper.getUserURL(bml.guiFolderLocation + "\\SubscriberDOM_IBML.xml");
		File reportFile = new File(bml.guiFolderLocation + "\\SubscriberDOM_IBML.xml");//\\SBMLCLIENT\\dist\\SubscriberDOM.xml");
		Scanner reportpullScanner = new Scanner(reportFile);
    
		// Build the editor using the w3c Dom document instead of the XML file
		bml.w3cBmlDom = SBMLSubscriber.listenerDocument;
    
		while (reportpullScanner.hasNext()){
			reportString = reportpullScanner.next();
                        BMLC2GUI.printDebug("======== Report String is  : " + reportString);
        	
			if(reportType != "UNKNOWN"){break;}
		}
                
                reportType = bml.getBmlDocumentType(reportString);
                
                BMLC2GUI.printDebug("======== Report String is  : " + reportString);
                
            
                        String subString;
			// convert the csv string to an array of strings
                        String [] subStringArray;
			subStringArray = reportString.split(",");
                        BMLC2GUI.printDebug("======== Report String length  = " + subStringArray.length);
			for (int i=0; i < subStringArray.length;i++){
				BMLC2GUI.printDebug("XML Document String [ "+  i  + " ] = " + subStringArray[i] );
			}

			// call method that starts the drawing process of the location information 
			bml.drawLocation(subStringArray , reportType);
                        
		bml.bmlDocumentType = reportType;
    
		//Step 6 the report type now is known
		BMLC2GUI.printDebug("======== Report Type is  : " + reportType);
		bml.documentTypeLabel.setText(reportType);

		// Step 7 : Display the Report Document
		//set xsdUrl and xuiUrl
		root = bml.setUrls(reportType);
		
        //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      root, 
      bml.bmlDocumentType,
      null,
      null,
      null,
      bml.ibmlns,
      null
    );
	}

	/**
	 * Open an existing BML Report (XML Document) from the web services
	 */
	void openReportWS(String reportIDString) throws IOException {	
		String root = null;
		String reportType =""; // report type 
		String reportID ;
		String reportPullString ="";	
		reportID = reportIDString;	
		releaseXUICache();
	
		//create reportpullresult file
		File reportPullResultFile = new File(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\reportpullresult.xml");
		PrintWriter reportPullPWXml = new PrintWriter(reportPullResultFile);

		// Running the SBML Query through the SBMLClient  
		// The SBML Query Syntax string
		String xmlString = "<ReportPull><ReportID>" + reportID + "</ReportID></ReportPull>"; 

		//SBMLClient - Secure or non-secure depending on library implementation   
		SBMLClient sbmlClient = null;
		try {
			sbmlClient = new SBMLClient(bml.sbmlOrderServerName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}	
		try {
			BMLC2GUI.printDebug("Starting the Web Service query ");
	
			// call the callSBML method to execute the query	
			reportPullString = sbmlClient.sbmlProcessBML(xmlString, bml.sbmlReportPullDomainName,bml.reportBMLType);	
		} catch (Exception_Exception e2) {
			System.err.println("The query execution was unsuccessful....... ");		
		}
 
		BMLC2GUI.printDebug("The report pull query result is : " + reportPullString);  	

		//Write to the reportpullresult.xml
		reportPullPWXml.println(reportPullString);
		reportPullPWXml.close();
		BMLC2GUI.printDebug("Finished creating the reportpullresult.xml file");
        
		//reportPullResultFile -  ReportPullResult.xml
		Scanner reportpullScanner = new Scanner(reportPullResultFile);
		String reportString ="";
		
		while (reportpullScanner.hasNext()){
			reportString = reportpullScanner.next();
        	reportType = bml.getBmlDocumentType(reportString);
			if(reportType != "UNKNOWN"){break;}
		}
		bml.bmlDocumentType = reportType;
     
                      
		//the report type is now known
          
		BMLC2GUI.printDebug("======== Report Type is  : " + reportType);
		bml.documentTypeLabel.setText(reportType);

		//set xsdUrl and xuiUrl
		root = bml.setUrls(reportType);
		
		bml.xmlUrl = URLHelper.getUserURL(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\ReportPullResult.xml");
    	
        //Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      root, 
      bml.bmlDocumentType,
      null,
      null,
      null,
      null,
      null
    );
	}
	
	/**
	 * Open an existing BML Report (XML Document) from the web services
	 */
	void openReportSub(org.w3c.dom.Document subscriberDocument) throws IOException {
		String root = null;
		String reportType =""; // report type 
		String reportID ;
		String reportPullString ="";
		releaseXUICache();
		String subscriberDocumentType ="";
		subscriberDocumentType ="GeneralStatusReport";
                reportType = "GeneralStatusReport";
		
                // print the document/xml message
                System.out.print("**************Subscriber document is  : " );
                
                //BMLC2GUI.printDebug( subscriberDocument.toString());
                  
                  //the report type now is known
		System.out.print("***************Report Type is  : " );
                System.out.print( reportType);
                
		bml.documentTypeLabel.setText(reportType);
    
		// Step 7 : Display the Report Document
		String schemaLocation = bml.schemaFolderLocation + "/Reports/";
     		
		// reading the whole DOM values of the XML file into a csv string
		bml.bmlString = bml.currentDom.getRootType().getDisplayValue(); 
		BMLC2GUI.printDebug("===============================XML Document String : "+ bml.bmlString);
                        
    //Generate the swing GUI using the org.w3c.dom.Document instead of an XML File
		try { //can not call drawFromXML because build is different
			bml.currentDom = 
        DOMBuilder.getInstance().build(
          "default-context", 
          bml.xsdUrl, 
          subscriberDocument, 
          bml.xuiUrl, 
          root);
			bml.currentDom.getGlobalDefinition().setIsUsingButtonBar(false);
			bml.currentDom.getGlobalDefinition().setIsUsingStatusBar(true);
			bml.currentDom.getGlobalDefinition().setLanguage(bml.currentLanguage);
                         
      if (bml.editor != null) bml.editor.selectNode((Type) null);
			bml.visualizeBmlDom();          
                        
			// reading the whole DOM values of the XML file into a csv string
			bml.bmlString = bml.currentDom.getRootType().getDisplayValue();
			BMLC2GUI.printDebug("XML Document String : "+ bml.bmlString);
		
			// convert the csv string to an array of strings
			bml.bmlStringArray = bml.bmlString.split(",");
			for (int i=0; i < bml.bmlStringArray.length;i++){
				BMLC2GUI.printDebug("XML Document String [ "+  i  + " ] = " + bml.bmlStringArray[i] );
			}

			// call method that starts the drawing process of the location information 
			bml.drawLocation(bml.bmlStringArray , reportType);
	
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Report Error ", "Couldn't Create Report ",JOptionPane.ERROR_MESSAGE);
		} // End of Catch
	}

	/**
	 * Open an existing IBML_CBML Order (XML Document) from a web service
	 *
	 * @since	5/11/2011
	 */
	void openOPORD_IBML_CBML_WS() throws IOException {
		String root = bml.opord_C2CoreRoot; //null;	
		String orderID = "";
		bml.bmlDocumentType = "OPORD";//IBML_CBMLOrder"; // Temp for array method of location graphics - "OPORD"; 5/13/2011	
		bml.documentTypeLabel.setText(bml.bmlDocumentType);	
		releaseXUICache();
	
		// pull the order of a given ReportID
		orderID = JOptionPane.showInputDialog(null, "Enter OPORD Order ID ", "Open an OPORD Order from Web Service",JOptionPane.QUESTION_MESSAGE);
		File orderPullResultFile = new File(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\orderpullresult.xml");
		PrintWriter orderPullPWXml = new PrintWriter(orderPullResultFile);

		// The result of the query returned by the SBMLClient
		String orderPullResultString = "";

		//Running the SBML Query through the SBMLClient

		// Update 11May2011 - because of use C_BML instead of GMU_CBML (CBML Lite)
		// The SBML Query Syntax string  
		//String xmlString = "<C_BML:OperationsOrderPull xmlns:C_BML="+'"'+
		//"urn:sisostds:bml:coalition:draft:cbml:1"+'"'+" ><C_BML:OrderID>" + orderID + 
		//"</C_BML:OrderID></C_BML:OperationsOrderPull>"; 
	    	
		String xmlString = "<bml:OrderPull" +
			" xmlns:jc3iedm-us=" + '"' + "urn:int:nato:standard:mip:jc3iedm:3.1a:oo:2.0" +'"'+
			" xmlns:C_BML=" +'"' + "http://www.sisostds.org/schemas/c-bml/1.0" + '"' +
			" xmlns:jc3iedm=" +'"' + "urn:int:nato:standard:mip:jc3iedm:3.0.2:oo:2.2" +'"' +
			" xmlns:bml=" +'"' + "http://netlab.gmu.edu/IBML" +'"' + 
			" xmlns:newwho=" +'"' + "http://netlab.gmu.edu/JBML/BML" + '"' + ">" +
			"<bml:OrderID>" + orderID + "</bml:OrderID>" + 
			"</bml:OrderPull>";

		//SBMLClient - Secure or non-secure depending on library implementation	   
		SBMLClient sbmlClient = null;
		try {
			sbmlClient = new SBMLClient(bml.sbmlOrderServerName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}	
		try {
			BMLC2GUI.printDebug("Starting the Web Service query ");
	
			// call the callSBML method to execute the query
			orderPullResultString = sbmlClient.sbmlProcessBML(xmlString, bml.sbmlOrderDomainName,bml.orderBMLType);		
		} catch (Exception_Exception e2) {
			BMLC2GUI.printDebug("The query execution was unsuccessful....... ");		
		}	
		BMLC2GUI.printDebug("The query result is : " + orderPullResultString);  	

		// Write to the orderpullresult.xml 
		orderPullPWXml.println(orderPullResultString);
		orderPullPWXml.close();
		BMLC2GUI.printDebug("Finished creating the orderpullresult.xml file");

		// Input from Web Service is valid 
		if (orderPullResultString.length() == 0 || orderPullResultString.contains("Error")){
			JOptionPane.showMessageDialog(null, orderPullResultString.substring(38) , "Pull Error Message",JOptionPane.INFORMATION_MESSAGE);
		}      
		else{

      // Display the Report Document
      root = bml.opord_C2CoreRoot;//"OrderPush";
      bml.xsdUrl = URLHelper.getUserURL(bml.opord_C2CoreSchemaFile);           	//Schema File XSD
      bml.xuiUrl = URLHelper.getUserURL(bml.opord_C2CoreXUIFile);           	// Jaxfront XUI file
      bml.xmlUrl = URLHelper.getUserURL(bml.guiFolderLocation + "\\SBMLCLIENT\\dist\\orderpullresult.xml");
           	
      // Generate the swing GUI
      bml.drawFromXML(
        null, 
        bml.xsdUrl, 
        bml.xmlUrl, 
        bml.xuiUrl, 
        root, 
        bml.bmlDocumentType,
        null,
        null,
        null,
        null,
        null
      );
      
    }// end else
	}
	
	public String processBML(String xml, String domain, String bmlType, 
			String bmlDescription) {
		
		// Sanitize the input
		Pattern p = Pattern.compile("<\\?\\s*jaxfront\\s*.*?>\r?\n?");
		xml = p.matcher(xml).replaceFirst("");
		String result = "Error";
                
		if (BMLC2GUI.serverType.getIsREST()) {

			BMLClientREST_Lib sbmlClient = null;
			try {
				sbmlClient = new BMLClientREST_Lib();
				sbmlClient.setHost(BMLC2GUI.sbmlOrderServerName);
				sbmlClient.setPort("8080");
				sbmlClient.setRequestor("BMLC2GUI");
				sbmlClient.setDomain(domain);
				//sbmlClient.setValidate("false");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				BMLC2GUI.printDebug("Starting the bmlRequest Web Service query ");

				// call the callSBML method to execute the query
				result = sbmlClient.bmlRequest(xml);
			} catch (Exception e2) {
				System.err
						.println("The bmlRequest query execution was unsuccessful....... ");
				JOptionPane.showMessageDialog(null, result, bmlDescription,
						JOptionPane.INFORMATION_MESSAGE);
			}
			BMLC2GUI.printDebug("The bmlRequest query result is : " + result);
      } else {
      
			// Running the SBML Query through the SBMLClient
			// SBMLClient - Secure or non-secure depending on library
			// implementation
			SBMLClient sbmlClient = null;
			try {
				sbmlClient = new SBMLClient(BMLC2GUI.bml.sbmlOrderServerName);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			try {
				BMLC2GUI.printDebug("Starting the sbmlProcessBML Web Service query ");

				// call the callSBML method to execute the query
				result = sbmlClient.sbmlProcessBML(xml, domain, bmlType);
			} catch (Exception_Exception e2) {
				System.out
						.println("The sbmlProcessBML query execution was unsuccessful....... ");
				JOptionPane.showMessageDialog(null, result, bmlDescription,
						JOptionPane.INFORMATION_MESSAGE);
			}

			BMLC2GUI.printDebug("The sbmlProcessBML query result is : " + result);
		}
		return result;
	}
} // End of webservice class
