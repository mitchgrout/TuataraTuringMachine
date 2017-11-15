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

import java.awt.geom.Point2D;
import tuataraTMSim.TMGraphicsPanel;
import tuataraTMSim.TM.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class MoveTransitionCommand  implements TMCommand
{
    /**
     * Creates a new instance of MoveTransitionCommand
     */
    public MoveTransitionCommand(TMGraphicsPanel panel, TM_Transition transition, int moveX, int moveY)
    {
        m_panel = panel;
        m_transition = transition;
        m_moveX = moveX;
        m_moveY = moveY;
    }
    
    public void doCommand()
    {
        Point2D midPoint = m_transition.getMidpoint();
        Point2D newCP = TM_Transition.getControlPointGivenMidpoint(
                new Point2D.Double(midPoint.getX() + m_moveX, midPoint.getY() + m_moveY),
                m_transition.getFromState(), m_transition.getToState());
        
        m_transition.setControlPoint((int)newCP.getX(), (int)newCP.getY());
    }
    
    public void undoCommand()
    {
        Point2D midPoint = m_transition.getMidpoint();
        Point2D newCP = TM_Transition.getControlPointGivenMidpoint(
                new Point2D.Double(midPoint.getX() - m_moveX, midPoint.getY() - m_moveY),
                m_transition.getFromState(), m_transition.getToState());
        
        m_transition.setControlPoint((int)newCP.getX(), (int)newCP.getY());
    }

    public String getName()
    {
        return "Move Transition";
    }
        
    TMGraphicsPanel m_panel;
    TM_Transition m_transition;
    int m_moveX;
    int m_moveY;
}
