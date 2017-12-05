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
import tuataraTMSim.machine.Transition;

/**
 * A collection of methods which may be used by internally by various commands. 
 * Not visible outside this package. 
 */
abstract class CommandUtils
{
    /**
     * Delete a specified state and related transitions from the machine, and update the simulator.
     * @param panel The current graphics panel.
     * @param s The state to remove.
     */
    public static void deleteState(MachineGraphicsPanel panel, State s)
    {
        panel.getSimulator().getMachine().deleteState(s);
        panel.removeLabelFromDictionary(s.getLabel());

        // Computation can't continue if we deleted the current state
        if(panel.getSimulator().getCurrentState() == s)
        {
            panel.getSimulator().resetMachine();
        }
    }

    /**
     * Delete a specified transition from the machine, and update the simulator.
     * @param panel The current graphics panel.
     * @param t The transition to remove.
     */
    public static void deleteTransition(MachineGraphicsPanel panel, Transition t)
    {
        panel.getSimulator().getMachine().deleteTransition(t);
        if(panel.getSelectedTransition() == t)
        {
            panel.deselectSymbol();
        }
    }
}
