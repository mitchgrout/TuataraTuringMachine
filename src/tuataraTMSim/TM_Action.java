/*
 * TM_Action.java
 *
 * Created on November 7, 2006, 1:26 PM
 *
 */

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

package tuataraTMSim;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.font.GlyphVector;


/** An action for a state transition of a Turing machine, such as shifting the read/write
 *  head, or writing to the tape.
 * @author Jimmy
 */
public class TM_Action implements Serializable
{
 
    public static final char LEFT_ARROW = (char)0x2190;
    public static final char RIGHT_ARROW = (char)0x2192;
    /** Creates a new instance of TM_Action */
    public TM_Action(int dir, char inputSymbol, char outputSymbol)
    {
        m_direction = (byte)dir;
        m_writeChar = outputSymbol;
        m_inputChar = inputSymbol;
    }
    
    /** Perform the action specified by this object on the given tape,
     *  updating both the physical tape and the location of the read/write head.
     */
    public void performAction(Tape t) throws TapeBoundsException
    {
        if (m_direction == -1)
            t.headLeft();
        else if (m_direction == 1)
            t.headRight();
        else if (m_writeChar != TMachine.EMPTY_ACTION_SYMBOL)
            t.write(m_writeChar);
    }
    
    /** Get the direction the head moves for this action.
     *  -1: left
     *  0: don't move the head
     *  1: right
     */
    public int getDirection()
    {
        return m_direction;
    }
    
    /** Gets the character that this action will write to the tape.
     *  This is only used if the direction is 0 (does not move the head).
     */
    public char getChar()
    {
        return m_writeChar;
    }
    
    /** Returns true IFF this action will adjust the location of the read/
     *  write head.
     */
    public boolean movesHead()
    {
        return (m_direction == -1 || m_direction == 1);
    }
    
     /** Render the state to a graphics object.
     */
    public void paint(Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setColor(Color.BLACK);
        
        String outputStr = toString();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        
        //stringWidth method doesnt seem to work correctly, using char size instead
        //x -= metrics.stringWidth(outputStr) / 2; //centre on the given x value
        x -= (metrics.charWidth('_') * outputStr.length()) / 2; //assumes monospace font
        //The vertical position is already ok - the baseline of the text rather than the top-left
        //is specified.
                
        g2d.drawString(outputStr, x, y);
    }
    
    
    public String toString()
    {
        String outputStr = m_inputChar + "/";
        switch (m_direction)
        {
            case -1:
                outputStr += LEFT_ARROW;
                break;
            case 0:
                outputStr += m_writeChar;
                break;
            case 1:
                outputStr += RIGHT_ARROW;
        }
        return outputStr;
    }
    
    public void setInputSymbol(char c)
    {
        m_inputChar = c;
    }
    
    private byte m_direction; //-1 is left, +1 is right, otherwise don't move head and write char instead.
    private char m_writeChar;
    private char m_inputChar;
}
