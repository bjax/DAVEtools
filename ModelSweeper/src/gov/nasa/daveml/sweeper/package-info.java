/**
 * <p>
 * Provides the classes necessary to create ModelSweeper for input/output
 * mapping visualization of DAVE-ML files.
 * <p>
 * To use, run <code>java -jar ModelSweeper.jar</code>
 * <p>
 * then browse to locate the DAVE-ML model you'd like to examine.
 * <p>
 * Select two inputs and one output to view the resulting 3-D grid surface.
 * The Coarser and Denser buttons densify or thin the input/output grid.
 * <p>
 * Min, max and nominal values for the input signals are selected from the
 * extrema values found in any embedded checkcases. Nominal is set to half-way
 * between the extremes.  These can be changed in the provided dialog box.
 * <p>
 * Grid images can be exported as PNGs or as text tables using buttons
 * provided.
 *
 * @author Bruce Jackson, Digital Flight Dynamics, 
 *  <a href="bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 *
 * @since 1.0
 * @see gov.nasa.daveml.dave
 */
package gov.nasa.daveml.sweeper;
