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

/**
 * A command which deals with joining arbitrarily many commands into a single unified command.
 */
public class JoinCommand implements TMCommand
{
    /** 
     * Creates a new instance of JoinCommand.
     * @param first The first command to run.
     * @param second The second command to run.
     */
    public JoinCommand(TMCommand first, TMCommand second)
    {
        m_first = first;
        m_second = second;
    }
    
    /**
     * Run all commands in order.
     */
    public void doCommand()
    {
        m_first.doCommand();
        m_second.doCommand();
    }
    
    /**
     * Undo all commands in reverse order.
     */
    public void undoCommand()
    {
        m_second.undoCommand();
        m_first.undoCommand();
    }
    
    /**
     * Get the friendly name of this command.
     * @return The friendly name of this command.
     */
    public String getName()
    {
        // TODO: Potentially precompute this; to do so, we need to postulate that all names must
        //       either be CT constants, or defined in the constructor of every command.
        return String.format("%s & %s", m_first.getName(), m_second.getName());
    }
    
    /**
     * The first command to run.
     */
    private TMCommand m_first;

    /**
     * The second command to run.
     */
    private TMCommand m_second;
}
