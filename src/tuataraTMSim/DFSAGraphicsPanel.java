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
        // TODO: Move to MachineGraphicsPanel
        m_sim = new DFSA_Simulator(machine, tape);
        m_file = file;
        m_labelsUsed = m_sim.getMachine().getLabelHashset();
        initialization();
    }

    /**
     * Get the simulator object for the machine associated with this panel.
     * @return The simulator object for the machine.
     */
    public DFSA_Simulator getSimulator()
    {
        return m_sim;
    }

    /**
     *
     */
    public void onActivation()
    {
        // Naming schemes are not applicable for DFSAs.
        MainWindow.getInstance().m_configureSchemeAction.setEnabled(false);
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

        int x = e.getX() - DFSA_State.STATE_RENDERING_WIDTH / 2;
        int y = e.getY() - DFSA_State.STATE_RENDERING_WIDTH / 2;
        String label = getFirstFreeName();
        doCommand(new AddStateCommand(this, new DFSA_State(label, false, false, x, y)));
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
            DFSA_State mouseReleasedState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                DFSA_Transition newTrans = new DFSA_Transition((DFSA_State)m_mousePressedState, mouseReleasedState,
                        new DFSA_Action(Machine.UNDEFINED_SYMBOL));
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
        DFSA_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            deleteState(stateClickedOn);
        }
        else
        {
            DFSA_Transition transitionClickedOn = m_sim.getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
        DFSA_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            doCommand(new ToggleStartStateCommand(this, m_sim.getMachine().getStartState(), stateClickedOn));
        }
    }

    /**
     * Handle when a mouse click occurs while in select accepting state mode. If the mouse click
     * occurs over a state, the accepting state of the machine is changed.
     * @param e The generating event.
     */
    protected void handleChooseAcceptingClick(MouseEvent e)
    {
        DFSA_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        DFSA_State finalState = m_sim.getMachine().getFinalStates().isEmpty()?
                              null : m_sim.getMachine().getFinalStates().iterator().next();
        if (stateClickedOn != null)
        {
            // NOTE: Passing stateClickedOn twice prevents the old final states from being unset
            doCommand(new ToggleAcceptingStateCommand(this, stateClickedOn, stateClickedOn));
        }
    }

    /**
     * Handle when a mouse click occurs while in selection mode. If the mouse click occurs over a
     * state, the state is either added or removed from the selected state set, depending on context.
     * @param e The generating event.
     */
    protected void handleSelectionClick(MouseEvent e)
    {
        DFSA_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

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
        DFSA_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            m_sim.setCurrentState(stateClickedOn);
        }

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

    /**
     * The machine simulator. Exposes the machine and tape via .getMachine() and .getTape() respectively.
     */
    protected DFSA_Simulator m_sim;
}
