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

import java.io.*;
import tuataraTMSim.exceptions.TapeBoundsException;
import tuataraTMSim.MainWindow;

/** A tape for a turing machine, also stores the position of the read/write head.
 *
 * @author Jimmy
 */
public abstract class Tape implements Serializable
{
    public static final char BLANK_SYMBOL = '_';
    
    /**
     * Read the current character from the tape, at the position of the read/write heard.
     */
    public abstract char read();
    
    /**
     * Shift the head one cell to the left.
     */
    public abstract void headLeft() throws TapeBoundsException;
    
    /**
     * Shift the head one cell to the right.
     */
    public abstract void headRight();
    
    /**
     * Write the character specified by 'c' to the location of the read/write head.
     * @param c    the character to write.
     */
    public abstract void write(char c);
    
    /**
     * Reset the read/write head to the start of the tape.
     */
    public abstract void resetRWHead();
    
    /**
     * True IFF the read/write head is parked in the first cell of the input tape.
     */
    public abstract boolean isParked();
    
    /**
     * Returns a string representation of the tape.  This must contain the exact characters of the tape,
     * in sequence, with no other text added.
     */
    public abstract String toString();
    
    /**
     * Returns a string containing the characters in a segment of the tape. The tape is a one-ended
     * infinite tape, with blank characters filling any unset tape character.
     */
    public abstract String getPartialString(int begin, int length);
    
    /**
     * Returns the location of the head relative to the start of the tape.
     */
    public abstract int headLocation();
    
    /**
     * Set this tape to be the empty tape.
     */
    public abstract void clearTape();
    
    /**
     * Set this tape to have exactly the characters of the other tape.
     * The read/write head is reset to the beginning of the tape.
     */
    public abstract void copyOther(Tape other);
    
    /**
     * Set the window that this tape is associated with.  If not null, all of the machine panels
     * in window will be kept up to date every time the tape is modified.
     */
    public abstract void setWindow(MainWindow window);
    
    /**
     * Save (serialize) a tape to persistent storage.
     * @returns true IFF the serialization was successful.
     */
    public static boolean saveTape(Tape t, String file)
    {
         FileOutputStream fos = null;
         ObjectOutputStream out = null;
         try
         {
            fos = new FileOutputStream(file);
            out = new ObjectOutputStream(fos);
            out.writeObject(t);
            out.close();
         }
         catch(IOException ex)
         {
            ex.printStackTrace();
            return false;
         }
         return true;
    }
    
    /**
     * Load (deserialize) a tape from persistent storage.
     * @param file     The file where the tape is stored.
     * @returns        The tape that was loaded, or null
     *                 if the tape was not successfully
     *                 loaded.
     */
    public static Tape loadTape(String file)
    {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        Tape returner = null;
        try
        {
            fis = new FileInputStream(file);
            in = new ObjectInputStream(fis);
            returner = (Tape)in.readObject();
            in.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        return returner;
   }   
}
