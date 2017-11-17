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

package tuataraTMSim.commands;

import java.util.ArrayList;
import java.util.Collection;
import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_State;
import tuataraTMSim.TM.TM_Transition;

/**
 * A command which deals with deleting a state from a machine.
 * @author Jimmy
 */
public class DeleteStateCommand implements TMCommand
{
    /**
     * Creates a new instance of DeleteStateCommand.
     * @param panel The current graphics panel.
     * @param state The state to delete.
     */
    public DeleteStateCommand(TMGraphicsPanel panel, TM_State state)
    {
        m_panel = panel;
        m_state = state;
        m_outTransitions.addAll(state.getTransitions());
        m_inTransitions = m_panel.getSimulator().getMachine().getTransitionsTo(state);
    }
    
    /**
     * Delete the state from the machine, deleting associated transitions, and compute the next
     * transition to be taken by the machine.
     */
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().deleteState(m_state);
        m_panel.removeLabelFromDictionary(m_state.getLabel());

        // Computation can't continue if we deleted the current state
        if (m_panel.getSimulator().getCurrentState() == m_state)
        {
            m_panel.getSimulator().resetMachine();
        }
        else
        {
            m_panel.getSimulator().computeNextTransition();
        }
    }
    
    /**
     * Add the state back to the machine, restoring deleted transitions.
     */
    public void undoCommand()
    {
        // TODO: Should this also cause a call to computeNextTransition?

        m_panel.getSimulator().getMachine().addState(m_state);
        m_panel.addLabelToDictionary(m_state.getLabel());
        for (TM_Transition t : m_outTransitions)
            m_panel.getSimulator().getMachine().addTransition(t);
        
        for (TM_Transition t : m_inTransitions)
            m_panel.getSimulator().getMachine().addTransition(t);
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Delete State";
    }
    
    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;
    
    /**
     * The state to delete.
     */
    private TM_State m_state;
    
    /**
     * The outgoing transitions associated with m_state.
     */
    private Collection<TM_Transition> m_outTransitions = new ArrayList<TM_Transition>();
    
    /**
     * The incoming transitions associated with m_state.
     */
    private Collection<TM_Transition> m_inTransitions;
}
