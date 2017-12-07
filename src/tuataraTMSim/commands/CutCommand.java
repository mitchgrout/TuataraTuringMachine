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

import java.util.HashSet;
import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.State;
import tuataraTMSim.machine.Transition;

/**
 * A command which deals with cutting states and transitions from a machine.
 * @author Jimmy
 */
public class CutCommand implements TMCommand
{
    /** 
     * Creates a new instance of CutCommand.
     * @param panel The current graphics panel.
     * @param selectedStates A copy of the set of states to cut.
     * @param selectedTransitions A copy of the set of transitions to cut.
     */
    public CutCommand(MachineGraphicsPanel panel, HashSet<? extends State> selectedStates,
                      HashSet<? extends Transition> selectedTransitions)
    {
        m_panel = panel;
        m_selectedStates = selectedStates;
        m_selectedTransitions = selectedTransitions;
        m_borderTransitions = 
            m_panel.getSimulator().getMachine().getHalfSelectedTransitions(selectedStates);
    }

    /**
     * Cut the selected states, transitions, and half-selected transitions from the machine.
     */
    public void doCommand()
    {
        for (State s : m_selectedStates)
        {
            CommandUtils.deleteState(m_panel, s);
        }
        
        for (Transition t : m_selectedTransitions)
        {
            CommandUtils.deleteTransition(m_panel, t);
        }
        
        m_panel.setSelectedStates(new HashSet<State>()); // TODO: is this what should happen?
        m_panel.setSelectedTransitions(new HashSet<Transition>());
    }
    
    /**
     * Restore the cut selected states, transitions, and half-selected transitions to the machine.
     */
    public void undoCommand()
    {
        // TODO: decide if they should come back selected or not
        
        for (State s : m_selectedStates)
        {
            m_panel.getSimulator().getMachine().addState(s);
            m_panel.addLabelToDictionary(s.getLabel());
        }
        
        for (Transition t : m_selectedTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
            // if (m_panel.getSelectedStates().contains(m_transition.getFromState()) &&
            //     m_panel.getSelectedStates().contains(m_transition.getToState()))
            // {
            //     m_panel.getSelectedTransitions().add(m_transition);
            // }
        }
        
        // Handle transitions that attach to items outside of the state cloud
        for (Transition t : m_borderTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
            // if (m_panel.getSelectedStates().contains(m_transition.getFromState()) &&
            //     m_panel.getSelectedStates().contains(m_transition.getToState()))
            // {
            //     m_panel.getSelectedTransitions().add(m_transition);
            // }
        }
    }
   
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Cut";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The set of states to cut.
     */
    private HashSet<? extends State> m_selectedStates;

    /**
     * The set of transitions to cut.
     */
    private HashSet<? extends Transition> m_selectedTransitions;
    
    /**
     * The set of half-selected transitions to cut.
     */
    private HashSet<? extends Transition> m_borderTransitions;
}
