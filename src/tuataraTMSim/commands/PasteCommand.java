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
 * A command which deals with pasting states and transitions into a machine.
 * @author Jimmy
 */
public class PasteCommand implements TMCommand
{
    /**
     * Creates a new instance of PasteCommand.
     * @param panel The current graphics panel.
     * @param selectedStates A copy of the set of states to paste.
     * @param selectedTransitions A copy of the set of transitions to paste.
     */
    public PasteCommand(MachineGraphicsPanel panel, HashSet<? extends State> selectedStates,
                        HashSet<? extends Transition> selectedTransitions)
    {
        m_panel = panel;
        m_selectedStates = selectedStates;
        m_selectedTransitions = selectedTransitions;
    }
   
    /**
     * Paste the selected states and transitions into the machine.
     */
    public void doCommand()
    {
        for (State s : m_selectedStates)
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

        for (Transition t : m_selectedTransitions)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
        }

        m_panel.setSelectedStates((HashSet<State>)m_selectedStates.clone());
        m_panel.setSelectedTransitions((HashSet<Transition>)m_selectedTransitions.clone());
    }
    
    /**
     * Delete the pasted states and transitions from the machine.
     */
    public void undoCommand()
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
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Paste";
    }
    
    /**
     * The current graphics panel
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The set of states to paste.
     */
    private HashSet<? extends State> m_selectedStates;

    /**
     * The set of transitions to paste.
     */
    private HashSet<? extends Transition> m_selectedTransitions;
}
