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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import javax.swing.*;

/**
 * A frame for machines to log information to. This panel may be written to by every machine
 * currently loaded due to the fact that the accesses are mutually exclusive.
 */
public class ConsoleInternalFrame extends JInternalFrame
{
    /**
     * Text displayed when the console is opened, or cleared.
     */
    protected static final String SPLASH_TEXT = ">>> Tuatara Turing Machine Simulator 1.0 <<<";

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
        this.setSize(new Dimension(600, 400));

        // Only two components in this frame; a menu bar and text area

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JButton(m_clearAction));
        setJMenuBar(menuBar);

        // Text area
        m_text = new JTextArea();
        m_text.setFont(Global.FONT_DIALOG);
        m_text.setLineWrap(true);
        m_text.setEditable(false);
        JScrollPane scroll = new JScrollPane(m_text);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        getContentPane().add(scroll);

        // Clear and setup any text on m_text
        clear();
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
    public void logPartial(MachineGraphicsPanel panel, String s)
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
            m_text.append(" -- Interrupted\n");
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
     * @param s The string to be logged.
     */
    public void log(String s)
    {
        // Finish any partial messages, then log
        endPartial();
        m_text.append(String.format("[%s] %s\n", timestamp(), s));
    }

    /**
     * Clear the text area, and draw the splash text.
     */
    private void clear()
    {
        endPartial();
        m_text.setText(SPLASH_TEXT + "\n");
    }

    /**
     * The underlying text area used to store the logged text.
     */
    private JTextArea m_text;

    /**
     * Panel currently logging a partial message.
     */
    private MachineGraphicsPanel m_panel;

    /**
     * Whether or not we are waiting for more information to add to the log.
     */
    private boolean m_partial;

    /**
     * Action to clear the current console window.
     */
    private Action m_clearAction =
        new AbstractAction("Clear")
        {
            public void actionPerformed(ActionEvent e)
            {
                clear();
            }
        };
}
