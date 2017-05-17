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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * package edu.gmu.c4i.sbmlsubscriber;
 */

package edu.gmu.netlab;

import com.jaxfront.core.util.URLHelper;
import static edu.gmu.netlab.BMLC2GUI.orderBMLType;
import static edu.gmu.netlab.BMLC2GUI.reportBMLType;
import static edu.gmu.netlab.BMLC2GUI.guiFolderLocation;
import static edu.gmu.netlab.BMLC2GUI.guiLocationXML;
import static edu.gmu.netlab.BMLC2GUI.initMapLat;
import static edu.gmu.netlab.BMLC2GUI.initMapLon;
import static edu.gmu.netlab.BMLC2GUI.latTag;
import static edu.gmu.netlab.BMLC2GUI.latlonParentTag;
import static edu.gmu.netlab.BMLC2GUI.lonTag;
import static edu.gmu.netlab.BMLC2GUI.mapGraph;
import static edu.gmu.netlab.BMLC2GUI.mapHandler;
import static edu.gmu.netlab.BMLC2GUI.mapMSDL;
import static edu.gmu.netlab.BMLC2GUI.opord_C2CoreRoot;
import static edu.gmu.netlab.BMLC2GUI.opord_C2CoreSchemaFile;
import static edu.gmu.netlab.BMLC2GUI.opord_C2CoreXUIFile;
import static edu.gmu.netlab.BMLC2GUI.orderBMLType;
import static edu.gmu.netlab.BMLC2GUI.orderIDXPath;
import static edu.gmu.netlab.BMLC2GUI.reportBMLType;
import static edu.gmu.netlab.BMLC2GUI.reportOrderScale;
import static edu.gmu.netlab.BMLC2GUI.routeFromViaToTag;
import static edu.gmu.netlab.BMLC2GUI.routeIdLabelTag;
import static edu.gmu.netlab.BMLC2GUI.routeLayerMSDL;
import static edu.gmu.netlab.BMLC2GUI.routeXPathTag;
import static edu.gmu.netlab.BMLC2GUI.sbmlOrderDomainName;
import static edu.gmu.netlab.BMLC2GUI.sbmlOrderServerName;
import static edu.gmu.netlab.BMLC2GUI.sbmlReportInfoDomainName;
import static edu.gmu.netlab.BMLC2GUI.sbmlReportPullDomainName;
import static edu.gmu.netlab.BMLC2GUI.sbmlReportServerName;
import static edu.gmu.netlab.BMLC2GUI.sbmlUnitInfoDomainName;
import static edu.gmu.netlab.BMLC2GUI.schemaFolderLocation;
import static edu.gmu.netlab.BMLC2GUI.serverType;
import static edu.gmu.netlab.BMLC2GUI.submitterID;
import static edu.gmu.netlab.BMLC2GUI.w3cBmlDom;
import static edu.gmu.netlab.BMLC2GUI.whereIdLabelTag;
import static edu.gmu.netlab.BMLC2GUI.whereShapeTypeTag;
import static edu.gmu.netlab.BMLC2GUI.whereXPathTag;
import static edu.gmu.netlab.BMLC2GUI.xuiFolderLocation;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.jms.*;
import javax.naming.*;
import javax.swing.JOptionPane;
import java.util.*;

// DOM and XPATH
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

// ************** NOTE: this reverts to early STOMP lib that works on command line
// Needs to be updated to latest STOMP Lib. There is a start on this in 
// workingGUI/BMLC2GUIREST/src/SBMLSubscriber.java

/**
 * subscribes to STOMP server, reads STOMP message,
 * and if they are General Status Report posts them to map
 * 
 * @author dcorner
 * @author modified: mababneh
 * @since	3/1/2010
 * @param args the command line arguments
 */
public class SBMLSubscriber implements Runnable {
    
    public static String host = "HOSTNAME";
    static int count = 0;
    public static org.w3c.dom.Document listenerDocument;    // the BML document
    public static boolean subscriberIsOn = false;
    public static TopicConnection conn = null;
    public static TopicSession session = null;
    private javax.jms.Topic topic;
    BMLC2GUI bml = BMLC2GUI.bml;
    
    //, topicDemo, topicDemo2, topic1, topic2, topic3, topic4, topic5, topic6, topic7 = null;
    private TopicSubscriber recvDemo, recvDemo2, recv1, recv2, recv3, recv4, recv5, recv6, recv7;
    private Properties prop;
    
    private DocumentBuilderFactory w3cDocFactory = null;
    private DocumentBuilder w3cDocBuilder = null;
    private org.w3c.dom.Document w3cSubDoc;
    private XPathFactory xpathFactory = null;
    private XPath xpath = null;
    
    String subDocReportType = "";
    String subDocUnitID = "";
    String subDocGDCLat ="";
    String subDocGDCLon ="";
    /**
     * Constructor (empty)
     */
    public SBMLSubscriber() throws Exception {}
    
    /**
     * Method to stop Subscriber   TODO: This needs a shutdownhook **** debugx
     */
    public void stopSub() {
    	subscriberIsOn = false;
      if(!bml.stompIsConnected)return;
        BMLC2GUI.printDebug("Subscriber is OFF - No messages will be accepted");
        if(!BMLC2GUI.serverType.getIsREST())return;
    	stomp.disconnect();
      bml.stompIsConnected =  false;
    }

    //  make a DOM Document from a String
    public static org.w3c.dom.Document loadXmlFrom(java.io.InputStream is)
        throws org.xml.sax.SAXException, java.io.IOException
    {
        javax.xml.parsers.DocumentBuilderFactory factory =
        javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex) {}
        org.w3c.dom.Document doc = builder.parse(is);
        BMLC2GUI.printDebug("Document string" + doc.toString());
        is.close();
        return doc;
    }
    
    /**
     * Start of thread
     */
    BMLClientSTOMP_Lib stomp;
    public void run() {
      
        host = BMLC2GUI.sbmlOrderServerName; // = sbmlReportServerName;
        BMLC2GUI.printDebug("Begin Subscriber");
        BMLC2GUI.printDebug("Subscribe to : " +  host);

        // string array of info to be passed to drawing method
        String[] subDocArray;
        subDocArray = new String[4]; //unitID, Symbol, Lat, Lon
           
        // assume if server is REST it's also STOMP
        if(!BMLC2GUI.serverType.getIsREST()) {
          System.err.println(
            "unable to make STOMP connection because serverType is not REST");
          return;
        }
        
        // we are only able to handle IBML09 reports
        if(!BMLC2GUI.reportBMLType.equals("IBML")) {
          System.err.println("unable to handle reports other than IBML09 GSR");
          return;
        }
        
            BMLC2GUI.printDebug("making STOMP connection");
            String user, domain, hostSource;
            Document doc;

            // Create STOMP Client Object
            stomp = new BMLClientSTOMP_Lib();

            // Set STOMP parameters
            user = "BMLC2GUI";
            domain = BMLC2GUI.sbmlOrderDomainName;
            hostSource = BMLC2GUI.sbmlReportServerName;
            // temp 
            // mababneh@10-29-2014
            hostSource = BMLC2GUI.sbmlOrderServerName;
            stomp.setHost(hostSource);
            String stompPort = "61613";
            stomp.setPort(stompPort);
            stomp.setDestination("/topic/BML");

            // echo parameters to log
            BMLC2GUI.printDebug("STOMP client parameters:");
            BMLC2GUI.printDebug("Domain:" + domain);
            BMLC2GUI.printDebug("STOMP host:" + hostSource);
            BMLC2GUI.printDebug("STOMP port:" + stompPort);

            // Connect to the host
            String response = null;
            if(!bml.stompIsConnected) {
              try {
                response = stomp.connect();
                if (!(response.equals("OK"))) {//debugx 
                    System.err.println(
                      "Failed to connect to STOMP host: " + hostSource + " - " + response);
                    return;
                }
              } catch(Exception bce){
                BMLC2GUI.printDebug("STOMP connect failed with message:" +bce.getMessage());
                return;
              }
              bml.stompIsConnected =  true;
              subscriberIsOn = true;
            }// end if(!bml.stompIsConnected) 
            BMLC2GUI.printDebug("STOMP connect response:" + stomp.getMsgType());
            
 
            // possible header values
            //BMLSTOMPMessage message = new BMLSTOMPMessage();//debugx
            String message = null;
            String selectorDomain = stomp.getHeader("selectorDomain");
            String submitter = stomp.getHeader("submitter");
            String firstforwarder = stomp.getHeader("firstforwarder");
            
            int messageCounter = 0;

            // Start listening
            while (subscriberIsOn) {
            while (true)
            {
                // read message with STOMP blocking until next
                try
                {
                    message = stomp.getNext_Block();
                }
                catch(Exception e)
                {
                    System.err.println("STOMP exception:"+e.getMessage());
                    e.printStackTrace();
                }

                    // extract parameters from header
                    if(stomp.getHeader("FKIE").length() > 0)
                        selectorDomain = "FKIE";
                    else if(stomp.getHeader("CBML_Order").length() > 0)
                        selectorDomain = "CBML";
                    else if(stomp.getHeader("CBML_Report").length() > 0)
                        selectorDomain = "CBML";
                    else if(stomp.getHeader("CBMLFULL_Order").length() > 0)
                        selectorDomain = "CBMLFULL";
                    else if(stomp.getHeader("CBMLFULL_Report").length() > 0)
                        selectorDomain = "CBMLFULL";
                    else if(stomp.getHeader("MSDL").length() > 0)
                        selectorDomain = "MSDL";
                    else if(stomp.getHeader("IBML_Order").length() > 0)
                        selectorDomain = "IBML";
                    else if(stomp.getHeader("IBML_Report").length() > 0)
                        selectorDomain = "IBML";
                    else if(stomp.getHeader("DOC").length() > 0)
                        selectorDomain = "DOC";
                    else selectorDomain = "UNKNOWN";

                    firstforwarder = stomp.getHeader("firstforwarder");
                
                BMLC2GUI.printDebug("received selectorDomain:" + selectorDomain);
                BMLC2GUI.printDebug("received submitter:" + submitter);
                BMLC2GUI.printDebug("received firstforwarder:" + firstforwarder);
                String messageBody = message.trim();

                // for debug, print the message
                BMLC2GUI.printDebug("STOMP received:"+messageBody);
                messageCounter++;
                BMLC2GUI.printDebug("Number of STOMP messages received so far:" + messageCounter);
                
                // in order to use the message to display a report
                // we will need to make a DOM Document out of it
                
                // only draw messages of selectorDomain = IBML. 
                // The other 2 types contain the same geo info
                if (BMLC2GUI.reportBMLType.equals("IBML")) {
                  
                  // set parameters for IBML09 report
                  BMLC2GUI.sbmlOrderDomainName = "IBML";
                  BMLC2GUI.generalBMLFunction = "IBML";
                  BMLC2GUI.reportBMLType = "IBML";
                  bml.bmlDocumentType = "GeneralStatusReport";	
                  bml.documentTypeLabel.setText(bml.bmlDocumentType);
                  bml.xsdUrl = 
                    URLHelper.getUserURL(
                      BMLC2GUI.guiFolderLocation + 
                        BMLC2GUI.ibml09ReportSchemaLocation);//Schema File XSD
   
                  bml.xmlUrl = null;
                  bml.tmpUrl = null;
                  bml.tmpFileString = null;
                  bml.xuiUrl = URLHelper.getUserURL(
                    // Jaxfront XUI file
                    BMLC2GUI.xuiFolderLocation + "/GeneralStatusReportView09.xui");
                  bml.root = BMLC2GUI.ibmlns+"BMLReport";
                  
                  // Generate the swing GUI
                  bml.drawFromXML(
                    "default-context", 
                    bml.xsdUrl, 
                    bml.xmlUrl, 
                    bml.xuiUrl, 
                    bml.root, 
                    bml.bmlDocumentType,
                    BMLC2GUI.ibmlns+"GeneralStatusReport",
                      (new String[]{BMLC2GUI.ibmlns+"UnitID",BMLC2GUI.ibmlns+"Hostility"}),
                      (new String[]{BMLC2GUI.ibmlns+"Latitude",BMLC2GUI.ibmlns+"Longitude"}),
                    bml.ibmlns,
                    messageBody
                  );
              }// end if (BMLC2GUI.reportBMLType.equals("IBML"))   

              // reset message and go back to wait loop
              message = null;
                
           
        }// end Subscriber while(true)
            
        }// end STOMP subscriber    
            
        // wait for messages to be delivered to the onMessage method
        while (subscriberIsOn) 
            try{Thread.sleep(1000);}catch(InterruptedException ie){}
        
    }// end run()
    
    	/**
	 * Open an existing BML Order (XML Document) from the file System
	 * 
	 * @since	02/03/2010
	 */
	void editorSubDoc() {
		//.releaseXUICache();
    BMLC2GUI.sbmlOrderDomainName = "IBML";
		bml.bmlDocumentType = "IBML Report";
		bml.documentTypeLabel.setText(bml.bmlDocumentType);
		bml.xsdUrl = 
      URLHelper.getUserURL(
        BMLC2GUI.guiFolderLocation + 
        "/BMLC2GUI/Schema/IBML-2/IBMLReports.xsd");		//Schema File XSD
		/*JFileChooser xmlFc = new JFileChooser(bml.guiFolderLocation + "//");		//XML file
		xmlFc.setDialogTitle("Enter the Order XML file name");
		xmlFc.showOpenDialog(bml);
    if(xmlFc.getSelectedFile() == null)return;
		bml.xmlUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		bml.tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		bml.tmpFileString = xmlFc.getSelectedFile().toString() + "(tmp)";
                */
    bml.xmlUrl = URLHelper.getUserURL(
      BMLC2GUI.guiFolderLocation+"BMLC2GUI/Schema/IBML09_GSR.xml");
		//bml.xuiUrl = URLHelper.getUserURL(bml.xuiFolderLocation + "/TabStyleOrder.xui");		// Jaxfront XUI file
    bml.xuiUrl = URLHelper.getUserURL(
      BMLC2GUI.guiFolderLocation+"BMLC2GUI/XUIView/IBML09.xui");		// Jaxfront XUI file
		bml.root = "BMLReport";
		
    // Generate the swing GUI
		bml.drawFromXML(
      "default-context", 
      bml.xsdUrl, 
      bml.xmlUrl, 
      bml.xuiUrl, 
      bml.root, 
      bml.bmlDocumentType,
      null,
      null,
      null,
      bml.ibmlns,
      null
    );
	}
/**
 * method drawSub draws map graphics 
 * @param unitArray
 * @param subType 
 */
    public void drawSub(String [] subDocArray, String subType){ //URL url1, URL url2, URL url3, String root){

                //initDom(null, url1, url2, url3, root);
		
    /*
		try {	
			// reading the whole DOM values of the XML file using XPath
		
			//Serialize the JaxFront DOM to to W3C DOM
			try {w3cBmlDom = currentDom.serializeToW3CDocument();}
			catch (Exception e) {e.printStackTrace();}

			NodeList unitNodes = (NodeList) xpathMSDL.evaluate("//" + "Unit" ,w3cBmlDom, XPathConstants.NODESET);
			NodeList equipmentNodes = (NodeList) xpathMSDL.evaluate("//" + "EquipmentItem" ,w3cBmlDom, XPathConstants.NODESET);
	
	    	BMLC2GUI.printDebug("Unit Node List Length = " + unitNodes.getLength());
	    	BMLC2GUI.printDebug("Equipment Node List Length = " + equipmentNodes.getLength());
	    	
	    	int orgArrayLength = unitNodes.getLength()+ equipmentNodes.getLength();
	    	int orgArrayIndex = 0;
	    	String [][] orgUnitsAndEquipments = new String[orgArrayLength][4];
	    	for (int i=0 ; i < unitNodes.getLength() ; i++){
	    		//WhereLabel
	    		Node unit = unitNodes.item(i);
	    		//unitName
	    		orgUnitsAndEquipments[orgArrayIndex][0] = xpathMSDL.evaluate("Name", unit);
	    		//unitSymbol
	    		orgUnitsAndEquipments[orgArrayIndex][1] = xpathMSDL.evaluate("SymbolIdentifier", unit);
	    		//unitLat
	    		orgUnitsAndEquipments[orgArrayIndex][2] = xpathMSDL.evaluate("Disposition//Location//CoordinateData//GDC//Latitude", unit);
	    		//unitLon 
	    		orgUnitsAndEquipments[orgArrayIndex][3] = xpathMSDL.evaluate("Disposition//Location//CoordinateData//GDC//Longitude", unit);
	    		orgArrayIndex++;
	    	} // end for
	    	
	    	
	    	
	    	for (int j=0 ; j < equipmentNodes.getLength() ; j++){
	    		//WhereLabel
	    		Node equipment = equipmentNodes.item(j);
	    		//unitName
	    		orgUnitsAndEquipments[orgArrayIndex][0] = xpathMSDL.evaluate("Name", equipment);
	    		//unitSymbol
	    		orgUnitsAndEquipments[orgArrayIndex][1] = xpathMSDL.evaluate("SymbolIdentifier", equipment);
	    		//unitLat
	    		orgUnitsAndEquipments[orgArrayIndex][2] = xpathMSDL.evaluate("Disposition//Location//CoordinateData//GDC//Latitude", equipment);
	    		//unitLon 
	    		orgUnitsAndEquipments[orgArrayIndex][3] = xpathMSDL.evaluate("Disposition//Location//CoordinateData//GDC//Longitude", equipment);
	    		orgArrayIndex++;
	    	} // end for
	    	
	    	BMLC2GUI.printDebug("=== Printing Units and Equipments found in MSDL Organization");
	    	for (int j1=0 ; j1< orgUnitsAndEquipments.length; j1++){
	    		BMLC2GUI.printDebug("Org Name: " + orgUnitsAndEquipments[j1][0]);
	    		BMLC2GUI.printDebug("Org Symbol ID: " + orgUnitsAndEquipments[j1][1]);
	    		BMLC2GUI.printDebug("Org Lat: " + orgUnitsAndEquipments[j1][2]);
	    		BMLC2GUI.printDebug("Org Lon: " + orgUnitsAndEquipments[j1][3]);   		 		
	    	}
	    	
	    	String areaOfInterestName, areaofInterestUpperRightLat, areaofInterestUpperRightLon, areaofInterestLowerLeftLat, areaofInterestLowerLeftLon;
	    	NodeList areaOfInterestNodes = (NodeList) xpathMSDL.evaluate("//" + "AreaOfInterest" ,w3cBmlDom, XPathConstants.NODESET);
	    	Node areaofInterestNode = areaOfInterestNodes.item(0);
	    	areaOfInterestName = xpathMSDL.evaluate("Name", areaofInterestNode);
	    	areaofInterestUpperRightLat = xpathMSDL.evaluate("//UpperRight/CoordinateData//GDC//Latitude", areaofInterestNode);
	    	areaofInterestUpperRightLon = xpathMSDL.evaluate("//UpperRight/CoordinateData//GDC//Longitude", areaofInterestNode);
	    	areaofInterestLowerLeftLat = xpathMSDL.evaluate("//LowerLeft/CoordinateData//GDC//Latitude", areaofInterestNode);
	    	areaofInterestLowerLeftLon = xpathMSDL.evaluate("//LowerLeft/CoordinateData//GDC//Longitude", areaofInterestNode);
	    	
	    	BMLC2GUI.printDebug("areaOfInterestName: " + areaOfInterestName);
	    	BMLC2GUI.printDebug("areaofInterestUpperRightLat: " + areaofInterestUpperRightLat);
	    	BMLC2GUI.printDebug("areaofInterestUpperRightLon: " + areaofInterestUpperRightLon); 
	    	BMLC2GUI.printDebug("areaofInterestLowerLeftLat : " + areaofInterestLowerLeftLat);
	    	BMLC2GUI.printDebug("areaofInterestLowerLeftLon : " + areaofInterestLowerLeftLon );

	    	String [] areaOfInterestArray = new String[5];
	    	areaOfInterestArray[0] = areaOfInterestName;
	    	areaOfInterestArray[1] = areaofInterestUpperRightLat;
	    	areaOfInterestArray[2] = areaofInterestUpperRightLon;
	    	areaOfInterestArray[3] = areaofInterestLowerLeftLat;
	    	areaOfInterestArray[4] = areaofInterestLowerLeftLon;
	    	
                */
	    	// Calling the drawing method
	    	mapGraph = true;
			BMLC2GUI.printDebug("------drawSubscribe-----");
			
			//if (!mapMSDL) {
				// mapMSDL 
                            RouteLayer routeLayerSub;
                            routeLayerSub = new RouteLayer();
                            routeLayerSub.setName("Subscriber"); //bmlDocumentType
                            routeLayerSub.setVisible(true);
                            mapHandler.add(routeLayerSub);
                            mapMSDL = true;
			//}
			routeLayerSub.createSubscriberGraphics(subDocArray, subType);
			
		/*	
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
                */
	}// end drawSub
    /**
     * 
     * @param fileName
     * @param doc
     * @return 
     */
    private boolean saveSubDocument(String fileName, Document doc) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        BMLC2GUI.printDebug("Saving XML file... " + fileName);
        
        	// open output stream where XML Document will be saved
        	File xmlOutputFile = new File(fileName);
        	FileOutputStream fos;
        	Transformer transformer;
     
        	try {
        		fos = new FileOutputStream(xmlOutputFile);
        	}
        	catch (FileNotFoundException e) {
        		System.err.println("Error occured: " + e.getMessage());
        		return false;
        	}
    
        	// Use a Transformer for output
        	TransformerFactory transformerFactory = TransformerFactory.newInstance();
        	try {
        		transformer = transformerFactory.newTransformer();
        	}
        	catch (TransformerConfigurationException e) {
        		System.err.println("Transformer configuration error: " + e.getMessage());
        		return false;
        	}
        	DOMSource source = new DOMSource(doc);
        	StreamResult result = new StreamResult(fos);
   
        	try {        	// transform source into result will do save
        		transformer.transform(source, result);
        	}
        	catch (TransformerException e) {
        		System.err.println("Error transform: " + e.getMessage());
        	}
        	BMLC2GUI.printDebug("XML file saved.");
        	return true;
        
    }
    
    /**
     *  Embedded call back class that will receive published messages.  One instance per subscription
     */
    public static class ExListener implements MessageListener {
        String id;            //  Same topic name or other identifier
        XPathFactory f = null;
        XPath xpath = null;
        DocumentBuilderFactory factory = null;
        DocumentBuilder docB = null;
      
        /**
         * Constructor
         * 
         * @param id
         */
        ExListener(String id) throws Exception {
            f = XPathFactory.newInstance();   // Create an xpath object
            xpath = f.newXPath();
            factory = DocumentBuilderFactory.newInstance();
            docB = factory.newDocumentBuilder();
            this.id = id;
        }

        public static org.w3c.dom.Document getListenerDocument(){
        	return listenerDocument;
        }
       
        public void onMessage(Message msg) {
            TextMessage tm = (TextMessage) msg;
            BMLC2GUI.printDebug("###########################----------------###############################");
            try {
            	BMLC2GUI.subscriberNewReport = true;
                Document doc = docB.parse(new ByteArrayInputStream(tm.getText().getBytes()));
                
                saveXMLDocument(BMLC2GUI.guiFolderLocation +"/BMLC2GUI/SubscriberDOM.xml", doc);
                listenerDocument = doc;
                                      
                String when = xpath.evaluate("//When", doc);    
                String orderID = xpath.evaluate("//OrderID", doc);
                NodeList taskList = (NodeList) xpath.evaluate("//Task", doc, XPathConstants.NODESET);
                int numTasks = taskList.getLength();
                String task = xpath.evaluate("//TaskID", doc);
                String tasker = xpath.evaluate("//TaskerWho/UnitID", doc);
                String taskee = xpath.evaluate("//TaskerWho/UnitID", doc);

                NodeList reportList = (NodeList) xpath.evaluate("//Report", doc, XPathConstants.NODESET);
                int numReports = reportList.getLength();
                String reportID = xpath.evaluate("//ReportID", doc);
                String reporter = xpath.evaluate("//ReporterWho/UnitID", doc);
                String executer = xpath.evaluate("//Executer/Taskee/NameText", doc);
                String reportType = xpath.evaluate("//TypeOfReport", doc);

                BMLC2GUI.reportInfoComboBox.addItem(reportID + " | " + reportType + " | " + executer);
                BMLC2GUI.reportInfoComboBox.hide();                // trigger the ComboBox Component Listener 
                BMLC2GUI.reportInfoComboBox.show();
                
                // Print contents from returned BML string.
                if (!orderID.equals("")) {
                    System.out.printf("%4d  %s  Topic:%-10s OrderID:%s numTasks:%2d FirstTaskID:%s  Tasker:%s Taskee:%s \n",
                            ++count, getTimeStamp(), id, orderID, numTasks, task, tasker, taskee);
                } else if (!reportID.equals("")) {
                    System.out.printf("%4d  %s  Topic:%-10s Date:%s FirstReportID:%-20s numReports:%2d Reporter:%s Executer:%s\n",
                            ++count, getTimeStamp(), id, when, reportID, numReports, reporter, executer);
                } else {
                    System.out.printf("%s  Topic:%s - Contents :%s\n",
                            getTimeStamp(), id, msg);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
        }// end onMessage(0
        
        /**
         * Save XML Document into XML file.
         */
        public boolean saveXMLDocument(String fileName, Document doc) {
        	BMLC2GUI.printDebug("Saving XML file... " + fileName);
        
        	// open output stream where XML Document will be saved
        	File xmlOutputFile = new File(fileName);
        	FileOutputStream fos;
        	Transformer transformer;
     
        	try {
        		fos = new FileOutputStream(xmlOutputFile);
        	}
        	catch (FileNotFoundException e) {
        		System.err.println("Error occured: " + e.getMessage());
        		return false;
        	}
    
        	// Use a Transformer for output
        	TransformerFactory transformerFactory = TransformerFactory.newInstance();
        	try {
        		transformer = transformerFactory.newTransformer();
        	}
        	catch (TransformerConfigurationException e) {
        		System.err.println("Transformer configuration error: " + e.getMessage());
        		return false;
        	}
        	DOMSource source = new DOMSource(doc);
        	StreamResult result = new StreamResult(fos);
   
        	try {        	// transform source into result will do save
        		transformer.transform(source, result);
        	}
        	catch (TransformerException e) {
        		System.err.println("Error transform: " + e.getMessage());
        	}
        	BMLC2GUI.printDebug("XML file saved.");
        	return true;
        }
    }

    /**
     * Returns String YYYYMMDDHHMMSS.sss
     */
    public synchronized static String getTimeStamp() {
        Calendar now = Calendar.getInstance();        // Create a timestamp
        long i = System.currentTimeMillis() % 1000;
        String dttm = String.format("%04d/%02d/%02d %02d:%02d:%02d",
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DATE),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                i);
        return dttm;
    }
} // End of SBMLSubscriber Class
