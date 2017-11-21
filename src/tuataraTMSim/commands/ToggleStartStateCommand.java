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
 * A command which deals with changing the start state of a machine.
 * @author Jimmy
 */
public class ToggleStartStateCommand implements TMCommand
{
    /**
     * Creates a new instance of ToggleStartStateCommand.
     * @param panel The current graphics panel.
     * @param oldState The previous start state.
     * @param newState The new accepting state. If newState and oldState are the same state, then we
     *                 toggle the value for the state.
     */
    public ToggleStartStateCommand(TMGraphicsPanel panel, TM_State oldState, TM_State newState)
    {
        m_panel = panel;
        m_oldState = oldState;
        m_newState = newState;
    }
   
    /**
     * If oldState and newState are different, then newState is made to be the start state.
     * If they are the same, then the value is toggled.
     */
    public void doCommand()
    {
        // If the old start is the new start, or there was no old start, we're simply toggling
        if(m_oldState == null || m_oldState.equals(m_newState))
        {
            m_newState.setStartState(!m_newState.isStartState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldState.setStartState(false);
            m_newState.setStartState(true);
        }
    }
    
    /**
     * If oldState and newState are different, thenoldState is made to be the start state.
     * If they are the same, then the value is toggled.
     */
    public void undoCommand()
    {
        // If the old start is the new start, or there was no old start, we're simply toggling
        if(m_oldState == null || m_oldState.equals(m_newState))
        {
            m_newState.setStartState(!m_newState.isStartState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldState.setStartState(true);
            m_newState.setStartState(false);
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Toggle Start State";
    }
   
    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;

    /**
     * The old start state.
     */
    private TM_State m_oldState;
    
    /**
     * The new start state.
     */
    private TM_State m_newState;
}
