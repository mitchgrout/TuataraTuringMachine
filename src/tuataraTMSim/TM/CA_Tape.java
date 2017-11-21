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

import java.io.IOException;
import java.io.Serializable;
import tuataraTMSim.exceptions.TapeBoundsException;
import tuataraTMSim.MainWindow;

/**
 * An implementation of Tape, using a char array.
 * @author Jimmy
 */
public class CA_Tape extends Tape implements Serializable 
{   
    /**
     * Creates a new instance of CA_Tape.
     */
    public CA_Tape()
    {
        clearTape();
    }
    
    /**
     * Creates a new instance of CA_Tape, setting the tape contents to a string.
     * @param initialTape The initial value of the tape.
     */
    public CA_Tape(String initialTape)
    {
        setToString(initialTape);
    }
    
    /**
     * Creates a new instance of CA_Tape, setting the tape contents to a string.
     * @param initialTape The initial value of the tape.
     * @param window A reference to the main program window.
     */
    public CA_Tape(String initialTape, MainWindow window)
    {
        setToString(initialTape);
        m_mainWindow = window;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /**
     * Read the current character from the tape, at the position of the read/write head.
     * @return The current character from the tape, at the positoin of the read/write head.
     */
    public char read()
    {
        return m_tapeArray[m_headLoc];
    }
    
    /**
     * Shift the read/write head one cell to the left.
     * @throws TapeBoundsException If the read/write head is at the leftmost position of the tape.
     */
    public void headLeft() throws TapeBoundsException
    {
        m_headLoc--;
        if (m_headLoc < 0)
        {
            resetRWHead();
            throw new TapeBoundsException();
        }
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /** 
     * Shift the read/write head one cell to the right.
     */
    public void headRight()
    {
        m_headLoc++;
        if (m_headLoc >= m_tapeArray.length)
        {
            char[] newArray = new char[m_tapeArray.length * 2];
            for (int i = 0; i < m_tapeArray.length; i++)
            {
                newArray[i] = m_tapeArray[i];
            }
            for (int i = m_tapeArray.length; i < newArray.length; i++)
            {
                // Blank
                newArray[i] = Tape.BLANK_SYMBOL;
            }
            m_tapeArray = newArray;
        }
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /**
     * Write the given character to tape, at the location of the read/write head.
     * @param c The character to write.
     */
    public void write(char c)
    {
        m_tapeArray[m_headLoc] = c;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /**
     * Reset the read/write head to the start of the tape.
     */
    public void resetRWHead()
    {
        m_headLoc = 0;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /**
     * Determine if the read/write head is parked.
     * @return true if the read/write head is in the first cell of the input tape, otherwise false.
     */
    public boolean isParked()
    {
        return m_headLoc == 0;
    }
    
    /**
     * Get the tape contents as a String object.
     * @return The exact characters of the tape, in sequence, with no other text added.
     */
    public String toString()
    {
        return new String(m_tapeArray);
    }
    
    /**
     * Get the tape contents from a specified offset and length as a String object.
     * @param begin The offset from the start of the tape.
     * @param length How many characters to read.
     * @return Exactly length many characters, read from the tape, beginning at the offset begin, in
     *         sequence, with no other text added.
     */
    public String getPartialString(int begin, int length)
    {
        char[] returnCA = new char[length];
        for (int i = 0; i < length; i++)
        {
            if (i + begin >= m_tapeArray.length)
            {
                returnCA[i] = Tape.BLANK_SYMBOL;
            }
            else
            {
                returnCA[i] = m_tapeArray[i + begin];
            }
        }
        return new String(returnCA);
    }
    
    /**
     * Get the location of the read/write head, relative to the start of the tape.
     * @return The location of the read/write head.
     */
    public int headLocation()
    {
        return m_headLoc;
    }
    
    /**
     * Set this tape to be the empty tape.
     */
    public void clearTape()
    {
        m_tapeArray = new char[100];
        for (int i = 0; i < m_tapeArray.length; i++)
        {
            // Blank
            m_tapeArray[i] = Tape.BLANK_SYMBOL;
        }
        m_headLoc = 0;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /**
     * Set this tape to have exactly the characters of the other tape.
     * The read/write head is reset to the beginning of the tape.
     * @param other The tape to copy.
     */
    public void copyOther(Tape other)
    {
        setToString(other.toString());
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
   
    /**
     * Set this tape to have exactly the characters in the given string.
     * The read/write head is reset to the beginning of the tape.
     * @param s The string to copy.
     */
    private void setToString(String s)
    {
        m_tapeArray = new char[100 + s.length()];
        for (int i = 0; i < s.length(); i++)
        {
            m_tapeArray[i] = s.charAt(i);
        }
        for (int i = s.length(); i < m_tapeArray.length; i++)
        {
            // Blank
            m_tapeArray[i] = Tape.BLANK_SYMBOL;
        }
        m_headLoc = 0;
    }
    
    /**
     * Set the window that this tape is associated with. 
     * @param window The window to track. If not null, all of the machine panels in window will be
     *               kept up to date every time the tape is modified.
     */
    public void setWindow(MainWindow window)
    {
        m_mainWindow = window;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
   
    /**
     * Read a CA_Tape into this object from the given stream.
     * @param in The stream to read.
     * @throws IOException If an IO error occurs.
     * @throws ClassNotFoundException If the deserialized object is not recognized.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        m_headLoc = 0;
        m_mainWindow = null;
    }
    
    /**
     * The position of the read/write head as an offset to the head of the tape.
     */
    private transient int m_headLoc = 0;
    
    /**
     * Memory used to store the tape data.
     */
    private char[] m_tapeArray;
    
    /**
     * The main window to track.
     */
    private transient MainWindow m_mainWindow = null;
}
