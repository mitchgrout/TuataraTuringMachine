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
import tuataraTMSim.TM.TM_State;
import tuataraTMSim.TM.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class AddTransitionCommand implements TMCommand
{
        public final double PIXELS_BETWEEN_TRANSITIONS = 15;
    /**
     * Creates a new instance of AddTransitionCommand 
     */
    public AddTransitionCommand(TMGraphicsPanel panel, TM_Transition transition)
    {
        m_panel = panel;
        m_transition = transition;
    }
    
    public void doCommand()
    {
        //calculate where to place the midpoint so as to hopefully not interfere
        //with existing transitions.  This is just a heuristic but will hopefully
        //be good enough in general.

        int numTransitions = 1; //count how many transitions between the two states
        for (TM_Transition t: m_transition.getFromState().getTransitions())
            if (t.getToState() == m_transition.getToState())
                numTransitions++;
        for (TM_Transition t: m_transition.getToState().getTransitions())
            if (t.getToState() == m_transition.getFromState())
                numTransitions++;
        
        int countOffset = 0;
        if (numTransitions % 2 == 0)
            countOffset = -1;
        
        int middleX = (m_transition.getFromState().getX() + m_transition.getToState().getX() + TM_State.STATE_RENDERING_WIDTH) / 2;
        int middleY = (m_transition.getFromState().getY() + m_transition.getToState().getY() + TM_State.STATE_RENDERING_WIDTH) / 2;

        int vectorX = Math.abs(m_transition.getFromState().getX() - m_transition.getToState().getX());
        int vectorY = Math.abs(m_transition.getFromState().getY() - m_transition.getToState().getY());
        
        if (vectorX == 0 && vectorY == 0) //avoid a problematic case
        {
            vectorY = 1;
            vectorX = 1;
        }
        int vectorOldX = vectorX;
        int vectorOldY = vectorY;
        //make perpendicular to line between the states
        int temp = vectorX;
        vectorX = vectorY;
        vectorY = -temp;
        //scale
        double length = (int)Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        double newLength = (numTransitions + countOffset) * PIXELS_BETWEEN_TRANSITIONS;
        double vectorNewX = vectorX * newLength/length;
        double vectorNewY = vectorY * newLength/length;
        
        if (m_transition.getFromState() != m_transition.getToState()) //not loop
        {
            //note that these coordinates are based on the top left of the states.
            if (numTransitions % 2 == 1)
            {
                Point2D cp = m_transition.getControlPointGivenMidpoint(new Point2D.Double(middleX
                         + vectorNewX, middleY + vectorNewY), 
                        m_transition.getFromState(), m_transition.getToState());
                m_transition.setControlPoint((int)cp.getX(), (int)cp.getY());
            }
            else
            {
                Point2D cp = m_transition.getControlPointGivenMidpoint(new Point2D.Double(middleX
                         - vectorNewX, middleY - vectorNewY), 
                        m_transition.getFromState(), m_transition.getToState());
                m_transition.setControlPoint((int)cp.getX(), (int)cp.getY());
            }
        }
        else //is a loop
        {
            numTransitions /= 2;
            int xOff = 0;
            int yOff= -(int)(TM_State.STATE_RENDERING_WIDTH * 2);
            
            if (numTransitions % 4 == 1)
            {
                xOff = -(int)(TM_State.STATE_RENDERING_WIDTH * 2);
                yOff = 0;
            }
            else if (numTransitions % 4 == 2)
                yOff = -yOff;
            else if (numTransitions % 4 == 3)
            {
                yOff = 0;
                xOff = (int)(TM_State.STATE_RENDERING_WIDTH * 2);
            }
            
            xOff *= (1 + numTransitions / 4);
            yOff *= (1 + numTransitions / 4);
            
            xOff += TM_State.STATE_RENDERING_WIDTH/2;
            yOff += TM_State.STATE_RENDERING_WIDTH/2;
            
            m_transition.setControlPoint(m_transition.getFromState().getX() + xOff,
                    m_transition.getFromState().getY() + yOff);
        }

        //add the transition to the machine
        m_panel.getSimulator().getMachine().addTransition(m_transition);
        if (m_panel.getSelectedStates().contains(m_transition.getFromState())
            && m_panel.getSelectedStates().contains(m_transition.getToState()))
            m_panel.getSelectedTransitions().add(m_transition);
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().deleteTransition(m_transition);
        if (m_transition == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public String getName()
    {
        return "Add Transition";
    }
    
    
    private TMGraphicsPanel m_panel;
    private TM_Transition m_transition;
}
