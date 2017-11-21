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

import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_State;

/**
 * A command which deals with adding new states to a machine.
 * @author Jimmy
 */
public class AddStateCommand implements TMCommand
{
    /** 
     * Creates a new instance of AddStateCommand.
     * @param panel The current graphics panel.
     * @param state The state to add.
     */
    public AddStateCommand(TMGraphicsPanel panel, TM_State state)
    {
        m_panel = panel;
        m_state = state;
    }
    
    /**
     * Adds the state to the machine.
     */
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().addState(m_state);
        m_panel.addLabelToDictionary(m_state.getLabel());
    }
    
    /**
     * Removes the state from the machine; if this state is the current state of the computation,
     * the computation is cancelled.
     */
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().deleteState(m_state);
        m_panel.removeLabelFromDictionary(m_state.getLabel());

        if (m_panel.getSimulator().getCurrentState() == m_state)
        {
            // Computation can't continue if we deleted the current state
            m_panel.getSimulator().resetMachine(); 
        }
        else
        {
            m_panel.getSimulator().computeNextTransition();
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Add State";
    }
    
    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;

    /**
     * The state to add or remove.
     */
    private TM_State m_state;
}
