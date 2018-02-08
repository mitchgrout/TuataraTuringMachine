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

import java.util.Collection;
import java.util.HashMap;
import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.State;

/**
 * A command which resets the labels of all states in the machine.
 * @author Jimmy
 */
public class ResetLabelCommand implements TMCommand
{
    /**
     * Creates a new instance of ResetLabelCommand.
     * @param panel The current graphics panel.
     */
    public ResetLabelCommand(MachineGraphicsPanel panel)
    {
        m_panel = panel;
        m_oldLabels = new HashMap<State, String>();
        m_newLabels = new HashMap<State, String>();

        // Set up the mappings for new/old labels.
        // NOTE: states cannot be moved directly into the loop as the compiler complains about type errors
        int counter = 0;
        Collection<State> states = m_panel.getSimulator().getMachine().getStates();
        for (State s : states)
        {
            m_oldLabels.put(s, s.getLabel());
            m_newLabels.put(s, String.format("q%d", counter++));
        }
    }

    /**
     * Change the label of the state to the new label.
     */
    public void doCommand()
    {
        // Assign the new labels
        // NOTE: states cannot be moved directly into the loop as the compiler complains about type errors
        Collection<State> states = m_panel.getSimulator().getMachine().getStates();
        for (State s : states) 
        {
            s.setLabel(m_newLabels.get(s));
        }
    }
    
    /**
     * Restore the label of the state to its previous value.
     */
    public void undoCommand()
    {
        // Assign the old labels
        // NOTE: states cannot be moved directly into the loop as the compiler complains about type errors
        Collection<State> states = m_panel.getSimulator().getMachine().getStates();
        for (State s : states)
        {
            s.setLabel(m_oldLabels.get(s));
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Reset State Labels";
    }

    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * A mapping of states to their old labels.
     */
    private HashMap<State, String> m_oldLabels;

    /**
     * A mapping of states to their new labels.
     */
    private HashMap<State, String> m_newLabels;
}
