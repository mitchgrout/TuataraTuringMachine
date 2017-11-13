/*
 * MoveSelectedCommand.java
 *
 * Created on February 6, 2007, 5:54 PM
 *
 */

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


package tuataraTMSim.gui;
import java.awt.geom.Point2D;
import java.util.Collection;
import tuataraTMSim.TM_Transition;
import tuataraTMSim.TM_State;

/**
 *
 * @author Jimmy
 */
public class MoveSelectedCommand implements TMCommand
{
    
    /**
     * Creates a new instance of MoveSelectedCommand 
     */
    public MoveSelectedCommand(TMGraphicsPanel panel, Collection<TM_State> states,
            Collection<TM_Transition> transitions, int moveX, int moveY)
    {
        m_panel = panel;
        m_states = states;
        m_transitions = transitions;
        m_moveX = moveX;
        m_moveY = moveY;
        m_borderTransitions = 
                m_panel.getSimulator().getMachine().getHalfSelectedTransitions(states);
    }

    public void doCommand()
    {
        for (TM_State s : m_states)
        {
            s.setPosition(s.getX() + m_moveX, s.getY() + m_moveY);
        }
        
        double halfOfTranslatedX = m_moveX / 2.0;
        double halfOfTranslatedY = m_moveY / 2.0;
        
        for (TM_Transition t : m_transitions)
        {
            if (t.getFromState() == t.getToState())
            {
                Point2D cp = t.getControlPoint();
                t.setControlPoint((int)(cp.getX() + m_moveX), (int)(cp.getY() + m_moveY));
                continue;
            }
            Point2D cp = t.getControlPoint();
            Point2D midpoint = t.getMidpoint();
            midpoint.setLocation(midpoint.getX() + halfOfTranslatedX, midpoint.getY()+ halfOfTranslatedY);

            Point2D newCP = TM_Transition.getControlPointGivenMidpoint(midpoint, t.getFromState(), t.getToState());
            t.setControlPoint((int)(newCP.getX()), (int)(newCP.getY()));  
        }

        //handle movement of transitions that are connected to the moved states
        //by one end only
       
        for (TM_Transition t : m_borderTransitions)
        {
            TMGraphicsPanel.updateTransitionLocationWhenStateMoved(t, m_moveX, m_moveY);
        }
        for (TM_Transition t : m_borderTransitions)
        {
            TMGraphicsPanel.updateTransitionLocationWhenStateMoved(t, m_moveX, m_moveY);
        }
        
        
    }
    
    public void undoCommand()
    {
        for (TM_State s : m_states)
        {
            s.setPosition(s.getX() - m_moveX, s.getY() - m_moveY);
        }
        
        double halfOfTranslatedX = m_moveX / 2.0;
        double halfOfTranslatedY = m_moveY / 2.0;
        
        for (TM_Transition t : m_transitions)
        {
            if (t.getFromState() == t.getToState())
            {
                Point2D cp = t.getControlPoint();
                t.setControlPoint((int)(cp.getX() - m_moveX), (int)(cp.getY() - m_moveY));
                continue;
            }
            Point2D cp = t.getControlPoint();
            Point2D midpoint = t.getMidpoint();
            midpoint.setLocation(midpoint.getX() - halfOfTranslatedX, midpoint.getY() - halfOfTranslatedY);

            Point2D newCP = TM_Transition.getControlPointGivenMidpoint(midpoint, t.getFromState(), t.getToState());
            t.setControlPoint((int)(newCP.getX()), (int)(newCP.getY()));  
        }

        //handle movement of transitions that are connected to the moved states
        //by one end only
       
        for (TM_Transition t : m_borderTransitions)
        {
            TMGraphicsPanel.updateTransitionLocationWhenStateMoved(t, -m_moveX, -m_moveY);
        }
        for (TM_Transition t : m_borderTransitions)
        {
            TMGraphicsPanel.updateTransitionLocationWhenStateMoved(t, -m_moveX, -m_moveY);
        }
    }
    
    public String getName()
    {
        return "Move Selected Items";
    }

    TMGraphicsPanel m_panel;
    Collection<TM_State> m_states;
    Collection<TM_Transition> m_transitions;
    Collection<TM_Transition> m_borderTransitions;
    int m_moveX;
    int m_moveY;
    
}
