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
import tuataraTMSim.TM.TM_Action;
import tuataraTMSim.TM.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class ModifyTransitionActionCommand implements TMCommand
{
    /**
     * Creates a new instance of ModifyTransitionActionCommand
     */
    public ModifyTransitionActionCommand(TMGraphicsPanel panel, TM_Transition transition,
                                         TM_Action action)
    {
        m_panel = panel;
        m_transition = transition;
        m_action = action;
        m_oldAction = m_transition.getAction();
    }
    
    public void doCommand()
    {
        m_transition.setAction(m_action);
    }
    
    public void undoCommand()
    {
        m_transition.setAction(m_oldAction);
    }
    
    public String getName()
    {
        return "Modify Transition Action";
    }
        
    private TMGraphicsPanel m_panel;
    private TM_Transition m_transition;
    private TM_Action m_action;
    private TM_Action m_oldAction;   
}
