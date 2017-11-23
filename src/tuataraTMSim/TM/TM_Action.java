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

package tuataraTMSim.TM;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.exceptions.TapeBoundsException;

/**
 * An action for a state transition of a Turing machine, such as shifting the read/write head, or
 * writing to the tape.
 * @author Jimmy
 */
public class TM_Action implements Serializable
{
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
        setDirection(dir);
        m_outputChar = outputSymbol;
        m_inputChar = inputSymbol;
    }
    
    /** 
     * Perform the action specified by this object on the given tape, updating both the physical
     * tape, and the location of the read/write head.
     * @param t The tape to be modified.
     * @throws TapeBoundsException If the read/write head falls off the tape.
     */
    public void performAction(Tape t) throws TapeBoundsException
    {
        if (m_direction == -1)
        {
            t.headLeft();
        }
        else if (m_direction == 1)
        {
            t.headRight();
        }
        else if (m_outputChar != TMachine.EMPTY_ACTION_SYMBOL)
        {
            t.write(m_outputChar);
        }
    }
    
    /** 
     * Get the direction the head moves for this action.
     * -1: left
     *  0: don't move the head
     *  1: right
     *  @return The direction the head moves for this action.
     */
    public int getDirection()
    {
        return m_direction;
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
     * Gets the character that must be read from the tape in order for the action to occur.
     * @return The input character for this action.
     */
    public char getInputChar()
    {
        return m_inputChar;
    }

    /**
     * Set the input character for this action.
     * @param c The new input character.
     */
    public void setInputChar(char c)
    {
        m_inputChar = c;
    }
 
    /**
     * Gets the character that this action will write to the tape.
     * This is only used if the direction is 0 (does not move the head).
     * @return The value to be written to the tape.
     */
    public char getOutputChar()
    {
        return m_outputChar;
    }

    /**
     * Set the character that this action will write to the tape. 
     * This is only used if the direction is 0 (does not move the head).
     * @param c The new value to be written to the tape.
     */
    public void setOutputChar(char c)
    {
        m_outputChar = c;
    }
    
    /**
     * Determine if this action moves the read/write head of the tape.
     * @return true if the action moves the read/write head of the tape, false otherwise.
     */
    public boolean movesHead()
    {
        return m_direction != 0;
    }
    
    /**
     * Render the action to a graphics object.
     * @param g The graphics object to render to.
     * @param x The X ordinate to render to.
     * @param y The Y ordinate to render to.
     */
    public void paint(Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setColor(Color.BLACK);
        
        String outputStr = toString();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        
        // stringWidth method doesnt seem to work correctly, using char size instead, assuming monospace
        x -= (metrics.charWidth('_') * outputStr.length()) / 2;
        // The vertical position is already ok - the baseline of the text rather than the top-left is specified.
        
        g2d.drawString(outputStr, x, y);
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
      
    /**
     * The direction the read/write head should move. Is exactly one of { -1, 0, +1 }
     */
    private byte m_direction;

    /**
     * The character that must be read from the tape for this action to occur.
     */
    private char m_inputChar;

    /**
     * The character that should be written to the tape if m_direction is zero.
     */
    private char m_outputChar;
}
