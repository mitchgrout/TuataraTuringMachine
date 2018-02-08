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
import java.util.HashSet;
import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.Transition;

/**
 * A command which deals with a set of transitions which are deemed inconsistent with the alphabet.
 */
public class RemoveInconsistentTransitionsCommand implements TMCommand
{
    /**
     * Creates a new instance of RemoveInconsistentTransitionsCommand.
     * @param panel The current graphics panel.
     * @param purge The set of transitions to remove.
     */
    public RemoveInconsistentTransitionsCommand(MachineGraphicsPanel panel, Collection<? extends Transition> purge)
    {
        m_panel = panel;
        m_purge = purge;
    }
    
    /**
     * Remove the inconsistent transitions from the machine.
     */
    public void doCommand()
    {
        for (Transition t : m_purge)
        {
            CommandUtils.deleteTransition(m_panel, t);
        }
    }
    
    /**
     * Restore the inconsistent transitions to the machine.
     */
    public void undoCommand()
    {
        for (Transition t : m_purge)
        {
            m_panel.getSimulator().getMachine().addTransition(t);
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Remove Inconsistent Transitions";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The set of transitions to remove.
     */
    private Collection<? extends Transition> m_purge;   
}
