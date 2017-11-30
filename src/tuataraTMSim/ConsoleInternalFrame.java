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
    public static final Font FONT_USED = new Font("Dialog", Font.PLAIN, 16);

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
        m_text.setFont(FONT_USED);
        m_text.setLineWrap(true);
        m_text.setEditable(false);
        JScrollPane scroll = new JScrollPane(m_text);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        getContentPane().add(scroll);
    }

    /**
     * Get the current timestamp formatted as a string.
     * @return The current timestamp.
     */
    private String timestamp()
    {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
    }

    /**
     * Log a partial message to the console. Subsequent calls to logPartial will continue on the
     * same line, without adding a new timestamp.
     * @param panel The panel logging the text.
     * @param s The string to be logged.
     */
    public void logPartial(TMGraphicsPanel panel, String s)
    {
        // Last message was not partial; timestamp and log
        if (!m_partial)
        {
            // Do not add a newline, so we can continue logging after this message.
            m_text.append(String.format("[%s] %s", timestamp(), s));
            m_partial = true;
            m_panel = panel;
        }
        // Last message was a partial message; did the given panel send it?
        else if (m_panel == panel)
        {
            // Continue logging on the same line
            m_text.append(s);
        }
        // Last message was partial, and the panel did not send it.
        else
        {
            // TODO: Decide if this is necessary for the user to see
            m_text.append("Interrupted\n");
            // Begin logging on a new line
            m_text.append(String.format("[%s] %s", timestamp(), s));
            m_partial = true;
            m_panel = panel;
        }
    }

    /**
     * End partial logging. This prints a newline to the end of the current log text.
     */
    public void endPartial()
    {
        if (m_partial)
        {
            m_text.append("\n");
            m_panel = null;
            m_partial = false;
        }
    }

    /**
     * Log text to the console. Forces text to appear on a new line.
     * @param panel The panel logging the text.
     * @param s The string to be logged.
     */
    public void log(TMGraphicsPanel panel, String s)
    {
        // Finish any partial messages; log, and then finish the partial message again.
        endPartial();
        logPartial(panel, s);
        endPartial();
    }

    /**
     * Action to clear the console of all logged text.
     */
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
            m_partial = false;
            m_panel = null;
        }
    }

    /**
     * Action to save the text stored in the console to file.
     */
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
            log(null, "TODO");
        }
    }

    /**
     * The underlying text area used to store the logged text.
     */
    private JTextArea m_text;

    /**
     * Panel currently logging a partial message.
     */
    private TMGraphicsPanel m_panel;

    /**
     * Whether or not we are waiting for more information to add to the log.
     */
    private boolean m_partial;
}
