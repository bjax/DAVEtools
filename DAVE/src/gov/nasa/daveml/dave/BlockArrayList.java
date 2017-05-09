// BlockArrayList
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
 * Extends <code>ArrayList</code> for {@link Block} objects
 * @author Bruce Jackson, Digital  &lt;bruce@digiflightdyn.com&gt;</li>
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 *
 */

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * The <code>BlockArrayList</code> extends the standard Java
 * <code>ArrayList</code> object to allow searching for specific types
 * of {@link Block}s.
 *
 **/

@SuppressWarnings("serial")
public class BlockArrayList extends ArrayList<Block>
{

    /**
     *
     * Simplest <code>BlockArrayList</code> constructor that takes no arguments
     *
     **/

    public BlockArrayList() { super(); }

    /**
     *
     * <code>BlockArrayList</code> constructor takes initial capacity estimate
     * @param initialCapacity integer estimate of required slots
     *
     **/

    public BlockArrayList( int initialCapacity ) { super(initialCapacity); }


    /**
     *
     * <code>BlockArrayList</code> constructor converts existing {@link Collection}
     * @param c existing Collection to convert
     *
     **/

    public BlockArrayList( Collection<Block> c ) { super(c); }

}
