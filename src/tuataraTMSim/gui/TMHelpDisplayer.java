/*
 * TMHelpDisplayer.java
 *
 * Created on January 31, 2007, 3:41 PM
 *
 */

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


package tuataraTMSim.gui;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *
 * @author Jimmy
 */
public class TMHelpDisplayer extends JInternalFrame{
    
    /** Creates a new instance of TMHelpDisplayer */
    public TMHelpDisplayer() {
        initialize();
    }
    
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
            
            java.net.URL helpURL = TMHelpDisplayer.class.getResource(
                                "help/index.html");
            if (helpURL != null)
            {
                try
                {
                    jep.setPage(helpURL);
                } catch (IOException e)
                {
                    System.err.println("Attempted to read a bad URL: " + helpURL);
                }
            }
            else
            {
                System.err.println("Couldn't find file: help/index.html");
            }
            
            jep.addHyperlinkListener(new Hyperactive());
            
            //jep.setSize(640, 480);
            //myPanel.add(jep);
            JScrollPane editorScrollPane = new JScrollPane(jep);
            //editorScrollPane.setVerticalScrollBarPolicy(
            //                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(640, 480));
            editorScrollPane.setMinimumSize(new Dimension(100, 100));
            
            myPanel.add(editorScrollPane);
            
            //myPanel.add(new JButton("blah"));
            add(myPanel);
            
            pack();
            setLocation(100, 100);
        }
        catch(Exception e) {System.out.println("Caught exception: " +
            e.toString());}
        
        
    }
    
    
}

 /** Handle hyperlinks in the html help.  Code borrowed from the Sun Java 1.4.2 API Guide:
  *  http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JEditorPane.html
  */
 class Hyperactive implements HyperlinkListener {

     public void hyperlinkUpdate(HyperlinkEvent e) {
              if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                  JEditorPane pane = (JEditorPane) e.getSource();
                  if (e instanceof HTMLFrameHyperlinkEvent) {
                      HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                      HTMLDocument doc = (HTMLDocument)pane.getDocument();
                      doc.processHTMLFrameHyperlinkEvent(evt);
                  } else {
                      try {
                          pane.setPage(e.getURL());
                      } catch (Throwable t) {
                          t.printStackTrace();
                      }
                  }
              }
          }
 }
