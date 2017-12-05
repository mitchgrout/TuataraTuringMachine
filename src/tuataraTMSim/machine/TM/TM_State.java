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
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.machine.*;

/**
 * Represents a state in a Turing machine.
 * @author Jimmy
 */
public class TM_State extends State<TM_Action, TM_Transition, TMachine, TM_Simulator>
    implements Serializable
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
     * Creates a new instance of TM_State, with a specified location.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is the final state.
     * @param x The X ordinate of the state.
     * @param y The Y ordinate of the state.
     */
    public TM_State(String label, boolean startState, boolean finalState, int x, int y)
    {
        super(label, startState, finalState, x, y);
        m_transitions = new ArrayList<TM_Transition>();
    }
    
    /**
     * Creates a new instance of TM_State.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is the final state.
     */
    public TM_State(String label, boolean startState, boolean finalState)
    {
        super(label, startState, finalState, 0, 0);
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
     * The list of transitions leaving this state.
     */
    protected ArrayList<TM_Transition> m_transitions;
   
    // /**
    //  * The submachine associated with this state.
    //  */
    // protected TMachine m_subMachine;
}
