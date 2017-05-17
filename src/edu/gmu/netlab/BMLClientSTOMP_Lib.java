/**
 * Java STOMP Client
 */
/*----------------------------------------------------------------*
|    Copyright 2001-2013 Networking and Simulation Laboratory     |
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
package edu.gmu.netlab;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Date;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;


// Library to provide STOMP Client support
/**
 * BMLClientSTOMP-Lib - STOMP Client library
 * @author dcorner
 *
 */
public class BMLClientSTOMP_Lib extends Thread {

    private static final String END_OF_FRAME = "\u0000";
    /**
     * @param args the command line arguments
     */
    private Socket socket = null;
    private String myHost;
    private Integer myPort;
    private String myDestination;
    private Vector<String> subscriptions;
    private java.util.concurrent.LinkedBlockingQueue<StompMessage> queue;
    private java.util.HashMap<String, String> headers;
    private StompMessage currentMsg;
    private String messageSelector;
    private InputStreamReader inputStream;
    private BufferedReader in;
    private byte b[] = new byte[1000];



    /**
     * Constructor - No parameter
     */
    public BMLClientSTOMP_Lib() {
        // Constructor

        subscriptions = new Vector<String>();
        headers = new java.util.HashMap<String, String>();
        queue = new java.util.concurrent.LinkedBlockingQueue<StompMessage>();

        // Default value for port
        myPort = 61613;

        // Default value for destination
        myDestination = "jms.topic.SBMLTopic";


    }   // BMLClientSTOMP()

    // Getters and setters

    // Three setters for port, String, Long and int
    /**
     * setPort number for STOMP connection
     * @param port - String
     */
    public void setPort(String port) {
        myPort = Integer.decode(port);
    }

    /**
     * setPort number for STOMP connection
     * @param port - Long object
     */
    public void setPort(Long port) {
        myPort = port.intValue();
    }

    /**
     * setPort number for STOMP connection
     * @param port - int
     */
    public void setPort(int port) {
        myPort = port;
    }
    /**
     *
     * @return Current port setting
     */
    int getPort() {
        return myPort;
    }

    /**
     * Set the host name or IP address
     * @param host
     */
    public void setHost(String host) {
        myHost = host;
    }

    /**
     * Set the destination queue or topic
     * @param destination - String
     */
    public void setDestination(String destination) {
        myDestination = destination;
    }
    
    /**
     * addSubscription - Add a Message Selector to list of selectors submitted with SUBSCRIBE
     *   Host will only publish messages matching one of the selectors.
     *   If no addSubscriptions are submitted then all messages will be received.
     * @param msgSelector
     */
    public void addSubscription(String msgSelector) {
        subscriptions.add(msgSelector);
    }


    // End of Getters/Setters
    // Connect to STOMP Host
    //  Wait for CONNECTED Message

    /**
     * Connect to Stomp host
     * @return - "OK" if connection make otherwise return an error message
     */
    public String connect() {

        try {
            // Create socket and make initial connection to host
            socket = new Socket(myHost, myPort);

            // Connect to STOMP Service
            String connectFrame = "CONNECT\n" +
                    "login:\n" +
                    "passcode:\n" +
                    "\n" +
                    END_OF_FRAME;
            sendFrame(socket, connectFrame);

            // Send subscription
            Date date = new Date();
            String subscribeFrame;

            // Create subscribe message
            subscribeFrame = "SUBSCRIBE\n" +
                "destination: " + myDestination + "\n";

            // Add message selectors
            if (!subscriptions.isEmpty()) {
                subscribeFrame +="selector:"+ subscriptions.elementAt(0) + "='true'";
                for (int j = 1; j < subscriptions.size(); ++j) {
                    subscribeFrame += " or " + subscriptions.elementAt(j) + "='true'";
                }
                subscribeFrame += "\n";
            }

            // Add message ID, blank line and null
            subscribeFrame += "id: " + date.toString() + "\n"
                    + "\n" + END_OF_FRAME;

            // Send the subscribe frame
            sendFrame(socket, subscribeFrame);

            // Start forground thread
            this.start();

            // Wait for response to connection request
            String resp = getNext_Block();

            // Are we connected?
            if (getMsgType().equals("CONNECTED")) {
                return "OK";
            } else {
                return currentMsg.messageBody;
            } 

        } //try
        catch (java.net.UnknownHostException e) {
            return "Unknown Host";
        } // Unknown host
        catch (Exception e) {
            return "Exception - " + e;
        }   // Exception

    }   // connect()

    /**
     * Get the next message without blocking the thread.
     * @return Next message or null string if no messages are ready
     * @throws Exception
     */
    public String getNext_NoBlock() throws Exception {

        if (queue.isEmpty()) {
            return "";
        }
        else
            return getNext_Block();
    }

    /**
     * Get next message - Block thread until a message is available
     * @return Next STOMP message
     * @throws Exception
     */
    public String getNext_Block() throws Exception {

        Vector<String> lines = new Vector<String>();
        String header;
        String headerVal;
        String stompMessage = "";

        int i, j;
        int headerValStart;

        // Share processor
        Thread.yield();
        
        // Wait for next STOMP Message
        currentMsg = queue.take();

        // Move headers into Map
        // Clear headers from last message
        headers.clear();
        for (i = 0; i < currentMsg.headers.size(); ++i) {

            String s = currentMsg.headers.elementAt(i);

            headerValStart = s.indexOf(":") + 1;
            header = s.substring(0, headerValStart - 1);
            headerVal = s.substring(headerValStart, s.length());
            headers.put(header, headerVal);
            
            // A value of true is used with the particular message Selector for this message
            if (headerVal.toLowerCase().equals("true"))
                messageSelector = header;

        }   // header loop

        return currentMsg.messageBody;

    }   // getNext()

    /**
     * Get the contents of a specific STOMP header from last message
     * Headers set by HornerQ are:
     * subscription:
     * content-length:
     * message-id:
     * destination:  (Name of queue)
     * expires:
     * redlivered:
     * priority:
     * timestamp:
     * type:
     * submitter:
     * id:
     * Message selectors in form of
     * IBML_Report:true

     * @param header e.g. "content-length"
     * @return - Value of header
     */
    public String getHeader(String header) {
        if (headers.containsKey(header)) {
            return headers.get(header);
        } else {
            return "";
        }
    }   // getHeader()

    /**
     * Get type of last message
     * Should be "MESSAGE"
     * @return "MESSAGE" or "CONNECTED"
     */
    public String getMsgType() {
        if (currentMsg != null)
            return currentMsg.messageType;
        else
            return "";
    }
    /**
     * Disconnect from STOMP server and close socket.
     * @return
     */
    public String disconnect() {
        String disconnectFrame = "DISCONNECT\n" +
                "\n" +
                END_OF_FRAME;
        try {
            sendFrame(socket, disconnectFrame);
            socket.close();
        } catch (Exception e) {
            return e.toString();
        }
        return "OK";

    }   // disconnect()

    // Send frame on current socket (private method)
    private static void sendFrame(Socket socket, String data) throws Exception {
        byte[] bytes = data.getBytes("UTF-8");
        OutputStream outputStream = socket.getOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            outputStream.write(bytes[i]);
        }
        outputStream.flush();
    }

    // Foreground message receive routine
    @Override
    public void run() {

        String line;
        String body;

        try {
            // Initialize Readers
            inputStream = new InputStreamReader(socket.getInputStream());
            in = new BufferedReader(inputStream);
            int c;
            int i;
            String cmd;
            String msgBody;

            // Loop forever
            while (true) {
                StompMessage msg = new StompMessage();

                // Make sure we are on a message boundry - ignore leading LF, CR, Space, Tab etc
                while ((c = inputStream.read()) <= 32)
                {}

                // Put i into byte array

                b[0] = (byte) c;
                i = 0;

                // Get next line into byte array including first character
                while ((c = inputStream.read()) > 32)
                {
                    b[++i] = (byte)c;
                }
                cmd = new String(b, 0, i+1);
                if (cmd.equals("MESSAGE")) {
                        msg.messageType = "MESSAGE";
                    } else if (cmd.equals("CONNECTED")) {
                        msg.messageType = "CONNECTED";
                    }
                    else
                       System.out.println("Not MESSAGE or CONNECTED");

                // Read the STOMP message headers into a Vector
                while (!(line = getNextLine().trim()).equals("")) {
                    msg.addHeader(line);
                }

                // End of headers - accumulate the message body into a single string
                //  Read until null string is received marking end of STOMP frame
  
                while ((c = inputStream.read()) != 0) {
                   b[0] = (byte)c;
                   msg.addToBody(new String(b,0,1));
                }

                // Add the message to the queue
                queue.add(msg);
            }   // Main Loop
        }   // try

        catch (Exception e) {
            System.out.println("Exception in STOMP Client Library " + e);
        }   // catch

    }   // run()

    // Get next line one byte at a time.
    String getNextLine() throws IOException {
        int c;
        int i;
        String cmd;
        i = 0;
        do
        {
            c = inputStream.read();
            b[i++] = (byte)c;
        } while (c >= 32);

        cmd = new String(b, 0, i);
        return cmd;

    }   // getNextLine()

}   // Class BMLClientSTOMP_Lib

// Encapsulating class used to convey STOMP messages between forground and background
class StompMessage {

    String messageType;
    Vector<String> headers;
    String messageBody;

    // Constructor - Initialize properties
    public StompMessage() {
        headers = new Vector<String>();
        messageBody = "";
    }

    // Add a line to the list of headers
    void addHeader(String s) {
        headers.add(s);
    }

    // Add line to message body
    void addToBody(String s) {
        messageBody += s;
    }
}   // class StompMessage
