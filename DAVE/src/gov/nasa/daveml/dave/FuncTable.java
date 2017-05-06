// FuncTable
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, originally of NASA LaRC, now at
//  Digital Flight Dynamics <bruce@digiflightdyn.com>
//
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://github.com/bjax/DAVEtools
//
//  Original version of DAVEtools, prior to version 0.9.8: Copyright (c) 2007 United States
//  Government as represented by LAR-17460-1. No copyright is claimed in the United States under
//  Title 17, U.S. Code. All Other Rights Reserved.
//
//  Copyright (c) 2017 Digital Flight Dynamics

package gov.nasa.daveml.dave;


/**
 * 
 * <p> Object representing a Function Table definition </p>
 * <p> 040105 Bruce Jackson <mailto:bruce@digiflightdyn.com> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * <p> The <code>FuncTable</code> stores a definition of an
 * interpolated function table; it can be used by more than one
 * <code>BlockFuncTable</code>s
 *
 **/

public class FuncTable
{

    /**
     *  breakpoint IDs associated with each input
     */

    ArrayList<String> bpIDs;

    /**
     *  stored as Doubles
     */

    ArrayList<Double> functionValues;

    /**
     *  dimensions of table
     */

    int[] myDimensions;

    /**
     *  description of table
     */

    String description;

    /**
     *  name of gridded table
     */

    String tableName;

    /**
     *  ID of gridded table (if non-simple and not griddedTable element)
     */

    String gtID;

    /**
     * our parent model
     */

    Model myModel;

    /**
     *  debugging flag
     */

    boolean verbose;

    /**
     *  BlockFuncTables that reference our table
     */

    BlockArrayList users;

    /**
     * Default namespace of parent <code>&lt;root&gt;</code> element
     */
    
    Namespace ns;

    /**
     *
     * Common constructor
     * @param m the parent Model
     *
     **/

    public FuncTable( Model m )
    {
        // Set up bpID array
        this.bpIDs = new ArrayList<String>(5);

        this.functionValues = null;
        this.myDimensions = null;
        this.description = "No description available.";
        this.tableName = "No table name set.";
        this.gtID = "";
        this.ns = null;

        // Remember our parent
        this.myModel = m;

        // By default, be quiet
        this.verbose = false;

        // Set up user array
        this.users = new BlockArrayList(5);

    }


    /**
     *
     * Constructor for FuncTable with manual ID 
     *
     * @param gtid The ID of this table
     * @param gtd  either griddedTableDef or griddedTable
     * @param m <code>Model</code> to which we attach
     * @throws IOException if unable to construct table
     *
     **/

    public FuncTable( String gtid, Element gtd, Model m) throws IOException { 
        this( m );      // call common constructor

        // Save our name, if any
        
        if (gtd.getAttributeValue("name") != null)
            this.tableName = gtd.getAttributeValue("name");

        this.gtID = gtid;

        // Fetch default namespace
        Element parent = gtd.getParentElement();
        if (parent != null) 
            this.ns = gtd.getParentElement().getNamespace();

        // Parse description, if any
        Element descrip = gtd.getChild("description",this.ns);
        if (descrip != null)
            this.description = descrip.getTextTrim();

        // Functions contain either a functionDefn or dependentVarPts

        // Parse down to and load table values
        Element table       = gtd.getChild("dataTable",this.ns);
        this.functionValues = ParseText.toList(table.getTextTrim());

        // Parse and record breakpoint IDs; set # of dimensions
        this.parseBPIDsFromTableDef( gtd );

        // Register with model
        m.register( this );
    }


    /**
     *
     * <p> Constructor for FuncTable </p>
     *
     * @param gtd  Top-level <code>Element</code> griddedTableDef
     * @param m <code>Model</code> to which we attach
     * @throws IOException if unable to build table
     *
     **/

    public FuncTable( Element gtd, Model m) throws IOException
    { 
        // call common constructor
        this( gtd.getAttributeValue("gtID"), gtd, m);
    }

    /**
     * Constructor from basics, not from XML Elements
     *
     * This constructor intended for non-reused, simple tables
     * local to a single function. We need to know what {@link Model} we're
     * associated with so we can look up breakpoint sets by ID.
     *
     * After calling this constructor, the BlockFuncTable needs to
     * also add any bpIDs associated with this table by calling
     * {@link #addBPID} and then call {@link #setDimensions}.
     *
     * The resulting <code>BlockFuncTable</code> should also call the
     * {@link #register} method so this table can keep track of who is
     * using this table definition.
     *
     * @param tableID     a String containing the table's identifier
     * @param tableName   a String containing the more readable name of the table
     * @param tableValues a String containing comma-separated floating point values unraveled to a vector
     * @param description a String with a description of the function represented by the table
     * @param ndim        an integer with the number of dimensions in the reconstructed table
     * @param m           our parent {@link Model}
     *
     **/

    public FuncTable( String tableID, String tableName, String tableValues, String description, int ndim, Model m ) {
    	
        this( m );      // call common constructor

        this.gtID = tableID;
        this.tableName = tableName;
        this.description = description;
        this.myDimensions = new int [ ndim ];
        this.verbose = false;
        try { // load values from text
            this.functionValues = ParseText.toList( tableValues );
        } catch (IOException e) {
            System.err.println("Unable to load table " + this.tableName + "... aborting.");
            System.exit(0);
        }

        // Register with model
        m.register( this );
    }

    
    /**
     *
     * <p> Register a user of our function table definition
     *
     * <p> Should be called by BlockFuncTable that uses this table, in
     * case it has to change or something.</p>
     *
     * @param userBFT <code>BlockFuncTable</code> that wishes to register
     **/

    public void register( BlockFuncTable userBFT )
    {
        this.users.add( userBFT );
    }


    /**
     * Returns verbose status
     * @return the status of the verbose flag
     **/

    public boolean isVerbose() { return this.verbose; }


    /**
     * Sets verbose status flag
     **/
    
    public void makeVerbose() { this.verbose = true; }


    /**
     * <p> Clears the verbose flag </p>
     **/

    public void silence() { this.verbose = false; }


    /**
     *
     * <p> Finds and saves a list of breakpoint references found in a
     *     <code>griddedTable</code> or <code>griddedTableDef</code> element.
     *     
     *     This method is called by the constructors that are given an Element
     *     tree with a griddedTable and bpRefs.
     *
     * @param gtd JDOM "griddedTableDef" element
     *
     **/

    @SuppressWarnings("unchecked")
    protected void parseBPIDsFromTableDef( Element gtd )
    {
    	Element breakpointRefs = gtd.getChild("breakpointRefs", this.ns);
    	if (breakpointRefs == null) { // try again without namespace
            breakpointRefs = gtd.getChild("breakpointRefs");
        }
    	
    	if (breakpointRefs != null) {
            List<Element> bpRefs = breakpointRefs.getChildren("bpRef",this.ns);
            if (bpRefs == null) {
                // try again without namespace
                bpRefs = breakpointRefs.getChildren("bpRef");
            }
            // even if still null, no need to throw exception - we 
            // silently use null for breakpointIDs, setDimensions.

            Iterator<Element> bpRefIterator   = bpRefs.iterator();

            while (bpRefIterator.hasNext()) {
                // get ID of breakpoint identifier
                Element bpRefElement = bpRefIterator.next();
                String bpName = bpRefElement.getAttributeValue("bpID");
                this.bpIDs.add( bpName );
            }
    	}
        // set our dimensions by resolving breakpoint IDs
        
        this.setDimensions();
    }


    /**
     * 
     * <p> Add breakpoint ID to list </p>
     * 
     * TODO - what is this used for?
     *
     * @param portNum port to associate with breakpoint ID (0-based)
     * @param bpID <code>String</code> containing breakpoint ID
     *
     **/

    public void addBPID( int portNum, String bpID )
    {
        // increase length of array if necessary
        while(bpIDs.size() < portNum) {
            bpIDs.add("");
        }
        bpIDs.set(portNum-1, bpID);
    }


    /**
     * 
     * Returns an iterator for the bpID ArrayList
     * @return an Iterator of String for elements within the bpID ArrayList
     *
     **/

    public Iterator<String> getBPIterator() { return bpIDs.iterator(); }


    /**
     *
     * Returns breakpoint ID (<code>bpID</code>) associated with
     *  particular port number (1-based).
     *
     * @param portNum integer offset (1-based) port number
     * @return a String containing the bpID associated with the given port number
     *
     **/

    public String getBPID( int portNum )
    {
        int portIndex = portNum-1;
        return bpIDs.get( portIndex );
    }


    /**
     *
     * Returns our name
     * @return String with the name of the table
     *
     **/

    public String getName() { return this.tableName; }


    /**
     *
     * Returns our table ID 
     * @return String with our gridded table ID (gtID)
     *
     **/

    public String getGTID() { return this.gtID; }


    /**
     *
     * <p> Sets our dimensionality from discussions with associated breakpoint sets </p>
     * 
     * Assumes that breakpoint sets have been previously defined
     *
     **/

    protected void setDimensions()
    {
        // Verify the number of dimensions
        this.myDimensions = new int[ bpIDs.size() ];

        // Find length of each breakpoint set
        int i = 0;
        Iterator<String> bpit = this.bpIDs.iterator();
        while (bpit.hasNext()) {
            String bpID = bpit.next();
            BreakpointSet bps = this.myModel.getBPSetByID( bpID );
            if (bps == null) {
                System.err.println("Unable to find breakpoint block with ID of " + bpID);
                System.exit(0);
            }
            this.myDimensions[ i ] = bps.length();
            i++;
        }
    }

    /**
     *
     * Return our table size
     * @return the number of values in the embedded table
     *
     **/

    public int size() { 
        if (this.functionValues == null) {
            return 0;
        }
        return this.functionValues.size(); 
    }


    /**
     *
     * Return our number of dimensions
     * @return the number of dimensions in the table
     *
     **/

    public int numDim() { 
        if (myDimensions == null) {
            return 0;
        }
        return this.myDimensions.length; 
    }


    /**
     *
     * Return a specific dimension's length
     *
     * @param theAxis <code>int</code> (0-based) dimension to get length of
     * @return the length of the table in a given dimension
     *
     **/

    public int dim(int theAxis) { return this.myDimensions[ theAxis ]; }


    /**
     *
     * Return the dimensions vector
     * @return an array of integers giving the length in each dimension of our table
     *
     **/

    public int[] getDimensions() { return this.myDimensions; }


    /**
     *
     * Return the function values as an ArrayList
     * @return the function values as a single-dimensional ArrayList of Doubles
     * TODO: figure out the sequence of dimensions
     *
     **/

    public ArrayList<Double> getValues() { return this.functionValues; }


    /** 
     *
     * Recursively prints table values dimension by dimension
     * 
     * The order of values in the provided table array must be
     *
     *  [1, ... 1, 1], 
     *  [1, ... 1, 2], 
     *  [1, ... 1, 3],
     *   .
     *   .
     *   .
     *  [n, ... q, r]
     *
     * @param  writer      Writer    to receive the function table values
     * @param  table       ArrayList of type Double containing the table values
     * @param  dims        array     of integers whose product should match the number of values in the table parameter.
     * @param  startIndex  integer   0-based offset into the next elements to print (when called recursively for multiple dimensions)
     * @throws IOException if unable to write the values
     * @return             integer   representing the next starting index for a recursive application
     *
     **/

    protected int printTable( Writer writer, ArrayList<Double> table, int[] dims, int startIndex)
        throws IOException
    {
        int offset;
        int i;
        String newline = System.getProperty("line.separator");
        DecimalFormat form = new DecimalFormat(myModel.dataFormat);


        // System.out.println("printTable called recursively, index = " + startIndex + 
        //                 "; dims.length = " + dims.length + "; dims[0] = " + dims[0]);

        switch (dims.length)
            {
            case 0:     // shouldn't happen
                return 0;
            case 1:
                for ( i = 0; i < dims[0]; i++) {
                        Double theValue = table.get(i+startIndex);
                        String valueStr = form.format( theValue );
                        writer.write( valueStr );
                        if( i < dims[0]-1) {
                            writer.write(", ");
                        }
                }
                return i;
            case 2:
                for ( i = 0; i < dims[0]; i++) {
                    int[] newDims = new int[1];
                    newDims[0] = dims[1];
                    offset = printTable( writer, table, newDims, startIndex );
                    if( i < dims[0]-1) {
                        writer.write(", ");
                    }
                    writer.write(newline);
                    startIndex = startIndex + offset;
                }
                return startIndex;
            default:
                for ( i = 0; i < dims[0]; i++ ) {
                    int[] newDims = new int[1];
                    newDims[0] = dims[1];
                    //System.out.println(" For dimension " + dims.length + " layer " + i);
                    //System.out.println();
                    offset = printTable( writer, table, newDims, startIndex );
                    if( i < dims[0]-1) {
                        writer.write(", ");
                    }
                    writer.write(newline);
                    startIndex = startIndex + offset;
                }
                return startIndex;
            }
    }

    /** 
     *
     * This method directs output to designated Writer
     *
     * @param writer <code>Writer</code> to receive values
     * @throws IOException if unable to generate output
     *
     **/

    public void printTable( Writer writer )
        throws IOException
    {
        printTable(writer, this.functionValues, this.myDimensions, 0);
    }


    /**
     *
     * Returns data point at specified coordinates
     * @param indices an array of integers (0-based) identifying which value to retrieve
     * @return the data value at specified coordinates
     **/

    public double getPt( int[] indices )
    {
    	// FIXME - need to check all indices to see if they are within bounds 
    	// currently only check final result
        int mult = 1;
        int offset = 0;
        int i;

        if (verbose) {
            System.out.print("  getting point '" + this.tableName + "[");
            for (i = 0; i < indices.length ; i++) {
                System.out.print(" " + indices[i]);
                if (i < indices.length-1) {
                    System.out.print(",");
                }
            }
            System.out.println(" ]'");
        }

        for (i = indices.length-1 ; i >= 0; i--) {      // 3, 2, 1, 0
            if (verbose) {
                System.out.println("  getPt: i " + i);
            }
            offset += indices[i]*mult;
            mult   *= this.myDimensions[i];
            if (verbose) {
                System.out.println("   getPt: offset " + offset + " mult " + mult);
            }
        }
        Double value = functionValues.get(offset);
        double val = value.doubleValue();
        if (verbose) {
            System.out.println("  returned value " + val);
        }
        return val;
    }


	public String getDescription() {
            return this.description;
	}
}

