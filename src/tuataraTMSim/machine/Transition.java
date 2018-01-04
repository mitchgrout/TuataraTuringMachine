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

package tuataraTMSim.machine;

import java.awt.*;
import java.awt.geom.*;
import java.util.Collection;
import java.io.Serializable;
import tuataraTMSim.Spline;

/**
 * Represents a transition in a machine.
 */
public abstract class Transition<
    PREACTION extends PreAction,
    STATE extends State<PREACTION, ?, MACHINE, SIMULATOR>,
    MACHINE extends Machine<PREACTION, ?, STATE, SIMULATOR>,
    SIMULATOR extends Simulator<PREACTION, ?, STATE, MACHINE>> implements Serializable
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Number of pixels for arrowheads on transitions.
     */
    public static final double ARROWHEAD_LENGTH = 6;
    
    /**
     * Number of pixels away from transitions actions should be drawn.
     */
    public static final double ACTION_TEXT_DISTANCE = 20;
    
    /**
     * Width of bounding box used for padding around selected symbols.
     */
    public static final double SELECTED_SYMBOL_BOX_PAD_X = 3;
    
    /**
     * Height of bounding box used for padding around selected symbols.
     */
    public static final double SELECTED_SYMBOL_BOX_PAD_Y = 0; 

    /**
     * Pixels to offset input symbol bounding box.
     */
    public static final double SELECTED_INPUT_SYMBOL_BOX_X_OFFSET = -2;

    public Transition(STATE from, STATE to, PREACTION action)
    {
        m_fromState = from;
        m_toState = to;
        m_action = action;
        if (from != to)
        {
            m_controlPtX = (from.getX() + to.getX()) / 2;
            m_controlPtY = (from.getY() + to.getY()) / 2;
        }
        else
        {
            m_controlPtX = from.getX() + STATE.STATE_RENDERING_WIDTH / 2;
            m_controlPtY = from.getY() + (int)(STATE.STATE_RENDERING_WIDTH * 1.5);
        }
    }

    public Transition(STATE from, STATE to, PREACTION action, int controlX, int controlY)
    {
        m_fromState = from;
        m_toState = to;
        m_action = action;
        m_controlPtX = controlX;
        m_controlPtY = controlY;
    }

    /**
     * Gets the state that this transition starts from.
     * @return The state this transition leaves.
     */
    public STATE getFromState()
    {
        return m_fromState;
    }
    
    /**
     * Gets the state that this transition ends at.
     * @return The state this transition arrives at.
     */
    public STATE getToState()
    {
        return m_toState;
    }
    
    /**
     * Gets the action for this transition.
     * @return The action associated with this action.
     */
    public PREACTION getAction()
    {
        return m_action;
    }
    
    /**
     * Set the action for this transition.
     * @param action The new action.
     */
    public void setAction(PREACTION action)
    {
        m_action = action;
    }
 
    /**
     * Get the location of the control point for the curve representing this transition, i.e. the
     * point used to build the spline.
     * @return The control point of the transition.
     */
    public Point2D getControlPoint()
    {
        return new Point2D.Double(m_controlPtX, m_controlPtY);
    }
    
    /**
     * Set the location of the control point for the curve representing this transition, i.e. the
     * point used to build the spline.
     * @param x The X ordinate of the control point, in viewplane space.
     * @param y The Y ordinate of the control point, in viewplane space.
     */
    public void setControlPoint(int x, int y)
    {
        m_controlPtX = x;
        m_controlPtY = y;
    }

    /**
     * Get the midpoint of the spline representing the transition, i.e. the point sitting on the
     * spline used to manipulate it. A wrapper to Spline.getMidPointFromControlPoint.
     * @return The midpoint of the spline.
     */
    public Point2D getMidpoint()
    {
        return Spline.getMidPointFromControlPoint(getControlPoint(), m_fromState, m_toState);
    }

    /**
     * Get the color to paint this object.
     * @param isSelected Whether or not this transition is selected.
     * @param isCurrent Whether or not this transition is the next transition to be taken.
     * @return The color to paint this object.
     */
    protected Paint getPaint(boolean isSelected, boolean isCurrent)
    {
        return isSelected? Color.RED
             : isCurrent?  Color.PINK
             : Color.BLUE;
    }

    /**
     * Render the transition to a graphics object.
     * @param g The graphics object on which to render.
     * @param selectedTransitions The set of transitions selected by the user.
     * @param simulator The current simulator.
     */
    public void paint(Graphics g, Collection<? extends Transition> selectedTransitions, 
                      SIMULATOR simulator)
    {
        // Get a 2d graphics object
        Graphics2D g2d = (Graphics2D)g;

        // Choose color based off of this transitions state
        g2d.setPaint(getPaint(selectedTransitions.contains(this), simulator.getNextTransition() == this));
       
        // An arc
        if (m_fromState != m_toState)
        {
            // Build and render the arc spline
            QuadCurve2D curve = Spline.buildArcSpline(getControlPoint(), m_fromState, m_toState);
            g2d.draw(curve);
        }
        // A loop
        else
        {
            // Get a perpendicular vector
            Point2D perp = new Point2D.Double(
                    -m_controlPtY + (m_fromState.getY() + STATE.STATE_RENDERING_WIDTH / 2),
                     m_controlPtX - (m_fromState.getX() + STATE.STATE_RENDERING_WIDTH / 2));

            // Rescale our perpendicular vector by the scaling factor
            double scaleFactor = 
                State.STATE_RENDERING_WIDTH * 1.5 / perp.distance(new Point2D.Float(0,0));
            AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            scale.transform(perp, perp);

            // Build the two control points for the rendered arc
            Point2D controlPoint1 = new Point2D.Double(
                    m_controlPtX + perp.getX(), 
                    m_controlPtY + perp.getY());
            Point2D controlPoint2 = new Point2D.Double(
                    m_controlPtX - perp.getX(),
                    m_controlPtY - perp.getY());
           
            // Build and render the loop spline
            CubicCurve2D curve = Spline.buildLoopSpline(controlPoint1, controlPoint2, m_fromState);
            g2d.draw(curve);
        }

        boolean startEqualsEnd = 
            ((int)m_fromState.getX() == (int)m_toState.getX() &&
            ((int)m_fromState.getY() == (int)m_toState.getY()));
        
        // TODO: Justify this conditional
        if (!startEqualsEnd || (m_fromState == m_toState))
        {
            // Change the line style 
            Stroke originalStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

            // Get the arrowhead
            GeneralPath triangle = buildArrowHead();

            // Render and fill the path
            g2d.draw(triangle);
            g2d.fill(triangle);

            // Reset
            g2d.setStroke(originalStroke);
        }

        // Get the location of the action associated with this transition
        Point2D actionLocation = getActionLocation();

        // Render the action
        m_action.paint(g, (int)actionLocation.getX(), (int)actionLocation.getY());
    }

    /**
     * Build a path with three vertices which represents our arrowhead.
     * @return A path representing our arrowhead.
     */
    protected GeneralPath buildArrowHead()
    {
        // Get the midpoint of the curve
        Point2D arrowLoc = getMidpoint();
        // and the tangent vector associated with the midpoint
        Point2D tangentVector = 
            Spline.getMidPointTangentVector(getControlPoint(), arrowLoc, m_fromState, m_toState);

        // Find the angle between the tangent vector and horizontal
        double angle = Math.atan2(tangentVector.getY(), tangentVector.getX());
        double inc = 13 * Math.PI / 18;

        // Render a triangle centered at the midpoint. We avoid using an equilateral triangle as
        // orientation can become confusing to the user at certain angles.
        GeneralPath triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        double x1 = arrowLoc.getX() + ARROWHEAD_LENGTH * Math.cos(angle),
               y1 = arrowLoc.getY() + ARROWHEAD_LENGTH * Math.sin(angle),
               x2 = arrowLoc.getX() + ARROWHEAD_LENGTH * Math.cos(angle + inc),
               y2 = arrowLoc.getY() + ARROWHEAD_LENGTH * Math.sin(angle + inc),
               x3 = arrowLoc.getX() + ARROWHEAD_LENGTH * Math.cos(angle - inc),
               y3 = arrowLoc.getY() + ARROWHEAD_LENGTH * Math.sin(angle - inc);
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x1, y1);

        return triangle;
    }

    /**
     * Determine if the action associated with this transition contains the specified point.
     * @param x The X ordinate.
     * @param y The Y ordinate.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return true if the action contains the specified point, false otherwise.
     */
    public boolean actionContainsPoint(int x, int y, Graphics g)
    {
        return getInputSymbolBoundingBox(g).contains(x, y) ||
               getOutputSymbolBoundingBox(g).contains(x, y);
    }
 
    /**
     * Determine if the arrow associated with this transition contains the specified point.
     * @param x The X ordinate.
     * @param y The Y ordinate.
     * @param g The graphics object, used to measure label's dimensions.
     * @return true if the arrow contains the specified point, false otherwise.
     */
    public boolean arrowContainsPoint(int x, int y, Graphics g)
    {
        return buildArrowHead().contains(new Point2D.Float(x, y));
    }
       
    /**
     * Get the midpoint of the spline, represented by the arrow on the transition..
     * @return The location of the midpoint.
     */
    protected Point2D getActionLocation()
    {
        // Compute the location of the arrow, i.e. the midpoint
        Point2D arrowLoc = getMidpoint();
        
        // An arc 
        if (m_fromState != m_toState)
        {
            AffineTransform translate = AffineTransform.getTranslateInstance(0, -ACTION_TEXT_DISTANCE);
            return translate.transform(arrowLoc, null);
        }
        else
        {
            // We will place the action on the line running from the centre of the state, through
            // the midpoint, and place the action location further along on this line so all three
            // are colinear.
            double dx = arrowLoc.getX() - (m_fromState.getX() + STATE.STATE_RENDERING_WIDTH / 2),
                   dy = arrowLoc.getY() - (m_fromState.getY() + STATE.STATE_RENDERING_WIDTH / 2);
            double angle = Math.atan2(dy, dx);

            return new Point2D.Double(
                    arrowLoc.getX() + ACTION_TEXT_DISTANCE * Math.cos(angle),
                    arrowLoc.getY() + ACTION_TEXT_DISTANCE * Math.sin(angle));
        }
    }
      
    /**
     * Get the bounding box associated with the input symbol.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the input symbol.
     */
    public abstract Rectangle2D getInputSymbolBoundingBox(Graphics g);
 
    /**
     * Get the bounding box associated with the action.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the action.
     */
    public abstract Rectangle2D getOutputSymbolBoundingBox(Graphics g);

    /**
     * Get a String representation of this transition.
     * @return A string representation of this transition.
     */
    public abstract String toString();
   
    /**
     * The state this transition leaves.
     */
    protected STATE m_fromState;
    
    /**
     * The state this transition arrives at.
     */
    protected STATE m_toState;
    
    /**
     * The action associated with this transition.
     */
    protected PREACTION m_action;
    
    /**
     * The X ordinate of the control point for the curve, i.e. the point used to build the spline.
     */
    protected int m_controlPtX;
    
    /**
     * The Y ordinate of the control point for the curve, i.e. the point used to build the spline.
     */
    protected int m_controlPtY;
}
