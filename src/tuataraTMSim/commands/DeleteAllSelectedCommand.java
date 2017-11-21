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
import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_State;
import tuataraTMSim.TM.TM_Transition;

/**
 * A command which deals with deleting all selected states, and related transitions, from a machine.
 * @author Jimmy
 */
public class DeleteAllSelectedCommand implements TMCommand
{
    /** 
     * Creates a new instance of DeleteAllSelectedCommand.
     * @param panel The current graphics panel.
     * @param selectedStates A copy of the states to delete.
     * @param selectedTransitions A copy of the transitions to delete.
     */
    public DeleteAllSelectedCommand(TMGraphicsPanel panel, HashSet<TM_State> selectedStates,
                                    HashSet<TM_Transition> selectedTransitions)
    {
        m_panel = panel;
        m_selectedStates = selectedStates;
        m_selectedTransitions = selectedTransitions;
        m_borderTransitions = 
            m_panel.getSimulator().getMachine().getHalfSelectedTransitions(selectedStates);
    }
   
    /**
     * Delete all the selected states, transitions, and half-selected transitions from the machine.
     */
    public void doCommand()
    {
        for (TM_State s : m_selectedStates)
        {
            deleteState(s);
        }
        
        for (TM_Transition t : m_selectedTransitions)
        {
            deleteTransition(t);
        }
        
        m_panel.setSelectedStates(new HashSet<TM_State>()); // TODO: is this what should happen?
        m_panel.setSelectedTransitions(new HashSet<TM_Transition>());
    }
    
    /**
     * Restore the deleted states, transitions, and half-selected transitions to the machine.
     */
    public void undoCommand()
    {
        // TODO: decide if they should come back selected or not
        
        for (TM_State s : m_selectedStates)
        {
            m_panel.getSimulator().getMachine().addState(s);
            m_panel.addLabelToDictionary(s.getLabel());
        }
        
        for (TM_Transition t : m_selectedTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
            // if (m_panel.getSelectedStates().contains(m_transition.getFromState()) &&
            //     m_panel.getSelectedStates().contains(m_transition.getToState()))
            // {
            //     m_panel.getSelectedTransitions().add(m_transition);
            // }
        }
        
        //handle transitions that attach to items outside of the state cloud
        for (TM_Transition t : m_borderTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
            // if (m_panel.getSelectedStates().contains(m_transition.getFromState()) &&
            //     m_panel.getSelectedStates().contains(m_transition.getToState()))
            // {
            //     m_panel.getSelectedTransitions().add(m_transition);
            // }
        }
        
        m_panel.getSimulator().computeNextTransition();
    }
    
    /**
     *  Get the friendly name of this command.
     *  @return The friendly name of this command.
     */
    public String getName()
    {
        return "Delete Selected Items";
    }
    
    /**
     * A helper function which deletes a specified state from the machine.
     * @param s The state to delete.
     */
    private void deleteState(TM_State s)
    {
        m_panel.getSimulator().getMachine().deleteState(s);
        m_panel.removeLabelFromDictionary(s.getLabel());
        
        // Computation can't continue if we deleted the current state
        if (m_panel.getSimulator().getCurrentState() == s)
        {
            m_panel.getSimulator().resetMachine();
        }
        else
        {
            m_panel.getSimulator().computeNextTransition();
        }
    }
    
    /**
     * A helper function which deletes a specified transition from the machine.
     * @param t The transition to delete.
     */
    private void deleteTransition(TM_Transition t)
    {
        m_panel.getSimulator().getMachine().deleteTransition(t);
        if (t == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
        m_panel.getSimulator().computeNextTransition();
    }
    
    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;
    
    /**
     * The set of states to delete.
     */
    private HashSet<TM_State> m_selectedStates;
    
    /**
     * The set of transitions to delete.
     */
    private HashSet<TM_Transition> m_selectedTransitions;

    /**
     * The set of half-selected transitions to delete.
     */
    private HashSet<TM_Transition> m_borderTransitions;
}
