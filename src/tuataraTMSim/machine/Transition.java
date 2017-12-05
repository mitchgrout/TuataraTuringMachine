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
     * Number of pixels for arrowheads on transitions.
     */
    public static final double ARROWHEAD_LENGTH = 6;
    
    /**
     * Number of pixels away from transitions actions should be drawn.
     */
    public static final double ACTION_TEXT_DISTANCE = 10;
    
    /**
     * Number of pixels away from transitions actions should be drawn, if the transition is a loop.
     */
    public static final double ACTION_TEXT_DISTANCE_FOR_LOOPS = 15;
    
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
        if (selectedTransitions.contains(this))
        {
            g2d.setColor(Color.RED);
        }
        else if (simulator.getNextTransition() == this)
        {
            g2d.setColor(Color.PINK);
        }
        else
        {
            g2d.setColor(Color.BLUE);
        }
       
        // Get the location of the action associated with this transition
        Point2D actionLocation = getActionLocation();

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
            paintArrowHead(g);
        }

        // Render the action
        m_action.paint(g, (int)actionLocation.getX(), (int)actionLocation.getY());
    }
    
    /** 
     * Paints the arrowhead of a transition to the graphics object in the graphics object's current
     * colour.
     * @param g The graphics object on which to render.
     */
    public void paintArrowHead(Graphics g)
    {
        // Get a 2d graphics object
        Graphics2D g2d = (Graphics2D)g;

        // Change the line style 
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

        // Get the midpoint of the curve
        Point2D arrowLoc = getMidpoint();
        // and the tangent vector associated with the midpoint
        Point2D tangentVector = 
            Spline.getMidPointTangentVector(getControlPoint(), arrowLoc, m_fromState, m_toState);
       
        // Get the magnitude of the tangent, and build a scale factor
        double length = tangentVector.distance(new Point2D.Double(0, 0));
        double scaleFactor = 1.0/length * ARROWHEAD_LENGTH;

        // Build transforms for scaling, rotation, and translation
        AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        AffineTransform rotateClockwise = AffineTransform.getRotateInstance(Math.PI/ 2.0 + Math.PI/4.5, arrowLoc.getX(), arrowLoc.getY());
        AffineTransform rotateAnticlockwise = AffineTransform.getRotateInstance(-(Math.PI/ 2.0 + Math.PI/4.5), arrowLoc.getX(), arrowLoc.getY());
        AffineTransform translateToEndpoint = AffineTransform.getTranslateInstance(arrowLoc.getX(), arrowLoc.getY());
        
        // Copy the tangent vector, scale, translate, and rotate
        Point2D p1 = (Point2D)tangentVector.clone();
        scale.transform(p1, p1);
        translateToEndpoint.transform(p1, p1);
        rotateClockwise.transform(p1, p1);
        
        // Copy the tangent vector, scale, translate, and rotate
        Point2D p2 = (Point2D)tangentVector.clone();
        scale.transform(p2, p2);
        translateToEndpoint.transform(p2, p2);
        rotateAnticlockwise.transform(p2, p2);
        
        // Render it
        drawTriangle(g, arrowLoc,p1, p2);
        g2d.setStroke(originalStroke);
    }
   
    /**
     * Helper function which renders a triangle at a given triple of points.
     * @param g The graphics object on which to render.
     * @param p1 The first vertex of the triangle.
     * @param p2 The second vertex of the triangle.
     * @param p3 The third vertex of the triangle.
     */
    protected void drawTriangle(Graphics g, Point2D p1, Point2D p2, Point2D p3)
    {
        // Get a 2d graphics object
        Graphics2D g2d = (Graphics2D)g;

        // Build the path
        GeneralPath triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        triangle.moveTo((float)p1.getX(), (float)p1.getY());
        triangle.lineTo((float)p2.getX(), (float)p2.getY());
        triangle.lineTo((float)p3.getX(), (float)p3.getY());
        triangle.lineTo((float)p1.getX(), (float)p1.getY());

        // Render and fill the path
        g2d.draw(triangle);
        g2d.fill(triangle);
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
        // Object used to measure string dimensions using the graphics object
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        
        // Assuming monospace, the width of a single character
        int symbolWidth = metrics.charWidth('_');

        // Get the text representing the action
        String actionString = m_action.toString();
        
        // Get the size of the action string, and create its bounding box
        Rectangle2D actionDims = metrics.getStringBounds(actionString, g);
        Rectangle2D boundingBox = new Rectangle2D.Double(
                actionDims.getX(), actionDims.getY(),
                actionString.length() * symbolWidth,
                metrics.getHeight());
       
        // Get the location of the action 
        Point2D actionLocation = getActionLocation();
   
        // Translate to the correct position
        AffineTransform trans = AffineTransform.getTranslateInstance(
                actionLocation.getX() - ((symbolWidth * actionString.length()) / 2),
                actionLocation.getY());
        Shape translated = trans.createTransformedShape(boundingBox);
        return translated.contains(x, y);
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
        // Object used to measure string dimensions using the graphics object
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        // Get the text representing the action
        String actionString = m_action.toString();
       
        // Get the size of the action string
        Rectangle2D boundingBox = metrics.getStringBounds(actionString, g);
       
        // Get the midpoint of the spline
        Point2D arrowLocation = getMidpoint();

        // Translate to the correct position
        AffineTransform trans = AffineTransform.getTranslateInstance(
                arrowLocation.getX() - metrics.stringWidth(actionString) / 2,
                arrowLocation.getY());
        Shape translated = trans.createTransformedShape(boundingBox);
        return translated.contains(x, y);
    }
       
    /**
     * Get the midpoint of the spline, represented by the arrow on the transition..
     * @return The location of the midpoint.
     */
    protected Point2D getActionLocation()
    {
        // Compute the location of the arrow, i.e. the midpoint
        Point2D arrowLoc = getMidpoint();
        
        // A loop 
        if (m_fromState != m_toState)
        {
            AffineTransform translate = AffineTransform.getTranslateInstance(0, -ACTION_TEXT_DISTANCE);
            return translate.transform(arrowLoc, null);
        }
        else
        {
            // Get the vector between the midpoint and start
            Point2D startToMidpoint = new Point2D.Double(
                    arrowLoc.getX() - m_fromState.getX(),
                    arrowLoc.getY() - m_fromState.getY());

            // Scale the start to midpoint vector to account for text distance from midpoint
            double scaleFactor = 
                (startToMidpoint.distance(new Point2D.Float(0,0)) + ACTION_TEXT_DISTANCE_FOR_LOOPS)
                / startToMidpoint.distance(new Point2D.Float(0,0));
           
            // Scale 
            AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            Point2D scaled = new Point2D.Float();
            scale.transform(startToMidpoint, scaled);
            
            return new Point2D.Double(
                    m_fromState.getX() + scaled.getX(),
                    m_fromState.getY() + scaled.getY());
        }
    }
   
    /**
     * Get the bounding box associated with the input symbol.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the input symbol.
     */
    public Shape getInputSymbolBoundingBox(Graphics g)
    {
        // Object used to measure string dimensions using the graphics object
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        // Assuming monospace, the width of a single character
        int symbolWidth = metrics.charWidth('_');

        // How many symbols are used in drawing the action
        int actionLen = m_action.toString().length();

        // Bounds for the input character, with added padding
        Rectangle2D boundingBox = metrics.getStringBounds("" + m_action.getInputChar(), g);
        boundingBox = new Rectangle2D.Double(
                boundingBox.getMinX(), boundingBox.getMinY(),
                symbolWidth + 2 * SELECTED_SYMBOL_BOX_PAD_X,
                boundingBox.getHeight() + 2 * SELECTED_SYMBOL_BOX_PAD_Y);

        // Get the location of the action
        Point2D actionLocation = getActionLocation();

        // Translate to the correct position
        AffineTransform trans = AffineTransform.getTranslateInstance(
                actionLocation.getX() - (symbolWidth * actionLen) / 2
                - SELECTED_SYMBOL_BOX_PAD_X + SELECTED_INPUT_SYMBOL_BOX_X_OFFSET,
                actionLocation.getY() - SELECTED_SYMBOL_BOX_PAD_Y);
        return trans.createTransformedShape(boundingBox);
    }
    
    /**
     * Get the bounding box associated with the action.
     * @param g The graphics object, used to measure the label's dimensions. 
     * @return The bounding box associated with the action.
     */
    public Shape getOutputSymbolBoundingBox(Graphics g)
    {
        // Object used to measure string dimensions using the graphics object
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        // Assuming monospace, the width of a single character
        int symbolWidth = metrics.charWidth('_');

        // How many symbols are used in drawing the action
        int actionLen = m_action.toString().length();
 
        // Bounds for the output character, with added padding
        Rectangle2D boundingBox = metrics.getStringBounds("" + m_action.getOutputChar(), g);
        boundingBox = new Rectangle2D.Double(
            boundingBox.getMinX(), boundingBox.getMinY(), 
            symbolWidth + 2 * SELECTED_SYMBOL_BOX_PAD_X, 
            boundingBox.getHeight() + 2 * SELECTED_SYMBOL_BOX_PAD_Y);
        
        // Get the location of the action
        Point2D actionLocation = getActionLocation();
        
        // Translate to the correct position
        AffineTransform trans = AffineTransform.getTranslateInstance(
            actionLocation.getX() - (symbolWidth * actionLen) / 2
            + (symbolWidth * 2) - SELECTED_SYMBOL_BOX_PAD_X, 
            actionLocation.getY() - SELECTED_SYMBOL_BOX_PAD_Y);
        return trans.createTransformedShape(boundingBox);
    }
   
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
