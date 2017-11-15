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
     * Creates a new instance of TMGraphicsPanel. The input variables must not be null, except for file.
     */
    public TMGraphicsPanel(TMachine machine, Tape tape, File file, MainWindow mainWindow)
    {
        m_machine = machine;
        m_tape = tape;
        m_sim = new TM_Simulator(machine, tape);
        m_file = file;
        m_labelsUsed = machine.createLabelsHashtable();
        m_mainWindow = mainWindow;
        initialization();
    }
    
    // Allow Swing to optimize painting by letting it know that we fill our entire bounds.
    public boolean isOpaque()
    {
        return true;
    }
    
    /**
     * Set the user interface interaction mode for this panel.
     * Determines the result of user interaction such as clicking.
     */
    public void setUIMode(TM_GUI_Mode currentMode)
    {
        m_currentMode = currentMode;
    }
    
    /**
     * Get the simulator object for the machine associated with this panel.
     */
    public TM_Simulator getSimulator()
    {
        return m_sim;
    }
    
    /**
     * Get the alphabet for the machine associated with this panel.
     */
    public Alphabet getAlphabet()
    {
        return m_machine.getAlphabet();
    }

    /**
     * Render to a graphics object.
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

        m_machine.paint(g, selectedStates, selectedTransitions, m_sim);
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
     * Setup stuff for TMGraphicsPanel. Should only be called by the constructor.
     */
    private void initialization()
    {
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
                
                if (m_currentMode != TM_GUI_Mode.ERASER && m_currentMode != TM_GUI_Mode.CHOOSENEXTTRANSITION)
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
                    case CHOOSENEXTTRANSITION:
                    {
                        handleChooseNextTransitionClick(e);
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
                if (m_machine.getStateClickedOn(e.getX(), e.getY())!= null ||
                    m_machine.getTransitionClickedOn(e.getX(), e.getY(), getGraphics()) != null)
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
     * selected by the user.  Returns true IFF a transition action was selected and hence has been updated.
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
                        doCommand(new ModifyTransitionActionCommand(this, selectedTransition, new TM_Action(-1, selectedTransition.getSymbol(), c)));
                    }
                    else
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                    {
                        doCommand(new ModifyTransitionActionCommand(this, selectedTransition, new TM_Action(1, selectedTransition.getSymbol(), c)));
                    }
                }
                else if (c == 'E' && e.isShiftDown()) //shift + E makes an epsilon transition
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition, new TM_Action(0, selectedTransition.getSymbol(), TMachine.EMPTY_ACTION_SYMBOL)));
                }
                else if (Character.isLetterOrDigit(c)  && getAlphabet().containsSymbol(c))
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition, new TM_Action(0, selectedTransition.getSymbol(), c)));
                }
                else if ((c == ' ' || c == Tape.BLANK_SYMBOL) && getAlphabet().containsSymbol(Tape.BLANK_SYMBOL))
                {
                    doCommand(new ModifyTransitionActionCommand(this, selectedTransition, new TM_Action(0, selectedTransition.getSymbol(), Tape.BLANK_SYMBOL)));
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
     * Handles the case where a mouse click lands on the name of a state, by bringing up a dialog
     * box request for a new name for the state.
     * @returns true IFF the mouse click triggered the dialog box and thus
     *     should not be considered for further use.
     */
    private boolean clickStateName(MouseEvent e)
    {
        TM_State nameClicked = m_machine.getStateNameClickedOn(getGraphics(),e.getX(), e.getY());
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
     * Update the user selection of action characters for transitions
     * following a mouse click event.
     * @param e    The mouse click event to process.
     * @returns true IFF the mouse click triggered a selection and thus
     *      should not be considered for further use.
     */
    private boolean selectCharacterByClicking(MouseEvent e)
    {
        TM_Transition transitionClicked = m_machine.getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
     * Handle a mouse click in add nodes mode.
     */
    private void handleAddNodesClick(MouseEvent e)
    {
        if (m_machine.getStateClickedOn(e.getX(), e.getY()) != null)
        {
            // Adding states on top of states is not allowed
            handleSelectionClick(e);
            return;
        }
        
        String label = getFirstFreeName();
        doCommand(new AddStateCommand(this, new TM_State(label,false, false,
                e.getX() - TM_State.STATE_RENDERING_WIDTH / 2,
                e.getY() - TM_State.STATE_RENDERING_WIDTH / 2)));
    }
    
    /** 
     * Handle a mouse click in eraser mode.  Delete a transition or state that was clicked on, if any.
     */
    public void handleEraserClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_machine.getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            deleteState(stateClickedOn);
        }
        else
        {
            TM_Transition transitionClickedOn = m_machine.getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
            if (transitionClickedOn != null)
            {
                deleteTransition(transitionClickedOn);
            }
        }
    }
    
    public void deleteState(TM_State s)
    {
        doCommand(new DeleteStateCommand(this, s));
    }
    
    public void deleteTransition(TM_Transition t)
    {
        doCommand(new DeleteTransitionCommand(this, t));
    }
    
    /**
     * Handle a mouse click in choose start state mode.
     */
    private void handleChooseStartClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_machine.getStateClickedOn(e.getX(), e.getY());

        if (stateClickedOn != null)
        {
            doCommand(new ToggleStartStateCommand(this, m_machine.getStartState(), stateClickedOn));
        }
    }
    
    /**
     * Handle a mouse click in choose start state mode.
     */
    private void handleChooseAcceptingClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_machine.getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            doCommand(new ToggleAcceptingStateCommand(this, m_machine.getFinalState(), stateClickedOn));
        }
    }
    
    /**
     * Handle a mouse click in selection mode.
     */
    private void handleSelectionClick(MouseEvent e)
    {
        TM_State stateClickedOn = m_machine.getStateClickedOn(e.getX(), e.getY());
        
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
        selectedTransitions = m_machine.getSelectedTransitions(selectedStates);
    }
    
    private void handleChooseNextTransitionClick(MouseEvent e)
    {
        TM_Transition transitionClickedOn = m_machine.getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
        if (transitionClickedOn != null)
        {
            if (m_tape != null)
            {
                if (transitionClickedOn.getSymbol() != m_tape.read())
                {
                    // Can't select this one
                    return;
                }
            }
            this.getSimulator().setCurrentNextTransition(transitionClickedOn);
            repaint();
        }
    }
    
    private void handleChooseCurrentState(MouseEvent e)
    {
        TM_State stateClickedOn = m_machine.getStateClickedOn(e.getX(), e.getY());
        
        if (stateClickedOn != null)
        {
            m_sim.setCurrentState(stateClickedOn);
        }
        
    }
    
    /** 
     * Handle a mouse button pressed event. Determines selected states or transitions to prepare for
     * mouse dragging.
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
            mousePressedState = m_machine.getStateClickedOn(e.getX(), e.getY());
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
            m_transitionsToMoveState = m_machine.getTransitionsTo(mousePressedState);
            precomputeSelectedTransitionsToDrag();
            return;
        }
        else
        {
            mousePressedTransition = m_machine.getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
     * Handles a mouse button released event.  Creates any new transitions if a transition creation
     * drag has occurred, and resets mouse pressed states and transitions.
     */
    private void handleMouseReleased(MouseEvent e)
    {
        if (m_currentMode == TM_GUI_Mode.ADDTRANSITIONS && mousePressedState != null)
        {
            TM_State mouseReleasedState = m_machine.getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                doCommand(new AddTransitionCommand(this,
                        new TM_Transition(mousePressedState, mouseReleasedState,
                        TMachine.OTHERWISE_SYMBOL, new TM_Action(0,
                        TMachine.OTHERWISE_SYMBOL, TMachine.EMPTY_ACTION_SYMBOL))));

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
     * Update our sets of selected states and transitions.  Call this when the user selection region
     * (the draggable region, not the symbol selection) has been moved.
     */
    private void updateSelectedStatesAndTransitions()
    {
        int topLeftX = Math.min(selectionBoxStartX, selectionBoxEndX);
        int topLeftY = Math.min(selectionBoxStartY, selectionBoxEndY);
        int width = Math.abs(selectionBoxStartX - selectionBoxEndX);
        int height = Math.abs(selectionBoxStartY - selectionBoxEndY);
        
        HashSet<TM_State> states = m_machine.getSelectedStates(topLeftX, topLeftY, width, height);
        
        if (selectionConcatenateMode)
        {
            selectedStates.addAll(states);
        }
        else
        {
            selectedStates = states;
        }
        selectedTransitions = m_machine.getSelectedTransitions(selectedStates);
    }
    
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
     * When a single state is moved by the user, update the control point locations of all of the
     * transitions to and from that state.  Must be called before the actual state locations are updated.
     * @pre m_transitionsToMoveState contains the transitions into mousePressedState.
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
    
    private void calcMovedTransitionSets(HashSet<TM_Transition> inTransitions,
            HashSet<TM_Transition> outTransitions)
    {
        for (TM_State s : selectedStates)
        {
            ArrayList<TM_Transition> out = s.getTransitions();
            outTransitions.addAll(out);
            ArrayList<TM_Transition> in = m_machine.getTransitionsTo(s);
            inTransitions.addAll(in);
        }
    }
    
    public File getFile()
    {
        return m_file;
    }
    
    public void setFile(File f)
    {
        m_file = f;
        updateTitle();
    }
    
    private void updateTitle()
    {
        if (m_iFrame != null)
        {
            m_iFrame.updateTitle();
        }
    }
    
    /** 
     * Find the first unused standard state label in the machine.  Standard labels are 'q' followed
     * by a non-negative integer.
     */
    public String getFirstFreeName()
    {
       int current = 0;
       while (m_labelsUsed.containsKey("q" + current))
           current++;
       String returner = "q" + current;
       // m_labelsUsed.put(returner, returner);
       return returner;
    }
    
    public boolean dictionaryContainsName(String name)
    {
        return m_labelsUsed.containsKey(name);
    }
    
    /** 
     * Adds a state name label to the dictionary of labels that have been used and cannot be used
     * again (unless deleted).
     */
    public void addLabelToDictionary(String label)
    {
        m_labelsUsed.put(label, label);
    }
    
    /**
     * Removes a state name label from the dictionary of labels that have been used and cannot be
     * used again (unless deleted).
     */
    public void removeLabelFromDictionary(String label)
    {
        m_labelsUsed.remove(label);
    }
    
    public void setWindow(TMInternalFrame iFrame)
    {
        m_iFrame = iFrame;
        updateTitle();
    }
    
    public TMInternalFrame getWindow()
    {
        return m_iFrame;
    }
    
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
     * or null if there isn't one.
     */
    public TM_Transition getSelectedTransition()
    {
        return selectedTransition;
    }
    
    public HashSet<TM_Transition> getSelectedTransitions()
    {
        return selectedTransitions;
    }
    
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
        doCommand(new DeleteAllSelectedCommand(this, selectedStatesCopy,
                selectedTransitionsCopy));

        repaint();
    }
    
    /**
     * Copy the states and transitions currently selected by the user into a byte array via Java
     * serialization.  The intent of this method is to provide a copy of a partial machine where the
     * graph structure of the partial machine is preserved, but no references to the original
     * machine are maintained.  Java serialization is a useful mechanism for achieving this.
     * Returns null if the process fails. That shouldnt happen, though.
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
     * @pre All of the elements of states are elements of the machine.
     */
    public void setSelectedStates(HashSet<TM_State> states)
    {
        selectedStates = states;
    }
    
    /**
     * Set which transtions are selected by the user.
     * @pre All of the elements of transitions are elements of the machine.
     */
    public void setSelectedTransitions(HashSet<TM_Transition> transitions)
    {
        selectedTransitions = transitions;
    }
    
    public boolean isModifiedSinceSave()
    {
        return m_modifiedSinceSave;
    }
    
    public void setModifiedSinceSave(boolean isModified)
    {
        m_modifiedSinceSave = isModified;
        if (m_iFrame != null)
        {
            m_iFrame.updateTitle();
        }
    }
    
    /**
     * Returns true IFF editing operations such as cut copy paste delete or changing of the machines
     * or tapes is currently allowed.
     */
    public boolean isEditingEnabled()
    {
        return m_editingEnabled;
    }
    
    /** 
     * Enable/disable editing operations such as cut copy paste delete or changing of the machines
     * or tapes.
     */
    public void setEditingEnabled(boolean enabled)
    {
        m_editingEnabled = enabled;
        m_keyboardEnabled = enabled;
    }
     
    /** 
     * Store the location of the last place we pasted to, in order to help avoid pasting items in
     * the same place when pasting repeatedly. Also sets the count of pastes to the location to 1.
     */
    public void setLastPastedLocation(Point2D location)
    {
        m_lastPastedLocation = location;
        m_numPastesToSameLocation = 1;
    }
    
    /**
     * Get the location of the last place we pasted to.
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
     */
    public int getNumPastesToSameLocation()
    {
        return m_numPastesToSameLocation;
    }
    
    // Command handing:
    /**
     * Executes a command and adds it to the undo stack. Clears the redo stack also.
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
     * Adds a command to the undo stack and clears the redo stack, but doesn't execute the command.
     * This is useful when the command has already been executed at the time of adding to the stack.
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
     */
    public String redoCommandName()
    {
        if (!redoStack.isEmpty())
        {
            return redoStack.getLast().getName();
        }
        return null;
    }
    
    public MainWindow getMainWindow()
    {
        return m_mainWindow;
    }
    
    private TMachine m_machine;
    private Tape m_tape;
    private TM_GUI_Mode m_currentMode;
    
    private HashSet<TM_State> selectedStates = new HashSet<TM_State>();
    private HashSet<TM_Transition> selectedTransitions = new HashSet<TM_Transition>();
    private int selectionBoxStartX = Integer.MIN_VALUE;
    private int selectionBoxStartY = Integer.MIN_VALUE;
    private int selectionBoxEndX = Integer.MIN_VALUE;
    private int selectionBoxEndY = Integer.MIN_VALUE;
    private boolean madeSelection = false;
    private boolean selectionInProgress = false;
    private boolean selectionConcatenateMode = false;
    private boolean m_modifiedSinceSave = false;
    
    private TM_Simulator m_sim;
    
    private File m_file;
    private TMInternalFrame m_iFrame;
    
    private MainWindow m_mainWindow;
    
    // State information for clicking and dragging interactions with the panel:
    private TM_State mousePressedState = null; // The state we last pressed the mouse down on.
    private TM_Transition mousePressedTransition = null; // The transition we last pressed the mouse down on.
    private int drawPosX = Integer.MIN_VALUE; // For drawing lines to display potential transitions as the mouse is dragged/
    private int drawPosY = Integer.MIN_VALUE;
    private int moveTransitionClickOffsetX = Integer.MIN_VALUE; // Vector from mouse click to control point
    private int moveTransitionClickOffsetY = Integer.MIN_VALUE;
    private int moveStateClickOffsetX = Integer.MIN_VALUE; // Vector from mouse click to state location
    private int moveStateClickOffsetY = Integer.MIN_VALUE;
    private int moveStateLastLocationX = Integer.MIN_VALUE;
    private int moveStateLastLocationY = Integer.MIN_VALUE;
    private int moveStateStartLocationX = Integer.MIN_VALUE; // The location of a state at the start of its movement
    private int moveStateStartLocationY = Integer.MIN_VALUE; // When it's dragged.
    private boolean movedState = false;
    private ArrayList<TM_Transition> m_transitionsToMoveState = null; // Cached list of transitions that finish
                                                                      // at a state we are dragging.
    private HashSet<TM_Transition> m_inTransitionsToMove = new HashSet<TM_Transition>();
    private HashSet<TM_Transition> m_outTransitionsToMove = new HashSet<TM_Transition>();
    HashSet<TM_Transition> m_TransitionsToMoveintersection = new HashSet<TM_Transition>();
    
    private Point2D transitionMidPointBeforeMove = null;
    private boolean movedTransition = false;
    
    private boolean m_keyboardEnabled = true; // Used to temporarily disable keyboard processing,
                                              //for instance when a dialog box is open.
    private boolean m_editingEnabled = true; // Used for temporarily disabling editing operations
    private Hashtable<String,String> m_labelsUsed = new Hashtable<String,String>();
    
    // undo/redo stacks
    private LinkedList<TMCommand> undoStack = new LinkedList<TMCommand>();
    private LinkedList<TMCommand> redoStack = new LinkedList<TMCommand>();
    
    // Variables for storing the transition action currently selected by the user.
    private Shape selectedSymbolBoundingBox = null;
    private TM_Transition selectedTransition = null;
    private boolean inputSymbolSelected = false;
    
    // Used to facilitate prevention stacked objects when pasting
    // to the same spot
    private Point2D m_lastPastedLocation = null;
    private int m_numPastesToSameLocation = 0; // How many times we pasted to lastPastedLocation.
}
