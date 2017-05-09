// BlockMathMinmax
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
 * Extrema math function block
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 *
 * The MathMinmax block provides min, max functions
 *
 **/

public class BlockMathMinmax extends BlockMath
{
    /**
     *
     * Defined supported functions, for speed of execution
     *
     **/
    private static final int UNK   = 0;
    private static final int MIN   = 1;
    private static final int MAX   = 2;

    String funcType;    // can be "max" or "min"
    int op;             // can be 1 = MIN or 2 = MAX
    
    /**
     *
     * Constructor for Min-Max Math Block
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing <code>&lt;apply&gt;</code> element
     * @param m The parent <code>Model</code>
     *
     **/
    @SuppressWarnings("unchecked")
    public BlockMathMinmax( Element applyElement, Model m )
    {
        // Initialize superblock elements
        super("pending", "maxmin", m);
        this.funcType = null;
        this.op = UNK;

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        try {
            this.setFunction( first.getName () );
        } catch (DAVEException e) {
            System.err.println("Error - BlockMathMinmax constructor called with" +
                               " unknown element type:" + first.getName());
        }
        String blockType = first.getName();
        this.setName( blockType + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(blockType.equals("min") || blockType.equals("max") || 
                blockType.equals("ceil") || blockType.equals("floor")) {
                this.genInputsFromApply(ikid, 1);
        } else {
            System.err.println("Error - BlockMathMinmax constructor called with" +
                               " unknown element type:" + blockType);
        }
    }

    private void setFunction(String functionType) throws DAVEException {
    	this.funcType = functionType;
        this.setName( funcType + "_" + this.ourModel.getNumBlocks() );
        
        // take appropriate action based on type
        if(funcType.equals("min")) {
            this.op = MIN;
            this.myType = "minimum selector";
        } else if (funcType.equals("max")) {
            this.op = MAX;
            this.myType = "maximum selector";
        } else {
           throw new DAVEException("Unrecognized operator " + this.funcType +
        		   " in call to BlockMathMinMax.setFunction() method." );
        }
    }
    
    
    /**
     *
     * Returns the extrema function desired.
     * @return String containing function type (either "max" or "min")
     *
     */
    public String getFuncType() {
        return funcType;
    }

    /**
     *
     * Generates description of self
     * @param writer The output stream Writer
     * @throws IOException if unable to write
     *
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a extrema math block.");
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
        Iterator<Signal> theInputs;
        double[] inputVals;
        Signal theInput;
        int index;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Method update() called for extrema block '" + this.getName() + "'");
        }
        
        // Check to see if inputs are ready
        theInputs = this.inputs.iterator();
        inputVals = new double[this.inputs.size()];

        index = 0;
        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose) {
                    System.out.println(" Upstream signal '" + theInput.getName() + 
                            "' is not ready.");
                }
                return;
            } else {
                inputVals[index] = theInput.sourceValue();
                if (verbose) {
                    System.out.println(" Input #" + index + " value is " + 
                            inputVals[index]);
                }
            }
            index++;
        }

        this.value = 0.0;
        switch (this.op) {
        case MIN:
            this.value = Double.POSITIVE_INFINITY; break;
        case MAX:
            this.value = Double.NEGATIVE_INFINITY; break;
        }
        
        for(int i = 0; i<inputVals.length; i++) {
            switch (this.op) {
            case MIN:
                this.value = Math.min(this.value, inputVals[i]); break;
            case MAX:
                this.value = Math.max(this.value, inputVals[i]); break;
            }
        }

        if (this.value == Double.NaN) {
            throw new DAVEException("Unrecognized operator " + this.funcType + 
                    " in block " + this.getName());
        }

        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();

    }
}
