

/**
 * Java REST Client
 */
package edu.gmu.netlab;

/*----------------------------------------------------------------*
|    Copyright 2001-2017 Networking and Simulation Laboratory     |
|         George Mason University, Fairfax, Virginia              |
|                                                                 |
| Permission to use, copy, modify, and distribute this            |
| software and its documentation for academic purposes is hereby  |
| granted without fee, provided that the above copyright notice   |
| and this permission appear in all copies and in supporting      |
| documentation, and that the name of George Mason University     |
| not be used in advertising or publicity pertaining to           |
| distribution of the software without specific, written prior    |
| permission. GMU makes no representations about the suitability  |
| of this software for any purposes.  It is provided "AS IS"      |
| without express or implied warranties.  All risk associated     |
| with use of this software is expressly assumed by the user.     |
 *-----------------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * BMLClientREST-Lib - RESTful Web Services Client library
 * @author dcorner
 */
public class BMLClientREST_Lib {

    private final String DEFAULT_PATH = "BMLServer/bml";
    
    private String host;
    private String port;
    private String path;
    private String submitter;
    private String domain;
    private String firstforwarder;

    /**
     * BMLClientREST_Lib Constructor
     */
    public BMLClientREST_Lib() {
        
        // Set defaults
        domain = "";
        port = "8080";
        host = "localhost";
        path = DEFAULT_PATH;
        submitter = "";
        firstforwarder = "";
    }   // BMLClientREST()

    /**
     *
     * @return the current setting of the domain property
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set the domain property.  Used to discriminate between BML dialects
     * @param domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * Get current setting of host property
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the host name or address
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get the current setting of the port property
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the port number (as a string)
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

        /**
     * Get the current setting of the path property
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the port number (as a string)
     * @param path
     */
    public void setPath(String path) {
        this.port = path;
    }
    
    /**
     * Get current setting of Requestor property
     * @return
     */
    public String getRequestor() {
        return submitter;
    }

    /**
     * Set the Requestor property indicating the identity of the client
     * @param requestor
     */
    public void setRequestor(String requestor) {
        this.submitter = requestor;
    }

    /**
     * Get the FirstForwarder property indicating first server to
     * forward the XML
     */
    public String getFirstForwarder() {
        return firstforwarder;
    }

    /**
     * Set the FirstForwarder property indicating first server to
     * forward the XML
     */
    public void setFirstForwarder(String firstforwarder) {
        this.firstforwarder = firstforwarder;
    }

    /**
     * Submit a bml transaction to the host specified
     * @param xmlTransaction - An XML string contaiing the bml
     * @return - The response returned by the host BML server
     */
    public String bmlRequest(String xmlTransaction) {
        URL url;
        HttpURLConnection conn;
        OutputStream os;
        BufferedReader br;
        String output;
        String result = "";

        if (submitter.equals(""))
            return "Error - Submitter not specified";
        if (domain.equals(""))
            return "Error - Domain not specified";

        try {

            String u =
                "http://" + host + ":" + port
                + "/" + path 
                + "?submitterID=" + submitter
                + "&domain=" + domain;
            
            if (!firstforwarder.equals(""))
                u += ";forwarders=" + firstforwarder;

            url = new URL(u);

            // Set up parameters to do a POST of the xml BML transaction
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "application/xml");
            conn.addRequestProperty("Accept", "application/xml");

            // Send the transaction and flush it out
            os = conn.getOutputStream();
            os.write(xmlTransaction.getBytes());
            os.flush();

            // Read the response, creating a single string
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                result += output + System.getProperty("line.separator");
            }

            // Disconnect our connection with host
            conn.disconnect();
        } // try

        catch (MalformedURLException e) {
            return "MalformedURL Exception - " + e;
        } catch (IOException e) {
            return "I/O Exception  - " + e;
        }   // catch

        return result;
    }   // bmlRequest()

}   // Class BMLClientREST
