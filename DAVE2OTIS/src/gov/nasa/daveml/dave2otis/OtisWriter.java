// OtisWriter.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, originally of NASA LaRC, now at
//  Digital Flight Dynamics. <bruce@digiflightdyn.com>
//
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://github.com/bjax/DAVEtools
//
//  Original version of DAVEtools, prior to version 0.9.8: Copyright (c) 2007 United States
//  Government as represented by LAR-17460-1. No copyright is claimed in the United States under
//  Title 17, U.S. Code. All Other Rights Reserved.
//
//  Copyright (c) 2017 Digital Flight Dynamics

package gov.nasa.daveml.dave2otis;

import gov.nasa.daveml.dave.Model;

import gov.nasa.daveml.dave.Signal;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds common methods for both equation and table writers
 * @author ebjackso
 */
public abstract class OtisWriter extends FileWriter {
    Map<String,String> idMap; /** mapping of Std AIAA names to OTIS names   */
        Model  ourModel;

    public OtisWriter(Model theModel, String tableFileName) 
            throws IOException {
        super( tableFileName );
        ourModel = theModel;
    }
    
    /**
     * Translates variable identified by varID into OTIS ABLOCK name
     * if available.
     * 
     * Uses the varID to fetch the variable; if the variable is a standard AIAA 
     * variable, and there is a matching OTIS variable name, it returns the OTIS
     * name.
     * 
     * @param varID variable ID to translate into OTIS name
     * @return a String containing the translated varID
     */
    
    protected String translate( String varID ) {
        
        String output = varID;
        if (idMap == null)
            this.setupMap(); // initialize the mapping of AIAA -> OTIS varnames
        
        // find variable (signal) definition in source XML
        Signal signal = ourModel.getSignals().findByID(varID);
        
        // if standard, do lookup in map
        if( signal.isStdAIAA() ) {
            String aiaaName = this.getAIAAName( signal );
            String otisName = idMap.get(aiaaName);
            if (otisName != null) {
                output = otisName;
            }
        }
        return output;
    }

    /**
     * Build the map from Standard AIAA (S-119 defined) variable names to OTIS
     */
    private void setupMap() {
        idMap = new HashMap<String, String>();
        //         AIAA Standard Name_units  OTIS
        idMap.put("angleOfAttack_rad"      , "ALPHA" );
        idMap.put("angleOfAttack_deg"      , "ALPHAD");
        idMap.put("angleOfSideslip_rad"    , "BETA"  );
        idMap.put("angleOfSideslip_deg"    , "BETAD" );
        idMap.put("totalCoefficientOfDrag" , "CD"    );
        idMap.put("totalCoefficientOfLift" , "CL"    );
        idMap.put("mach"                   , "MACH"  );
        idMap.put("eulerAngle_rad_Roll"    , "PHIG"  );
        idMap.put("eulerAngle_deg_Roll"    , "PHIGD" );
        idMap.put("eulerAngle_rad_Yaw"     , "PSIG"  );
        idMap.put("eulerAngle_deg_Yaw"     , "PSIGD" );
        idMap.put("dynamicPressure_lbf_ft2", "Q"     );
        idMap.put("eulerAngle_rad_Pitch"   , "THETG" );
        idMap.put("eulerAngle_deg_Pitch"   , "THETGD");
        idMap.put("trueAirspeed_ft_s"      , "VEL"   );
        // TODO - needs expansion - above for proof-of-concept
    }

    
    /**
     * Convert signal name (pseudo-AIAA name) into true AIAA name
     * that potentially has both units and/or axis id appended
     * @param signal
     * @return 
     */
    private String getAIAAName(Signal signal) {
        String varName  = signal.getName();
        String units    = signal.getUnits();
        String aiaaName = varName;
        String axisName = "";

        // now deal with eulerAngle_deg_Roll; scalar AIAA name would 
        // be coded as eulerAngle_Roll; must insert units ahead of _Roll
        int underbar = varName.indexOf("_");
        int varNameLen = varName.length();

        if (underbar > 0) {
            axisName = varName.substring(underbar,(varNameLen-underbar));
            aiaaName = varName.substring(underbar);
        }
        if (!units.equalsIgnoreCase("nd")) {
            aiaaName = varName + "_" + units;
        }
        aiaaName += axisName; // add _Roll, e.g.
        
        return aiaaName;
    }
    
    
    /**
     *
     * Remove extra whitespace from input string
     * @param input a String possibly with redundant whitespace
     * @return a String with whitespace normalized
     *
     */
    
    public String normalize( String input ) {
        if (input != null) {
            input = input.replace('\n', ' '); // newlines -> spaces
            input = input.replace('\t', ' '); // tabs -> spaces
            input = input.replace("  ", " "); // remove dup spaces
            input = input.replace("  ", " "); // again
        }
        return input;
    }
    
    /**
     * Indicates if provided varID needs to be converted
     * 
     * Uses the varID to fetch the variable; if the variable is a standard AIAA 
     * variable, and there is a matching OTIS variable name, it returns the OTIS
     * name.
     * 
     * @param varID variable ID to translate into OTIS name
     * @return flag is true if translation required
     */
    
    public boolean needsTranslation(String varID) {
        
        String output = varID;
        if (idMap == null)
            this.setupMap(); // initialize the mapping of AIAA -> OTIS varnames
        
        // find variable (signal) definition in source XML
        Signal signal = ourModel.getSignals().findByID(varID);
        
        // if standard, do lookup in map
        if( signal.isStdAIAA() ) {
            String aiaaName = this.getAIAAName( signal );
            String otisName = idMap.get(aiaaName);
            if (otisName != null) {
                return true;
            }
        }
        return false;
    }


}
