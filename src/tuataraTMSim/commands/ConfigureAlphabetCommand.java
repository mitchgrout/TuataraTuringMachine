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

import tuataraTMSim.machine.Alphabet;
import tuataraTMSim.MachineGraphicsPanel;

/**
 * A command which deals with manipulating the tape alphabet.
 * @author Jimmy
 */
public class ConfigureAlphabetCommand implements TMCommand
{
    /**
     * Creates a new instance of ConfigureAlphabetCommand.
     * @param panel The current graphics panel.
     * @param beforeAlphabet The current tape alphabet.
     * @param afterAlphabet The new tape alphabet.
     */
    public ConfigureAlphabetCommand(MachineGraphicsPanel panel, Alphabet beforeAlphabet, Alphabet afterAlphabet)
    {
        m_panel = panel;
        m_beforeAlphabet = beforeAlphabet;
        m_afterAlphabet = afterAlphabet;
    }

    /**
     * Sets the current tape alphabet to the new alphabet.
     */
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().setAlphabet(m_afterAlphabet);
    }

    /**
     * Sets the current tape alphabet to the previous alphabet.
     */
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().setAlphabet(m_beforeAlphabet);
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Configure Alphabet";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The previous alphabet.
     */
    private Alphabet m_beforeAlphabet;
    
    /**
     * The new alphabet
     */
    private Alphabet m_afterAlphabet;
}
