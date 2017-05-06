// SignalArrayList
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
//
// 020501 EBJ

package gov.nasa.daveml.dave;

/**
 * <p>  Extends ArrayList for Signal objects </p>
 * <p> 031212 Bruce Jackson <mailto:bruce@digiflightdyn.com> </p>
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * <p>
 * Extends the ArrayList object for Signal objects
 * </p>
 * 
 */

@SuppressWarnings("serial")

    public class SignalArrayList extends ArrayList<Signal> {

    /**
     * 
     * <p>
     * Constructor takes no arguments
     * </p>
     * 
     **/

    public SignalArrayList() {
        super();
    }

    /**
     * 
     * <p>
     * Constructor takes initial capacity estimate
     * </p>
     * 
     * @param initialCapacity
     *            integer estimate of required slots
     * 
     **/

    public SignalArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 
     * <p>
     * Constructor converts existing <code>Collection</code>
     * </p>
     * 
     * @param c
     *            existing <code>Collection</code>
     * 
     **/

    public SignalArrayList(Collection<Signal> c) {
        super(c);
    }

    /**
     * 
     * This method locates a {@link Signal} from a varID
     * @param ID String with the varID of interest
     * @return Signal or null if not found
     * 
     **/

    public Signal findByID(String ID) {
        Iterator<Signal> signalIterator = this.iterator();
        while (signalIterator.hasNext()) {
            Signal theSignal = signalIterator.next();
            if (ID.equals(theSignal.getVarID())) {
                return theSignal;
            }
        }
        return null;
    }

    /**
     *
     * This method returns a new <code>SignalArrayList</code> containing just
     * the <code>Signal</code>s that are inputs to function blocks. If these do
     * not have a BlockBP as their source, they are "dangling," meaning they
     * need a breakpoint block inserted.
     * <p>
     * Deprecated; no longer used for DAVE-ML v 1.5 syntax and higher.
     * 
     * @return {@link SignalArrayList} containing any dangling function inputs
     * @deprecated 
     * 
     **/

    @Deprecated
    public SignalArrayList findDanglingFuncInputs() {
        SignalArrayList funcInputs = new SignalArrayList(this.size());
        BlockArrayList bal;
        Iterator<Block> i;

        Iterator<Signal> signalIterator = this.iterator();
        while (signalIterator.hasNext()) {
            Signal s = signalIterator.next();
            bal = s.getDests();
            i = bal.iterator();
            while (i.hasNext()) {
                Block b = i.next();
                if (b instanceof BlockFuncTable) {
                    // here with Signal that has a function
                    // table destination. Now check for
                    // missing breakpoint source.

                    Block a = s.getSource();
                    if (!(a instanceof BlockBP)) {
                        funcInputs.add(s);
                    }
                    break;
                }
            }
        }
        return funcInputs;
    }
}
