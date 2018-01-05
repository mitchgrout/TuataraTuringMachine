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
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.commands.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;
import tuataraTMSim.machine.TM.*;

/**
 * The canvas for drawing a Turing machine state diagram.
 * @author Jimmy
 */
public class TMGraphicsPanel 
    extends MachineGraphicsPanel<TM_Action, TM_Transition, TM_State, TM_Machine, TM_Simulator>
{
    /**  
     * File extension.
     */
    public static final String MACHINE_EXT = ".tm";

    /**
     * Friendly description.
     */
    public static final String MACHINE_TYPE = "Turing Machine";

    /**
     * Creates a new instance of TMGraphicsPanel. 
     * @param machine A non-null reference to a machine to render.
     * @param tape A non-null reference to a tape for the machine to use.
     * @param file The file the machine is associated with.
     */
    public TMGraphicsPanel(TM_Machine machine, Tape tape, File file)
    {
        super(new TM_Simulator(machine, tape), file);

        // Add TM-specific context menu actions
        m_contextMenu.addSeparator();
        m_contextMenu.add(new SubmachineAction("Make Submachine"));

        initialization();
    }

    /**
     * Get the filename associated with the machine. If getFile() is null, then this value is a
     * temporary name for the machine.
     * @return The filename associated with the machine.
     */
    public String getFilename()
    {
        // Submachines should just reflect their parents names
        if (m_parent != null)
        {
            return m_parent.getFilename() + "-SUBMACHINE";
        }
        else
        {
            return super.getFilename();
        }
    }

    public void onActivation()
    {
        // Do nothing
    }

    /** 
     * Accept a KeyEvent detected in the main window, and use it to update any transition action
     * selected by the user.
     * @param e The generating event.
     * @return true if a transition action was selected and updated, false otherwise.
     */
    public boolean handleKeyEvent(KeyEvent e)
    {
        if (m_selectedSymbolBoundingBox != null && getSelectedTransition() != null)
        {
            // There is a transition action currently selected by the user.
            char c = e.getKeyChar();
            c = Character.toUpperCase(c);

            if (m_inputSymbolSelected)
            {
                if (e.isActionKey() && e.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    JOptionPane.showMessageDialog(null,"'" + TM_Action.LEFT_ARROW +
                            "' cannot be used as an input symbol!", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                }
                if (e.isActionKey() && e.getKeyCode() == KeyEvent.VK_RIGHT)
                {
                    JOptionPane.showMessageDialog(null,"'" + TM_Action.RIGHT_ARROW +
                            "' cannot be used as an input symbol!", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                }
                else if (c == TM_Machine.OTHERWISE_SYMBOL)
                {
                    doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), 
                                TM_Machine.OTHERWISE_SYMBOL));
                }
                else if (c == 'E' && e.isShiftDown())
                {
                    JOptionPane.showMessageDialog(null,"'" + TM_Machine.EMPTY_ACTION_SYMBOL +
                            "' cannot be used as an input symbol!", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                }
                else if (Character.isLetterOrDigit(c) && getAlphabet().containsSymbol(c))
                {
                    doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), c));
                }
                else if ((c == ' ' || c == Tape.BLANK_SYMBOL) && getAlphabet().containsSymbol(Tape.BLANK_SYMBOL))
                {
                    doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), Tape.BLANK_SYMBOL));
                }
                else if (Character.isLetterOrDigit(c))
                    JOptionPane.showMessageDialog(null,"The input symbol for this transition"
                            + " cannot be set to the value '" + c + "', as that symbol is not in "
                            + "the alphabet for this machine.", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                else if (c == ' ' || c == Tape.BLANK_SYMBOL)
                    JOptionPane.showMessageDialog(null,"The input symbol for this transition"
                            + " cannot be set to the value '" + Tape.BLANK_SYMBOL +"', as that symbol is not in "
                            + "the alphabet for this machine.", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);

            }
            else
            {
                TM_Action actn = getSelectedTransition().getAction();
                if (e.isActionKey())
                {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT)
                    {
                        doCommand(new ModifyTransitionActionCommand(this, getSelectedTransition(),
                                    new TM_Action(-1, getSelectedTransition().getAction().getInputChar(), c)));
                    }
                    else
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                        {
                            doCommand(new ModifyTransitionActionCommand(this, getSelectedTransition(), 
                                        new TM_Action(1, getSelectedTransition().getAction().getInputChar(), c)));
                        }
                }
                else if (c == 'E' && e.isShiftDown()) //shift + E makes an epsilon transition
                {
                    doCommand(new ModifyTransitionActionCommand(this, getSelectedTransition(), 
                                new TM_Action(0, getSelectedTransition().getAction().getInputChar(),
                                    TM_Machine.EMPTY_ACTION_SYMBOL)));
                }
                else if (Character.isLetterOrDigit(c)  && getAlphabet().containsSymbol(c))
                {
                    doCommand(new ModifyTransitionActionCommand(this, getSelectedTransition(), 
                                new TM_Action(0, getSelectedTransition().getAction().getInputChar(), 
                                    c)));
                }
                else if ((c == ' ' || c == Tape.BLANK_SYMBOL) && getAlphabet().containsSymbol(Tape.BLANK_SYMBOL))
                {
                    doCommand(new ModifyTransitionActionCommand(this, getSelectedTransition(),
                                new TM_Action(0, getSelectedTransition().getAction().getInputChar(),
                                    Tape.BLANK_SYMBOL)));
                }
                else if (Character.isLetterOrDigit(c))
                {
                    JOptionPane.showMessageDialog(null,"The action symbol for this transition"
                            + " cannot be set to the value '" + c + "', as that symbol is not in "
                            + "the alphabet for this machine.", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                }
                else if (c == ' ' || c == Tape.BLANK_SYMBOL)
                {
                    JOptionPane.showMessageDialog(null,"The action symbol for this transition"
                            + " cannot be set to the value '" + Tape.BLANK_SYMBOL + "', as that symbol is not in "
                            + "the alphabet for this machine.", "Update transition properties", 
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Handle when a mouse click occurs while in select accepting state mode. If the mouse click
     * occurs over a state, the accepting state of the machine is changed.
     * @param e The generating event.
     */
    protected void handleChooseAcceptingClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        TM_State finalState = m_sim.getMachine().getFinalStates().isEmpty()?
                              null : m_sim.getMachine().getFinalStates().iterator().next();
        if (stateClickedOn != null)
        {
            switch (m_sim.getMachine().getNamingScheme())
            {
                case GENERAL:
                    doCommand(new ToggleAcceptingStateCommand(this, finalState, stateClickedOn));
                    break;

                case NORMALIZED:
                    doJoinCommand(
                            new ToggleAcceptingStateCommand(this, finalState, stateClickedOn),
                            new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
            }
        }
    }

    /**
     * Create a TM_State object with the given label at the specified location.
     * @param label The state label.
     * @param x The x-ordinate of the state.
     * @param y The y-ordinate of the state.
     * @return A new TM_State object.
     */
    protected TM_State makeState(String label, int x, int y)
    {
        return new TM_State(label, false, false, x, y);
    }

    /**
     * Create a TM_Transition object with a default action, attached to the two specified states.
     * @param start The state the transition leaves.
     * @param end The state the transition arrives at.
     * @return A new TM_Transition object.
     */
    protected TM_Transition makeTransition(TM_State from, TM_State to)
    {
        return new TM_Transition(from, to, new TM_Action(0, Machine.UNDEFINED_SYMBOL, Machine.UNDEFINED_SYMBOL));
    }

    public String getErrorMessage(ComputationCompletedException e)
    {
        return "The machine halted correctly with the r/w head parked.";
    }

    public String getErrorMessage(ComputationFailedException e)
    {
        // Unused
        return null;
    }

    public String getErrorMessage(NondeterministicException e)
    {
        return String.format("The machine could not be validated. %s", e.getMessage()); 
    }

    public String getErrorMessage(TapeBoundsException e)
    {
        return "The machine r/w head went past the start of the tape.";
    }

    public String getErrorMessage(UndefinedTransitionException e)
    {
        return String.format("The machine did not complete a computation. %s", e.getMessage());
    }

    /**
     * Get the file extension associated with Turing machines.
     * @return The file extension associated with Turing machines.
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

    /**
     * Action to open a frame to edit a submachine.
     */
    protected class SubmachineAction extends AbstractAction
    {
        /**
         * Creates a new instance of SubmachineAction.
         * @param text Description of the action.
         */ 
        public SubmachineAction(String text)
        {
            super(text);
            putValue(Action.SHORT_DESCRIPTION, text);

            // TODO: Create a JFrame to allow blitting across an existing machine
        }

        /**
         * If no existing submachine, prompt the user to copy across an existing machine, or start
         * blank. Otherwise, load the machine into a new frame with no backing file.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            // Spawn a new frame containing the submachine
            if (m_contextState.getSubmachine() == null)
            {
                m_contextState.setSubmachine(new TM_Machine( 
                            new ArrayList<TM_State>(), new ArrayList<TM_Transition>(),
                            getAlphabet())); 
            }
            MainWindow inst = MainWindow.getInstance();
            TMGraphicsPanel gfx = new TMGraphicsPanel(m_contextState.getSubmachine(), inst.getTape(), null)
            {
                public boolean isModifiedSinceSave()
                {
                    // Submachines should not indicate that they have been modified;
                    // this should be pushed all the way up to the owning machine.
                    return false;
                }

                public void setModifiedSinceSave(boolean isModified)
                {
                    // Push the notif. upwards
                    TMGraphicsPanel owner = m_parent;
                    while (owner.m_parent != null)
                    {
                        owner = owner.m_parent;
                    }
                    owner.setModifiedSinceSave(isModified);
                    if (owner.m_iFrame != null)
                    {
                        owner.m_iFrame.updateTitle();
                    }
                }

            };
            addChild(gfx);
            JInternalFrame iFrame = inst.newMachineWindow(gfx);
            inst.getDesktopPane().add(iFrame);
            iFrame.setVisible(true);
            try { iFrame.setSelected(true); }
            catch (PropertyVetoException e2) { }
        }
    }

    /**
     * Set the parent of this panel.
     * @param parent The new parent
     */ 
    public void setParent(TMGraphicsPanel parent)
    {
        m_parent = parent;
    }

    /**
     * Add a child to this panel. Additionally calls child.setParent(this).
     * @param child The child to add.
     */
    public void addChild(TMGraphicsPanel child)
    {
        m_children.add(child);
        child.setParent(this);
    }

    /**
     * Remove a child from this panel. Additionally calls child.setParent(null).
     * @param child The child to remove.
     * @return true if the child is removed, false otherwise.
     */
    public boolean removeChild(TMGraphicsPanel child)
    {
        if (m_children.remove(child))
        {
            child.setParent(null);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * The parent of this panel.
     */
    protected TMGraphicsPanel m_parent;

    /**
     * The children of this panel.
     */
    protected ArrayList<TMGraphicsPanel> m_children = new ArrayList<TMGraphicsPanel>();
}
