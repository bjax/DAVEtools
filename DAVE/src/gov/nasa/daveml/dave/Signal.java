// Signal.java
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
 * <p>  Object representing each "variable" in algorithm </p>
 * <p> 031211 Bruce Jackson <mailto:bjackson@adaptiveaero.com> </p>
 *
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Object representing each "variable" in a model implementation algorithm.
 * <p> 
 * There should be one of these for each signal line going between
 * {@link Block}s in a {@link Model}. Each signal has a parent
 * <code>Block</code> &amp; port and one or more child
 * <code>Block</code>s &amp; ports - in essence, a network.
 *<p> 
 * Modification history: 
 * <ul>
 *  <li>031211: Written EBJ</li>
 * </ul>
 *
 * @author Bruce Jackson <a href="mailto:bjackson@adaptiveaero.com">bjackson@adaptiveaero.com</a>
 * @version 0.9
 *
 **/

public class Signal
{
    /**
     *  our model parent
     */

    Model ourModel;

    /**
     *  name of signal
     */

    String myName;

    /**
     *  variable name
     */

    String myVarID;

    /**
     *  units of signal
     */

    String myUnits;
    
    /**
     *  Description of signal
     */
    
    String description;

    /**
     *  source block for signal
     */

    Block source;

    /**
     *  port number on source block (1-based)
     */

    int sourcePort;

    /**
     *  list of downstream blocks
     */

    BlockArrayList dests;

    /**
     *  list of port numbers (1-based) on output blocks
     */

    ArrayList<Integer> destPorts;

    /**
     *  flag to indicate an initial value has been specified
     */

    boolean hasIC;

    /**
     *  initial value of signal (e.g. for constants)
     */

    private String IC;

    /**
     *  flag to indicate this is an input signal
     */

    private boolean isInput;

    /**
     *  flag to indicate this is a simulation control variable
     */

    private boolean isControl;

    /**
     *  flag to indicate this is a disturbance signal
     */

    private boolean isDisturbance;

    /**
     *  flag to indicate this is a state variable
     */

    private boolean isState;

    /**
     *  flag to indicate this is a state derivative
     */

    private boolean isStateDeriv;

    /**
     *  flag to indicate this should be an output signal
     */

    private boolean isOutput;

    /**
     *  flag to indicate this variable is one of the standard AIAA predefined
     * variables
     */

    private boolean isStdAIAA;

    /**
     *  indicates if we're to be chatty
     */

    boolean verboseFlag = false;

    /**
     *  indicates this signal does not correspond to a declared varDef
     */

    private boolean derived = false;

    /**
     * indicates this signals equivalent code has been emitted by a call to 
     * {@link genCode}
     * 
     */
    
    private boolean defined = false;
    
    /**
     * general purpose marking capability; all signals initially unmarked
     * @since 0.9.4
     */
    
    private boolean marked = false;
    
    /**
     * lower limit. Default is -Infinity (no lower limit)
     */

    private double lowerLim = Double.NEGATIVE_INFINITY;

    /**
     * upper limit. Default is +Infinity (no upper limit)
     */

    private double upperLim = Double.POSITIVE_INFINITY;

    /**
     *
     * Elementary constructor
     *
     **/

    public Signal()
    {
        ourModel = null;
        myName =  "unnamed";
        myVarID = "unnamed";
        myUnits = "";
        source = null;
        sourcePort = 0;
        description = "No description.";
        dests = null;
        destPorts = null;
        hasIC = false;
        IC = "";
        isInput = false;
        isControl = false;
        isDisturbance = false;
        isState = false;
        isStateDeriv = false;
        isOutput = false;
        isStdAIAA = false;
        derived = false;
        defined = false;
        lowerLim = Double.NEGATIVE_INFINITY;
        upperLim = Double.POSITIVE_INFINITY;
        marked = false;
    }


    /**
     *
     * <p> Simple constructor </p>
     *
     * @param signalName A <code>String</code> containing the name of
     * the signal to construct
     * @param m <code>Model</code> we're part of
     *
     **/

    public Signal(String signalName, Model m)
    {
        this( signalName, Signal.toValidId(signalName), "unkn", 1, m);
    }


    /**
     * 
     * <p> Explicit constructor to create <code>Signal</code> from scratch </p>
     *
     * @param signalName Name of signal - does not have to be unique
     * @param varID ID of signal - must be unique
     * @param units Unit of measure of signal (e.g. deg/sec)
     * @param numConnects How many connections it goes to (guess)
     * @param m <code>Model</code> we're part of
     *
     **/

    public Signal(String signalName, String varID, String units, int numConnects, Model m)
    {
        this();
        ourModel = m;
        myName = signalName;
        if (signalName == null) {
            myName = "unnamed";
        }
        myVarID = varID;
        if (varID == null) {
            myVarID = "derived_signal_" + m.getNumSignals();
        }
        myUnits = units;
        dests = new BlockArrayList(numConnects);
        destPorts = new ArrayList<Integer>(numConnects);
        //System.out.println("  Signal constructor: " + myName + "(" + units + ")");
        m.add(this);
    }

    /**
     *
     * <p> Builds <code>Signal</code> from org.jdom.Element </p>
     * 
     * <p>Supplied element can be either a 'variableDef' or an 'apply' element.
     * Based on the type of <code>Element</code> provided, different actions occur. 
     * 
     * A variableDef element is parsed for information about the signal; if a 
     * sequence of child &lt;math&gt;&lt;calculation&gt;&lt;apply&gt; elements are found, the first
     * &lt;apply&gt; element is handled (an upstream block of proper type is created, and 
     * our signal is hooked onto it).
     * 
     * An apply element does the same (creates an upstream block and connects us to
     * its output port) but no variableDef information (like varID or units) is parsed.
     *
     * @param signalElement jdom.org.Element variableDef' or 'apply' element
     *
     * @param m <code>Model</code> to which new blocks and signals
     * belong. Any new blocks or signals are
     * <b>add</b>ed to <code>Model</code> as they are created,
     * including <b>this</b> object.
     * 
     * @throws DAVEException if syntax error is found in top-level apply element
     *
     **/

    public Signal(Element signalElement, Model m) throws DAVEException
    {
        this(signalElement.getAttributeValue("name" ), m);
        String name = signalElement.getName();
        if(name.equals("apply")) {
            Block b = this.handleApply(signalElement, m);
            if (b == null) {
                throw new DAVEException("illegal syntax in <apply> element");
            }
        } else if (name.equals("variableDef") ) { // not an apply element - should check to make sure it is a varDef?
            myVarID = signalElement.getAttributeValue("varID");
            myUnits = signalElement.getAttributeValue("units");

            // Record the initial condition, if any
            Attribute theIC = signalElement.getAttribute("initialValue");
            if( theIC != null ) {
                this.hasIC = true;
                this.IC = theIC.getValue();
            }

            // Look for minValue and/or maxValue attributes
            // These are dealt with in call to Model.hookUpIO()
            Attribute minVal = signalElement.getAttribute("minValue");
            if (minVal != null ) {
                this.setLowerLimit( minVal.getValue() );
            }

            Attribute maxVal = signalElement.getAttribute("maxValue");
            if (maxVal != null ) {
                this.setUpperLimit( maxVal.getValue() );
            }

            // Search for various flags (empty elements)
            Element isInputElement = signalElement.getChild("isInput",
                    signalElement.getNamespace());
            Element isControlElement = signalElement.getChild("isControl",
                    signalElement.getNamespace());
            Element isDisturbanceElement = signalElement.getChild("isDisturbance",
                    signalElement.getNamespace());
            Element isStateElement = signalElement.getChild("isState",
                    signalElement.getNamespace());
            Element isStateDerivElement = signalElement.getChild("isStateDeriv",
                    signalElement.getNamespace());
            Element isOutputElement = signalElement.getChild("isOutput",
                    signalElement.getNamespace());
            Element isStdAIAAElement = signalElement.getChild("isStdAIAA",
                    signalElement.getNamespace());
            
            // record findings

            // first three flags (input, control, disturbance) are mutually exclusive
            if(isInputElement != null) {
                this.isInput = true;
            } else {
                if(isControlElement != null) {
                    this.isControl = true;
                } else {
                    if(isDisturbanceElement != null) {
                        this.isDisturbance = true;
                    }
                }
            }
            if(isStateElement      != null) { this.isState      = true; }
            if(isStateDerivElement != null) { this.isStateDeriv = true; }
            if(isOutputElement     != null) { this.isOutput     = true; }
            if(isStdAIAAElement    != null) { this.isStdAIAA    = true; }

            // Look for description
            this.description = signalElement.getChildTextNormalize("description",
                    signalElement.getNamespace());
            if (this.description == null) {
                this.description = "No description.";
            }
            
            // Search for calculation/math element
            this.handleCalculation( signalElement, m );
        } else {
            System.err.println("Error: Signal constructor called with " + name + " element.");
        }
    }
    
    /**
     *
     * Converts String to valid XML ID by replacing spaces with underscores
     * @param input String with possible whitespace (invalid XML id}
     * @return String with whitespace replaced with underscores
     */
    
    protected static String toValidId( String input ) {
        String output = "";
        if (input != null) {
            output = input.replace(" ", "_");
        }
        return output;
    }
    
    /**
     * Look for and deal with any <code>calculation</code> child element
     * 
     * @param theVarDefElement  variableDef parent
     * @param m  Model to which calculations should be added
     */

    private void handleCalculation( Element theVarDefElement, Model m ) {
        Element calc = theVarDefElement.getChild("calculation", theVarDefElement.getNamespace());
        if (calc != null) {
        	if (verboseFlag) {
                    System.out.println("Calculation element found in signal " + myName + "...");
                }
            Element math = this.findMathMLChild(calc, "math");
            if (math != null) {
            	if (verboseFlag) {
                    System.out.println("...it appears to be valid math!");
                }
                Element apply = this.findMathMLChild( math, "apply");
                if (apply != null) {
                    if (verboseFlag) {
                        System.out.println("...with an apply element");
                    }
                    this.handleApply( apply, m );
                }                    
            } else { // here if math == null
                System.err.println("Invalid calculation element in signal " + myName + 
                                   "... no <math> element found.");
            } // end of if-then-else for <math>
        } // end of if (calc != null) - no else clause, this varDef has no calculation
    }
    
    
    /**
     * Look for a MathML element (with or without MathML namespace)
     * 
     * @param e Parent element of potential MathML subelement
     * @param elementType Type of MathML element to search for
     * @return child MathML element of desired type, or null if not found
     */
    
    private Element findMathMLChild( Element e, String elementType ) {
    	Namespace mathml = Namespace.getNamespace("", "http://www.w3.org/1998/Math/MathML");
    	Element child;
    	child = e.getChild(elementType, mathml);
        if (child == null) {
            child = e.getChild(elementType);  // try without namespace
        }
        return child;
    }

    /**
     *
     * Does deep copy of supplied <code>Signal</code>
     *
     * @param s the original to copy
     *
     **/

    public Signal( Signal s ) {
        this();                 // invoke basic constructor
        this.ourModel   = s.ourModel;   // our model parent
        this.myName     = s.myName;     // name of signal
        this.myVarID    = s.myVarID;    // variable name
        this.myUnits    = s.myUnits;    // units of signal
        this.source     = s.source;     // source block for signal
        this.sourcePort = s.sourcePort; // port number on source block (1-based)
        this.hasIC      = s.hasIC;      // flag to indicate an initial value has been specified
        this.IC         = s.IC;         // initial value of signal (e.g. for constants)
        this.lowerLim   = s.lowerLim;   // lower limit to output range
        this.upperLim   = s.upperLim;   // upper limit to output range
        this.isOutput   = s.isOutput;   // flag to indicate this should be an output signal
        // don't copy verboseFlag
        this.derived  = s.derived;  // indicates this does not correspond to varDef
        // don't copy ready flag
        // copies list of pointers to downstream blocks
        this.dests      = new BlockArrayList( s.dests.size() );
        Iterator<Block> dest_iterator = s.dests.iterator();
        while (dest_iterator.hasNext()) {
            this.dests.add(dest_iterator.next() );
        }
        // copies list of port indexs
        this.destPorts  = new ArrayList<Integer>( s.destPorts.size());
        Iterator<Integer> port_iterator = s.destPorts.iterator();
        while (port_iterator.hasNext()) {
            this.destPorts.add(port_iterator.next() );
        }
    }

    /**
     *
     * Indicate if upstream block has new value.
     * @return <code>true</code> if the upstream source block is up-to-date, else <code>false</code>a
     * @throws DAVEException if missing source block
     *
     **/

    public boolean sourceReady() throws DAVEException
    {
        if (source == null) {
            throw new DAVEException("Error: Signal '" + this.getName() + "' has no source block - wiring error?");
        }
        return source.isReady();
    }


    /**
     *
     * Returns output value of source block.
     * @return double output value of block
     * @throws DAVEException if missing source block
     *
     **/

    public double sourceValue() throws DAVEException
    {
        if (source == null) {
            throw new DAVEException("Error: Signal '" + this.getName() + "' has no source block - wiring error?");
        }
        return source.getValue();
    }


    /**
     *
     * Sets verbose flag
     *
     **/

    public void makeVerbose() { this.verboseFlag = true; }


    /**
     *
     * Unsets verbose flag
     *
     **/

    public void silence() { this.verboseFlag = false; }


    /**
     *
     * Returns verbose flag
     * @return <code>true</code> if we're to be chatty; otherwise <code>false</code>
     **/

    public boolean isVerbose() { return this.verboseFlag; }



    /**
     *
     * Recursive function that builds math element networks.
     *
     * <p> This method returns the last block of a perhaps
     *     extensive network of blocks constructed in accordance with
     *     a calculation element. It builds and connects blocks and
     *     signals as required to complete the specified calculation. 
     *     Implicit signals (variables not named in &lt;ci&gt;
     *     elements) are created and attached to necessary
     *     blocks. Explicit inputs (those called out in &lt;ci&gt;
     *     blocks) are simply named as block inputs to be hooked up
     *     later. 
     *
     * @param applyElement JDOM <code>Element</code> containing the
     *     apply description.
     * @return Block that represents the output of the calculation.
     *
     **/

    private Block handleApply( Element applyElement, Model m )
    {
        Block b = null;
        try {
            b = BlockMath.factory(applyElement, m);
        } catch (DAVEException e) {
            System.err.println("Unable to process apply element found in calculations for '" +
                    this.getName() + ".'");
            System.err.println("Error: " + e.getMessage() );
            System.exit(0);
        }
        if(b == null) {
            String errmsg = "Encountered illegal element following <apply> in math calculation.";
            System.err.println(errmsg);
            if (this.verboseFlag) {
                System.out.println(errmsg);
            }
        }
        else {
            try {
                b.addOutput(this);
            } catch (DAVEException e) {
                System.err.println("Unable to add signal '" + this.getName() 
                                   + "' as input to block '" + b.getName() + "'.");
                System.err.println("Error: " + e.getMessage() );
                System.exit(0);
            }
        }
        return b;
    }


    /**
     * <p> Set the name of the signal </p>
     *
     * @param theName <code>String</code> to use for name
     *
     **/

    public void setName( String theName ) { this.myName = theName; }

    
    /**
     *
     * <p> Set the units of the signal </p>
     *
     * @param theUnits <code>String</code> to use for name
     *
     **/

    public void setUnits( String theUnits ) { this.myUnits = theUnits; }



    /**
     *
     * <p> Set the unique varID of the signal </p>
     * 
     * <p> Also changes the varID of connected blocks </p>
     *
     * @param theVarID <code>String</code> to use for varID
     *
     **/

    public void setVarID( String theVarID ) { 
        // set our variable ID
        this.myVarID = theVarID;
        
        // set our source blocks output var ID
        if (this.source != null) {
            this.source.renameOutVarID();
        }
        
        // set our destination blocks input varIDs
        BlockArrayList sinks = this.getDests();
        ArrayList<Integer> sinkPorts = this.getDestPortNumbers();
        if ((sinks != null) && (sinkPorts != null)) {
            for (int i = 0; i < sinks.size(); i++) {
                Block blk = sinks.get(i);
                blk.renameInVarID( sinkPorts.get(i));
                // Special treatment for breakpoint blocks - need to 
                // change associated varID in all function blocks too
                if (blk instanceof BlockBP) {
                    Signal sig = blk.getOutput();
                    sig.setVarID(theVarID);
                }
            }
        }
    }


    /**
     *
     * Sets the indicator flag to show this was automatically generated
     *
     * <p>
     *  This flag should be set for all 'derived' signals; i.e. those
     *  not corresponding to varDefs in the DAVE-ML source file
     *
     **/

    public void setDerivedFlag() { this.derived = true; }

    
    /**
     * Clears the derived flag
     */
    
    public void clearDerivedFlag() { this.derived = false; }


    /**
     *
     * Returns the indicator flag to show this was automatically generated
     *
     *  This flag should be set for all 'derived' signals; i.e. those
     *  not corresponding to varDefs in the DAVE-ML source file
     *
     * @return <code>true</code> if this Signal was automatically
     * generated, else <code>false</code>
     **/

    public boolean isDerived() { return this.derived; }

    /**
     * Sets the indicator flag to show this has been defined in code
     */
    
    protected void setDefinedFlag() { this.defined = true; }
    
    
    /**
     * Clears the indicator flag (unlikely instance)
     */
    
    protected void clearDefinedFlag() { this.defined = false; }
    
    
    /**
     * Returns true if this signal has been encoded already
     * @return <code>true</code> if this Signal has already been
     * encoded; otherwise <code>false</code>
     */
    
    public boolean isDefined() { return this.defined; }
    
    
    /**
     * Sets general purpose marker flag
     * @since 0.9.4
     */
    
    public void mark() { this.marked = true; }
    
    /**
     * Clears the general purpose marking flag
     * @since 0.9.4
     */
    
    public void unmark() { this.marked = false; }
    
    /**
     * Returns true if the signal is marked for some reason
     * @return <code>true</code> if signal is marked, otherwise <code>false</code>
     * @since 0.9.4
     */
    
    public boolean isMarked() { return this.marked; }
    
    /**
     * indicates a lower limit is in force
     */

    boolean hasLowerLimit() {
      return lowerLim != Double.NEGATIVE_INFINITY;
    }

    /**
     * indicates a lower limit is in force
     */

    boolean hasUpperLimit() {
      return upperLim != Double.POSITIVE_INFINITY;
    }

    /**
     * indicates a limit of some type is in force
     */

    boolean isLimited() {
        return hasLowerLimit() || hasUpperLimit();
    }

    /**
     * sets value of lower limit (string argument)
     * @param strValue String containing the value
     */

    protected final void setLowerLimit( String strValue ) {
        lowerLim = Double.parseDouble( strValue );
    }

    /**
     * sets value of lower limit (numeric argument)
     * @param value the value of the lower limit
     */

    void setLowerLimit( double value ) {
        lowerLim = value;
    }

    /**
     * gets value of lower limit
     * @return double
     */

    double getLowerLimit() {
        return lowerLim;
    }

    /**
     * sets value of upper limit (string argument)
     * @param strValue String containing the value
     */

    protected final void setUpperLimit( String strValue ) {
        upperLim = Double.parseDouble( strValue );
    }

    /**
     * sets value of upper limit (numeric argument)
     * @param value the value of the upper limit
     */

    void setUpperLimit( double value ) {
        upperLim = value;
    }

    /**
     * gets value of lower limit
     * @return double
     */

    double getUpperLimit() {
        return upperLim;
    }

    /**
     * Clears the isOutput flag
     */

    void clearIsOutputFlag() { this.isOutput = false; }

    /**
     *
     * <p> Connect to upstream source block </p>
     *
     * <p> By convention, upstream block is responsible for recording
     *     this connection for itself. </p>
     *
     * @param sourceBlock the source block
     * @param portNum the source block's port number (1-based)
     *
     **/

    public void addSource(Block sourceBlock, int portNum)
    {
        //System.out.println("  Signal " + myName + " is adding source block " + sourceBlock.getName());
        source = sourceBlock;
        sourcePort = portNum;
    }

    /**
     *
     * <p> Connect to downstream block </p>
     *
     * <p> By convention, the upstream element (this signal) alerts
     *     downstream block of the new connection. </p>
     *
     * @param sinkBlock the downstream block to add
     * @param portNum the port number on the downstream block (1-based)
     *
     **/

    public void addSink(Block sinkBlock, int portNum)
    {
        //System.out.println("  Signal " + myName + " is adding sink block " + sinkBlock.getName());
        dests.add( sinkBlock );
        destPorts.add( new Integer(portNum) );
        sinkBlock.addInput( this, portNum );
    }
    
    /**
     * Returns a BlockArrayList containing pointers to all the destination
     * blocks for this signal.
     * @return BlockArrayList the list of destination blocks for this signal
     * @since 0.9.6
     */
    
    public BlockArrayList getDestBlocks() { return this.dests; }

    /**
     *
     * <p> Returns output block array list </p>.
     * <em> Alias for getDestBlocks() </em>
     * @return {@link BlockArrayList} of destination blocks
     * @deprecated since 0.9.6
     *
     **/

    public BlockArrayList getDests() { return this.getDestBlocks(); }

    /**
     *
     * Returns <code>ArrayList</code> with the port numbers of the associated
     * output blocks
     * @return ArrayList with the port numbers of the associated output blocks
     *
     **/

    public ArrayList<Integer> getDestPortNumbers()
    {
        return this.destPorts;
    }


    /**
     * 
     * <p> Sets the (1-indexed) port number for (0-indexed) numbered
     *     destination port </p>
     *
     * @param portIndex indicates which destination port we're renumbering
     * @param newPortNumber the new (1-based) port number to assign
     *
     **/

    public void setPortNumber( int portIndex, int newPortNumber )
    {
        destPorts.set(portIndex, new Integer(newPortNumber));
    }


    /**
     *
     * Returns signal name as <code>String</code>
     * @return String with the nane of this Signal
     *
     **/

    public String getName() { return myName; }

    /**
     *
     * Returns variable ID as <code>String</code>
     * @return String with the varID of this Signal
     *
     **/

    public String getVarID() { return myVarID; }

    /**
     *
     * Returns units as <code>String</code>
     * @return String with the units-of-measure encoded per ANSI/AIAA-S-119-2011
     *
     **/

    public String getUnits() { return myUnits; }

    /**
     * Get the source block
     * @return Block the <code>Signal</code>'s source block
     * @since 0.9.6
     */
    
    public Block getSourceBlock() { return this.source; }
    
    /**
     *
     * Returns source block
     * <em> Alias for getSourceBlock </em>
     * @return {@link Block} that is our upstream Block
     * @deprecated
     *
     **/
    
    public Block getSource() { return this.getSourceBlock(); }


    /**
     *
     * Returns source (1-based) port number
     * @return int with the 1-based output port number of our upstream
     * Block
     *
     **/
    
    public int getSourcePort() { return sourcePort; }


    /**
     *
     * Returns true if we have are connected to an upstream block
     * @return <code>true</code> if we are connected to a source
     * Block, else <code>false</code>
     *
     **/

    public boolean hasSource() { return (source != null); }


    /**
     *
     * Returns true if we have are connected to any downstream block
     * @return <code>true</code> if we are connected to at least one
     * downstream Block, else <code>false</code>
     *
     **/

    public boolean hasDest() { 
        if (this.dests == null) {
            return false;
        }
        return (!dests.isEmpty()); 
    }

    /**
     * Clears the list of destination blocks
     */

    public void removeDestBlocks() {
        if (this.dests != null) {
            this.dests.clear();
        }
        if (this.destPorts != null) {
            this.destPorts.clear();
        }
    }
    
    /**
     *
     * Returns true if IC value specified for this variable
     * @return boolean <code>true</code> if an initial value was
     * specified for this Signal, otherwise <code>false</code>. 
     *
     **/

    public boolean hasIC() { return this.hasIC; }

    
    /**
     *
     * <p> Returns text value of initial condition </p>
     *
     * @return String containing IC value
     **/

    public String getIC() { return this.IC; }

    /**
     * 
     * <p> Returns value of initial condition as Double </p>
     * 
     * @return Double representing IC value
     **/
    
    public Double getICValue() {
        return Double.parseDouble(this.getIC());
    }
    
    /**
     *
     * Indicates if we were declared an input signal
     * @return boolean <code>true</code> if we are an input signal,
     * otherwise <code>false</code>.
     *
     */

    public boolean isInput() { return this.isInput; }

    /**
     *
     * Indicates if we were declared a simulation control parameter
     * @return boolean <code>true</code> if we are flagged as a
     * 'simulation control parameter' per ANSI/AIAA-S-119-2011,
     * otherwise <code>false</code>
     *
     */

    public boolean isControl() { return this.isControl; }

    /**
     *
     * Indicates if we were declared a disturbance input
     * @return boolean <code>true</code> if we were declared a
     * 'disturbance input' per ANSI/AIAA-S-119-2011, otherwise
     * <code>false</code>.
     *
     */

    public boolean isDisturbance() { return this.isDisturbance; }

    /**
     *
     * Indicates if we were declared a state variable
     * @return boolean <code>true</code> if we were declared a
     * 'state variable' per ANSI/AIAA-S-119-2011, otherwise
     * <code>false</code>.
     *
     */

    public boolean isState() { return this.isState; }

    /**
     *
     * Indicates if we were declared a state derivative
     * @return boolean <code>true</code> if we were declared a
     * 'state derivative' per ANSI/AIAA-S-119-2011, otherwise
     * <code>false</code>.
     *
     */

    public boolean isStateDeriv() { return this.isStateDeriv; }

    /**
     *
     * Indicates if we need a downstream output block 
     * @return boolean <code>true</code> if we were declared an output
     * Signal, otherwise <code>false</code>.
     *
     **/

    public boolean isOutput() { return this.isOutput; }

    /**
     *
     * Indicates if we are one of the predefined AIAA variables
     * @return boolean <code>true</code> if our variable name conforms
     * to one of the standard variables defined in
     * ANSI/AIAA-S-119-2011, otherwise <code>false</code>.
     *
     */

    public boolean isStdAIAA() { return this.isStdAIAA; }
    
    
    /**
     *
     * Set the description directly
     * @param newDescription String containing our description
     *
     */
    
    public void setDescription( String newDescription ) {
        this.description = newDescription;
    }
    
    /**
     *
     * Get the description field
     * @return String containing out description
     *
     */
    
    public String getDescription() { return this.description; }
    
    /**
     *
     * Generate code for signal
     * @return {@link CodeAndVarNames} a snipped of logic to be turned
     * into source code based on the code dialect selected.
     *
     */
    
    public CodeAndVarNames genCode( ) {
        CodeAndVarNames cvn = new CodeAndVarNames();
        if (this.isDerived()) {
            if (this.source != null) {
                cvn.appendCode("(");
                cvn.append(this.source.genCode());
                cvn.appendCode(")");
            }
        } else {
            cvn.appendCode(this.getVarID());
            cvn.addVarName(this.getVarID());
        }  
        if (this.source != null) {
            this.setDefinedFlag(); // record that we've emitted our code
        }
        return cvn;
    }


    /**
     *
     * Generates brief description on output stream
     * @param writer FileWriter to use
     * @throws IOException if unable to write description
     *
     **/
    
    public void describeSelf( FileWriter writer ) throws IOException
    {
        int numDests = dests.size();
        Block outputBlock;
        Integer outputBPort;
        int thePortNum;

        writer.write("Signal \"" + myName + "\" (" + myUnits + ") [" + myVarID + "] connects ");

        if (source == null) {
            writer.write("NO SOURCE BLOCK to ");
        }
        else {
            writer.write("outport " + sourcePort + " of block ");
            writer.write(source.getName() + " to " );
        }
        //</editor-fold>

        switch (numDests) {
            case 0:
                writer.write("NO SINK BLOCKS.");
                break;
            case 1:
                thePortNum = (destPorts.get(0)).intValue();
                writer.write("inport " + thePortNum + " of block ");
                outputBlock = dests.get(0);
                writer.write(outputBlock.getName() + ".");
                break;
            default:
                writer.write("the following " + numDests + " blocks:\n");
                Iterator<Block> outBlockIterator = dests.iterator();
                Iterator<Integer> outBPortIterator = destPorts.iterator();
                while ( outBlockIterator.hasNext() ) {
                    outputBlock = outBlockIterator.next();
                    outputBPort = outBPortIterator.next();
                    thePortNum = outputBPort.intValue();
                    writer.write("   inport " + thePortNum + " of block " + outputBlock.getName() );
                    if (outBlockIterator.hasNext()) {
                        writer.write("\n");
                    }
                }
        }
    }

}
