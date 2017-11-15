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
public class PasteCommand implements TMCommand
{
    /**
     * Creates a new instance of PasteCommand.
     * Be sure to only pass in copies of selectedStates and selectedTransitions
     * - we don't want our lists of states and transitions to change when the user copies new things!
     */
    public PasteCommand(TMGraphicsPanel panel, HashSet<TM_State> selectedStates,
                        HashSet<TM_Transition> selectedTransitions)
    {
        m_panel = panel;
        m_selectedStates = selectedStates;
        m_selectedTransitions = selectedTransitions;
    }
    
    public void doCommand()
    {
        for (TM_State s : m_selectedStates)
        {
            if (m_panel.dictionaryContainsName(s.getLabel()))
            {
                String label = m_panel.getFirstFreeName();
                s.setLabel(label);
            }
            m_panel.addLabelToDictionary(s.getLabel());
            s.removeAllTransitions();
            m_panel.getSimulator().getMachine().addState(s);
        }

        for (TM_Transition t : m_selectedTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
        }

        m_panel.setSelectedStates((HashSet<TM_State>)m_selectedStates.clone());
        m_panel.setSelectedTransitions((HashSet<TM_Transition>)m_selectedTransitions.clone());
    }
    
    public void undoCommand()
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
    
    public String getName()
    {
        return "Paste";
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
    private HashSet<TM_Transition>  m_selectedTransitions;
}
