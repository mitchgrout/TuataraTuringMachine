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
public class ToggleStartStateCommand implements TMCommand
{
    
    /** Creates a new instance of ToggleStartStateCommand */
    public ToggleStartStateCommand(TMGraphicsPanel panel, TM_State oldStart, TM_State newStart)
    {
        m_panel = panel;
        m_oldStart = oldStart;
        m_newStart = newStart;
    }
    
    
    public void doCommand()
    {
        // If the old start is the new start, or there was no old start,
        // we're simply toggling
        if(m_oldStart == null || m_oldStart.equals(m_newStart))
        {
            m_newStart.setStartState(!m_newStart.isStartState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldStart.setStartState(!m_oldStart.isStartState());
            m_newStart.setStartState(!m_newStart.isStartState());
        }
    }
    
    public void undoCommand()
    {
        // If the old start is the new start, or there was no old start,
        // we're simply toggling
        if(m_oldStart == null || m_oldStart.equals(m_newStart))
        {
            m_newStart.setStartState(!m_newStart.isStartState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldStart.setStartState(!m_oldStart.isStartState());
            m_newStart.setStartState(!m_newStart.isStartState());
        }
    }
    
    public String getName()
    {
        return "Toggle Start State";
    }
    
    private TMGraphicsPanel m_panel;
    private TM_State m_oldStart, m_newStart;
}
