// BlockOutput
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
 * <p> Object representing an output block </p>
 * <p> 031215 Bruce Jackson, NASA Langley Research Center
 *     <mailto:bjackson@adaptiveaero.com> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>  The Output Block represents an output to the system </p>
 *
 **/

public class BlockOutput extends Block
{
    /**
     *  units of measure of downstream block
     */

    String units;
    
    /**
     *
     * <p> Constructor for output Block <p>
     *
     * @param sourceSignal Upstream <code>Signal</code> with which to connect
     * @param m <code>Model</code> we're part of
     *
     **/

    public BlockOutput( Signal sourceSignal, Model m )
    {
        // Initialize superblock elements
        super(sourceSignal.getName(), "output", 1, m);

        // record our U of M
        this.units = sourceSignal.getUnits();

        // hook up to upstream signal
        //System.out.println("    BlockOutput constructor: " + myName + " is hooking up to signal " + sourceSignal.getName());
        sourceSignal.addSink( this, 1 );
        //System.out.println("    BlockOutput constructor: " + myName + " as output " + seqNumber);
    }


    /**
     * <p> Returns the output value </p>
     *
     * <p> This method is distinguished from normal
     * <code>Block.getValue()</code> in that it is public</p>
     *
     **/

    @Override
    public double getValue()    { return this.value; }


    /**
     * Returns the units of measure of the output signal
     * @return a String containing the units-of-measure, in ANSI/AIAA-S-119-2011 encoding
     **/

    public String getUnits() { return this.units; }


    /**
     *
     * Returns our sequence number (1-based) such as output 1, output 2, etc.
     * @return our sequuence number (1-base)
     *
     **/

    public int getSeqNumber()
    {
        BlockArrayList outputs = this.ourModel.getOutputBlocks();
        return outputs.indexOf( this ) + 1;
    }

    /**
     * Generate C-code comment about inputs
     * @return string containing C comment description of output
     */
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        cvn.appendCode(this.wrapComment(this.genComment()));
        cvn.addVarName(this.getInput(0).getVarID());
        return cvn;
    }

    /**
     * Common output documentation scheme for all code types
     * @return string with description of input signal
     */
    private String genComment() {
        String code = "";
        Signal theSignal = this.getInput(0);
        String inVarID = theSignal.getVarID();
        if (theSignal != null) {
            code = code + inVarID;
            if (theSignal.isStdAIAA()) {
                code = code + " (" + theSignal.getName() + ")";
            }
            code = code + " is a model output";
            if (units.equalsIgnoreCase("nd")) {
                code = code + " and is non-dimensional.";
            } else {
                code = code + " with units \'" + units + "\'";
            }
        }
        return code;
    }


    /**
     *
     * Generates description of self
     * @param  writer The output Writer to receive the text description
     * @throws IOException if unable to write self-description
     *
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" (" + units + ") and is an output block.");
    }

    /**
     *
     * Implements update() method
     * @throws DAVEException if update fails
     *
     **/

    @Override
    public void update() throws DAVEException
    {
        if (isVerbose()) {
            System.out.println();
            System.out.println("Method update() called for output block '" + this.getName() + "'");
        }
        
        // Check to see if only one input
        if (this.inputs.size() < 1) {
            throw new DAVEException(" Output block " + this.myName + 
                    " has no input.");
        }
        if (this.inputs.size() > 1) {
            throw new DAVEException(" Output block " + this.myName + 
                    " has more than one input.");
        }
        // see if single input variable is ready
        Signal theInput = this.inputs.get(0);
        if (!theInput.sourceReady()) {
            if (this.isVerbose()) {
                System.out.println(" Upstream signal '" + theInput.getName() + 
                        "' is not ready.");
            }
            return;
        }

        // get single input variable value
        double inputValue = theInput.sourceValue();
        if (this.isVerbose()) {
            System.out.println(" Input value is " + inputValue);
        }
        // show ourselves up-to-date
        this.resultsCycleCount = ourModel.getCycleCounter();

        // save answer
        this.value = inputValue;

    }
}
