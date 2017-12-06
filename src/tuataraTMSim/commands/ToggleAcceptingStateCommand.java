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

/**
 * A command which deals with changing the accepting state of a machine.
 * @author Jimmy
 */
public class ToggleAcceptingStateCommand implements TMCommand
{
    /**
     * Creates a new instance of ToggleAcceptingCommand.
     * @param panel The current graphics panel.
     * @param oldState The previous accepting state.
     * @param newState The new accepting state. If newState and oldState are the same state, then we
     *                 toggle the value for the state.
     */
    public ToggleAcceptingStateCommand(MachineGraphicsPanel panel, State oldState, State newState)
    {
        m_panel = panel;
        m_oldState = oldState;
        m_newState = newState;
    }
    
    /**
     * If oldState and newState are different, then newState is made to be the accepting state.
     * If they are the same, then the value is toggled.
     */
    public void doCommand()
    {
        // Changing the accepting state of a machine invalidates it.
        m_panel.getSimulator().getMachine().invalidate();

        // If the old start is the new start, or there was no old start, we're simply toggling
        if(m_oldState == null || m_oldState.equals(m_newState))
        {
            m_newState.setFinalState(!m_newState.isFinalState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldState.setFinalState(false);
            m_newState.setFinalState(true);
        }
    }
    
    /**
     * If oldState and newState are different, then oldState is made to be the accepting state.
     * If they are the same, then the value is toggled.
     */
    public void undoCommand()
    {
        // Changing the accepting state of a machine invalidates it.
        m_panel.getSimulator().getMachine().invalidate();

        // If the old start is the new start, or there was no old start, we're simply toggling
        if(m_oldState == null || m_oldState.equals(m_newState))
        {
            m_newState.setFinalState(!m_newState.isFinalState());
        }
        // Otherwise, we're unsetting one, and setting the other
        else
        {
            m_oldState.setFinalState(true);
            m_newState.setFinalState(false);
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Toggle Accepting State";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * The old accepting state.
     */
    private State m_oldState;
    
    /**
     * The new accepting state.
     */
    private State m_newState;
}
