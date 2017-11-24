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

import java.util.HashMap;
import tuataraTMSim.NamingScheme;
import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_State;

/**
 * A command which deals with changing the label of all states in a machine to conform to the naming
 * scheme chosen by the user.
 */
public class SchemeRelabelCommand implements TMCommand
{
    /**
     * Creates a new instance of SchemeRelabelCommand.
     * @param panel The current graphics panel.
     * @param state The state to rename.
     * @param label The new label for the state.
     */
    public SchemeRelabelCommand(TMGraphicsPanel panel, NamingScheme scheme)
    {
        m_panel  = panel;
        m_scheme = scheme;

        // Keep track of all old labels
        m_oldLabels = new HashMap<TM_State, String>();
        for (TM_State s : m_panel.getSimulator().getMachine().getStates())
        {
            m_oldLabels.put(s, s.getLabel());
        }
    }

    /**
     * Change the label of all the states to the schemes value.
     */
    public void doCommand()
    {
        // TODO: GENERALIZE
        int counter = 0;
        for (TM_State s : m_panel.getSimulator().getMachine().getStates())
        {
            s.setLabel((m_scheme == NamingScheme.NORMALIZED? "" : "q") + counter);
            counter++;
        }
    }
    
    /**
     * Restore the labels of all the states.
     */
    public void undoCommand()
    {
        for (TM_State s : m_panel.getSimulator().getMachine().getStates())
        {
            s.setLabel(m_oldLabels.get(s));
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Rename All States";
    }

    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;
   
    /**
     * The naming scheme for states.
     */
    private NamingScheme m_scheme;

    /**
     * A mapping between states and their old labels.
     */
    private HashMap<TM_State, String> m_oldLabels;
}
