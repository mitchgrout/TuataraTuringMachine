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
import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_State;
import tuataraTMSim.TM.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class MoveStateCommand implements TMCommand
{
    
    /** Creates a new instance of MoveStateCommand */
    public MoveStateCommand(TMGraphicsPanel panel, TM_State state,
            int moveX, int moveY, Collection<TM_Transition> transitionsInto)
    {
        m_panel = panel;
        m_state = state;
        m_moveX = moveX;
        m_moveY = moveY;
        m_transitionsInto = transitionsInto;
        m_transitionsOut = new ArrayList<TM_Transition>();
        m_transitionsOut.addAll(state.getTransitions());
    }

    public void doCommand()
    {
        TMGraphicsPanel.updateTransitionLocations(m_state,m_moveX, m_moveY,
                    m_transitionsInto, m_transitionsOut);
        m_state.setPosition(m_state.getX() + m_moveX,
                    m_state.getY() + m_moveY);
    }
       
    public void undoCommand()
    {
        TMGraphicsPanel.updateTransitionLocations(m_state,-m_moveX, -m_moveY,
                    m_transitionsInto, m_transitionsOut);
        m_state.setPosition(m_state.getX() - m_moveX,
                    m_state.getY() - m_moveY);
    }
    
    public String getName()
    {
        return "Move State";
    }

    TMGraphicsPanel m_panel;
    TM_State m_state;
    int m_moveX;
    int m_moveY;
    Collection<TM_Transition> m_transitionsInto;
    Collection<TM_Transition> m_transitionsOut;
}
