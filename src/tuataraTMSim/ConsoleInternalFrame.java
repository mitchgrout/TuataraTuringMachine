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
import java.awt.geom.*;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;
import tuataraTMSim.commands.*;
import tuataraTMSim.TM.*;

/**
 * A frame for machines to log information to. This panel may be written to by every machine
 * currently loaded due to the fact that the accesses are mutually exclusive.
 */
public class ConsoleInternalFrame extends JInternalFrame
{
    public ConsoleInternalFrame()
    {
        // Create a frame with the title "Console", which can be resized, closed, maximized, but
        // cannot be minimized.
        super("Console", true, true, true, false);

        initComponents();
    }

    /**
     * Initialize every component in this panel.
     */
    private void initComponents()
    {
        this.setSize(new Dimension(800, 600));
   
        // Only two components in this frame; a menu bar and text area

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(new JButton(new ClearAction("Clear")));
        menuBar.add(new JButton(new SaveAction("Save")));
        // Force everything leftward with glue
        menuBar.add(Box.createGlue());

        setJMenuBar(menuBar);

        // Text area
        m_text = new JTextArea();
        m_text.setLineWrap(true);
        m_text.setEditable(false);
        JScrollPane scroll = new JScrollPane(m_text);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        getContentPane().add(scroll);
    }

    /**
     * Log text to the console.
     * @param panel The panel logging the text.
     * @param s The string to be logged.
     */
    public void log(String s)
    {
        // Log the text with a given timestamp, plus the panel name
        m_text.append(String.format("[%s] %s\n",
                    DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()), s)); 
    }

    private class ClearAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveAction.
         * @param text Description of the action.
         */
        public ClearAction(String text)
        {
            super(text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
 
        public void actionPerformed(ActionEvent e)
        {
            m_text.setText(null);
        }
    }

    private class SaveAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveAction.
         * @param text Description of the action.
         */
        public SaveAction(String text)
        {
            super(text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e)
        {
            log("TODO");
        }
    }

    /**
     * The underlying text area used to store the logged text.
     */
    private JTextArea m_text;
}
