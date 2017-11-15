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
import tuataraTMSim.TM.Alphabet;

/**
 *
 * @author Jimmy
 */
public class ConfigureAlphabetCommand implements TMCommand
{
    /**
     * Creates a new instance of ConfigureAlphabetCommand 
     */
    public ConfigureAlphabetCommand(TMGraphicsPanel panel, Alphabet beforeAlphabet, Alphabet afterAlphabet)
    {
        m_panel = panel;
        m_beforeAlphabet = beforeAlphabet;
        m_afterAlphabet = afterAlphabet;
    }

    public void doCommand()
    {
        m_panel.getSimulator().getMachine().setAlphabet(m_afterAlphabet);
    }

    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().setAlphabet(m_beforeAlphabet);
    }
    
    public String getName()
    {
        return "Configure Alphabet";
    }
    
    private TMGraphicsPanel m_panel;
    private Alphabet m_beforeAlphabet;
    private Alphabet m_afterAlphabet;
    
}
