/*
 * Alphabet.java
 *
 * Created on November 22, 2006, 10:27 AM
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

import java.io.Serializable;

/**
 *
 * @author Jimmy
 */
public class Alphabet implements Serializable, Cloneable
{
    /** Creates a new instance of Alphabet.  Default is binary alphabet. */
    public Alphabet()
    {
        setAlphabetical(false);
        setDigits(false);
        setBlank(true);
        setSymbol('0', true);
        setSymbol('1', true);
    }
    
    public boolean containsSymbol(char c)
    {
        c = Character.toUpperCase(c);
        if (c == TMachine.EMPTY_ACTION_SYMBOL ||
            c == TMachine.WILDCARD_INPUT_SYMBOL)
            return false; //cant be in the alphabet
        if (Character.isLetter(c))
        {
            if (c < 'A' || c > 'Z')
                return false;
            return letters[c - 'A'];
        }
        if (Character.isDigit(c))
        {
            return digits[c - '0'];
        }
        if (c == ' ' || c == Tape.BLANK_SYMBOL)
            return blank;
        return false;
    }
    
    public void setSymbol(char c, boolean value)
    {
        c = Character.toUpperCase(c);
        
        if (Character.isLetter(c))
        {
            letters[c - 'A'] = value;
        }
        else if (Character.isDigit(c))
        {
            digits[c - '0'] = value;
        }
        else if (c == ' ' || c == Tape.BLANK_SYMBOL)
        {
            blank = value;
        }
    }
    
    public void setAlphabetical(boolean value)
    {
        for (boolean l : letters)
        {
            l = value;
        }
    }
    
    public void setDigits(boolean value)
    {
        for (boolean d : digits)
        {
            d = value;
        }
    }
    
    public void setBlank(boolean value)
    {
        blank = value;
    }
    
    public Object clone()
    {
        try
        {
            Alphabet returner = (Alphabet)super.clone();
        
            returner.letters = letters.clone();
            returner.digits = digits.clone();
            //blank should be copied correctly by super.clone
            
            return returner;
        } catch (CloneNotSupportedException e)
        {
            System.err.println("Clone failed!!!!!!");
            return null;
        }
    }
    
    protected boolean[] letters = new boolean[26];
    protected boolean[] digits = new boolean[10];
    protected boolean blank = false;
}
