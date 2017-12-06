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

import java.util.ArrayList;
import java.util.Collection;
import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.State;
import tuataraTMSim.machine.Transition;

/**
 * A comand which deals with moving a state.
 * @author Jimmy
 */
public class MoveStateCommand implements TMCommand
{
    /**
     * Creates a new instance of MoveStateCommand.
     * @param panel The current graphics panel.
     * @param state The state to move.
     * @param moveX The change in X position.
     * @param moveY The change in Y position.
     * @param transitionsInto The set of transitions coming into the state.
     */
    public MoveStateCommand(MachineGraphicsPanel panel, State state, int moveX, int moveY,
                            Collection<Transition> transitionsInto)
    {
        m_panel = panel;
        m_state = state;
        m_moveX = moveX;
        m_moveY = moveY;
        m_transitionsInto = transitionsInto;
        m_transitionsOut = new ArrayList<Transition>();
        m_transitionsOut.addAll(state.getTransitions());
    }

    /**
     * Move the state, incoming and outgoing transitions, by the specified amount.
     */
    public void doCommand()
    {
        MachineGraphicsPanel.updateTransitionLocations(m_state,m_moveX, m_moveY, m_transitionsInto, m_transitionsOut);
        m_state.setPosition(m_state.getX() + m_moveX, m_state.getY() + m_moveY);
    }
       
    /**
     * Move the state, and incoming and outgoing transitions, back by the specified amount.
     */
    public void undoCommand()
    {
        MachineGraphicsPanel.updateTransitionLocations(m_state,-m_moveX, -m_moveY, m_transitionsInto, m_transitionsOut);
        m_state.setPosition(m_state.getX() - m_moveX, m_state.getY() - m_moveY);
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Move State";
    }

    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The state to move.
     */
    private State m_state;
    
    /**
     * The change in X position.
     */
    private int m_moveX;
    
    /**
     * The change in Y position.
     */
    private int m_moveY;
    
    /**
     * The set of transitions coming into m_state.
     */
    private Collection<? extends Transition> m_transitionsInto;
    
    /**
     * The set of transitions leaving m_state.
     */
    private Collection<? extends Transition> m_transitionsOut;
}
