/*
 * NoStartStateException.java
 *
 * Created on November 7, 2006, 4:10 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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

/** An exception thrown when a machine without a start state is executed.
 *
 * @author Jimmy
 */
public class NoStartStateException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>NoStartStateException</code> without detail message.
     */
    public NoStartStateException() {
    }
    
    
    /**
     * Constructs an instance of <code>NoStartStateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoStartStateException(String msg) {
        super(msg);
    }
}
