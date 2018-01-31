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

import java.io.Serializable;
import tuataraTMSim.machine.Machine;

/**
 * Represents a collection of symbols which can appear on a Tape.
 * @author Jimmy
 */
public class Alphabet implements Serializable, Cloneable
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of Alphabet. Default is binary alphabet.
     */
    public Alphabet()
    {
        setAlphabetical(false);
        setDigits(false);
        setBlank(true);
        setSymbol('0', true);
        setSymbol('1', true);
    }

    /**
     * Get a character array containing all symbols which are deemed to be in this alphabet.
     * @return An array containing all symbols in this alphabet.
     */
    public char[] getSymbols()
    {
        // TODO: Can we make this better?
        String buf = "";
        for (int i = 0; i < m_letters.length; i++)
        {
            if (m_letters[i])
            {
                buf += (char)(i + 'A');
            }
        }
        for (int i = 0; i < m_digits.length; i++)
        {
            if (m_digits[i])
            {
                buf += (char)(i + '0');
            }
        }
        return buf.toCharArray();
    }

    /**
     * Determine if this alphabet contains the given symbol.
     * @param c The symbol to check.
     * @return true if this alphabet contains c, otherwise false.
     */
    public boolean containsSymbol(char c)
    {
        c = Character.toUpperCase(c);
        if (Character.isLetter(c))
        {
            return (c < 'A' || c > 'Z')? false : m_letters[c - 'A'];
        }
        else if (Character.isDigit(c))
        {
            return m_digits[c - '0'];
        }
        else if (c == ' ' || c == Tape.BLANK_SYMBOL)
        {
            return m_blank;
        }
        return false;
    }
    
    /**
     * Set whether or not the given symbol is in this alphabet.
     * @param c The symbol to set.
     * @param value If true, then make c part of this alphabet, otherwise remove it.
     */
    public void setSymbol(char c, boolean value)
    {
        c = Character.toUpperCase(c);
        
        if (Character.isLetter(c))
        {
            m_letters[c - 'A'] = value;
        }
        else if (Character.isDigit(c))
        {
            m_digits[c - '0'] = value;
        }
        else if (c == ' ' || c == Tape.BLANK_SYMBOL)
        {
            m_blank = value;
        }
    }
   
    /**
     * Set all letters in this alphabet to either be in the alphabet or not.
     * @param value If true, then set all letters to be in this alphabet, otherwise set all letters
     *              to not be in this alphabet.
     */
    public void setAlphabetical(boolean value)
    {
        for (boolean l : m_letters)
        {
            l = value;
        }
    }
    
    /**
     * Set all digits in this alphabet to either be in the alphabet or not.
     * @param value If true, then set all digits to be in this alphabet, otherwise set all digits to
     *              not be in this alphabet.
     */
   public void setDigits(boolean value)
    {
        for (boolean d : m_digits)
        {
            d = value;
        }
    }
    
    /**
     * Set whether or not the blank character is in the alphabet or not.
     * @param value If true, then the blank character is in the alphabet, otherwise not.
     */
    public void setBlank(boolean value)
    {
        m_blank = value;
    }
    
    /**
     * Create a deep copy of this object.
     * @return A deep copy of this object. Specifically, the object will have a distinct address,
     *         and all member variables will be pointing to distinct addresses, but represent the 
     *         same value.
     */
    public Object clone()
    {
        try
        {
            Alphabet returner = (Alphabet)super.clone();
        
            returner.m_letters = m_letters.clone();
            returner.m_digits = m_digits.clone();
            // Blank should be copied correctly by super.clone
            return returner;
        } 
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    /**
     * A bitmap determining which letters are in this alphabet.
     */
    protected boolean[] m_letters = new boolean[26];

    /**
     * A bitmap determining which digits are in this alphabet.
     */
    protected boolean[] m_digits = new boolean[10];
    
    /**
     * A boolean determining if the blank character is in this alphabet.
     */
    protected boolean m_blank = false;
}
