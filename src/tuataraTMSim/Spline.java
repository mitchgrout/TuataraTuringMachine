//  ------------------------------------------------------------------
//
//  Copyright (c) 2006-2007 James Foulds and the University of Waikato
//
//  ------------------------------------------------------------------
//  This file is part of Tuatara Turing Machine Simulator.
//
//  Tuatara Turing Machine Simulator is free software: you can redistribute //  it and/or modify it under the terms of the GNU General Public License as
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
import java.awt.geom.*;
import tuataraTMSim.TM.TM_State;

/**
 * Utility functions for working with splines, which are used to render transitions in this program.
 * In these splines, three points are used: two fixed points, found by using TM_State objects, and a
 * given control point. The control point itself may not lie on the curve, but its location
 * influences the curvature. The midpoint is found using the control point, and is a point which
 * sits exactly on the curve. As such, most graphical manipulation involves the midpoint.
 */
public abstract class Spline
{
    /**
     * Build a loop spline from the given state, with the given control point.
     * @param ctrl The control point of the spline.
     * @param from The state the spline is both leaving and arriving at.
     * @return A spline looping to and from the given state.
     */
    public static CubicCurve2D buildLoopSpline(Point2D ctrl, TM_State from)
    {
        return new CubicCurve2D.Double(
                from.getX() + TM_State.STATE_RENDERING_WIDTH / 2, 
                from.getY() + TM_State.STATE_RENDERING_WIDTH / 2, 
                ctrl.getX() - TM_State.STATE_RENDERING_WIDTH,
                ctrl.getY(), 
                ctrl.getX() + TM_State.STATE_RENDERING_WIDTH,
                ctrl.getY(),
                from.getX() + TM_State.STATE_RENDERING_WIDTH / 2,
                from.getY() + TM_State.STATE_RENDERING_WIDTH / 2);
    }

    /**
     * Build a loop spline from the given state, with two given control points.
     * This spline is better for rendering, but shares its midpoint with the other cubic spline.
     * @param ctrl1 The first control point.
     * @param ctrl2 The second control point.
     * @param from The state the spline is both leaving and arriving at.
     * @return A spline looping to and from the given state.
     */
    public static CubicCurve2D buildLoopSpline(Point2D ctrl1, Point2D ctrl2, TM_State from)
    {
        return new CubicCurve2D.Double(
                from.getX() + TM_State.STATE_RENDERING_WIDTH / 2,
                from.getY() + TM_State.STATE_RENDERING_WIDTH / 2,
                ctrl1.getX(), ctrl1.getY(),
                ctrl2.getX(), ctrl2.getY(),
                from.getX() + TM_State.STATE_RENDERING_WIDTH/2,
                from.getY() + TM_State.STATE_RENDERING_WIDTH/2);
    }
    /**
     * Build an arc spline from the given state, to the other state, with the given control point.
     * The two states should be distinct.
     * @param ctrl The control point of the spline.
     * @param from The state the spline is leaving.
     * @param to The state the spline is arriving at.
     * @return The midpoint of the spline.
     */
    public static QuadCurve2D buildArcSpline(Point2D ctrl, TM_State from, TM_State to)
    {
        return new QuadCurve2D.Double(
                from.getX() + TM_State.STATE_RENDERING_WIDTH / 2, 
                from.getY()+ TM_State.STATE_RENDERING_WIDTH / 2, 
                ctrl.getX(), ctrl.getY(),
                to.getX() + TM_State.STATE_RENDERING_WIDTH / 2, 
                to.getY() + TM_State.STATE_RENDERING_WIDTH / 2);
    }

    /**
     * Determine the midpoint of our spline, given a known control point, and the  start and end
     * points.
     * @param ctrl The control point of the spline.
     * @param from The state the spline is leaving.
     * @param to The state the spline is arriving at.
     * @return The midpoint of the spline.
     */
    public static Point2D getMidPointFromControlPoint(Point2D ctrl, TM_State from, TM_State to)
    {
        // A loop
        if (from == to)
        {
            // Build the loop spline
            CubicCurve2D curve = Spline.buildLoopSpline(ctrl, from);

            // Get the left half of the spline
            curve.subdivide(curve, null);
            
            // The midpoint of the overall spline is the end vertex of this subdivided spline
            return curve.getP2();
        }
        else
        {
            // Build the arc spline
            QuadCurve2D curve = Spline.buildArcSpline(ctrl, from, to);

            // Get the left half of the spline
            curve.subdivide(curve, null);

            // The midpoint of the overall spline is the end vertex of this subdivided spline
            return curve.getP2();
        }
    }

    /**
     * Determine the control point of our spline, given a known midpoint, and the start and end
     * points.
     * @param midpoint The midpoint of the spline.
     * @param from The state the spline is leaving.
     * @param to The state the spline is arriving at.
     * @return The control point of the spline.
     */
    public static Point2D getControlPointFromMidPoint(Point2D mid, TM_State from, TM_State to)
    {
        // Creating a loop
        if (from == to)
        {
            // Cubic bezier curve formula to find the control point given the midpoint, pretending
            // that both inner control points are actually the control point of the transition.
            return new Point2D.Double(
                4.0 /3.0 * (mid.getX() - (to.getX() + TM_State.STATE_RENDERING_WIDTH / 2) / 4.0),
                4.0 /3.0 * (mid.getY() - (to.getY() + TM_State.STATE_RENDERING_WIDTH / 2) / 4.0));
        }
        else
        {
            // Quadratic curve formula to find the control point given the midpoint
            return new Point2D.Double(
                2.0 * mid.getX() - 0.5 * (from.getX() + TM_State.STATE_RENDERING_WIDTH / 2 
                                          + to.getX() + TM_State.STATE_RENDERING_WIDTH / 2), 
                2.0 * mid.getY() - 0.5 * (from.getY() + TM_State.STATE_RENDERING_WIDTH / 2 
                                          + to.getY() + TM_State.STATE_RENDERING_WIDTH / 2));
        }
    }

    /**
     * Get a vector tangent to the midpoint.
     * @param ctrl The control point of the spline.
     * @param mid The midpoint of the spline.
     * @param from The state the spline is leaving.
     * @param to The state the spline is arriving at.
     * @return A vector tangent to the midpoint.
     */
    public static Point2D getMidPointTangentVector(Point2D ctrl, Point2D mid, TM_State from, TM_State to)
    {
        // NOTE: Although we could derive either ctrl or mid from each other, the current decision
        // is to have the user pass both. The reason is, the caller already has ctrl, and needs mid
        // for later computations. So it is better to pass both to prevent unnecessary computations.

        // A loop
        if (from == to)
        {
            // In a loop, the vector between the state and the control point is tangential to the
            // control point
            Point2D startOfCurve = new Point2D.Double(
                    from.getX() + TM_State.STATE_RENDERING_WIDTH / 2,
                    from.getY() + TM_State.STATE_RENDERING_WIDTH /2);
            Point2D vectorToArrow = new Point2D.Double(
                    mid.getX() - startOfCurve.getX(),
                    mid.getY() - startOfCurve.getY());
            return new Point2D.Double(-vectorToArrow.getY(), vectorToArrow.getX());
        }
        else
        {
            // Build the arc spline
            QuadCurve2D curve = Spline.buildArcSpline(ctrl, from, to);

            // Get the left half of the spline
            curve.subdivide(curve, null);

            // Get the control point of the left segment
            Point2D left_ctrl = curve.getCtrlPt();
            return new Point2D.Double(
                    mid.getX() - left_ctrl.getX(),
                    mid.getY() - left_ctrl.getY());
        }
    }
}
