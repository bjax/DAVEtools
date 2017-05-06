// Block.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.Writer;

/**
 * Object representing each <code>Block</code> in a {@link Model}.
 * <p>
 * There should be one of these for each computational element in a {@link Model}. These are
 * connected to each other with {@link Signal}s.
 * <p>
 * Hook up convention is, after creating a new <code>Block</code> and <code>Signal</code>, to call
 * the <code>Block</code>'s {@link #addOutput(Signal)} method with the desired output
 * <code>Signal</code>; in turn, this <code>Block</code> will invoke that <code>Signal</code>'s
 * {@link Signal#addSource(Block)} method with this <code>Block</code> element as the input
 * parameter.
 * <p>
 * Modification history:
 * <ul>
 * <li>2003-12-11 Written EBJ</li>
 * </ul>
 *
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 */

abstract public class Block {

    /**
     * our model parent
     */

    Model ourModel;

    /**
     * name of block
     */
    String myName;

    /**
     * our type
     */
    String myType;

    /**
     * Guarantees unique XML name of block
     */
    NameList nameList;

    /**
     * variable IDs associated with each input - keep in sync with inputs!
     */
    private ArrayList<String> inVarIDs;

    /**
     * list of input signals
     */
    SignalArrayList inputs;

    /**
     * only one output variable - not set by all blocks!
     */
    protected String outVarID;

    /**
     * our sole output signal
     */
    private Signal output;

    /**
     * value of Model cycleCounter when results were valid
     */
    protected int resultsCycleCount;

    /**
     * our latest output value
     */
    double value;

    /**
     * are we chatty?
     */
    boolean verboseFlag;

    /**
     * selected flag
     *
     * @since 0.9.4
     */
    protected boolean selectedFlag;

    /**
     * possible masking object (like SLBlock)
     */
    Object mask;

    /**
     * Basic constructor for <code>Block</code>
     */
    public Block() {
        this.ourModel = null;
        this.myName = null;
        this.myType = "not defined";
        this.nameList = null;
        this.output = null;
        this.inVarIDs = new ArrayList<String>(1);
        this.inputs = new SignalArrayList(1);
        this.resultsCycleCount = -1;
        this.value = Double.NaN;
        this.verboseFlag = false;
        this.selectedFlag = true;
        this.mask = null;
    }

    /**
     * More detailed Constructor for <code>Block</code> that adds itself 
     * to the parent {@link Model}.
     *
     * @param m Model we're part of
     */
    public Block(Model m) {
        this();
        this.ourModel = m;
        m.add(this);    // it can override verbose
    }

    /**
     *
     * <p>
     * Constructor for Block </p>
     *
     * @param blockName Name of block - must be unique
     * @param blockType The type of block to build
     * @param numInputs number of input ports
     * @param m <code>Model</code> we're part of
     *
     *
     */
    public Block(String blockName, String blockType, int numInputs, Model m) {
        this();
        this.ourModel = m;
        this.myName = blockName;
        this.myType = blockType;
        this.inVarIDs = new ArrayList<String>(numInputs);
        this.inputs = new SignalArrayList(numInputs);
        m.add(this);    // it can override verbose
    }

    /**
     *
     * Performs a deep copy of supplied <code>Block</code>
     *
     * @param b The Block to clone
     *
     *
     */
    public Block(Block b) {
        this();                 // invoke basic constructor
        this.ourModel = b.getModel();
        this.myName = b.getName();
        this.myType = b.getType();
        this.nameList = b.nameList;
        this.inVarIDs = b.inVarIDs;
        this.inputs = b.inputs;
        this.outVarID = b.outVarID;
        this.output = b.output;
        // don't copy resultsCycleCount, value, verboseFlag, or mask
    }

    /**
     *
     * Returns our current value.
     *
     * @return the current value
     *
     *
     */
    protected double getValue() {
        return this.value;
    }

    /**
     *
     * Indicates if all results are up-to-date.
     *
     * @return true if our result is up-to-date
     *
     *
     */
    public boolean isReady() {
        return ourModel.getCycleCounter() == this.resultsCycleCount;
    }

    /**
     *
     * Indicates if all inputs are connected
     *
     * @return true if all inputs are connected to Signals
     *
     */
    public boolean allInputsConnected() {
        boolean answer = true;
        Iterator<Signal> it = this.getInputIterator();
        while (it.hasNext() && answer) {
            answer = answer && (it.next() != null);
        }
        return answer;
    }

    /**
     *
     * Indicates if output is connected
     *
     * @return true if the output is connected to a Signal
     *
     */
    public boolean outputConnected() {
        return (this.output != null);
    }

    /**
     *
     * Checks to see if all upstream blocks are ready.
     *
     * @return true if all inputs to this block are ready
     *
     *
     */
    public boolean allInputsReady() {
        boolean allReady = true;

        // get signal iterator
        Iterator<Signal> sigIt = this.getInputIterator();

        // ask each signal if it's source is ready
        while (sigIt.hasNext()) {
            try {
                allReady = allReady && sigIt.next().sourceReady();
            } catch (DAVEException e) {
                System.err.println(e.getMessage());
                System.err.println("Wiring error found - missing source block.");
                System.exit(0);
            }
        }

        // output is AND of all signal ready results
        return allReady;
    }

    /**
     *
     * Sets verbose flag.
     *
     *
     */
    public void makeVerbose() {
        this.verboseFlag = true;
    }

    /**
     *
     * Unsets verbose flag.
     *
     *
     */
    public void silence() {
        this.verboseFlag = false;
    }

    /**
     *
     * Returns our verbose state.
     *
     * @return true if the verbose flag is set.
     *
     *
     */
    public boolean isVerbose() {
        return this.verboseFlag;
    }

    /**
     * Sets the block as 'selected'
     *
     * @since 0.9.4
     */
    public void select() {
        selectedFlag = true;
    }

    /**
     * Sets the block and all ancestors as 'selected'
     *
     * @since 0.9.4
     */
    public void selectWithAncestors() {
        this.select();
        Iterator<Signal> sigIt = this.getInputIterator();
        while (sigIt.hasNext()) {
            Signal sig = sigIt.next();
            Block ancestor = sig.getSource();
            if (ancestor != null) {
                ancestor.selectWithAncestors();
            }
        }
    }

    /**
     * Unselects the block
     *
     * @since 0.9.4
     */
    public void deselect() {
        selectedFlag = false;
    }

    /**
     * Returns the status of selection
     *
     * @return selectedFlag
     * @since 0.9.4
     */
    public boolean isSelected() {
        return selectedFlag;
    }

    /**
     *
     * Set a pointer to some masking object (like Simulink SLBlock)
     *
     * @param o the object of whose mask we are a part
     *
     *
     */
    public void setMask(Object o) {
        this.mask = o;
    }

    /**
     *
     * Returns the masking object (possibly null).
     *
     * @return mask the masking object
     *
     *
     */
    public Object getMask() {
        return this.mask;
    }

    /**
     *
     * Adds input to next port.
     *
     * <p>
     * Returns port number (1-based).
     *
     * @param inSignal signal to add
     * @return port number (1-based)
     *
     *
     */
    public int addInput(Signal inSignal) {
        int portIndex;

        inputs.add(inSignal);                   // add to end of list
        portIndex = inputs.indexOf(inSignal);   // find it's position
        while (inVarIDs.size() <= portIndex) {  // increase length of companion list if necessary
            inVarIDs.add(inSignal.getName());
        }
        String existingID = inVarIDs.get(portIndex);
        if (existingID.equals("")) {           // don't override existing ID (for FTB)
            inVarIDs.set(portIndex, inSignal.getVarID());       // keep in sync with inputs
        }
        //System.out.println("Adding signal " + inSignal.getName() + " as input " + (portIndex+1) + " to block " + getName());

        return portIndex + 1;
    }

    /**
     *
     * <p>
     * Adds input to specified port </p>
     *
     * @param inSignal signal to add
     * @param portNum port (1-based) to add signal
     *
     */
    public void addInput(Signal inSignal, int portNum) {
        int portIndex = portNum - 1;
        //System.out.println("Adding signal " + inSignal.getName() + " as input " + (portIndex+1) + " to block " + getName());
        while (inputs.size() <= portIndex) {             // grow list if necessary to reach offset
            inputs.add(inSignal);
        }
        inputs.set(portIndex, inSignal);                // record input signal

        while (inVarIDs.size() <= portIndex) {    // grow ID list with input list
            inVarIDs.add("");
        }
        String existingID = inVarIDs.get(portIndex);    // see if already set
        if (existingID.equals("")) {             // don't override existing ID (for FTB)
            inVarIDs.set(portIndex, inSignal.getVarID());       // keep in sync with inputs
        }
        //System.out.println("  Block " + myName + " has added upstream signal " + inSignal.getName());
    }

    /**
     *
     * <p>
     * Add input variable ID (usually to hook up later) </p>
     *
     * @param portNum port to associate with varID (1-based)
     * @param varID <code>String</code> containing unique variable ID
     *
     *
     */
    public void addVarID(int portNum, String varID) {
        if (this.isVerbose()) {
            System.out.println("Adding varID '" + varID + "' to port "
                    + portNum + " of block '" + this.getName() + "'.");
        }
        int portIndex = portNum - 1;
        if (portNum < 1) {
            System.err.println("Block '" + getName() + "' asked to hook up varID '" + varID
                    + "' to port " + portNum + " -- error!");
        } else {
            while (inVarIDs.size() <= portIndex) {
                inVarIDs.add("");
            }
            inVarIDs.set(portIndex, varID);

            while (inputs.size() <= portIndex) // keep in sync with varID array
            {
                inputs.add(null);
            }
            inputs.set(portIndex, null);
        }
    }

    /**
     * Replace current input Signal with new input Signal; used to insert
     * in-line limiters in Model's setUpLimiterFor() method
     *
     * @param oldSignal - the signal to replace
     * @param newSignal - the signal to use instead of oldSignal
     * @since 0.9.3
     */
    public void replaceInput(Signal oldSignal, Signal newSignal) {
        // find oldSignal's varID; in not found, ignore

        while (inputs.contains(oldSignal)) {
            int oldSignalIndex = inputs.indexOf(oldSignal);
            if (oldSignalIndex >= 0) {
                inputs.remove(oldSignalIndex);
                inputs.add(oldSignalIndex, newSignal);
            }
        }
    }

    /**
     *
     * Hook up output signal
     *
     * @param outSignal signal to add
     * @throws DAVEException if signal varID is different from expected or if
     * already hooked up to different signal
     *
     */
    public void addOutput(Signal outSignal) throws DAVEException {

        String newVarID = outSignal.getVarID();

        //  see if it's variD matches ours, if any.
        if (this.outVarID == null) {
            this.outVarID = newVarID;
        } else {
            if (!this.outVarID.equals(newVarID)) {
                throw new DAVEException("Error: specified output signal ID '" + newVarID
                        + "' doesn't match expected ('" + this.outVarID + "'.");
            }
        }
        this.output = outSignal;
        outSignal.addSource(this, 1); // tell signal about us
    }

    /**
     *
     * Record output varID
     *
     * @param outVarID <code>String</code> varID of output signal
     *
     */
    public void addOutput(String outVarID) {
        this.outVarID = outVarID;
    }

    /**
     * <p>
     * Hook up the output of a block to the given input, creating an output
     * {@link Signal} if necessary
     * @param blk the <tt>Block</tt> to connect the output from
     * @param inPort our input port number (1-based) to connect to the block's
     * output
     */
    protected void addInput(Block blk, int inPort) {
        Signal sig;
        if (blk.outputConnected()) {
            sig = blk.output;
        } else {
            sig = new Signal(blk.getName(), this.ourModel);
            sig.setDerivedFlag();  // flag as a variable we've constructed
            try {
                blk.addOutput(sig);
            } catch (DAVEException e) {
                System.err.println(" Unexpected error: couldn't add output signal to " + blk.getName());
                System.exit(0);
            }
        }
        sig.addSink(this, inPort);
    }

    /**
     *
     * <p>
     * Creates new upstream const block and signal wire </p>
     *
     * @param constantValue <code>String</code> containing value
     * @param inPort <code>int</code> value of our input port to use (1-based)
     *
     *
     */
    protected void addConstInput(String constantValue, int inPort) {
        Block constBlock = new BlockMathConstant(constantValue, ourModel);
        this.addInput(constBlock, inPort);
        // mark this new constant signal as 'derived' for code generators
        // since it doesn't appear as separate varID in source model
        Signal derivedConst = constBlock.getOutput();
        derivedConst.setDerivedFlag();
    }

    /**
     *
     * Returns our model.
     *
     * @return our model to which we belong
     *
     *
     */
    public Model getModel() {
        return this.ourModel;
    }

    /**
     *
     * Returns our name.
     *
     * @return a String containing our name
     *
     *
     */
    public String getName() {
        return this.myName;
    }

    /**
     *
     * Returns our type.
     *
     * @return a String containing our block type
     *
     *
     */
    public String getType() {
        return this.myType;
    }

    /**
     *
     * <p>
     * Returns variable ID (<code>varID</code>) associated with particular INPUT
     * port number (1-based). </p>
     *
     * @param portNum (1-based) port number
     * @return the variable ID associated with the given inport
     *
     *
     */
    public String getVarID(int portNum) {

        return inVarIDs.get(portNum - 1);
    }

    /**
     *
     * <p>
     * Returns an iterator to loop through input signals </p>
     *
     * @return <code>Iterator</code>
     *
     *
     */
    public Iterator<Signal> getInputIterator() {
        return this.inputs.iterator();
    }

    /**
     *
     * Returns signal hooked up to specified input port (0-based)
     *
     * @param index (0-based) of port
     * @return the signal from the given input port
     * @since 0.9
     */
    public Signal getInput(int index) {
        return inputs.get(index);
    }

    /**
     *
     * Returns our output signal
     *
     * @return our output Signal
     *
     *
     */
    public Signal getOutput() {
        return this.output;
    }

    /**
     *
     * <p>
     * Returns an iterator to loop through input variable IDs </p>
     *
     * @return <code>Iterator</code>
     *
     *
     */
    public Iterator<String> getVarIDIterator() {
        return this.inVarIDs.iterator();
    }

    /**
     *
     * Returns our output signal's ID
     *
     * @return a String with the output variable ID
     *
     *
     */
    public String getOutputVarID() {
        return this.outVarID;
    }

    /**
     *
     * <p>
     * Set name list and adjust name to match language requirements </p>
     *
     * @param newNameList <code>NameList</code> name must meet and be unique
     * within
     *
     *
     */
    public void setNameList(NameList newNameList) {
        //      System.out.println("Setting name list for block " + this.myName);
        this.nameList = newNameList;
        this.setName(this.getName()); // convert old name to be unique and well-formed
    }

    /**
     *
     * Change name of block to one acceptable to namespace
     *
     * @param newName the new name to record
     *
     *
     */
    protected void setName(String newName) {
        if (this.nameList != null) {
            this.myName = this.nameList.addUnique(newName);
        } else {
            this.myName = newName;
        }
        //System.out.println("Setting block name to " + this.myName);
    }

    /**
     *
     * This method forces the output varID to match that of the downstream
     * signal It is used when the downstream variable inserts a limiter block.
     *
     *
     */
    public void renameOutVarID() {
        this.outVarID = this.output.getVarID();
    }

    /**
     *
     * Forces the variable ID of the specified input port to match the connected
     * signal
     *
     * @param portNum the input port number (1-based) to modify
     *
     */
    public void renameInVarID(Integer portNum) {
        Signal input = this.inputs.get(portNum - 1);
        String newVarID = input.myVarID;
        this.inVarIDs.set(portNum - 1, newVarID);
    }

    /**
     *
     * <p>
     * Returns number of inputs </p>
     *
     * @return an int with the number of inputs
     *
     *
     */
    public int numInputs() {
        if (this.inputs != null) {
            return this.inputs.size();
        } else {
            return 0;
        }
    }

    /**
     *
     * <p>
     * Returns number of VarIDs </p>
     *
     * @return an int with the number of input variable IDS
     *
     *
     */
    public int numVarIDs() {
        return this.inVarIDs.size();
    }

    /**
     *
     * <p>
     * Hook up a single input signal, if it exists; otherwise, leave unconnected
     * for now. Returns true if successful. </p>
     *
     * @param port <code>int</code> with 1-based input number
     * @return true if successful
     * @throws DAVEException if the expected input variableID doesn't match the
     * associated signal's variableID
     *
     *
     */
    protected boolean hookUpInput(int port) throws DAVEException {
        // get input variable ID
        String signalVarID = inVarIDs.get(port - 1);

        // look for existing signal
        Signal theSignal = ourModel.getSignals().findByID(signalVarID);
        if (theSignal != null) {
            //System.out.println("     found signal with name " + signalVarID + ", connecting...");
            theSignal.addSink(this, port);        // does double link
            return true;
        } else {
            throw new DAVEException("    No signal named " + signalVarID
                    + " found; leaving input " + port
                    + " of block " + getName()
                    + " disconnected for now...");
        }
    }

    /**
     * Hook up to input signals
     *
     * For each input port that is not connected to a Signal, search for 
     * a Signal in the model with the correct variable ID and connect to it;
     * otherwise, leave port unconnected.
     */
    protected void hookUpInputs() {
        int portCount = 0;

        // loop through input variable IDs
        Iterator<String> iVarIDIterator = this.inVarIDs.iterator();

        while (iVarIDIterator.hasNext()) {

            // get independent variable ID
            String signalVarID = iVarIDIterator.next();

            // look for existing signal from previously built breakpoint block
            Signal theSignal = ourModel.getSignals().findByID(signalVarID);
            if (theSignal != null) {
                //System.out.println("     found signal with name " + signalVarID + ", connecting...");
                theSignal.addSink(this, portCount + 1); // does double link
                portCount++;    // count # of dimensions
            } else {
                System.err.println("    No signal named " + signalVarID + " found.");
            }
        }
    }

    /**
     * Attempts to hook up all inputs and the output to existing signals.
     *
     * @throws DAVEException if a necessary <code>Signal</code> is not found.
     *
     *
     */
    public void hookUp() throws DAVEException {
        // hook up all signals

        Iterator<Signal> it;
        int i;
        boolean result;
        Signal s;

        if (this.isVerbose()) {
            System.out.println(" hookUp called for block '" + this.getName() + "'");
        }
        // hook up inputs

        it = this.getInputIterator();
        i = 1;
        while (it.hasNext()) {
            s = it.next();
            if (s == null) {    // missing signal found
                result = hookUpInput(i);        // try to hook up
                if (!result) {  // hook up unsuccessful
                    String inputSignalVarID = inVarIDs.get(i - 1);
                    throw new DAVEException("Unable to find signal '" + inputSignalVarID + "' for input");
                }
            }
            i++;
        }

        // hook up output
        s = this.getOutput();   // get our sole output, if any

        if (s == null) { // it's not hooked up
            // search by varID for missing signal
            if (this.outVarID == null) {
                throw new DAVEException("Block '" + this.myName + "' has no output signal identified.");
            }
            s = ourModel.getSignals().findByID(this.outVarID);
            // if null returned, generate exception
            if (s == null) {
                throw new DAVEException("Unable to find signal '" + this.outVarID + "' for output");
            }
            this.addOutput(s);
        }

    }

    /**
     *
     * Verify that all inputs are connected. If not, try again to hook them up.
     * Returns true if all inputs are successfully connected to upstream
     * signals.
     *
     * @return true if inputs are connected to Signals
     *
     *
     */
    public boolean verifyInputs() {
        //System.out.println("Verifying inputs for block " + getName() + " which has " + this.inputs.size() + " inputs.");
        boolean error = false;

        // special debugging
        /*
         if (this.getName().equals("divide_4")) {
         System.out.println("*-*-*-*");
         System.out.println("in divide_4:");
         System.out.println("inVarIDs.size() is " + inVarIDs.size());
         System.out.println("inputs.size()   is " + inputs.size());
         System.out.println("*-*-*-*");
         }
         */
        // check to make sure inVarIDs size matches inputs
        if (inVarIDs.size() < inputs.size()) {
            System.err.println("In block '" + this.getName() + "' found more input signals than IDs!");
            System.exit(0);
        }

        if (inVarIDs.size() > inputs.size()) {  // there are missing input signals!
            System.err.println("In block '" + this.getName() + "' found missing input signals.");
            // should be able to repair
            System.err.println("FIXME needed in Block.java, near line 564");
            System.exit(0);
        }

        Iterator<Signal> ii = this.getInputIterator();
        int i = 1;
        while (ii.hasNext()) {
            //System.out.println("...checking input " + i );
            Signal s = ii.next();
            if (s == null) {
                try {
                    // Found missing input. Try to connect to existing signal...
                    error = error || !this.hookUpInput(i);
                } catch (DAVEException e) {
                    System.err.println("Encountered exception...");
                    System.err.println(e.getMessage());
                    System.exit(0);
                }
                i++;
            }
        }
        return error;
    }

    /**
     *
     * <p>
     * Verify that all outputs are connected. If not, try again to hook them up.
     * Returns true if all outputs are successfully connected to downstream
     * signals. </p>
     *
     * @return true if all outputs are connected to signals
     *
     *
     */
    public boolean verifyOutputs() {
        Signal s = this.getOutput();
        if (s == null) {        // output not yet filled
            String theOutVarID = this.getOutputVarID();
            if (theOutVarID == null) { // no output to hook up
                return false;
            } else {
                s = ourModel.getSignals().findByID(theOutVarID);
                try {
                    this.addOutput(s);
                } catch (DAVEException e) {
                    System.err.println("Unexpected error in verifyOutputs for block '"
                            + this.getName() + "': unable to add signal named '" + theOutVarID + "'.");
                    System.exit(0);
                }
            }
        }
        return (s == null);
    }

    /**
     *
     * Generate C-code equivalent of our operation This procedure should be
     * overridden.
     *
     * @return a CodeAndVarNames new instance containing a comment
     * @since 0.9.4
     *
     */
    public CodeAndVarNames genCode() {
        return new CodeAndVarNames(
                wrapComment("WARNING -- "
                        + "code generator not available for variable \""
                        + outVarID + "\" whose type is \"" + this.myType + "\""));
    }

    /**
     *
     * Generate appropriate indentation spaces
     *
     * @return a String with the correct number of indentation spaces for the
     * current level of indentation
     * @since 0.9.4
     *
     */
    public String indent() {
        String indent = "";
        int dialect = ourModel.getCodeDialect();
        switch (dialect) {
            case Model.DT_ANSI_C:
                indent = "  ";
                break;
            case Model.DT_FORTRAN:
                indent = "       ";
                break;
        }
        return indent;
    }

    /**
     *
     * Wrap message in appropriate language
     *
     * @param comment the comment String to wrap in the appropriate comment
     * block
     * @return String with original String wrapped in comment delimiters
     * @since 0.9.4
     */
    public String wrapComment(String comment) {
        String wrappedComment = "";
        int dialect = ourModel.getCodeDialect();
        switch (dialect) {
            case Model.DT_ANSI_C:
                wrappedComment = "/* " + comment + " */";
                break;
            case Model.DT_FORTRAN:
                wrappedComment = "!  " + comment;
                break;
        }
        wrappedComment += "\n";
        return wrappedComment;
    }

    /**
     *
     * Wrap error message in appropriate dialect
     *
     * @param errMsg a String containing an error message
     * @return String with the original message preceded with 'ERROR: '
     * @since 0.9.4
     */
    public String errorComment(String errMsg) {
        return this.wrapComment("ERROR: " + errMsg);
    }

    /**
     *
     * Add appropriate statement-ending characters
     *
     * @return String with the appropriate line ending characters
     * @since 0.9.4
     */
    public String endLine() {
        String lineEnd = "\n";
        if (ourModel.getCodeDialect() == Model.DT_ANSI_C) {
            lineEnd = ";\n";
        }
        return lineEnd;
    }

    /**
     *
     * Generate appropriate 'if' statement for given language
     *
     * @param condition A String containing a condition test
     * @return String containing the appropriate 'if' statement syntax to wrap
     * the condition string
     * @since 0.9.4
     */
    public String beginIf(String condition) {
        String ifStart = "";
        int dialect = ourModel.getCodeDialect();
        switch (dialect) {
            case Model.DT_ANSI_C:
                ifStart = indent() + "if ( " + condition + " ) {\n";
                break;
            case Model.DT_FORTRAN:
                ifStart = indent() + "IF( " + condition + " ) THEN\n";
                break;
        }
        return ifStart;
    }

    /**
     *
     * Generate appropriate if statement ending for given language
     *
     * @return String with the appropriate if-then ending grammar
     * @since 0.9.4
     */
    public String endIf() {
        String ifEnd = "";
        int dialect = ourModel.getCodeDialect();
        switch (dialect) {
            case Model.DT_ANSI_C:
                ifEnd = indent() + "}\n";
                break;
            case Model.DT_FORTRAN:
                ifEnd = indent() + "ENDIF\n";
                break;
        }
        return ifEnd;
    }

    /**
     *
     * Generates brief description on output stream
     *
     * @param writer FileWriter to use
     * @throws IOException if writing has a problem
     *
     *
     */
    public void describeSelf(Writer writer) throws IOException {
        int numInputs = inputs.size();
        int numOutputs = 0;
        if (this.output != null) {
            numOutputs = 1;
        }

        writer.write("Block \"" + myName + "\" has ");
        switch (numInputs) {
            case 0:
                writer.write("NO INPUTS, ");
                break;
            case 1:
                writer.write("one input (");
                break;
            case 2:
                writer.write("two inputs (");
                break;
            case 3:
                writer.write("three inputs (");
                break;
            case 4:
                writer.write("four inputs (");
                break;
            case 5:
                writer.write("five inputs (");
                break;
            case 6:
                writer.write("six inputs (");
                break;
            case 7:
                writer.write("seven inputs (");
                break;
            case 8:
                writer.write("eight inputs (");
                break;
            case 9:
                writer.write("nine inputs (");
                break;
            default:
                writer.write(numInputs + " inputs (");
        }

        if (numInputs > 0) {
            Iterator<Signal> sigIt = this.getInputIterator();
            while (sigIt.hasNext()) {
                Signal sig = sigIt.next();
                if (sig != null) {
                    String theName = sig.getName();
                    if (theName == null) {
                        writer.write("Unnamed signal");
                    } else {
                        writer.write(theName);
                    }
                }
                if (sigIt.hasNext()) {
                    writer.write(", ");
                }
            }
            writer.write("), ");
        }

        switch (numOutputs) {
            case 0:
                writer.write("NO OUTPUTS, ");
                break;
            case 1:
                writer.write("one output (");
                break;
            case 2:
                writer.write("two outputs (");
                break;
            case 3:
                writer.write("three outputs (");
                break;
            case 4:
                writer.write("four outputs (");
                break;
            case 5:
                writer.write("five outputs (");
                break;
            case 6:
                writer.write("six outputs (");
                break;
            case 7:
                writer.write("seven outputs (");
                break;
            case 8:
                writer.write("eight outputs (");
                break;
            case 9:
                writer.write("nine outputs (");
                break;
            default:
                writer.write(numOutputs + " (");
        }

        if (numOutputs > 0) {
            Signal sig = this.getOutput();
            if (sig != null) {
                String theName = sig.getName();
                if (theName == null) {
                    writer.write("Unnamed signal");
                } else {
                    writer.write(theName);
                }
            }
            writer.write("), ");
        }

        writer.write("value [" + this.value + "]");
    }

    /**
     *
     * Updates the output value of the block.
     *
     * @throws DAVEException if problems are encountered updating the block
     *
     *
     */
    abstract public void update() throws DAVEException; // updates value of block

}
