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
import tuataraTMSim.NamingScheme;

/**
 * A command which deals with changing the naming scheme for a machine. 
 */
public class ChangeSchemeCommand implements TMCommand
{
    /**
     * Creates a new instance of ChangeSchemeCommand.
     * @param panel The current graphics panel.
     * @param scheme The new naming scheme.
     */
    public ChangeSchemeCommand(MachineGraphicsPanel panel, NamingScheme scheme) 
    {
        m_panel = panel;
        m_oldScheme = m_panel.getSimulator().getMachine().getNamingScheme();
        m_newScheme = scheme;
    }

    /**
     * Sets the naming scheme to the new naming scheme.
     */
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().setNamingScheme(m_newScheme);
    }

    /**
     * Sets the naming scheme to the old naming scheme.
     */
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().setNamingScheme(m_oldScheme);
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Change Naming Scheme";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The previous naming scheme.
     */
    private NamingScheme m_oldScheme;
    
    /**
     * The new naming scheme.
     */
    private NamingScheme m_newScheme;
}
