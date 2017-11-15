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

package tuataraTMSim.TM;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Represents a state (node) in a Turing machine.
 * @author Jimmy
 */
public class TM_State implements Serializable
{
    // Width of the graphical representation of the state, in pixels
    public static final int STATE_RENDERING_WIDTH = 30;
    // Distance of graphical text below picture of the state
    public static final int TEXT_DISTANCE = 15; 
    
    /**
     * Creates a new instance of TM_State
     */
    public TM_State(String label, boolean startState, boolean finalState)
    {
        m_label = label;
        m_startState = startState;
        m_finalState = finalState;
        m_transitions = new ArrayList<TM_Transition>();
    }
    
    /**
     * Creates a new instance of TM_State
     * @param x, y     The graphical coordinates of the state.
     */
    public TM_State(String label, boolean startState, boolean finalState, int x, int y)
    {
        m_label = label;
        m_startState = startState;
        m_finalState = finalState;
        m_transitions = new ArrayList<TM_Transition>();
        
        m_windowX = x;
        m_windowY = y;
    }
    
    /**
     * Returns true IFF this is an initial state of the machine.
     */
    public boolean isStartState()
    {
        return m_startState;
    }
    
    /**
     * Change whether this state is a start state or not.
     */
    public void setStartState(boolean value)
    {
        m_startState = value;
    }
    
    /**
     * Returns true IFF this is a final (accepting) state of the machine.
     */
    public boolean isFinalState()
    {
        return m_finalState;
    }
    
    /**
     * Change whether this state is an accepting state or not.
     */
    public void setFinalState(boolean value)
    {
        m_finalState = value;
    }
    
    /**
     * Get the outgoing transitions of this state.
     */
    public ArrayList<TM_Transition> getTransitions()
    {
        return m_transitions;
    }
    
    /**
     * Adds an outgoing transition from this state.  It is best to use the TMachine object's methods
     * to modify the machine, instead of calling this directly.
     */
    public void addTransition(TM_Transition tr)
    {
        m_transitions.add(tr);
    }
    
    /**
     * Removes an outgoing transition from this state.  It is best to use the TMachine object's
     * methods to modify the machine, instead of calling this directly.
     */
    public void removeTransition(TM_Transition tr)
    {
        m_transitions.remove(tr);
    }

    /**
     * Removes all outgoing transition from this state.  Does not update the machine itself, so
     * generally this is only useful to remove any transitions before adding a copy of a state to
     * the machine.
     */
    public void removeAllTransitions()
    {
        m_transitions.clear();
    }
    
    /** 
     * Gets this state's label.
     */
    public String getLabel()
    {
        return m_label;
    }
    
    /**
     * Sets the label for this state.
     */
    public void setLabel(String name)
    {
        m_label = name;
    }
    
    /**
     * Returns true IFF this state is actually a submachine.
     */
    public boolean isSubmachine()
    {
        return (m_subMachine != null);
    }
    
    /**
     * If this state is a submachine, gets the submachine, otherwise returns null.
     */
    public TMachine getSubMachine()
    {
        return m_subMachine;
    }
    
    // Graphics-related methods:    
    /**
     * Get the spatial x-coordinate of the object on the viewing plane.
     */
    public int getX()
    {
        return m_windowX;
    }
    
    /**
     * Get the spatial y-coordinate of the object on the viewing plane.
     */
    public int getY()
    {
        return m_windowY;
    }
    
    /**
     * Move the graphical representation of the state to position x, y on the view plane.
     */
    public void setPosition(int x, int  y)
    {
        m_windowX = x;
        m_windowY = y;
    }
    
    /**
     * Render the state to a graphics object.
     */
    public void paint(Graphics g, Collection<TM_State> selectedStates)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        GradientPaint redtowhite = new GradientPaint(m_windowX,m_windowY,Color.RED,m_windowX + STATE_RENDERING_WIDTH, m_windowY + STATE_RENDERING_WIDTH,Color.WHITE);
        g2d.setPaint(redtowhite);
        Ellipse2D.Float stateCircle = new Ellipse2D.Float(m_windowX, m_windowY, STATE_RENDERING_WIDTH, STATE_RENDERING_WIDTH);
        g2d.fill(stateCircle);
        
        if (selectedStates.contains(this))
        {
            g2d.setColor(Color.RED);
        }
        else
        {
            g2d.setColor(Color.BLACK);
        }
        g2d.draw(stateCircle);
        
        if (isFinalState())
        {
            g2d.draw(new Ellipse2D.Float(m_windowX + 5, m_windowY + 5, STATE_RENDERING_WIDTH - 10, STATE_RENDERING_WIDTH - 10));
        }
        
        if (isStartState())
        {
            g2d.draw(new Line2D.Float(m_windowX - STATE_RENDERING_WIDTH/2, m_windowY + STATE_RENDERING_WIDTH/2, m_windowX, m_windowY + STATE_RENDERING_WIDTH/2));
            g2d.draw(new Line2D.Float(m_windowX - STATE_RENDERING_WIDTH/4, m_windowY + STATE_RENDERING_WIDTH/4, m_windowX, m_windowY + STATE_RENDERING_WIDTH/2));
            g2d.draw(new Line2D.Float(m_windowX - STATE_RENDERING_WIDTH/4, m_windowY + STATE_RENDERING_WIDTH*3/4, m_windowX, m_windowY + STATE_RENDERING_WIDTH/2));
            // Tail
            g2d.draw(new QuadCurve2D.Float(m_windowX - STATE_RENDERING_WIDTH/2, m_windowY +
                        STATE_RENDERING_WIDTH/2, m_windowX - STATE_RENDERING_WIDTH*3/4, m_windowY + STATE_RENDERING_WIDTH/2,
                        m_windowX - STATE_RENDERING_WIDTH*2/3, m_windowY + STATE_RENDERING_WIDTH/3));
        }
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textTranslationX = -metrics.stringWidth(m_label) / 2;
        
        g2d.drawString(m_label, m_windowX + STATE_RENDERING_WIDTH/2 + textTranslationX, m_windowY + STATE_RENDERING_WIDTH + TEXT_DISTANCE);
    }
    
    /**
     * Returns true IFF view plane coordinates x,y are within circle that graphically represents
     * this state.
     */
    public boolean containsPoint(int x, int y)
    {
        return new Ellipse2D.Float(m_windowX, m_windowY, STATE_RENDERING_WIDTH, STATE_RENDERING_WIDTH).contains(x,y);
    }
    
    public boolean nameContainsPoint(Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textTranslationX = -metrics.stringWidth(m_label) / 2;
        
        Rectangle2D boundingbox = metrics.getStringBounds(m_label,g);
        
        // Translate x and y to bounding box coordinate system.
        x -= (m_windowX + STATE_RENDERING_WIDTH/2 + textTranslationX);
        y -= (m_windowY + STATE_RENDERING_WIDTH + TEXT_DISTANCE);
        
        return boundingbox.contains(x, y);
    }
        
    protected String m_label;
    protected ArrayList<TM_Transition> m_transitions;
    protected boolean m_startState;
    protected boolean m_finalState;
    protected TMachine m_subMachine;
    
    // Graphical location fields
    protected int m_windowX = 0;
    protected int m_windowY = 0;
}
