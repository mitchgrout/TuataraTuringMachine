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

package tuataraTMSim.machine.TM;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.exceptions.ComputationFailedException;
import tuataraTMSim.machine.*;

/**
 * An action for a state transition of a Turing machine, such as shifting the read/write head, or
 * writing to the tape.
 */
public class TM_Action extends PreAction
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 2L;

    /**
     * Character representing a left-shift to the read/write head of the tape.
     */
    public static final char LEFT_ARROW = (char)0x2190;
    
    /**
     * Character representing a right-shift to the read/write head of the tape.
     */
    public static final char RIGHT_ARROW = (char)0x2192;
    
    /**
     * Creates a new instance of TM_Action.
     * @param dir The direction the read/write head should move.
     * @param inputSymbol The value for which if read from the tape, the action will occur.
     * @param outputSymbol The value for which if dir is zero, will be written to the tape.
     * */
    public TM_Action(int dir, char inputSymbol, char outputSymbol)
    {
        // NOTE: We use setDirection instead of directly setting m_direction, as it sanitizes input
        super(dir, inputSymbol, outputSymbol);
        setDirection(dir);
    }
 

    /**
     * Set the direction the head moves for this action.
     * -1: left
     *  0: don't move the head
     *  1: right
     *  @param dir The direction the head should move.
     */
    public void setDirection(int dir)
    {
        // Sanitize our input, just in case.
        if(dir < 0)
        {
            m_direction = -1;
        }
        else if(dir > 0)
        {
            m_direction = 1;
        }
        else
        {
            dir = 0;
        }
    }

    /** 
     * Perform the action specified by this object on the given tape, updating both the physical
     * tape, and the location of the read/write head.
     * @param t The tape to be modified.
     * @throws ComputationFailedException If the read/write head falls off the tape.
     */
    public void performAction(Tape t) throws ComputationFailedException
    {
        if (m_direction == -1)
        {
            t.headLeft();
        }
        else if (m_direction == 1)
        {
            t.headRight();
        }
        else if (m_outputChar != TM_Machine.EMPTY_ACTION_SYMBOL)
        {
            t.write(m_outputChar);
        }
    }

    /**
     * Get a String representation of this action.
     * @return A String representation of this action.
     */
    public String toString()
    {
        String outputStr = m_inputChar + "/";
        switch (m_direction)
        {
            case -1:
                outputStr += LEFT_ARROW;
                break;
            case 0:
                outputStr += m_outputChar;
                break;
            case 1:
                outputStr += RIGHT_ARROW;
                break;
        }
        return outputStr;
    }
}
