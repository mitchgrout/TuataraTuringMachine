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
     * Creates a new instance of MachineGraphicsPanel.
     * @param sim The simulator, containing the machine to render, and tape.
     * @param file The file the machine is associated with.
     */
    public MachineGraphicsPanel(SIMULATOR sim, File file)
    {
        // Setup
        m_sim  = sim;
        m_file = file;
        m_labelsUsed = m_sim.getMachine().getLabelHashset();

        // Create our context menu
        m_contextMenu = new JPopupMenu();
        m_contextMenu.add(new RenameStateAction("Rename State"));
    }

    /**
     * Determine if this panel is opaque; allows for optomization by Swing.
     * @return true in all cases.
     */
    public final boolean isOpaque()
    {
        return true;
    }

    /**
     * Get the internal frame for this panel.
     * @return The internal frame for this panel.
     */
    public MachineInternalFrame getFrame()
    {
        return m_iFrame;
    }

    /**
     * Set the internal frame for this panel.
     * @param iFrame The new internal frame.
     */
    public void setFrame(MachineInternalFrame iFrame)
    {
        m_iFrame = iFrame;
        updateTitle();
    }

    /**
     * Get the simulator associated with this panel.
     * @return The simulator for this panel.
     */
    public SIMULATOR getSimulator()
    {
        return m_sim;
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
     * Get the filename associated with the machine. If getFile() is null, then this value is a
     * temporary name for the machine.
     * @return The filename associated with the machine.
     */
    public String getFilename()
    {
        if (m_file == null)
        {
            return String.format("untitled-%d", m_iFrame.getIndex()); 
        }
        else
        {
            return m_file.getName();
        }
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
     * Set the user interface interaction mode for this panel. This determines the result of a click
     * in the panel.
     * @param currentMode The new GUI mode.
     */
    public void setUIMode(GUI_Mode currentMode)
    {
        m_currentMode = currentMode;
    }

    /**
     * Get the set of states selected by the user.
     * @return The set of states selected by the user.
     */
    public HashSet<STATE> getSelectedStates()
    {
        return m_selectedStates;
    }

    /**
     * Set which states are selected by the user.
     * @param states The states selected by the user.
     */
    public void setSelectedStates(HashSet<STATE> states)
    {
        m_selectedStates = states;
    }

    /**
     * Get the set of transitions selected by the user.
     * @return The set of transitions selected by the user.
     */
    public HashSet<TRANSITION> getSelectedTransitions()
    {
        return m_selectedTransitions;
    }

    /**
     * Set which transtions are selected by the user.
     * @param transitions The transitions selected by the user.
     */
    public void setSelectedTransitions(HashSet<TRANSITION> transitions)
    {
        m_selectedTransitions = transitions;
    }

    /**
     * Get the current transition selected by the user for modification of its input/output symbols,
     * or null if there is no selected transition one.
     * @return The current transition selected by the user for modification.
     */
    public TRANSITION getSelectedTransition()
    {
        return m_selectedTransition;
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
     * Get the alphabet for the machine associated with this panel.
     * @return The alphabet for the machine.
     */
    public Alphabet getAlphabet()
    {
        return getSimulator().getMachine().getAlphabet();
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
     * Get the location of the last place we pasted to.
     * @return The location where the last pasted item was placed.
     */
    public Point2D getLastPastedLocation()
    {
        return m_lastPastedLocation;
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
     * Get the count of the number of times we've pasted to the same location on the canvas.
     * @return The number of times an item has been pasted to the same location on the canvas.
     */
    public int getNumPastesToSameLocation()
    {
        return m_numPastesToSameLocation;
    }

    /**
     * Increase the count of the number of times we've pasted to the same location on the canvas.
     */
    public void incrementNumPastesToSameLocation()
    {
        m_numPastesToSameLocation++;
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

        getSimulator().getMachine().paint(g, m_selectedStates, m_selectedTransitions, getSimulator());
        if (m_currentMode == GUI_Mode.ADDTRANSITIONS && m_mousePressedState != null)
        {
            if (!(m_drawPosX == Integer.MIN_VALUE) || !(m_drawPosY == Integer.MIN_VALUE))
            {
                g2d.setColor(Color.BLACK);
                g2d.draw(new Line2D.Float(m_mousePressedState.getX() + STATE.STATE_RENDERING_WIDTH/2,m_mousePressedState.getY()+ STATE.STATE_RENDERING_WIDTH/2, m_drawPosX, m_drawPosY));
            }
        }

        if (m_selectedSymbolBoundingBox != null)
        {
            Stroke current = g2d.getStroke();
            g2d.setStroke(dashed);
            g2d.setColor(Color.BLACK);
            g2d.draw(m_selectedSymbolBoundingBox);
            g2d.setStroke(current);
        }

        // A marquee selection is taking place
        if (m_selectionBox != null)
        {
            g2d.setColor(Color.BLACK);
            int topLeftX = Math.min(m_selectionBox.x, m_selectionBox.x + m_selectionBox.width),
                topLeftY = Math.min(m_selectionBox.y, m_selectionBox.y + m_selectionBox.height),
                width    = Math.abs(m_selectionBox.width),
                height   = Math.abs(m_selectionBox.height);
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
        setFocusable(false);

        // Set up event listeners and their corresponding actions.
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {   
                // Do nothing if editing is disabled, or this is not a left-click
                if (!m_editingEnabled ||
                    e.getButton() != MouseEvent.BUTTON1)
                {
                    return;
                }

                // Deselect any selected action symbol
                m_selectedSymbolBoundingBox = null;
                m_selectedTransition = null;

                // Selecting a transition action
                if (m_currentMode != GUI_Mode.ERASER && selectCharacterByClicking(e))
                {
                    repaint();
                    return;
                }

                // Handle GUI mode events
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
                        handleEraserClick(e);
                        break;

                    case CHOOSESTART:
                        handleChooseStartClick(e);
                        break;

                    case CHOOSEACCEPTING:
                        handleChooseAcceptingClick(e);
                        break;

                    case SELECTION:
                        handleSelectionClick(e);
                        break;

                    case CHOOSECURRENTSTATE:
                        handleChooseCurrentState(e);
                        break;
                }

                // Update modified if anything is clicked on
                MACHINE mac = getSimulator().getMachine();
                if (mac.getStateClickedOn(e.getX(), e.getY())!= null ||
                    mac.getTransitionClickedOn(e.getX(), e.getY(), getGraphics()) != null)
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
                else if (e.getButton() == MouseEvent.BUTTON1)
                {
                    handleMousePressed(e);
                }
                else if (e.getButton() == MouseEvent.BUTTON3)
                {
                    tryShowPopup(e);
                }
                repaint();
            }

            public void mouseReleased(MouseEvent e)
            {
                if (!m_editingEnabled)
                {
                    return;
                }
                else if (e.getButton() == MouseEvent.BUTTON1)
                {
                    handleMouseReleased(e);
                }
                else if (e.getButton() == MouseEvent.BUTTON3)
                {
                    tryShowPopup(e);
                }
                repaint();
            }

            private void tryShowPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    m_contextState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
                    if (m_contextState != null)
                    {
                        m_contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
                // Deselect any selected action symbol
                m_selectedSymbolBoundingBox = null;
                m_selectedTransition = null;

                boolean repaintNeeded = false;
                if (m_currentMode != GUI_Mode.ADDTRANSITIONS)
                {
                    if (m_mousePressedState != null)
                    {
                        handleStateDrag(e);
                        repaintNeeded = true;
                    }
                    else if (m_currentMode == GUI_Mode.SELECTION && m_selectionBox != null)
                    {
                        m_selectionBox.width  = e.getX() - m_selectionBox.x;
                        m_selectionBox.height = e.getY() - m_selectionBox.y;
                        repaintNeeded = true;
                    }
                }
                // Add transitions mode
                else
                {
                    m_drawPosX = e.getX();
                    m_drawPosY = e.getY();
                    repaintNeeded = true;
                }

                if (m_mousePressedTransition != null)
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
            Rectangle2D s1 = transitionClicked.getInputSymbolBoundingBox(getGraphics());
            Rectangle2D s2 = transitionClicked.getOutputSymbolBoundingBox(getGraphics());
            if (s1.contains(e.getX(), e.getY()))
            {
                m_selectedSymbolBoundingBox = s1;
                m_inputSymbolSelected = true;
            }
            else if (s2.contains(e.getX(), e.getY()))
            {
                m_selectedSymbolBoundingBox = s2;
                m_inputSymbolSelected = false;
            }
            m_selectedTransition = transitionClicked;
            return true;
        }
        else
        {
            m_selectedSymbolBoundingBox = null;
            m_selectedTransition = null;
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
            m_mousePressedState = null;
        }
        else
        {
            m_mousePressedState = getSimulator().getMachine().getStateClickedOn(e.getX(), e.getY());
        }
        if (m_mousePressedState != null) // Mouse press on a state
        {
            setModifiedSinceSave(true);
            m_moveStateClickOffsetX = m_mousePressedState.getX() - e.getX();
            m_moveStateClickOffsetY = m_mousePressedState.getY() - e.getY();
            m_moveStateLastLocationX = m_mousePressedState.getX();
            m_moveStateLastLocationY = m_mousePressedState.getY();
            m_moveStateStartLocationX = m_mousePressedState.getX();
            m_moveStateStartLocationY = m_mousePressedState.getY();
            m_transitionsToMoveState = getSimulator().getMachine().getTransitionsTo(m_mousePressedState);
            precomputeSelectedTransitionsToDrag();
            return;
        }
        else
        {
            m_mousePressedTransition = getSimulator().getMachine().getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
            if (m_mousePressedTransition != null) // Mouse press on a transition
            {
                setModifiedSinceSave(true);
                m_transitionMidPointBeforeMove = m_mousePressedTransition.getMidpoint();
                m_moveTransitionClickOffsetX = (int)m_transitionMidPointBeforeMove.getX() - e.getX();
                m_moveTransitionClickOffsetY = (int)m_transitionMidPointBeforeMove.getY() - e.getY();

                return;
            }
        }
        // No state or transition clicked on
        if (m_currentMode == GUI_Mode.SELECTION)
        {
            // Start building a selection bounding box
            m_selectionBox = new Rectangle(e.getX(), e.getY(), 0, 0);
            m_selectionConcatenateMode = (e.isControlDown() || e.isShiftDown());

        }
    }

    /**
     * Handle when a mouse drag occurs while in selection mode. Moves a transition relative to mouse
     * movement.
     * @param e The generating event.
     */
    protected void handleTransitionDrag(MouseEvent e)
    {
        // Update control point location
        // TODO: enforce area bounds?
        m_movedTransition = true;

        // Find the midpoint by correcting for the offset of where the user clicked
        double correctedMidpointX = e.getX() + m_moveTransitionClickOffsetX; 
        double correctedMidpointY = e.getY() + m_moveTransitionClickOffsetY;

        Point2D newCP = Spline.getControlPointFromMidPoint(
                new Point2D.Double(correctedMidpointX, correctedMidpointY),
                m_mousePressedTransition.getFromState(), m_mousePressedTransition.getToState());

        m_mousePressedTransition.setControlPoint((int)newCP.getX(), (int)newCP.getY());

        // Move the bounding box for the selected symbol if it is on this transition action
        if (m_mousePressedTransition == m_selectedTransition)
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
        int newX = e.getX() + m_moveStateClickOffsetX;
        int newY = e.getY() + m_moveStateClickOffsetY;

        // Check that this is within panel bounds.
        // This is complicated in the case where multiple items are selected
        Dimension boundaries = getSize();
        int minY = 0;
        int minX = 0;
        int maxX = (int)boundaries.getWidth() - STATE.STATE_RENDERING_WIDTH;
        int maxY = (int)boundaries.getHeight() - STATE.STATE_RENDERING_WIDTH;

        if (m_selectedStates.contains(m_mousePressedState)) // Clicked on a selected state
        {
            int rightMostX = m_mousePressedState.getX();
            int leftMostX = m_mousePressedState.getX();
            int bottomMostY = m_mousePressedState.getY();
            int topMostY = m_mousePressedState.getY();
            for (STATE s : m_selectedStates)
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
            maxX -= rightMostX - m_mousePressedState.getX();
            maxY -= bottomMostY - m_mousePressedState.getY();
            minX += m_mousePressedState.getX() - leftMostX;
            minY += m_mousePressedState.getY() - topMostY;
        }
        newX = Math.min(newX, maxX);
        newY = Math.min(newY, maxY);
        newX = Math.max(minX, newX); // Constrain to accessable bounds of view plane
        newY = Math.max(minY, newY);

        int translateX = newX - m_moveStateLastLocationX;
        int translateY = newY - m_moveStateLastLocationY;

        if (translateX == 0 && translateY == 0)
        {
            return;
        }
        m_movedState = true;
        if (!m_selectedStates.contains(m_mousePressedState))
        {
            // Just move the one state
            updateTransitionLocations(m_mousePressedState,translateX, translateY,
                    m_transitionsToMoveState, m_mousePressedState.getTransitions());
            m_mousePressedState.setPosition(m_mousePressedState.getX() + translateX,
                    m_mousePressedState.getY() + translateY);
        }
        else
        {
            // Move all selected states
            pullSelectedTransitionsWithState(translateX, translateY);            
            for (STATE s : m_selectedStates)
            {
                s.setPosition(s.getX() + translateX,
                        s.getY() + translateY);
            }
        }
        m_moveStateLastLocationX = m_mousePressedState.getX();
        m_moveStateLastLocationY = m_mousePressedState.getY();
        updateSelectedSymbolBoundingBox();
    }

    /** 
     * Move the bounding box for the transition symbol currently selected by the user, if any, in
     * the case where a transition has been moved.
     */
    protected void updateSelectedSymbolBoundingBox()
    {
        if (m_selectedSymbolBoundingBox == null || m_selectedTransition == null)
        {
            return;
        }
        if (m_inputSymbolSelected)
        {
            m_selectedSymbolBoundingBox = m_selectedTransition.getInputSymbolBoundingBox(getGraphics());
        }
        else
        {
            m_selectedSymbolBoundingBox = m_selectedTransition.getOutputSymbolBoundingBox(getGraphics());
        }
    }

    /** 
     * Update our sets of selected states and transitions. Should be called when the user selected
     * region has been moved.
     */
    protected void updateSelectedStatesAndTransitions()
    {
        int topLeftX = Math.min(m_selectionBox.x, m_selectionBox.x + m_selectionBox.width),
            topLeftY = Math.min(m_selectionBox.y, m_selectionBox.y + m_selectionBox.height),
            width    = Math.abs(m_selectionBox.width),
            height   = Math.abs(m_selectionBox.height);
 
        HashSet<STATE> states = getSimulator().getMachine().getSelectedStates(topLeftX, topLeftY, width, height);

        if (m_selectionConcatenateMode)
        {
            m_selectedStates.addAll(states);
        }
        else
        {
            m_selectedStates = states;
        }
        m_selectedTransitions = getSimulator().getMachine().getSelectedTransitions(m_selectedStates);
    }

    /**
     * Precompute transitions that should be moved in a drag event.
     */
    protected void precomputeSelectedTransitionsToDrag()
    {
        m_inTransitionsToMove = new HashSet<TRANSITION>();
        m_outTransitionsToMove = new HashSet<TRANSITION>();
        calcMovedTransitionSets(m_inTransitionsToMove, m_outTransitionsToMove);

        m_transitionsToMoveintersection = new HashSet<TRANSITION>();
        m_transitionsToMoveintersection.addAll(m_inTransitionsToMove);
        m_transitionsToMoveintersection.retainAll(m_outTransitionsToMove);
        m_inTransitionsToMove.removeAll(m_transitionsToMoveintersection);
        m_outTransitionsToMove.removeAll(m_transitionsToMoveintersection);
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

        for (TRANSITION t : m_transitionsToMoveintersection)
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
        for (STATE s : m_selectedStates)
        {
            Collection<TRANSITION> out = s.getTransitions();
            outTransitions.addAll(out);
            Collection<TRANSITION> in = getSimulator().getMachine().getTransitionsTo(s);
            inTransitions.addAll(in);
        }
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
     * Deselect any transition action character currently selected by the user. Causes a repaint.
     */
    public void deselectSymbol()
    {
        m_selectedSymbolBoundingBox = null;
        m_selectedTransition = null;
        repaint();
    }

   /**
     * Delete all states and transitions that the user has currently selected.
     */
    public void deleteAllSelected()
    {
        HashSet<STATE> selectedStatesCopy = (HashSet<STATE>)m_selectedStates.clone();
        HashSet<TRANSITION> selectedTransitionsCopy = (HashSet<TRANSITION>)m_selectedTransitions.clone();
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
    protected byte[] copySelectedToByteArray() 
    {
        try
        {
            ByteArrayOutputStream returner = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(returner);
            oos.writeObject(m_selectedStates);
            oos.writeObject(m_selectedTransitions);
            oos.flush();

            return returner.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    // Command handing:
    /**
     * Executes a command and adds it to the undo stack. Clears the redo stack also.
     * @param command The command to execute.
     */
    public void doCommand(TMCommand command)
    {
        command.doCommand();
        m_undoStack.add(command);
        m_redoStack.clear();
        setModifiedSinceSave(true);
        MainWindow.getInstance().updateUndoActions();
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
        m_undoStack.add(command);
        m_redoStack.clear();
        MainWindow.getInstance().updateUndoActions();
        repaint();
    }

    /** 
     * Undoes a command.
     */
    public void undoCommand()
    {
        try
        {
            TMCommand c = m_undoStack.removeLast();
            c.undoCommand();
            m_redoStack.add(c);
            setModifiedSinceSave(true);
            MainWindow.getInstance().updateUndoActions();
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
            TMCommand c = m_redoStack.removeLast();
            c.doCommand();
            m_undoStack.add(c);
            setModifiedSinceSave(true);
            MainWindow.getInstance().updateUndoActions();
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
        if (!m_undoStack.isEmpty())
        {
            return m_undoStack.getLast().getName();
        }
        return null;
    }

    /** 
     * Returns the name of the command at the top of the redo stack or null if the stack is empty.
     * @return The name at the top of the redo stack
     */
    public String redoCommandName()
    {
        if (!m_redoStack.isEmpty())
        {
            return m_redoStack.getLast().getName();
        }
        return null;
    }

    /**
     * Handle when a mouse button is released. Creates any new transitions if a transition creating
     * drag has occured.
     * @param e The generating event.
     */
    protected void handleMouseReleased(MouseEvent e)
    {
        if (m_currentMode == GUI_Mode.ADDTRANSITIONS && m_mousePressedState != null)
        {
            STATE mouseReleasedState = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
            if (mouseReleasedState != null)
            {
                TRANSITION newTrans = makeTransition(m_mousePressedState, mouseReleasedState);
                doCommand(new AddTransitionCommand(this, newTrans));
                repaint();
            }
        }
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
            int translateX = m_mousePressedState.getX() - m_moveStateStartLocationX,
                translateY = m_mousePressedState.getY() - m_moveStateStartLocationY;

            if (translateX != 0 || translateY != 0)
            {
                if (m_selectedStates.contains(m_mousePressedState))
                {
                    // Moved a set of states
                    Collection<State> statesCopy = (Collection<State>)m_selectedStates.clone();
                    Collection<Transition> transitionsCopy = (Collection<Transition>)m_selectedTransitions.clone();
                    addCommand(new MoveSelectedCommand(this, statesCopy, transitionsCopy, translateX, translateY));
                }
                else
                {
                    // Moved one state
                    Collection<Transition> transitions = new ArrayList<Transition>();
                    transitions.addAll(m_transitionsToMoveState);
                    addCommand(new MoveStateCommand(this, m_mousePressedState, translateX, translateY, transitions));
                }
            }
        }

        if (m_mousePressedTransition != null && m_movedTransition)
        {
            // Create an undo/redo command object for the move of a transition
            int translateX = (int)(m_mousePressedTransition.getMidpoint().getX() - m_transitionMidPointBeforeMove.getX()),
                translateY = (int)(m_mousePressedTransition.getMidpoint().getY() - m_transitionMidPointBeforeMove.getY());
            addCommand(new MoveTransitionCommand(this, m_mousePressedTransition, translateX, translateY));
        }

        m_selectionBox = null;
        m_mousePressedState = null;
        m_mousePressedTransition = null;
        m_transitionMidPointBeforeMove = null;
        m_movedTransition = false;
        m_movedState = false;
        m_drawPosX = Integer.MIN_VALUE;
        m_drawPosY = Integer.MIN_VALUE;
    }

    /**
     * Handle when a mouse click occurs over a state, by either selecting the existing underlying
     * state, or creating a new state.
     * @param e The generating event.
     */
    protected void handleAddNodesClick(MouseEvent e)
    {
        // Clicking on another state should just select the existing state
        if (m_sim.getMachine().getStateClickedOn(e.getX(), e.getY()) != null)
        {
            // Adding states on top of states is not allowed
            handleSelectionClick(e);
            return;
        }

        int x = e.getX() - STATE.STATE_RENDERING_WIDTH / 2,
            y = e.getY() - STATE.STATE_RENDERING_WIDTH / 2;
        switch (m_sim.getMachine().getNamingScheme())
        {
            case GENERAL:
                String label = getFirstFreeName();
                doCommand(new AddStateCommand(this, makeState(label, x, y)));
                break;
            
            case NORMALIZED:
                doJoinCommand(
                        new AddStateCommand(this, makeState("", x, y)),
                        new SchemeRelabelCommand(this, NamingScheme.NORMALIZED));
                break;
        }
    }

    /** 
     * Handle when a mouse click occurs while in eraser mode. If the mouse click occurs over a
     * state, it is deleted, and if it is over a transition, that is deleted.
     * @param e The generating event.
     */
    protected void handleEraserClick(MouseEvent e)
    {
        STATE stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            deleteState(stateClickedOn);
        }
        else
        {
            TRANSITION transitionClickedOn = m_sim.getMachine()
                .getTransitionClickedOn(e.getX(), e.getY(), getGraphics());
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
        STATE stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
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
        STATE stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (!(e.isControlDown() || e.isShiftDown()))
        {
            m_selectedStates.clear();
            m_selectedTransitions.clear();
        }
        if (stateClickedOn != null && !m_selectedStates.remove(stateClickedOn))
        {
            m_selectedStates.add(stateClickedOn);
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
        STATE stateClickedOn = m_sim.getMachine().getStateClickedOn(e.getX(), e.getY());
        if (stateClickedOn != null)
        {
            m_sim.setCurrentState(stateClickedOn);
        }
    }

    /**
     * Handle when a mouse click occurs while in select accepting state mode. If the mouse click
     * occurs over a state, the accepting state of the machine is changed.
     * @param e The generating event.
     */
    protected abstract void handleChooseAcceptingClick(MouseEvent e);

    /**
     * Called when the owning frame is activated. Should deactivate any and all events which are not
     * used by the simulated machine.
     */
    public abstract void onActivation();

    /** 
     * Accept a KeyEvent detected in the main window, and use it to update any transition action
     * selected by the user.
     * @param e The generating event.
     * @return true if a transition action was selected and updated, false otherwise.
     */
    public abstract boolean handleKeyEvent(KeyEvent e);

    /**
     * Create a STATE object with the given label at the specified location.
     * @param label The state label.
     * @param x The x-ordinate of the state.
     * @param y The y-ordinate of the state.
     * @return A new STATE object.
     */
    protected abstract STATE makeState(String label, int x, int y);

    /**
     * Create a TRANSITION object with a default action, attached to the two specified states.
     * @param start The state the transition leaves.
     * @param end The state the transition arrives at.
     * @return A new TRANSITION object.
     */
    protected abstract TRANSITION makeTransition(STATE start, STATE end);

    /**
     * Build an error message for the given ComputationCompletedException.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public abstract String getErrorMessage(ComputationCompletedException e);

    /**
     * Build an error message for the given ComputationFailedException.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public abstract String getErrorMessage(ComputationFailedException e);

    /**
     * Build an error message for the given NondeterministicException.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public abstract String getErrorMessage(NondeterministicException e);

    /**
     * Build an error message for the given TapeBoundsException.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public abstract String getErrorMessage(TapeBoundsException e);

    /**
     * Build an error message for the given UndefinedTransitionException.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public abstract String getErrorMessage(UndefinedTransitionException e);

    /**
     * Build an error message for the given Exception. Automatically checks the type of the
     * exception to see if it matches any existing definitions of getErrorMessage and dispatches
     * accordingly.
     * @param e The exception to build a message for.
     * @return An error message for the given exception.
     */
    public String getErrorMessage(Exception e)
    {
        // Pretend we have dynamic dispatch
        if (e instanceof ComputationCompletedException)
        {
            return getErrorMessage((ComputationCompletedException) e);
        }
        else if (e instanceof ComputationFailedException)
        {
            return getErrorMessage((ComputationFailedException) e);
        }
        else if (e instanceof NondeterministicException)
        {
            return getErrorMessage((NondeterministicException) e);
        }
        else if (e instanceof TapeBoundsException)
        {
            return getErrorMessage((TapeBoundsException) e);
        }
        else if (e instanceof UndefinedTransitionException)
        {
            return getErrorMessage((UndefinedTransitionException) e);
        }
        else 
        { 
            e.printStackTrace();
            return String.format("Unknown error [%s]. %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Action to rename a state selected by the context menu.
     */
    protected class RenameStateAction extends AbstractAction
    {
        /**
         * Creates a new instance of RenameStateAction.
         * @param text Description of the action.
         */
        public RenameStateAction(String text)
        {
            super(text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        /**
         * Display a dialog to change the state label. If the user accepts the new name, and it
         * passes relevant checks, fire the command to change the label.
         */
        public void actionPerformed(ActionEvent e)
        {
            // Should be fired by m_contextMenu, which in turn is only open if we have right clicked
            // on a state. Hence we have a non-null state to work with.
            
            // Disable the keyboard while we prompt for user input
            m_keyboardEnabled = false;
            String result = (String) JOptionPane.showInputDialog(null, "Please enter the new state label",
                                                                 "Rename State", JOptionPane.QUESTION_MESSAGE,
                                                                 null, null, m_contextState.getLabel());
            m_keyboardEnabled = true;
        
            // User cancelled, or no change
            if (result == null || result.equals(m_contextState.getLabel()))
            {
                // Do nothing
            }
            // Blank label not allowed
            else if (result.equals(""))
            {
                JOptionPane.showMessageDialog(null, "Empty labels are not allowed!");
            }
            // Label already in use
            else if (m_labelsUsed.contains(result))
            {
                JOptionPane.showMessageDialog(null, "Label is already used by another state!");
            }
            // Otherwise rename
            else
            {
                doCommand(new RenameStateCommand(MachineGraphicsPanel.this, m_contextState, result));
            }
        }
    }

    /**
     * Get the file extension associated with this type of machine. Should return a value from a
     * symbol named MACHINE_EXT.
     * @return The file extension associated with this type of machine.
     */
    public abstract String getMachineExt();

    /**
     * Get a friendly name for the type of machine this graphics panel renders. Should return a
     * value from a symbol named MACHINE_TYPE.
     * @return A friendly name for the type of machine being stored.
     */
    public abstract String getMachineType();

    /**
     * The owning frame.
     */
    protected MachineInternalFrame m_iFrame;

    /**
     * The associated simulator
     */
    protected SIMULATOR m_sim;

    /**
     * The underlying file.
     */
    protected File m_file;

    /**
     * Whether or not the machine has been modified since the last save.
     */
    protected boolean m_modifiedSinceSave = false;

    /**
     * The current GUI mode.
     */
    protected GUI_Mode m_currentMode;

    /**
     * The right-click context menu associated with this panel.
     * Specialized panels should take this menu and add new actions after a separator.
     */
    protected JPopupMenu m_contextMenu;

    /**
     * The state currently selected by the right-click context menu.
     * If m_contextMenu is displayed, then m_contextState is non-null.
     */
    protected STATE m_contextState;

    /**
     * Stack containing commands which can be undone.
     */
    protected LinkedList<TMCommand> m_undoStack = new LinkedList<TMCommand>();

    /**
     * Stack containing commands which can be redone.
     */
    protected LinkedList<TMCommand> m_redoStack = new LinkedList<TMCommand>();

    /**
     * The set of selected states.
     */
    protected HashSet<STATE> m_selectedStates = new HashSet<STATE>();

    /**
     * The set of selected transitions.
     */
    protected HashSet<TRANSITION> m_selectedTransitions = new HashSet<TRANSITION>();

    /**
     * The bounds of the selection marquee. If null, then no selection is in progress.
     * If width and height are nonzero, then a selection has been made.
     */
    protected Rectangle m_selectionBox;

    /**
     * Whether or not selected items will be concatenated to the list of selected items, or the
     * previous selected items overwritten.
     */
    protected boolean m_selectionConcatenateMode = false;

    /**
     *  The state we last pressed a mouse button on.
     */
    protected STATE m_mousePressedState = null;

    /**
     * The transition we last pressed a mouse button on
     */
    protected TRANSITION m_mousePressedTransition = null;

    /**
     * The X ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    protected int m_drawPosX = Integer.MIN_VALUE;

    /**
     * The Y ordinate of the temporary transition to be drawn when the mouse is dragged.
     */
    protected int m_drawPosY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to the control point.
     */
    protected int m_moveTransitionClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to the control point.
     */
    protected int m_moveTransitionClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate from the mouse click to state location.
     */
    protected int m_moveStateClickOffsetX = Integer.MIN_VALUE;

    /**
     * Y ordinate from the mouse click to state location.
     */
    protected int m_moveStateClickOffsetY = Integer.MIN_VALUE;

    /**
     * X ordinate of the last location of the moved state.
     */
    protected int m_moveStateLastLocationX = Integer.MIN_VALUE;

    /**
     * Y ordinate of the last location of the moved state.
     */
    protected int m_moveStateLastLocationY = Integer.MIN_VALUE;

    /**
     * X ordinate of the original position of the state, before movement.
     */
    protected int m_moveStateStartLocationX = Integer.MIN_VALUE;

    /**
     * Y ordinate of the original position of the state, before movement.
     */
    protected int m_moveStateStartLocationY = Integer.MIN_VALUE;

    /**
     * Whether a state has been moved.
     */
    protected boolean m_movedState = false;

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
    protected HashSet<TRANSITION> m_transitionsToMoveintersection = new HashSet<TRANSITION>();

    /**
     * Midpoint of the currently selected transition, before movement.
     */
    protected Point2D m_transitionMidPointBeforeMove = null;

    /**
     * Whether a transition has been moved.
     */
    protected boolean m_movedTransition = false;

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
     * Bounding box of the currently selected transition action.
     */
    protected Rectangle2D m_selectedSymbolBoundingBox = null;

    /**
     * The selected transition.
     */
    protected TRANSITION m_selectedTransition = null;

    /**
     * Whether a transition action has been selected.
     */
    protected boolean m_inputSymbolSelected = false;

    /**
     * Last location a value was pasted.
     */
    protected Point2D m_lastPastedLocation = null;

    /**
     * How many times an object was pasted to the last pasted location.
     */
    protected int m_numPastesToSameLocation = 0;
}
