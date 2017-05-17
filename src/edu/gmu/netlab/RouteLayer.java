/*----------------------------------------------------------------*
|   Copyright 2009-2010 Networking and Simulation Laboratory      |
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import nl.tno.sims.ListEditableOMGraphics;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.EditableOMCircle;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMLine;
import com.bbn.openmap.omGraphics.EditableOMPoint;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.EditableOMScalingRaster;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMScalingIcon;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.symbology.milStd2525.PNGSymbolImageMaker;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolReferenceLibrary;

import edu.gmu.c4i.sbml.Exception_Exception;
import edu.gmu.c4i.sbmlclientlib.SBMLClient;

/**
 * The Location Layer implementation. 
 *
 * @version		C2ML GUI (Initially BMLGUI)
 * @author 		Mohammad Ababneh, C4I Center, George Mason University
 * @since		4/9/2010
 */
public class RouteLayer extends Layer implements MapMouseListener {

    BMLC2GUI bml = BMLC2GUI.bml;
    private ListEditableOMGraphics omgraphics;    /* A list of graphics to be painted on the map. */
    private Projection projection;    /* The current projection. */
    private OMGraphic selectedGraphic;    /* The currently selected graphic. */
    private GrabPoint selectedPoint;
    private boolean mouseButtonDown = false;    
    private float latOpen, lonOpen;
    float[] areaPoints = new float[16]; // array of points related to an area
    private String[] stringArray; // Array of split order or report csv string
    
    // variable to hold UnitID to be sent to SBMLClient in order to get unit info
    private String sbmlUnitID;     // Unit ID from BML document : Order or Report
    private String unitArm_Cat_Code = null;    // Unit Arm_Cat_Code from SBML Client
    private String unitHostility = null;	// // Unit Hostility from SBML Client
    
    private String milStd2525bSymbolCode ="";   // milStd2525b SymbolCode to be calculated
    private String milStd2525bCodeScheme ="";   // milStd2525b CodeScheme Position 1
    private String milStd2525bAffliation ="";   // milStd2525b Affiliation Position 2
    private String milStd2525bDimention ="";   // milStd2525b Battle Dimension Position 3
    private String milStd2525bStatus ="";   // milStd2525b Unit Status ( Anticipated / Present)Position 4 
    private String milStd2525bFunctionID ="";   // milStd2525b Unit Function ID Position 5-10
    private String milStd2525bModifier ="";   // milStd2525b Modifier Position 11-12
    private String milStd2525bCountryCode ="";   // milStd2525b Country Code Position 13-14
    private String milStd2525bOrderOfBattle ="";   // milStd2525b Order of Battle Position 15
    
    /**
     * Constructor (Default)
     */
    public RouteLayer() {
        omgraphics = new ListEditableOMGraphics();
    	BMLC2GUI.mapBean.setScale(3000000f);
        omgraphics.setProjection(projection);   
        
    }
    /**
     * Default Constructor with String 
     *
     */
    public RouteLayer(String pName) {
        omgraphics = new ListEditableOMGraphics();
    	BMLC2GUI.mapBean.setScale(3000000f);
        omgraphics.setProjection(projection);   
    }


	/**
	 * Constructor that branches to initialization
	 *
	 * @param	bmlDocumentType		value determines branch
	 */
    public RouteLayer(String[] orderStringArray, String bmlDocumentType) {
    	stringArray = orderStringArray;
    	System.out.println("--------------------------------------------------");
    	System.out.println("Passed BML Document String is : " /*+ stringArray +" "*/+ bmlDocumentType);
    	System.out.println("--------------------------------------------------");     
    	omgraphics = new ListEditableOMGraphics();
        omgraphics.setProjection(projection);
    	  	
    	if (bmlDocumentType == "IBML Order"){
    		createOrderGraphics(omgraphics);
    	}
        //This is commented out because OPORDs are never passed through this constructor
    	//else if (bmlDocumentType == "OPORD" || bmlDocumentType == "C2CoreOPORD"){
    	//	createIBML_CBML_OPORDGraphics(omgraphics); // actually IBML_CBML OPORD not NATOOPORD called by drawOPORD
    	//}
    	else if (bmlDocumentType == "IBML Order09"){
    		createOrderGraphicsIBML09(omgraphics);
    	}
    	else if (bmlDocumentType == "OrderOld"){
    		createOrderGraphicsOld(omgraphics);
    	}
      else if (bmlDocumentType == "BRIDGEREP" || bmlDocumentType == "MINOBREP" || 
        bmlDocumentType == "SPOTREP" || bmlDocumentType == "NATOSPOTREP" || 
        bmlDocumentType == "TRKREP"){
			createSIMCIReportGraphics(omgraphics,  bmlDocumentType);
		}
    	else if (bmlDocumentType == "OldGeneralStatusReport" || bmlDocumentType == "GeneralStatusReport" || bmlDocumentType == "PositionStatusReport"){
    		
        System.out.println("GeneralStatusReport");
        createGeneralStatusReportGraphics(omgraphics, bmlDocumentType);
    	}
  
    } // End of RouteLayer main method
    
    /**
     *  checks a tag for match; then tries again with namespace prefix
     */
    boolean cbmlTagCompare(String checkTag, String soughtTag) {
      String trimmedTag = checkTag.trim();
      if(trimmedTag.equals(soughtTag))return true;
      return trimmedTag.equals(bml.cbmlns + soughtTag);
    }
    
   /**
     *  checks a tag for match; then tries again with namespace prefix
     */
    boolean ibmlTagCompare(String checkTag, String soughtTag) {
      String trimmedTag = checkTag.trim();
      if(trimmedTag.equals(soughtTag))return true;
      return trimmedTag.equals(bml.ibmlns + soughtTag);
    }
     
    /**
     * Draws report graphics for all report types. Clears and then fills the given OMGraphicList. 
     */
    public ListEditableOMGraphics createSIMCIReportGraphics(ListEditableOMGraphics graphics, String simciReportType) {
        graphics.clear();
		String[][] gdcArray = new String [100][3];
		int [] descArray = new int[23]; // Array of a max of 23 locations
		boolean locationFound =false;
		int pointCount = 0;	// number of points in all locations
		int locationCount = 0; // number of location objects
		int locationPointCount = 0; // number of points in a location
				
		for (int i=0; i < stringArray.length; i++){
			if (stringArray[i].trim().equals("GDC")){ 			// Look for location Information
				System.out.println("A Location has been discovered #: " + pointCount + "  It started at : " + i);
				locationFound = true;
				
				while (locationFound){
					pointCount++;
					locationPointCount++;
					gdcArray[pointCount][0]= stringArray[i+1].toString();
					gdcArray[pointCount][1]= stringArray[i+2].toString();
					gdcArray[pointCount][2]= stringArray[i+3].toString();
					if (stringArray[i+4].toString().trim().equals("GDC")){
						System.out.println("One more GDC");
					}
					else{
						System.out.println("No  more GDC");
						locationCount++;
						descArray[locationCount] = locationPointCount; 
						locationPointCount = 0;
					}
					if (stringArray[i].trim() != "GDC"){
						locationFound = false;
					}
				}// end while
			} // end if
		} // end for
		
		for (int h = 1 ; h <= locationCount; h++ ){
			// creating a temp array for each graphical location
			// descArray[h]][2] is number of points in a location
			int tempArrayLength = ((descArray [h]) * 2);
			float[] tempArray = new float[tempArrayLength]; // temp array with rows equal to number of locations and Lat Lon Values
			int tempArrayIndex = 0;
			for (int pointIndex = 1; pointIndex <= descArray[h] ; pointIndex++ ){
				tempArray[tempArrayIndex]= (float) Double.parseDouble(gdcArray[pointIndex][0]);	
				tempArray[tempArrayIndex+1]= (float) Double.parseDouble(gdcArray[pointIndex][1]);
				tempArrayIndex+=2;	
			} //gdc array for end
			
			// Read temp Array
			System.out.println("=====Printing temp Array =============");
			System.out.println("temp  Array length =  " + tempArray.length);
			for (int tempIndex =0; tempIndex < tempArray.length ; tempIndex++){
				System.out.println("temp  Array [ " + tempIndex + " ] = " + tempArray[tempIndex]);
			}
			
			// Set map center to first point of location
			//Auto Zoom
			BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
			BMLC2GUI.mapBean.setScale(48000f);	
			latOpen = tempArray[0];
			lonOpen = tempArray[1];

			// Draw 2525b Symbol          S * G * EV EB -- ** ** *
			String reportLabel = "UNKNOWN---*****";
			if (simciReportType == "BRIDGEREP"){
				reportLabel = "SFGPEVEB--*****";
			}	
			else if (simciReportType == "MINOBREP"){
				reportLabel = "SFGPEXM---*****";									
			}
			else if (simciReportType == "SPOTREP" || simciReportType == "TRKREP"){
				reportLabel = "SUGPU-----*****";
			}
			graphics.add("SIMCIReport "+h, createPoint(reportLabel));				
			graphics.add("SIMCIReport "+h, createPoly(tempArray, 0, 1));	
		} // desc array for end
        return graphics;    
    } // End of SIMCI Report Method
     
    /**
     * Draw order graphics (old). Clears and then fills the given OMGraphicList. 
     * 
     * Start parsing the Array representing the XML document
     * First: create a description array
     * Second: use the description array to get each graphics type
     * Third: call the appropriate drawing method (Point, Line, Area)
     * If it is an area then the last and first points should be the same
     *
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     * @deprecated retained for legacy demo
     */
    public ListEditableOMGraphics createOrderGraphicsOld(ListEditableOMGraphics graphics) {
        graphics.clear();   
        int locationCount = 0;	// number of locations
		int [][] descArray = new int [23][5]; // number , startGDC, endGDC, lengthGDC, noOfPointGDC - 25 locations
		String[][] strDescArray = new String [23][3];
		int startGDC =0;  	 // start index in the array of the location
		int endGDC =0;	  	 // end index in the array of the location
		int lengthGDC =0; 	 // length of the location 
		int noOfPointGDC =0; // number of points in the location. If it is one then it is a point
							 // if it is more than one then it is a line or an area
							 // the line differs from the area in that the area has the same last and first points

		//Parsing an order string and drawing location information 
		String sGraphicsName ="oldOrder";
			
		for (int i=0; i < stringArray.length; i++){
			if (stringArray[i].trim().equals("WhereLocation")){			// Look for location Information
				locationCount++;
				System.out.print("A Location has been discovered #: " + locationCount + "  It started at : " + i);
				startGDC = i;
			
				// Storing the word desc in strDescArray
				strDescArray[locationCount-1][0] = stringArray[i-1].toString();// WhereLocation Type = AREA, LINE, POINT
				strDescArray[locationCount-1][1] = stringArray[i-2].toString();//WhereLocation Class = AREAOFINTEREST, LINEOFDEPARTURE
				strDescArray[locationCount-1][2] = stringArray[i-3].toString();//WhereLocation Label
				sGraphicsName = strDescArray[locationCount-1][2];
				
				WhereOutput(strDescArray[locationCount-1]);  //to System.out
			} // end if
			
			if (stringArray[i].trim().equals("AT") && !stringArray[i-1].trim().equals("WhenTime")){
				endGDC = i;
				lengthGDC = (endGDC - startGDC);
				noOfPointGDC = (endGDC - startGDC)/4;
				System.out.println("  It ended at : " + i + " It is Length is: " + lengthGDC + " No of Points is : " + noOfPointGDC);
				descArray[locationCount-1][0]=locationCount;
				descArray[locationCount-1][1]=startGDC;
				descArray[locationCount-1][2]=endGDC;
				descArray[locationCount-1][3]=lengthGDC;
				descArray[locationCount-1][4]=noOfPointGDC;
			}
		} // end for      
		
		int latLocation = 0, lonLocation = 0, elvLocation = 0;
		int tempArraySize = 0;
		//For each drawing object 
		for (int i=0 ; i < descArray.length ; i++){ //descArray.length
			// creating a temp array for each graphical location
			// the length of the array will be twice as the number of lat,lon points
			//Area : the last lat,;on should be the same lat,lon
			
			tempArraySize = (descArray[i][4])*2;	// A line
			if (strDescArray[i][0].trim().equals("AREA")){  //An Area
				tempArraySize += 2;	
			}
			
			float[] tempArray = new float[tempArraySize];
			int tempArrayIndex = 0;

			for (int h = 0; h < descArray[i][4]; h++) {								
				latLocation = (descArray[i][1])+(2+(4*h));
				lonLocation = (descArray[i][1])+(3+(4*h));
				elvLocation = (descArray[i][1])+(4+(4*h));
				
				// creating a temp array for each graphical location
				tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[latLocation].trim());
				tempArrayIndex++;
				tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[lonLocation].trim());
				tempArrayIndex++;	
			} // end for

			// Auto Center
			BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
			BMLC2GUI.mapBean.setScale(3000000f);
			
			//Area : the last lat,;on should be the same lat,lon
			if (strDescArray[i][0].trim().equals("AREA")){
				tempArray[tempArrayIndex]=tempArray[0];	
				tempArray[tempArrayIndex+1]=tempArray[1];	
				tempArrayIndex+=2;
				graphics.add(strDescArray[i][2],createPoly(tempArray, 0, 1));// the shape is not a point so draw a Poly
			}
			if (strDescArray[i][0].trim().equals("LINE")){
				// A POLY is used to draw a line instead of a LINE because it might not be a straight line
				graphics.add(strDescArray[i][2],createPoly(tempArray, 0, 1));
			}
			// if the object is a point call the draw point method
			if (tempArray.length == 2) {
				System.out.println("You are here... in a point... ready to draw....");
				latOpen = tempArray[0];
	            lonOpen = tempArray[1];
				graphics.add(strDescArray[i][2],createPoint(Color.red));
			}
		} // endfor processing location information of an Order		
        return graphics;
    } // End of method
    
    /**
     * Draws IBML_CBML_OPORD Graphics. Clears and then fills the given OMGraphicList.
     * 
     * Start parsing the Array representing the XML document
     * First: create a description array
     * Second: use the description array to get each graphics type
     * Third: call the appropriate drawing method (Point, Line, Area)
     *
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    
    /* Old code from previous version
    public OMGraphicList createIBML_CBML_OPORDGraphics(OMGraphicList graphics) {
        graphics.clear();
        int locationCount = 0;	// number of locations
		int [][] descArray = new int [23][5]; // number , startGDC, endGDC, lengthGDC, noOfPointGDC - 25 control measures
		String[][] strDescArray = new String [23][3];
		int startGDC =0;  	 // start index in the array of the location
		int endGDC =0;	  	 // end index in the array of the location
		int lengthGDC =0; 	 // length of the location 
		int noOfPointGDC =0; // number of points in the location. If it is one then it is a point
							 // if it is more than one then it is a line or an area
							 // the line differs from the area in that the area has the same last and first points

		// Parsing an order string and drawing location information 
		boolean moreLocation = false;
		int startLocation = 0;
		int nextLocation = 0;
		int pointLocation = 0;
		boolean subLocation = false;
		
		for (int i=0; i < stringArray.length; i++){
			// Look for location Information
			if (stringArray[i].trim().equals("SpecificLocation")) {
				locationCount++;
				System.out.println("A Location has been discovered #: " + locationCount + "  It started at : " + i);
				startGDC = i;  // new location
				startLocation = i;
				
				strDescArray[locationCount-1][0] = stringArray[i+1].toString(); // in IBML_CBML location type index is SpecificLocation index +1
				strDescArray[locationCount-1][1] = stringArray[i].toString(); 	//in IBML_CBML no where class
				strDescArray[locationCount-1][2] = stringArray[i-1].toString(); // in IBML_CBML location Label index is SpecificLocation index -1 
	
				WhereOutput(strDescArray[locationCount-1]);  //to System.out
			} // end if
			
			for (int ii=startGDC+1; ii < stringArray.length; ii++){
				moreLocation = false;
				if (stringArray[i].trim().equals("SpecificLocation")){
					nextLocation = ii;
					moreLocation = true;
				}
			}
			if (moreLocation){
				descArray[locationCount-1][0]=locationCount;
				descArray[locationCount-1][1]=startGDC;		
			}
		} // end for      
		
		int pointCount =0;
		int tempArraySize = 20; // 10 points max per location
		int tempArrayIndex = 0;
		float[] tempArray = new float[tempArraySize];
		String shapeType ="";
		String shapeLabel="";
		
		// // Start For of going through all location shapes
		for (int jj=0; jj < locationCount ;jj++){ //locationCount
			System.out.println( descArray[jj][0]);
			System.out.println( descArray[jj][1]);
			WhereOutput(strDescArray[jj]);  //to System.out
			shapeType = strDescArray[jj][0];
			shapeLabel = strDescArray[jj][2];
						
			for (int j=descArray[jj][1]; j<descArray[jj+1][1];j++ ){
				if (stringArray[j].trim().equals("SpecificPoint")){
					pointLocation = j;
					pointCount++;
					System.out.println("  There is a point in the location ....point count..."  + pointCount + " ..... The new Point is at :" + pointLocation);
					System.out.println("  The Lat is :"  + stringArray[j+1] +"  The Lon is :"  + stringArray[j+2]);
					
					// creating a temp array for each graphical location
					tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[j+1].trim());
					tempArray[tempArrayIndex+1]= (float) Double.parseDouble(stringArray[j+2].trim());
					tempArrayIndex+=2;
					subLocation = true;
				}	
			}
			
			float[] shapeArray = new float[tempArrayIndex]; // the size of the shape array is tempArrayIndex
			for (int shapeArrayIndex = 0; shapeArrayIndex < tempArrayIndex; shapeArrayIndex++){
				shapeArray[shapeArrayIndex]= tempArray[shapeArrayIndex];
				System.out.println(" shapeArray[ " + shapeArrayIndex + "] " + shapeArray[shapeArrayIndex] );
			}
			
			// Drawing
			System.out.println("   ===  Drawing shapeType  is   : " + shapeType +
			                 "\n   ===  Drawing shapeLabel is   : " + shapeLabel +
			                 "\n   ===  Temp Array Index is   : " + tempArrayIndex);
			
			//auto center auto zoom
			BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
			BMLC2GUI.mapBean.setScale(600000f);
		
			if (shapeType.trim().equals("Surface") ){
				System.out.println(" -----------------------Drawing  surface  " );
				addOMGraphic(shapeLabel, createPoly(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if ( shapeType.trim().equals("CorridorArea")){	
				System.out.println(" -----------------------Drawing  CorridorArea " );
				addOMGraphic(shapeLabel, createPoly(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if (shapeType.trim().equals("Line")){
				System.out.println(" -----------------------Drawing  line " );
				addOMGraphic(shapeLabel, createPolyLine(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if (shapeType.trim().equals("Point")){
				System.out.println(" -----------------------Drawing point  " );
				latOpen = shapeArray[0];
		        lonOpen = shapeArray[1];
				addOMGraphic(shapeLabel, createPoint(Color.red));
			}else {
				System.out.println(" Drawing Unknown type, assuming polyline " );
				addOMGraphic(shapeLabel, createPolyLine(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}
			// End Drawing
			
			pointCount = 0;
			tempArrayIndex = 0;
		} // End For of going through all location shapes	
        return graphics;  
    }  
    */  
    
    /**
     * Draw order graphics. Clears and then fills the given OMGraphicList. 
     * 
     * Start parsing the Array representing the XML document
     * First: create a description array
     * Second: use the description array to get each graphics type
     * Third: call the appropriate drawing method (Point, Line, Area)
     * If it is an area then the last and first points should be the same
     *
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    public ListEditableOMGraphics createOrderGraphicsIBML09(ListEditableOMGraphics graphics) {
        graphics.clear();      
        int locationCount = 0;	// number of locations
		int [][] descArray = new int [23][5]; // number , startGDC, endGDC, lengthGDC, noOfPointGDC - 25 control measures
		String[][] strDescArray = new String [23][3];	
		int startGDC =0;  	 // start index in the array of the location
		int endGDC =0;	  	 // end index in the array of the location
		int lengthGDC =0; 	 // length of the location 
		int noOfPointGDC =0; // number of points in the location. If it is one then it is a point
							 // if it is more than one then it is a line or an area
							 // the line differs from the area in that the area has the same last and first points
	
		// Parsing an order string and drawing location information 
		
		System.out.println("============   Drawing IBML Order09   ==================");
		for (int i=0; i < stringArray.length; i++){		
			// Look for location Information
			if (ibmlTagCompare(stringArray[i],"WhereLocation") || 
        ibmlTagCompare(stringArray[i],"Coords")){
				locationCount++;
				System.out.println("A Location has been discovered #: " + 
          locationCount + "  It started at : " + i);
				startGDC = i;
			
				// Storing the word desc in strDescArray
				strDescArray[locationCount-1][0] = stringArray[i-1].toString();// WhereLocation Type = AREA, LINE, POINT 
				strDescArray[locationCount-1][1] = stringArray[i-2].toString();//WhereLocation Class = AREAOFINTEREST, LINEOFDEPARTURE
				strDescArray[locationCount-1][2] = stringArray[i-3].toString();//WhereLocation Label 
				WhereOutput(strDescArray[locationCount-1]);  //to System.out
			} // end if
			
			if (stringArray[i].trim().equals("AT") || (stringArray[i].trim().equals("ALONG")) || 
			   (stringArray[i].trim().equals("BETWEEN"))	|| (stringArray[i].trim().equals("FROM")) ||
			   (stringArray[i].trim().equals("IN"))	|| (stringArray[i].trim().equals("ON"))	|| 
			   (stringArray[i].trim().equals("TO"))	||
			   (stringArray[i].trim().equals("WhenTime") && stringArray[i-1].trim().equals("0"))){ //&& !
				endGDC = i;
				lengthGDC = (endGDC - startGDC);
				noOfPointGDC = (endGDC - startGDC)/4;
				bml.printDebug(
          "  It ended at : " + i + " It is Length is: " + 
            lengthGDC + " No of Points is : " + noOfPointGDC);
				descArray[locationCount-1][0]=locationCount;
				descArray[locationCount-1][1]=startGDC;
				descArray[locationCount-1][2]=endGDC;
				descArray[locationCount-1][3]=lengthGDC;
				descArray[locationCount-1][4]=noOfPointGDC;
				
				System.out.println( descArray[locationCount-1][0]);
				System.out.println( descArray[locationCount-1][1]);
				System.out.println( descArray[locationCount-1][2]);
				System.out.println( descArray[locationCount-1][3]);
				System.out.println( descArray[locationCount-1][4]);
			}
		} // end for      
		
		int latLocation = 0, lonLocation = 0, elvLocation = 0;
		int tempArraySize = 0;
		System.out.println(" descArray.length   : " + descArray.length );
		System.out.println(" locationCount   : " + locationCount );
		
		for (int i=0 ; i < locationCount ; i++){ //descArray.length
			WhereOutput(strDescArray[i]); //to System.out

			// creating a temp array for each graphical location
			// the length of the array will be twice as the number of lat,lon points	
			//Area : the last lat,;on should be the same lat,lon
			tempArraySize = (descArray[i][4])*2;		
			if (strDescArray[i][0].trim().equals("LN")){
				System.out.println(" Reading a line  Info " );
			}
			else if (strDescArray[i][0].trim().equals("PT")){
				System.out.println(" Reading a point  Info " );
			}
			else {				// A line // Along Route
				System.out.println(" Reading a SURFAC Info " );
				tempArraySize += 2;	
			}
			System.out.println(" tempArraySize = " + tempArraySize  );
			
			float[] tempArray = new float[tempArraySize];
			int tempArrayIndex = 0;

			for (int h = 0; h < descArray[i][4]; h++) {							
				latLocation = (descArray[i][1])+(2+(4*h));
				lonLocation = (descArray[i][1])+(3+(4*h));
				elvLocation = (descArray[i][1])+(4+(4*h));
				
				// creating a temp array for each graphical location
				tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[latLocation].trim());
				System.out.println(" tempArray[tempArrayIndex] " + tempArray[tempArrayIndex] );
				tempArray[tempArrayIndex+1]= (float) Double.parseDouble(stringArray[lonLocation].trim());
				System.out.println(" tempArray[tempArrayIndex] " + tempArray[tempArrayIndex+1] );
				tempArrayIndex+=2;
			} // end for

			// Auto Center
			bml.mapBean.setCenter(tempArray[0],tempArray[1]);
			bml.mapBean.setScale(600000f);
				
			if (strDescArray[i][0].trim().equals("LN")){	
				System.out.println(" Drawing  a Line Info " );
				
				// the shape is a Route, so draw a Line between each pair of points 
				// the last point and the first one are not the same
				//for (int j = 0; j < tempArray.length-2; j++){
				//	graphics.add(strDescArray[i][2],createLine(tempArray, j, Color.RED,Color.BLUE));
				//	j++;
				//}
				add(strDescArray[i][2], createPolyLine(tempArray, 
          OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}
			
			// if the location object is a surface
			// then add a point at the end of tempArray that has the values of the first point
			// Draw a Polygon with the last point and the first one are the same
			if (strDescArray[i][0].trim().equals("SURFAC")){
				System.out.println(" Drawing  SURFAC  " );
				tempArray[tempArrayIndex]=tempArray[0];	
				tempArray[tempArrayIndex+1]=tempArray[1];	
				tempArrayIndex += 2;
				
				// the shape is not a point so draw a Poly
				graphics.add(strDescArray[i][2],createPoly(tempArray, 0, 1));
			}
			
			// if the object is a point call the draw point method
			if (tempArray.length == 2) {
				System.out.println(" Drawing Point Info " );
				System.out.println("You are here... in a point... ready to draw....");
				latOpen = tempArray[0];
	            lonOpen = tempArray[1];
				graphics.add(strDescArray[i][2],createPoint(Color.red));
			}
		} // endfor processing location information of an Order	
        add("", createPoint("SFGPUCAA--*****"));
        return graphics;
        
    } // end createOrderGraphicsIBML09()
    
    public void clearGraphics() {
    	omgraphics.clear();
    }

    public void add(String pName, EditableOMGraphic eg) {
    	com.bbn.openmap.omGraphics.OMLabeler label = new com.bbn.openmap.omGraphics.OMTextLabeler(pName);
    	eg.getGraphic().putAttribute(OMGraphic.LABEL, label);
    	omgraphics.add(pName, eg);  //@@@ in old version it is "omgraphics.add(g);"
    }
    
    public EditableOMGraphic getGraphic(String sName) {
    	return omgraphics.getGraphic(sName);
    }
    
    public void removeGraphic(String sName) {
    	omgraphics.removeGraphic(sName);
    }

        /**
     * Draws STOMP Subscriber General Status Report Graphics. Clears and then fills the given OMGraphicList.
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     * 
     * mababneh
     * 11/9/2014
     */
    public void createSubscriberGraphics(String[] subUnitArray, String subDocType) {
    	
    	String subUnitName = null;
    	String subUnitSymbolID = null;
    	float subUnitLat = 0;
    	float subUnitLon = 0;
    	
       
        subUnitLat = (float) Double.parseDouble(subUnitArray[0].trim());
        subUnitLon = (float) Double.parseDouble(subUnitArray[1].trim());
        subUnitName = subUnitArray[2].trim();// "SubUnit"; //subUnitArray[0];
        subUnitSymbolID = subUnitArray[3].trim();//"SFGPUCAA--*****"; //subUnitArray[1];

        System.out.println("Unit Name: " + subUnitName);
        System.out.println("Unit Symbol ID: " + subUnitSymbolID);
        System.out.println("Unit Lat: " + subUnitLat);
        System.out.println("Unit Lon: " + subUnitLon);   	


        // Draw a point - to be replaced with 2525b icon
        latOpen = subUnitLat;
        lonOpen = subUnitLon;
        //add(subUnitName, createPoint(Color.red));
        add(subUnitName, createPoint(subUnitSymbolID));
    	// Set focus on first subUnit lat lon
    	BMLC2GUI.mapBean.setCenter(latOpen, lonOpen);
        BMLC2GUI.mapBean.setScale(6000000f);

        System.out.println("=== Printing Units in--------------- ---- RouteLayer");

        /*
        // Unit & Equipment drawing
    	for (int i=0; i<unitArray.length;i++){
    		unitName = unitArray[i][0];
    		unitSymbolID = unitArray[i][1];
    		unitLat = (float) Double.parseDouble(unitArray[i][2].trim());
        	unitLon = (float) Double.parseDouble(unitArray[i][3].trim());
        	
        	
        	System.out.println("Org Name: " + unitName);
    		System.out.println("Org Symbol ID: " + unitSymbolID);
    		System.out.println("Org Lat: " + unitLat);
    		System.out.println("Org Lon: " + unitLon);   	
        	
    		
        	// Draw a point - to be replaced with 2525b icon
        	latOpen = unitLat;
	        lonOpen = unitLon;
	        //add(unitName, createPoint(Color.red));
	        add(unitName, createPoint(unitSymbolID));
    	} // End for
 	*/	
    	
    	
    
    } // End of method create STOMP Subscriber Graphics
    /**
     * Draws MSDL Graphics. Clears and then fills the given OMGraphicList.
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     * 
     * mababneh
     * 11/9/2011
     */
    public void createMSDLGraphics(String[][] orgArray, String[]environmentArray) {
    	
    	String orgName = null;
    	String orgSymbolID = null;
    	float orgLat = 0;
    	float orgLon = 0;
    	
    	String areaName = null;
    	
    	float areaUpperRightLat = 0;
    	float areaUpperRightLon = 0;
    	float areaLowerLefttLat = 0;
    	float areaLowerLeftLon = 0;
    	float tempAreaArray[] = new float[10]; // array to hold area of interest points.
    	
    	float firstLat = 0, firstLon = 0;
    	// Auto Center
    	firstLat = (float) Double.parseDouble(orgArray[0][2].trim());
    	firstLon = (float) Double.parseDouble(orgArray[0][3].trim());

    	// Set focus on first unit lat lon
    	BMLC2GUI.mapBean.setCenter(firstLat, firstLon);
		BMLC2GUI.mapBean.setScale(6000000f);
		
		System.out.println("=== Printing Units and Equipments ---- RouteLayer");
		
		// Environment drawing
    	
		areaName = environmentArray[0];
    	
		areaUpperRightLat = (float) Double.parseDouble(environmentArray[1].trim());
    	areaUpperRightLon = (float) Double.parseDouble(environmentArray[2].trim());
    	areaLowerLefttLat = (float) Double.parseDouble(environmentArray[3].trim());
    	areaLowerLeftLon = (float) Double.parseDouble(environmentArray[4].trim());
    	
	
    	System.out.println("areaName : " + areaName);
		System.out.println("areaUpperRightLat: " + areaUpperRightLat);
		System.out.println("areaUpperRightLon: " + areaUpperRightLon);
		System.out.println("areaLowerLefttLat " + areaLowerLefttLat);
		System.out.println("areaLowerLefttLon " + areaLowerLeftLon);
    	
		tempAreaArray[0] = areaLowerLefttLat;
		tempAreaArray[1] = areaUpperRightLon;
		tempAreaArray[2] = areaUpperRightLat;
		tempAreaArray[3] = areaUpperRightLon;
		tempAreaArray[4] = areaUpperRightLat;
		tempAreaArray[5] = areaLowerLeftLon;
		tempAreaArray[6] = areaLowerLefttLat;
		tempAreaArray[7] = areaLowerLeftLon;
		tempAreaArray[8] = areaLowerLefttLat;
		tempAreaArray[9] = areaUpperRightLon;
        
        add(areaName, createPolyLine(tempAreaArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
        
		// Unit & Equipment drawing
    	for (int i=0; i<orgArray.length;i++){
    		orgName = orgArray[i][0];
    		orgSymbolID = orgArray[i][1];
    		orgLat = (float) Double.parseDouble(orgArray[i][2].trim());
        	orgLon = (float) Double.parseDouble(orgArray[i][3].trim());
        	
        	
        	System.out.println("Org Name: " + orgName);
    		System.out.println("Org Symbol ID: " + orgSymbolID);
    		System.out.println("Org Lat: " + orgLat);
    		System.out.println("Org Lon: " + orgLon);   	
        	
    		
        	// Draw a point - to be replaced with 2525b icon
        	latOpen = orgLat;
	        lonOpen = orgLon;
	        //add(orgName, createPoint(Color.red));
	        add(orgName, createPoint(orgSymbolID));
	        
    	
    	} // End for
 		
    	
    	
    
    } // End of method create MSDL Graphics
    /**
     * Draws OPORD Graphics. Clears and then fills the given OMGraphicList.
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    public void createOPORDGraphics(String shapeName, String[] shapeStringArray, String shapeType, int noOfCoords) {
        System.out.println(	" Shape Name     : " + shapeName +
                			"\n Shape Type     : " + shapeType +
                			"\n Coords count   : " + noOfCoords );
		float[] tempArray = new float[shapeStringArray.length];
		
		//For each drawing object 
		// convert the string Array to float array
		// the first point is empty, fix later , test now
		for (int i = 0; i < shapeStringArray.length; i++){
			tempArray[i]= (float) Double.parseDouble(shapeStringArray[i].trim());
			System.out.println(" tempArray [" + i + "] " + tempArray[i] );
		}
			
		// Auto Center
		BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
		BMLC2GUI.mapBean.setScale(600000f);
			
		float firstLat = 0, firstLon = 0;
		float lastLat = 0, lastLon = 0;
			
		if (shapeType.equals("LN")||shapeType.equals("LINE")){// the shape is a Line, so draw a PolyLine
			System.out.println(" Drawing  line " );				
			add(shapeName, createPolyLine(tempArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
		}
			// if the location object is a surface
			// then add a point at the end of tempArray that has the values of the first point
			// Draw a Polygon with the last point and the first one are the same
		else if (shapeType.equals("SURFAC")){
			System.out.println(" Drawing  surface  " );
			add(shapeName, createPoly(tempArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
		}
		else if (shapeType.equals("PT")){// if the object is a point call the draw point method
			System.out.println(" Drawing point  " );
			latOpen = tempArray[0];
	        lonOpen = tempArray[1];
			add(shapeName, createPoint(Color.red));
		}
		else {
			System.out.println(" Drawing Unknown type, assuming polyline " );
			add(shapeName, createPolyLine(tempArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
		}
    } // End of method create OPORD Graphics
    
    /**
     * Draws General StatusReport Graphics from old schema and sample. 
     * Clears and then fills the given OMGraphicList. 
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    public ListEditableOMGraphics createGeneralStatusReportGraphics(
      ListEditableOMGraphics graphics, String bmlDocumentType) {
      
      graphics.clear();        
      System.out.println("Processing GeneralStatusReport String................ ");
      System.out.println("The string length is : " + stringArray.length);
      boolean foundFirstUnitID = false;
      latOpen = 0f;
      lonOpen = 0f;
      unitHostility = "";
      sbmlUnitID = "";
      boolean isGsr = false;
      boolean isCbml = (bml.generalBMLType == "CBML");
      boolean isIbml = (bml.generalBMLType == "IBML");
        
      // scan stringArray created by XmlParse to get the data associated with 
      // tags UnitID, Hostility, Latitude and Longitude
      int scanRange = stringArray.length - 1;
			for (int i=0; i < scanRange; i++){	
				System.out.println("Report String Array  [ " + i + " ] == " + stringArray[i]);
		
				// look for GSR
        if(cbmlTagCompare(stringArray[i],"GeneralStatusReport") ||
          ibmlTagCompare(stringArray[i],"GeneralStatusReport"))isGsr = true;
        
        // look for CBML lat/lon, and Hostility
        if(isGsr && isCbml){
          if(cbmlTagCompare(stringArray[i],"Latitude")) 
            if(latOpen == 0f)
					    latOpen = (float) Double.parseDouble(stringArray[i+1].trim());
          if(cbmlTagCompare(stringArray[i],"Longitude")) 
            if(lonOpen == 0f)
              lonOpen = (float) Double.parseDouble(stringArray[i+1].trim());
					
					// Set map center to first point of location and Auto Zoom
					bml.mapBean.setCenter(latOpen,lonOpen);
					bml.mapBean.setScale(48000f);	
					if(bmlDocumentType == "OldGeneralStatusReport")
						bml.mapBean.setScale(6000000f);
          
          if(cbmlTagCompare(stringArray[i],"Hostility"))
            unitHostility = stringArray[i+1];
            
				}// end if(isCbml
        
        // look for IBML lat/lon, and Hostility
        if(isGsr && isIbml){
          if(ibmlTagCompare(stringArray[i],"Latitude")) 
            if(latOpen == 0f)
					    latOpen = (float) Double.parseDouble(stringArray[i+1].trim());
          if(ibmlTagCompare(stringArray[i],"Longitude")) 
            if(lonOpen == 0f)
              lonOpen = (float) Double.parseDouble(stringArray[i+1].trim());
					
					// Set map center to first point of location and Auto Zoom
					bml.mapBean.setCenter(latOpen,lonOpen);
					bml.mapBean.setScale(48000f);	
					if(bmlDocumentType == "OldGeneralStatusReport")
						bml.mapBean.setScale(6000000f);
          
          if(ibmlTagCompare(stringArray[i],"Hostility"))
            unitHostility = stringArray[i+1];
 
				}// end if(isIbml
      } // end For i going through the Report String (data)
        
      // scan again for UnitID no that we have Hostility
      for (int i=0; i < scanRange; i++){	
				if((isGsr && isCbml && ibmlTagCompare(stringArray[i],"UnitID")) ||  // IBML09
            (isGsr && isIbml && cbmlTagCompare(stringArray[i],"UnitID"))) { // CBML
					sbmlUnitID = stringArray[i+1].trim();  // unitID from the CBML GUI Editor to be sent to SBML Client
				  bml.printDebug("Executer unit hostility value is :" + unitHostility );
					bml.printDebug("Executer unit ID value is :" + sbmlUnitID );
					bml.printDebug("============================================================");
												
					// use SBML Client to get UnitID, details, hostility and Unit Symbol Icon Code
				  //	  (0) get unitID
					//   (1) get unit information using UnitID
					//   (2) get unit arm_cat_code
					//   (3) get hostility
					//   (4) calculate unit symbol code milstd2525b to draw the icon on the map
					//   (5) call createPoint method to draw the point in the specifid location
					try {
						// call method to get unit details from the sbmlclient especially arm_cat_code
						unitArm_Cat_Code = getUnitDetails(sbmlUnitID);
          } catch (Exception e) {
						System.err.println(
              "There is a problem in createGeneralStatusReportGraphics " + 
                "to connect to the web services and get unit information");
					  unitArm_Cat_Code = "NKN";
					} // end catch
					
					// Start Calculating the Symbol Code of the SymbolIcon to be displayed
					// Modify the create point method to acceptd a String representing the 
          // Symbol Code milstd2525b code       
					milStd2525bCodeScheme ="S";   
					bml.printDebug("unitHostility to be passed : " + unitHostility);
					milStd2525bAffliation = getHostility(unitHostility);   
					milStd2525bDimention ="G";   
					milStd2525bStatus ="P";   
					bml.printDebug("unitArm_Cat_Code to be passed : " + unitArm_Cat_Code);
					milStd2525bFunctionID = getFunctionID(unitArm_Cat_Code);   
					milStd2525bModifier ="--";   
					milStd2525bCountryCode ="**";   
					milStd2525bOrderOfBattle ="*";
            
				  bml.printDebug("milStd2525bCodeScheme S  : 1 " + milStd2525bCodeScheme);
				  bml.printDebug("milStd2525bAffliation    : 2 " + milStd2525bAffliation);
				  bml.printDebug("milStd2525bDimention  G  : 3 " + milStd2525bDimention);
				  bml.printDebug("milStd2525bStatus     P  : 4 " + milStd2525bStatus);
				  bml.printDebug("milStd2525bFunctionID    : 5 " + milStd2525bFunctionID);
				  bml.printDebug("milStd2525bModifier  --  :11 " + milStd2525bModifier);
				  bml.printDebug("milStd2525bCountryCode **:13 " + milStd2525bCountryCode);
				  bml.printDebug("milStd2525bOrderOfBattle*:15 " + milStd2525bOrderOfBattle);
				  milStd2525bSymbolCode = 
            milStd2525bSymbolCode + 
            milStd2525bCodeScheme + 
            milStd2525bAffliation +
				    milStd2525bDimention + 
            milStd2525bStatus + 
            milStd2525bFunctionID +
            milStd2525bModifier + 
            milStd2525bCountryCode + 
            milStd2525bOrderOfBattle;
				  bml.printDebug(
            "The associated Symbol Code for this unit is : " + 
              milStd2525bSymbolCode);
          
        }// end if(stringArray
	    } // end For i going through the Report String (data)

      // confirm we have all the data needed to draw GSR
      if(!isGsr || (unitHostility.length()==0) || (sbmlUnitID.length()==0) ||
        ((latOpen == 0f) && (lonOpen == 0f))) {
        System.err.println("critical report data missing - can't make report graphic");
        System.err.println("isGSR:"+isGsr+" UnitID:"+sbmlUnitID+
          " Hostility:"+unitHostility+" Latitude:"+latOpen+" Longitude:"+lonOpen);
        return null;
      }

			bml.printDebug("======================================================== ");
			bml.printDebug("Unit ID of the Taskee for this BML Document is : " + sbmlUnitID);
			bml.printDebug("======================================================== ");
      if(latOpen != 0f || lonOpen != 0f)
      {
        bml.printDebug("A Location has been discovered # Lat Value: " + latOpen);
        bml.printDebug("A Location has been discovered # Lon Value: " + lonOpen);
      }
      bml.printDebug(
        "The associated Symbol Code for this unit is : " + milStd2525bSymbolCode);

    // add the graphic component (point) to the list in the location in the report
    if(sbmlUnitID.length() > 0)
      this.add("     "+sbmlUnitID, createPoint(milStd2525bSymbolCode));
    return graphics;

  }// end createGeneralStatusReportGraphics()

    /**
     * Retrieve the <arm_cat_code> from the web services using SBMLClient
     *
     * @param unitID	It takes the ID from the report editor as an input
     */
    public String getUnitDetails(String unitID) throws Exception {
    	String arm_cat_code = "";
    	String listWhoResultString = "";

        // RESTful server does not have query capability yet
        // so we return default arm_cat_code NKN "unknown"
        if (BMLC2GUI.serverType.getIsREST())
           return unitIdToArmCatCode(unitID);
    	
    	//Running the SBML Query through the SBMLClient  
    	// The SBML Query Syntax string
    	String xmlString = "<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "UTF-8" + '"' + "?>" +
    					   "<newwho:ListWho" + " xmlns:bml=" + '"' + "http://netlab.gmu.edu/IBML" + '"' + 
    					   " xmlns:newwho=" + '"'	+("http://netlab.gmu.edu/JBML/BML" + '"'+ 
    					   " xmlns:jc3iedm=" + '"' +	"urn:int:nato:standard:mip:jc3iedm:3.1a:oo:2.0"	+'"' + 
    					   ">" + "<bml:UnitID>"	+ unitID + "</bml:UnitID>" + "</newwho:ListWho>"); 

    	//SBMLClient - Secure or non-secure depending on library implementation
    	SBMLClient sbmlClient = new SBMLClient(BMLC2GUI.sbmlReportServerName);   
    	
    	try {
    		bml.printDebug("Starting the Web Service query ");
    		
    		// call the callSBML method to execute the query
    		listWhoResultString = sbmlClient.sbmlProcessBML(xmlString, BMLC2GUI.sbmlUnitInfoDomainName,"IBML");
		} catch (Exception_Exception e2) {
			System.err.println("The query execution was unsuccessful....... ");		
		}

    	bml.printDebug("The  query result is : " + listWhoResultString);  
        bml.printDebug("========= String list who result is = " + listWhoResultString);
        bml.printDebug("========= Sub String list who result is = " + listWhoResultString.indexOf("arm_cat_code"));
        
        // the listWhoResultString now contains the xml result string that we need to look into
        // to find the arm_cat_code of the specified unit
        // Now we either create an xml file from this string and parse it (DOM)
        // or just extract the value from the string directly    
        bml.printDebug("========Final ======String list who result is = " + listWhoResultString);
        int elementStartIndex;// Index of the start of the <arm_cat_code> in the string read from the file
        int elementEndIndex;// Index of the start of the </arm_cat_code> in the string read from the file
        elementStartIndex = listWhoResultString.indexOf("<newwho:arm_cat_code>");
        elementEndIndex = listWhoResultString.indexOf("</newwho:arm_cat_code>");        
        bml.printDebug("======start index of substring is = " + 
          elementStartIndex);
        bml.printDebug("======start index of substring is = " + 
          listWhoResultString.indexOf("<newwho:arm_cat_code>"));
        bml.printDebug("======start index of substring is = " + 
          elementEndIndex);
        bml.printDebug("======start index of substring is = " + 
          listWhoResultString.indexOf("</bml:arm_cat_code>"));
        
       
        // start retrieving the substring starting the elementStartIndex + 21
        // 18 is the length of the string <bml:arm_cat_code> . Get the String starting after it
        bml.printDebug("====== <newwho:arm_cat_code> of substring is = " + 
          listWhoResultString.substring(elementStartIndex + 21, elementEndIndex));        
        arm_cat_code = listWhoResultString.substring(elementStartIndex + 21, elementEndIndex);
        
        if (arm_cat_code.equals("")){  
          arm_cat_code = unitIdToArmCatCode(unitID);
        	//arm_cat_code="ARMANT";
        	// Default value in case couldn't get it from SBMLCient Web Service
        	// Or use Unknown - this might be better.
        	JOptionPane.showMessageDialog(null, "The Value of temp arm_cat_code is : " +arm_cat_code);
        }
    	return arm_cat_code;
      
    } // end getUnitDetails()

    /**
	 * Returns the unit Function ID part of the milstd2525b symbol Code
	 *	
	 * Arm Cat Codes : AIRDEF ARMANT ARMOUR ARTLRY AV AVAFW AVARW INF NKN RECCE 
	 * @param arm_cat_code	A milstd2525b symbol that we get through the SBMLClient query.
     */
    public String getFunctionID(String arm_cat_code){
    	String symbolFunctionID ="";
    	System.out.println("==============================================");
    	System.out.println("Inside get Function ID part of the symbol Code ");
    	System.out.println("Passed arm_cat_code value is : " + arm_cat_code);
    	System.out.println("==============================================");
    	
    	if (arm_cat_code.equals("AIRDEF")){         // AIR DEFENSE
    		symbolFunctionID = "UCD---";
    	}else if (arm_cat_code.equals("ARMANT")){   // ANTI ARMOR
    		symbolFunctionID = "UCAA--";
    	}else if (arm_cat_code.equals("ARMOUR")){   // ARMOR
    		symbolFunctionID = "UCA---";
    	}else if (arm_cat_code.equals("ARTLRY")){   // FIELD ARTILLERY
    		symbolFunctionID = "UCF---";
    	}else if (arm_cat_code.equals("AV")){       // AVIATION
    		symbolFunctionID = "UCV---";
    	}else if (arm_cat_code.equals("AVAFW")){    // FIXED WING
    		symbolFunctionID = "UCVF--";
    	}else if (arm_cat_code.equals("AVARW")){    // ROTARY WING
    		symbolFunctionID = "UCVR--";
    	}else if (arm_cat_code.equals("INF")){      // INFANTRY
    		symbolFunctionID = "UCI---";
    	}else if (arm_cat_code.equals("NKN")){      // Not known
    		symbolFunctionID = "------";	
    	}else if (arm_cat_code.equals("RECCE")){    // RECONNAISSANCE
    		symbolFunctionID = "UCR---";
    	}else {										// UNKNOWN
    		System.out.print("(Unknown) ");
    		symbolFunctionID = "------";
    	}
		System.out.println(arm_cat_code + " unit");

    	return symbolFunctionID; 
    } // End of getFunctionID

    /**
     * Returns the unit hostility or affiliation part of the milstd2525b symbol Code
     *
     * @param hostility		A value that we get through the SBMLClient query
     */
    public String getHostility(String hostility){
    	String symbolAffiliation ="";
    	System.out.println("==============================================");
    	System.out.println("Inside get Hostility part of the symbol Code ");
    	System.out.println("Passed hostility value is : " + hostility);
    	System.out.println("==============================================");
    	
    	symbolAffiliation = "*"; //default to hostile aka unknown or AHO, AIV, ANT, IV
    	if (hostility.equals("AFR")){      
    		symbolAffiliation = "A";	 // Assumed Friend
    	}else if (hostility.equals("FAKER")){
    		symbolAffiliation = "K";	
    	}else if (hostility.equals("FR")){  // Friend
    		symbolAffiliation = "F";
    	}else if (hostility.equals("HO")){  // Hostile
    		symbolAffiliation = "H";
    	}else if (hostility.equals("JOKER")){    		
    		symbolAffiliation = "J";	
    	}else if (hostility.equals("NEUTRL")){
    		symbolAffiliation = "N";	
    	}else if (hostility.equals("PENDNG")){
    		symbolAffiliation = "P";	
    	}else if (hostility.equals("SUSPCT")){
    		symbolAffiliation = "S";	
    	}else if (hostility.equals("UNK")){  // UNKNOWN
    		symbolAffiliation = "U";	
    	}else {
    		System.out.println("Unknown Hostility");
    	}  	
    	return symbolAffiliation;
    	
    } // End of getHostility      

    /**
     * Creates an OMLine from the given parameters.
     * 
     * @param tempArray	Contains two pair of lat/long
     * @param color		The line's color
     * @param selColor	The line's selected color
     * @return An OMLine with the given properties
     */
    public EditableOMLine createLine(float[] tempArray, int j, Color color, Color selColor) {
        OMLine line = new OMLine(tempArray[j], tempArray[j+1], tempArray[j+2], tempArray[j+3], 1);
        line.setLinePaint(color);
        line.setSelectPaint(selColor);
        line.setFillColor(Color.BLUE);
        EditableOMLine eline = new EditableOMLine(line);
        return eline;
    }
    
    public EditableOMPoint createPoint(Color color) {       
    	OMPoint point = new OMPoint(latOpen, lonOpen);
    	point.setLinePaint(color);
    	point.setFillColor(Color.RED);
    	EditableOMPoint epoint = new EditableOMPoint(point);
    	epoint.setProjection(projection);	
    	return epoint;
    }

    /**
     * Draws a milstd2525b symbol icon on the map in the specified location
     */
    public EditableOMScalingRaster createPoint(String symbolCode) {
        Dimension di = new Dimension(100,100);
    	ImageIcon ii = new ImageIcon("");
    	
    	// testing
    	System.out.println(" The passed symbol is : " + symbolCode);
    	
        PNGSymbolImageMaker pngsim = 
            new PNGSymbolImageMaker(
              bml.guiFolderLocation+"BMLC2GUI/milstd2525b/milStd2525_png");
        SymbolReferenceLibrary srl = new SymbolReferenceLibrary(pngsim);
     
        if (symbolCode.length()!= 15){
     
        	System.out.println("  Error in Symbol Code, Length not 15 Character");
        	System.out.println("  Display another symbol temporarily");
        	
        	// Test to be removed when symbol code is always correct
        	ii = srl.getIcon("SFGPUCAA--*****", di); 
        }
        else {
        	
        	ii = srl.getIcon(symbolCode, di);
        	//ii = srl.getIcon(symbolCode, di);// debugx why twice?

                // fallback if result is bad
                if(ii == null)
                {
                    System.out.println("can't produce icon for symbol:" + symbolCode);
                    System.out.println("substituting symbol SFGPUCAA--*****");
                    ii = srl.getIcon("SFGPUCAA--*****", di);
                }
                if(ii == null)
                {
                    System.err.println("fallback symbol code failed");
                    return null;
                }
        	
        	System.out.println(" Drawing milstd 2525b symbol........" + ii.toString());
        	if (ii.getImageLoadStatus()!=8){
        		System.out.println("Symbol Code not found in MilStd2525b image library........ ");
        		ii = srl.getIcon("SFGPUCAAD-*****", di);
        		//SymbolPart sp = new SymbolPart("fff");
        	}
        }
        
        //ii = srl.getIcon("SFGPUCAAD-*****", di); 
        				  
        // Unknown WAR.GRDTRK.UNT.CBT.AARM "SUGPUCAA--*****"
        
        // draw a unit symbol on the map
        OMScalingIcon omsi = new OMScalingIcon(latOpen, lonOpen, ii); 
        EditableOMScalingRaster eomsr = new EditableOMScalingRaster(omsi);
        return eomsr;
    }
    
    public EditableOMCircle createCircle(float lat1, float lon1, float dia1, Color color) {
    	OMCircle circle = new OMCircle(lat1,lon1, dia1);
    	circle.setLinePaint(color);
    	EditableOMCircle ecircle = new EditableOMCircle(circle);
    	return ecircle;
    }

    public EditableOMPoly createPolyLine(float[] llPoints, int units, int lType) {
    	OMPoly poly = new OMPoly(llPoints, units, lType);
    	poly.setLinePaint(Color.BLUE);
    	EditableOMPoly  epoly = new EditableOMPoly(poly);
    	epoly.setProjection(projection);
    	return epoly;
    }

    public EditableOMPoly createPoly(float[] llPoints, int units, int lType) {
    	OMPoly poly = new OMPoly(llPoints, units, lType);
    	poly.setLinePaint(Color.BLUE);
    	poly.setFillPaint(Color.yellow);
    	EditableOMPoly  epoly = new EditableOMPoly(poly);
    	return epoly;
    }
      
    String unitIdToArmCatCode(String unitID) {
      
      // ToDo: when RESTful server supports query we will
      // fetch arm_cat_code from it here
      // For now, need to add other common codes here
      if(unitID.endsWith("INF"))return "INF";
      if(unitID.endsWith("ARMOR"))return "ARMOUR";
      return "NKN";
    }
 
    /**
     * Layer overrides (multiple methods)
     */

    public void paint(Graphics g) {
        omgraphics.render(g);
    }

    public MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * ProjectionListener interface implementation 
     */
    public void projectionChanged(ProjectionEvent e) {
        projection = (Projection) e.getProjection().makeClone();
        omgraphics.setProjection(projection);
        repaint();
    }

    /**   
     * MapMouseListener interface implementation (multiple methods)
     * 
     * @see com.bbn.openmap.*
     * @see com.bbn.openmap.event.*
     */
    public String[] getMouseModeServiceList() {
        String[] ret = new String[1];
        ret[0] = SelectMouseMode.modeID; // "Gestures"
        return ret;
    }

    public boolean mousePressed(MouseEvent e) {
    	mouseButtonDown=true;
    	selectedPoint = omgraphics.getGrabPoint(e);
    	if (selectedPoint!=null) {
    		 selectedPoint.select();
    	}
        return false;
    }

    public boolean mouseReleased(MouseEvent e) {
    	mouseButtonDown=false;
    	if (selectedPoint!=null) {
    		selectedPoint.set(e.getX(), e.getY());
        	EditableOMGraphic g = omgraphics.getSelectedGraphic();
    		g.setGrabPoints();
    	}
    	BMLC2GUI.mapBean.setScale(BMLC2GUI.mapBean.getScale());    	
        return false;
    }

    public boolean mouseClicked(MouseEvent e) {
//        selectedGraphic = omgraphics.selectClosest(e.getX(),
//                e.getY(),
//                10.0f);
//    	selectedPoint = omgraphics._getMovingPoint(e);

        if (selectedGraphic != null) {
            switch (e.getClickCount()) {
            case 1:
//                System.out.println("Show Info: " + ((OMTextLabeler)selectedGraphic.getAttribute(OMGraphic.LABEL)).getData());
                break;
            case 2:
//                System.out.println("Request URL: " + selectedGraphic);
                break;
            default:
                break;
            }
            return true;
        } else {
            return false;
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public boolean mouseDragged(MouseEvent e) {
//    	System.out.println("RouteLayer::MouseDragged to " + projection.inverse(e.getPoint()));
    	return false;
    }

    public boolean mouseMoved(MouseEvent e) {
//    	selectedPoint = omgraphics.getGrabPoint(e);
//    	if (selectedPoint!=null) {
//    		 selectedPoint.select();
//    		 System.out.println("RouteLayer::MouseMoved selected: " + selectedPoint.getDescription());
//    	}
    	
        return true;
    }

    public void mouseMoved() {
//    	if (!mouseButtonDown) {
//    		omgraphics.deselectAll();
//    	}
        repaint();
    }
    
    public String getSelectedObject() {
    	EditableOMGraphic g = omgraphics.getSelectedGraphic();
    	if (g!=null){
    		selectedGraphic = g.getGraphic();
    		return ((OMTextLabeler)selectedGraphic.getAttribute(OMGraphic.LABEL)).getData();
    	}
    	else return null;
    }
    
    public int getSelectedPointIndex() {
    	return omgraphics.getSelectedPointIndex();
    }
    
    public LatLonPoint getProjectedPoint(Point e) {
    	return projection.inverse(e);
    }
    
    /**
     * Prints formatted output
     */    
    public void WhereOutput(String[] whereOut){ //simplifies/standardizes common output
    	System.out.println(" WHERE Type  : " + whereOut[0] +
                         "\n WHERE Class : " + whereOut[1] +
                         "\n WHERE Label : " + whereOut[2]);
    }
	/**
	 * Draws IBML_CBML_OPORD Graphics. Clears and then fills the given OMGraphicList.
	 * 
	 * Start parsing the Array representing the XML document
	 * First: create a description array
	 * Second: use the description array to get each graphics type
	 * Third: call the appropriate drawing method (Point, Line, Area)
	 *
	 * @param graphics The OMGraphicList to clear and populate
	 * @return the graphics list, after being cleared and filled
	 */
	
	/* Old code from previous version
	public OMGraphicList createIBML_CBML_OPORDGraphics(OMGraphicList graphics) {
	    graphics.clear();
	    int locationCount = 0;	// number of locations
		int [][] descArray = new int [23][5]; // number , startGDC, endGDC, lengthGDC, noOfPointGDC - 25 control measures
		String[][] strDescArray = new String [23][3];
		int startGDC =0;  	 // start index in the array of the location
		int endGDC =0;	  	 // end index in the array of the location
		int lengthGDC =0; 	 // length of the location 
		int noOfPointGDC =0; // number of points in the location. If it is one then it is a point
							 // if it is more than one then it is a line or an area
							 // the line differs from the area in that the area has the same last and first points
	
		// Parsing an order string and drawing location information 
		boolean moreLocation = false;
		int startLocation = 0;
		int nextLocation = 0;
		int pointLocation = 0;
		boolean subLocation = false;
		
		for (int i=0; i < stringArray.length; i++){
			// Look for location Information
			if (stringArray[i].trim().equals("SpecificLocation")) {
				locationCount++;
				System.out.println("A Location has been discovered #: " + locationCount + "  It started at : " + i);
				startGDC = i;  // new location
				startLocation = i;
				
				strDescArray[locationCount-1][0] = stringArray[i+1].toString(); // in IBML_CBML location type index is SpecificLocation index +1
				strDescArray[locationCount-1][1] = stringArray[i].toString(); 	//in IBML_CBML no where class
				strDescArray[locationCount-1][2] = stringArray[i-1].toString(); // in IBML_CBML location Label index is SpecificLocation index -1 
	
				WhereOutput(strDescArray[locationCount-1]);  //to System.out
			} // end if
			
			for (int ii=startGDC+1; ii < stringArray.length; ii++){
				moreLocation = false;
				if (stringArray[i].trim().equals("SpecificLocation")){
					nextLocation = ii;
					moreLocation = true;
				}
			}
			if (moreLocation){
				descArray[locationCount-1][0]=locationCount;
				descArray[locationCount-1][1]=startGDC;		
			}
		} // end for      
		
		int pointCount =0;
		int tempArraySize = 20; // 10 points max per location
		int tempArrayIndex = 0;
		float[] tempArray = new float[tempArraySize];
		String shapeType ="";
		String shapeLabel="";
		
		// // Start For of going through all location shapes
		for (int jj=0; jj < locationCount ;jj++){ //locationCount
			System.out.println( descArray[jj][0]);
			System.out.println( descArray[jj][1]);
			WhereOutput(strDescArray[jj]);  //to System.out
			shapeType = strDescArray[jj][0];
			shapeLabel = strDescArray[jj][2];
						
			for (int j=descArray[jj][1]; j<descArray[jj+1][1];j++ ){
				if (stringArray[j].trim().equals("SpecificPoint")){
					pointLocation = j;
					pointCount++;
					System.out.println("  There is a point in the location ....point count..."  + pointCount + " ..... The new Point is at :" + pointLocation);
					System.out.println("  The Lat is :"  + stringArray[j+1] +"  The Lon is :"  + stringArray[j+2]);
					
					// creating a temp array for each graphical location
					tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[j+1].trim());
					tempArray[tempArrayIndex+1]= (float) Double.parseDouble(stringArray[j+2].trim());
					tempArrayIndex+=2;
					subLocation = true;
				}	
			}
			
			float[] shapeArray = new float[tempArrayIndex]; // the size of the shape array is tempArrayIndex
			for (int shapeArrayIndex = 0; shapeArrayIndex < tempArrayIndex; shapeArrayIndex++){
				shapeArray[shapeArrayIndex]= tempArray[shapeArrayIndex];
				System.out.println(" shapeArray[ " + shapeArrayIndex + "] " + shapeArray[shapeArrayIndex] );
			}
			
			// Drawing
			System.out.println("   ===  Drawing shapeType  is   : " + shapeType +
			                 "\n   ===  Drawing shapeLabel is   : " + shapeLabel +
			                 "\n   ===  Temp Array Index is   : " + tempArrayIndex);
			
			//auto center auto zoom
			BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
			BMLC2GUI.mapBean.setScale(600000f);
		
			if (shapeType.trim().equals("Surface") ){
				System.out.println(" -----------------------Drawing  surface  " );
				addOMGraphic(shapeLabel, createPoly(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if ( shapeType.trim().equals("CorridorArea")){	
				System.out.println(" -----------------------Drawing  CorridorArea " );
				addOMGraphic(shapeLabel, createPoly(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if (shapeType.trim().equals("Line")){
				System.out.println(" -----------------------Drawing  line " );
				addOMGraphic(shapeLabel, createPolyLine(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}else if (shapeType.trim().equals("Point")){
				System.out.println(" -----------------------Drawing point  " );
				latOpen = shapeArray[0];
		        lonOpen = shapeArray[1];
				addOMGraphic(shapeLabel, createPoint(Color.red));
			}else {
				System.out.println(" Drawing Unknown type, assuming polyline " );
				addOMGraphic(shapeLabel, createPolyLine(shapeArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
			}
			// End Drawing
			
			pointCount = 0;
			tempArrayIndex = 0;
		} // End For of going through all location shapes	
	    return graphics;  
	}  
	*/  
	
	/**
	 * Draw order graphics. Clears and then fills the given OMGraphicList. 
	 * 
	 * Start parsing the Array representing the XML document
	 * First: create a description array
	 * Second: use the description array to get each graphics type
	 * Third: call the appropriate drawing method (Point, Line, Area)
	 * If it is an area then the last and first points should be the same
	 *
	 * @param graphics The OMGraphicList to clear and populate
	 * @return the graphics list, after being cleared and filled
	 */
	public ListEditableOMGraphics createOrderGraphics(ListEditableOMGraphics graphics) {
	    graphics.clear();      
	    int locationCount = 0;	// number of locations
		int [][] descArray = new int [23][5]; // number , startGDC, endGDC, lengthGDC, noOfPointGDC - 25 control measures
		String[][] strDescArray = new String [23][3];	
		int startGDC =0;  	 // start index in the array of the location
		int endGDC =0;	  	 // end index in the array of the location
		int lengthGDC =0; 	 // length of the location 
		int noOfPointGDC =0; // number of points in the location. If it is one then it is a point
							 // if it is more than one then it is a line or an area
							 // the line differs from the area in that the area has the same last and first points
	
		// Parsing an order string and drawing location information 
		for (int i=0; i < stringArray.length; i++){		
			// Look for location Information
			if (stringArray[i].trim().equals("WhereLocation")|| stringArray[i].trim().equals("Coords")){
				locationCount++;
				System.out.println("A Location has been discovered #: " + locationCount + "  It started at : " + i);
				startGDC = i;
			
				// Storing the word desc in strDescArray
				strDescArray[locationCount-1][0] = stringArray[i-1].toString();// WhereLocation Type = AREA, LINE, POINT 
				strDescArray[locationCount-1][1] = stringArray[i-2].toString();//WhereLocation Class = AREAOFINTEREST, LINEOFDEPARTURE
				strDescArray[locationCount-1][2] = stringArray[i-3].toString();//WhereLocation Label 
				WhereOutput(strDescArray[locationCount-1]);  //to System.out
			} // end if
			
			if (stringArray[i].trim().equals("AT") || (stringArray[i].trim().equals("WhenTime") && stringArray[i-1].trim().equals("0"))){ //&& !
				endGDC = i;
				lengthGDC = (endGDC - startGDC);
				noOfPointGDC = (endGDC - startGDC)/4;
				System.out.println("  It ended at : " + i + " It is Length is: " + lengthGDC + " No of Points is : " + noOfPointGDC);
				descArray[locationCount-1][0]=locationCount;
				descArray[locationCount-1][1]=startGDC;
				descArray[locationCount-1][2]=endGDC;
				descArray[locationCount-1][3]=lengthGDC;
				descArray[locationCount-1][4]=noOfPointGDC;
				
				System.out.println( descArray[locationCount-1][0]);
				System.out.println( descArray[locationCount-1][1]);
				System.out.println( descArray[locationCount-1][2]);
				System.out.println( descArray[locationCount-1][3]);
				System.out.println( descArray[locationCount-1][4]);
			}
		} // end for      
		
		int latLocation = 0, lonLocation = 0, elvLocation = 0;
		int tempArraySize = 0;
		System.out.println(" descArray.length   : " + descArray.length );
		System.out.println(" locationCount   : " + locationCount );
		
		for (int i=0 ; i < locationCount ; i++){ //descArray.length
			WhereOutput(strDescArray[i]); //to System.out
	
			// creating a temp array for each graphical location
			// the length of the array will be twice as the number of lat,lon points	
			//Area : the last lat,;on should be the same lat,lon
			tempArraySize = (descArray[i][4])*2;		
			if (strDescArray[i][0].trim().equals("Along")){
				System.out.println(" Reading Route Along  Info " );
			}
			else  {				// A line // Along Route
				System.out.println(" Reading SURFAC Info " );
				tempArraySize += 2;	
			}
			System.out.println(" tempArraySize = " + tempArraySize  );
			
			float[] tempArray = new float[tempArraySize];
			int tempArrayIndex = 0;
	
			for (int h = 0; h < descArray[i][4]; h++) {							
				latLocation = (descArray[i][1])+(2+(4*h));
				lonLocation = (descArray[i][1])+(3+(4*h));
				elvLocation = (descArray[i][1])+(4+(4*h));
				
				// creating a temp array for each graphical location
				tempArray[tempArrayIndex]= (float) Double.parseDouble(stringArray[latLocation].trim());
				System.out.println(" tempArray[tempArrayIndex] " + tempArray[tempArrayIndex] );
				tempArray[tempArrayIndex+1]= (float) Double.parseDouble(stringArray[lonLocation].trim());
				System.out.println(" tempArray[tempArrayIndex] " + tempArray[tempArrayIndex+1] );
				tempArrayIndex+=2;
			} // end for
	
			// Auto Center
			BMLC2GUI.mapBean.setCenter(tempArray[0],tempArray[1]);
			BMLC2GUI.mapBean.setScale(600000f);
				
			if (strDescArray[i][0].trim().equals("Along")){	
				System.out.println(" Drawing  Route Along Info " );
				
				// the shape is a Route, so draw a Line between each pair of points 
				// the last point and the first one are not the same
				//for (int j = 0; j < tempArray.length-2; j++){
				//	graphics.add(strDescArray[i][2],createLine(tempArray, j, Color.RED,Color.BLUE));
				//	j++;
				//}
				add(strDescArray[i][2], createPolyLine(tempArray, OMGraphic.DECIMAL_DEGREES, OMGraphic.DECLUTTERTYPE_SPACE));
				
			}
			
			// if the location object is a surface
			// then add a point at the end of tempArray that has the values of the first point
			// Draw a Polygon with the last point and the first one are the same
			if (strDescArray[i][0].trim().equals("SURFAC")){
				System.out.println(" Drawing  SURFAC  " );
				tempArray[tempArrayIndex]=tempArray[0];	
				tempArray[tempArrayIndex+1]=tempArray[1];	
				tempArrayIndex += 2;
				
				// the shape is not a point so draw a Poly
				graphics.add(strDescArray[i][2],createPoly(tempArray, 0, 1));
			}
			
			// if the object is a point call the draw point method
			if (tempArray.length == 2) {
				System.out.println(" Drawing Point Info " );
				System.out.println("You are here... in a point... ready to draw....");
				latOpen = tempArray[0];
	            lonOpen = tempArray[1];
				graphics.add(strDescArray[i][2],createPoint(Color.red));
			}
		} // endfor processing location information of an Order		
	    return graphics;
	} // End of method create Order Graphics

} // End of RouteLayer Class