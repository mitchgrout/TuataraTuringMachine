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

package tuataraTMSim.machine.DFSA;

import java.awt.*;
import java.io.*;
import tuataraTMSim.exceptions.ComputationFailedException;
import tuataraTMSim.machine.*;

/**
 * An action for a state transition of a DFSA, which consists only of final a character.
 */
public class DFSA_Action extends PreAction
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Creates an instance of DFSA_Action.
     * @param match The character this action matches.
     */
    public DFSA_Action(char match)
    {
        // Matching this action moves the head rightward, writing nothing
        super(1, match, ' ');
    }

    /** 
     * Perform the action specified by this object on the given tape.
     * @param t The tape to be modified.
     */
    public void performAction(Tape t)
    {
        if (m_inputChar == Machine.EMPTY_INPUT_SYMBOL)
        {
            // A match, with the empty string; no action
        }
        else
        {
            // We have matched our input character, hence we simply move rightward.
            t.headRight();
        }
    }

    /**
     * Get a String representation of this action.
     * @return A String representation of this action.
     */
    public String toString()
    {
        // No other information is necessary bar the character to match
        return m_inputChar + "";
    }
}
