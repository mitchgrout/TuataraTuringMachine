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
import tuataraTMSim.commands.ConfigureAlphabetCommand;
import tuataraTMSim.TM.Alphabet;
import tuataraTMSim.TM.Tape;

/**
 *
 * @author Jimmy
 */
public class AlphabetSelectorInternalFrame extends JInternalFrame
{    
    /**
     * Creates a new instance of AlphabetSelectorInternalFrame
     * */
    public AlphabetSelectorInternalFrame(MainWindow parent)
    {
        initComponents();
        m_parent = parent;
    }
    
    public void initComponents()
    {
        this.setLayout(new BorderLayout());
        
        m_letters.clear();
        m_digits.clear();
        blankCheckBox.setSelected(false);
        
        setTitle("Please select the symbols for the alphabet");
                
        JPanel selectAlphabetical = new JPanel();
        selectAlphabetical.setLayout(new BorderLayout());
        selectAlphabetical.setBorder(BorderFactory.createTitledBorder("Alphabetical Symbols"));
                
        JPanel alphabetSoup = new JPanel();
        alphabetSoup.setLayout(new GridLayout(6,5));
        for (char c = 'A'; c <= 'Z'; c++)
        {
            JCheckBox bc = new JCheckBox("" + c);
            alphabetSoup.add(bc);
            m_letters.add(bc);
        }
        selectAlphabetical.add(alphabetSoup, BorderLayout.CENTER);
        JPanel aButtonPanel = new JPanel();
        JButton aSelectAll = new JButton("Select all");
        JButton aSelectNone = new JButton("Select none");
        
        aSelectAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (JCheckBox l : m_letters)
                {
                    l.setSelected(true);
                }
            }
        });
        
        aSelectNone.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (JCheckBox l : m_letters)
                {
                    l.setSelected(false);
                }   
            }
        });
        
        aButtonPanel.add(aSelectAll);
        aButtonPanel.add(aSelectNone);
        selectAlphabetical.add(aButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(selectAlphabetical, BorderLayout.WEST);
        
        JPanel selectNumerical = new JPanel();
        selectNumerical.setLayout(new BorderLayout());
        selectNumerical.setBorder(BorderFactory.createTitledBorder("Numerical Symbols"));
                
        JPanel miscPanel = new JPanel();
        miscPanel.setLayout(new BorderLayout());
        
        JPanel numericalSoup = new JPanel();
        numericalSoup.setLayout(new GridLayout(4,5));
        for (int c = 0; c <= 9; c++)
        {
            JCheckBox bc = new JCheckBox("" + c);
            numericalSoup.add(bc);
            m_digits.add(bc);
        }
        selectNumerical.add(numericalSoup, BorderLayout.CENTER);
        JPanel nButtonPanel = new JPanel();
        JButton nSelectAll = new JButton("Select all");
        JButton nSelectNone = new JButton("Select none");
        
        nSelectAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (JCheckBox d : m_digits)
                {
                    d.setSelected(true);
                }
            }
        });
        
        nSelectNone.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (JCheckBox d : m_digits)
                {
                    d.setSelected(false);
                }
            }
        });
        
        nButtonPanel.add(nSelectAll);
        nButtonPanel.add(nSelectNone);
        selectNumerical.add(nButtonPanel, BorderLayout.SOUTH);
        miscPanel.add(selectNumerical, BorderLayout.NORTH);
        getContentPane().add(miscPanel, BorderLayout.EAST);
        
        JPanel blankPanel = new JPanel();
        blankPanel.add(blankCheckBox);
        blankPanel.setBorder(BorderFactory.createTitledBorder(""));
        miscPanel.add(blankPanel, BorderLayout.SOUTH);
        
        JPanel southButtons = new JPanel();
        southButtons.setLayout(new BorderLayout());
        JPanel finish = new JPanel();
        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Alphabet tempA = new Alphabet();
                for (JCheckBox d : m_digits)
                {
                    tempA.setSymbol(d.getText().charAt(0), d.isSelected());
                }
                for (JCheckBox l : m_letters)
                {
                    tempA.setSymbol(l.getText().charAt(0), l.isSelected());
                }
                tempA.setSymbol(Tape.BLANK_SYMBOL, blankCheckBox.isSelected());
                
                Alphabet oldAlphabet =(Alphabet)m_panel.getSimulator().getMachine().getAlphabet().clone();
                
                if (m_panel.getSimulator().getMachine().isConsistentWithAlphabet(tempA))
                {
                    m_panel.doCommand(new ConfigureAlphabetCommand(m_panel, oldAlphabet, tempA));
                    setVisible(false);
                    try {setSelected(false);} catch (PropertyVetoException e2) {}
                }
                else
                {
                    int choice = JOptionPane.showOptionDialog(null, "There are transitions containing"
                            + " symbols that are not in this alphabet.  Delete these transitions?",
                            "Configure alphabet", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, "Cancel");
                    if (choice == JOptionPane.YES_OPTION)
                    {
                        m_panel.doCommand(new ConfigureAlphabetCommand(m_panel, oldAlphabet, tempA));
                        m_panel.getSimulator().getMachine().removeInconsistentTransitions(m_panel);
                        setVisible(false);
                        try { setSelected(false); }
                        catch (PropertyVetoException e2) { }
                        // This causes a repaint also
                        m_panel.deselectSymbol();
                    }
                    else if (choice == JOptionPane.CANCEL_OPTION)
                    {
                        // Leave window open
                    }
                    else if (choice == JOptionPane.NO_OPTION)
                    {
                        m_panel.doCommand(new ConfigureAlphabetCommand(m_panel, oldAlphabet, tempA));
                        setVisible(false);
                        try { setSelected(false); }
                        catch (PropertyVetoException e2) { }
                    }
                }
                m_panel.setModifiedSinceSave(true);
            }
        });
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                try { setSelected(false); }
                catch (PropertyVetoException e2) { }
            }
        });
        
        this.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameDeactivated(InternalFrameEvent e) {
                m_parent.handleLostFocus();
                m_parent.getGlassPane().setVisible(false);
                
                // Fix the bug where pressing spacebar doesnt reach the system
                // and merely makes an annoying beep instead.
                // presumably the problem is that the focus owner becomes null
                // when the internal frame was deactivated.
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                //kfm.clearGlobalFocusOwner();
                m_parent.getContentPane().requestFocusInWindow();
            }
        });
        
        finish.add(ok);
        finish.add(cancel);
        southButtons.add(finish, BorderLayout.EAST);
        getContentPane().add(southButtons, BorderLayout.SOUTH);
    }
    
    /** 
     * Must be called before each time this internal frame is set to visible.
     */
    public void setPanel(TMGraphicsPanel panel)
    {
        m_panel = panel;
        synchronizeToAlphabet();
    }
    
    /**
     * Process a key event passed to this internal frame from the main window.
     * In particular, if the key event is a KEY_TYPED event which is a letter or a digit, toggle the
     * checkbox for that letter or digit.
     */
    public void handleKeyEvent(KeyEvent e)
    {
        if (e.getID() != KeyEvent.KEY_TYPED)
        {
            return;
        }
        if (e.isActionKey())
        {
            return;
        }
        if (e.getModifiers() != 0)
        {
            return;
        }
        char c = e.getKeyChar();
        if (Character.isDigit(c))
        {
            for (JCheckBox bc : m_digits)
            {
                if (bc.getText().charAt(0) == c)
                {
                    bc.setSelected(!bc.isSelected());
                }
            }
        }
        else if (Character.isLetter(c))
        {
            c = Character.toUpperCase(c);
            for (JCheckBox bc : m_letters)
            {
                if (bc.getText().charAt(0) == c)
                {
                    bc.setSelected(!bc.isSelected());
                }
            }
        }
        // TODO handle blank (space)?  The trouble is that spacebar
        // conflicts with the ordinary usage of spacebar - to press
        // the selected button/checkbox.
    }
    
    private void synchronizeToAlphabet()
    {
        for (JCheckBox d : m_digits)
        {
            d.setSelected(m_panel.getAlphabet().containsSymbol(d.getText().charAt(0)));
        }
        for (JCheckBox l : m_letters)
        {
            l.setSelected(m_panel.getAlphabet().containsSymbol(l.getText().charAt(0)));
        }
        blankCheckBox.setSelected(m_panel.getAlphabet().containsSymbol(Tape.BLANK_SYMBOL));
        
    }
    
    ArrayList<JCheckBox> m_letters = new ArrayList<JCheckBox>();
    ArrayList<JCheckBox> m_digits = new ArrayList<JCheckBox>();
    JCheckBox blankCheckBox = new JCheckBox("_ (blank symbol)");
    TMGraphicsPanel m_panel;
    MainWindow m_parent;
}
