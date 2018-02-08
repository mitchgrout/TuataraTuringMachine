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

/**
 * An enumeration representing the different user interaction modes that the GUI can be in.
 */
public enum GUI_Mode
{ 
    /**
     * Add states to the machine.
     */
    ADDNODES,

    /**
     * Add transitions to the machine.
     */
    ADDTRANSITIONS,

    /**
     * Select multiple states and transitions with a rectangular marquee select.
     */
    SELECTION,

    /**
     * Remove states and transitions from the machine.
     */
    ERASER,

    /**
     * Choose the start state of the machine.
     */
    CHOOSESTART,

    /**
     * Choose the accepting state of the machine.
     */
    CHOOSEACCEPTING,

    /**
     * Choose the currently executing state of the machine.
     */
    CHOOSECURRENTSTATE
}
