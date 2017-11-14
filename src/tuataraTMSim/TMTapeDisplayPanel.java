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
import java.io.File;
import javax.swing.*;
import tuataraTMSim.TM.Tape;

/** A panel for displaying a turing machine tape.  Does not include any buttons, just the tape.
 *
 * @author Jimmy
 */
public class TMTapeDisplayPanel extends JPanel
{
    
    //padding constants determining empty space around components, in pixels.
    public static final int CELLPADDING_X  = 4;
    public static final int CELLPADDING_Y  = 2;
    public static final int TAPEPADDING_X = 5;
    public static final int TAPEPADDING_Y = 2;
    
    /**
     * Creates a new instance of TMTapeDisplayPanel 
     */
    public TMTapeDisplayPanel(Tape tape)
    {
        m_tape = tape;
        m_file = null;
        initComponents();
    }
    
    /**
     * Creates a new instance of TMTapeDisplayPanel 
     */
    public TMTapeDisplayPanel(Tape tape, File file)
    {
        m_tape = tape;
        m_file = file;
        initComponents();
    }
    
    /** Initialization routine.
     */
    public void initComponents()
    {
        setFocusable(false);
        this.setPreferredSize(new Dimension(500,50));
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                
                if (m_isEditingEnabled == false)
                    return;
                
                //shift r/w head to the cell that was clicked on.
                Graphics g = getGraphics();
                Graphics2D g2d = (Graphics2D)g;
                FontMetrics metrics = g2d.getFontMetrics();
                int charWidth = metrics.charWidth('_') + 2 * CELLPADDING_X;
                int cellsFromLeft = e.getX() / charWidth;
                
                int visibleCells = numCellsViewable(g);
                boolean drawTapeEnd = false;
                int startPos = m_tape.headLocation() - (visibleCells/2);
                int takeAccountOfTapeEnd = 0;
                if (startPos < 0)
                {
                    startPos = 0;
                    drawTapeEnd = true;
                    visibleCells--;
                    takeAccountOfTapeEnd = -1;
                }
                int newCell = Math.max(cellsFromLeft + startPos + takeAccountOfTapeEnd, 0);
                while (m_tape.headLocation() < newCell)
                {
                    m_tape.headRight();
                }
                
                while (m_tape.headLocation() > newCell)
                {
                    try
                    {     
                        m_tape.headLeft();
                    }
                    catch (Exception e2) {break;}
                }
                repaint();
                
                
            }
        });
    }
    
    /** Paint this component to the given graphics object.
     */
    protected void paintComponent(Graphics g)
    {
        int w = getWidth();
        int h = getHeight();
        
        Graphics2D g2d = (Graphics2D)g;
        
        
        //fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        FontMetrics metrics = g2d.getFontMetrics();
        int ascent = metrics.getAscent();
        
        paintTape(g, TAPEPADDING_X,ascent + TAPEPADDING_Y);
    }
    
    /** Paint the tape on the graphics object at the given location.
     */
    public void paintTape(Graphics g, int x, int y)
    {
        int visibleCells = numCellsViewable(g);
        boolean drawTapeEnd = false;

        int startPos = m_tape.headLocation() - (visibleCells/2);
        if (startPos < 0)
        {
            startPos = 0;
            drawTapeEnd = true;
            visibleCells--;
        }
        String tapeStr = m_tape.getPartialString(startPos, visibleCells + 1); //include any partial cell
        
        Graphics2D g2d = (Graphics2D)g;
        
        //we need a monospaced font to ensure that the cells are all the same size.
        //I can't seem to set the font in the constructor because the graphics object
        //associated with the panel has not been created until the component is packed.
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        FontMetrics metrics = g2d.getFontMetrics();
        int height = metrics.getHeight();
        int ascent = metrics.getAscent();
        int descent = metrics.getDescent();
        int xAccumulator = 0;
        if (drawTapeEnd)
        {
            paintTapeCell(g, '*', false, x + xAccumulator, y, ascent, descent);
            xAccumulator += metrics.charWidth('*') + 2 * CELLPADDING_X;
        }
        for (int i = 0; i < tapeStr.length(); i++)
        {
            boolean isHeadLoc = m_tape.headLocation() == i + startPos;
            paintTapeCell(g, tapeStr.charAt(i), isHeadLoc, x + xAccumulator, y, ascent, descent);
            xAccumulator += metrics.charWidth(tapeStr.charAt(i)) + 2 * CELLPADDING_X;
        }
    }
    
    /** Paint a single cell of the tape.
     * @param g     The graphics object to paint to.
     * @param c     The character (symbol) contained in this cell.
     * @param isHeadLocation    True iff the read/write head is currently in this cell.
     * @param x, y  Coordinates of the baseline of the text when painted.
     * @param ascent, descent   Distance in pixels the font can typically extend to above and
     *                  below the baseline of the text.
     */
    public void paintTapeCell(Graphics g, char c, boolean isHeadLocation, int x, int y, int ascent, int descent)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g2d.getFontMetrics();
        int width = metrics.charWidth(c);
        g2d.setColor(Color.BLACK);
        g2d.draw(new Rectangle2D.Float(x - CELLPADDING_X, y - CELLPADDING_Y - ascent, width + CELLPADDING_X * 2, ascent + descent + CELLPADDING_Y * 2));
        if (!isHeadLocation)
        {
            g2d.drawString("" + c, x, y);
        }
        else
        {
            g2d.fill(new Rectangle2D.Float(x - CELLPADDING_X, y - CELLPADDING_Y - ascent, width + CELLPADDING_X * 2, ascent + descent + CELLPADDING_Y * 2));
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + c, x, y);
        }
        
    }
    
    /** A helper function that calculates how many cells will fit on the
     *  viewing panel at one time, rounded down to the nearest whole number.
     */
    private int numCellsViewable(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g2d.getFontMetrics();
        int width = metrics.charWidth('_'); //assumes a monospace font.
        width += 2 * CELLPADDING_X;
        
        return getWidth() / width; //integer division, rounding down.
    }
    
    /** Get the tape currently associated with this panel.
     */
    public Tape getTape()
    {
        return m_tape;
    }
    
    /** Change the tape currently associated with this panel to t.
     */
    public void setTape(Tape t)
    {
        m_tape = t;
    }
    
    public boolean handleKeyEvent(KeyEvent e)
    {
       char c = e.getKeyChar();
       c = Character.toUpperCase(c);
       if (Character.isLetterOrDigit(c))
       {
            getTape().write(c);
            getTape().headRight();
            repaint();
            return true;
       }
       else if (c == ' ' || c == Tape.BLANK_SYMBOL)
       {
            getTape().write(Tape.BLANK_SYMBOL);
            getTape().headRight();
            repaint();
            return true;
       }
       else if (e.getKeyCode() == KeyEvent.VK_LEFT)
       {
            try {
            getTape().headLeft();
            } catch (Exception e2) {}
            repaint();
            return true;
       }
       else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
       {
            getTape().headRight();
            repaint();
            return true;
       }
       else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
       {
           getTape().write(Tape.BLANK_SYMBOL);
           try {
            getTape().headLeft();
            } catch (Exception e2) {}
           repaint();
           return true;
       }
       return false;
    }
    
    /** Save (serialize) a tape to persistent storage.
     *  @returns true IFF the serialization was successful.
     */
    public boolean saveTapeAs(String file)
    {
        m_file = new File(file);
        return Tape.saveTape(m_tape, file);
    }
    
    /** Save (serialize) the tape to persistent storage in
     *  the file this panel is associated with.
     *  @returns true IFF the serialization was successful.
     *  If there is no file associated with the panel,
     *  it will return false and no serialization will occur.
     */
    public boolean saveTape()
    {
        if (m_file == null)
            return false;
        if (m_tape == null) //shouldnt happen
            return false;
        return Tape.saveTape(m_tape, m_file.toString());
    }
    /** Load (deserialize) a tape from persistent storage.
     *  @param file     The file where the tape is stored.
     *  *  @returns true IFF the deserialization was successful.
     */
    public boolean loadTape(String file)
    {
        Tape t = Tape.loadTape(file);
        if (t == null)
            return false;
        m_tape.copyOther(t);
        return true;
    }
    
    public File getFile()
    {
        return m_file;
    }
    
    public void setFile(File file)
    {
        m_file = file;
    }
    
    
    /** Reload the tape from the file it was loaded from or last
     *  saved as, or clear the tape if it was never loaded or
     *  saved at all.
     */
    public void reloadTape()
    {
        if (m_file == null)
        {
            m_tape.clearTape();
            return;
        }
        if (!loadTape(m_file.toString()))
        {
            JOptionPane.showMessageDialog(null, "Error reloading " + m_file.toString(), "Loading tape", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void setEditingEnabled(boolean isEnabled)
    {
        m_isEditingEnabled = isEnabled;
    }
    
    private Tape m_tape;
    private File m_file = null;
    private boolean m_isEditingEnabled = true;
    
}
