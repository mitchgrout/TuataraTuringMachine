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

import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.io.File;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/** 
 * An internal frame containing a panel for displaying a machine.
 * @author Jimmy
 */
public class MachineInternalFrame extends JInternalFrame
{ 
    /** 
     * Creates a new instance of MachineInternalFrame.
     * @param gfxPanel The current graphics panel.
     */
    public MachineInternalFrame(MachineGraphicsPanel gfxPanel)
    {
        // TODO: Justify this super call's arguments.
        super("hello",true,true,true, true);
        m_gfxPanel = gfxPanel;
        
        this.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameActivated(InternalFrameEvent e)
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
            }
            
            public void internalFrameClosed(InternalFrameEvent e)
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
            }

            public void	internalFrameDeactivated(InternalFrameEvent e)
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
                m_gfxPanel.deselectSymbol();
            }
            
            public void	internalFrameDeiconified(InternalFrameEvent e)
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
            }
            
            public void	internalFrameIconified(InternalFrameEvent e)
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
            }

            public void	internalFrameOpened(InternalFrameEvent e) 
            {
                m_gfxPanel.getMainWindow().updateUndoActions();
            }
        });   
    }
    
    /** 
     * Gets the current graphics panel.
     * @return The current graphics panel
     */
    public MachineGraphicsPanel getGfxPanel()
    {
        return m_gfxPanel;
    }
   
    /**
     * Update the title of the frame to reflect the filename of the underlying machine.
     */
    public void updateTitle()
    {
        File file = m_gfxPanel.getFile();
        String titleString = "";
        if (m_gfxPanel.isModifiedSinceSave())
        {
            titleString = "* ";
        }
        if (file == null)
        {
            titleString += "untitled";
        }
        else
        {
            titleString += file.getName();
        }
        setTitle(titleString);
    }
    
    /**
     * Sets this internal frame's pointer to its scroll pane, but doesnt actually do anything in the
     * way of adding the scroll pane to the component.
     * @param sp The scroll panel to track.
     */
    public void setScrollPane(JScrollPane sp)
    {
        m_sp = sp;
    }
   
    /**
     * Get the center of the frame viewport.
     * @return The center of the frame viewport.
     */
    public Point2D getCenterOfViewPort()
    {
        if (m_sp == null)
        {
            return new Point2D.Float(0.0f,0.0f);
        }
        JViewport vp = m_sp.getViewport();
        Rectangle vpRect = vp.getViewRect();
        return new Point2D.Double(vpRect.getCenterX(), vpRect.getCenterY());
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_gfxPanel;

    /**
     * The scroll panel to track.
     */
    private JScrollPane m_sp;
}
