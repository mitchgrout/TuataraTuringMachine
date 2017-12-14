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
import java.io.Serializable;
import java.util.ArrayList;
import tuataraTMSim.machine.*;

/**
 * Represents a state in a machine.
 */
public class DFSA_State extends State<DFSA_Action, DFSA_Transition, DFSA_Machine, DFSA_Simulator>
{
    /**
     * Creates a new instance of DFSA_State, with a specified location.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is a final state.
     * @param windowX The X ordinate of the state.
     * @param windowY The Y ordinate of the state.
     */
    public DFSA_State(String label, boolean startState, boolean finalState, int windowX, int windowY)
    {
        super(label, startState, finalState, windowX, windowY);
        m_transitions = new ArrayList<DFSA_Transition>();
    }

    /**
     * Creates a new instance of DFSA_State.
     * @param label The label for the state.
     * @param startState Whether or not this state is the start state.
     * @param finalState Whether or not this state is a final state.
     */
    public DFSA_State(String label, boolean startState, boolean finalState)
    {
        this(label, startState, finalState, 0, 0);
    }
 
    /**
     * Get the outgoing transitions of this state.
     * @return An array list of all transitions which leave this state.
     */
    public ArrayList<DFSA_Transition> getTransitions()
    {
        return m_transitions;
    }

    /**
     * Adds an outgoing transition from this state. It is suggested to use the Machine object's
     * methods to modify the machine, instead of calling this directly.
     * @param tr The transition to add to the state.
     */
    public void addTransition(DFSA_Transition tr)
    {
        m_transitions.add(tr);
    }

    /**
     * Removes an outgoing transition from this state. It is suggested to use the Machine object's
     * methods to modify the machine, instead of calling this directly.
     * @param tr The transition to remove from the state.
     */
    public void removeTransition(DFSA_Transition tr)
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
    protected ArrayList<DFSA_Transition> m_transitions;
}
