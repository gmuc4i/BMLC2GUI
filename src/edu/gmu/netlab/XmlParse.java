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
/**
 * Reads an XML string from file and parses it to enable extracting elements
 */
package edu.gmu.netlab;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import java.io.*;

/**
 * @author jmarkpullen with input from 
 * https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
 */
public class XmlParse {
  
  String xmlString;   // XML read from file
  Document xmlDoc;    // XML as a DOM Document
  String newLine = System.getProperty("line.separator");

 /**
  * constructor parses the XML into structure for extraction
  */
  public XmlParse(String xmlString) {
  
    // create a DocumentBuilder
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;
    try {
      dbFactory = DocumentBuilderFactory.newInstance();
      dBuilder = dbFactory.newDocumentBuilder();
    }
    catch(ParserConfigurationException pce)
    { 
      System.err.println("ParserConfigurationException preparing to parse XML" + pce);
      return;
    }
    
    // create a document
    StringBuilder sb = new StringBuilder();
    sb.append(xmlString);
    ByteArrayInputStream bais = null;
    try {
      bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
    } catch(UnsupportedEncodingException uee)
    { // can't happen with literal
    }
    try {
      xmlDoc = dBuilder.parse(bais);
    }
    catch (Exception e)
    { 
      System.err.println("Exception preparing to parse XML" + e);
      return;
    }
    xmlDoc.getDocumentElement().normalize();

  }// end constructor xmlParse
  
 /**
  * returns root Element
  */
  String getRootName()
  {
    return xmlDoc.getDocumentElement().getNodeName();
  }
  
 /**
   * returns the XML read from file as a String
   */
  String getXml()
  {
    return xmlString;
  }
  
/**
  * returns array of data values for Elements of given name
  *
  **/
  String[] getElementDataByTagName(String elementName)
  {
    NodeList dataNodes = xmlDoc.getElementsByTagName(elementName);
    int listLength = dataNodes.getLength();
    
    // scan Nodes in the list extracting data
    int outputLength = 0;
    String[] tempData = new String[listLength];
    for(int index=0; index < listLength; ++index){
      Node testNode = dataNodes.item(index);
      if(testNode.getNodeType() != Node.ELEMENT_NODE)continue;
      tempData[outputLength++] = 
        removeAllButFirstValue(testNode.getTextContent());
    }// end for index
    
    // return only elements with values
    String[] returnData = new String[outputLength];
    System.arraycopy(tempData,0,returnData,0,outputLength);
    return returnData;
    
  }// end getElementDataByTagName
  
  /**
   * multiple subelements can produce a lot of data;
   * cut off any values after the first
   */
  String removeAllButFirstValue(String contentString) {
    String trimmedContent = contentString.trim();
    int newLineIndex = trimmedContent.indexOf(' ');// newline?
    if(newLineIndex < 0)newLineIndex = trimmedContent.length();
    return trimmedContent.substring(0, newLineIndex);
  }
  
/**
  * returns array of names & data values associated with Nodes of given name
  * that are Elements; sequence is Node name followed
  * by name then data of each sub-Node that matches nodeName in subNodeNames
  * and finally by groups with groupNodeNames in index order (used for 
  * groups of location point attributes: lat/lon/elev)
  *
  **/
  String[] getElementNameDataByTagName(
    String nodeName,
    String[] subNodeNames,
    String[] groupNodeNames,
    String nsPrefix)
  {
    // find top-level list of elements
    int outputLength = 0;
    NodeList dataNodes = xmlDoc.getElementsByTagName(nodeName);
    int listLength = dataNodes.getLength();
    if(listLength == 0) {
      dataNodes = xmlDoc.getElementsByTagName(nsPrefix+nodeName);
      listLength = dataNodes.getLength();
      if(listLength == 0) {
        System.err.println(
          "error in XML: getElementsByTagName returns empty list for " + nodeName);
        return null;
      }
    }
    
    // array to hold results
    // using conservative estimate 100 entries per groupNode with 6 in group
    // TODO: pre-count the entries
    int subNodesLength = subNodeNames.length;
    int groupNodesLength = groupNodeNames.length;
    String[] tempData = new String[listLength*(subNodesLength+groupNodesLength*600)];
    int groupDataLength = 0;

    // scan Nodes in the list
    for(
      int listIndex=0; 
      listIndex < listLength; 
      ++listIndex)
    {
      Node subNode = dataNodes.item(listIndex);
      if(subNode.getNodeType() != Node.ELEMENT_NODE)continue;
      tempData[outputLength++] = subNode.getNodeName();

      // scan subnodes of the node
      Element subNodeElement = (Element) subNode;
      for(
        int subNodesIndex = 0; 
        subNodesIndex < subNodesLength; 
        ++subNodesIndex)
      {
        NodeList subNodeList = 
          subNodeElement.getElementsByTagName(subNodeNames[subNodesIndex]);
        String textContent = subNodeElement.getTextContent();
        
        // hack to deal with "buried hostility"  TODO: fix this!
        if(subNodeNames[subNodesIndex].equals("Hostility")){
          if(textContent.contains("FR"))textContent = "FR";
          else if(textContent.contains("HO"))textContent = "HO"; 
        }
        
        if(textContent != null)if(textContent.length() > 0){
          tempData[outputLength++] = subNodeNames[subNodesIndex];
          tempData[outputLength++] = removeAllButFirstValue(textContent);
        }
      }// end for subNodesIndex  
        
      // scan group subnodes, extracting them in index order
      int groupIndex;
      NodeList[] groupNodeList = new NodeList[groupNodesLength];
      for(
        groupIndex = 0; 
        groupIndex < groupNodesLength; 
        ++groupIndex)
      {
        // get the NodeList for each element of the group
        groupNodeList[groupIndex] = 
          subNodeElement.getElementsByTagName(groupNodeNames[groupIndex]);
        if(groupNodeList[groupIndex].getLength() == 0)
          groupNodeList[groupIndex] =
            subNodeElement.getElementsByTagName(nsPrefix+groupNodeNames[groupIndex]);
        
        // all must be of same length; confirm that
        groupDataLength = groupNodeList[0].getLength();
        if(groupNodeList[groupIndex].getLength() != groupDataLength)
        {
          System.err.println(
            "error in XML: number of elements for " + 
            groupNodeNames[0] + " and " + groupNodeNames[groupIndex] +
            " do not match");
          return null;
        }
      }// end for groupIndex
          
      // emit the data into output stringarray in index order
      for(
        int groupDataIndex = 0;
        groupDataIndex < groupDataLength;
        groupDataIndex++)
      {
        for(
          groupIndex = 0;
          groupIndex < groupNodesLength;
          ++groupIndex)
        {
          Node groupSubNode = 
            groupNodeList[groupIndex].item(groupDataIndex);
          tempData[outputLength++] = groupNodeNames[groupIndex];
          tempData[outputLength++] = 
            removeAllButFirstValue(groupSubNode.getTextContent());
        }// end for groupIndex
      }// end for groupDataIndex
    }// end for listIndex
    
    // return only elements with values
    String[] returnData = new String[outputLength];
    System.arraycopy(tempData,0,returnData,0,outputLength);
    return returnData;
    
  }// end getElementNameDataByTagName()
  
}// end class xmlParse
