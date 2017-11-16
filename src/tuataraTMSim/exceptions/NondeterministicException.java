//  ------------------------------------------------------------------
//
//  Copyright (c) 2017-2018 Mitchell Grout and the University of Waikato
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
//  author email: mjg44 (at) waikato (dot) ac (dot) nz
//
//  ------------------------------------------------------------------

package tuataraTMSim.exceptions;

/**
 * An Exception thrown when the machine finds a nondeterministic state, i.e. one that has multiple
 * transitions for the same input
 * @author Mitchell
 */
public class NondeterministicException extends java.lang.Exception
{
    /**
     * Creates a new instance of <code>NondeterministicException</code> without detail message.
     */
    public NondeterministicException() { }
    
    /**
     * Constructs an instance of <code>NondeterministicException</code> with the specified detail message.
     * @param msg The detailed error message.
     */
    public NondeterministicException(String msg) { super(msg); }
}
