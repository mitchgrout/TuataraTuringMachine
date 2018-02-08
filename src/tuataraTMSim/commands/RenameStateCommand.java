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

import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.State;

/**
 * A command which deals with changing the label of a state.
 */
public class RenameStateCommand implements TMCommand
{
    /**
     * Creates a new instance of RenameStateCommand.
     * @param panel The current graphics panel.
     * @param state The state to rename.
     * @param label The new label for the state.
     */
    public RenameStateCommand(MachineGraphicsPanel panel, State state, String label)
    {
        m_panel = panel;
        m_state = state;
        m_label = label;
        m_oldLabel = state.getLabel();
    }

    /**
     * Change the label of the state to the new label.
     */
    public void doCommand()
    {
        m_panel.removeLabelFromDictionary(m_oldLabel);
        m_panel.addLabelToDictionary(m_label);
        m_state.setLabel(m_label);
    }
    
    /**
     * Restore the label of the state to its previous value.
     */
    public void undoCommand()
    {
        m_panel.removeLabelFromDictionary(m_label);
        m_panel.addLabelToDictionary(m_oldLabel);
        m_state.setLabel(m_oldLabel);
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Rename State";
    }

    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The state to rename.
     */
    private State m_state;
    
    /**
     * The new label for m_state.
     */
    private String m_label;
    
    /**
     * The old label for m_state
     */
    private String m_oldLabel;
}
