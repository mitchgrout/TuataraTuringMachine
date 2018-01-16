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
import javax.swing.event.*;
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

        // Turing machines need to know about their owning panels
        m_sim.setPanel(this);

        // Add TM-specific context menu actions
        m_contextMenu.addSeparator();
        m_contextMenu.add(m_normalizeAction);
        m_contextMenu.add(m_submachineAction);

        initialization();
    }

    /**
     * Determine if the machine has been modified since its last save.
     * @return true if it has been modified since its last save, false otherwise.
     */
    public boolean isModifiedSinceSave()
    {
        // Submachines should not indicate that they are modified; this should instead be pushed to
        // the topmost machine
        return m_parent != null? false : super.isModifiedSinceSave(); 
    }

    /**
     * Set whether the machine has been modified since its last save.
     * @param isModified true if it has been modified since its last save, false otherwise.
     */
    public void setModifiedSinceSave(boolean isModified)
    {
        if (m_parent == null)
        {
            super.setModifiedSinceSave(isModified);
        }
        else
        {
            // Push notification upwards
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
    }

    /**
     * Get the owning graphics panel.
     * @return The owning graphics panel.
     */
    public TMGraphicsPanel getParentPanel()
    {
        return m_parent;
    }

    /**
     * Set the parent of this panel.
     * @param parent The new parent
     */ 
    public void setParentPanel(TMGraphicsPanel parent)
    {
        m_parent = parent;
    }

    /**
     * Get the state that owns this panel, and by extension the underlying simulator and machine.
     * @return The owning state.
     */
    public TM_State getParentState()
    {
        // No parent means no parent state
        if (m_parent == null)
        {
            return null;
        }

        // Iterate through all states searching for our machine
        for (TM_State state : m_parent.getSimulator().getMachine().getStates())
        {
            if (state.getSubmachine() == m_sim.getMachine())
            {
                return state;
            }
        }
        // Not reachable
        return null;
    }

    /**
     * Get the children of this panel.
     * @return The children of this panel.
     */
    public ArrayList<TMGraphicsPanel> getChildren()
    {
        return m_children;
    }

    /**
     * Add a child to this panel. Additionally calls child.setParentPanel(this).
     * @param child The child to add.
     */
    public void addChild(TMGraphicsPanel child)
    {
        m_children.add(child);
        child.setParentPanel(this);
    }

    /**
     * Remove a child from this panel. Additionally calls child.setParentPanel(null).
     * @param child The child to remove.
     * @return true if the child is removed, false otherwise.
     */
    public boolean removeChild(TMGraphicsPanel child)
    {
        if (m_children.remove(child))
        {
            child.setParentPanel(null);
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Get the filename associated with the machine. If getFile() is null, then this value is a
     * temporary name for the machine.
     * @return The filename associated with the machine.
     */
    public String getFilename()
    {
        // Submachines should just reflect their parents names, plus the state they belong to
        if (m_parent != null)
        {
            TM_State owner = getParentState();
            return String.format("%s - %s submachine", m_parent.getFilename(), owner.getLabel());
        }
        else
        {
            return super.getFilename();
        }
    }

    /**
     * Set the internal frame for this panel.
     * @param iFrame The new internal frame.
     */
    public void setFrame(MachineInternalFrame iFrame)
    {
        super.setFrame(iFrame);
        iFrame.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameClosed(InternalFrameEvent e)
            {
                // If we have an empty machine, destroy this frame
                if (m_parent != null && m_sim.getMachine().getStates().size() == 0)
                {
                    // m_parent != null => getParentState() != null
                    TM_State owner = getParentState();
                    owner.setSubmachine(null);
                    m_parent.removeChild(TMGraphicsPanel.this);
                }
                // Otherwise, close all children, but do not delete their references
                else for (TMGraphicsPanel child : m_children)
                {
                    MainWindow.getInstance().removeFrame(child.m_iFrame);
                }
            }
        });
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
     * The parent of this panel.
     */
    protected TMGraphicsPanel m_parent;

    /**
     * The children of this panel.
     */
    protected ArrayList<TMGraphicsPanel> m_children = new ArrayList<TMGraphicsPanel>();

    /**
     * Action to normalize the states in the machine.
     */
    protected Action m_normalizeAction = 
        new TriggerAction("Normalize", TRIGGER_ALL)
        {
            public void actionPerformed(ActionEvent e)
            {
                // TODO: Currently this is allocated as a closure; should we instead move it to a
                //       proper class?
                Collection<TM_State> states = m_sim.getMachine().getStates();
                HashMap<TM_State, String> oldLabels = new HashMap<TM_State, String>();
                for (TM_State s : states)
                {
                    oldLabels.put(s, s.getLabel());   
                }
                
                doCommand(new TMCommand()
                {
                    public void doCommand()
                    {
                        int counter = 1;
                        int size = states.size();
                        for (TM_State s : states)
                        {
                            // Start states are labelled 0
                            if (s.isStartState())
                            {
                                s.setLabel("0");
                            }
                            // Halt states are labelled (n-1), with n states
                            else if (s.isFinalState())
                            {
                                s.setLabel("" + (size - 1));
                            }
                            else
                            {
                                s.setLabel("" + counter++);
                            }
                        }
                    }

                    public void undoCommand()
                    {
                        for (State s : states)
                        {
                            s.setLabel(oldLabels.get(s));
                        }
                    }

                    public String getName()
                    {
                        return "Normalize machine";
                    }
                });
                // repaint();
            }
        };

    /**
     * Action to create a panel used to edit submachines.
     */
    protected Action m_submachineAction = 
        new TriggerAction("Edit Submachine", TRIGGER_STATE)
        {
            public void actionPerformed(ActionEvent e)
            {
                // For safety reasons, we can only ever have one instance of a frame for a submachine
                // due to machines being shared by reference. Before spawning a frame, check if one
                // exists.
                MainWindow inst = MainWindow.getInstance();

                // Create a new machine if necessary
                if (m_contextState.getSubmachine() == null)
                    switch (JOptionPane.showConfirmDialog(MainWindow.getInstance(), 
                                "Would you like to clone an existing machine?", "Make Submachine",
                                JOptionPane.YES_NO_CANCEL_OPTION))
                    {
                        case JOptionPane.YES_OPTION: 
                            // Show a file dialog and clone the given machine
                            JFileChooser fc = new JFileChooser();
                            fc.setDialogTitle("Clone Submachine");
                            fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
                                    {
                                        public boolean accept(File f)
                                        {
                                            return f.isDirectory() || f.getName().endsWith(MACHINE_EXT);
                                        }

                                        public String getDescription()
                                        {
                                            return String.format("%s files (*%s)", MACHINE_TYPE, MACHINE_EXT);
                                        }
                                    });
                            if (fc.showOpenDialog(MainWindow.getInstance()) != JFileChooser.APPROVE_OPTION)
                            {
                                // Cancel
                                return;
                            }
                            try { m_contextState.setSubmachine((TM_Machine) Machine.loadMachine(fc.getSelectedFile())); }
                            catch (Exception ex)
                            {
                                JOptionPane.showMessageDialog(MainWindow.getInstance(),
                                        String.format("Error opening machine file %s", fc.getSelectedFile().toString()));
                                MainWindow.getInstance().getConsole().log(String.format(
                                        "Encountered an error when loading the machine %s: %s",
                                        fc.getSelectedFile().toString(), ex.getMessage()));
                            }
                            break;

                        case JOptionPane.NO_OPTION:
                            // Add a blank machine
                            m_contextState.setSubmachine(new TM_Machine( 
                                        new ArrayList<TM_State>(), new ArrayList<TM_Transition>(),
                                        getAlphabet())); 
                            break;

                        default: 
                            // Cancel creating a submachine
                            return;
                    }

                // Determine if a MachineInternalFrame already exists
                MachineInternalFrame frame = null;
                for (TMGraphicsPanel child : m_children)
                {
                    if (child.getSimulator().getMachine() == m_contextState.getSubmachine())
                    {
                        frame = child.getFrame();
                        break;
                    }
                }

                // No frame present
                if (frame == null)
                {
                    TMGraphicsPanel gfx = new TMGraphicsPanel(m_contextState.getSubmachine(), inst.getTape(), null);
                    addChild(gfx);
                    frame = inst.newMachineWindow(gfx);
                    gfx.setFrame(frame);
                    inst.addFrame(frame);
                }

                if (!frame.isVisible())
                {
                    inst.addFrame(frame);
                }
            }
        };
}
