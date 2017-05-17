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

/**
 *
 * @author wise
 */
// hack:placeholder class to force ServerType to be WISE-SBML RESTful
// ToDo: replace this with original class when found
public class SBMLServerType {

    enum serverTypes { SOAP, REST };

    serverTypes thisServerType;

    public SBMLServerType(String newServerType)
    {
        if(newServerType.trim().endsWith("SOAP"))
            thisServerType = serverTypes.SOAP;
        else
            thisServerType = serverTypes.REST;
        if(thisServerType == serverTypes.SOAP)
            BMLC2GUI.printDebug("Initialized serverType SOAP");
        if(thisServerType == serverTypes.REST)
            BMLC2GUI.printDebug("Initialized serverType REST");
    }

    boolean getIsREST(){return thisServerType == serverTypes.REST;}

    boolean getIsWISE() {return true;}

    String toString(SBMLServerType serverType)
    {
        if(thisServerType == serverTypes.SOAP)return "SOAP";
        else return "REST";
    }

    static SBMLServerType valueOf(String serverType)
    {
       return new SBMLServerType(serverType);
    }
}
