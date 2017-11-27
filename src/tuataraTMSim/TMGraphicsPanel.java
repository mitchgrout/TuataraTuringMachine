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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.commands.*;
import tuataraTMSim.TM.*;

/**
 * The canvas for drawing a Turing machine state diagram.
 * @author Jimmy
 */
public class TMGraphicsPanel extends JPanel
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
        m_sim = new TM_Simulator(machine, tape);
        m_file = file;
        m_labelsUsed = machine.createLabelsHashtable();
        m_mainWindow = mainWindow;
        initialization();
    }
    
    /**
     * Determine if this panel is opaque; allows for optomization by Swing.
     * @return true in all cases.
     */
    public boolean isOpaque()
    {
        return true;
    }
    
    /**
     * Set the user interface interaction mode for this panel. This determines the result of a click
     * in the panel.
     * @param currentMode The new GUI mode.
     */
    public void setUIMode(TM_GUI_Mode currentMode)
    {
        m_currentMode = currentMode;
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
     * Get the alphabet for the machine associated with this panel.
     * @return The alphabet for the machine.
     */
    public Alphabet getAlphabet()
    {
        return m_sim.getMachine().getAlphabet();
    }

    /**
     * Render to a graphics object.
     * @param g The graphics object to render onto.
     */
    protected void paintComponent(Graphics g)
    {
        int w = getWidth();
        int h = getHeight();
        
        Graphics2D g2d = (Graphics2D)g;
        // Fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        g2d.setFont(PANEL_FONT);
        
        TM_State currentState = m_sim.getCurrentState();
        if (currentState != null)
        {
            g2d.setColor(Color.YELLOW);
            g2d.fill(new Ellipse2D.Float(currentState.getX() - 5, currentState.getY() - 5, TM_State.STATE_RENDERING_WIDTH + 10, TM_State.STATE_RENDERING_WIDTH + 10));
            g2d.setColor(Color.BLACK);
            g2d.draw(new Ellipse2D.Float(currentState.getX() - 5, currentState.getY() - 5, TM_State.STATE_RENDERING_WIDTH + 10, TM_State.STATE_RENDERING_WIDTH + 10));
        }

        m_sim.getMachine().paint(g, selectedStates, selectedTransitions, m_sim);
        if (m_currentMode == TM_GUI_Mode.ADDTRANSITIONS && mousePressedState != null)
        {
            if (!(drawPosX == Integer.MIN_VALUE) || !(drawPosY == Integer.MIN_VALUE))
            {
                g2d.setColor(Color.BLACK);
                g2d.draw(new Line2D.Float(mousePressedState.getX() + TM_State.STATE_RENDERING_WIDTH/2,mousePressedState.getY()+ TM_State.STATE_RENDERING_WIDTH/2, drawPosX, drawPosY));
            }
        }
        
        if (selectedSymbolBoundingBox != null)
        {
            
            Stroke current = g2d.getStroke();
            g2d.setStroke(dashed);
            g2d.setColor(Color.BLACK);
            g2d.draw(selectedSymbolBoundingBox);
            g2d.setStroke(current);
        }
        if (selectionInProgress)
            {
                g2d.setColor(Color.BLACK);
                int topLeftX = Math.min(selectionBoxStartX, selectionBoxEndX);
                int topLeftY = Math.min(selectionBoxStartY, selectionBoxEndY);
                int width = Math.abs(selectionBoxStartX - selectionBoxEndX);
                int height = Math.abs(selectionBoxStartY - selectionBoxEndY);
                Stroke current = g2d.getStroke();
                g2d.setStroke(dashed);
                
                g2d.draw(new Rectangle2D.Float(topLeftX, topLeftY,
                    width, height));
                g2d.setStroke(current);

            }
    }
    
    /** 
     * Set up the panel. Should only be called by the constructor.
     */
    private void initialization()
    {
        // TODO: If only called by the constructor, of which there is only one, this should be moved
        //       entirely to the constructor.
        setFocusable(false);
        
        final Component thisPtr = this;
        // Set up event listeners and their corresponding actions.
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {   
                if (!m_editingEnabled)
                {
                    return;
                }

                // Deselect any selected action symbol
                selectedSymbolBoundingBox = null;
                selectedTransition = null;
                
                if (m_currentMode != TM_GUI_Mode.ERASER)
                {
                    // Select characters on transitions by left clicking
                    // or rename states
                    if (clickStateName(e) || selectCharacterByClicking(e))
                    {
                        repaint();
                        return;
                    }
                }
                
                switch (m_currentMode)
                {
                    case ADDNODES:
                        handleAddNodesClick(e);
                        break;
                    case ADDTRANSITIONS:
                        if ((e.isControlDown() || e.isShiftDown()))
                        {
                            handleSelectionClick(e);
                        }
                        break;
                    case ERASER:
                    {
                        handleEraserClick(e);
                        break;
                    }
                    case CHOOSESTART:
                    {
                        handleChooseStartClick(e);
                        break;
                    }
                    case CHOOSEACCEPTING:
                    {
                        handleChooseAcceptingClick(e);
                        break;
                    }
                    case SELECTION:
                    {
                        handleSelectionClick(e);
                        break;
                    }
                    case CHOOSECURRENTSTATE:
                    {
                        handleChooseCurrentState(e);
                        break;
                    }
                }
                if (m_sim.getMachine().getStateClickedOn(e.getX(), e.getY())!= null ||
                    m_sim.getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics()) != null)
                {
                    setModifiedSinceSave(true);
                }
                repaint();
                
            }

            public void mousePressed(MouseEvent e)
            {
                if (!m_editingEnabled)
                {
                    return;
                }
                handleMousePressed(e);
            }
            
            public void mouseReleased(MouseEvent e)
            {
                if (!m_editingEnabled)
                {
                    return;
                }
                handleMouseReleased(e);
                repaint();
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
                // Deselect any selected action symbol
                selectedSymbolBoundingBox = null;
                selectedTransition = null;
            
                boolean repaintNeeded = false;
                if (m_currentMode != TM_GUI_Mode.ADDTRANSITIONS)
                {
                    if (mousePressedState != null)
                    {
                        handleStateDrag(e);
                        repaintNeeded = true;
                    }
                    else if (m_currentMode == TM_GUI_Mode.SELECTION && selectionInProgress)
                    {
                        selectionBoxEndX = e.getX();
                        selectionBoxEndY = e.getY();
                        repaintNeeded = true;
                    }
                }
                // Add transitions mode
                else
                {
                    drawPosX = e.getX();
                    drawPosY = e.getY();
                    repaintNeeded = true;
                }
            
                if (mousePressedTransition != null)
                {
                    handleTransitionDrag(e);
                    repaintNeeded = true;
                }
            
                if (repaintNeeded)
                {
                    repaint();
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
        if (selectedSymbolBoundingBox != null && selectedTransition != null)
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
                    doCommand(new ModifyInputSymbolCommand(this, selectedTransition, 
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
                    doCommand(new ModifyInputSymbolCommand(this, selectedTransition, c));
                }
                else if ((c == ' ' || c == Tape.BLANK_SYMBOL) && getAlphabet().containsSymbol(Tape.BLANK_SYMBOL))
                {
                    doCommand(new ModifyInputSymbolCommand(this, selectedTransition, Tape.BLANK_SYMBOL));
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
                TM_Action actn = selectedTransition.getAction();
                if (e.isActionKey())
                {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT)
                    {
                        doCommand(new ModifyTransitionActionCommand(this, selectedTransition,
                                    new TM_Action(-1, selectedTransition.getAction().getInputChar(), c)));
                    }
                    else
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                    {
                        doCommand(new ModifyTransitionActionCommand(this, selectedTransition, 
                                    new TM_Action(1, selectedTransition.getAction().getInputChar(), c)));
                    }
                }
                else if (c == 'E' && e.isShiftDown()) //shift + E makes an epsilon transition
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition, 
                                new TM_Action(0, selectedTransition.getAction().getInputChar(),
                                TMachine.EMPTY_ACTION_SYMBOL)));
                }
                else if (Character.isLetterOrDigit(c)  && getAlphabet().containsSymbol(c))
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition, 
                                new TM_Action(0, selectedTransition.getAction().getInputChar(), 
                                c)));
                }
                else if ((c == ' ' || c == Tape.BLANK_SYMBOL) && getAlphabet().containsSymbol(Tape.BLANK_SYMBOL))
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition,
                                new TM_Action(0, selectedTransition.getAction().getInputChar(),
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
     
    // Auxiliary methods to handle click events in different situations:
    /**
     * Handle when a mouse click occurs over the label of a state, by bringing up a dialog box
     * requesting the new value for the label.
     * @param e The generating event.
     * @return true if the mouse click created a dialog box, false otherwise.
     */
    private boolean clickStateName(MouseEvent e)
    {
        TM_State nameClicked = m_sim.getMachine().getStateNameClickedOn(getGraphics(),e.getX(), e.getY());
        if (nameClicked == null)
        {
            return false;
        }
        m_keyboardEnabled = false; // Disable keyboard while dialog box shows
        String result = (String)JOptionPane.showInputDialog(null, "What would you like to label the state as?", "", JOptionPane.QUESTION_MESSAGE, null, null, nameClicked.getLabel());
        m_keyboardEnabled = true;
        
        if (result == null)
        {
            return true;
        }
        if (result.equals(""))
        {
            JOptionPane.showMessageDialog(null, "Empty labels are not allowed!");
            return true;
        }
        if (result.equals(nameClicked.getLabel()))
        {
            return true; // Change to the same thing
        }
        if (m_labelsUsed.containsKey(result))
        {
            JOptionPane.showMessageDialog(null, "You cannot assign a label that is already being used by another state!");
            return true;
        }
        
        doCommand(new RenameStateCommand(this, nameClicked, result));
        
        return true;
    }
    
    /**
     * Handle when a mouse click occurs over the action of a transition, by selecting the
     * appropriate symbol for editing.
     * @param e The generating event.
     * @return true if the action was clicked, false otherwise.
     */
    private boolean selectCharacterByClicking(MouseEvent e)
    {
        TM_Transition transitionClicked = m_sim.getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
        if (transitionClicked != null)
        {
            Shape s1 = transitionClicked.getInputSymbolBoundingBox(getGraphics());
            Shape s2 = transitionClicked.getOutputSymbolBoundingBox(getGraphics());
            if (s1.contains(e.getX(), e.getY()))
            {
                selectedSymbolBoundingBox = s1;
                inputSymbolSelected = true;
            }
            else if (s2.contains(e.getX(), e.getY()))
            {
                selectedSymbolBoundingBox = s2;
                inputSymbolSelected = false;
            }
            selectedTransition = transitionClicked;
            return true;
        }
        else
        {
            selectedSymbolBoundingBox = null;
            selectedTransition = null;
            return false;
        }
    }
    
    /**
     * Handle when a mouse click occurs over a state, by either selecting the existing underlying
     * state, or creating a new state.
     * @param e The generating event.
     */
    private void handleAddNodesClick(MouseEvent e)
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
     * Delete a state from the machine.
     * @param s The state to delete.
     */
    public void deleteState(TM_State s)
    {
        switch (m_sim.getMachine().getNamingScheme())
        {
            case GENERAL:
                doCommand(new DeleteStateCommand(this, s));
                break;

            case NORMALIZED:
                doJoinCommand(
                    new DeleteStateCommand(this, s),
                    new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
                break;
        }
    }
    
    /**
     * Delete a transition from the machine.
     * @param t The transition to delete.
     */
    public void deleteTransition(TM_Transition t)
    {
        doCommand(new DeleteTransitionCommand(this, t));
    }
    
    /**
     * Handle when a mouse click occurs while in select start state mode. If the mouse click occurs
     * over a state, the start state of the machine is changed.
     * @param e The generating event.
     */
    private void handleChooseStartClick(MouseEvent e)
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
    private void handleChooseAcceptingClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            switch (m_sim.getMachine().getNamingScheme())
            {
                case GENERAL:
                    doCommand(new ToggleAcceptingStateCommand(this, m_sim.getMachine().getFinalState(), stateClickedOn));
                    break;

                case NORMALIZED:
                    doJoinCommand(
                        new ToggleAcceptingStateCommand(this, m_sim.getMachine().getFinalState(), stateClickedOn),
                        new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
            }
        }
    }
    
    /**
     * Handle when a mouse click occurs while in selection mode. If the mouse click occurs over a
     * state, the state is either added or removed from the selected state set, depending on context.
     * @param e The generating event.
     */
    private void handleSelectionClick(MouseEvent e)
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
    private void handleChooseCurrentState(MouseEvent e)
    {
        TM_State stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            m_sim.setCurrentState(stateClickedOn);
        }
        
    }
    
    /** 
     * Handle when a mouse button is pressed. Determines selected states or transitions.
     * @param e The generating event.
     */
    private void handleMousePressed(MouseEvent e)
    {
        if ((e.isControlDown() || e.isShiftDown()))
        {
            // We don't want to start creating a new transition in this case
            mousePressedState = null;
        }
        else
        {
            mousePressedState = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        }
        if (mousePressedState != null) // Mouse press on a state
        {
            setModifiedSinceSave(true);
            moveStateClickOffsetX = mousePressedState.getX() - e.getX();
            moveStateClickOffsetY = mousePressedState.getY() - e.getY();
            moveStateLastLocationX = mousePressedState.getX();
            moveStateLastLocationY = mousePressedState.getY();
            moveStateStartLocationX = mousePressedState.getX();
            moveStateStartLocationY = mousePressedState.getY();
            m_transitionsToMoveState = m_sim.getMachine().getTransitionsTo(mousePressedState);
            precomputeSelectedTransitionsToDrag();
            return;
        }
        else
        {
            mousePressedTransition = m_sim.getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
            if (mousePressedTransition != null) // Mouse press on a transition
            {
                setModifiedSinceSave(true);
                transitionMidPointBeforeMove = mousePressedTransition.getMidpoint();
                moveTransitionClickOffsetX = (int)transitionMidPointBeforeMove.getX() - e.getX();
                moveTransitionClickOffsetY = (int)transitionMidPointBeforeMove.getY() - e.getY();
                
                return;
            }
        }
        // No state or transition clicked on
        if (m_currentMode == TM_GUI_Mode.SELECTION)
        {
            // Start building a selection bounding box
            madeSelection = false;
            selectionInProgress = true;
            selectionBoxStartX = selectionBoxEndX = e.getX();
            selectionBoxStartY = selectionBoxEndY = e.getY();
            selectionConcatenateMode = (e.isControlDown() || e.isShiftDown());
            
        }
    }
    
    /**
     * Handle when a mouse button is released. Creates any new transitions if a transition creating
     * drag has occured.
     * @param e The generating event.
     */
    private void handleMouseReleased(MouseEvent e)
    {
        if (m_currentMode == TM_GUI_Mode.ADDTRANSITIONS && mousePressedState != null)
        {
            TM_State mouseReleasedState = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                TM_Transition newTrans = new TM_Transition(mousePressedState, mouseReleasedState,
                        new TM_Action(0, TMachine.UNDEFINED_SYMBOL, TMachine.UNDEFINED_SYMBOL));
                doCommand(new AddTransitionCommand(this, newTrans));
                repaint();
            }
        }
        else if (m_currentMode == TM_GUI_Mode.SELECTION)
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
                    Collection<TM_State> statesCopy = (HashSet<TM_State>)selectedStates.clone();
                    Collection<TM_Transition> transitionsCopy = (HashSet<TM_Transition>)selectedTransitions.clone();
                    addCommand(new MoveSelectedCommand(this, statesCopy, transitionsCopy,
                            translateX,  translateY));
                }
                else
                {
                    // Moved one state
                    Collection<TM_Transition> transitions = new ArrayList<TM_Transition>();
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
     * Handle when a mouse drag occurs while in selection mode. Moves a transition relative to mouse
     * movement.
     * @param e The generating event.
     */
    private void handleTransitionDrag(MouseEvent e)
    {
        // Update control point location
        // TODO: enforce area bounds?
        movedTransition = true;
        
        // Find the midpoint by correcting for the offset of where the user clicked
        double correctedMidpointX = e.getX() + moveTransitionClickOffsetX; 
        double correctedMidpointY = e.getY() + moveTransitionClickOffsetY;
        
        Point2D newCP = TM_Transition.getControlPointGivenMidpoint(new Point2D.Double(correctedMidpointX, correctedMidpointY),
                mousePressedTransition.getFromState(), mousePressedTransition.getToState());
        
        mousePressedTransition.setControlPoint((int)newCP.getX(), (int)newCP.getY());
        
        // Move the bounding box for the selected symbol if it is on this transition action
        if (mousePressedTransition == selectedTransition)
        {
            updateSelectedSymbolBoundingBox();
        }
    }
    
    /**
     * Handle when a mouse drag occurs while in selection mode. Moves a state relative to mouse
     * movement.
     * @param e The generating event.
     */
    private void handleStateDrag(MouseEvent e)
    {
        int newX = e.getX() + moveStateClickOffsetX;
        int newY = e.getY() + moveStateClickOffsetY;

        // Check that this is within panel bounds.
        // This is complicated in the case where multiple items are selected
        Dimension boundaries = getSize();
        int minY = 0;
        int minX = 0;
        int maxX = (int)boundaries.getWidth() - TM_State.STATE_RENDERING_WIDTH;
        int maxY = (int)boundaries.getHeight() - TM_State.STATE_RENDERING_WIDTH;

        if (selectedStates.contains(mousePressedState)) // Clicked on a selected state
        {
            int rightMostX = mousePressedState.getX();
            int leftMostX = mousePressedState.getX();
            int bottomMostY = mousePressedState.getY();
            int topMostY = mousePressedState.getY();
            for (TM_State s : selectedStates)
            {
                if (s.getX() > rightMostX)
                {
                    rightMostX = s.getX();
                }
                if (s.getY() > bottomMostY)
                {
                    bottomMostY = s.getY();
                }
                if (s.getX() < leftMostX)
                {
                    leftMostX = s.getX();
                }
                if (s.getY() < topMostY)
                {
                    topMostY = s.getY();
                }
            }
            maxX -= rightMostX - mousePressedState.getX();
            maxY -= bottomMostY - mousePressedState.getY();
            minX += mousePressedState.getX() - leftMostX;
            minY += mousePressedState.getY() - topMostY;
        }
        newX = Math.min(newX, maxX);
        newY = Math.min(newY, maxY);
        newX = Math.max(minX, newX); // Constrain to accessable bounds of view plane
        newY = Math.max(minY, newY);

        int translateX = newX - moveStateLastLocationX;
        int translateY = newY - moveStateLastLocationY;
        
        if (translateX == 0 && translateY == 0)
        {
            return;
        }
        movedState = true;
        if (!selectedStates.contains(mousePressedState))
        {
            // Just move the one state
            updateTransitionLocations(mousePressedState,translateX, translateY,
                    m_transitionsToMoveState, mousePressedState.getTransitions());
            mousePressedState.setPosition(mousePressedState.getX() + translateX,
                    mousePressedState.getY() + translateY);
        }
        else
        {
            // Move all selected states
            pullSelectedTransitionsWithState(translateX, translateY);            
            for (TM_State s : selectedStates)
            {
                s.setPosition(s.getX() + translateX,
                    s.getY() + translateY);
            }
        }
        moveStateLastLocationX = mousePressedState.getX();
        moveStateLastLocationY = mousePressedState.getY();
        updateSelectedSymbolBoundingBox();
    }
    
    /** 
     * Move the bounding box for the transition symbol currently selected by the user, if any, in
     * the case where a transition has been moved.
     */
    private void updateSelectedSymbolBoundingBox()
    {
        if (selectedSymbolBoundingBox == null || selectedTransition == null)
        {
            return;
        }
        if (inputSymbolSelected)
        {
            selectedSymbolBoundingBox = selectedTransition.getInputSymbolBoundingBox(getGraphics());
        }
        else
        {
            selectedSymbolBoundingBox = selectedTransition.getOutputSymbolBoundingBox(getGraphics());
        }
    }
    
    /** 
     * Update our sets of selected states and transitions. Should be called when the user selected
     * region has been moved.
     */
    private void updateSelectedStatesAndTransitions()
    {
        int topLeftX = Math.min(selectionBoxStartX, selectionBoxEndX);
        int topLeftY = Math.min(selectionBoxStartY, selectionBoxEndY);
        int width = Math.abs(selectionBoxStartX - selectionBoxEndX);
        int height = Math.abs(selectionBoxStartY - selectionBoxEndY);
        
        HashSet<TM_State> states = m_sim.getMachine().getSelectedStates(topLeftX, topLeftY, width, height);
        
        if (selectionConcatenateMode)
        {
            selectedStates.addAll(states);
        }
        else
        {
            selectedStates = states;
        }
        selectedTransitions = m_sim.getMachine().getSelectedTransitions(selectedStates);
    }
   
    /**
     * Precompute transitions that should be moved in a drag event.
     */
    private void precomputeSelectedTransitionsToDrag()
    {
        m_inTransitionsToMove = new HashSet<TM_Transition>();
        m_outTransitionsToMove = new HashSet<TM_Transition>();
        calcMovedTransitionSets(m_inTransitionsToMove, m_outTransitionsToMove);
        
        m_TransitionsToMoveintersection = new HashSet<TM_Transition>();
        m_TransitionsToMoveintersection.addAll(m_inTransitionsToMove);
        m_TransitionsToMoveintersection.retainAll(m_outTransitionsToMove);
        m_inTransitionsToMove.removeAll(m_TransitionsToMoveintersection);
        m_outTransitionsToMove.removeAll(m_TransitionsToMoveintersection);
    }
   
    /**
     * Move transitions relative to their connected states movement.
     * @param translateX The amount of pixels in the X direction the state moved.
     * @param translateY The amount of pixels in the Y direction the state moved.
     */
    private void pullSelectedTransitionsWithState(int translateX, int translateY)
    {
        double halfOfTranslatedX = translateX / 2.0;
        double halfOfTranslatedY = translateY / 2.0;
        
        for (TM_Transition t : m_TransitionsToMoveintersection)
        {
            if (t.getFromState() == t.getToState())
            {
                Point2D cp = t.getControlPoint();
                t.setControlPoint((int)(cp.getX() + translateX), (int)(cp.getY() + translateY));
                continue;
            }
            Point2D cp = t.getControlPoint();
            Point2D midpoint = t.getMidpoint();
            midpoint.setLocation(midpoint.getX() + halfOfTranslatedX, midpoint.getY()+ halfOfTranslatedY);

            Point2D newCP = TM_Transition.getControlPointGivenMidpoint(midpoint, t.getFromState(), t.getToState());
            t.setControlPoint((int)(newCP.getX()), (int)(newCP.getY()));  
        }

        for (TM_Transition t : m_inTransitionsToMove)
        {
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
        for (TM_Transition t : m_outTransitionsToMove)
        {
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
    }
    
    /** 
     * Update transitions associated with a moved state. Must be called before the actual state
     * location is updated.
     * @param mousePressedState The state being moved.
     * @param translateX The change in X ordinate.
     * @param translateY The change in Y ordinate.
     * @param transitionsInto The transitions coming into mousePressedState.
     * @param transitionsOut The transitions leaving mousePressedState.
     */
    public static void updateTransitionLocations(TM_State mousePressedState, int translateX, int translateY,
            Collection<TM_Transition> transitionsInto, Collection<TM_Transition> transitionsOut)
    {
        for (TM_Transition t : transitionsOut)
        {
            if (t.getFromState() == t.getToState())
            {
                Point2D cp = t.getControlPoint();
                t.setControlPoint((int)(cp.getX() + translateX), (int)(cp.getY() + translateY));
                continue;
            }
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
        for (TM_Transition t : transitionsInto)
        {
            if (t.getFromState() == t.getToState())
            {
                continue; // Already handled
            }
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
    }
    
    /** 
     * When one end of a transition is moved, update the control point correctly.
     * Will not handle loops, or other cases where both ends of the transition are moved.
     * Must be called before the actual state locations are updated.
     * @param t The transition being moved.
     * @param translateX The change in X ordinate.
     * @param translateY The change in Y ordinate.
     */
    public static void updateTransitionLocationWhenStateMoved(TM_Transition t, int translateX, int translateY)
    {
        double middleOfLineX = (t.getFromState().getX() +  t.getToState().getX()) / 2;
        double middleOfLineY = (t.getFromState().getY() +  t.getToState().getY()) / 2;

        double arcCPDisplacementVectorX = t.getControlPoint().getX() - middleOfLineX;
        double arcCPDisplacementVectorY = t.getControlPoint().getY() - middleOfLineY;
        double newFromX = t.getFromState().getX();
        double newFromY = t.getFromState().getY();
        double newToX = t.getToState().getX();
        double newToY = t.getToState().getY();

        double newMiddleOfLineX = (newFromX + newToX + translateX) / 2;
        double newMiddleOfLineY = (newFromY + newToY + translateY) / 2;
        t.setControlPoint((int)(newMiddleOfLineX + arcCPDisplacementVectorX),
                (int)(newMiddleOfLineY + arcCPDisplacementVectorY));
    }
   
    /**
     * Determine all transitions associated with all selected states. Results are stored in the two
     * arguments.
     * @param inTransitions The collection of transitions coming into all selected states.
     * @param outTransitions The collection of transitions leaving all selected states.
     */
    private void calcMovedTransitionSets(HashSet<TM_Transition> inTransitions,
            HashSet<TM_Transition> outTransitions)
    {
        for (TM_State s : selectedStates)
        {
            ArrayList<TM_Transition> out = s.getTransitions();
            outTransitions.addAll(out);
            ArrayList<TM_Transition> in = m_sim.getMachine().getTransitionsTo(s);
            inTransitions.addAll(in);
        }
    }
    
    /**
     * Get the file associated with the machine.
     * @return The file associated with the machine.
     */
    public File getFile()
    {
        return m_file;
    }
    
    /**
     * Set the file associated with the machine.
     * @param f The new file associated with the machine.
     */
    public void setFile(File f)
    {
        m_file = f;
        updateTitle();
    }
    
    /**
     * Update the title for the frame.
     */
    private void updateTitle()
    {
        if (m_iFrame != null)
        {
            m_iFrame.updateTitle();
        }
    }
   
    /** 
     * Find the first unused standard state label in the machine. Standard labels are 'q' followed
     * by a non-negative integer.
     * @return The first unused standard state label.
     */
    public String getFirstFreeName()
    {
        switch (m_sim.getMachine().getNamingScheme())
        {
            case GENERAL:
                int current = 0;
                while (m_labelsUsed.containsKey("q" + current))
                {
                    current++;
                }
                return "q" + current;

            case NORMALIZED:
                // Assume every state name is normalized, hence no naming conflicts
                return "" + m_sim.getMachine().getStates().size();
        
            default:
                return null;
        }
    }
    
    /**
     * Determine if the label dictionary contains a given label.
     * @param name The label to check.
     * @return true if name is a used label, false otherwise.
     */
    public boolean dictionaryContainsName(String name)
    {
        return m_labelsUsed.containsKey(name);
    }
    
    /** 
     * Adds a state name label to the dictionary of labels that have been used and cannot be used
     * again, unless deleted.
     * @param label The label to be add to the used list.
     */
    public void addLabelToDictionary(String label)
    {
        m_labelsUsed.put(label, label);
    }
    
    /**
     * Removes a state name label from the dictionary of labels that have been used and cannot be
     * used again, unless deleted.
     * @param label The label to be removed from the used list.
     */
    public void removeLabelFromDictionary(String label)
    {
        m_labelsUsed.remove(label);
    }
    
    /**
     * Set the internal frame for this panel.
     * @param iFrame The new internal frame.
     */
    public void setWindow(TMInternalFrame iFrame)
    {
        m_iFrame = iFrame;
        updateTitle();
    }
    
    /**
     * Get the internal frame for this panel.
     * @return The internal frame for this panel.
     */
    public TMInternalFrame getWindow()
    {
        return m_iFrame;
    }
    
    /**
     * Determine if the keyboard is enabled.
     * @return true if the keyboard is enabled, false otherwise.
     */
    public boolean getKeyboardEnabled()
    {
        return m_keyboardEnabled;
    }
    
    /**
     * Deselect any transition action character currently selected by the user. Causes a repaint.
     */
    public void deselectSymbol()
    {
        selectedSymbolBoundingBox= null;
        selectedTransition = null;
        repaint();
    }
    
    /**
     * Get the current transition selected by the user for modification of its input/output symbols,
     * or null if there is no selected transition one.
     * @return The current transition selected by the user for modification.
     */
    public TM_Transition getSelectedTransition()
    {
        return selectedTransition;
    }
    
    /**
     * Get the set of transitions selected by the user.
     * @return The set of transitions selected by the user.
     */
    public HashSet<TM_Transition> getSelectedTransitions()
    {
        return selectedTransitions;
    }
    
    /**
     * Get the set of states selected by the user.
     * @return The set of states selected by the user.
     */
    public HashSet<TM_State> getSelectedStates()
    {
        return selectedStates;
    }
    
    /**
     * Delete all states and transitions that the user has currently selected.
     */
    public void deleteAllSelected()
    {
        HashSet<TM_State> selectedStatesCopy = (HashSet<TM_State>)selectedStates.clone();
        HashSet<TM_Transition> selectedTransitionsCopy = (HashSet<TM_Transition>)selectedTransitions.clone();
        switch (m_sim.getMachine().getNamingScheme())
        {
            case GENERAL:
                doCommand(new DeleteAllSelectedCommand(this, selectedStatesCopy,
                        selectedTransitionsCopy));
                break;

            case NORMALIZED:
                doJoinCommand(
                    new DeleteAllSelectedCommand(this, selectedStatesCopy, selectedTransitionsCopy),
                    new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
        }
        repaint();
    }
    
    /**
     * Copy the states and transitions currently selected by the user into a byte array via Java
     * serialization. The intent of this method is to provide a copy of a partial machine where the
     * graph structure of the partial machine is preserved, but no references to the original
     * machine are maintained. Returns null if the process fails, which should not occur.
     * @return A byte array representation of the states and transitions.
     */
    byte[] copySelectedToByteArray() 
    {
        try
        {
            ByteArrayOutputStream returner = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(returner);
            oos.writeObject(selectedStates);
            oos.writeObject(selectedTransitions);
            oos.flush();
            
            return returner.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
    }
    
    /**
     * Set which states are selected by the user.
     * @param states The states selected by the user.
     */
    public void setSelectedStates(HashSet<TM_State> states)
    {
        selectedStates = states;
    }
    
    /**
     * Set which transtions are selected by the user.
     * @param transitions The transitions selected by the user.
     */
    public void setSelectedTransitions(HashSet<TM_Transition> transitions)
    {
        selectedTransitions = transitions;
    }
   
    /**
     * Determine if the machine has been modified since its last save.
     * @return true if it has been modified since its last save, false otherwise.
     */
    public boolean isModifiedSinceSave()
    {
        return m_modifiedSinceSave;
    }
   
    /**
     * Set whether the machine has been modified since its last save.
     * @param isModified true if it has been modified since its last save, false otherwise.
     */
    public void setModifiedSinceSave(boolean isModified)
    {
        m_modifiedSinceSave = isModified;
        if (m_iFrame != null)
        {
            m_iFrame.updateTitle();
        }
    }
    
    /**
     * Determine if editing of the machine is enabled.
     * @return true if editing is enabled, false otherwise.
     */
    public boolean isEditingEnabled()
    {
        return m_editingEnabled;
    }
    
    /** 
     * Set if editing of the machine is enabled.
     * @param enabled true if editing is enabled, false otherwise.
     */
    public void setEditingEnabled(boolean enabled)
    {
        m_editingEnabled = enabled;
        m_keyboardEnabled = enabled;
    }
     
    /** 
     * Set the location where the last pasted item was placed. This is to prevent pasting multiple
     * items in the same place.
     * @param location The location where the last pasted item was placed.
     */
    public void setLastPastedLocation(Point2D location)
    {
        m_lastPastedLocation = location;
        m_numPastesToSameLocation = 1;
    }
    
    /**
     * Get the location of the last place we pasted to.
     * @return The location where the last pasted item was placed.
     */
    public Point2D getLastPastedLocation()
    {
        return m_lastPastedLocation;
    }
    
    /**
     * Increase the count of the number of times we've pasted to the same location on the canvas.
     */
    public void incrementNumPastesToSameLocation()
    {
        m_numPastesToSameLocation++;
    }
    
    /**
     * Get the count of the number of times we've pasted to the same location on the canvas.
     * @return The number of times an item has been pasted to the same location on the canvas.
     */
    public int getNumPastesToSameLocation()
    {
        return m_numPastesToSameLocation;
    }
    
    // Command handing:
    /**
     * Executes a command and adds it to the undo stack. Clears the redo stack also.
     * @param command The command to execute.
     */
    public void doCommand(TMCommand command)
    {
        command.doCommand();
        undoStack.add(command);
        redoStack.clear();
        setModifiedSinceSave(true);
        m_mainWindow.updateUndoActions();
        repaint();
    }

    /**
     * Convenience wrapper to doCommand(new JoinCommand(first, second)).
     * @param first The first command to run.
     * @param second The second command to run.
     */
    public void doJoinCommand(TMCommand first, TMCommand second)
    {
        doCommand(new JoinCommand(first, second));
    }
    
    /** 
     * Adds a command to the undo stack and clears the redo stack, but doesn't execute the command.
     * This is useful when the command has already been executed at the time of adding to the stack.
     * @param command The command to add to the stack.
     */
    public void addCommand(TMCommand command)
    {
        undoStack.add(command);
        redoStack.clear();
        m_mainWindow.updateUndoActions();
        repaint();
    }
    
    /** 
     * Undoes a command.
     */
    public void undoCommand()
    {
        try
        {
            TMCommand c = undoStack.removeLast();
            c.undoCommand();
            redoStack.add(c);
            setModifiedSinceSave(true);
            m_mainWindow.updateUndoActions();
            repaint();
        }
        catch (NoSuchElementException e) { }
    }
    
    /**
     * Redoes a command.
     */
    public void redoCommand()
    {
        try
        {
            TMCommand c = redoStack.removeLast();
            c.doCommand();
            undoStack.add(c);
            setModifiedSinceSave(true);
            m_mainWindow.updateUndoActions();
            repaint();
        }
        catch (NoSuchElementException e) { }
    }
    
    /** 
     * Returns the name of the command at the top of the undo stack or null if the stack is empty.
     * @return The name of the command at the top of the undo stack.
     */
    public String undoCommandName()
    {
        if (!undoStack.isEmpty())
        {
            return undoStack.getLast().getName();
        }
        return null;
    }
    
    /** 
     * Returns the name of the command at the top of the redo stack or null if the stack is empty.
     * @return The name at the top of the redo stack
     */
    public String redoCommandName()
    {
        if (!redoStack.isEmpty())
        {
            return redoStack.getLast().getName();
        }
        return null;
    }
   
    /**
     * Get the main window.
     * @return The main window.
     */
    public MainWindow getMainWindow()
    {
        return m_mainWindow;
    }
    
    /**
     * The current GUI mode.
     */
    private TM_GUI_Mode m_currentMode;

    /**
     * The set of selected states.
     */
    private HashSet<TM_State> selectedStates = new HashSet<TM_State>();

    /**
     * The set of selected transitions.
     */
    private HashSet<TM_Transition> selectedTransitions = new HashSet<TM_Transition>();

    /**
     * The start X ordinate of the selection marquee.
     */
    private int selectionBoxStartX = Integer.MIN_VALUE;
    
    /** 
     * The start Y ordinate of the selection marquee.
     */
    private int selectionBoxStartY = Integer.MIN_VALUE;

    /**
     * The end X ordinate of the selection marquee.
     */
    private int selectionBoxEndX = Integer.MIN_VALUE;

    /**
     * The end Y ordinate of the selection marquee.
     */
    private int selectionBoxEndY = Integer.MIN_VALUE;

    /**
     * Whether or not the user has made a marquee selection; determines if the values for
     * selectionBoxStartX, selectionBoxStartY, selectionBoxEndX, and selectionBoxEndY are valid.
     */
    private boolean madeSelection = false;

    /**
     * Whether or not a marquee selection is in progress.
     */
    private boolean selectionInProgress = false;

    /**
     * Whether or not selected items will be concatenated to the list of selected items, or the
     * previous selected items overwritten.
     */
    private boolean selectionConcatenateMode = false;

    /**
     * Whether or not the machine has been modified since the last save.
     */
    private boolean m_modifiedSinceSave = false;
    
    /**
     * The machine simulator. Exposes the machine and tape via .getMachine() and .getTape() respectively.
     */
    private TM_Simulator m_sim;
    
    /**
     * The underlying file.
     */
    private File m_file;

    /**
     * The owning frame.
     */
    private TMInternalFrame m_iFrame;
    
    /**
     * The main window.
     */
    private MainWindow m_mainWindow;
    
    /**
     *  The state we last pressed a mouse button on.
     */
    private TM_State mousePressedState = null;

    /**
     * The transition we last pressed a mouse button on
     */
    private TM_Transition mousePressedTransition = null;

    /**
     * The X ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    private int drawPosX = Integer.MIN_VALUE;

    /**
     * The Y ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    private int drawPosY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to the control point.
     */
    private int moveTransitionClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to the control point.
     */
    private int moveTransitionClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to state location.
     */
    private int moveStateClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to state location.
     */
    private int moveStateClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate of the last location of the moved state.
     */
    private int moveStateLastLocationX = Integer.MIN_VALUE;

    /**
     * Y ordinate of the last location of the moved state.
     */
    private int moveStateLastLocationY = Integer.MIN_VALUE;

    /**
     * X ordinate of the original position of the state, before movement.
     */
    private int moveStateStartLocationX = Integer.MIN_VALUE;
    
    /**
     * Y ordinate of the original position of the state, before movement.
     */
    private int moveStateStartLocationY = Integer.MIN_VALUE;

    /**
     * Whether a state has been moved.
     */
    private boolean movedState = false;

    /**
     * Cached list of transitions that finish at the state we are dragging.
     */
    private ArrayList<TM_Transition> m_transitionsToMoveState = null;

    /**
     * Cached list of transitions coming into all selected states.
     */
    private HashSet<TM_Transition> m_inTransitionsToMove = new HashSet<TM_Transition>();

    /**
     * Cached list of transitions leaving all selected states.
     */
    private HashSet<TM_Transition> m_outTransitionsToMove = new HashSet<TM_Transition>();

    /**
     * Intersection of m_inTransitionsToMove and m_outTransitionsToMove.
     */
    private HashSet<TM_Transition> m_TransitionsToMoveintersection = new HashSet<TM_Transition>();
    
    /**
     * Midpoint of the currently selected transition, before movement.
     */
    private Point2D transitionMidPointBeforeMove = null;

    /**
     * Whether a transition has been moved.
     */
    private boolean movedTransition = false;
    
    /**
     * Whether the keyboard is enabled.
     */
    private boolean m_keyboardEnabled = true;

    /**
     * Whether editing is enabled.
     */
    private boolean m_editingEnabled = true;

    /**
     * Set of labels in use.
     */
    private Hashtable<String,String> m_labelsUsed = new Hashtable<String,String>();
    
    /**
     * Stack containing commands which can be undone.
     */
    private LinkedList<TMCommand> undoStack = new LinkedList<TMCommand>();

    /**
     * Stack containing commands which can be redone.
     */
    private LinkedList<TMCommand> redoStack = new LinkedList<TMCommand>();
    
    /**
     * Bounding box of the currently selected transition action.
     */
    private Shape selectedSymbolBoundingBox = null;

    /**
     * The selected transition.
     */
    private TM_Transition selectedTransition = null;

    /**
     * Whether a transition action has been selected.
     */
    private boolean inputSymbolSelected = false;
    
    /**
     * Last location a value was pasted.
     */
    private Point2D m_lastPastedLocation = null;

    /**
     * How many times an object was pasted to the last pasted location.
     */
    private int m_numPastesToSameLocation = 0;
}
