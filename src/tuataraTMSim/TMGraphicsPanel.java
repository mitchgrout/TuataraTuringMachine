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
import tuataraTMSim.machine.*;
import tuataraTMSim.machine.TM.*;

/**
 * The canvas for drawing a Turing machine state diagram.
 * @author Jimmy
 */
public class TMGraphicsPanel 
    extends MachineGraphicsPanel<TM_Action, TM_Transition, TM_State, TMachine, TM_Simulator>
{
    final float dash1[] = {3.0f};
    final BasicStroke dashed = new BasicStroke(1.0f, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 
            10.0f, dash1, 0.0f);
    final Font PANEL_FONT = /*new Font("SansSerif", Font.PLAIN, 14);*/
        new Font("Monospaced", Font.PLAIN, 14); //TESTING

    /**
     * Creates a new instance of TMGraphicsPanel. 
     * @param machine A non-null reference to a machine to render.
     * @param tape A non-null reference to a tape for the machine to use.
     * @param file The file the machine is associated with.
     * @param mainWindow The main window.
     */
    public TMGraphicsPanel(TMachine machine, Tape tape, File file, MainWindow mainWindow)
    {
        // TODO: Move to MachineGraphicsPanel
        m_sim = new TM_Simulator(machine, tape);
        m_file = file;
        m_labelsUsed = m_sim.getMachine().getLabelHashset();
        m_mainWindow = mainWindow;
        initialization();
    }

    /**
     * Get the simulator object for the machine associated with this panel.
     * @return The simulator object for the machine.
     */
    public TM_Simulator getSimulator()
    {
        return m_sim;
    }

    /** 
     * Accept a KeyEvent detected in the main window, and use it to update any transition action
     * selected by the user.
     * @param e The generating event.
     * @return true if a transition action was selected and updated, false otherwise.
     */
    public boolean handleKeyEvent(KeyEvent e)
    {
        if (selectedSymbolBoundingBox != null && getSelectedTransition() != null)
        {
            // There is a transition action currently selected by the user.
            char c = e.getKeyChar();
            c = Character.toUpperCase(c);

            if (inputSymbolSelected)
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
                else if (c == TMachine.OTHERWISE_SYMBOL)
                {
                    doCommand(new ModifyInputSymbolCommand(this, getSelectedTransition(), 
                                TMachine.OTHERWISE_SYMBOL));
                }
                else if (c == 'E' && e.isShiftDown())
                {
                    JOptionPane.showMessageDialog(null,"'" + TMachine.EMPTY_ACTION_SYMBOL +
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
                                    TMachine.EMPTY_ACTION_SYMBOL)));
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
        if (m_currentMode == GUI_Mode.ADDTRANSITIONS && mousePressedState != null)
        {
            TM_State mouseReleasedState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                TM_Transition newTrans = new TM_Transition((TM_State)mousePressedState, mouseReleasedState,
                        new TM_Action(0, Machine.UNDEFINED_SYMBOL, Machine.UNDEFINED_SYMBOL));
                doCommand(new AddTransitionCommand(this, newTrans));
                repaint();
            }
        }
        else if (m_currentMode == GUI_Mode.SELECTION)
        {
            if (selectionInProgress)
            {
                madeSelection = (selectionBoxStartX != selectionBoxEndX ||
                        selectionBoxStartY != selectionBoxEndY); //true IFF selection not empty

                if (madeSelection)
                {
                    updateSelectedStatesAndTransitions();
                }
                repaint();
            }
        }
        if (mousePressedState != null && movedState)
        {
            // Create an undo/redo command object for the move of a state/set of states/transitions.
            int translateX = mousePressedState.getX() - moveStateStartLocationX;
            int translateY = mousePressedState.getY() - moveStateStartLocationY;

            if (translateX != 0 || translateY != 0)
            {
                if (selectedStates.contains(mousePressedState))
                {
                    // Moved a set of states
                    Collection<State> statesCopy = (HashSet<State>)selectedStates.clone();
                    Collection<Transition> transitionsCopy = (HashSet<Transition>)selectedTransitions.clone();
                    addCommand(new MoveSelectedCommand(this, statesCopy, transitionsCopy,
                                translateX,  translateY));
                }
                else
                {
                    // Moved one state
                    Collection<Transition> transitions = new ArrayList<Transition>();
                    transitions.addAll(m_transitionsToMoveState);
                    addCommand(new MoveStateCommand(this, mousePressedState, translateX, 
                                translateY, transitions));
                }
            }
        }

        if (mousePressedTransition != null && movedTransition)
        {
            // Create an undo/redo command object for the move of a transition

            int translateX = (int)(mousePressedTransition.getMidpoint().getX() - transitionMidPointBeforeMove.getX());
            int translateY = (int)(mousePressedTransition.getMidpoint().getY() - transitionMidPointBeforeMove.getY());
            addCommand(new MoveTransitionCommand(this, mousePressedTransition, translateX, translateY));
        }
        selectionInProgress = false;

        mousePressedState = null;
        mousePressedTransition = null;
        transitionMidPointBeforeMove = null;
        movedTransition = false;
        movedState = false;
        drawPosX = Integer.MIN_VALUE; // Reset these values so that the line is not drawn.
        drawPosY = Integer.MIN_VALUE;
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
            selectedStates.clear();
            selectedTransitions.clear();
        }
        if (stateClickedOn != null)
        {
            if (!selectedStates.remove(stateClickedOn))
            {
                selectedStates.add(stateClickedOn);
            }
        }
        selectedTransitions = m_sim.getMachine().getSelectedTransitions(selectedStates);
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

    /**
     * The machine simulator. Exposes the machine and tape via .getMachine() and .getTape() respectively.
     */
    protected TM_Simulator m_sim;
}
