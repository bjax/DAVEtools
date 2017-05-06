// BlockMathSwitchTest.java
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

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathSwitchTest extends TestCase {

    protected Model _model;
    private StringWriter _writer;
    protected BlockMathSwitch _block;
    protected BlockMathConstant _inputBlock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _model = new Model(3, 3);
        _writer = new StringWriter();

        _block = generateSampleSwitch(_model);

        // retrieve the input block
        SignalArrayList theSignals = _model.getSignals();
        Signal inputSignal = theSignals.findByID("switchVal");
        assertNotNull(inputSignal);
        Block theBlock = inputSignal.getSource();
        assertNotNull(theBlock);
        assertEquals("constant value", theBlock.myType);
        _inputBlock = (BlockMathConstant) theBlock;
        assertNotNull(_inputBlock);
    }

    public void testUpdate() {
        assertNotNull(_block);
        String routineName = "TestBlockMathSwitch::testUpdate()";

        _model.wireBlocks();

        // must call this routine before cycling (but can ignore returned object)
        try {
            _model.getInputVector();
        } catch (DAVEException e) {
            fail("error when trying to obtain VectorInfoArrayList in "
                    + routineName + ": " + e.getMessage());
        }

        try {
            _model.initialize();
        } catch (DAVEException e) {
            fail("error when trying to initialize model in "
                    + routineName + ": " + e.getMessage());
        }

        assertEquals(-1.0, checkSwitch(-99.));
        assertEquals(-1.0, checkSwitch(-1.));
        assertEquals(-1.0, checkSwitch(-0.000001));
        assertEquals(+1.0, checkSwitch(-0.0));
        assertEquals(+1.0, checkSwitch(0.0));
        assertEquals(+1.0, checkSwitch(+0.0));
        assertEquals(+1.0, checkSwitch(+0.000001));
        assertEquals(+1.0, checkSwitch(+1.));
        assertEquals(+1.0, checkSwitch(+99.));
    }

    private double checkSwitch(Double switchVal) {
        String routineName = "TestBlockMathSwitch.checkSwitch()";

        // set operand values
        _inputBlock.setValue(switchVal);

        // run model
        try {
            _model.cycle();
        } catch (Exception e) {
            fail("Unexpected exception in " + routineName
                    + " for [" + switchVal.toString() + "]: " + e.getMessage());
        }

        // check result
        return _block.getValue();
    }
    
    public void testGenCcode() {
        _model.setCodeDialect(Model.DT_ANSI_C);
        String code = "";
        String indent = "  ";
        code = code + indent + "outputSignal = (1);\n";
        code = code + indent + "if ( (switchVal < (0)) ) {\n";
        code = code + indent + indent + "outputSignal = (-1);\n";
        code = code + indent + "}\n";
        CodeAndVarNames cvn = _block.genCode();
        assertEquals(code, cvn.getCode());
    }
    
    public void testGenFcode() {
        _model.setCodeDialect(Model.DT_FORTRAN);
        String code = "";
        String indent = "       ";
        code = code + indent + "outputSignal = (1)\n";
        code = code + indent + "IF( (switchVal .LT. (0)) ) THEN\n";
        code = code + indent + "  outputSignal = (-1)\n";
        code = code + indent + "ENDIF\n";
        CodeAndVarNames cvn = _block.genCode();
        assertEquals(code, cvn.getCode());
    }

    public void testDescribeSelfWriter() {
        try {
            _block.describeSelf(_writer);
        } catch (IOException e) {
            fail("testDescribeSelfWriter of TestBlockMathSwitch threw unexpected exception: "
                    + e.getMessage());
        }
        assertEquals("Block \"switch_2\" has three inputs (const_2, unnamed, const_5),"
                + " one output (outputSignal), value [NaN] and is a Switch math block.",
                _writer.toString());
    }

    private BlockMathSwitch generateSampleSwitch(Model model) {

        // Generate a BlockMathSwitch block (and associated wiring) for
        // a switch that outputs -1 if switchVal is less than 0 and +1 otherwise
        //        <apply>
        //          <piecewise>
        //            <piece>
        //              <cn>-1</ci>
        //              <apply>
        //                <lt/>
        //                <ci>switchVal</ci>
        //                <cn>0</cn>
        //              </apply>
        //            </piece>
        //            <otherwise>
        //              <cn>1</cn>
        //            </otherwise>
        //          </piecewise>
        //        </apply>

        Block  swValueBlock;
        String swValueSignalID;
        Signal swValueSignal;
        Signal outputSignal;

        swValueBlock = new BlockMathConstant("99", model);
        swValueSignalID = "switchVal";
        swValueSignal = new Signal("switch value", swValueSignalID, "nd", 1, model);
        try {
            swValueBlock.addOutput(swValueSignal);
        } catch (DAVEException e) {
            fail("Unexpected exception in TestBlockMathSwitch.generateSampleSwitch() "
                    + e.getMessage());
        }

        // create downstream signal
        outputSignal = new Signal("outputSignal", model);

        // build JDOM from XML snippet

        Element swValue = new Element("ci");
        swValue.addContent("switchVal");          //     <ci>switchVal</ci>

        // build <cn>0</cn>
        Element zero = new Element("cn");
        zero.addContent("0");	                    //     <cn>0</cn>

        // build <lt/>
        Element lt = new Element("lt");             //     </lt>

        Element innerApply = new Element("apply");  //     <apply>
        innerApply.addContent(lt);                  //       <lt/>
        innerApply.addContent(swValue);             //       <ci>switchVal</ci>
        innerApply.addContent(zero);                //       <cn>0</cn>
        //     </apply>

        Element minusOne = new Element("cn");
        minusOne.addContent("-1");                  //     <cn>-1</cn>

        Element piece = new Element("piece");       //   <piece>
        piece.addContent(minusOne);                 //     <cn>-1</cn>
        piece.addContent(innerApply);               //     <apply>
                                                    //       <lt/>
                                                    //       <ci>switchVal</ci>
                                                    //       <cn>0</cn>
                                                    //     </apply>
                                                    //   </piece>

        Element one = new Element("cn");
        one.addContent("1");                        //     <cn>1</cn>

        Element otherwise = new Element("otherwise");
        otherwise.addContent(one);                  //   <otherwise>
                                                    //     <cn>1</cn>
                                                    //   </otherwise>

        Element piecewise = new Element("piecewise");
        piecewise.addContent(piece);
        piecewise.addContent(otherwise);


        Element outerApply = new Element("apply");
        outerApply.addContent(piecewise);

        BlockMathSwitch bms = null;
        bms = new BlockMathSwitch(outerApply, model);
        try { // hook up inputs and outputs to the switch
            bms.addOutput(outputSignal);
            _model.wireBlocks();
        } catch (DAVEException e) {
            fail("Unexpected exception in hooking up output signal "
                    + "in TestBlockMathSwitch.generateSampleSwitch: " + e.getMessage());
        }
        return bms;
    }
}
