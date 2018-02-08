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
import tuataraTMSim.machine.Transition;

/**
 * A command which deals with deleting a transition from a machine.
 */
public class DeleteTransitionCommand implements TMCommand
{    
    /**
     * Creates a new instance of AddTransitionCommand.
     * @param panel The current graphics panel.
     * @param transition The transition to delete.
     */
    public DeleteTransitionCommand(MachineGraphicsPanel panel, Transition transition)
    {
        m_panel = panel;
        m_transition = transition;
    }
    
    /**
     * Delete the transition from the machine, and compute the next transition to be taken by the
     * machine.
     */
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().deleteTransition(m_transition);
        if (m_transition == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
    }
    
    /**
     * Add the transition back to the machine, and compute the next transition to be taken by the
     * machine.
     */
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().addTransition(m_transition);
        // if (selectedStates.contains(mousePressedState) && selectedStates.contains(mouseReleasedState))
        //     selectedTransitions.add(t);
    }
   
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Delete Transition";
    }
       
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * The transition to delete.
     */
    private Transition m_transition;
}
