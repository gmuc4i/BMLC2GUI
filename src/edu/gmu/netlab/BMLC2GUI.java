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

// this is Version 2.0 of BMLC2GUI that works with REST and STOMP

// capable of loading, parsing, editing, and pushing CBML Light and IBML09
// reports and orders using REST

package edu.gmu.netlab;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;



import javax.swing.*;
import javax.swing.border.Border;
import com.jaxfront.core.dom.DOMBuilder;
import com.jaxfront.core.dom.Document;
import com.jaxfront.core.help.HelpEvent;
import com.jaxfront.core.help.HelpListener;
import com.jaxfront.core.schema.ValidationException;
import com.jaxfront.core.ui.TypeVisualizerFactory;
import com.jaxfront.core.util.LicenseErrorException;
import com.jaxfront.core.util.URLHelper;
import com.jaxfront.core.util.io.BrowserControl;
import com.jaxfront.core.util.io.cache.XUICache;
import com.jaxfront.pdf.PDFGenerator;
import com.jaxfront.swing.ui.editor.EditorPanel;
import com.jaxfront.swing.ui.editor.ShowXMLDialog;

import java.net.MalformedURLException;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.*;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.tools.symbology.milStd2525.PNGSymbolImageMaker;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolChooser;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolReferenceLibrary;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;

import edu.gmu.c4i.sbml.Exception_Exception;
import edu.gmu.c4i.sbmlclientlib.SBMLClient;
import java.util.*;

// DOM and XPATH
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.*;

/**
 * BMLC2 GUI (Initially BMLGUI)
 * 
 * Started : 1/20/2009
 * Used Tools: This project uses two great tools
 * 			   1. Xcentric JAXFront. WebSite http://www.jaxfront.org
 * 			   2. BBN OpenMap (Now a Raytheon Company). WebSite http://www.openmap.org/
 * Purpose : The purpose of this project is to develop an Easy End-User Interface for Viewing and 
 * 			 Editing BML Orders and Reports. It is a Run-Time GUI Generation  Based on BML-XML Schema.
 * 			 Also an important development goal is to make it a Open-Source application.
 * Functionality : This Application will provide the user with the following functionality:
 * 		1. Create a new order : 
 * 			a)  The user can create a new order (create the XML file that will be used by BML 
 * 			    Web Services that will process it and send it to the JC3IDEM database).
 *      	b)  The user can create any type of order (Ground, Air, �)
 *      	c)	The user can enter as many tasks as he wants (Multiple or single task).
 *      	d)	This is done through an easy Java Swing desktop window
 *      	e)	The resulting order should conform to the BML XML Schema
 *      2.	View an order :
 *      	a)	The user can open and view an existing order (order that was created previously 
 *      		using this BMLC2GUI tool or by anything else).
 *      	b)	[Validation] The user can run a compatibility (conformability) test to make sure
 *      		 that the order (XML file)  is compatible with the BML Schema  (XSD file).
 *      3.	Edit an order :
 *      	a)	The user can open and edit an existing order (order that was created previously 
 *      		using this BMLC2GUI tool or by anything else).
 *      	b)	The user can add a new task, modify an existing task or delete some task.
 *      4.	Validate an Order :	[Validation] The user can run a compatibility (conformability) 
 *      	test to make sure that the order (XML file)  is compatible with the BML Schema  (XSD file).
 *      5.	Serialize an order:	[Serialization] The user can see XML version of what is he editing
 *      	on the screen (the XML source code)
 *      6.	Save an order :
 *      	a)	The user can save the order (what he gets on the screen)
 *      	b)	The user can have the option to save the order weather it is valid 
 *      		(conforms to the BML Schema) or not ( he or somebody else can review it later).
 *      7.	Print an order :
 *      	a)	The user can print the order (what he gets on the screen) in a form layout.
 *      	b)	The user can save /print the order (what he gets on the screen) in a PDF Format.
 *      	c)	The user can print the order source XML code.
 *      8.	Reports :	The user can open BML and SIMCI Reports reports 
 *      9. Submit an Order / Report:
 *      	a)	The user can submit the order / Report (invoke the web service
 *      10.	Maps :
 *      	a)	The user can see any coordinates values from the GUI on the Map. Points, lines 
 *      		and shapes are drawn based on geo data from the GUI.
 * 
 * @author	Mohammad Ababneh, C4I Center, George Mason University
 * @since	4/9/2010
 * 
 * @author	Eric Popelka, C4I Center, George Mason University
 * @since	6/29/2013
 */
public class BMLC2GUI extends JFrame implements WindowListener, HelpListener, 
	ItemListener, ActionListener {
	
	// Main Application's Frame Height and Width
	private final static int WINDOW_HEIGHT = 768; 
	private final static int WINDOW_WIDTH = 1440;
	
	// Main Frame Panels
	private JPanel centerPanel;
	private JPanel editorMapPanel;
	private JPanel editorButtonPanel;
	private JPanel topButtonPanel;
	
	private JSplitPane splitPane;
	EditorPanel editor;
	
	//topButtonPanel Buttons - 
	private JTextField sbmlHost;
	private JTextField sbmlReportDomain;
	private JTextField sbmlOrderDomain;
	private JTextField sbmlClientLocation;
   
	// Report Info Combo Box
	private String[] reportInfoArrayList;  			// Array to build combo box
	private String[] reportInfoArray;      			// Array to read report information from ws
	public static	JComboBox  reportInfoComboBox;  // Report Info combo box
	private JButton pullReportInfoButton;  			// refresh report info combo box
	private JButton pullReportByIDButton;  			// refresh report info combo box
	private String selectedReportInfo = "";
        
        private JLabel subscriberStatusLabel;

	// Buttons
	private JButton drawButton;
	private JButton eraseButton;
	private JButton testVisButton;
	private JButton testEditorButton;
	private JButton subscribeButton;
	private JButton unsubscribeButton;
	private JButton testPullButton;
	private JButton pullReportButton;
	private JButton pullReportIDButton;  // refresh combo box
	
	JLabel documentTypeLabel;
	private String[] reportIDArrayList;  	// Array to build combo box
	private String[] reportIDArray;      	// Array to read report information from ws
	private	JComboBox  reportIDComboBox; 	// ID combo box
	private String afterTime = "";       	// time after which to bring reports 
	private String selectedReportID = "";

	public static boolean subscriberNewReport = false;
	Document currentDom;     				      // Current Jaxfront Dom Document
  String currentXmlString = null;
	String currentLanguage = "en"; 			  // English as default
  String[] currentXmlDataArray = null;
	
	public static MapHandler mapHandler;  // OpenMap MapHandler
	public static RouteLayer[] routeLayer;// Array of Route Layers
	private final static int RouteLayerArraySize = 100; // Size of Route Layer Array
	private int routeLayerIndex = 0;   	  // Index of Route Layer
	private LayerHandler layerHandler;
	XPath xpathCBML = null;								// CBML XPath
	XPath xpathMSDL = null;								// MSDL XPath
	public static RouteLayer routeLayerOPORD = null; 	// Array of Route Layers
	public static RouteLayer routeLayerMSDL = null; 	// MSDL Layer
	private Properties c2mlProps;
	private MapPanel mapPanel;
  public boolean stompIsConnected = false;
	
	public static BMLC2GUI bml;					  // bml variable for main GUI frame
  
  public static webservices ws;         // to access REST/STOMP
  
  public static boolean debugMode = false;// to print parameters as GUI runs
	
	public static MapBean mapBean; 			  // Open Map mapBean
	
	 /** Property for space separated layers. "c2ml.layers" */
    public static final String layersProperty = "c2ml.layers";

    /** The name of the resource file. "BMLC2GUI.properties" */
  public static String c2mlResources = "bmlc2gui.properties"; 
    
  String bmlString = new String();    // BML Document converted to a csv
	String[] bmlStringArray;    		    // array from bmlstring
	private String[] shapeStringArray;	// array from Shape Coordinate
	String shapeType;					          // Shape Type
	int shapeCoords;					          // Shape's number of coordinates
	String[] tempShapeOPORD;
	String label;
	String stringOPORDName;
	String[] tempLocationsOPORD;
	int locationCoords;
	String bmlDocumentType = ""; // bml document type // Order, Report, ..and what type of report
	String tmpFileString;
	String[] bmlLatCoords;
	String[] bmlLonCoords;
	int numCoords = -1;
	int highlightedCoord = -1;
	int nextCoord = -1;
	int selectedCoord = -1;
  String lattmp = "";
  String lontmp = "";
  boolean redrawing = false;
  static boolean isWindows;

	String root = null; // Document Root node
	URL xsdUrl = null;  // String for Schema File
	URL xmlUrl = null;  // String for XML File / Document
	URL xuiUrl = null;  // String for JaxFront XUI file / Style view file
	URL tmpUrl = null;	// String for Temporary XML File / Document
	
	public static boolean mapGraph = false;	// Boolean variable to check if a graph exists on the map
  private org.w3c.dom.Document subscriberDocument;
  public static org.w3c.dom.Document w3cBmlDom;
	
  private XPathFactory xpathFactory = null;
  private XPath xpath = null;
  private DocumentBuilderFactory w3cDocFactory = null;
  private DocumentBuilder w3cDocBuilder = null;
  private org.w3c.dom.Document w3cReportInfoDoc;

  private SBMLSubscriber sub = null;
	private volatile Thread threadSub;
  
	// Variables to hold configuration file values
  public static String ibmlns;                    // namespace string for IBML09 XML documents
  public static String cbmlns;                    // namespace string for CBML XML documents
	public static String sbmlReportServerName;	    // SBMLServer name variable
	public static SBMLServerType serverType;        // Software running the SBML server
	public static String submitterID;               // SBMLClient SubmitterID variable
	public static String sbmlReportInfoDomainName;
	public static String reportBMLType;		          // SBMLCLient Report BML Type
	public static String orderBMLType;              // SBMLCLient Order BML Type
  public static String generalBMLFunction;        // Order or Report
  public static String generalBMLType;            // CBML or IBML 
	public static String sbmlReportPullDomainName;	// Report pull domain name
	public static String sbmlUnitInfoDomainName;	  // Unit info domain name
	public static String sbmlOrderDomainName;       // Order push / pull domain name
	public static String sbmlOrderServerName;       // Order push / pull server name
	public static String initMapLat;                // Initial Map Latitude
	public static String initMapLon;                // Initial Map Longitude
	public static String reportOrderScale;          // Report or Order Zoom scale value
	public static String guiFolderLocation;	        // GUI URL folder location
	public static String guiLocationXML;			      // XML config file location
	public static String guiLocationXSD;			      // XSD config file location
	public static String guiLocationXUI;			      // XUI config file location
	public static String delimiter = "/";			      // set to "/" for Unix
  public static String schemaFolderLocation;		  // schema work folder
  public static String cbmlOrderSchemaLocation;   // CBML Order schema
  public static String cbmlReportSchemaLocation;  // CBML Order schema
  public static String ibml09OrderSchemaLocation; // CBML Order schema
  public static String ibml09ReportSchemaLocation;// CBML Order schema
	public static String opord_C2CoreSchemaFile;    // OPORD or C2Core schema location
	public static String opord_C2CoreRoot;			    // OPORD or C2Core Root
	public static String xuiFolderLocation;			    // Location of XUI file
	public static String opord_C2CoreXUIFile;		    // OPORD or C2Core of XUI file
	public static String orderIDXPath;			        // OrderID string of config file XPath
	public static String whereXPathTag;			        // At Where Location XPath
	public static String routeXPathTag;			        // Route Where Location XPath	
	public static String whereIdLabelTag;			      // Where ID Label
	public static String whereShapeTypeTag;			    // Where shape type
	public static String routeIdLabelTag;			      // Route label
	public static String latlonParentTag;			      // Lat Lon Parent xpath
	public static String latTag;				            // Lat xpath
	public static String lonTag;				            // Lon Xpath
	public static String routeFromViaToTag;			    // Route from-via-to xpath
	public static boolean mapOPORD = false;
	public static boolean mapMSDL = false;
	public static String osName;
	
	NamespaceContext nsContext= new NamespaceContext() {
		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}
		public String getPrefix(String namespaceURI) {
			return null;
		}
		public String getNamespaceURI(String prefix) {
			String uri = null;
            if (prefix.equals("C_BML"))
                uri = "urn:sisostds:bml:coalition:draft:cbml:1";
            return uri;
		}
	};
	
    /**
	 * Constructor (no-arg only)
	 */
	public BMLC2GUI() {
		super();
		init();
    
    // XPath
	xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
    	xpathCBML = xpathFactory.newXPath();
	xpathCBML.setNamespaceContext(nsContext);
	xpathMSDL = xpathFactory.newXPath();
	xpathMSDL.setNamespaceContext(nsContext);
    
    // webservices
    ws = new webservices(this);
	}

	public static void main(String[] args) {	
    
    // determine host platform
    osName = System.getProperty("os.name");
    isWindows = osName.contains("Windows");
    if(isWindows) {
      printDebug("OS Windows");
      delimiter = "\\";
    }
    else {
      printDebug("OS Unix-like");
      c2mlResources = "bmlc2guiUnix.properties";
		  delimiter = "/";
    }
      
    // command-line argument option: path to the GUI folder
    // if it does not end with a / add one; this way the
    // GUI folder can be in working directory with no path provided 
		if(args.length > 0) {
      guiFolderLocation = args[0];
      if(!guiFolderLocation.endsWith("/") && 
        !guiFolderLocation.endsWith("\\") &&
        (guiFolderLocation.length() != 0)) {
        guiFolderLocation += delimiter;
      }
    }
    
    // debug output
    if(args.length > 1)
      debugMode = args[1].equals("true");
    
    printDebug("guiFolderLocation:" + guiFolderLocation);
    
    // build path-name for the GUI Config XML file
    if(isWindows)
      guiLocationXML = guiFolderLocation + "BMLC2GUI/Config/BMLC2GUIConfig.xml";
    else
		  guiLocationXML = guiFolderLocation + "BMLC2GUI/Config/BMLC2GUIConfigUnix.xml";
    printDebug("guiLocationXML:" + guiLocationXML);
    printDebug("url2:" + URLHelper.getUserURL(guiLocationXML));

    //Schema File XSD
		guiLocationXSD = 
      guiFolderLocation + "BMLC2GUI/Config" + delimiter + "BMLC2GUIConfig.xsd";
    printDebug("guiLocationXSD:" + guiLocationXSD);
	  guiLocationXUI = 
      guiFolderLocation + "BMLC2GUI/Config" + delimiter + "BMLC2GUIConfigView.xui";	
    printDebug("guiLocationXUI:" + guiLocationXUI);
		
	  // Set look and feel based on OS type
	  try {
	    if (isWindows) {
	      // For Windows, use JGoodies looks
			  UIManager.setLookAndFeel(new com.jgoodies.looks.windows.WindowsLookAndFeel());	    		
	  } else {
	    // For Linux, etc., use GTK+ looks
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
			   if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {   
			     UIManager.setLookAndFeel(info.getClassName());
			       break;
			     } 
			   }	    	
	    }
	  } catch (Exception e) {
	    System.err.println("Error setting look and feel: " + e.getMessage());
	  }
		
    // startup BMLC2GUI 
	  bml = new BMLC2GUI();
		
    if (subscriberNewReport == true) {
	    JOptionPane.showMessageDialog(null, " New Report detected in Subscriber" ,"SBML Subscriber ",JOptionPane.INFORMATION_MESSAGE,null);
	  }
  }// end main()
  
  /**
   * print argument only if in debug mode
   */
  static void printDebug(String toPrint)
  {
    if(debugMode)System.out.println(toPrint);
  }
  static void printDebug(int toPrint)
  {
    if(debugMode)System.out.println(toPrint);
  }
  
	/**
	 * Initialize the window frame GUI Frame (Widgets, Layouts, and ActionListeners)
	 */
	public void init() {
		try {			
			// Add the title of the BMLC2GUI Frame, also specify sizes and display it
			setTitle("GMU C4I BMLC2GUI ");
			setSize(WINDOW_WIDTH, WINDOW_HEIGHT);	
			setLayout(new BorderLayout());	
			initMenuBar();			// initialize gui components
			JLabel editorLabel = new JLabel("BMLC2GUI Editor................");
			subscriberStatusLabel = new JLabel("Subscriber is Off");
                         subscriberStatusLabel.setForeground(Color.RED);
			centerPanel = new JPanel(new BorderLayout());
			centerPanel.add(editorLabel,BorderLayout.NORTH);
			
                        
			JPanel _mapPanel = new JPanel();
			_mapPanel.setSize(500, 768);
			_mapPanel.setBackground(Color.GRAY);
		
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); //vertical
			splitPane.setDividerSize(10);
			splitPane.setLastDividerLocation(512);
			splitPane.setBorder(null);
			splitPane.setTopComponent(centerPanel);
			add(splitPane, BorderLayout.CENTER);
					
			//topButtonPanel	
			topButtonPanel = new JPanel();
			topButtonPanel.setLayout(new FlowLayout());
			
			Dimension dim = new Dimension(50,10);
			JLabel sbmlHostLabel = new JLabel("SBML Server: ");
	
			reportInfoArrayList = new String[1];
			reportInfoArrayList[0] = "Report ID           | Report Type       | Unit ID    ";
	
			reportInfoComboBox = new JComboBox(reportInfoArrayList); 
			reportInfoComboBox.isEditable();
			reportInfoComboBox.setAutoscrolls(true);	
			topButtonPanel.add(reportInfoComboBox);
			reportInfoComboBox.addComponentListener(new ComponentListener(){	
				public void componentHidden(ComponentEvent arg0) {}		//Override
				public void componentMoved(ComponentEvent arg0) {}		//Override
				public void componentResized(ComponentEvent arg0) {}	//Override
				public void componentShown(ComponentEvent arg0) {		//Override
				
          printDebug("SBML Subscriber - New Report detected   ");
          printDebug("SBML Subscriber - add report list later to combo box   ");
                                        
          subscriberDocument = SBMLSubscriber.ExListener.getListenerDocument();
                                
				  printDebug(
            "Subscriber XML Document String : "+ 
              subscriberDocument.getTextContent());
					
			    try {
            // disable temp
            //mababneh@11/23/2014
            ws.openReportSub(subscriberDocument);
          } catch (IOException e) {
					  e.printStackTrace();
				  }                              
				}
			});
			
			reportInfoComboBox.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent ie){
					selectedReportInfo = ie.getItem().toString();
					int endIDIndex = 0;
					endIDIndex = selectedReportInfo.indexOf(" | ");
					selectedReportID = selectedReportInfo.substring(0,endIDIndex);
					printDebug("selectedReportInfo : " + selectedReportInfo);
					printDebug("selectedReportID : " + selectedReportID);
					printDebug("Open Report WS >>>>>>>>>>>>>>>>>>>>>> ");
				}
				public void actionPerformed(ActionEvent e) {}
			});
			
			// add the pull report ID button to the toolbar
			pullReportInfoButton = new JButton("Refresh Report Info");
			topButtonPanel.add(pullReportInfoButton);
			pullReportInfoButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					reportInfoComboBox.removeAllItems();
					printDebug("Pull new report info");
					try {
						getReportInfo();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}); 

			// add the open report button to the toolbar
			pullReportByIDButton = new JButton("Pull Selected Report");
			topButtonPanel.add(pullReportByIDButton);
			pullReportByIDButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					printDebug(">>>>>>>>>>>>>>>>>> pullReportByIDButton");
          if(checkCanDo())return;
					try { 	// open the report using the report ID from report info Combo box
						ws.openReportWS(selectedReportID);
					} catch (IOException e1) {	
						e1.printStackTrace();
					}
				}
			});
			
			sbmlHost = new JTextField();
			sbmlHost.setText("armstrong.netlab.gmu.edu ");
			JLabel sbmlReportDomainLabel = new JLabel("Report's Domain: ");
			sbmlReportDomain = new JTextField("report ");
			sbmlReportDomain.setSize(dim);
			JLabel sbmlOrderDomainLabel = new JLabel("Order's Domain: ");
			sbmlOrderDomain = new JTextField("order ");
			sbmlOrderDomain.setSize(dim);
			JLabel sbmlClientLocationLabel = new JLabel("SBMLClient: ");
			sbmlClientLocation = new JTextField("c:\\SBMLClient    ");
			sbmlClientLocation.setSize(dim);
				
			// create a Panel for the buttons
			editorButtonPanel = new JPanel();
			editorButtonPanel.setLayout(new FlowLayout());
			Border border = BorderFactory.createEtchedBorder();
			editorButtonPanel.setBorder(border);

			// add the Order ID Combo Box to the toolbar			
			reportIDArrayList = new String[1];
			reportIDArrayList[0] = "00000000000000000000";
			reportIDComboBox = new JComboBox(reportIDArrayList); 
			reportIDComboBox.isEditable();		
			reportIDComboBox.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent ie){
					selectedReportID = ie.getItem().toString();
					printDebug("selectedReportID : " + selectedReportID);
					printDebug("Open Report WS >>>>>>>>>>>>>>>>>>>>>> ");
					printDebug("Open Report WS >>>>>>>>>>>>>>>>>>>>>> ");
				}
				public void actionPerformed(ActionEvent e) {}
			});
			
			pullReportIDButton = new JButton("Pull Report IDs");
			pullReportIDButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String afterTime = "20060101000000.000";					
			    	afterTime = JOptionPane.showInputDialog(null, " Enter Date-Time Format Example: 20060101000000.000","Pull Report IDs After Time",JOptionPane.QUESTION_MESSAGE);
			    	try {
						reportIDArrayList = getReportIDs(afterTime);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					reportIDComboBox.removeAllItems();
					for (int j=0;j< reportIDArrayList.length; j++){
			    		printDebug(reportIDArrayList[j]);
			    		reportIDComboBox.addItem(reportIDArrayList[j]);
			    	}
				}
			});
			
			// add the open report button to the toolbar
			pullReportButton = new JButton("Pull Report");
			pullReportButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					printDebug("pull report test");
					try {   //Open report with selectedReportID
						ws.openReportWS(selectedReportID);	
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			
			// add the Draw button to the toolbar
			drawButton = new JButton("Refresh Map Graphics");
			drawButton.addActionListener(this);
			
			// add the Erase button to the toolbar
			eraseButton = new JButton("Erase Map Graphics");
			eraseButton.addActionListener(this);
/* debugx is this needed?
			JButton testSubButton = new JButton("Test Sub DOM");
			testSubButton.setSize(50, 10);
			testSubButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					printDebug("Test Sub DOM");
				
					//Open report from Dom
					String root = null;
					String reportType =""; // report type 
					String reportID ;
					String reportPullString ="";					
					releaseXUICache();	  
					String schemaLocation = schemaFolderLocation + "/Reports/";   
					xmlUrl = URLHelper.getUserURL(guiFolderLocation + "\\SBMLCLIENT\\dist\\SubscriberDOM.xml");
					xsdUrl = URLHelper.getUserURL( schemaLocation.concat("IBMLReports.xsd"));
					root = "BMLREPORT";
					xuiUrl = URLHelper.getUserURL(xuiFolderLocation + "/GeneralStatusReportView.xui");
					printDebug("Display General Status Report");
					xpathFactory = XPathFactory.newInstance();
		      xpath = xpathFactory.newXPath();
		      String subReportID;
		      subscriberDocument = SBMLSubscriber.ExListener.getListenerDocument();

		      try {
            subReportID = xpath.evaluate("//ReportID", subscriberDocument);
            JOptionPane.showMessageDialog(null, "Subscriber Report ID : " + subReportID, "SBML Subscriber ",JOptionPane.INFORMATION_MESSAGE,null);
				} catch (XPathExpressionException e1)
        {
				  e1.printStackTrace();               }
				  System.err.println("XML Document String : "+ subscriberDocument.getTextContent());
					
			    //Generate the swing GUI
					drawFromXML("default-context", xsdUrl, xmlUrl, xuiUrl, root, reportType);					
				}// end catch
			});
*/ // end is this needed?			
			// Label for Document Type 
			documentTypeLabel = new JLabel("Document Type"+"");
			documentTypeLabel.setSize(1000, 10);
			documentTypeLabel.setToolTipText("Document Type");
			documentTypeLabel.setForeground(Color.BLUE);
			editorButtonPanel.add(documentTypeLabel);
				
			// add the subscriber button to the toolbar
			subscribeButton = new JButton("Server Subscribe");
			subscribeButton.addActionListener(this);
		
			// add the UN subscriber button to the toolbar
			unsubscribeButton = new JButton("Server Unsubscribe");
			unsubscribeButton.addActionListener(this);
			centerPanel.add(editorButtonPanel,BorderLayout.NORTH);
						
			// add the top Button panel to the main frame 	
			topButtonPanel.add(drawButton);
			topButtonPanel.add(eraseButton);
			topButtonPanel.add(subscribeButton);
			topButtonPanel.add(unsubscribeButton);
      topButtonPanel.add(subscriberStatusLabel);
			add(topButtonPanel,BorderLayout.NORTH);

			JLabel jaxfrontLabel = new JLabel(" Forms generated by JAXFront free community license, Xcentric Technology & Consulting");
			jaxfrontLabel.setForeground(Color.GRAY);	
			add(jaxfrontLabel,BorderLayout.SOUTH);
			initMap();			// Add the Map Panel			
			addWindowListener(this);
			setVisible(true);	// Set GUI Frame visible
			loadConfig();		// Call loadConfig to activate SBMLServer values
		} catch (LicenseErrorException licEx) {
			licEx.showLicenseDialog(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} // End of init method
  
  public String readAnXmlFile(String xmlFilename) {
    
    // read the file
    FileReader xmlFile;
    String xmlString = "";
    try{
      xmlFile=new FileReader(new File(xmlFilename));
      int charBuf; 
      while((charBuf=xmlFile.read())>0) {
        xmlString+=(char)charBuf;
      }
    }
    catch(Exception e) {
      System.err.println("Exception in reading XML file " + xmlFilename + ":"+e);
      e.printStackTrace();
      return "";
    }
    return xmlString;
  
  }// end readAnXmlFile()

	/**
	 * Action Performed / Action Listener method to respond to button selection
	 */
	public void actionPerformed(ActionEvent buttonAction) {
		//Draw Action
		if(buttonAction.getSource() == pullReportIDButton){}
		if(buttonAction.getSource() == pullReportButton){}

		// subscriber  button
		if(buttonAction.getSource() == subscribeButton){
			printDebug("Start Subscriber Thread");
			try {
				sub = new SBMLSubscriber();	// initialize the Subscriber and a thread
			} catch (Exception e) {
				e.printStackTrace();
			}
			threadSub = new Thread(sub);
		
			// Thread implementation - Run the Subscriber in a separate thread
			printDebug("Thread state "  + threadSub.getState().toString());
                        
      subscriberStatusLabel.setText("Subscriber is ON");
      subscriberStatusLabel.setForeground(Color.GREEN);
			
			if (threadSub.getState().toString()=="TERMINATED"){
				threadSub.run();
			}
			else {
				threadSub.start();	
			}
		}
		
		// unsubscribe button
		if(buttonAction.getSource() == unsubscribeButton){	
      if(sub == null) {
        return;
      }
			printDebug("Stop Subscriber Thread");
			printDebug("Thread state "  + threadSub.getState().toString());
                        subscriberStatusLabel.setText("Subscriber is OFF");
                        subscriberStatusLabel.setForeground(Color.RED);
			sub.stopSub();
		}
		
		if(buttonAction.getSource() == testPullButton){
			String reportID ="";
			String[] reportIDList;
			printDebug("Button testPullButton is selected");
	    	File reportIdPullResultFile = 
          new File(guiFolderLocation + "\\SBMLCLIENT\\dist\\reportidspullresult.xml");
	    	String reportString ="";
	        Scanner reportIdPullScanner;
			try {
				reportIdPullScanner = new Scanner(reportIdPullResultFile);
				while (reportIdPullScanner.hasNext()){
		        	reportString = reportIdPullScanner.next();		        	
		        }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			printDebug("The OrderIDs are :" + reportString);
		} 
		
		if(buttonAction.getSource() == drawButton){
			printDebug("Button Draw is selected");
			//if (mapOPORD){		
			//	printDebug(stringOPORDName + " - " +  label + " " + 
			//			tempLocationsOPORD + " " +  shapeType + " " + locationCoords);
			//	drawOPORD(stringOPORDName, label, tempShapeOPORD, shapeType, shapeCoords);
			//}
			redrawing = true;
			saveTMP(false, false);
		} // End if drawButton
		
		// Erase Action		
		if(buttonAction.getSource() == eraseButton){
			printDebug("Button Erase is selected");
			
			// Remove All routeLayer from mapHandler which is drawn on the map
			if (mapOPORD){
				mapHandler.remove(routeLayerOPORD);
			}
			if (routeLayerIndex > 0){
				for (int i = 0; i < routeLayerIndex; i++){	
					mapHandler.remove(routeLayer[i]);
				}
			}
			routeLayerIndex = 0;	// set number of graphics to 0
		} // end if eraseButton
		
		//Test Action		
		if(buttonAction.getSource() == testVisButton){
			printDebug("Button Test Visualizer is selected");
		} // end if testButton
		
		//Test Action		
		if(buttonAction.getSource() == testEditorButton){
			printDebug("Button Test Editor is selected");
			printDebug(editor.getCursor().toString());
			printDebug(editor.getCursor().getName());
			printDebug(editor.getCursor().getType());
			printDebug("ComponentCount() : "+ editor.getComponentCount());
			printDebug(" Editor Name :"+ editor.getName());
			printDebug(" Editor UI :"+ editor.getUI().toString());	
			printDebug(" Editor Mouse Position : "+ editor.getBackground().toString());
			printDebug(" Editor centerPanel.getComponentCount : "+ 
        centerPanel.getComponentCount());
			printDebug(" Editor centerPanel.getComponentCount : "+ 
        splitPane.getLeftComponent().getMousePosition());
			printDebug(" Editor centerPanel.getComponentCount : "+ 
        splitPane.getLeftComponent().getMousePosition());
			printDebug(" Editor centerPanel.getComponentCount : "+ 
        centerPanel.getMousePosition());
			
			Point point = new Point();
			point = centerPanel.getMousePosition();
			printDebug(" Editor centerPanel getComponent at point : "+ 
        centerPanel.getComponentAt(point));
			printDebug(" Editor centerPanel components count ---- : "+ 
        centerPanel.countComponents());
			printDebug(" Editor editor component count ---------- : "+ 
        editor.getComponentCount());
			printDebug(" Editor centerPanel getComponent at point : "+ 
        currentDom.getEditor().getSelectedTreeNode().getVisualName());
			printDebug(" Editor centerPanel getComponent at point : "+ 
        currentDom.getEditor().getSelectedTreeNode().getLeafCount());
			printDebug(" Editor centerPanel getComponent at point : "+ 
        currentDom.getEditor().getSelectedTreeNode().getSiblingCount());
			printDebug(" Editor centerPanel getComponent at point : "+ 
        currentDom.getEditor().getSelectedTreeNode().getLeafCount());
			printDebug(" Editor Root Type Child  : " + editor.getRootType().getChild("Report"));
			
			editor.getRootType().getChild("StatusReport").setLabelColor(Color.cyan);
			editor.getRootType().getChild("StatusReport").setVisible(false);	
			printDebug(" Editor Root Type Child  : " +editor.getRootType().getChild("StatusReport").getDisplayValue());					
		} // end if testEditorButton
	}
	
	/**
	 * Create the Menu Bar of the BMLC2GUI Frame/Window 
   * (Widgets, Layout, and ActionListeners)
	 */
	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mainMenu = new JMenu("File");		// File Menu : New, Open, Exit...
		
    // Overall File Menu
		JMenu newMenu   = new JMenu("New         ");
		JMenu openMenu  = new JMenu("Open");
		JMenu pushMenu  = new JMenu("Push");
                
		// MSDL Menu
		JMenuItem newMSDL   = new JMenuItem("New  MSDL");
		JMenuItem openMSDL  = new JMenuItem("Open MSDL");
		JMenuItem pushMSDL  = new JMenuItem("Push MSDL");
/*
		// NATO OPORD Menu
		JMenuItem newOPORD  = new JMenuItem("New  CBML OPORD");
		JMenu openOPORD = new JMenu("Open CBML OPORD");
		JMenuItem openOPORDFileSystem  = new JMenuItem("File System");
		JMenuItem openOPORDWebService  = new JMenuItem("Web Service");		
		JMenuItem pushOPORD = new JMenuItem("Push CBML OPORD");
*/
		// CBML Order Menu
		JMenuItem newOrder  = new JMenuItem("New  CBML Order");
		JMenu openOrder = new JMenu("Open CBML Order");
		JMenuItem openOrderFileSystem  = new JMenuItem("File System");
		JMenuItem openOrderWebService  = new JMenuItem("Web Service");
		JMenuItem openOrderOldFS  = new JMenuItem("Demo Order");
		JMenuItem pushOrder = new JMenuItem("Push CBML Order");

		// IBML09 Order Menu
		JMenuItem newOrder09  = new JMenuItem("New  IBML09 Order");
		JMenu openOrder09 = new JMenu("Open IBML09 Order");
		JMenuItem openOrderFileSystem09  = new JMenuItem("File System");
		JMenuItem openOrderWebService09  = new JMenuItem("Web Service");
		JMenuItem pushOrder09 = new JMenuItem("Push IBML09 Order");
		
		// Reports Menu
		JMenu newReport  = new JMenu("New  CBML Report");
		JMenuItem generalReport  = new JMenuItem("General Status Report");
		JMenuItem positionReport  = new JMenuItem("Position Report");
		JMenuItem bridgeReport  = new JMenuItem("Bridge Report");
		JMenuItem mineReport  = new JMenuItem("Mine Report");
		JMenuItem natoReport  = new JMenuItem("Nato Report");
		JMenuItem spotReport  = new JMenuItem("Spot Report");
		JMenuItem trackReport  = new JMenuItem("Track Report");
		
    // IBML09 Reports
		JMenuItem newIBML09Report  = new JMenuItem("New  IBML09 Report");
		JMenu openIBML09Report  = new JMenu("Open IBML09 Report");
		JMenuItem generalReport09  = new JMenuItem("IBML09 General Status Report");
		JMenuItem taskReport09  = new JMenuItem("IBML09 Task Status Report");
		JMenuItem pushIBML09Report  = new JMenuItem("Push IBML09 Report");
		/*
		JMenuItem pullbridgeReport  = new JMenuItem("Bridge Report");
		JMenuItem pullmineReport  = new JMenuItem("Mine Report");
		JMenuItem pullnatoReport  = new JMenuItem("Nato Report");
		JMenuItem pullspotReport  = new JMenuItem("Spot Report");
		JMenuItem pulltrackReport  = new JMenuItem("Track Report");
		*/
		JMenu openReport = new JMenu("Open CBML Report");
		JMenuItem openReportFileSystem  = new JMenuItem("File System");
		JMenu openReportWebService  = new JMenu("Web Service");
		JMenuItem openReportWSById  = new JMenuItem("By Report ID");
		JMenuItem openReportWSList  = new JMenuItem("From ID List");
		JMenuItem openReportOldFS  = new JMenuItem("Demo General Status Report");
		JMenuItem reportDemo = new JMenuItem("Report Demo");
		JMenuItem pushReport = new JMenuItem("Push CBML Report");
		
		JMenuItem newDocument  = new JMenuItem("New  XML Document");
		JMenuItem openDocument = new JMenuItem("Open XML Document");
		JMenuItem saveDocument = new JMenuItem("Save");
		JMenuItem closeDocument = new JMenuItem("Close");
		JMenuItem printDocument = new JMenuItem("Print");
		JMenuItem exitOrder = new JMenuItem("Exit");
		
		
    // Overall File  Menu
		mainMenu.add(newMenu);
		mainMenu.add(openMenu);
		mainMenu.add(pushMenu);
		//mainMenu.addSeparator();
                
    // MSDL Menu
		newMenu.add(newMSDL);
		openMenu.add(openMSDL);
		pushMenu.add(pushMSDL);
		//mainMenu.addSeparator();
/*
		// OPORD Menu
		newMenu.add(newOPORD);	
		openMenu.add(openOPORD);
		openOPORD.add(openOPORDFileSystem);
		openOPORD.add(openOPORDWebService);
		pushMenu.add(pushOPORD);
		//mainMenu.addSeparator();
*/
		// CBML Order Menu
		newMenu.add(newOrder);	
		openMenu.add(openOrder);
		openOrder.add(openOrderFileSystem);
		//openOrder.add(openOrderWebService); needs WS
		//openOrder.add(openOrderOldFS);  needs work
		pushMenu.add(pushOrder);
		//mainMenu.addSeparator();
		
		//Order IBML09 Menu		
		newMenu.add(newOrder09);	
		openMenu.add(openOrder09);
		openOrder09.add(openOrderFileSystem09);
		//openOrder09.add(openOrderWebService09); needs WS
		pushMenu.add(pushOrder09);
		//mainMenu.addSeparator();	
		
		//Report Menu
		newMenu.add(newReport);
		newReport.add(generalReport);
		//newReport.add(positionReport); all of these need work
		//newReport.add(bridgeReport);
		//newReport.add(mineReport);
		//newReport.add(natoReport);
		//newReport.add(spotReport);
		//newReport.add(trackReport);
		
		openMenu.add(openReport);
		openReport.add(openReportFileSystem);
		//openReport.add(openReportWebService); needs work
		//openReportWebService.add(openReportWSById); needs WS
		//openReport.add(openReportOldFS);  needs demo
		pushMenu.add(pushReport);
		//openMenu.add(reportDemo); Report.java needs rework
		//mainMenu.addSeparator();
		
		newMenu.add(newIBML09Report);
		openMenu.add(openIBML09Report);
		openIBML09Report.add(generalReport09);
		//openIBML09Report.add(taskReport09); Report09 needs reworked for this
		pushMenu.add(pushIBML09Report);
		
		mainMenu.addSeparator();
		
		//newMenu.add(newDocument);   needs work
		//openMenu.add(openDocument); needs work
		mainMenu.add(saveDocument);
		mainMenu.addSeparator();
		mainMenu.add(closeDocument);
		mainMenu.addSeparator();
		
		mainMenu.add(printDocument);
		mainMenu.addSeparator();
		mainMenu.add(exitOrder);
		menuBar.add(mainMenu);
		
		//MSDL Menu actions
		// Open MSDL file from File System
		openMSDL.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open MSDL from File System..............");
			    MSDL msdl = new MSDL();
			    msdl.openMSDL_FS();
			}
		});
		
		// New MSDL file from File System
		newMSDL.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New MSDL .............................");
				MSDL msdl = new MSDL();
			    msdl.newMSDL();
			}
		});
		//Push MSDL
		pushMSDL.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Push MSDL .............................");
			    MSDL msdl = new MSDL();
			    msdl.pushMSDL();
			}
		});
    /*
		//OPORD Menu actions
		newOPORD.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New CBML OPORD .............................");
				
				//CBMLOpord op = new CBMLOpord();
			  //op.newCBMLOPORD();
			  
				// replaced GMU Light CBML with CBML for IITSEC 2011
				Opord op = new Opord();
			    op.newOPORD();
			   
			}
		});
		openOPORDFileSystem.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open CBML OPORD from File System..............");
			    
			    //CBMLOpord op = new CBMLOpord();
			    //op.openCBMLOPORD_FS();
			    
			    // replaced GMU Light CBML with CBML for IITSEC 2011
			    Opord op = new Opord();
			    op.openOPORD_FS();
			    
			}
		});
		openOPORDWebService.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open CBML OPORD from Web Service................");	 
			    try {
			    	ws.openOPORD_IBML_CBML_WS();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}		    
			}
		});
		pushOPORD.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Push CBML OPORD .............................");
			    
			    //CBMLOpord op = new CBMLOpord();
			    //op.pushCBMLOPORD();
			    
			    // replaced GMU Light CBML with CBML for IITSEC 2011
			     Opord op = new Opord();
			     op.pushOPORD_C2Core();
			     
			}
		});
*/ // end OPORD action listeners

		// IBML Order
		newOrder.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New CBML Order .............................");
        Order o = new Order();
				o.newOrder();
			}
		});
		openOrderOldFS.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
          printDebug("Open CBML Order from File System..........................");
			    Order o=new Order();
			    o.openOrder();
			}
		});
                
    // CBML
		openOrderFileSystem.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
          printDebug("Open CBML Order from File System..............");
			    Order o = new Order();
			    o.openOrderFS();
			}
		});
		openOrderWebService.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
         if(checkCanDo())return;
         printDebug("Open CBML Order from Web Service................");
			    try {
					ws.openOrderWS();
				} catch (IOException e1) {
					e1.printStackTrace();
				}		    
			}
		});
		pushOrder.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
          printDebug("Push CBML Order .............................");
			    Order o = new Order();
			    o.pushOrder();
			}
		});
		
		// IBML Order09
		newOrder09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New IBML09 Order .............................");
				Order09 order09 = new Order09();
				order09.newOrder09();
			}
		});
		
		openOrderFileSystem09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open IBML09 Order from File System..............");
			    Order09 order09 = new Order09();
			    order09.openOrderFS09();
			}
		});
		openOrderWebService09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			  printDebug("Open IBML09 Order from Web Service................");	 
			  try {
					ws.openOrderWS();
				} catch (IOException e1) {
					e1.printStackTrace();
				}		    
			}
		});
		pushOrder09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Push IBML 09 Order .............................");
			    Order09 order09 = new Order09();
			    order09.pushOrder09();
			}
		});
		
		// Reports
		newReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report .............................");
			}
		});	
		generalReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  generalReport .............................");
				Report r= new Report();
				r.newReport("GeneralStatus");
			}
		});
		positionReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Position Report .............................");
				Report r= new Report();
				r.newReport("PositionStatus");
			}
		});
		bridgeReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Bridge Report .............................");
				Report r= new Report();
				r.newReport("Bridge");
			}
		});
		mineReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Min Ob Report .............................");
				Report r= new Report();
				r.newReport("MinOb");
			}
		});
		spotReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Spot Report .............................");
				Report r= new Report();
				r.newReport("Spot");
			}
		});
		natoReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Nato Spot Report .............................");
				Report r= new Report();
				r.newReport("NatoSpot");
			}
		});
		trackReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New Report  Track Report .............................");
				Report r= new Report();
				r.newReport("Track");
			}
		});
		openReportFileSystem.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open Report from File System................");
			    try {
			    	Report r= new Report();
					r.openReportFS();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		generalReport09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open IBML09 Report from File System................");
			    try {
			    	Report09 r= new Report09();
					r.openReportFS_General09();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		taskReport09.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open IBML09 Report from File System................");
			    try {
			    	Report09 r= new Report09();
					r.openReportFS_Task09();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		pushIBML09Report.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Push IBML09 Report to Web Service................");
			    try {
                                    Report09 r= new Report09();
                                    r.pushReport09();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		openReportOldFS.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open Report from File System - MSG Schema ................");
			    Report r= new Report();
				r.openReport();
			}
		});
		openReportWSById.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open Report from Web Service................");
			    String reportID ="";			    // Enter report ID to pull
			    reportID = 
            JOptionPane.showInputDialog(
              null, 
              "Enter Report ID ", 
              "Open a Report from Web Service",
              JOptionPane.QUESTION_MESSAGE);
			    try {
			    	ws.openReportWS(reportID);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		openReportWSList.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
                            if(checkCanDo())return;
			    printDebug("Open Report from Web Service................");
			    String reportID ="";
				String[] reportIDArray = new String[100];
				String afterTime = "20060101000000.000";
			    try {			    // get ID from a list using getReportIDs method
			    	reportID = 
              JOptionPane.showInputDialog(
                null, 
                "Date Time Format 20060101000000.000" ,
                "Pull Report IDs After Time",
                JOptionPane.QUESTION_MESSAGE);
			    	reportIDArray = getReportIDs(afterTime);
			    	if (reportIDArray[0] == "00000000000000000000"){
			    		JOptionPane.showMessageDialog(
                null, 
                "Unable to get List of Report ID's ", 
                "Web Service Error",
                JOptionPane.QUESTION_MESSAGE);
			    	}
			    	else {
			    		reportID = 
                (String) JOptionPane.showInputDialog(
                  null, 
                  "Select Report ID ", 
                  "Open a Report from Web Services",
                  JOptionPane.QUESTION_MESSAGE,null, 
                  reportIDArray, null);
			    		ws.openReportWS(reportID);
			    	}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		pushReport.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Push Report .............................");
			    Report rep= new Report();
			    rep.pushReport();
			}
		});
		
		reportDemo.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				long x;
				long y=0;
				
			    printDebug("Report Demo .............................");
				Report rep= new Report();
			    try {
			    	rep.openReportFSDemo(
              guiFolderLocation + 
              "\\BMLC2GUI\\Samples\\Reports\\Demo\\GeneralStatusReport1.xml"
            );
			    	
				} catch (IOException e1) {
					e1.printStackTrace();
				} //end try
				
				x = System.currentTimeMillis();
				y = x + 5000;		// 	Wait for 5 seconds	
				while (System.currentTimeMillis() < y ){}  // Do Nothing		//Just wait
				
				try {
					rep.openReportFSDemo(
            guiFolderLocation +
            "\\BMLC2GUI\\Samples\\Reports\\Demo\\GeneralStatusReport2.xml");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} //end try
		    	
			}
		});
		
		newDocument.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("New XML Document...........................");
				newDocument();
			}
		});
		openDocument.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Open XML Document .............................");
			    openDocument();
			}
		});
		saveDocument.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Save XML Document .............................");
			    saveDocument(); 
			}
		});
		closeDocument.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Close the Current editor Document ..............");
			    closeDocument();
			}
		});
		printDocument.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Print XML Document .............................");
			    printBmlDoc();
			}
		});
		exitOrder.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Exit BML GUI .............................");
			    System.exit(0);
			}
		});		
		
		// Edit Menu : Serialize, Validate
		JMenu editMenu = new JMenu("Edit");
		JMenuItem serializeOrder  = new JMenuItem("Serialize Order");
		JMenuItem validateOrder = new JMenuItem("Validate Order");
		editMenu.add(serializeOrder);
		editMenu.addSeparator();
		editMenu.add(validateOrder);
		menuBar.add(editMenu);
		serializeOrder.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Serialize Order .............................");
				serializeBmlDoc();
			}
		});
		validateOrder.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Validate Order .............................");
			    validateBmlDoc();
			}
		});

		// Configuration Menu : server and domain name setup c2mlguiconfig.xml
		JMenu configMenu = new JMenu("Config");
		JMenuItem loadConfig  = new JMenuItem("Load");
		JMenuItem saveConfig = new JMenuItem("Save");
		configMenu.add(loadConfig);
		configMenu.add(saveConfig);
		
		menuBar.add(configMenu);
		loadConfig.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("load Config File .............................");
				openConfig();
			}
		});
		saveConfig.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("Save Config File .............................");
			    saveConfig();
			}
		});
		
		// View Menu : Styles
		JMenu styleMenu = new JMenu("Editor Style");
		JMenuItem defaultStyle  = new JMenuItem("Default Style");
		JMenuItem viewStyle1 = new JMenuItem("Tab Style");
		JMenuItem viewStyle2 = new JMenuItem("Serial Style");
		styleMenu.add(defaultStyle);
		styleMenu.add(viewStyle1);
		styleMenu.add(viewStyle2);
		menuBar.add(styleMenu);
		defaultStyle.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Default Style .............................");
			
			}
		});
		viewStyle1.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("View Style 1 .............................");
	
			}
		});
		viewStyle2.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    printDebug("View Style 2 .............................");
			}
		});		
		
		// Map Menu : 
		JMenu mapMenu = new JMenu("Map");
		JMenuItem mapOptions  = new JMenuItem("Options");
		mapMenu.add(mapOptions);
		
		// Bookmarks
		JMenu mapViews  = new JMenu("Views");
		JMenuItem mapView1  = new JMenuItem("World");
		JMenuItem mapView2  = new JMenuItem("North America");
		JMenuItem mapView3  = new JMenuItem("South America");
		JMenuItem mapView4  = new JMenuItem("Europe");
		JMenuItem mapView5  = new JMenuItem("Africa");
		JMenuItem mapView6  = new JMenuItem("Asia");
		JMenuItem mapView7  = new JMenuItem("Australia");
		JMenuItem mapView8  = new JMenuItem("Azerbaijan");
		
		// Bookmarks
		mapMenu.add(mapViews);
		mapViews.add(mapView1);
		mapViews.add(mapView2);
		mapViews.add(mapView3);
		mapViews.add(mapView4);
		mapViews.add(mapView5);
		mapViews.add(mapView6);
		mapViews.add(mapView7);
		mapViews.add(mapView8);
		
		menuBar.add(mapMenu);
		mapView1.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 1 .............................");
				mapBean.setCenter(new LatLonPoint(0.0f, 0.0f));
				mapBean.setScale(200000000f); 				// Set the map's scale 
			}
		});
		mapView2.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 2 .............................");
				mapBean.setCenter(new LatLonPoint(50.0f, -100f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView3.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 3 .............................");
				mapBean.setCenter(new LatLonPoint(-10.0f, -60f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView4.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 4 .............................");
				mapBean.setCenter(new LatLonPoint(50.0f, 30f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView5.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 5 .............................");
				mapBean.setCenter(new LatLonPoint(10.0f, 20f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView6.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 6 .............................");
				mapBean.setCenter(new LatLonPoint(40.0f, 80f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView7.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 6 .............................");
				mapBean.setCenter(new LatLonPoint(-20.0f, 140f));
				mapBean.setScale(80000000f);				// Set the map's scale 
			}
		});
		mapView8.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Map View 6 .............................");
				mapBean.setCenter(new LatLonPoint(40.0f, 48.9f));
				mapBean.setScale(3000000f);				// Set the map's scale 
			}
		});
		
		// Language Menu : 
		JMenu languageMenu = new JMenu("Languages");
		JMenuItem languageEnglish  = new JMenuItem("English");
		JMenuItem languageGerman  = new JMenuItem("German");
		JMenuItem languageFrench  = new JMenuItem("French");
		JMenuItem languageItalian  = new JMenuItem("Italian");
		languageMenu.add(languageEnglish);
		languageMenu.add(languageGerman);
		languageMenu.add(languageFrench);
		languageMenu.add(languageItalian);
		
		menuBar.add(languageMenu);
		languageEnglish.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Language Option English.............................");
				languageEnglish();	
			}
		});
		languageGerman.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Language Option German  .............................");
				languageGerman();		
			}
		});
		languageFrench.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Language Option French  .............................");
				languageFrench();
			}
		});
		languageItalian.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Language Option Italian  .............................");
				languageItalian();
			}
		});
		
		// Help Menu : 
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem milstd2525 = new JMenuItem("MILSTD2525b");
		JMenuItem jaxfrontAbout = new JMenuItem("JAXFront");
		JMenuItem openmapAbout = new JMenuItem("OpenMap");	
		helpMenu.add(about);
		helpMenu.addSeparator();
		helpMenu.add(milstd2525);
		helpMenu.addSeparator();
		helpMenu.add(jaxfrontAbout);
		helpMenu.addSeparator();
		helpMenu.add(openmapAbout);
		menuBar.add(helpMenu);

		about.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("About BMLC2GUI .............................");
				JTextArea jta = new JTextArea("George Mason University C4I Center\n"
                                        + "Mark Pullen and Mohammad Ababneh\n"
                                        + "mpullen@c4i.gmu.edu\n"
                                        + "mababneh@c4i.gmu.edu\n"
                                        + "2009-2014");
				 JOptionPane.showMessageDialog(null, jta , "About BMLC2GUI ",JOptionPane.INFORMATION_MESSAGE,null);
			}
		});
		milstd2525.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printDebug("Help milstd2525 .............................");								
				Dimension di = new Dimension(100,100);
				ImageIcon ii = new ImageIcon(guiFolderLocation + delimiter + "milstd2525b" + delimiter + "milStd2525_png" + delimiter + "SHAPMFUM----***.png");
                                PNGSymbolImageMaker pngsim = new PNGSymbolImageMaker(guiFolderLocation + delimiter + "milstd2525b" + delimiter + "milStd2525_png"); //\\Referenced Libraries\\
			    SymbolReferenceLibrary srl = new SymbolReferenceLibrary(pngsim);    
			    ii = srl.getIcon("SHAPMFT-----***", di);    
			    printDebug("The icon height is : " + ii.getIconHeight());
			    printDebug("The icon width is : " + ii.getIconWidth());
			     
			    //Display Symbol Chooser
			    SymbolChooser sc = new SymbolChooser(srl);
		        SymbolChooser.showDialog(editorMapPanel,"Mil Std 2525b Symbols" , srl, "shapmfum-------");
		        printDebug("The selected Code is : " +sc.getCode());
			}
		});
		setJMenuBar(menuBar);		// Set JMenuBar
	} // end of initmenu method
	
	/**
	 * Create a MapBean and a MapHandler, which locates and places objects.
	 * 
	 * The BasicMapPanel automatically creates many default components, including the MapBean and the MapHandler.
	 */
	public void initMap(){ 
		c2mlProps = new Properties();				//Properties file
    loadResource(c2mlResources, c2mlProps);		// Reading the Properties file 
        
    // Initialize an array of Route Layers to hold BML Documents Graphics
    routeLayer = new RouteLayer[RouteLayerArraySize];
    mapPanel = new BasicMapPanel();		    	// OpenMap Map Panel
		mapHandler = mapPanel.getMapHandler();		// Get the default MapHandler the BasicMapPanel created.
		mapBean = mapPanel.getMapBean();			// Get the default MapBean that the BasicMapPanel created.	
		mapBean.setCenter(new LatLonPoint(35.0f, 58.0f));
		mapBean.setScale(24000000f); 		    	// Set the map's scale 1:120 million

		// Create and add a LayerHandler to the MapHandler. The LayerHandler
		// manages Layers, whether they are part of the map or not.
		// layer.setVisible(true) will add it to the map. The LayerHandler has
		// methods to do this, too. The LayerHandler will find the MapBean in
		// the MapHandler.	
        printDebug("Creating Layers...");
        Layer[] layers = getLayers(c2mlProps);

        // Use the LayerHandler to manage all layers, whether they are
        // on the map or not. You can add a layer to the map by
        // setting layer.setVisible(true). 
        layerHandler = new LayerHandler();
       
        // add the layers from the property file and do not display them
        // the user can select to them at run time
        for (int i = 0; i < layers.length-2; i++) {
            layers[i].setVisible(false);
            layerHandler.addLayer(layers[i]);
        }
        
        // add the last two layers of the shape layers: graticule and political and display them
        for (int i = layers.length-2; i < layers.length; i++) {
            layers[i].setVisible(true);
            layerHandler.addLayer(layers[i]);
        }
        mapHandler.add(layerHandler);
        printDebug("Done creating shape layers...");
		mapHandler.add(new ProjectionStack());
		mapHandler.add(new ProjectionStackTool());
		mapHandler.add(new InformationDelegator());
		mapHandler.add(new OMGraphicDeleteTool());	//	Tool Bar Button to delete graphics
		mapHandler.add(new LayersPanel());		    // LayersPanel should be able to receive Location drawings later

		// Add Mouse handling objects. The MouseDelegator manages the
		// MouseModes, controlling which one receives events from the
		// MapBean. The active MouseMode sends events to the layers
		// that want to receive events from it. The MouseDelegator
		// will find the MapBean in the MapHandler, and hook itself up
		// to it.
		mapHandler.add(new MouseDelegator());
		mapHandler.add(new SelectMouseMode());	// SelectMouseMode

		mapHandler.add(new NavMouseMode());		// Adding NavMouseMode first makes it active.
		mapHandler.add(new MouseModeButtonPanel());
		mapHandler.add(new DistanceMouseMode());// DistanceMouseMode
		mapHandler.add(new NullMouseMode());	// NullMouseMode
		mapHandler.add(new PanMouseMode());		// PanMouseMode
		OMToolSet omts = new OMToolSet();		// Create the directional and zoom control tool
		ToolPanel mapToolBar = new ToolPanel();	// Create an OpenMap toolbar
		
		mapHandler.add(omts);
		mapHandler.add(mapToolBar);

		JLabel LatLonLabel = new JLabel("test output");
		mapHandler.add(LatLonLabel);
		
		// add the mapPanel/Bean to the original split Frame
		splitPane.setRightComponent((Component) mapPanel);
		
		// Add popup menu to capture coordinates and send them to the editor		
		final JPopupMenu gdcPopup = new JPopupMenu();
		final JMenuItem closestLatLon = new JMenuItem("closestLatLon......................................");
		final JMenuItem closestLatLonB = new JMenuItem("closestLatLonB......................................");
		final JMenuItem nextLatLon = new JMenuItem("nextLatLon.............          ");
		final JMenuItem addLatLon = new JMenuItem("addLatLon.............          ");
		final JMenuItem delLatLon = new JMenuItem("delLatLon.............          ");

		gdcPopup.add(closestLatLon);
		gdcPopup.add(nextLatLon);
		gdcPopup.add(addLatLon);
		gdcPopup.add(delLatLon);
		final JPopupMenu gdcPopupB = new JPopupMenu();
		gdcPopupB.add(closestLatLonB);

		//////MAIN POPUP CHOICES
		//Select closest reference point xx.xxxx xx.xxxx
		//Select next reference point yy.yyyy yy.yyyy
		//Add point zz.zzzz zz.zzzz after reference
		//Delete current reference point vv.vvvv vv.vvvv
		

		closestLatLon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				selectedCoord = highlightedCoord;
				printDebug("Lat Lon menu item selected " + selectedCoord + " of " + numCoords);
				printDebug("Popup Menu Item LatLon Cord : " + e.getActionCommand());
			}
		});
		closestLatLonB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				selectedCoord = highlightedCoord;
				printDebug("Lat Lon menu item selected " + selectedCoord + " of " + numCoords);
				printDebug("Popup Menu Item LatLon Cord : " + e.getActionCommand());
			}
		});
		nextLatLon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				selectedCoord++;
				printDebug("Next Lat Lon menu item selected " + selectedCoord + " of " + numCoords);
				printDebug("Popup Menu Item LatLon Cord : " + e.getActionCommand());
			}
		});
		addLatLon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				printDebug("Add Lat Lon menu item selected " + lattmp + " " + lontmp);
				printDebug("Popup Menu Item LatLon Cord : " + e.getActionCommand());
				redrawing = true;
				saveTMP(true, false);
			}
		});
		delLatLon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				printDebug("Add Lat Lon menu item selected " + lattmp + " " + lontmp);
				printDebug("Popup Menu Item LatLon Cord : " + e.getActionCommand());
				redrawing = true;
				saveTMP(true, true);
			}
		});
		mapBean.addMouseListener(new MouseAdapter(){
			//Motif Environment
			public void mousePressed(MouseEvent e){
				showPopup(e);
				//printDebug("Click x = " + e.getX() + " ....          Click y = " + e.getY());
				//MapMouseEvent mMouseEvent = new MapMouseEvent(null, e);
				//printDebug(" LAT LON " + mMouseEvent.getLatLon());
			}
			//Windows Environment
			public void mouseReleased(MouseEvent e){
				showPopup(e);	
			}
			
			private void showPopup(MouseEvent evt) {		
				if (evt.isPopupTrigger()){
					if((selectedCoord < numCoords - 1) & selectedCoord != -1){
  					 gdcPopup.show(evt.getComponent(), evt.getX(), evt.getY());
					}
					else{
	  			     gdcPopupB.show(evt.getComponent(), evt.getX(), evt.getY());
					}
					printDebug("Click x = " + evt.getX() + " ....          Click y = " + evt.getY());
					MapMouseEvent mMouseEvent = new MapMouseEvent(null, evt);
					printDebug(" LAT LON " + mMouseEvent.getLatLon());	
					String latValueString = "", lonValueString = "";	
					latValueString = latValueString + mMouseEvent.getLatLon().getLatitude();
					lonValueString = lonValueString + mMouseEvent.getLatLon().getLongitude();

			        int x = evt.getX();
			        int y = evt.getY();
			        int j=0;

			        //if(selectedCoord < numCoords - 1){nextCoord = selectedCoord + 1;}
			        LatLonPoint llp = null;
			        if (evt.getSource() instanceof MapBean) {
			            llp = ((MapBean) evt.getSource()).getProjection().inverse(x, y);
			            Clipboard systemClipboard = Toolkit.getDefaultToolkit()
			                    .getSystemClipboard();
                  
			            // format, UTM convert llp here
			            int lat_i = llp.toString().indexOf("lat=");
			            int lon_i = llp.toString().indexOf("lon=");
			            lattmp = llp.toString().substring(lat_i + 4, lat_i + 11);
			            lontmp = llp.toString().substring(lon_i + 4, lon_i + 11);
			            String latlon = lattmp + " " + lontmp;
			            j = closestTo(lattmp,lontmp);
			            highlightedCoord = j;
			            if(j < numCoords - 1){nextCoord = j + 1;}
			            printDebug("closest to " + j + "  " + bmlLatCoords[j] + " " + bmlLonCoords[j]);
			            printDebug("sending to clipboard: " + latlon);

			            Transferable transferableText = new StringSelection(latlon);
			            systemClipboard.setContents(transferableText, null);
			        }
			        closestLatLon.setLabel("Select closest reference point "+bmlLatCoords[j]+" "+bmlLonCoords[j]);
			        closestLatLonB.setLabel("Select closest reference point "+bmlLatCoords[j]+" "+bmlLonCoords[j]);
			        nextLatLon.setLabel("Select next reference point "+bmlLatCoords[selectedCoord+1]+" "+bmlLonCoords[selectedCoord+1]);
			        addLatLon.setLabel("Add point "+lattmp+" "+lontmp+" after reference");
			        delLatLon.setLabel("Delete selected reference point "+bmlLatCoords[j]+" "+bmlLonCoords[j]);
			        
				}
			}
			
			
		});
	} // End of initMap method

	public void removeLayer(){} 

	/**
	 * Establish and draw Route Layer
	 */
	public void drawLocation(String[] stringArray, String bmlDocumentType){

    printDebug("in BMLC2GUI-drawLocation method to Print Location on a new RouteLayer");
		
    // initialize the route layer containing points, areas and lines
		if (stringArray.length != 1)  {
			// create a graph object on the map
			if(mapGraph & redrawing){
				//printDebug("---------++++===============+++++---------");
				for(int r = 0; r < routeLayerIndex; r++){
					routeLayer[r].clearGraphics();
				}
			}
			routeLayer[routeLayerIndex] = new RouteLayer(stringArray, bmlDocumentType );
			mapGraph = true;			// a graph is there
		}
		else {
			routeLayer[routeLayerIndex] = new RouteLayer();
		}
		routeLayer[routeLayerIndex].setName(bmlDocumentType); //"Location Layer");
		routeLayer[routeLayerIndex].setVisible(true);
		mapHandler.add(routeLayer[routeLayerIndex]);
		
		// check if the number of graphical objects is too high
		// The map can display up to 100 objects, so warn the user at number 95
		if (routeLayerIndex == 98){
			JOptionPane.showMessageDialog(null, "The number of Graphical Objects on the map is too high. " +
					"\n Please click the Erase button before opening the next document." +
					"\n Or all graphics will be erased when the maximum is reached",
					"Map Graphics Warning",	JOptionPane.WARNING_MESSAGE);
		}
		
		// Force a Map cleanup when the maximum number of graphics is on the map
		if (routeLayerIndex == 99){
			for (int i = 0; i <= routeLayerIndex; i++){
				mapHandler.remove(routeLayer[i]);
			}
			routeLayerIndex = 0;			// set number of graphics to 0
		}
		routeLayerIndex++;		//Increment the number of route layers
    
	} // end drawLocation()
	
	public void drawOPORD(String sOPORDName, String sShapeName, String[] tempShape, String shapeType, int shapeCoords){
		mapGraph = true;
		printDebug("--------------------------------------====================--------------drawOPORD-----");
		if (!mapOPORD) {
			routeLayerOPORD = new RouteLayer();
			routeLayerOPORD.setName(sOPORDName); //bmlDocumentType
			routeLayerOPORD.setVisible(true);
			mapHandler.add(routeLayerOPORD);
			mapOPORD = true;
		}
		routeLayerOPORD.createOPORDGraphics(sShapeName, tempShape, shapeType, shapeCoords);
	}
	
	
	/**
	 * Various Frame methods (no-op)
	 */
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void showHelp(HelpEvent event) {}
	public void itemStateChanged(ItemEvent arg0) {}
	
	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	/**
	 * Create a blank BML with just an xsd
	 */
	private void newDocument() {
		String root = null;
		releaseXUICache();
		
		// create a method to deal with drawing unknown documents
		bmlDocumentType ="Unknown";
		documentTypeLabel.setText(bmlDocumentType);
		
		// Report Schema
		JFileChooser xsdFc = new JFileChooser(guiFolderLocation + delimiter);
		xsdFc.setDialogTitle("Enter Schema XSD file name");
		xsdFc.showOpenDialog(BMLC2GUI.this);
    if(xsdFc.getSelectedFile() == null)return;
		URL url = URLHelper.getUserURL(xsdFc.getSelectedFile().toURI().toString());
		URL xmlUrl = null; 		// Empty XML
		URL xuiUrl = null;  // XUI Style // Default View
		initDom("default-context", url, xmlUrl, xuiUrl, root);
	}

	/**
	 * Open an existing BML (XML Document)
	 */
	private void openDocument() {	
		String orderString = new String();
		String[] orderStringArray;
		String schemaFileName=""; // schema file name
		String schemaFile="";  // schema file name and location; full path // make it URI
		String xmlFileString ="";
		String schemaFileString ="";
		
		// create a method to deal with drawing unknown documents
		bmlDocumentType ="Unknown";	
		documentTypeLabel.setText("Unknown Document");	
		releaseXUICache();
		
		// XML file
		JFileChooser xmlFc = new JFileChooser(guiFolderLocation + delimiter);
		xmlFc.setDialogTitle("Enter XML file name");
		xmlFc.showOpenDialog(BMLC2GUI.this);
    if(xmlFc.getSelectedFile() == null)return;
		xmlFileString = xmlFc.getSelectedFile().toURI().toString();
		URL xmlUrl = URLHelper.getUserURL(xmlFileString);
		
		// Schema File XSD
		JFileChooser xsdFc = new JFileChooser(guiFolderLocation + delimiter);
		xsdFc.setDialogTitle("Enter Schema XSD file name");
		xsdFc.showOpenDialog(BMLC2GUI.this);
		URL url = URLHelper.getUserURL(xsdFc.getSelectedFile().toURI().toString());	
		URL xuiUrl = null;
		
    // Generate the swing GUI
		drawFromXML(
      "default-context", 
      url, 
      xmlUrl, 
      xuiUrl, 
      root, 
      bmlDocumentType,
      null,
      null,
      null,
      null,
      null
    );
	}

	/**
	 * Save an Edited (optionally validated) BML (XML Document)
	 */
	private void saveDocument() {	
		printDebug("Save the current XML Document ");
		String xmlFileString ="";
		JFileChooser xmlFc = new JFileChooser(guiFolderLocation + delimiter);
		xmlFc.setDialogTitle("Enter XML file name to save");
		xmlFc.showSaveDialog(BMLC2GUI.this);
    if(xmlFc.getSelectedFile() == null)return;
		xmlFileString = xmlFc.getSelectedFile().toString();
		
		//set temporary values to actual targets
		tmpFileString = xmlFileString;
		tmpUrl = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString());
		
		//hide new temporary values
		URL tmpUrl2 = URLHelper.getUserURL(xmlFc.getSelectedFile().toURI().toString() + "(tmp)");
		String tmp2 = xmlFc.getSelectedFile().toString() + "(tmp)";

		//save document
		saveTMP(false, false);

		//set new temporary values
		tmpFileString = tmp2;
		tmpUrl = tmpUrl2;
	}
	
	/**
	 * Save a Temporary (optionally validated) BML (XML Document)
	 */
	private void saveTMP(boolean addLatLon, boolean arg1) {	
		xmlUrl = tmpUrl;
		File file = new File(tmpFileString);
		
		try {
			//currentDom.saveAs(file);
			//printDebug(currentDom.serialize().toString());
			String filedata = currentDom.serialize().toString();
			//printDebug(filedata);
			filedata = moveLatLon(filedata);
			if(addLatLon){
				printDebug("modifying file for "+lattmp+" "+lontmp);
				filedata = addPointToFile(filedata, arg1);
			}
			printDebug("Saving to " + tmpFileString);
			BufferedWriter out = new BufferedWriter(new FileWriter(tmpFileString));
			out.write(filedata);
			out.close();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		printDebug("refreshing a -------------------------- " + bmlDocumentType);
		if(bmlDocumentType == "OPORD"){
	      //PULL DRAW OPORD INTO BMLC2GUI.java
	      drawOPORD_FS(xsdUrl, xmlUrl, xuiUrl, root);
		}
		else{
		  drawFromXML(
        "default-context", 
        xsdUrl, 
        xmlUrl, 
        xuiUrl, 
        root, 
        bmlDocumentType,
        null,
        null,
        null,
        null,
        null
      );
		}
	}
	
	private String moveLatLon(String fileIn){
		int index1, index2, index3, pad, pad0=13, padA = 42, padB = 46, latpad = 14, lonpad = 15;
		String latlong="<bml:LatLong>",cutOut="<bml:LatLong/>",lat="<bml:Latitude>",lon="<bml:Longitude>";
		String latlongB="<C_BML:LatLong",cutOutB="<C_BML:LatLong/>",latB="<C_BML:LatitudeCoordinate>",lonB="<C_BML:LongitudeCoordinate>";

		//printDebug("----------------------------------------------------------------");
		String cutStr="",latPiece="",lonPiece="",currentLat="",currentLon="",currentLatLon="",newLatLon="";
	    index1 = fileIn.indexOf(latlong, 1);
	    if(index1 == -1){
	    	latlong = latlongB;   cutOut = cutOutB;
	    	lat = latB;           lon = lonB;
	    	padA = padB;          pad0 = 15;
	    	latpad = 26;          lonpad = 27;
	    	printDebug("Switched to Opord standards--------------------------");
	    }
		index1 = 0;
		while ((index1 = fileIn.indexOf(latlong, index1+1)) > 0){
			pad = pad0; // for deletion (default)
			if((index2 = fileIn.indexOf(cutOut, index1)) != index1){
				//split coords
				latPiece = fileIn.substring(index1 + pad, index1 + pad + 7);
				lonPiece = fileIn.substring(index1 + pad + 8, index1 + pad + 15);
				//printDebug(latPiece + " " + lonPiece + "==========================");
				
				//get lat and long strings
				index2 = fileIn.lastIndexOf(lon, index1);
				index3 = fileIn.indexOf("<", index2+1);
				currentLon = fileIn.substring(index2 + lonpad, index3);
				index2 = fileIn.lastIndexOf(lat, index1);
				index3 = fileIn.indexOf("<", index2+1);
				currentLat = fileIn.substring(index2 + latpad, index3);
				//printDebug(currentLat + " " + currentLon + "========--=========");
				index2 = fileIn.lastIndexOf("\n", index2-1); 
				
				//replace lat and replace lon
				currentLatLon = fileIn.substring(index2, index1);
				newLatLon = currentLatLon;
				newLatLon = newLatLon.replace(currentLat,latPiece).replace(currentLon, lonPiece);
                fileIn = fileIn.replace(currentLatLon, newLatLon);
				pad = padA; // for deletion
			}
			index2 = fileIn.lastIndexOf("\n",index1);
			cutStr = fileIn.substring(index2, index1 + pad);
			fileIn = fileIn.replace(cutStr, "");
			
		}		
        //printDebug(fileIn);
		return fileIn;
	}
	
	private String addPointToFile(String fileIn, boolean del){
		//printDebug("START ADD POINT based on selectedCoord "+selectedCoord+" "+bmlLatCoords[selectedCoord]+" "+bmlLonCoords[selectedCoord]);
		//printDebug(fileIn);
		//printDebug("--------------------------");
		int index1,index2,pad=13,ii=0;
		
		//Locate selectedCoord
		String latSearchA = "<bml:Latitude>"+bmlLatCoords[selectedCoord].trim()+"</bml:Latitude>";
		String lonSearchA = "<bml:Longitude>"+bmlLonCoords[selectedCoord].trim()+"</bml:Longitude>";
		//String latSearchB = "<C_BML:LatitudeCoordinate>"+bmlLatCoords[selectedCoord].trim()+"</C_BML:LatitudeCoordinate>";
		//String lonSearchB = "<C_BML:LongitudeCoordinate>"+bmlLonCoords[selectedCoord].trim()+"</C_BML:LongitudeCoordinate>";
		//String parentSearchA = "<bml:Coords>";
		//String parentSearchB = "<C_BML:Location>";
		//String parentSearchB1 = "C_BML:Waypoint";
		
		//String parentSearch = parentSearchA;
		String latSearch = latSearchA;
		String lonSearch = lonSearchA;
		index1 = fileIn.indexOf(latSearch);
		index2 = fileIn.indexOf(lonSearch);
		//if(index1 == -1){
			//latSearch = latSearchB;
			//lonSearch = lonSearchB;
			//index1 = fileIn.indexOf(latSearch);
			//index2 = fileIn.indexOf(lonSearch);			
		//}
		while(Math.abs(index2 - index1) > 110 & ii < 20){
			if(index2 > index1){index1 = fileIn.indexOf(latSearch,index1+1);}
			else{index2 = fileIn.indexOf(lonSearch,index2+1);}
			printDebug("searching "+index1+" "+index2+" "+ii);
			printDebug(fileIn.substring(index1,index1+14));
			ii++;
		}
		//Define bounds of xml Block
		index1 = fileIn.lastIndexOf("<bml:Coords>", index1);
        if(index1 == -1 || (index2-index1) > 400){
        	index1 = fileIn.lastIndexOf("<bml:WhereLocation>", index2);
        	index1 = fileIn.lastIndexOf("\n", index1);
        	index2 = fileIn.indexOf("</bml:WhereLocation>", index1);
        	pad=20;
        }
        else{
    		index1 = fileIn.lastIndexOf("\n", index1);
        	index2 = fileIn.indexOf("</bml:Coords>", index1);
        }
        index1++;
        
		//Copy xml surrounding selectedCoord
        String copySample = fileIn.substring(index1,index2+pad);
        
		//Substitute new coord into copy
        String tmpSample = copySample.replace(bmlLatCoords[selectedCoord].trim(),lattmp.trim()).replace(bmlLonCoords[selectedCoord].trim(),lontmp.trim());
        String newSample;
        if(del){
         newSample = "";
         selectedCoord--;
        }
        else{
    	 newSample = "\n"+copySample + "\n" + tmpSample;
    	 selectedCoord++;
        }
        
        //paste into string after selectedCoord
        String returnSample = fileIn.replace("\n"+copySample,newSample);
        //printDebug(returnSample);
		//printDebug("END ADD POINT------------------------------------");
		return returnSample;
	}
	
	/**
     * Retieve the schema file name for the given report xml file
     * 
     * @param xmlReportFile		It takes the xml file name as an input
     * @return rootNodeString	schema file name to be used in generation of the xml editor gui at run-time
     */
    public String getSchemaFile(String xmlReportFile) throws Exception {
    	String schemaFileName = "";
    	String schemaFileNameString = "";
    	String rootNodeString = "";
    	String line = "";   	
    	File xmlFile = new File(xmlReportFile);
        Scanner xmlFileScanner = new Scanner(xmlFile);  
        printDebug("=== Inside the getschema method");
        
   	    // search for the root element and mapping it to its schema    	
        while (xmlFileScanner.hasNext()){    	
        	line = xmlFileScanner.next();
        	schemaFileNameString = schemaFileNameString + xmlFileScanner.next();

        	if (line.equals("GeneralStatusReport") || line.equals("SpotReport") || line.equals("TrackReport") || 
        			line.equals("NatoReport") || line.equals("BridgeReport") || line.equals("MineFieldReport")){
        		rootNodeString = line;
        		schemaFileName = line + ".xsd";
        		break;
        	}else{
        		printDebug("===Couldn't decide schema of the xml Report");
        	}  	
        }
 
        printDebug("===String schemaFileNameString result is = " + schemaFileNameString);
        printDebug("===String rootNodeString  result is = " + rootNodeString);    
        schemaFileNameString = schemaFileName + ".xsd";           
        return rootNodeString;   	
    }

	/**
	 * Validate the BML Document against the Schema
	 */
	private void validateBmlDoc() {
		if (currentDom != null) {
			currentDom.validate();
		}
	}
	
	/**
	 * Print the BML Document using the PDF Renderer
	 */
	private void printBmlDoc() {
		if (currentDom != null) {
			ByteArrayOutputStream bos = PDFGenerator.getInstance().print(currentDom);
			if (bos != null) {
				try {
					String tempPDFName = guiFolderLocation + "\\bml.pdf";
					FileOutputStream fos = new FileOutputStream(tempPDFName);
					bos.writeTo(fos);
					fos.close();
					BrowserControl.displayURL(tempPDFName);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Generate the JAVA Swing GUI from the DOM of the XML source of the BML Document
	 */
	void visualizeBmlDom() {
		if (editor != null){
			centerPanel.remove(editor);
		}
		com.jaxfront.core.type.Type lastSelectedType = null;
		
		if (editor != null && editor.getSelectedTreeNode() != null) {
			lastSelectedType = editor.getSelectedTreeNode().getType();
		}	
		TypeVisualizerFactory.getInstance().releaseCache(currentDom);	
		editor = new EditorPanel(currentDom.getRootType(), this);
		
		if (lastSelectedType != null) {
			editor.selectNode(lastSelectedType);
		}		
		editor.setBorder(null);
		editor.addHelpListener(this);	
		JPanel validationErrorPanel = new JPanel(new BorderLayout());
		validationErrorPanel.setBorder(null);		
		editor.setTargetMessageTable(validationErrorPanel);		
		centerPanel.add(editor, BorderLayout.CENTER);
	}

	/**
	 * Generate the XML source of the BML Order
	 */
	private void serializeBmlDoc() {
		ShowXMLDialog dialog = new ShowXMLDialog(currentDom);
		dialog.prettyPrint();
		Dimension dialogDim = dialog.getSize();
		Dimension thisDim = getSize();
		int x = (thisDim.width - dialogDim.width) / 2;
		int y = (thisDim.height - dialogDim.height) / 2;
		if (getLocation().x > 0 || getLocation().y > 0) {
			x = x + getLocation().x;
			y = y + getLocation().y;
		}
		dialog.setLocation(((x > 0) ? x : 0), ((y > 0) ? y : 0));
		dialog.setVisible(true);
	}
	
	private void releaseXUICache() {
		XUICache.getInstance().releaseCache();
	}

	/**
	 * Open the configuration file for editing
	 */
	private void openConfig() { 	
		releaseXUICache();
		documentTypeLabel.setText("BMLC2GUI Configuration");
		xsdUrl = URLHelper.getUserURL(guiLocationXSD);		// Schema File XSD
		xmlUrl = URLHelper.getUserURL(guiLocationXML);		// XML file		
		xuiUrl = URLHelper.getUserURL(guiLocationXUI);		// Jaxfront XUI file
		root = null;
		initDom("default-context", xsdUrl, xmlUrl, xuiUrl, root);
	}
	
	private void saveConfig() { 	
		File configFile = new File(guiLocationXML);
		try {
			currentDom.saveAs(configFile);
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadConfig(); 		// Call loadConfig to activate SBMLServer values
	}
	
	private void loadConfig() { 	
		// generate a DOM from the XML Config file and read the elements into the variables
		printDebug("GuiFolder Location : " + guiFolderLocation);
    printDebug("Gui Config Location : " + guiLocationXML);
		File configFile = new File(guiLocationXML);
		w3cDocFactory = DocumentBuilderFactory.newInstance();
		
		try {
			w3cDocBuilder = w3cDocFactory.newDocumentBuilder();
			w3cReportInfoDoc = w3cDocBuilder.parse(configFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		    
	    // read report info using xpath
		  xpathFactory = XPathFactory.newInstance();
	    xpath = xpathFactory.newXPath();
	    try {   	
			sbmlReportServerName = xpath.evaluate("/SBMLServer//SBMLServer", w3cReportInfoDoc);
			String strServerType = xpath.evaluate("/SBMLServer//ServerType", w3cReportInfoDoc);
			serverType = SBMLServerType.valueOf(strServerType);
			submitterID = xpath.evaluate("SubmitterID", w3cReportInfoDoc);
  	  reportBMLType = xpath.evaluate("//ReportBMLType", w3cReportInfoDoc);
			orderBMLType = xpath.evaluate("//OrderBMLType", w3cReportInfoDoc);
      cbmlns = xpath.evaluate("/SBMLServer//CBMLns",w3cReportInfoDoc);
      ibmlns = xpath.evaluate("/SBMLServer//IBMLns",w3cReportInfoDoc);
      sbmlOrderDomainName = xpath.evaluate("//OrderDomainName", w3cReportInfoDoc);
			sbmlReportInfoDomainName = xpath.evaluate("//ReportInfoDomain", w3cReportInfoDoc);
			sbmlReportPullDomainName = xpath.evaluate("//ReportPullDomain", w3cReportInfoDoc);
			sbmlUnitInfoDomainName = xpath.evaluate("//UnitInfoDomain", w3cReportInfoDoc);		
			sbmlOrderServerName = xpath.evaluate("//OrderServer", w3cReportInfoDoc);
      sbmlReportServerName = xpath.evaluate("//ReportServer", w3cReportInfoDoc);
			initMapLat = xpath.evaluate("//InitMapLat", w3cReportInfoDoc);
			initMapLon = xpath.evaluate("//InitMapLon", w3cReportInfoDoc);
			reportOrderScale = xpath.evaluate("//ReportOrderScale", w3cReportInfoDoc);
	    schemaFolderLocation=xpath.evaluate("//SchemaLocation", w3cReportInfoDoc);
			cbmlOrderSchemaLocation = xpath.evaluate("//CBMLOrderSchema", w3cReportInfoDoc);
      cbmlReportSchemaLocation = xpath.evaluate("//CBMLReportSchema", w3cReportInfoDoc);
      ibml09OrderSchemaLocation = xpath.evaluate("//IBML09OrderSchema", w3cReportInfoDoc);
      ibml09ReportSchemaLocation = xpath.evaluate("//IBML09ReportSchema", w3cReportInfoDoc);
      opord_C2CoreSchemaFile = xpath.evaluate("//OPORD_C2CoreSchema", w3cReportInfoDoc);
			opord_C2CoreRoot = xpath.evaluate("//OPORD_C2CoreRoot", w3cReportInfoDoc);		
			xuiFolderLocation = xpath.evaluate("//XUILocation", w3cReportInfoDoc);
			opord_C2CoreXUIFile = xpath.evaluate("//OPORD_C2CoreXUI", w3cReportInfoDoc);	
			orderIDXPath=xpath.evaluate("//OrderIDXPATH",w3cReportInfoDoc);
			whereXPathTag=xpath.evaluate("//LocationXPATH",w3cReportInfoDoc);
			routeXPathTag=xpath.evaluate("//LocationRouteWhereXPATH", w3cReportInfoDoc);	
      latlonParentTag=xpath.evaluate("//latlonParentTag", w3cReportInfoDoc);
      latTag=xpath.evaluate("//latTag", w3cReportInfoDoc);
      lonTag=xpath.evaluate("//lonTag", w3cReportInfoDoc);
      whereIdLabelTag=xpath.evaluate("//whereIdLabelTag", w3cReportInfoDoc);
      whereShapeTypeTag=xpath.evaluate("//whereShapeTypeTag", w3cReportInfoDoc);
      routeIdLabelTag=xpath.evaluate("//routeIdLabelTag", w3cReportInfoDoc);
      routeFromViaToTag=xpath.evaluate("//routeFromViaToTag", w3cReportInfoDoc);
			
			printDebug(" sbmlServerName =" + sbmlReportServerName);
			printDebug(" serverType =" + serverType.toString());
			printDebug(" submitterID =" + submitterID);
			printDebug(" Report BML Type =" + reportBMLType);
			printDebug(" Order BML Type =" + orderBMLType);
      printDebug(" CBML namespace =" + cbmlns);
      printDebug(" IBML namespace =" + ibmlns);
			printDebug(" sbmlReportInfoDomainName =" + sbmlReportInfoDomainName);
			printDebug(" sbmlReportPullDomainName =" + sbmlReportPullDomainName);
			printDebug(" sbmlUnitInfoDomainName =" + sbmlUnitInfoDomainName);
			printDebug(" sbmlOrderDomainName =" + sbmlOrderDomainName);
			printDebug(" sbmlOrderPushServerName =" + sbmlOrderServerName);
			printDebug(" whereXPath =" + whereXPathTag);
			printDebug(" initMapLat =" + initMapLat);
			printDebug(" initMapLon =" + initMapLon);	
			printDebug(" reportOrderScale =" + reportOrderScale);
      printDebug(" SchemaLocation =" + schemaFolderLocation);
      printDebug(" cbmlOrderSchemaLocation =" + cbmlOrderSchemaLocation);
      printDebug(" cbmlReportSchemaLocation =" + cbmlReportSchemaLocation);
      printDebug(" ibml09OrderSchemaLocation =" + ibml09OrderSchemaLocation);
      printDebug(" ibml09ReportSchemaLocation =" + ibml09ReportSchemaLocation);
			printDebug(" OPORDSchemaLocation =" + opord_C2CoreSchemaFile);
			printDebug(" OPORD_C2CoreRoot =" + opord_C2CoreRoot);
			printDebug(" Order ID path =" + orderIDXPath);
			printDebug(" Locationpath =" + whereXPathTag);
			printDebug(" LocationRouteWhereXPATH ="+routeXPathTag);
			printDebug(" xuiFolderLocation ="+xuiFolderLocation);
			printDebug(" OPORDXUIFile ="+ opord_C2CoreXUIFile);	
			printDebug(" reportBMLType = " + reportBMLType);
			printDebug(" orderBMLType =" + orderBMLType);
			printDebug(" latlonParentTag =" + latlonParentTag);
			printDebug(" latTag =" + latTag); 
			printDebug(" lonTag =" + lonTag);
			printDebug(" whereIdLabelTag =" + whereIdLabelTag);
			printDebug(" whereShapeTypeTag =" + whereShapeTypeTag);
			printDebug(" routeIdLabelTag ="+ routeIdLabelTag);
			printDebug(" routeFromViaToTag =" + routeFromViaToTag);	
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
		}	
	} 
	
	private void printDOM() {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(w3cBmlDom);
			StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	private String[] getReportIDs(String afterTimeValue) throws IOException{
	    // For Now just pull the report of a given ReportID
	    File reportIDPullFile = 
        new File(guiFolderLocation + "\\SBMLCLIENT\\dist\\reportidpull.xml");	
    	PrintWriter reportIDPullPW = new PrintWriter(reportIDPullFile);	
    	reportIDPullPW.println("<IDPullStatusReport>");
    	reportIDPullPW.println("<ReportTime>");
    	reportIDPullPW.println("<AllAfterTime>");
    	reportIDPullPW.println(afterTimeValue);
    	reportIDPullPW.println("</AllAfterTime>");
    	reportIDPullPW.println("</ReportTime>");
    	reportIDPullPW.println("</IDPullStatusReport>");	
    	reportIDPullPW.close();
    	printDebug("Finished creating the reportidPull.xml file");	
    	String reportIDPullString = "";
    	String finalIdString = "";
    	
    	try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(guiFolderLocation + "\\SBMLCLIENT\\dist\\reportidpull.bat"); 
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));    
            String line = "";
            
            while((line = input.readLine()) != null) {
                printDebug(" The Line Value from the Input Stream is : " + line);
                reportIDPullString = line;    //read the string in the line variable and assign it to reportPullString
                if (reportIDPullString.contains("ERROR") || reportIDPullString.contains("time")){
                	JOptionPane.showMessageDialog(null, "Couldn't get Report ID's ","Web Service Error",JOptionPane.INFORMATION_MESSAGE);
                	printDebug(" Couldn't get Report ID's");
               	 	pr.destroy();
               	 	break;
               } 
            }
            
            // Step 3 read the stream and clean it
            printDebug("========= String Report ID String result is = " + reportIDPullString);
            
            // Index of the start of the "Result = " in the string  from the stream
            int startIndex;
            startIndex = reportIDPullString.indexOf("<ReportIDs>");
            finalIdString = reportIDPullString.substring(startIndex + 11);
            printDebug("====== XML substring is = " + reportIDPullString.substring(startIndex + 9));
            printDebug("====== finalIdString is = " + finalIdString);      
        } catch(Exception e) {  // jar execution error
            System.err.println(e.toString());
            e.printStackTrace();
        }
        
        int stringIndex=0;
        printDebug("====== finalIdString is = " + finalIdString.indexOf("<ReportID>"));
        boolean moreId = true;
        String tempfinalIdString = finalIdString;
        stringIndex = finalIdString.indexOf("<ReportID>");       
        printDebug("====== stringIndex  is = " + stringIndex );
        
        // No Report IDs , maybe due to no connectivity
        if (stringIndex ==-1){
        	reportIDArray = new String[3];
        	reportIDArray[0]= "99999999999999999999";  
        }
        else{	
        	int reportIDCount =1;
        	moreId = true;
        	stringIndex = tempfinalIdString.indexOf("<ReportID>");
        	
        	// Find the number of IDs first
        	while (moreId){	
              	if (tempfinalIdString.substring(stringIndex +41, stringIndex + 53).equals("</ReportIDs>")){
            		moreId = false;
            		printDebug("====== End of Report IDs ");
            	}
              	else {
              		tempfinalIdString =  tempfinalIdString.substring(stringIndex + 41);
              		stringIndex = tempfinalIdString.indexOf("<ReportID>");
              		reportIDCount++;
              	}
            }
        	printDebug("The number of IDs is : " + reportIDCount);   	
        	reportIDArray = new String[reportIDCount];
        	moreId = true;
        	stringIndex = 0;
        	stringIndex = finalIdString.indexOf("<ReportID>");	
        	int idarrayIndex = 0;
        	
        	while (moreId){
        		reportIDArray[idarrayIndex]= finalIdString.substring(stringIndex+10 , stringIndex + 30);
              	if (finalIdString.substring(stringIndex +41, stringIndex + 53).equals("</ReportIDs>")){
            		moreId = false;
            		printDebug("====== End of array ");
            	}
                printDebug("====== reportIDArray[ " + idarrayIndex + " ]  = " + reportIDArray[idarrayIndex]); 
                finalIdString =  finalIdString.substring(stringIndex + 41);
                stringIndex = finalIdString.indexOf("<ReportID>");
                idarrayIndex++;
            } // end while
        } // end if
        return reportIDArray;
	}
	
	/**
	 * Get a list of the latest reports : ID , Type , Unit
	 */
	private String[] getReportInfo() throws IOException{
    	// result xml file
    	File reportInfoResultFile = 
        new File(guiFolderLocation + "\\SBMLCLIENT\\dist\\reportinfopullresult.xml");
    	PrintWriter reportInfoPullPWXml = new PrintWriter(reportInfoResultFile);

    	// The result of the query returned by the SBMLClient
    	String reportInfoPullResultString = "";
    
    	//Running the SBML Query through the SBMLClient
    	String xmlString = "<GetLatestReportIDs></GetLatestReportIDs>"; 

    	//SBMLClient - Secure or non-secure depending on library implementation
    	SBMLClient sbmlClient = null;
		try {
			sbmlClient = new SBMLClient(sbmlOrderServerName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}	
    	try {
    		printDebug("Starting the Web Service query ");
    		
    		// use the callSBML method to execute the query
    		printDebug("sbmlReportServerName : "+ sbmlReportServerName);
    		printDebug("submitterID : "+ submitterID);
    		printDebug("sbmlReportInfoDomainName : "+ sbmlReportInfoDomainName);
    		printDebug("bmlType : "+ reportBMLType);	
    		reportInfoPullResultString = 
          sbmlClient.sbmlProcessBML(xmlString,submitterID, reportBMLType);
		} catch (Exception_Exception e2) {
			System.err.println("The query execution was unsuccessful....... ");		
		}
 
    	printDebug("The query result is : " + reportInfoPullResultString);  	
   
    	//Write to the orderpullresult.xml
    	reportInfoPullPWXml.println(reportInfoPullResultString);
    	reportInfoPullPWXml.close();
    	printDebug("Finished creating the reportinfopullresult.xml file");
    	w3cDocFactory = DocumentBuilderFactory.newInstance();   // extract DOM from the XML file

    	try {
    		w3cDocBuilder = w3cDocFactory.newDocumentBuilder();
    		w3cReportInfoDoc = w3cDocBuilder.parse(reportInfoResultFile);
    	} catch (ParserConfigurationException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	
    	// read report info using xpath
    	xpathFactory = XPathFactory.newInstance();
    	xpath = xpathFactory.newXPath();
    	String subReportID;
    	String objectName ;
    	String reportType ;
    
    	try {
    		subReportID = xpath.evaluate("//ReportID", w3cReportInfoDoc);
    		reportType = xpath.evaluate("//TypeOfReport", w3cReportInfoDoc);
    		objectName = xpath.evaluate("//ObjectName", w3cReportInfoDoc);
    	} catch (XPathExpressionException e1) {
    		e1.printStackTrace();
    	}

    	// Find the number of Reports in the Document
    	NodeList nodeList = w3cReportInfoDoc.getElementsByTagName("ReportID");
    	printDebug("Node list is : " + nodeList);
    	printDebug("Node list length = " + nodeList.getLength());
	
    	for (int i=1 ; i <= nodeList.getLength() ; i++){
    		try {
    			subReportID = xpath.evaluate("/Reports/ReportInfo[" + i + "]/ReportID", w3cReportInfoDoc);
    			reportType = xpath.evaluate("/Reports/ReportInfo[" + i + "]/TypeOfReport", w3cReportInfoDoc);
    			objectName = xpath.evaluate("/Reports/ReportInfo[" + i + "]/ObjectName", w3cReportInfoDoc);
    			printDebug("Report ID is : " + subReportID);
    			printDebug("Report Type is : " + reportType);
    			printDebug("Report Unit is : " + objectName);
    			reportInfoComboBox.addItem(subReportID + " | " + reportType + " | " + objectName);		
    		} catch (XPathExpressionException e) {
    			e.printStackTrace();
    		}
    	}
        return reportInfoArray;       
	} // End of method getReportInfo
	
	/**
	 * Maps raw reportString's to discrete reportType's
	 */
	public String getBmlDocumentType(String reportString){
		String reportType = "UNKNOWN";
		if (reportString.contains("BRIDGEREP")){
			reportType = "BRIDGEREP";
		}else if (reportString.contains("MINOBREP")){
			reportType = "MINOBREP";
		}else if (reportString.contains("NATOSPOTREP")){
			reportType = "NATOSPOTREP";
		}else if (reportString.contains("SPOTREP")){
			reportType = "SPOTREP";
		}else if (reportString.contains("TRKREP")){
			reportType = "TRKREP";
		}else if (reportString.contains("GeneralStatusReport")){
			reportType = "GeneralStatusReport";
		}else if (reportString.contains("PositionStatusReport")){
			reportType = "PositionStatusReport";
		}
		printDebug("======== This is a " + reportType + " report ==========");		
		
    	return reportType;
	}
	
	/**
	 * A wrapper around a commonly used DOMBuilder, which uses the params as-is.  Sets currentDom.
	 */
	public void initDom(String context, URL url1, URL url2, URL url3, String root){
    try {
			currentDom = DOMBuilder.getInstance().build(context, url1, url2, url3, root);
			currentDom.getGlobalDefinition().setIsUsingButtonBar(false);
			currentDom.getGlobalDefinition().setIsUsingStatusBar(true);
			currentDom.getGlobalDefinition().setLanguage(currentLanguage);
			if (editor != null)	editor.selectNode((com.jaxfront.core.type.Type) null);
			visualizeBmlDom();
		} catch (Exception ex) {
			ex.printStackTrace();	
		}		
	}
	
	/**
	 * Draws from currentDom, first calls initDom to set currentDom
	 */
	public void drawFromXML(
    String context, 
    URL url1, 
    URL url2, 
    URL url3, 
    String root, 
    String bmlDocumentType,
    String whereNodeTag,
    String[] whereTextTags,
    String[] whereElementTags,
    String nsPrefix,
    String xmlString){	

    printDebug("drawFromXML context:"+context);
    printDebug("drawFromXML url1:"+url1);
    printDebug("drawFromXML url2:"+url2);
    printDebug("drawFromXML url3:"+url3);
    printDebug("drawFromXML root:"+root);
    printDebug("drawFromXML bmlDocumentType:"+bmlDocumentType);
    
    // if the xmlString is empty read the file into it
    String filename = null;
    if(url2 != null) {
        filename = url2.getFile();
           if(isWindows && filename.charAt(0) == '/')// hack to clean up unexpected /
        filename = filename.substring(1);
    }
    if(xmlString == null)
        xmlString = readAnXmlFile(filename);
    else if(xmlString.length() == 0)
      xmlString = readAnXmlFile(filename);
    printDebug("FILENAME:" + filename);
    printDebug("NS PREFIX:" + nsPrefix);
    printDebug("XML:" + xmlString);
    currentXmlString = new String(xmlString);
    
    // get data for geolocations from XML file
    currentXmlDataArray = null;
    String elementZero = null;
    if(whereNodeTag != null && whereTextTags != null && whereElementTags != null) {
      XmlParse xrp = new XmlParse(xmlString);
      String nsPrefixRef = nsPrefix;
      if(nsPrefixRef == null)nsPrefixRef = new String("");
      currentXmlDataArray = 
        xrp.getElementNameDataByTagName(
          whereNodeTag,
          whereTextTags,
          whereElementTags,
          nsPrefixRef);

      // check if file was parsable
      if(currentXmlDataArray == null) {
        JOptionPane.showMessageDialog(
        null, 
        "file cannot be parsed ",
        "XML File Error ", 
        JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      // print the resulting array debugx 
      if(currentXmlDataArray.length == 0) {
        System.err.println("NO XML DATA WAS EXTRACTED");
        return;
      }
      printDebug("************ XML DATA:");
      for(int index = 0; index < currentXmlDataArray.length; ++ index) {
        printDebug(currentXmlDataArray[index]);
      }
      printDebug("************ END XML DATA");
      
      // verify first XML tag matches bmlDocumentType
      elementZero = new String(currentXmlDataArray[0]);
      if((bmlDocumentType.equals("CBML Light Order") && 
          !(elementZero.equals(cbmlns+"Task") || elementZero.equals("Task"))) 
        &&
        (bmlDocumentType.equals("IBML Order09") &&
          !(elementZero.equals(ibmlns+"GroundTask") || elementZero.equals("GroundTask"))) 
        &&
        (bmlDocumentType.equals("GeneralStatusReport") && 
          !(elementZero.equals(cbmlns+"GeneralStatusReport") || 
            elementZero.equals("GeneralStatusReport") ||
            elementZero.equals(ibmlns+"GeneralStatusReport")))) {
      JOptionPane.showMessageDialog(
        null, 
        "file doesn't match selected type ",
        "XML File Error ", 
        JOptionPane.ERROR_MESSAGE);
      return;
    }
      
    // set document function and dialect
    if(bmlDocumentType.equals("CBML Light Order")) {
      generalBMLFunction = "Order";
      generalBMLType = "CBML";
    }
    else if(bmlDocumentType.equals("IBML Order09")) {
      generalBMLFunction = "Order";
      generalBMLType = "IBML"; 
    }
    else if(bmlDocumentType.equals("GeneralStatusReport")) {
      generalBMLFunction = "Report";
      generalBMLType = reportBMLType;
    }
    
    // parse Control Measures from IBML09 Order
    String[] currentXmlControlMeasuresArray = new String[0];
    if(bmlDocumentType.equals("IBML Order09")) {
      currentXmlControlMeasuresArray = 
        xrp.getElementNameDataByTagName(
          "ControlMeasure",
          new String[]{
            "WhereClass",
            "WhereCategory",
            "WhereLabel",
            "WhereQualifier"},
          new String[]{
            "Latitude",
            "Longitude"},
          ibmlns);
      if(currentXmlControlMeasuresArray == null) {
        JOptionPane.showMessageDialog(
        null, 
        "IBML Order file ControlMeasures cannot be parsed ",
        "XML File Error ", 
        JOptionPane.ERROR_MESSAGE);
        currentXmlControlMeasuresArray = new String[0];
      }
    }// end if IBML Order09

    // parse Control Measures from CBML Order
    if(bmlDocumentType.equals("CBML Light Order")) {
      currentXmlControlMeasuresArray = 
      xrp.getElementNameDataByTagName(
        cbmlns+"ControlMeasure",
        new String[]{
          "AtWhere",
          "PointLight",
          "Line",
          "Surface",
          "CorridorArea"},
        new String[]{
          "Latitude",
          "Longitude"},
        cbmlns);
      if(currentXmlControlMeasuresArray == null) {
        JOptionPane.showMessageDialog(
        null, 
        "CBML Order file ControlMeasures cannot be parsed ",
        "XML File Error ", 
        JOptionPane.ERROR_MESSAGE);
        currentXmlControlMeasuresArray = new String[0];
      }
    }

    // append any Control Measures in Order to end of currentXmlDataArray  
    int currentLength = currentXmlDataArray.length;
    String[] holdXmlDataArray = 
      new String[currentLength + currentXmlControlMeasuresArray.length];
    System.arraycopy(
      currentXmlDataArray, 
      0, 
      holdXmlDataArray, 
      0,
      currentLength);
    System.arraycopy(
      currentXmlControlMeasuresArray, 
      0, 
      holdXmlDataArray, 
      currentLength, 
      currentXmlControlMeasuresArray.length);
    currentXmlDataArray = holdXmlDataArray;
      
//printDebug("FILE:"+url2.getFile());printDebug("DocType:"+bmlDocumentType);//debugx
//printDebug("NAME DATA for:"+whereNodeTag+"|"+whereElementTags[0]+"|");//debugx
//for(int i=0; i<currentXmlDataArray.length; ++i)printDebug(i+":"+currentXmlDataArray[i]);//debugx

    }
    // package array for drawing
    if(currentXmlDataArray != null)
        bmlStringArray = currentXmlDataArray;
    else 
    {
      // use original DOM approach to extracting data
      initDom(context, url1, url2, url3, root);
		  try {
			  // reading the whole DOM values of the XML file into a csv string
			  bmlString = currentDom.getRootType().getDisplayValue();
			  printDebug("XML Document String : "+ bmlString);

			  // convert the csv string to an array of strings
			  bmlStringArray = bmlString.split(",");

			  for (int i=0; i < bmlStringArray.length;i++){
				  printDebug("XML Document String [ "+  i  + " ] = |" + bmlStringArray[i] + "|" );
			  }
		  } catch (Exception ex) {
			  ex.printStackTrace();
			  JOptionPane.showMessageDialog(null, "XML Error ", "Couldn't Create Document ",JOptionPane.ERROR_MESSAGE);
		  } // end catch
    }// end else
    
    // draw the resulting graphics
    try {
      // call method that starts the drawing process of the location information
			drawLocation(bmlStringArray, bmlDocumentType);
			catalogPoints(bmlStringArray);
 		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(
        null, 
        "Graphics Error ", 
        "Couldn't Draw Document ",
        JOptionPane.ERROR_MESSAGE);
		} // end catch
    
	}// end drawFromXml()
	
	public void drawOPORD_FS(URL url1, URL url2, URL url3, String root){
    initDom(null, url1, url2, url3, root);
		try {	
			// reading the whole DOM values of the XML file into a csv string
			bmlString = currentDom.getRootType().getDisplayValue();
			
			// convert the csv string to an array of strings
			bmlStringArray = bmlString.split(",");
			catalogPoints(bmlStringArray);
			mapGraph = true;
			
			for (int i=0; i < bmlStringArray.length;i++){
				printDebug("Order String [ "+  i  + " ] = " + bmlStringArray[i] );
			}
						
			// call method that starts the drawing process of the location information
			/*Option 1 : direct analysis of the string array of the Document
			 *			 which uses RouteLayer.createC2CoreOPORDGraphics method
			 *			 see method for details
			 */			
						 
			/*Option 2 : use of Xpath to find location information
			 * Note : -didn't work for IBML_CBML because of jaxfront name space problems
			 *		  -unknown if it is going to work with C2Core.
			 * 		  -to use this option disable the drawLocation above and 
			 *		   enable commented code below --drawOPORD-- method
			 */
			 			
			//Serialize the JaxFront DOM to to W3C DOM
			try {w3cBmlDom = currentDom.serializeToW3CDocument();}
			catch (Exception e) {e.printStackTrace();}
			
			// display DOM in openMap
			if (routeLayerOPORD!=null) routeLayerOPORD.clearGraphics();
		
			stringOPORDName = xpathCBML.evaluate("//" + orderIDXPath,w3cBmlDom);
			printDebug("Order ID :" + stringOPORDName);
	    		   	
	    	// singlePointXPathTag usually represents atWhere
	    	// multiPointXPathTag usually represents routeWhere
	    	// Drawing a single point or a shape of multiple points
	    	NodeList atWhereNodes = (NodeList) xpathCBML.evaluate("//" + whereXPathTag,w3cBmlDom, XPathConstants.NODESET);

	    	int j = 0; // shape coords array counter , one dimension array    	
	    	String[] tempShape = new String[shapeCoords*2];
	    	for (int i=0 ; i < atWhereNodes.getLength() ; i++){
	    		//WhereLabel
	    		Node atWhere = atWhereNodes.item(i);
	    		String label = xpathCBML.evaluate(whereIdLabelTag, atWhere);
	    		shapeType = xpathCBML.evaluate(whereShapeTypeTag, atWhere);
				if (label.length()>0) {
					NodeList atWhereLocationNodes = (NodeList) xpathCBML.evaluate(latlonParentTag,atWhere, XPathConstants.NODESET);
					int length = atWhereLocationNodes.getLength();
					printDebug("AtWhere Location Node " +  label + " list length is : " + length);
			    	tempLocationsOPORD = new String[length*2];
					for (int x=0 ; x < length ; x++){
						tempLocationsOPORD[x*2] = xpathCBML.evaluate(latTag, atWhereLocationNodes.item(x));
						tempLocationsOPORD[x*2+1] = xpathCBML.evaluate(lonTag, atWhereLocationNodes.item(x));
						printDebug("Lat =" + tempLocationsOPORD[x*2] + "  ,  Lon = " + tempLocationsOPORD[x*2+1]);
					}	
					locationCoords = length*2;
					drawOPORD(stringOPORDName, label, tempLocationsOPORD, shapeType, locationCoords);
				}
	    	} // end for
	    	
	    	// Drawing Route - From-Via-To Locations
	    	String locationLat="";
	    	String locationLon="";
			  printDebug(routeXPathTag);
	    	NodeList LocNodeFromViaTo = (NodeList) xpathCBML.evaluate("//" + routeFromViaToTag ,w3cBmlDom, XPathConstants.NODESET);
	    	int iNumberOfRoutes = LocNodeFromViaTo.getLength();
	    	for (int iRoute=0 ; iRoute < iNumberOfRoutes ; iRoute++){
	    		NodeList LocNodeFromViaToLocations = (NodeList) xpathCBML.evaluate("//" + routeXPathTag + "["+(iRoute+1)+"]//" + latlonParentTag , LocNodeFromViaTo.item(iRoute), XPathConstants.NODESET);
	    		shapeCoords =LocNodeFromViaToLocations.getLength();
	    		shapeType ="LN";
	    		label =xpathCBML.evaluate("//" + routeXPathTag + "[" +(iRoute+1)+"]/" + routeIdLabelTag ,w3cBmlDom);
	    		tempShapeOPORD = new String[shapeCoords*2]; // add 2 coords for from and to
	    	
		    	// RouteWhere locations
		    	for (int i=0 ; i < shapeCoords ; i++){
		    		try {
		    			locationLat = xpathCBML.evaluate(latTag, LocNodeFromViaToLocations.item(i));
		    			tempShapeOPORD[i*2] = locationLat;
		    			locationLon = xpathCBML.evaluate(lonTag, LocNodeFromViaToLocations.item(i));
		    	    	tempShapeOPORD[i*2+1] = locationLon;
		    		} catch (XPathExpressionException e) {
		    			e.printStackTrace();
		    		}
		    	} // end for   	
		    	drawOPORD(stringOPORDName, label, tempShapeOPORD, shapeType, shapeCoords);
	    	} // end for	
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}// end drawOPORD_FS
	
  /*
  ** enters array of points into 
  */
	public void catalogPoints(String[] catalogArray){
    
    // count instances of GDC and VerticalDistance in 
    // parameter array and make new arrays to fit
		numCoords = 0;
		for(int i = 0; i < catalogArray.length - 2; i++){
			if(catalogArray[i].contains("GDC") || 
         catalogArray[i].contains("VerticalDistance"))
        numCoords++;
		}
		bmlLatCoords = new String[numCoords * 2 + 100];
		bmlLonCoords = new String[numCoords * 2 + 100];
		numCoords = 0;
		for(int i = 3; i < catalogArray.length - 2; i++){
			if(catalogArray[i].contains("GDC") && 
        catalogArray[i+1].contains(".") && catalogArray[i+2].contains(".")){
				bmlLatCoords[numCoords] = catalogArray[i+1];
				bmlLonCoords[numCoords] = catalogArray[i+2];
				numCoords++;
			}	
			else if(catalogArray[i].contains("VerticalDistance") && 
        catalogArray[i-2].contains(".") && catalogArray[i-1].contains(".")){
				bmlLatCoords[numCoords] = catalogArray[i-2];
				bmlLonCoords[numCoords] = catalogArray[i-1];
				numCoords++;
			}
		}

	}// end catalogPoints()
	
  /*
   * returns index in bmlLatCoords/bmlLonCoords of point
   * closest to the argument lat/lon
  */
	public int closestTo(String lat, String lon){
		float x1 = Float.valueOf(lat.trim()).floatValue();
		float y1 = Float.valueOf(lon.trim()).floatValue();
		float x2,y2;
		double dx,dy,dif,min;
		min = 1000;
		int index = -1;
        for(int i = 0; i < numCoords; i++){
        	x2 = Float.valueOf(bmlLatCoords[i].trim()).floatValue();
        	y2 = Float.valueOf(bmlLonCoords[i].trim()).floatValue();
        	dx = Math.pow((x1-x2),2);
        	dy = Math.pow((y1-y2),2);
        	dif = Math.sqrt(dx+dy);
        	if(dif < min){index = i; min = dif;}
        }
		return index;
	}
	
	/**
	 * Maps reportType's to URL's; common preprocessing before a call to drawFromXML
	 * 
	 * @param reportType
	 * @return root		The report type of the root
	 */
	public String setUrls(String reportType){
		String root;
		String schemaLocation = schemaFolderLocation + "/Reports/";
		if (reportType == "GeneralStatusReport" || reportType == "PositionStatusReport"){
      if( BMLC2GUI.sbmlOrderDomainName.equals("CBML")){
        xsdUrl = URLHelper.getUserURL( schemaLocation.concat("CBML_Reports.xsd"));
        root = "CBMLReport";
      }
      else {
        xsdUrl = URLHelper.getUserURL( schemaLocation.concat("IBMLReports.xsd"));
        root = "BMLREPORT";
      }
			xuiUrl = URLHelper.getUserURL(xuiFolderLocation + "/GeneralStatusReportView.xui");
			printDebug("New " + reportType);
		}
    else {
			if (reportType == "NATOSPOTREP"){ //Alias NatoSpotRep to regular Spot Report
				root = "SPOTREP";
			}
			xsdUrl = URLHelper.getUserURL(schemaLocation.concat("IBMLSIMCIReports.xsd"));
			root = reportType;
			xuiUrl = URLHelper.getUserURL(xuiFolderLocation + "/" + root + "View.xui");
			printDebug("Pull " + reportType);	
		}
		return root;
	}
	
    private static void loadResource(String resources, Properties props) {
        InputStream in = BMLC2GUI.class.getResourceAsStream(resources);
        if (props == null) {
            System.err.println("Unable to locate resources: " + resources);
            System.err.println("Using default resources.");
        } else {
            try {
                props.load(in);
                printDebug("Resources located ...........: " + resources);                
            } catch (java.io.IOException e) {
                System.err.println("Caught IOException loading resources: " + resources);
                System.err.println("Using default resources.");
            }
        }
    }

    /**
     * Gets the names of the Layers to be loaded from the properties passed in, initializes them, and returns them.
     * 
     * @param p the properties, among them the property represented by
     *        the String layersProperty above, which will tell us
     *        which Layers need to be loaded
     * @return an array of Layers ready to be added to the map bean
     * @see #layersProperty
     */
    private Layer[] getLayers(Properties p) {
        // Get the contents of the hello.layers property, which is a
        // space-separated list of marker names...
        String layersValue = p.getProperty(layersProperty);

        // Didn't find it if it's null.
        if (layersValue == null) {
            System.err.println("No property \"" + layersProperty
                    + "\" found in application properties.");
            return null;
        }
        
        // Parse the list
        StringTokenizer tokens = new StringTokenizer(layersValue, " ");
        Vector layerNames = new Vector();
        while (tokens.hasMoreTokens()) {
            layerNames.addElement(tokens.nextToken());
        }
        int nLayerNames = layerNames.size();
        Vector layers = new Vector(nLayerNames);

        // For each layer marker name, find that layer's properties.
        // The marker name is used to scope those properties that
        // apply to a particular layer. If you parse the layers'
        // properties from a file, you can add/remove layers from the
        // application without re-compiling. You could hard-code all
        // the properties being set if you'd rather...
        for (int i = 0; i < nLayerNames; i++) {
            String layerName = (String) layerNames.elementAt(i);

            // Find the .class property to know what kind of layer to create.
            String classProperty = layerName + ".class";
            String className = p.getProperty(classProperty);
            if (className == null) {
                // Skip it if you don't find it.
                System.err.println("Failed to locate property \"" + classProperty + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
                continue;
            }
            try {
                // Create it if you do...
                Object obj = java.beans.Beans.instantiate(null, className);
                if (obj instanceof Layer) {
                    Layer l = (Layer) obj;
                    
                    // All layers have a setProperties method, and
                    // should intialize themselves with proper
                    // settings here. If a property is not set, a
                    // default should be used, or a big, graceful
                    // complaint should be issued.
                    l.setProperties(layerName, p);
                    layers.addElement(l);
                }
            } catch (java.lang.ClassNotFoundException e) {
                System.err.println("Layer class not found: \"" + className + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            } catch (java.io.IOException e) {
                System.err.println("IO Exception instantiating class \"" + className + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            }
        }
        int nLayers = layers.size();
        if (nLayers == 0) {
            return null;
        } else {
            Layer[] value = new Layer[nLayers];
            layers.copyInto(value);
            return value;
        }
    }

	private void closeDocument() {		
		if (editor != null){
			centerPanel.remove(editor);
		}
		centerPanel.repaint();	
	}
	
	/**
	 * Change current language to English
	 */
	private void languageEnglish() {
		currentDom.getGlobalDefinition().setLanguage("en");
		TypeVisualizerFactory.getInstance().releaseCache(currentDom);	//to refresh the views
		
		//Recreate EditorPanel and add it to the container again
		editor = new EditorPanel(currentDom.getRootType(), this);
		centerPanel.removeAll();
		centerPanel.add(editor, BorderLayout.CENTER);
	}
	
	/**
	 * Change current language to French
	 */
	private void languageFrench() {
		currentDom.getGlobalDefinition().setLanguage("fr");
		centerPanel.remove(editor);		//to refresh the views

		//Recreate EditorPanel and add it to the container again
		editor = new EditorPanel(currentDom.getRootType(), this);
		centerPanel.add(editor);
	}
	
	/**
	 * Change current language to German
	 */
	private void languageGerman() {
		currentDom.getGlobalDefinition().setLanguage("de");
		TypeVisualizerFactory.getInstance().releaseCache(currentDom);	//to refresh the views

		//Recreate EditorPanel and add it to the container again
		editor = new EditorPanel(currentDom.getRootType(), this);
		centerPanel.removeAll();
		centerPanel.add(editor, BorderLayout.CENTER);
	}
	
	/**
	 * Change current language to Italian
	 */
	private void languageItalian() {
		currentDom.getGlobalDefinition().setLanguage("it");
		TypeVisualizerFactory.getInstance().releaseCache(currentDom);	//to refresh the views

		//Recreate EditorPanel and add it to the container again
		editor = new EditorPanel(currentDom.getRootType(), this);
		centerPanel.removeAll();
		centerPanel.add(editor, BorderLayout.CENTER);
	}

	/*
	 * mababneh
	 * 11/9/2011
	 * drawMSDL : draw the MSDL layer
	 */
	public void drawMSDL(URL url1, URL url2, URL url3, String root){

                initDom(null, url1, url2, url3, root);
		
		try {	
			// reading the whole DOM values of the XML file using XPath
		
			//Serialize the JaxFront DOM to to W3C DOM
			try {w3cBmlDom = currentDom.serializeToW3CDocument();}
			catch (Exception e) {e.printStackTrace();}

			NodeList unitNodes = (NodeList) xpathMSDL.evaluate("//" + "Unit" ,w3cBmlDom, XPathConstants.NODESET);
			NodeList equipmentNodes = (NodeList) xpathMSDL.evaluate("//" + "EquipmentItem" ,w3cBmlDom, XPathConstants.NODESET);
	
	    	printDebug("Unit Node List Length = " + unitNodes.getLength());
	    	printDebug("Equipment Node List Length = " + equipmentNodes.getLength());
	    	
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
	    	
	    	printDebug("=== Printing Units and Equipments found in MSDL Organization");
	    	for (int j1=0 ; j1< orgUnitsAndEquipments.length; j1++){
	    		printDebug("Org Name: " + orgUnitsAndEquipments[j1][0]);
	    		printDebug("Org Symbol ID: " + orgUnitsAndEquipments[j1][1]);
	    		printDebug("Org Lat: " + orgUnitsAndEquipments[j1][2]);
	    		printDebug("Org Lon: " + orgUnitsAndEquipments[j1][3]);   		 		
	    	}
	    	
	    	String areaOfInterestName, areaofInterestUpperRightLat, areaofInterestUpperRightLon, areaofInterestLowerLeftLat, areaofInterestLowerLeftLon;
	    	NodeList areaOfInterestNodes = (NodeList) xpathMSDL.evaluate("//" + "AreaOfInterest" ,w3cBmlDom, XPathConstants.NODESET);
	    	Node areaofInterestNode = areaOfInterestNodes.item(0);
	    	areaOfInterestName = xpathMSDL.evaluate("Name", areaofInterestNode);
	    	areaofInterestUpperRightLat = xpathMSDL.evaluate("//UpperRight/CoordinateData//GDC//Latitude", areaofInterestNode);
	    	areaofInterestUpperRightLon = xpathMSDL.evaluate("//UpperRight/CoordinateData//GDC//Longitude", areaofInterestNode);
	    	areaofInterestLowerLeftLat = xpathMSDL.evaluate("//LowerLeft/CoordinateData//GDC//Latitude", areaofInterestNode);
	    	areaofInterestLowerLeftLon = xpathMSDL.evaluate("//LowerLeft/CoordinateData//GDC//Longitude", areaofInterestNode);
	    	
	    	printDebug("areaOfInterestName: " + areaOfInterestName);
	    	printDebug("areaofInterestUpperRightLat: " + areaofInterestUpperRightLat);
	    	printDebug("areaofInterestUpperRightLon: " + areaofInterestUpperRightLon); 
	    	printDebug("areaofInterestLowerLeftLat : " + areaofInterestLowerLeftLat);
	    	printDebug("areaofInterestLowerLeftLon : " + areaofInterestLowerLeftLon );

	    	String [] areaOfInterestArray = new String[5];
	    	areaOfInterestArray[0] = areaOfInterestName;
	    	areaOfInterestArray[1] = areaofInterestUpperRightLat;
	    	areaOfInterestArray[2] = areaofInterestUpperRightLon;
	    	areaOfInterestArray[3] = areaofInterestLowerLeftLat;
	    	areaOfInterestArray[4] = areaofInterestLowerLeftLon;
	    	
	    	// Calling the drawing method
	    	mapGraph = true;
			printDebug("------drawMSDL-----");
			
			if (!mapMSDL) {
				// mapMSDL 
				routeLayerMSDL = new RouteLayer();
				routeLayerMSDL.setName("MSDL"); //bmlDocumentType
				routeLayerMSDL.setVisible(true);
				mapHandler.add(routeLayerMSDL);
				mapMSDL = true;
			}
			routeLayerMSDL.createMSDLGraphics(orgUnitsAndEquipments, areaOfInterestArray);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}// end drawMSDL()

  // if selected option can't be done in current server, popup message
  boolean checkCanDo()
  {
    if(BMLC2GUI.serverType.getIsREST())return false;
    JOptionPane.showMessageDialog(null, "Can't Do ", "Optionnot functional ",JOptionPane.ERROR_MESSAGE);
    return true;
  }
  
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
                
    //printDebug( subscriberDocument.toString());
                  
    //the report type now is known
		System.out.print("***************Report Type is  : " );
    System.out.print( reportType);
                
		bml.documentTypeLabel.setText(reportType);
    
		// Step 7 : Display the Report Document
		String schemaLocation = bml.schemaFolderLocation + "/Reports/";
     		
		// reading the whole DOM values of the XML file into a csv string
		bml.bmlString = bml.currentDom.getRootType().getDisplayValue(); 
		printDebug("===============================XML Document String : "+ bml.bmlString);
                        
    //Generate the swing GUI using the org.w3c.dom.Document instead of an XML File
		try { //can not call drawFromXML because build is different
			bml.currentDom = DOMBuilder.getInstance().build("default-context", bml.xsdUrl, subscriberDocument, bml.xuiUrl, root);
			bml.currentDom.getGlobalDefinition().setIsUsingButtonBar(false);
			bml.currentDom.getGlobalDefinition().setIsUsingStatusBar(true);
			bml.currentDom.getGlobalDefinition().setLanguage(bml.currentLanguage);
      if (bml.editor != null) bml.editor.selectNode((com.jaxfront.core.type.Type) null);
			bml.visualizeBmlDom();
                                             
			// reading the whole DOM values of the XML file into a csv string
			bml.bmlString = bml.currentDom.getRootType().getDisplayValue();
			printDebug("XML Document String : "+ bml.bmlString);
		
			// convert the csv string to an array of strings
			bml.bmlStringArray = bml.bmlString.split(",");
			for (int i=0; i < bml.bmlStringArray.length;i++){
				printDebug("XML Document String [ "+  i  + " ] = " + bml.bmlStringArray[i] );
			}

			// call method that starts the drawing process of the location information 
			bml.drawLocation(bml.bmlStringArray , reportType);
	
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Report Error ", "Couldn't Create Report ",JOptionPane.ERROR_MESSAGE);
		} // End of Catch
	}
	
} // End of BMLC2GUI Class
