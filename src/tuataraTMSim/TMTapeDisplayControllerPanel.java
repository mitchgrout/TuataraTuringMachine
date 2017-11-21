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
import javax.swing.*;
import javax.swing.border.*;
import tuataraTMSim.exceptions.TapeBoundsException;

/** 
 * A panel containing a tape display panel and some buttons to move the read/write head.
 * @author Jimmy
 */
public class TMTapeDisplayControllerPanel extends JPanel
{
    /**
     * Number of pixels used for padding between subcomponents.
     */
    public static final int PADDING = 0;
    
    /**
     * Creates a new instance of TMTapeDisplayControllerPanel.
     * @param tapeDP The tape display panel.
     * @param windowParent The main window.
     * @param headToStartAction Action used to move the read/write head to the start.
     * @param eraseTapeAction Action used to erase the entire tape.
     * @param reloadAction Action used to reload the tape.
     */
    public TMTapeDisplayControllerPanel(TMTapeDisplayPanel tapeDP, MainWindow windowParent,
            Action headToStartAction, Action eraseTapeAction, Action reloadAction)
    {
        m_tapeDP = tapeDP;
        m_mainWindow = windowParent;
        initComponents(headToStartAction, eraseTapeAction, reloadAction);
    }
    
    /** 
     * Initialization.
     * @param headToStartAction Action used to move the read/write head to the start.
     * @param eraseTapeAction Action used to erase the entire tape.
     * @param reloadAction Action used to reload the tape.
     */
    public void initComponents(Action headToStartAction, Action eraseTapeAction, Action reloadAction)
    {
        setBackground(Color.WHITE);
        setFocusable(false); // TODO: make this work

        Border currentBorder = getBorder();
        Border innerBorder = BorderFactory.createEmptyBorder(PADDING,PADDING,PADDING,PADDING);
        setBorder(BorderFactory.createCompoundBorder(currentBorder, innerBorder));
        
        java.net.URL imageURL = MainWindow.class.getResource("images/tapeLeft.gif");
        ImageIcon tapeLeftIcon = new ImageIcon(imageURL);
        imageURL = MainWindow.class.getResource("images/tapeRight.gif");
        ImageIcon tapeRightIcon = new ImageIcon(imageURL);
        
        
        m_BStart = new JButton();
        m_BStart.setFocusable(false);
        
        m_BLeft = new JButton();
        m_BLeft.setFocusable(false);
        m_BLeft.setToolTipText("Move the read/write head to the left.");
        m_BLeft.setIcon(tapeLeftIcon);
        
        m_BRight = new JButton();
        m_BRight.setFocusable(false);
        m_BRight.setToolTipText("Move the read/write head to the right.");
        m_BRight.setIcon(tapeRightIcon);
        
        m_BClearTape = new JButton();
        m_BClearTape.setFocusable(false);
        
        m_BReloadTape = new JButton();
        m_BReloadTape.setFocusable(false);
        
        setLayout(new BorderLayout());
        
        JPanel leftButtonPanel = new JPanel();
        leftButtonPanel.setBackground(Color.WHITE);
        leftButtonPanel.setLayout(new BorderLayout());
        leftButtonPanel.setFocusable(false);
        leftButtonPanel.add(m_BStart, BorderLayout.WEST);
        leftButtonPanel.add(m_BLeft, BorderLayout.EAST);
                
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setLayout(new BorderLayout());
        rightButtonPanel.setFocusable(false);
        rightButtonPanel.add(m_BRight, BorderLayout.WEST);
        rightButtonPanel.add(m_BClearTape, BorderLayout.EAST);
        rightButtonPanel.add(m_BReloadTape, BorderLayout.CENTER);
        
        m_BStart.setAlignmentY(Component.TOP_ALIGNMENT);
        m_BLeft.setAlignmentY(Component.TOP_ALIGNMENT);
        m_BRight.setAlignmentY(Component.TOP_ALIGNMENT);
        m_BClearTape.setAlignmentY(Component.TOP_ALIGNMENT);

        add(leftButtonPanel,BorderLayout.WEST);
        add(m_tapeDP,BorderLayout.CENTER);
        add(rightButtonPanel, BorderLayout.EAST);

        m_BStart.setAction(headToStartAction);
        m_BStart.setText("");
        
        m_BLeft.addActionListener(new ActionListener() 
        {
             public void actionPerformed(ActionEvent e) 
             {
                 try
                 {
                    // Move the head one cell to the left.
                    m_tapeDP.getTape().headLeft();
                    repaint();
                 }
                 catch (TapeBoundsException e1) { }
             }
        });
        
        m_BRight.addActionListener(new ActionListener()
        {    
             public void actionPerformed(ActionEvent e) 
             {
                 // Move the head one cell to the right
                 m_tapeDP.getTape().headRight();
                 repaint();
             }
        });
        
        m_BClearTape.setAction(eraseTapeAction);
        m_BClearTape.setText("");
        m_BReloadTape.setAction(reloadAction);
        m_BReloadTape.setText("");
    }
    
    /** 
     * Enable/disable user editing operations/buttons etc.
     * @param isEnabled true if editing is enabled, false otherwise.
     */
    public void setEditingEnabled(boolean isEnabled)
    {
        m_BLeft.setEnabled(isEnabled);
        m_BRight.setEnabled(isEnabled);
        m_tapeDP.setEditingEnabled(isEnabled);
    }
   
    /**
     * The tape display panel.
     */
    private TMTapeDisplayPanel m_tapeDP;
    
    /**
     * The main window.
     */
    private MainWindow m_mainWindow;
    
    /**
     * Button for moving the read/write head left.
     */
    private JButton m_BLeft;
    
    /**
     * Button for moving the read/write head right.
     */
    private JButton m_BRight;
    
    /**
     * Button for moving the read/write head to the start.
     */
    private JButton m_BStart;
    
    /**
     * Button for clearing the tape.
     */
    private JButton m_BClearTape;
    
    /**
     * Button for reloading the tape.
     */
    private JButton m_BReloadTape;
}

/**
 * Action for moving the read/write head to the start of the tape.
 */
class HeadToStartAction extends AbstractAction
{
    /**
     * Create a new instance of HeadToStartAction.
     * @param text Description of the action.
     * @param icon Icon for the action.
     */
    public HeadToStartAction(String text, Icon icon)
    {
        super(text);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, "Move the read/write head to the start of the tape.");
    }

    /**
     * Move the read/write head to the leftmost end of the tape.
     * @param e The generating event.
     */
    public void actionPerformed(ActionEvent e) 
    {
        // Move r/w head to the left end of the tape
        m_tapeDP.getTape().resetRWHead();
        m_tapeDCP.repaint();
    }

    /**
     * Set the underlying tape display panel.
     * @param tapeDP The new tape display panel.
     */
    public void setTapeDP(TMTapeDisplayPanel tapeDP)
    {
        m_tapeDP = tapeDP;
    }

    /**
     * Set the underlying tape controller panel.
     * @param tapeDCP The new tape controller panel.
     */
    public void setTapeDCP(TMTapeDisplayControllerPanel tapeDCP)
    {
        m_tapeDCP = tapeDCP;
    }

    /**
     * The underlying tape display panel.
     */
    private TMTapeDisplayPanel m_tapeDP;

    /**
     * The underlying tape controller panel.
     */
    private TMTapeDisplayControllerPanel m_tapeDCP;
}

/**
 * Action for erasing the tape.
 */
class EraseTapeAction extends AbstractAction
{
    /**
     * Create a new instance of EraseTapeAction.
     * @param text Description of the action.
     * @param icon Icon for the action.
     */
    public EraseTapeAction(String text, Icon icon)
    {
        super(text);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, "Erase the tape.");
    }

    /**
     * Erase the tape.
     * @param e The generating event.
     */
    public void actionPerformed(ActionEvent e) 
    {
        // Wipe the tape.
        Object[] options = {"Ok", "Cancel"};
        // TODO: should disable keyboard here
        int result = JOptionPane.showOptionDialog(null, "This will erase the tape.  Do you want to continue?", "Clear tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (result == JOptionPane.YES_OPTION)
        {
            m_tapeDP.getTape().clearTape();
            m_tapeDCP.repaint();
        }
    }

    /**
     * Set the underlying tape display panel.
     * @param tapeDP The new tape display panel.
     */
    public void setTapeDP(TMTapeDisplayPanel tapeDP)
    {
        m_tapeDP = tapeDP;
    }

    /**
     * Set the underlying tape controller panel.
     * @param tapeDCP The new tape controller panel.
     */
    public void setTapeDCP(TMTapeDisplayControllerPanel tapeDCP)
    {
        m_tapeDCP = tapeDCP;
    }

    /**
     * The underlying tape display panel.
     */
    private TMTapeDisplayPanel m_tapeDP;

    /**
     * The underlying tape controller panel.
     */
    private TMTapeDisplayControllerPanel m_tapeDCP;
}

/**
 * Action for reloading the tape.
 */
class ReloadTapeAction extends AbstractAction
{
    /**
     * Create a new instance of HeadToStartAction.
     * @param text Description of the action.
     * @param icon Icon for the action.
     */
    public ReloadTapeAction(String text, Icon icon)
    {
        super(text);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, "Reload the tape from disk, discarding any changes since the last save.");
    }

    public void actionPerformed(ActionEvent e) 
    {
        // Wipe the tape.
        Object[] options = {"Ok", "Cancel"};
        // TODO: should disable keyboard here
        int result = 0;
        if (m_tapeDP.getFile() == null)
            JOptionPane.showOptionDialog(null, "This will erase the tape.  Do you want to continue?", "Reload tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        else
            JOptionPane.showOptionDialog(null, "This will reload the tape, discarding any changes.  Do you want to continue?", "Reload tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (result == JOptionPane.YES_OPTION)
        {
            m_tapeDP.reloadTape();
            m_tapeDCP.repaint();
        }
    }

    /**
     * Set the underlying tape display panel.
     * @param tapeDP The new tape display panel.
     */
    public void setTapeDP(TMTapeDisplayPanel tapeDP)
    {
        m_tapeDP = tapeDP;
    }

    /**
     * Set the underlying tape controller panel.
     * @param tapeDCP The new tape controller panel.
     */
    public void setTapeDCP(TMTapeDisplayControllerPanel tapeDCP)
    {
        m_tapeDCP = tapeDCP;
    }

    /**
     * The underlying tape display panel.
     */
    private TMTapeDisplayPanel m_tapeDP;

    /**
     * The underlying tape controller panel.
     */
    private TMTapeDisplayControllerPanel m_tapeDCP;
}
