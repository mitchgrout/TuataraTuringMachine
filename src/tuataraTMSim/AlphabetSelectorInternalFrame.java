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
import tuataraTMSim.commands.JoinCommand;
import tuataraTMSim.commands.RemoveInconsistentTransitionsCommand;
import tuataraTMSim.machine.Alphabet;
import tuataraTMSim.machine.Tape;
import tuataraTMSim.machine.Transition;

/**
 * An frame used to select the current alphabet for a machine.
 * @author Jimmy
 */
public class AlphabetSelectorInternalFrame extends JInternalFrame
{    
    /**
     * Creates a new instance of AlphabetSelectorInternalFrame.
     * */
    public AlphabetSelectorInternalFrame()
    {
        initComponents();
    }
   
    /**
     * Setup the frame.
     */
    public void initComponents()
    {
        // TODO: Make private?
        this.setLayout(new BorderLayout());
        
        m_letters.clear();
        m_digits.clear();
        m_blank.setSelected(false);
       
        // Set frame title
        setTitle("Please select the symbols for the alphabet");
                
        
        // Controls for alphabetical characters:
        JPanel selectAlphabetical = new JPanel();
        selectAlphabetical.setLayout(new BorderLayout());
        selectAlphabetical.setBorder(BorderFactory.createTitledBorder("Alphabetical Symbols"));
        
        // Create and add all check buttons to a 6 row / 5 col grid 
        JPanel alphabetSoup = new JPanel();
        alphabetSoup.setLayout(new GridLayout(6,5));
        for (char c = 'A'; c <= 'Z'; c++)
        {
            JCheckBox bc = new JCheckBox("" + c);
            alphabetSoup.add(bc);
            m_letters.add(bc);
        }
        selectAlphabetical.add(alphabetSoup, BorderLayout.CENTER);

        // Create and add the select all/none buttons
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
       

        // For alignment, numeric and blank panels will be put in this panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());


        // Controls for numerical characters:
        JPanel selectNumerical = new JPanel();
        selectNumerical.setLayout(new BorderLayout());
        selectNumerical.setBorder(BorderFactory.createTitledBorder("Numerical Symbols"));
        
        // Create and add all check buttons to a 4 row / 3 col grid
        JPanel numericalSoup = new JPanel();
        numericalSoup.setLayout(new GridLayout(4, 3));
        for (int c = 0; c <= 9; c++)
        {
            JCheckBox bc = new JCheckBox("" + c);
            numericalSoup.add(bc);
            m_digits.add(bc);
        }
        selectNumerical.add(numericalSoup, BorderLayout.CENTER);

        // Create and add the select all/none buttons
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
        rightPanel.add(selectNumerical, BorderLayout.NORTH);
        
        // Controls for blank character:
        JPanel blankPanel = new JPanel();
        blankPanel.add(m_blank);
        blankPanel.setBorder(BorderFactory.createTitledBorder(""));
        rightPanel.add(blankPanel, BorderLayout.SOUTH);
        
        // Add the right pane containing the numeric and blank panels
        getContentPane().add(rightPanel, BorderLayout.EAST);


        // For alignment, specific alphabet and button panels will be put in this panel
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());


        // Controls for specific alphabets
        JPanel specificPanel = new JPanel();
        JButton sUnary = new JButton("Unary");
        JButton sBinary = new JButton("Binary");
        JButton sDecimal = new JButton("Decimal");

        sUnary.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Make the alphabet strictly unary
                for (JCheckBox a : m_letters)
                {
                    a.setSelected(false);
                }

                for (JCheckBox d : m_digits)
                {
                    char c = d.getText().charAt(0);
                    d.setSelected(c == '1');
                }

                m_blank.setSelected(true);
            }
        });

        sBinary.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Make the alphabet strictly binary
                for (JCheckBox a : m_letters)
                {
                    a.setSelected(false);
                }

                for (JCheckBox d : m_digits)
                {
                    char c = d.getText().charAt(0);
                    d.setSelected(c == '0' || c == '1');
                }

                m_blank.setSelected(true);
            }
        });

        sDecimal.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Make the alphabet strictly decimal
                for (JCheckBox a : m_letters)
                {
                    a.setSelected(false);
                }

                for (JCheckBox d : m_digits)
                {
                    char c = d.getText().charAt(0);
                    d.setSelected(c >= '0' && c <= '9');
                }

                m_blank.setSelected(true);
            }
        });

        specificPanel.add(sUnary);
        specificPanel.add(sBinary);
        specificPanel.add(sDecimal);
        southPanel.add(specificPanel, BorderLayout.WEST);
        

        // Controls for OK/Cancel buttons
        JPanel finish = new JPanel();
        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Set up an alphabet object corresponding to these options
                Alphabet newAlph = new Alphabet();
                for (JCheckBox d : m_digits)
                {
                    newAlph.setSymbol(d.getText().charAt(0), d.isSelected());
                }
                for (JCheckBox l : m_letters)
                {
                    newAlph.setSymbol(l.getText().charAt(0), l.isSelected());
                }
                newAlph.setSymbol(Tape.BLANK_SYMBOL, m_blank.isSelected());
                
                // Take a deep copy of the old alphabet
                Alphabet oldAlphabet = (Alphabet)m_panel.getSimulator().getMachine().getAlphabet().clone();
                
                // Can we copy the new alphabet across without issue?
                if (m_panel.getSimulator().getMachine().isConsistentWithAlphabet(newAlph))
                {
                    // Change the alphabet
                    m_panel.doCommand(new ConfigureAlphabetCommand(m_panel, oldAlphabet, newAlph));

                    // Hide the frame
                    setVisible(false);
                    try { setSelected(false); }
                    catch (PropertyVetoException e2) { }
                }
                else
                {
                    int choice = JOptionPane.showOptionDialog(null, 
                            "There are transitions in this machine contianing symbols not in this alphabet.\n" +
                            "Delete these transitions?", "Configure alphabet",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 
                            null, null, "Cancel");

                    switch (choice)
                    {
                        case JOptionPane.YES_OPTION:
                            // Get the set of inconsistent transitions
                            ArrayList<Transition> purge =
                                m_panel.getSimulator().getMachine().getInconsistentTransitions(newAlph);

                            // Change the alphabet, and remove inconsistent transitions
                            m_panel.doCommand(new JoinCommand(
                                    new RemoveInconsistentTransitionsCommand(m_panel, purge),
                                    new ConfigureAlphabetCommand(m_panel, oldAlphabet, newAlph)));

                            // Hide the frame
                            setVisible(false);
                            try { setSelected(false); }
                            catch (PropertyVetoException e2) { }

                            // This causes a repaint also
                            m_panel.deselectSymbol();
                            break;

                        case JOptionPane.NO_OPTION:
                            // Do not reconfigure alphabet, hide the frame
                            setVisible(false);
                            try { setSelected(false); }
                            catch (PropertyVetoException e2) { }

                        default:
                            // Leave window open
                            break;

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
                MainWindow.getInstance().handleLostFocus();
                MainWindow.getInstance().getGlassPane().setVisible(false);
                
                // Fix the bug where pressing spacebar doesnt reach the system
                // and merely makes an annoying beep instead.
                // presumably the problem is that the focus owner becomes null
                // when the internal frame was deactivated.
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                //kfm.clearGlobalFocusOwner();
                MainWindow.getInstance().getContentPane().requestFocusInWindow();
            }
        });
        
        finish.add(ok);
        finish.add(cancel);
        southPanel.add(finish, BorderLayout.EAST);
        getContentPane().add(southPanel, BorderLayout.SOUTH);
    }
    
    /** 
     * Must be called before each time this internal frame is set to visible.
     * @param panel The current graphics panel.
     */
    public void setPanel(MachineGraphicsPanel panel)
    {
        m_panel = panel;
        synchronizeToAlphabet();
    }
    
    /**
     * Process a key event passed to this internal frame from the main window.
     * In particular, if the key event is a KEY_TYPED event which is a letter or a digit, toggle the
     * checkbox for that letter or digit.
     * @param e The generating event.
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
   
    /**
     * Given the underlying alphabet, set all relevant checkboxes to match the alphabet.
     */
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
        m_blank.setSelected(m_panel.getAlphabet().containsSymbol(Tape.BLANK_SYMBOL));
        
    }
    
    /**
     * All checkboxes which represent letters.
     */
    private ArrayList<JCheckBox> m_letters = new ArrayList<JCheckBox>();

    /**
     * All checkboxes which represent digits.
     */
    private ArrayList<JCheckBox> m_digits = new ArrayList<JCheckBox>();

    /**
     * The checkbox which represents the blank character.
     */
    private JCheckBox m_blank = new JCheckBox("_ (blank symbol)");

    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
}
