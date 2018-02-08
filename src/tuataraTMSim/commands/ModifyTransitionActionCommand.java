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
import tuataraTMSim.machine.PreAction;
import tuataraTMSim.machine.Transition;

/**
 * A command which deals with changing the action of a transition.
 */
public class ModifyTransitionActionCommand implements TMCommand
{
    /**
     * Creates a new instance of ModifyTransitionActionCommand.
     * @param panel The current graphics panel.
     * @param transition The transition to modify
     * @param action The new action for the transition.
     */
    public ModifyTransitionActionCommand(MachineGraphicsPanel panel, Transition transition,
                                         PreAction action)
    {
        m_panel = panel;
        m_transition = transition;
        m_action = action;
        m_oldAction = m_transition.getAction();
    }
    
    /**
     * Set the action of the transitoin to be the new action.
     */
    public void doCommand()
    {
        m_transition.setAction(m_action);
    }
    
    /**
     * Set the action of the transition to be its previous value.
     */
    public void undoCommand()
    {
        m_transition.setAction(m_oldAction);
    }
   
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Modify Transition Action";
    }

    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The transition to modify.
     */
    private Transition m_transition;
    
    /**
     * The new action for m_transition.
     */
    private PreAction m_action;
    
    /**
     * The old action for m_transition.
     */
    private PreAction m_oldAction;   
}
