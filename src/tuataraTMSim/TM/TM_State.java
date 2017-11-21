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
    /**
     * Width of the graphical representation of the state, in pixels.
     */
    public static final int STATE_RENDERING_WIDTH = 30;
    
    /**
     * Distance of graphical text below picture of the state.
     */
    public static final int TEXT_DISTANCE = 15; 
    
    /**
     * Creates a new instance of TM_State.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is the final state.
     */
    public TM_State(String label, boolean startState, boolean finalState)
    {
        m_label = label;
        m_startState = startState;
        m_finalState = finalState;
        m_transitions = new ArrayList<TM_Transition>();
    }
    
    /**
     * Creates a new instance of TM_State, with a specified location.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is the final state.
     * @param x The X ordinate of the state.
     * @param y The Y ordinate of the state.
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
     * Determine if this state is the start state.
     * @return true if this is the start state, false otherwise.
     */
    public boolean isStartState()
    {
        return m_startState;
    }
    
    /**
     * Change whether this state is a start state or not.
     * @param value true if this state should be the start state, false otherwise.
     */
    public void setStartState(boolean value)
    {
        m_startState = value;
    }
    
    /**
     * Determine if this state is the final state.
     * @return true if this is the final state, false otherwise.
     */
    public boolean isFinalState()
    {
        return m_finalState;
    }
    
    /**
     * Change whether this state is an accepting state or not.
     * @param value true if this state should be the final state, false otherwise.
     */
    public void setFinalState(boolean value)
    {
        m_finalState = value;
    }
    
    /**
     * Get the outgoing transitions of this state.
     * @return An array list of all transitions which leave this state.
     */
    public ArrayList<TM_Transition> getTransitions()
    {
        return m_transitions;
    }
    
    /**
     * Adds an outgoing transition from this state. It is suggested to use the TMachine object's
     * methods to modify the machine, instead of calling this directly.
     * @param tr The transition to add to the state.
     */
    public void addTransition(TM_Transition tr)
    {
        m_transitions.add(tr);
    }
    
    /**
     * Removes an outgoing transition from this state. It is suggested to use the TMachine object's
     * methods to modify the machine, instead of calling this directly.
     * @param tr The transition to remove from the state.
     */
    public void removeTransition(TM_Transition tr)
    {
        m_transitions.remove(tr);
    }

    /**
     * Removes all outgoing transition from this state. Does not update the machine itself, so
     * generally this is only useful to remove any transitions before adding a copy of a state to
     * the machine.
     */
    public void removeAllTransitions()
    {
        m_transitions.clear();
    }
    
    /** 
     * Gets this state's label.
     * @return This state's label.
     */
    public String getLabel()
    {
        return m_label;
    }
    
    /**
     * Sets this state's label.
     * @param name The new label.
     */
    public void setLabel(String name)
    {
        m_label = name;
    }
    
    /**
     * Determine if this state represents a submachine.
     * @return true if this state is a submachine, false otherwise.
     */
    public boolean isSubmachine()
    {
        return (m_subMachine != null);
    }
    
    /**
     * Get the submachine associated with this state.
     * @return The submachine associated with this state, if this state is a submachine, otherwise
     *         null.
     */
    public TMachine getSubMachine()
    {
        return m_subMachine;
    }
    
    // Graphics-related methods:    
    /**
     * Get the spatial X ordinate of the object on the viewing plane.
     * @return The X ordinate of this object on the viewing plane.
     */
    public int getX()
    {
        return m_windowX;
    }
    
    /**
     * Get the spatial Y ordinate of the object on the viewing plane.
     * @return The Y ordinate of this object on the viewing plane.
     */
    public int getY()
    {
        return m_windowY;
    }
    
    /**
     * Move the graphical representation of the state to (x, y) on the viewing plane.
     * @param x The new X ordinate.
     * @param y The new Y ordinate.
     */
    public void setPosition(int x, int  y)
    {
        m_windowX = x;
        m_windowY = y;
    }
    
    /**
     * Render the state to a graphics object.
     * @param g The graphics object to render onto.
     * @param selectedStates The set of states selected by the user.
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
     * Determine if this state contains the point (x, y)
     * @param x The X ordinate.
     * @param y The Y ordinate.
     * @return true if this tate contains the specified point, false otherwise.
     */
    public boolean containsPoint(int x, int y)
    {
        return new Ellipse2D.Float(m_windowX, m_windowY, STATE_RENDERING_WIDTH, STATE_RENDERING_WIDTH).contains(x,y);
    }
   
    /**
     * Determine if this state's label contains the point (x, y)
     * @param g The graphics object, used to measure the label's dimensions.
     * @param x The X ordinate.
     * @param y The Y ordinate.
     * @return true if this state's label contains the point (x, y), false otherwise.
     */
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
       
    /**
     * The label for this state.
     */
    protected String m_label;
   
    /**
     * The list of transitions leaving this state.
     */
    protected ArrayList<TM_Transition> m_transitions;
   
    /**
     * Whether or not this state is a start state.
     */
    protected boolean m_startState;
    
    /**
     * Whether or not this state is a final state.
     */
    protected boolean m_finalState;
    
    /**
     * The submachine associated with this state.
     */
    protected TMachine m_subMachine;
    
    /**
     * The X ordinate of this state, relative to the window.
     */
    protected int m_windowX = 0;
    
    /**
     * The Y ordinate of this state, relative to the window.
     */
    protected int m_windowY = 0;
}
