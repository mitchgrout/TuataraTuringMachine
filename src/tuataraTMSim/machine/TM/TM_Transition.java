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

package tuataraTMSim.machine.TM;

import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.Collection;
import tuataraTMSim.machine.*;
import tuataraTMSim.Spline;

/**
 * Represents a transition in a Turing machine.
 * @author Jimmy
 */
public class TM_Transition extends Transition<TM_Action, TM_State, TM_Machine, TM_Simulator> 
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of TM_Transition.
     */
    public TM_Transition()
    { 
        // Default values for our member variables
        super(null, null, null, 0, 0);
    }
    
    /** 
     * Creates a new instance of TM_Transition, given the two connecting states, input symbol, and
     * action.
     * @param fromState The state this transition leaves.
     * @param toState The state this transition arrives at.
     * @param action The action associated with this transition.
     */
    public TM_Transition(TM_State fromState, TM_State toState, TM_Action action)
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
        int height = metric.getAscent();
        return new Rectangle2D.Double(
                mid.getX() - 2 * width, mid.getY() - (3 * height) / 4,
                2 * width, (3 * height) / 2);
    }

    /**
     * Get the bounding box associated with the action.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the action.
     */
    public Rectangle2D getOutputSymbolBoundingBox(Graphics g)
    {
        Point2D mid = getActionLocation();
        FontMetrics metric = g.getFontMetrics(g.getFont());
        int width = metric.charWidth('_');
        int height = metric.getAscent();
        return new Rectangle2D.Double(
                mid.getX(), mid.getY() - (3 * height) / 4,
                2 * width, (3 * height) / 2);
    }
    
    /**
     * Get a String representation of this transition.
     * @return A string representation of this transition.
     */
    public String toString()
    {
        return String.format("%s -> %s [%s/%s]", m_fromState.getLabel(), m_toState.getLabel(),
                m_action.getInputChar(), m_action.getOutputChar());
    }
}
