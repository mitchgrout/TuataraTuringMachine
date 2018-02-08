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
 * A command which deals with changing the start state of a machine.
 */
public class ToggleStartStateCommand implements TMCommand
{
    /**
     * Creates a new instance of ToggleStartCommand.
     * @param panel The current graphics panel.
     * @param state The state to toggle.
     */
    public ToggleStartStateCommand(MachineGraphicsPanel panel, State state)
    {
        m_panel = panel;
        m_state = state;
    }
    
    /**
     * Toggle whether or not the supplied state is accepting.
     */
    public void doCommand()
    {
        m_state.setStartState(!m_state.isStartState());
    }
    
    /**
     * Toggle whether or not the supplied state is accepting.
     */
    public void undoCommand()
    {
        m_state.setStartState(!m_state.isStartState());
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Toggle Start State";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * The state to toggle.
     */
    private State m_state;
}
