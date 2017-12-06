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

/**
 * The canvas for drawing a machine state diagram.
 */
public abstract class MachineGraphicsPanel<
    PREACTION extends PreAction,
    TRANSITION extends Transition<PREACTION, STATE, MACHINE, SIMULATOR>,
    STATE extends State<PREACTION, TRANSITION, MACHINE, SIMULATOR>,
    MACHINE extends Machine<PREACTION, TRANSITION, STATE, SIMULATOR>,
    SIMULATOR extends Simulator<PREACTION, TRANSITION, STATE, MACHINE>> extends JPanel
{
    private final BasicStroke dashed = new BasicStroke(
            1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f }, 0.0f);

    private final Font PANEL_FONT = new Font("Monospaced", Font.PLAIN, 14);

    /**
     * Determine if this panel is opaque; allows for optomization by Swing.
     * @return true in all cases.
     */
    public final boolean isOpaque()
    {
        return true;
    }

    /**
     * Set the user interface interaction mode for this panel. This determines the result of a click
     * in the panel.
     * @param currentMode The new GUI mode.
     */
    public void setUIMode(GUI_Mode currentMode)
    {
        m_currentMode = currentMode;
    }

    /**
     * Get the alphabet for the machine associated with this panel.
     * @return The alphabet for the machine.
     */
    public Alphabet getAlphabet()
    {
        return getSimulator().getMachine().getAlphabet();
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

        STATE currentState = getSimulator().getCurrentState();
        if (currentState != null)
        {
            g2d.setColor(Color.YELLOW);
            g2d.fill(new Ellipse2D.Float(currentState.getX() - 5, currentState.getY() - 5, STATE.STATE_RENDERING_WIDTH + 10, STATE.STATE_RENDERING_WIDTH + 10));
            g2d.setColor(Color.BLACK);
            g2d.draw(new Ellipse2D.Float(currentState.getX() - 5, currentState.getY() - 5, STATE.STATE_RENDERING_WIDTH + 10, STATE.STATE_RENDERING_WIDTH + 10));
        }

        getSimulator().getMachine().paint(g, selectedStates, selectedTransitions, getSimulator());
        if (m_currentMode == GUI_Mode.ADDTRANSITIONS && mousePressedState != null)
        {
            if (!(drawPosX == Integer.MIN_VALUE) || !(drawPosY == Integer.MIN_VALUE))
            {
                g2d.setColor(Color.BLACK);
                g2d.draw(new Line2D.Float(mousePressedState.getX() + STATE.STATE_RENDERING_WIDTH/2,mousePressedState.getY()+ STATE.STATE_RENDERING_WIDTH/2, drawPosX, drawPosY));
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
    protected void initialization()
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

                        if (m_currentMode != GUI_Mode.ERASER)
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
                        if (getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY())!= null ||
                                getSimulator().getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics()) != null)
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
                        if (m_currentMode != GUI_Mode.ADDTRANSITIONS)
                        {
                            if (mousePressedState != null)
                            {
                                handleStateDrag(e);
                                repaintNeeded = true;
                            }
                            else if (m_currentMode == GUI_Mode.SELECTION && selectionInProgress)
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
     * Handle when a mouse click occurs over the label of a state, by bringing up a dialog box
     * requesting the new value for the label.
     * @param e The generating event.
     * @return true if the mouse click created a dialog box, false otherwise.
     */
    protected boolean clickStateName(MouseEvent e)
    {
        STATE nameClicked = getSimulator().getMachine().getStateLabelClickedOn(getGraphics(),e.getX(), e.getY());
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
        if (m_labelsUsed.contains(result))
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
    protected boolean selectCharacterByClicking(MouseEvent e)
    {
        TRANSITION transitionClicked = getSimulator().getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
     * Delete a state from the machine.
     * @param s The state to delete.
     */
    public void deleteState(STATE s)
    {
        switch (getSimulator().getMachine().getNamingScheme())
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
    public void deleteTransition(TRANSITION t)
    {
        doCommand(new DeleteTransitionCommand(this, t));
    }

    /** 
     * Handle when a mouse button is pressed. Determines selected states or transitions.
     * @param e The generating event.
     */
    protected void handleMousePressed(MouseEvent e)
    {
        if ((e.isControlDown() || e.isShiftDown()))
        {
            // We don't want to start creating a new transition in this case
            mousePressedState = null;
        }
        else
        {
            mousePressedState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
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
            m_transitionsToMoveState = getSimulator().getMachine().getTransitionsTo(mousePressedState);
            precomputeSelectedTransitionsToDrag();
            return;
        }
        else
        {
            mousePressedTransition = getSimulator().getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
        if (m_currentMode == GUI_Mode.SELECTION)
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
    protected abstract void handleMouseReleased(MouseEvent e); // !!!


    /**
     * Handle when a mouse drag occurs while in selection mode. Moves a transition relative to mouse
     * movement.
     * @param e The generating event.
     */
    protected void handleTransitionDrag(MouseEvent e)
    {
        // Update control point location
        // TODO: enforce area bounds?
        movedTransition = true;

        // Find the midpoint by correcting for the offset of where the user clicked
        double correctedMidpointX = e.getX() + moveTransitionClickOffsetX; 
        double correctedMidpointY = e.getY() + moveTransitionClickOffsetY;

        Point2D newCP = Spline.getControlPointFromMidPoint(
                new Point2D.Double(correctedMidpointX, correctedMidpointY),
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
    protected void handleStateDrag(MouseEvent e)
    {
        int newX = e.getX() + moveStateClickOffsetX;
        int newY = e.getY() + moveStateClickOffsetY;

        // Check that this is within panel bounds.
        // This is complicated in the case where multiple items are selected
        Dimension boundaries = getSize();
        int minY = 0;
        int minX = 0;
        int maxX = (int)boundaries.getWidth() - STATE.STATE_RENDERING_WIDTH;
        int maxY = (int)boundaries.getHeight() - STATE.STATE_RENDERING_WIDTH;

        if (selectedStates.contains(mousePressedState)) // Clicked on a selected state
        {
            int rightMostX = mousePressedState.getX();
            int leftMostX = mousePressedState.getX();
            int bottomMostY = mousePressedState.getY();
            int topMostY = mousePressedState.getY();
            for (STATE s : selectedStates)
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
            for (STATE s : selectedStates)
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
    protected void updateSelectedSymbolBoundingBox()
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
    protected void updateSelectedStatesAndTransitions()
    {
        int topLeftX = Math.min(selectionBoxStartX, selectionBoxEndX);
        int topLeftY = Math.min(selectionBoxStartY, selectionBoxEndY);
        int width = Math.abs(selectionBoxStartX - selectionBoxEndX);
        int height = Math.abs(selectionBoxStartY - selectionBoxEndY);

        HashSet<STATE> states = getSimulator().getMachine().getSelectedStates(topLeftX, topLeftY, width, height);

        if (selectionConcatenateMode)
        {
            selectedStates.addAll(states);
        }
        else
        {
            selectedStates = states;
        }
        selectedTransitions = getSimulator().getMachine().getSelectedTransitions(selectedStates);
    }

    /**
     * Precompute transitions that should be moved in a drag event.
     */
    protected void precomputeSelectedTransitionsToDrag()
    {
        m_inTransitionsToMove = new HashSet<TRANSITION>();
        m_outTransitionsToMove = new HashSet<TRANSITION>();
        calcMovedTransitionSets(m_inTransitionsToMove, m_outTransitionsToMove);

        m_TransitionsToMoveintersection = new HashSet<TRANSITION>();
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
    protected void pullSelectedTransitionsWithState(int translateX, int translateY)
    {
        double halfOfTranslatedX = translateX / 2.0;
        double halfOfTranslatedY = translateY / 2.0;

        for (TRANSITION t : m_TransitionsToMoveintersection)
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

            Point2D newCP = Spline.getControlPointFromMidPoint(midpoint, t.getFromState(), t.getToState());
            t.setControlPoint((int)(newCP.getX()), (int)(newCP.getY()));  
        }

        for (TRANSITION t : m_inTransitionsToMove)
        {
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
        for (TRANSITION t : m_outTransitionsToMove)
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
    public static void updateTransitionLocations(State mousePressedState, int translateX, int translateY,
            Collection<? extends Transition> transitionsInto, Collection<? extends Transition> transitionsOut)
    {
        for (Transition t : transitionsOut)
        {
            if (t.getFromState() == t.getToState())
            {
                Point2D cp = t.getControlPoint();
                t.setControlPoint((int)(cp.getX() + translateX), (int)(cp.getY() + translateY));
                continue;
            }
            updateTransitionLocationWhenStateMoved(t, translateX, translateY);
        }
        for (Transition t : transitionsInto)
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
    public static void updateTransitionLocationWhenStateMoved(Transition t, int translateX, int translateY)
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
    protected void calcMovedTransitionSets(HashSet<TRANSITION> inTransitions,
            HashSet<TRANSITION> outTransitions)
    {
        for (STATE s : selectedStates)
        {
            Collection<TRANSITION> out = s.getTransitions();
            outTransitions.addAll(out);
            Collection<TRANSITION> in = getSimulator().getMachine().getTransitionsTo(s);
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
    protected void updateTitle()
    {
        if (m_iFrame != null)
        {
            m_iFrame.updateTitle();
        }
    }

    /** 
     * Find the first unused standard state label in the machine. Standard labels are 'q' followed
     * by a non-negative integer.
     * NOTE: Potentially should be abstract.
     * @return The first unused standard state label.
     */
    public String getFirstFreeName()
    {
        switch (getSimulator().getMachine().getNamingScheme())
        {
            case GENERAL:
                int current = 0;
                while (m_labelsUsed.contains("q" + current))
                {
                    current++;
                }
                return "q" + current;

            case NORMALIZED:
                // Assume every state name is normalized, hence no naming conflicts
                return "" + getSimulator().getMachine().getStates().size();

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
        return m_labelsUsed.contains(name);
    }

    /** 
     * Adds a state name label to the dictionary of labels that have been used and cannot be used
     * again, unless deleted.
     * @param label The label to be add to the used list.
     */
    public void addLabelToDictionary(String label)
    {
        m_labelsUsed.add(label);
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
    public void setWindow(MachineInternalFrame iFrame)
    {
        m_iFrame = iFrame;
        updateTitle();
    }

    /**
     * Get the internal frame for this panel.
     * @return The internal frame for this panel.
     */
    public MachineInternalFrame getWindow()
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
    public TRANSITION getSelectedTransition()
    {
        return selectedTransition;
    }

    /**
     * Get the set of transitions selected by the user.
     * @return The set of transitions selected by the user.
     */
    public HashSet<TRANSITION> getSelectedTransitions()
    {
        return selectedTransitions;
    }

    /**
     * Get the set of states selected by the user.
     * @return The set of states selected by the user.
     */
    public HashSet<STATE> getSelectedStates()
    {
        return selectedStates;
    }

    /**
     * Delete all states and transitions that the user has currently selected.
     */
    public void deleteAllSelected()
    {
        HashSet<STATE> selectedStatesCopy = (HashSet<STATE>)selectedStates.clone();
        HashSet<TRANSITION> selectedTransitionsCopy = (HashSet<TRANSITION>)selectedTransitions.clone();
        switch (getSimulator().getMachine().getNamingScheme())
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
    public void setSelectedStates(HashSet<STATE> states)
    {
        selectedStates = states;
    }

    /**
     * Set which transtions are selected by the user.
     * @param transitions The transitions selected by the user.
     */
    public void setSelectedTransitions(HashSet<TRANSITION> transitions)
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
     * Get the simulator object for the machine associated with this panel.
     * @return The simulator object for the machine.
     */
    public abstract SIMULATOR getSimulator();

    /** 
     * Accept a KeyEvent detected in the main window, and use it to update any transition action
     * selected by the user.
     * @param e The generating event.
     * @return true if a transition action was selected and updated, false otherwise.
     */
    public abstract boolean handleKeyEvent(KeyEvent e);

    /**
     * Handle when a mouse click occurs over a state, by either selecting the existing underlying
     * state, or creating a new state.
     * @param e The generating event.
     */
    protected abstract void handleAddNodesClick(MouseEvent e);

    /** 
     * Handle when a mouse click occurs while in eraser mode. If the mouse click occurs over a
     * state, it is deleted, and if it is over a transition, that is deleted.
     * @param e The generating event.
     */
    public abstract void handleEraserClick(MouseEvent e);

    /**
     * Handle when a mouse click occurs while in select start state mode. If the mouse click occurs
     * over a state, the start state of the machine is changed.
     * @param e The generating event.
     */
    protected abstract void handleChooseStartClick(MouseEvent e);

    /**
     * Handle when a mouse click occurs while in select accepting state mode. If the mouse click
     * occurs over a state, the accepting state of the machine is changed.
     * @param e The generating event.
     */
    protected abstract void handleChooseAcceptingClick(MouseEvent e);

    /**
     * Handle when a mouse click occurs while in selection mode. If the mouse click occurs over a
     * state, the state is either added or removed from the selected state set, depending on context.
     * @param e The generating event.
     */
    protected abstract void handleSelectionClick(MouseEvent e);

    /**
     * Handle when a mouse click occurs while in current state selection mode. If the mouse click
     * occurs over a state, the state is made to be the current state.
     * @param e The generating event.
     */
    protected abstract void handleChooseCurrentState(MouseEvent e);

    /**
     * The current GUI mode.
     */
    protected GUI_Mode m_currentMode;

    /**
     * The set of selected states.
     */
    protected HashSet<STATE> selectedStates = new HashSet<STATE>();

    /**
     * The set of selected transitions.
     */
    protected HashSet<TRANSITION> selectedTransitions = new HashSet<TRANSITION>();

    /**
     * The start X ordinate of the selection marquee.
     */
    protected int selectionBoxStartX = Integer.MIN_VALUE;

    /** 
     * The start Y ordinate of the selection marquee.
     */
    protected int selectionBoxStartY = Integer.MIN_VALUE;

    /**
     * The end X ordinate of the selection marquee.
     */
    protected int selectionBoxEndX = Integer.MIN_VALUE;

    /**
     * The end Y ordinate of the selection marquee.
     */
    protected int selectionBoxEndY = Integer.MIN_VALUE;

    /**
     * Whether or not the user has made a marquee selection; determines if the values for
     * selectionBoxStartX, selectionBoxStartY, selectionBoxEndX, and selectionBoxEndY are valid.
     */
    protected boolean madeSelection = false;

    /**
     * Whether or not a marquee selection is in progress.
     */
    protected boolean selectionInProgress = false;

    /**
     * Whether or not selected items will be concatenated to the list of selected items, or the
     * previous selected items overwritten.
     */
    protected boolean selectionConcatenateMode = false;

    /**
     * Whether or not the machine has been modified since the last save.
     */
    protected boolean m_modifiedSinceSave = false;

    /**
     * The underlying file.
     */
    protected File m_file;

    /**
     * The owning frame.
     */
    protected MachineInternalFrame m_iFrame;

    /**
     * The main window.
     */
    protected MainWindow m_mainWindow;

    /**
     *  The state we last pressed a mouse button on.
     */
    protected STATE mousePressedState = null;

    /**
     * The transition we last pressed a mouse button on
     */
    protected TRANSITION mousePressedTransition = null;

    /**
     * The X ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    protected int drawPosX = Integer.MIN_VALUE;

    /**
     * The Y ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    protected int drawPosY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to the control point.
     */
    protected int moveTransitionClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to the control point.
     */
    protected int moveTransitionClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to state location.
     */
    protected int moveStateClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to state location.
     */
    protected int moveStateClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate of the last location of the moved state.
     */
    protected int moveStateLastLocationX = Integer.MIN_VALUE;

    /**
     * Y ordinate of the last location of the moved state.
     */
    protected int moveStateLastLocationY = Integer.MIN_VALUE;

    /**
     * X ordinate of the original position of the state, before movement.
     */
    protected int moveStateStartLocationX = Integer.MIN_VALUE;

    /**
     * Y ordinate of the original position of the state, before movement.
     */
    protected int moveStateStartLocationY = Integer.MIN_VALUE;

    /**
     * Whether a state has been moved.
     */
    protected boolean movedState = false;

    /**
     * Cached list of transitions that finish at the state we are dragging.
     */
    protected ArrayList<TRANSITION> m_transitionsToMoveState = null;

    /**
     * Cached list of transitions coming into all selected states.
     */
    protected HashSet<TRANSITION> m_inTransitionsToMove = new HashSet<TRANSITION>();

    /**
     * Cached list of transitions leaving all selected states.
     */
    protected HashSet<TRANSITION> m_outTransitionsToMove = new HashSet<TRANSITION>();

    /**
     * Intersection of m_inTransitionsToMove and m_outTransitionsToMove.
     */
    protected HashSet<TRANSITION> m_TransitionsToMoveintersection = new HashSet<TRANSITION>();

    /**
     * Midpoint of the currently selected transition, before movement.
     */
    protected Point2D transitionMidPointBeforeMove = null;

    /**
     * Whether a transition has been moved.
     */
    protected boolean movedTransition = false;

    /**
     * Whether the keyboard is enabled.
     */
    protected boolean m_keyboardEnabled = true;

    /**
     * Whether editing is enabled.
     */
    protected boolean m_editingEnabled = true;

    /**
     * Set of labels in use.
     */
    protected HashSet<String> m_labelsUsed = new HashSet<String>();

    /**
     * Stack containing commands which can be undone.
     */
    protected LinkedList<TMCommand> undoStack = new LinkedList<TMCommand>();

    /**
     * Stack containing commands which can be redone.
     */
    protected LinkedList<TMCommand> redoStack = new LinkedList<TMCommand>();

    /**
     * Bounding box of the currently selected transition action.
     */
    protected Shape selectedSymbolBoundingBox = null;

    /**
     * The selected transition.
     */
    protected TRANSITION selectedTransition = null;

    /**
     * Whether a transition action has been selected.
     */
    protected boolean inputSymbolSelected = false;

    /**
     * Last location a value was pasted.
     */
    protected Point2D m_lastPastedLocation = null;

    /**
     * How many times an object was pasted to the last pasted location.
     */
    protected int m_numPastesToSameLocation = 0;
}
