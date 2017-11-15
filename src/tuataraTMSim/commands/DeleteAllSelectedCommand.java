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
 *
 * @author Jimmy
 */
public class DeleteAllSelectedCommand implements TMCommand
{
    /** 
     * Creates a new instance of DeleteAllSelectedCommand.
     * Be sure to only pass in copies of selectedStates and selectedTransitions
     * - we don't want our lists of states and transitions to change when the user copies new things!
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
        
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public String getName()
    {
        return "Delete Selected Items";
    }
    
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
            m_panel.getSimulator().computePotentialTransitions(false);
        }
    }
    
    private void deleteTransition(TM_Transition t)
    {
        m_panel.getSimulator().getMachine().deleteTransition(t);
        if (t == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    private TMGraphicsPanel m_panel;
    private HashSet<TM_State> m_selectedStates;
    private HashSet<TM_Transition> m_selectedTransitions;
    private HashSet<TM_Transition> m_borderTransitions;
}
