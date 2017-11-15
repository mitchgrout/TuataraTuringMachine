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

package tuataraTMSim.TM;

import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.Collection;

/**
 * Represents a transition in a Turing machine.
 * @author Jimmy
 */
public class TM_Transition implements Serializable
{
    public static final double ARROWHEAD_LENGTH = 6; // Number of pixels for arrows on transitions.
    public static final double ACTION_TEXT_DISTANCE = 10; // Number of pixels away from transitions to draw actions.
    public static final double ACTION_TEXT_DISTANCE_FOR_LOOPS = 15;
    public static final double SELECTED_SYMBOL_BOX_PAD_X = 3; // Size of padding space around
    public static final double SELECTED_SYMBOL_BOX_PAD_Y = 0; // Bounding boxes for selected symbols
    public static final double SELECTED_INPUT_SYMBOL_BOX_X_OFFSET = -2; // Pixels to offset input symbol bounding box
                                
    /**
     * Creates a new instance of TM_Transition
     * */
    protected TM_Transition() { }
    
    /** 
     * Creates a new instance of TM_Transition
     */
    public TM_Transition(TM_State fromState, TM_State toState, char symbol, TM_Action action)
    {
        m_fromState = fromState;
        m_toState = toState;
        m_action = action;
        m_symbol = symbol;
        if (fromState != toState)
        {
            // This is using the top-left coordinates of the state's circle,
            // rather than the centre. The result is a slight upward, leftward curve
            m_controlPtX = (m_fromState.getX() + m_toState.getX()) / 2;
            m_controlPtY = (m_fromState.getY() + m_toState.getY()) / 2;
        }
        else
        {
            // Do something nifty
            m_controlPtX = m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2;
            m_controlPtY = m_fromState.getY() - (int)(TM_State.STATE_RENDERING_WIDTH * 1.5);
        }
    }
    
    /**
     * Gets the state that this transition starts from.
     */
    public TM_State getFromState()
    {
        return m_fromState;
    }
    
    /**
     * Gets the state that this transition ends at.
     */
    public TM_State getToState()
    {
        return m_toState;
    }
    
    /**
     * Gets the action for this transition.
     */
    public TM_Action getAction()
    {
        return m_action;
    }
    
    /**
     * Gets the input symbol for this transition.
     */
    public char getSymbol()
    {
        return m_symbol;
    }
    
    /**
     * Render the transition to a graphics object.
     */
    public void paint(Graphics g, Collection<TM_Transition> selectedTransitions, TM_Simulator simulator)
    {
        final Color LIGHT_BLUE = new Color(0,0,0.5f);
        Graphics2D g2d = (Graphics2D)g;
        if (selectedTransitions.contains(this))
        {
            g2d.setColor(Color.RED);
        }
        else if (simulator.getCurrentNextTransition() == this)
        {
            g2d.setColor(Color.PINK);
        }
        else if (simulator.getPotentialTransitions().contains(this))
        {
            g2d.setColor(LIGHT_BLUE);
        }
        else
        {
            g2d.setColor(Color.BLUE);
        }
        
        Point2D actionLocation = getActionLocation();
        if (m_fromState != m_toState)
        {         
            QuadCurve2D curve = new QuadCurve2D.Float(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                    m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2, m_controlPtX, m_controlPtY,
                    m_toState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                    m_toState.getY() + TM_State.STATE_RENDERING_WIDTH/2);
            g2d.draw(curve);
        }
        else
        {
            Point2D perpendicular1 = new Point2D.Float(-(m_controlPtY - (m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2)),
                    m_controlPtX - (m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2));

            double scaleFactor = TM_State.STATE_RENDERING_WIDTH * 1.5 / perpendicular1.distance(new Point2D.Float(0,0));
            AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            
            scale.transform(perpendicular1, perpendicular1);
            
            Point2D controlPoint1 = new Point2D.Float(m_controlPtX + (float)perpendicular1.getX(), 
                    m_controlPtY + (float)perpendicular1.getY());
            Point2D controlPoint2 = new Point2D.Float(m_controlPtX - (float)perpendicular1.getX(),
                    m_controlPtY - (float)perpendicular1.getY());
            
            g2d.draw(new CubicCurve2D.Float(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                        m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2,
                        (int)controlPoint1.getX(), (int)controlPoint1.getY(),
                        (int)controlPoint2.getX(), (int)controlPoint2.getY(),
                        m_toState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                        m_toState.getY() + TM_State.STATE_RENDERING_WIDTH/2));
        }
        
        boolean startEqualsEnd = ((int)m_fromState.getX() == (int)m_toState.getX() &&
                ((int)m_fromState.getY() == (int)m_toState.getY()));
        
        if (!startEqualsEnd || (m_fromState == m_toState))
        {
            paintArrowHead(g);
        }
        m_action.paint(g, (int)actionLocation.getX(), (int)actionLocation.getY());
    }
    
    /** 
     * Paints the arrowhead of a transition to the graphics object in the graphics object's current colour.
     */
    public void paintArrowHead(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
        Point2D arrowLoc = getMidpoint();
        
        Point2D tangentVector = getArrowTangentVector(arrowLoc);
        double length = tangentVector.distance(new Point2D.Double(0, 0));
        double scaleFactor = 1.0/length * ARROWHEAD_LENGTH;
        AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        AffineTransform rotateClockwise = AffineTransform.getRotateInstance(Math.PI/ 2.0 + Math.PI/4.5, arrowLoc.getX(), arrowLoc.getY());
        AffineTransform rotateAnticlockwise = AffineTransform.getRotateInstance(-(Math.PI/ 2.0 + Math.PI/4.5), arrowLoc.getX(), arrowLoc.getY());
        AffineTransform translateToEndpoint = AffineTransform.getTranslateInstance(arrowLoc.getX(), arrowLoc.getY());
        
        Point2D p1 = (Point2D)tangentVector.clone();
        scale.transform(p1, p1);
        translateToEndpoint.transform(p1, p1);
        rotateClockwise.transform(p1, p1);
        
        Point2D p2 = (Point2D)tangentVector.clone();
        scale.transform(p2, p2);
        translateToEndpoint.transform(p2, p2);
        rotateAnticlockwise.transform(p2, p2);
        
        // Render it
        drawTriangle(g, arrowLoc,p1, p2);
        g2d.setStroke(originalStroke);
    }
    
    private void drawTriangle(Graphics g, Point2D p1, Point2D p2, Point2D p3)
    {
        GeneralPath triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        Graphics2D g2d = (Graphics2D)g;
        triangle.moveTo((float)p1.getX(), (float)p1.getY());
        triangle.lineTo((float)p2.getX(), (float)p2.getY());
        triangle.lineTo((float)p3.getX(), (float)p3.getY());
        triangle.lineTo((float)p1.getX(), (float)p1.getY());
        g2d.draw(triangle);
        g2d.fill(triangle);
    }
    
    private Point2D getArrowTangentVector(Point2D arrowLoc)
    {
        // Get the control point for the first half of the curve
        Point2D controlPoint;
        if (m_fromState != m_toState)
        {
            AffineTransform translate = AffineTransform.getTranslateInstance(0, -ACTION_TEXT_DISTANCE);
            QuadCurve2D curve = new QuadCurve2D.Float(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                    m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2, m_controlPtX, m_controlPtY,
                    m_toState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                    m_toState.getY() + TM_State.STATE_RENDERING_WIDTH/2);
            curve.subdivide(curve, null);
            controlPoint =  curve.getCtrlPt();
        }
        else
        {
            Point2D.Double startOfCurve = new Point2D.Double(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                    m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2);
            Point2D vectorToArrow = new Point2D.Double(arrowLoc.getX() - startOfCurve.getX(), arrowLoc.getY() - startOfCurve.getY());
            //get the vector orthogonal to it
            return new Point2D.Double(-vectorToArrow.getY(), vectorToArrow.getX());
        }
        return new Point2D.Double(arrowLoc.getX() - controlPoint.getX(), arrowLoc.getY() - controlPoint.getY());
    }
    
    /**
     * Returns true iff the specified panel coordinates are within the space taken up by the action.
     */
    public boolean actionContainsPoint(int x, int y, Graphics g)
    {
        String actionString = m_action.toString();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int symbolWidth = metrics.charWidth('_');
        
        Rectangle2D tempRect = metrics.getStringBounds(actionString, g);
        Rectangle2D boundingBox = new Rectangle2D.Double(tempRect.getX(),
                tempRect.getY(), actionString.length() * symbolWidth,
                metrics.getHeight());
        
        Point2D actionLocation = getActionLocation();
        
        AffineTransform trans = AffineTransform.getTranslateInstance(actionLocation.getX()
            - ((symbolWidth * actionString.length()) / 2), actionLocation.getY());
        Shape translated = trans.createTransformedShape(boundingBox);
        
        return translated.contains(x, y);
    }
    
    /**
     * Returns true iff the specified panel coordinates are within the space taken up by the
     * arrowhead in the centre of the this transition. The size of the containing box around the
     * arrow is the same as the size of the containing box around the action text in actionContainsPoint.
     */
    public boolean arrowContainsPoint(int x, int y, Graphics g)
    {
        String actionString = m_action.toString();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        
        Rectangle2D boundingBox = metrics.getStringBounds(actionString, g);
        
        Point2D arrowLocation = this.getMidpoint();
        AffineTransform trans =
            AffineTransform.getTranslateInstance(arrowLocation.getX() - metrics.stringWidth(actionString) / 2, arrowLocation.getY());
        Shape translated = trans.createTransformedShape(boundingBox);
        
        return translated.contains(x, y);
    }
    
    public Point2D getControlPoint()
    {
        return new Point2D.Float(m_controlPtX, m_controlPtY);
    }
    
    /**
     * Set the location of the control point for the curve representing this transition.
     * @param x,y  The new coordinates of the control point, in viewplane space.
     */
    public void setControlPoint(int x, int y)
    {
        m_controlPtX = x;
        m_controlPtY = y;
    }
    
    public void setSymbol(char symbol)
    {
        m_symbol = symbol;
        m_action.setInputSymbol(symbol);
    }
    
    public void setAction(TM_Action action)
    {
        m_action = action;
    }
    
    private Point2D getActionLocation()
    {
        Point2D arrowLoc = getMidpoint();
        
        if (m_fromState != m_toState)
        {
            AffineTransform translate = AffineTransform.getTranslateInstance(0, -ACTION_TEXT_DISTANCE);
            return translate.transform(arrowLoc, null);
        }
        else
        {
            Point2D startToMidpoint = new Point2D.Double(arrowLoc.getX() - m_fromState.getX(), arrowLoc.getY() - m_fromState.getY());
            // Scale the start to midpoint vector to account for text distance from midpoint
            double scaleFactor = (startToMidpoint.distance(new Point2D.Float(0,0)) + ACTION_TEXT_DISTANCE_FOR_LOOPS) / startToMidpoint.distance(new Point2D.Float(0,0));
            
            AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            Point2D scaled = new Point2D.Float();
            scale.transform(startToMidpoint, scaled);
            
            return new Point2D.Double(m_fromState.getX() + scaled.getX(), m_fromState.getY() + scaled.getY());
        }
    }
    
    public Point2D getMidpoint()
    {
        if (m_fromState != m_toState)
        {
            QuadCurve2D curve = new QuadCurve2D.Float(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2, m_fromState.getY()+ TM_State.STATE_RENDERING_WIDTH/2, m_controlPtX, m_controlPtY, m_toState.getX() + TM_State.STATE_RENDERING_WIDTH/2, m_toState.getY() + TM_State.STATE_RENDERING_WIDTH/2);
            curve.subdivide(curve, null);
            return curve.getP2();
        }
        else
        {
            CubicCurve2D curve = new CubicCurve2D.Float(m_fromState.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                        m_fromState.getY() + TM_State.STATE_RENDERING_WIDTH/2, m_controlPtX - TM_State.STATE_RENDERING_WIDTH,
                        m_controlPtY, m_controlPtX + TM_State.STATE_RENDERING_WIDTH , m_controlPtY, m_toState.getX()
                        + TM_State.STATE_RENDERING_WIDTH/2, m_toState.getY() + TM_State.STATE_RENDERING_WIDTH/2);
            curve.subdivide(curve, null);
            return curve.getP2();
        }
    }
    
    public static Point2D getControlPointGivenMidpoint(Point2D midpoint, TM_State fromState, TM_State toState)
    {
        double newCPX, newCPY;
        if (fromState == toState)
        {
            // Formula to find the control point given the location of the midpoint.
            // Calculated using the cubic bezier curve formula by pretending that both inner
            // control points are actually just mousePressedTransition's 'control point'.
            newCPX = 4.0/3.0 * (midpoint.getX() - (fromState.getX() + TM_State.STATE_RENDERING_WIDTH / 2) / 4.0);
            newCPY = 4.0/3.0 * (midpoint.getY() - (fromState.getY() + TM_State.STATE_RENDERING_WIDTH / 2) / 4.0);
        }
        else
        {
            // Quadratic curve formula to find the control point given the location of the 
            // midpoint of the curve.
            newCPX = 2.0 * midpoint.getX() - 0.5 * (fromState.getX() + TM_State.STATE_RENDERING_WIDTH / 2
                + toState.getX() + TM_State.STATE_RENDERING_WIDTH / 2);
            newCPY = 2.0 * midpoint.getY() - 0.5 * (fromState.getY() + TM_State.STATE_RENDERING_WIDTH / 2
                + toState.getY() + TM_State.STATE_RENDERING_WIDTH / 2);
        }
        return new Point2D.Float((float)newCPX, (float)newCPY);
    }
    
    public Shape getInputSymbolBoundingBox(Graphics g)
    {
        String actionString = m_action.toString();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int symbolWidth = metrics.charWidth('_');
        
        Rectangle2D boundingBox = metrics.getStringBounds("" + m_symbol, g);
        boundingBox = new Rectangle2D.Double(boundingBox.getMinX(),
             boundingBox.getMinY(),
             symbolWidth  + 2 * SELECTED_SYMBOL_BOX_PAD_X,
             boundingBox.getHeight() + 2 * SELECTED_SYMBOL_BOX_PAD_Y); // Add padding
        Point2D actionLocation = getActionLocation();
        
       AffineTransform trans = AffineTransform.getTranslateInstance(actionLocation.getX()
            - (symbolWidth * actionString.length()) / 2
            - SELECTED_SYMBOL_BOX_PAD_X
            + SELECTED_INPUT_SYMBOL_BOX_X_OFFSET,
            actionLocation.getY() - SELECTED_SYMBOL_BOX_PAD_Y);
        Shape translated = trans.createTransformedShape(boundingBox);
        
        return translated;
    }
    
    public Shape getOutputSymbolBoundingBox(Graphics g)
    {
        String actionString = m_action.toString();
        // rest of string without the output char.
        String restOfActionString = actionString.substring(0, actionString.length() - 1);
        char outputSymbol = actionString.charAt(actionString.length() - 1);
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int symbolWidth = metrics.charWidth('_');
        
        Rectangle2D boundingBox = metrics.getStringBounds("" + outputSymbol, g);
        
        boundingBox = new Rectangle2D.Double(boundingBox.getMinX(),
             boundingBox.getMinY(),
             symbolWidth + 2 * SELECTED_SYMBOL_BOX_PAD_X,
             boundingBox.getHeight() + 2 * SELECTED_SYMBOL_BOX_PAD_Y); // Add padding
        Point2D actionLocation = getActionLocation();
        
        AffineTransform trans = AffineTransform.getTranslateInstance(actionLocation.getX()
            - (symbolWidth * actionString.length()) / 2 // Centre text on action location
            + (symbolWidth * 2) // Shift two characters to the right
            - SELECTED_SYMBOL_BOX_PAD_X, actionLocation.getY());
        Shape translated = trans.createTransformedShape(boundingBox);
        
        return translated;
    }
    
    public String toString()
    {
        return m_controlPtX + " " + m_controlPtY + " " + m_fromState.getLabel() + " " + m_toState.getLabel();
    }
    
    private TM_State m_fromState;
    private TM_State m_toState;
    private TM_Action m_action;
    private char m_symbol;
    
    // Position of the control point for the curve.
    private int m_controlPtX = 0;
    private int m_controlPtY = 0;
}
