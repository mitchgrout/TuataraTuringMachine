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
 * A command which deals with changing the input symbol of a transition.
 * @author Jimmy
 */
public class ModifyInputSymbolCommand implements TMCommand
{    
    /**
     * Creates a new instance of ModifyInputSymbolCommand.
     * @param panel The current graphics panel.
     * @param transition The transition to modify.
     * @param symbol The new input symbol for the transition.
     */
    public ModifyInputSymbolCommand(TMGraphicsPanel panel, TM_Transition transition, char symbol)
    {
        m_panel = panel;
        m_transition = transition;
        m_symbol = symbol;
        m_oldSymbol = m_transition.getSymbol();
    }
    
    /**
     * Set the input symbol of the transition to be the new symbol.
     */
    public void doCommand()
    {
        m_transition.setSymbol(m_symbol);
        m_panel.getSimulator().computeNextTransition();
    }
    
    /**
     * Set the input symbol of the transition to be its previous value.
     */
    public void undoCommand()
    {
        m_transition.setSymbol(m_oldSymbol);
        m_panel.getSimulator().computeNextTransition();
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        return "Modify Input Symbol";
    }
       
    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;
    
    /**
     * The transition to modify.
     */
    private TM_Transition m_transition;
    
    /**
     * The new input symbol for m_transition.
     */
    private char m_symbol;
    
    /**
     * The old input symbol for m_transition.
     */
    private char m_oldSymbol;   
}
