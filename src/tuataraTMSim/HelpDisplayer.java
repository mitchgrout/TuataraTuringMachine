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
import java.io.IOException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * An extension of a frame which displays help information, stored as HTML.
 */
public class HelpDisplayer extends JInternalFrame
{    
    /**
     * Creates a new instance of HelpDisplayer.
     */
    public HelpDisplayer()
    {
        initialize();
    }
    
    /**
     * Set up the frame, including attaching subcomponents and loading HTML pages. 
     */
    public void initialize()
    {
        try
        {
            this.setTitle("Help");
            this.setClosable(true);
            this.setResizable(false);
            JPanel myPanel = new JPanel();
            JEditorPane jep = new JEditorPane();
            HTMLEditorKit htmlKit = new HTMLEditorKit();
            HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
            jep.setEditorKit(htmlKit);
            jep.setEditable(false);
            
            java.net.URL helpURL = HelpDisplayer.class.getResource("help/index.html");
            if (helpURL != null)
            {
                try
                {
                    jep.setPage(helpURL);
                }
                catch (IOException e) { }
            }
            else { }
            
            jep.addHyperlinkListener(new Hyperactive());
            
            JScrollPane editorScrollPane = new JScrollPane(jep);
            editorScrollPane.setPreferredSize(new Dimension(640, 480));
            editorScrollPane.setMinimumSize(new Dimension(100, 100));
            
            myPanel.add(editorScrollPane);
            
            add(myPanel);
            
            pack();
            setLocation(100, 100);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "An error occured: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE); 
        }
    }
}

/**
 * Handle hyperlinks in the html help. Code borrowed from the Sun Java 1.4.2 API Guide:
 * http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JEditorPane.html
 */
class Hyperactive implements HyperlinkListener
{
    /**
     * Called when a hyperlink is clicked.
     * @param e The event information associated with the click event.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) 
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent)
            {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            }
            else
            {
                try { pane.setPage(e.getURL()); }
                catch (Throwable t) { }
            }
        }
    }
}
