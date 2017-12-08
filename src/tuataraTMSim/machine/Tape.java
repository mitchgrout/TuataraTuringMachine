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

package tuataraTMSim.machine;

import java.io.*;
import tuataraTMSim.exceptions.TapeBoundsException;
import tuataraTMSim.MainWindow;

/** 
 * A tape for a machine.
 */
public abstract class Tape implements Serializable
{
    /**
     * The character used to represent a blank symbol, which fill the tape.
     */
    public static final char BLANK_SYMBOL = '_';

    /** 
     * Read the current character from the tape, at the position of the read/write head.
     * @return The current character from the tape, at the positoin of the
     read/write head.
     */
    public abstract char read();

    /**
     * Shift the read/write head one cell to the left.
     * @throws TapeBoundsException If the read/write head is at the leftmost
     position of the tape.
     */
    public abstract void headLeft() throws TapeBoundsException;

    /**
     * Shift the read/write head one cell to the right.
     */
    public abstract void headRight();

    /**
     * Write the given character to tape, at the location of the read/write head.
     * @param c The character to write.
     */
    public abstract void write(char c);

    /**
     * Reset the read/write head to the start of the tape.
     */
    public abstract void resetRWHead();

    /**
     * Determine if the read/write head is parked.
     * @return true if the read/write head is in the first cell of the input tape, otherwise false.
     */
    public abstract boolean isParked();

    /**
     * Determine how long the string on the tape is. Blank characters not belonging to the infinite
     * sequence of blanks are counted.
     * @return How long the string on the tape is.
     */
    public abstract int getLength();

    /**
     * Get the tape contents as a String object.
     * @return The exact characters of the tape, in sequence, with no other text added.
     */
    public abstract String toString();

    /**
     * Get the tape contents from a specified offset and length as a String object.
     * @param begin The offset from the start of the tape.
     * @param length How many characters to read.
     * @return Exactly length many characters, read from the tape, beginning at the offset begin, in
     *         sequence, with no other text added.
     */
    public abstract String getPartialString(int begin, int length);

    /**
     * Get the location of the read/write head, relative to the start of the tape.
     * @return The location of the read/write head.
     */
    public abstract int headLocation();

    /**
     * Set this tape to be the empty tape.
     */
    public abstract void clearTape();

    /**
     * Set this tape to have exactly the characters of the other tape.
     * The read/write head is reset to the beginning of the tape.
     * @param other The tape to copy.
     */
    public abstract void copyOther(Tape other);

    /**
     * Set the window that this tape is associated with.
     * @param window The window to track. If not null, all of the machine panels in window will be
     *               kept up to date every time the tape is modified.
     */
    public abstract void setWindow(MainWindow window);

    /**
     * Serialize a tape, and write it to persistent storage.
     * @param t The tape to serialize.
     * @param file The file to write to.
     * @throws IOException If something is thrown by the underlying FileOutputStream.
     * @throws InvalidClassException If there is an issue with the class.
     */
    public static boolean saveTape(Tape t, File file)// throws IOException, InvalidClassException
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(t);
            out.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Load and deserialize a tape from persistent storage.
     * @param file The file where the tape was serialized and written to.
     * @return The deserialized tape, or null if the tape was not successfully loaded.
     * @throws IOException If something is wrong with the underlying FileInputStream.
     * @throws InvalidClassException If there is an issue with the class.
     */
    public static Tape loadTape(File file)// throws IOException, InvalidClassException
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            Tape result = (Tape)in.readObject();
            in.close();
            return result;
        }
        catch (Exception e)
        {
            return null;
        }
    }   
}