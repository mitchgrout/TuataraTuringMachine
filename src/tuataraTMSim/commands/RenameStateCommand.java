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
 *
 * @author Jimmy
 */
public class RenameStateCommand implements TMCommand
{
    /**
     * Creates a new instance of RenameStateCommand
     */
    public RenameStateCommand(TMGraphicsPanel panel, TM_State state, String label)
    {
        m_panel = panel;
        m_state = state;
        m_label = label;
        m_oldLabel = state.getLabel();
    }

    public void doCommand()
    {
        m_panel.removeLabelFromDictionary(m_oldLabel);
        m_panel.addLabelToDictionary(m_label);
        m_state.setLabel(m_label);
    }
    
    public void undoCommand()
    {
        m_panel.removeLabelFromDictionary(m_label);
        m_panel.addLabelToDictionary(m_oldLabel);
        m_state.setLabel(m_oldLabel);
    }
    
    public String getName()
    {
        return "Rename State";
    }

    TMGraphicsPanel m_panel;
    TM_State m_state;
    String m_label;
    String m_oldLabel;
}
