// BlockInput
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
 * <p> Object representing an input block </p>
 *
 * <p> 031215 Bruce Jackson <mailto:bjackson@adaptiveaero.com> </p>
 **/
import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>  The Input Block represents an input to the system </p>
 *
 **/
public class BlockInput extends Block {

    /**
     *  units of measure for downstream signal
     */
    String units;
    
    /**
     * Indicates if there is an initial value associated
     * with the downstream signal
     */
    
    boolean hasIC;

    /**
     *
     * <p> Constructor for input Block <p>
     *
     * @param theSignal Downstream <code>Signal</code> with which to connect
     * @param m <code>Model</code> we're part of
     *
     **/
    public BlockInput(Signal theSignal, Model m) {
        // Initialize superblock elements
        super(theSignal.getName(), "input", 0, m);

        // record our units
        this.units = theSignal.getUnits();
        
        // set 'hasIC flag'
        this.hasIC = false;

        // hook up to downstream signal
        try {
            this.addOutput(theSignal);
            this.hasIC = theSignal.hasIC();
        } catch (DAVEException e) {
            System.err.println("Unexpected error: new Input block '" + this.getName()
                    + "' unable to hook up to downstream signal ");
            System.exit(0);
        }
//System.out.println("    BlockInput constructor: " + myName + " as input " + seqNumber);
    }

    /**
     * Accepts a new input value
     *
     * @param theValue a <code>double</code> value which represents the new input value
     *
     */
    public void setInputValue(double theValue) {
        this.value = theValue;
    }

    /**
     * Returns the units of measure of the input signal
     *
     * @return String containing units of measure (encoded per ANSI/AIAA S-119-2011)
     *
     **/
    public String getUnits() {
        return this.units;
    }
    
    /**
     * <p> Returns flag indicating if an initial condition has 
     * been specified for the downstream Signal</p>
     * @return flag indicates if there is an IC value
     **/
    public boolean hasIC() {
        return this.hasIC;
    }

    /**
     *
     * Returns our sequence number (1-based) such as input 1, input 2, etc.
     * @return 1-based sequence number
     *
     **/
    public int getSeqNumber() {
        BlockArrayList modelInputs = this.ourModel.getInputBlocks();
        return modelInputs.indexOf(this) + 1;
    }

    
    /**
     * Common input documentation scheme for all code types
     * @return string with description of input signal
     */
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        String code = "";
        Signal theSignal = this.getOutput();
        if (theSignal != null) {
            cvn.addVarName(outVarID);
            code += outVarID;
            if (theSignal.isStdAIAA()) {
                code += " (" + theSignal.getName() + ")";
            }
            code += " is a model input";
            if (units.equalsIgnoreCase("nd")) {
                code += " and is non-dimensional.";
            } else {
                code += " with units \'" + units + "\'";
            }
        }
        cvn.appendCode(this.wrapComment(code));
        return cvn;
    }

    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws IOException if problems writing out description
     **/
    @Override
    public void describeSelf(Writer writer) throws IOException {
        super.describeSelf(writer);
        writer.write(" (" + units + ")");
    }

    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException if an error occurs while updating
     *
     **/
    @Override
    public void update() throws DAVEException {
        if (isVerbose()) {
            System.out.println();
            System.out.println("Method update() called for input block '" + this.getName() + "'");
        }

        // input blocks are always assumed ready
        this.resultsCycleCount = ourModel.getCycleCounter();
    }
}
