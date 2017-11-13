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
import java.io.IOException;
import java.io.Serializable;
import tuataraTMSim.gui.MainWindow;

/** An implementation of Tape, using a char array.
 *
 * @author Jimmy
 */
public class CA_Tape extends Tape implements Serializable 
{
    
    /** Creates a new instance of CA_Tape */
    public CA_Tape()
    {
        clearTape();
    }
    
    /** Creates a new instance of CA_Tape, setting the tape contents to a string */
    public CA_Tape(String initialTape)
    {
        setToString(initialTape);
    }
    
    /** Creates a new instance of CA_Tape, setting the tape contents to a string */
    public CA_Tape(String initialTape, MainWindow window)
    {
        setToString(initialTape);
        m_mainWindow = window;
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /** Read the current character from the tape, at the position of the 
     *  read/write heard.
     */
    public char read()
    {
        return tapeArray[headLoc];
    }
    
    /** Shift the head one cell to the left.
     */
    public void headLeft() throws TapeBoundsException
    {
        headLoc--;
        if (headLoc < 0)
        {
            resetRWHead();
            throw new TapeBoundsException();
        }
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /** Shift the head one cell to the right.
     */
    public void headRight()
    {
        headLoc++;
        if (headLoc >= tapeArray.length)
        {
            char[] newArray = new char[tapeArray.length * 2];
            for (int i = 0; i < tapeArray.length; i++)
                newArray[i] = tapeArray[i];
            for (int i = tapeArray.length; i < newArray.length; i++)
                newArray[i] = Tape.BLANK_SYMBOL; //blank
            tapeArray = newArray;
        }
        if (m_mainWindow != null)
        {
            m_mainWindow.updateAllSimulators();
        }
    }
    
    /** Write the character specified by 'c' to the location of the read/write head.
     *  @param c    the character to write.
     */
    public void write(char c)
    {
        tapeArray[headLoc] = c;
        if (m_mainWindow != null)
            m_mainWindow.updateAllSimulators();
    }
    
    /** Reset the read/write head to the start of the tape.
     */
    public void resetRWHead()
    {
        headLoc = 0;
        if (m_mainWindow != null)
            m_mainWindow.updateAllSimulators();
    }
    
    /** True IFF the read/write head is parked in the first cell of the input tape.
     */
    public boolean isParked()
    {
        return headLoc == 0;
    }
    
    /** Returns a string representation of the tape.  This must contain the exact
     *  characters of the tape, in sequence, with no other text added.
     */
    public String toString()
    {
        return new String(tapeArray);
    }
    
    /** Returns a string containing the characters in a segment of the tape.
     *  The tape is a one-ended infinite tape, with blank characters filling
     *  any unset tape character.
     */
    public String getPartialString(int begin, int length)
    {
        char[] returnCA = new char[length];
        for (int i = 0; i < length; i++)
        {
            if (i + begin >= tapeArray.length)
            {
                returnCA[i] = Tape.BLANK_SYMBOL;
            }
            else
            {
                returnCA[i] = tapeArray[i + begin];
            }
        }
        return new String(returnCA);
    }
    
    /** Returns the location of the head relative to the start of the tape.
     */
    public int headLocation()
    {
        return headLoc;
    }
    
    /** Set this tape to be the empty tape.
     */
    public void clearTape()
    {
        tapeArray = new char[100];
        for (int i = 0; i < tapeArray.length; i++)
            tapeArray[i] = Tape.BLANK_SYMBOL; //blank
        headLoc = 0;
        if (m_mainWindow != null)
            m_mainWindow.updateAllSimulators();
    }
    
    /** Set this tape to have exactly the characters of the other tape.
     *  The read/write head is reset to the beginning of the tape.
     */
    public void copyOther(Tape other)
    {
        setToString(other.toString());
        if (m_mainWindow != null)
            m_mainWindow.updateAllSimulators();
    }
    
    private void setToString(String s)
    {
        tapeArray = new char[100 + s.length()];
        for (int i = 0; i < s.length(); i++)
            tapeArray[i] = s.charAt(i);
        for (int i = s.length(); i < tapeArray.length; i++)
            tapeArray[i] = Tape.BLANK_SYMBOL; //blank
        headLoc = 0;
    }
    
    /** Set the window that this tape is associated with.  If not null,
     *  all of the machine panels in window will be kept up to date every time
     *  the tape is modified.
     */
    public void setWindow(MainWindow window)
    {
        m_mainWindow = window;
        if (m_mainWindow != null)
            m_mainWindow.updateAllSimulators();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        headLoc = 0;
        m_mainWindow = null;
    }
    
    private transient int headLoc = 0;
    private char[] tapeArray;
    private transient MainWindow m_mainWindow = null;
}
