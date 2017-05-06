// BlockMathSum
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
 * <p> Summing math function block </p>
 * <p> 031211 Bruce Jackson <mailto:bruce@digiflightdyn.com> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 *
 * <p> The MathSum block represents a scalar summer </p>
 *
 **/

public class BlockMathSum extends BlockMath
{
    
    /**
     *
     * <p> Constructor for Sum Block <p>
     *
     * @param applyElement Reference to org.jdom.Element
     * containing "apply" element
     * @param m Our parent Model
     * @throws DAVEException if constructor fails
     *
     **/

    @SuppressWarnings("unchecked")
    public BlockMathSum( Element applyElement, Model m ) throws DAVEException
    {
        // Initialize superblock elements
        super("pending", "summing", m);

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        String blockType = first.getName();
        this.setName( blockType + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(!blockType.equals("plus")) {
          System.err.println("Error - BlockMathSum constructor called with" +
                             " type element:" + blockType);
        } else {
            //System.out.println("   BlockMathSum constructor called with " + kids.size() + "elements.");
            this.genInputsFromApply(ikid, 1); // may throw DAVEException
        }

        //System.out.println("    BlockMathSum constructor: " + this.getName() + " created.");
    }

    /**
     * <p> Generate code equivalent of our operation</p>
     */
    
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Iterator<Signal> inputSig = inputs.iterator();
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
//            code = "// Code for variable \"" + outVarID + "\":\n";
            cvn.appendCode(indent() + outVarID + " = ");
            cvn.addVarName(outVarID);
        }
        while (inputSig.hasNext()) {
            Signal inSig = inputSig.next();
            cvn.append(inSig.genCode());
            if (inputSig.hasNext()) {
                cvn.appendCode(" + ");
            }
        }
        // if not derived, need trailing semicolon and new line
        if (!outputSig.isDerived()) {
            cvn.appendCode(endLine());
        }
        return cvn;
    }

        
    /**
     *
     * Generates description of self
     * @param writer the Writer to receive our self-description
     * @throws IOException if unable to generate description on output Writer
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a Sum math block.");
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
            System.out.println("Method update() called for summing block '" + this.getName() + "'");
        }
        
        // Check to see if inputs are ready
        theInputs = this.inputs.iterator();
        inputVals = new double[this.inputs.size()];

        index = 0;
        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose) {
                    System.out.println(" Upstream signal '" + 
                            theInput.getName() + "' is not ready.");
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
        for(int i = 0; i<inputVals.length; i++) {
            this.value += inputVals[i];
        }
        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();

    }
}
