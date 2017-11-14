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
import tuataraTMSim.TM.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class DeleteTransitionCommand implements TMCommand
{
    
    /**
     * Creates a new instance of AddTransitionCommand 
     */
    public DeleteTransitionCommand(TMGraphicsPanel panel, TM_Transition transition)
    {
        m_panel = panel;
        m_transition = transition;
    }
    
    public void doCommand()
    {
        m_panel.getSimulator().getMachine().deleteTransition(m_transition);
        if (m_transition == m_panel.getSelectedTransition())
        {
            m_panel.deselectSymbol();
        }
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public void undoCommand()
    {
        m_panel.getSimulator().getMachine().addTransition(m_transition);
        //if (selectedStates.contains(mousePressedState) && selectedStates.contains(mouseReleasedState))
        //   selectedTransitions.add(t);
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public String getName()
    {
        return "Delete Transition";
    }
    
    
    private TMGraphicsPanel m_panel;
    private TM_Transition m_transition;
}

