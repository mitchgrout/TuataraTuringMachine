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

import tuataraTMSim.TM_Transition;

/**
 *
 * @author Jimmy
 */
public class ModifyInputSymbolCommand implements TMCommand
{
    
    /** Creates a new instance of ModifyInputSymbolCommand */
    public ModifyInputSymbolCommand(TMGraphicsPanel panel, TM_Transition transition,
            char symbol)
    {
        m_panel = panel;
        m_transition = transition;
        m_symbol = symbol;
        m_oldSymbol = m_transition.getSymbol();
    }
    
    public void doCommand()
    {
        m_transition.setSymbol(m_symbol);
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    public void undoCommand()
    {
        m_transition.setSymbol(m_oldSymbol);
        m_panel.getSimulator().computePotentialTransitions(false);
    }
    
    public String getName()
    {
        return "Modify Input Symbol";
    }
        
    private TMGraphicsPanel m_panel;
    private TM_Transition m_transition;
    private char m_symbol;
    private char m_oldSymbol;
    
}
