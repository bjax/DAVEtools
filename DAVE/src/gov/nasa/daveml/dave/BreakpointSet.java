// BreakpointSet
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, originally of NASA LaRC, now at
//  Adaptive Aerospace Group, Inc. <bjackson@adaptiveaero.com>
//
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://github.com/bjax/DAVEtools
//
//  Original version of DAVEtools, prior to version 0.9.8: Copyright (c) 2007 United States
//  Government as represented by LAR-17460-1. No copyright is claimed in the United States under
//  Title 17, U.S. Code. All Other Rights Reserved.
//
//  Copyright (c) 2017 Adaptive Aerospace Group, Inc.

package gov.nasa.daveml.dave;


/**
 * 
 * <p> Object representing a Breakpoint Set definition </p>
 * <p> 040107 Bruce Jackson <mailto:bjackson@adaptiveaero.com> </p>
 *
 * @author Bruce Jackson
 *
 **/

import java.io.IOException;
import java.util.ArrayList;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * <p> The <code>BreakpointSet</code> stores a breakpoint set and it's associated bpID;
 * it also registers users (BlockBPs) that reference it </p>
 *
 **/

public class BreakpointSet // throws DAVEException
{

    /**
     *  our identifier
     */

    String bpid;

    /**
     *  stored as Doubles
     */

    ArrayList<Double> bpValues;

    /**
     *  description of set - can be null
     */

    String myDescription;

    /**
     *  name of set - can be null
     */

    String myName;

    /**
     * our parent model
     */

    Model myModel;

    /**
     *  debugging flag
     */

    boolean verbose;

    /**
     *  BlockBPs that reference our table
     */

    BlockArrayList users;


    /**
     *
     * Simple constructor 
     *
     * @param m <code>Model</code> to which we attach
     *
     **/

    public BreakpointSet( Model m )
    {
        this.bpid = "";
        this.bpValues = null;
        this.myDescription = "";
        this.myName = "";
        this.myModel = m;
        this.verbose = false;
        this.users = new BlockArrayList(3);
    }


    /**
     *
     * Constructor for BreakpointSet
     *
     * @param bpdef  Top-level Element breakpointDef
     * @param m Model to which we attach
     *
     * @throws DAVEException if error during construction
     *
     **/

    public BreakpointSet( Element bpdef, Model m) throws DAVEException
    { 
        this( m );      // call early constructor

        // Save our name, if any
        if (bpdef.getAttributeValue("name") != null) {
            this.myName = bpdef.getAttributeValue("name");
        } else {
            this.myName = null;
        }

        // Save our ID
        this.bpid = bpdef.getAttributeValue("bpID");
        if (this.bpid == null) {
            throw new DAVEException("Missing bpID in breakpointDef " + this.myName);
        }
        // Register ourself with the Model by ID since reusable
        m.register( this );
        
        // Fetch default namespace
        Namespace ns = null;
        Element parent = bpdef.getParentElement();
        if (parent != null) {
        	ns = parent.getNamespace();
        }
        // Parse description, if any
        Element descrip = bpdef.getChild("description",ns);
        if (descrip != null) {
            this.myDescription = descrip.getTextTrim();
        } else {
            this.myDescription = null;
        }
        // Set up user array
        this.users = new BlockArrayList(5);

        // Parse down to and load table values
        Element set   = bpdef.getChild("bpVals",ns);
        try {
            this.bpValues = ParseText.toList(set.getTextTrim());
        } catch (IOException e) {
            throw new DAVEException("Unable to parse breakpoint set values for bpID " + this.bpid);
        }

        // check for returned object
        if (this.bpValues == null) {
            throw new DAVEException("Unable to parse breakpoint set values for bpID " + this.bpid);
        }

        // check # of dimensions
        if (this.bpValues.size() < 1) {
            throw new DAVEException("Breakpoint set has zero length in bpID " + this.bpid);
        }
    }

    /**
     * Constructor from parts, not XML elements
     *
     * This constructor intended for non-reused, simple breakpoint
     * sets defined on-the-fly from function table definitions that
     * don't use griddedTableDef or griddedTableRef. We still need to
     * know what <code>Model</code> we're associated with so we can look up
     * breakpoint sets by ID.
     *
     * The BlockBP should also call the register() method so
     * this object can keep track of who is using this set
     * definition.
     *
     * @param setName String containing the name of the BreakpointSet
     * @param bpID Breakpoint identifier String
     * @param setValues a String containing comma separated floating-point values for breakpoints
     * @param description a String containing a description of this BreakpointSet
     * @param m Our parent Model
     * @throws DAVEException if unable to construct the BreakpointSet
     *
     **/

    public BreakpointSet( String setName, String bpID, String setValues, String description, Model m )
        throws DAVEException
    {
        this( m );
        this.myName = setName;
        this.myDescription = description;
        this.bpid = bpID;
        this.verbose = false;
        try { // load values from text
            this.bpValues = ParseText.toList( setValues );
        } catch (IOException e) {
            throw new DAVEException("Unable to load breakpoint set " + this.myName);
        }

        // register with Model
        m.register( this );
    }

    
    /**
     *
     * <p> Register a user of our breakpoint set definition
     *
     * <p> Should be called by the BlockBP that uses this table </p>
     *
     * @param userBlockBP <code>BlockBP</code> that wishes to register
     **/

    public void register( BlockBP userBlockBP )
    {
        this.users.add( userBlockBP );
    }


    /**
     *
     * Returns verbose status
     * @return verbose status as a boolean
     *
     **/

    public boolean isVerbose() { return this.verbose; }


    /**
     * Sets verbose status flag
     **/
    
    public void makeVerbose() { this.verbose = true; }


    /**
     * Clears the verbose flag
     **/

    public void silence() { this.verbose = false; }


    /**
     * 
     * Returns our name
     * @return String containing name of this BreakpointSet
     *
     **/

    public String getName() { return this.myName; }


    /**
     *
     * Returns our breakpoint set ID
     * @return the breakpoint set ID
     *
     **/

    public String getBPID() { return this.bpid; }


    /**
     *
     * Return our breakpoint set length
     * @return the number of breakpoints in this set
     *
     **/

    public int length() { 
    	if (bpValues == null) {
            return 0;
        }
    	return this.bpValues.size(); 
    }


    /**
     *
     * Return our breakpoint array
     * @return all the breakpoint values as an ArrayList of Double
     *
     **/

    public ArrayList<Double> values() { return this.bpValues; }

}
