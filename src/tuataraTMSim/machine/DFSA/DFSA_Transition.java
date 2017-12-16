//  ------------------------------------------------------------------
//
//  Copyright (c) 2006-2007 James Foulds and the University of Waikato
//
//  ------------------------------------------------------------------
//  This file is part of Tuatara Turing Machine Simulator.
//
//  Tuatara Turing Machine Simulator is free software: you can redistribute
//  it and/or modify it under the terms of the GNU General Public License as
//  published by the Free Software Foundation, either version 3 of the License,
//  or (at your option) any later version.
//
//  Tuatara Turing Machine Simulator is distributed in the hope that it will be
//  useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with Tuatara Turing Machine Simulator.  If not, see
//  <http://www.gnu.org/licenses/>.
//
//  author email: jf47 (at) waikato (dot) ac (dot) nz
//
//  ------------------------------------------------------------------

package tuataraTMSim.machine.DFSA;

import java.awt.*;
import java.awt.geom.*;
import java.util.Collection;
import java.io.Serializable;
import tuataraTMSim.machine.*;
import tuataraTMSim.Spline;

/**
 * Represents a transition in a machine.
 */
public class DFSA_Transition extends Transition<DFSA_Action, DFSA_State, DFSA_Machine, DFSA_Simulator>
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of DFSA_Transition.
     */
    public DFSA_Transition()
    {
        super(null, null, null, 0, 0);
    }

    /**
     * Creates a new instance of DFSA_Transition, given the two connecting states and action.
     * @param fromState The state this transition leaves.
     * @param toState The state this transition arrives at.
     * @param action The action associated with this transition.
     */
    public DFSA_Transition(DFSA_State fromState, DFSA_State toState, DFSA_Action action)
    {
        super(fromState, toState, action);
    }

    /**
     * Get the bounding box associated with the input symbol.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the input symbol.
     */
    public Rectangle2D getInputSymbolBoundingBox(Graphics g)
    {
        Point2D mid = getActionLocation();
        FontMetrics metric = g.getFontMetrics(g.getFont());
        int width = metric.charWidth('_');
        return new Rectangle2D.Double(
                mid.getX() - width / 2, mid.getY() - metric.getAscent() / 2,
                width, metric.getAscent());
    }
 
    /**
     * Get the bounding box associated with the action.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the action.
     */
    public Rectangle2D getOutputSymbolBoundingBox(Graphics g)
    {
        // No output symbol for a DFSA
        return new Rectangle2D.Float(0f, 0f, 0f, 0f);
    }

    /**
     * Get a String representation of this transition.
     * @return A string representation of this transition.
     */
    public String toString()
    {
        return String.format("%s -%c-> %s", 
                m_fromState.getLabel(), m_action.getInputChar(), m_toState.getLabel());
    }
}
