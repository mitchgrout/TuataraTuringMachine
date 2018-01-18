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
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;
import tuataraTMSim.commands.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;
import tuataraTMSim.machine.TM.*;

/**
 * A tool bar component.
 */
class ToolBarPanel extends JPanel
{
    /**
     * Horizontal padding between toolbar components.
     */
    protected static final int TOOLBAR_HGAP = 5;

    /**
     * Vertical padding between toolbar components.
     */
    protected static final int TOOLBAR_VGAP = 5;

    /**
     * Creates a new instance of ToolBarPanel.
     * @param parent The owning component.
     * @param manager The layout manager to use. 
     */
    public ToolBarPanel(Component parent, LayoutManager manager)
    {
        super(manager);
        m_parent = parent;

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                validate();
            }
        });
    }

    /**
     * Get the preferred size for this component.
     * @return The preferred size for this component.
     */
    public Dimension getPreferredSize()
    {
        Component[] components = this.getComponents();
        // Sort from top left to bottom right order.
        Arrays.sort(components, new Comparator<Component>()
        {
            // NOTE: This comparator imposes orderings that are inconsistent with equals
            public int compare(Component a, Component b)
            {
                if (a.getY() < b.getY())
                {
                    return -1;
                }
                if (a.getY() > b.getY())
                {
                    return 1;
                }
                if (a.getX() < b.getX())
                {
                    return -1;
                }
                if (a.getX() > b.getX())
                {
                    return 1;
                }
                return 0;
            }
        });

        int widthCount = 2 * TOOLBAR_HGAP;
        int maxHeight = 0;
        int numRows = 1;
        final int MINIMUM_SPACE_AT_END_OF_ROW = 2;
        // This seems to be hard-coded into swing, I had to find it by trial and error.  If
        // there is not this much space plus the flow layout horizontal gap left at the end, 
        // a new row is started.
        // TODO" check this is still correct on linux.

        for (int i = 0; i < components.length; i++)
        {
            Component c = components[i];

            widthCount += c.getWidth();
            if (i != 0)
            {
                widthCount += TOOLBAR_HGAP;
            }
            maxHeight = Math.max(c.getHeight(), maxHeight);
            if (widthCount >= m_parent.getWidth() - MINIMUM_SPACE_AT_END_OF_ROW)
            {
                numRows++;
                widthCount = 2 * TOOLBAR_HGAP + c.getWidth();
            }
        }
        numRows = Math.min(numRows, components.length); // No more rows than components

        return new Dimension(m_parent.getWidth(), numRows * (maxHeight + TOOLBAR_VGAP) + TOOLBAR_VGAP);
    }

    /**
     * The owning component.
     */
    private Component m_parent;
}
