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
     * Handle when a mouse click occurs over a state, by either selecting the existing underlying
     * state, or creating a new state.
     * @param e The generating event.
     */
    protected void handleAddNodesClick(MouseEvent e)
    {
        if (m_sim.getMachine().getStateClickedOn(e.getX(), e.getY()) != null)
        {
            // Adding states on top of states is not allowed
            handleSelectionClick(e);
            return;
        }

        int x = e.getX() - TM_State.STATE_RENDERING_WIDTH / 2;
        int y = e.getY() - TM_State.STATE_RENDERING_WIDTH / 2;
        switch (m_sim.getMachine().getNamingScheme())
        {
            case GENERAL:
                String label = getFirstFreeName();
                doCommand(new AddStateCommand(this, new TM_State(label, false, false, x, y)));
                break;

            case NORMALIZED:
                doJoinCommand(
                        new AddStateCommand(this, new TM_State("", false, false, x, y)),
                        new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
                break;
        }
    }

    /**
     * Handle when a mouse button is released. Creates any new transitions if a transition creating
     * drag has occured.
     * @param e The generating event.
     */
    protected void handleMouseReleased(MouseEvent e)
    {
        // Adding a transition
        if (m_currentMode == GUI_Mode.ADDTRANSITIONS && m_mousePressedState != null)
        {
            TM_State mouseReleasedState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                TM_Transition newTrans = new TM_Transition((TM_State)m_mousePressedState, mouseReleasedState,
                        new TM_Action(0, Machine.UNDEFINED_SYMBOL, Machine.UNDEFINED_SYMBOL));
                doCommand(new AddTransitionCommand(this, newTrans));
                repaint();
            }
        }
        // Finishing a selection
        else if (m_currentMode == GUI_Mode.SELECTION && m_selectionBox != null)
        {
            if (m_selectionBox.width != 0 && m_selectionBox.height != 0)
            {
                updateSelectedStatesAndTransitions();
            }
            repaint();
        }

        if (m_mousePressedState != null && m_movedState)
        {
            // Create an undo/redo command object for the move of a state/set of states/transitions.
            int translateX = m_mousePressedState.getX() - m_moveStateStartLocationX;
            int translateY = m_mousePressedState.getY() - m_moveStateStartLocationY;

            if (translateX != 0 || translateY != 0)
            {
                if (m_selectedStates.contains(m_mousePressedState))
                {
                    // Moved a set of states
                    Collection<State> statesCopy = (HashSet<State>)m_selectedStates.clone();
                    Collection<Transition> transitionsCopy = (HashSet<Transition>)m_selectedTransitions.clone();
                    addCommand(new MoveSelectedCommand(this, statesCopy, transitionsCopy,
                                translateX,  translateY));
                }
                else
                {
                    // Moved one state
                    Collection<Transition> transitions = new ArrayList<Transition>();
                    transitions.addAll(m_transitionsToMoveState);
                    addCommand(new MoveStateCommand(this, m_mousePressedState, translateX, 
                                translateY, transitions));
                }
            }
        }

        if (m_mousePressedTransition != null && m_movedTransition)
        {
            // Create an undo/redo command object for the move of a transition

            int translateX = (int)(m_mousePressedTransition.getMidpoint().getX() - m_transitionMidPointBeforeMove.getX());
            int translateY = (int)(m_mousePressedTransition.getMidpoint().getY() - m_transitionMidPointBeforeMove.getY());
            addCommand(new MoveTransitionCommand(this, m_mousePressedTransition, translateX, translateY));
        }

        m_selectionBox = null;
        m_mousePressedState = null;
        m_mousePressedTransition = null;
        m_transitionMidPointBeforeMove = null;
        m_movedTransition = false;
        m_movedState = false;
        m_drawPosX = Integer.MIN_VALUE; // Reset these values so that the line is not drawn.
        m_drawPosY = Integer.MIN_VALUE;
    }


    /** 
     * Handle when a mouse click occurs while in eraser mode. If the mouse click occurs over a
     * state, it is deleted, and if it is over a transition, that is deleted.
     * @param e The generating event.
     */
    public void handleEraserClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            deleteState(stateClickedOn);
        }
        else
        {
            TM_Transition transitionClickedOn = m_sim.getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
            if (transitionClickedOn != null)
            {
                deleteTransition(transitionClickedOn);
            }
        }
    }

    /**
     * Handle when a mouse click occurs while in select start state mode. If the mouse click occurs
     * over a state, the start state of the machine is changed.
     * @param e The generating event.
     */
    protected void handleChooseStartClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            switch (m_sim.getMachine().getNamingScheme())
            {
                case GENERAL:
                    doCommand(new ToggleStartStateCommand(this, m_sim.getMachine().getStartState(), stateClickedOn));
                    break;

                case NORMALIZED:
                    doJoinCommand(
                            new ToggleStartStateCommand(this, m_sim.getMachine().getStartState(), stateClickedOn),
                            new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
                    break;
            }
        }
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
     * Handle when a mouse click occurs while in selection mode. If the mouse click occurs over a
     * state, the state is either added or removed from the selected state set, depending on context.
     * @param e The generating event.
     */
    protected void handleSelectionClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (!(e.isControlDown() || e.isShiftDown()))
        {
            m_selectedStates.clear();
            m_selectedTransitions.clear();
        }
        if (stateClickedOn != null)
        {
            if (!m_selectedStates.remove(stateClickedOn))
            {
                m_selectedStates.add(stateClickedOn);
            }
        }
        m_selectedTransitions = m_sim.getMachine().getSelectedTransitions(m_selectedStates);
    }

    /**
     * Handle when a mouse click occurs while in current state selection mode. If the mouse click
     * occurs over a state, the state is made to be the current state.
     * @param e The generating event.
     */
    protected void handleChooseCurrentState(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            m_sim.setCurrentState(stateClickedOn);
        }

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
            JInternalFrame iFrame = inst.newMachineWindow(new TMGraphicsPanel(
                        m_contextState.getSubmachine(), MainWindow.getInstance().getTape(), null));
            inst.getDesktopPane().add(iFrame);
            iFrame.setVisible(true);
            try { iFrame.setSelected(true); }
            catch (PropertyVetoException e2) { }
        }
    }
}
