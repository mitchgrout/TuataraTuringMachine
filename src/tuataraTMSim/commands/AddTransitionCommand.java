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
import java.util.Collection;
import tuataraTMSim.Spline;
import tuataraTMSim.MachineGraphicsPanel;
import tuataraTMSim.machine.State;
import tuataraTMSim.machine.Transition;

/**
 * A command which deals with adding new transitions to a machine.
 * @author Jimmy
 */
public class AddTransitionCommand implements TMCommand
{
    /**
     * Number of pixels to be used as padding between multiple transitions between two states.
     */
    private final double PIXELS_BETWEEN_TRANSITIONS = 15;

    /**
     * Creates a new instance of AddTransitionCommand.
     * @param panel The current graphics panel.
     * @param transition The transition to add.
     */
    public AddTransitionCommand(MachineGraphicsPanel panel, Transition transition)
    {
        m_panel = panel;
        m_transition = transition;
    }
   
    /**
     * Adds the transition to the machine, and compute the next transition to be taken by the
     * machine.
     */
    public void doCommand()
    {
        // Calculate where to place the midpoint so as to hopefully not interfere
        // with existing transitions.  This is just a heuristic but will hopefully
        // be good enough in general.

        // Count how many transitions there are between the two states
        int numTransitions = 1;

        // NOTE: Compiler complains about [...].getTransitions() returning na Object, not a
        //       Transition, if this expression is put directly in the foreach. [ Bug? ]
        Collection<Transition> transitions = m_transition.getFromState().getTransitions();
        for (Transition t : transitions)
        {
            if (t.getToState() == m_transition.getToState())
            {
                numTransitions++;
            }
        }
      
        // Ditto
        transitions = m_transition.getToState().getTransitions();
        for (Transition t : transitions)
        {
            if (t.getToState() == m_transition.getFromState())
            {
                numTransitions++;
            }
        }
        
        int countOffset = (numTransitions % 2 == 0)? -1 : 0;
        
        int middleX = (m_transition.getFromState().getX() + m_transition.getToState().getX() + State.STATE_RENDERING_WIDTH) / 2;
        int middleY = (m_transition.getFromState().getY() + m_transition.getToState().getY() + State.STATE_RENDERING_WIDTH) / 2;

        int vectorX = Math.abs(m_transition.getFromState().getX() - m_transition.getToState().getX());
        int vectorY = Math.abs(m_transition.getFromState().getY() - m_transition.getToState().getY());

        // Avoid a problematic case        
        if (vectorX == 0 && vectorY == 0)
        {
            vectorY = 1;
            vectorX = 1;
        }
        
        int vectorOldX = vectorX;
        int vectorOldY = vectorY;
        
        // Make perpendicular to line between the states
        int temp = vectorX;
        vectorX = vectorY;
        vectorY = -temp;

        // Scale
        double length = (int)Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        double newLength = (numTransitions + countOffset) * PIXELS_BETWEEN_TRANSITIONS;
        double vectorNewX = vectorX * newLength/length;
        double vectorNewY = vectorY * newLength/length;
        
        // Not a loop
        if (m_transition.getFromState() != m_transition.getToState())
        {
            // Note that these coordinates are based on the top left of the states.
            if (numTransitions % 2 == 1)
            {
                Point2D cp = Spline.getControlPointFromMidPoint(
                        new Point2D.Double(middleX + vectorNewX, middleY + vectorNewY),
                        m_transition.getFromState(), m_transition.getToState());
                m_transition.setControlPoint((int)cp.getX(), (int)cp.getY());
            }
            else
            {
                Point2D cp = Spline.getControlPointFromMidPoint(
                        new Point2D.Double(middleX - vectorNewX, middleY - vectorNewY), 
                        m_transition.getFromState(), m_transition.getToState());
                m_transition.setControlPoint((int)cp.getX(), (int)cp.getY());
            }
        }
        // Is a loop
        else
        {
            numTransitions /= 2;
            int xOff = 0;
            int yOff= -(int)(State.STATE_RENDERING_WIDTH * 2);
            
            if (numTransitions % 4 == 1)
            {
                xOff = -(int)(State.STATE_RENDERING_WIDTH * 2);
                yOff = 0;
            }
            else if (numTransitions % 4 == 2)
            {
                yOff = -yOff;
            }
            else if (numTransitions % 4 == 3)
            {
                yOff = 0;
                xOff = (int)(State.STATE_RENDERING_WIDTH * 2);
            }
            
            xOff *= (1 + numTransitions / 4);
            yOff *= (1 + numTransitions / 4);
            
            xOff += State.STATE_RENDERING_WIDTH/2;
            yOff += State.STATE_RENDERING_WIDTH/2;
            
            m_transition.setControlPoint(m_transition.getFromState().getX() + xOff,
                    m_transition.getFromState().getY() + yOff);
        }

        // Add the transition to the machine
        m_panel.getSimulator().getMachine().addTransition(m_transition);
        if (m_panel.getSelectedStates().contains(m_transition.getFromState())
            && m_panel.getSelectedStates().contains(m_transition.getToState()))
        {
            m_panel.getSelectedTransitions().add(m_transition);
        }
    }
    
    /**
     * Removes the transition from the machine, and compute the next transition to be taken by the machine.
     */
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().deleteTransition(m_transition);
        if (m_transition == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Add Transition";
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * The transition to add or remove.
     */
    private Transition m_transition;
}
