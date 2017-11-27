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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyVetoException;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import tuataraTMSim.commands.ChangeSchemeCommand;
import tuataraTMSim.commands.JoinCommand;
import tuataraTMSim.commands.SchemeRelabelCommand;
import tuataraTMSim.TM.Alphabet;
import tuataraTMSim.TM.Tape;

/**
 * An frame used to change the naming scheme for new states in a machine.
 */
public class SchemeSelectorInternalFrame extends JInternalFrame
{    
    /**
     * Creates a new instance of SchemeSelectorInternalFrame.
     * @param parent The main window.
     * */
    public SchemeSelectorInternalFrame(MainWindow parent)
    {
        initComponents();
        m_parent = parent;
    }

    /**
     * Setup the frame.
     */
    public void initComponents()
    {
        // TODO: Make private?
        this.setLayout(new BorderLayout());
      
        // Set frame title
        setTitle("Please select the naming scheme for new states");
        
        // Controls for radio buttons
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(1, 2));
        m_general = new JRadioButton("General");
        m_normalized = new JRadioButton("Normalized");
        
        m_general.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_general.setSelected(true);
                m_normalized.setSelected(false);
            }
        });

        m_normalized.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                m_normalized.setSelected(true);
                m_general.setSelected(false);
            }
        });

        radioPanel.add(m_general);
        radioPanel.add(m_normalized);
        getContentPane().add(radioPanel, BorderLayout.NORTH);
        
        
        // Controls for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        JButton bRename = new JButton("OK & Rename");
        JButton bOK = new JButton("OK");
        JButton bCancel = new JButton("Cancel");

        bRename.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Determine the new naming scheme
                NamingScheme scheme = m_general.isSelected()?
                                     NamingScheme.GENERAL : NamingScheme.NORMALIZED;
                
                // Rename all commands, and update the machine
                m_panel.doJoinCommand(
                    new SchemeRelabelCommand(m_panel, scheme),
                    new ChangeSchemeCommand(m_panel, scheme));
           
                // Hide the frame
                setVisible(false);
                try { setSelected(false); }
                catch (PropertyVetoException e2) { }
                m_panel.setModifiedSinceSave(true);
            }
        });

        bOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Determine the new naming scheme
                NamingScheme scheme = m_general.isSelected()?
                                      NamingScheme.GENERAL : NamingScheme.NORMALIZED;

                // Force a rename for normalized machines
                if (scheme == NamingScheme.NORMALIZED)
                {
                    int choice = JOptionPane.showOptionDialog(null,
                            "Changing the naming scheme to normalized requires renaming all " +
                            "states in the machine. Click OK to confirm that you would like " + 
                            "to change to normalized naming, and rename all of your states.",
                            "Change naming scheme", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, "Cancel");

                    if (choice == JOptionPane.OK_OPTION)
                    {
                        // Update the machine, renaming
                        m_panel.doJoinCommand(
                            new SchemeRelabelCommand(m_panel, scheme),
                            new ChangeSchemeCommand(m_panel, scheme));
                    }
                    else
                    {
                        return;
                    }
                }
                else
                {
                    // Update machine, without renaming
                    m_panel.doCommand(new ChangeSchemeCommand(m_panel, scheme));
                }
                // Close the frame
                setVisible(false);
                try { setSelected(false); }
                catch (PropertyVetoException e2) { }
                m_panel.setModifiedSinceSave(true);
            }
        });

        bCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Close the frame
                setVisible(false);
                try { setSelected(false); }
                catch (PropertyVetoException e2) { }
            }
        });

        buttonPanel.add(bRename);
        buttonPanel.add(bOK);
        buttonPanel.add(bCancel);
       
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameDeactivated(InternalFrameEvent e)
            {
                m_parent.handleLostFocus();
                m_parent.getGlassPane().setVisible(false);
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                // kfm.clearGlobalFocusOwner();
                m_parent.getContentPane().requestFocusInWindow();
            }
        });
    }
    
    /** 
     * Must be called before each time this internal frame is set to visible.
     * @param panel The current graphics panel.
     */
    public void setPanel(TMGraphicsPanel panel)
    {
        m_panel = panel;
        NamingScheme scheme = m_panel.getSimulator().getMachine().getNamingScheme();
        if (scheme == NamingScheme.GENERAL)
        {
            m_general.setSelected(true);
            m_normalized.setSelected(false);
        }
        else
        {
            m_normalized.setSelected(true);
            m_general.setSelected(false);
        }
    }
    
    /**
     * Radio button indicating that the naming scheme for new states can be anything.
     */
    private JRadioButton m_general;

    /**
     * Radio button indicating that the naming scheme for new states should be normalized.
     */
    private JRadioButton m_normalized;

    /**
     * The current graphics panel.
     */
    private TMGraphicsPanel m_panel;

    /**
     * The main window.
     */
    private MainWindow m_parent;
}
