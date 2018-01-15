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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.commands.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;
import tuataraTMSim.machine.DFSA.*;

/**
 * The canvas for drawing a DFSA state diagram.
 */
public class DFSAGraphicsPanel 
    extends MachineGraphicsPanel<DFSA_Action, DFSA_Transition, DFSA_State, DFSA_Machine, DFSA_Simulator>
{
    /**
     * File extension.
     */
    public static final String MACHINE_EXT = ".fsa";

    /**
     * Friendly description.
     */
    public static final String MACHINE_TYPE = "DFSA";

    /**
     * Creates a new instance of DFSAGraphicsPanel. 
     * @param machine A non-null reference to a machine to render.
     * @param tape A non-null reference to a tape for the machine to use.
     * @param file The file the machine is associated with.
     */
    public DFSAGraphicsPanel(DFSA_Machine machine, Tape tape, File file)
    {
        super(new DFSA_Simulator(machine, tape), file);
        initialization();
    }

    /** 
     * Accept a KeyEvent detected in the main window, and use it to update any transition action
     * selected by the user.
     * @param e The generating event.
     * @return true if a transition action was selected and updated, false otherwise.
     */
    public boolean handleKeyEvent(KeyEvent e)
    {
        if (m_selectedSymbolBoundingBox == null || getSelectedTransition() == null)
        {
            return false;
        }

        // There is a transition action currently selected by the user.
        char c = Character.toUpperCase(e.getKeyChar());

        if (!m_inputSymbolSelected)
        {
            return false;
        }

        // TODO: Determine if DFSAs should support wildcards
        /*
        if (c == DFSA_Machine.OTHERWISE_SYMBOL)
        {
            doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), 
                        DFSA_Machine.OTHERWISE_SYMBOL));
        }
        */
        if (Character.isLetterOrDigit(c) && getAlphabet().containsSymbol(c))
        {
            doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), c));
        }
        else if (Character.isLetterOrDigit(c))
        {
            JOptionPane.showMessageDialog(null,"The input symbol for this transition"
                    + " cannot be set to the value '" + c + "', as that symbol is not in "
                    + "the alphabet for this machine.", "Update transition properties", 
                    JOptionPane.WARNING_MESSAGE);
        }
        return true;
    }

    /**
     * Create a DFSA_State object with the given label at the specified location.
     * @param label The state label.
     * @param x The x-ordinate of the state.
     * @param y The y-ordinate of the state.
     * @return A new DFSA_State object.
     */
    protected DFSA_State makeState(String label, int x, int y)
    {
        return new DFSA_State(label, false, false, x, y);
    }

    /**
     * Create a DFSA_Transition object with a default action, attached to the two specified states.
     * @param start The state the transition leaves.
     * @param end The state the transition arrives at.
     * @return A new DFSA_Transition object.
     */
    protected DFSA_Transition makeTransition(DFSA_State start, DFSA_State end)
    {
        return new DFSA_Transition(start, end, new DFSA_Action(Machine.UNDEFINED_SYMBOL));
    }

    public String getErrorMessage(ComputationCompletedException e)
    {
        return "The input string was accepted.";
    }

    public String getErrorMessage(ComputationFailedException e)
    {
        return "The input string was not accepted.";
    }

    public String getErrorMessage(NondeterministicException e)
    {
        return String.format("The machine could not be validated. %s", e.getMessage());
    }

    public String getErrorMessage(TapeBoundsException e)
    {
        // Unused
        return null;
    }

    public String getErrorMessage(UndefinedTransitionException e)
    {
        // Unused
        return null;
    }

    /**
     * Get the file extension associated with DFSAs.
     * @return The file extension associated with DFSAs.
     */
    public String getMachineExt()
    {
        return MACHINE_EXT;
    }

    /**  
     * Get a friendly name for the type of machine this graphics panel renders.
     * @return A friendly name for the type of machine being stored.
     */
    public String getMachineType()
    {
        return MACHINE_TYPE;
    }
}
