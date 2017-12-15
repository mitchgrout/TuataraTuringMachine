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
import tuataraTMSim.machine.Tape;

/** 
 * A panel for displaying a Turing machine tape. Does not include any buttons, just the tape.
 * Assumes the use of a monospace font.
 * @author Jimmy
 */
public class TapeDisplayPanel extends JPanel
{ 
    /**
     * Monospaced font used by the panel.
     */
    protected static final Font MONOSPACE = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    /**
     * Width of a monospaced character, excluding padding. 
     */
    protected static final int CHAR_WIDTH = new Canvas().getFontMetrics(MONOSPACE).charWidth('_'); 

    /**
     * Horizontal padding around a tape cell.
     */
    public static final int CELLPADDING_X  = 4;

    /**
     * Vertical padding around a tape cell.
     */
    public static final int CELLPADDING_Y  = 2;

    /**
     * Horizontal padding around the entire tape.
     */
    public static final int TAPEPADDING_X = 5;

    /**
     * Vertical padding around the entire tape.
     */
    public static final int TAPEPADDING_Y = 2;
    
    /**
     * Creates a new instance of TapeDisplayPanel.
     * @param tape The underlying tape.
     */
    public TapeDisplayPanel(Tape tape)
    {
        this(tape, null);
    }
    
    /**
     * Creates a new instance of TapeDisplayPanel.
     * @param tape The underlying tape.
     * @param file The file associated with the tape.
     */
    public TapeDisplayPanel(Tape tape, File file)
    {
        m_tape = tape;
        m_file = file;
        initComponents();
    }
    
    /**
     * Initialization routine.
     */
    public void initComponents()
    {
        // TODO: Move into constructor.
        setFocusable(false);
        this.setPreferredSize(new Dimension(500,50));
        
        addMouseListener(new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent e)
            {    
                if (!m_isEditingEnabled)
                {
                    return;
                }

                // Shift r/w head to the cell that was clicked on.
                
                // Get the current graphics object
                Graphics g = getGraphics();
                Graphics2D g2d = (Graphics2D)g;

                // Measure how many cells there are
                FontMetrics metrics = g2d.getFontMetrics();
                // Need to take into account the 1px boundary
                int cellsFromLeft = (e.getX() - 1) / (CHAR_WIDTH + 2 * CELLPADDING_X);                
                int visibleCells = numCellsViewable(g);
                int startPos = m_tape.headLocation() - (visibleCells/2);
                if (startPos < 0)
                {
                    startPos = -1;
                    visibleCells--;
                }
                int newCell = Math.max(cellsFromLeft + startPos, 0);
                
                while (m_tape.headLocation() < newCell)
                {
                    m_tape.headRight();
                }
                
                while (m_tape.headLocation() > newCell)
                {
                    try { m_tape.headLeft(); }
                    catch (Exception e2) { break; }
                }
                repaint();
            }
        });
    }
    
    /** 
     * Paint this component to the given graphics object.
     * @param g The graphics object to render onto.
     */
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g2d.getFontMetrics();
        
        // Fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());      
        paintTape(g, TAPEPADDING_X, metrics.getAscent() + TAPEPADDING_Y);
    }
    
    /** 
     * Paint the tape on the graphics object at the given location.
     * @param g The graphics object to render onto.
     * @param x The X ordinate to render at.
     * @param y The Y ordinate to render at.
     */
    public void paintTape(Graphics g, int x, int y)
    {
        // Figure out what cells to render
        int visibleCells = numCellsViewable(g);
        boolean drawTapeEnd = false;
        int startPos = m_tape.headLocation() - (visibleCells/2);
        if (startPos < 0)
        {
            startPos = 0;
            drawTapeEnd = true;
            visibleCells--;
        }

        // Get the cell contents to render, including partial cells
        String tapeStr = m_tape.getPartialString(startPos, visibleCells + 1);
        
        // We need a monospaced font to ensure that the cells are all the same size.  This seemingly
        // cannot be set in the constructor as the graphics object has not been created until the
        // component is packed.
        Graphics2D g2d = (Graphics2D)g;
        g2d.setFont(MONOSPACE);
        FontMetrics metrics = g2d.getFontMetrics();
        
        int height = metrics.getHeight();
        int ascent = metrics.getAscent();
        int descent = metrics.getDescent();
        int xAccumulator = 0;
        if (drawTapeEnd)
        {
            paintTapeCell(g, '*', false, x + xAccumulator, y, ascent, descent);
            xAccumulator += CHAR_WIDTH + 2 * CELLPADDING_X;
        }
        for (int i = 0; i < tapeStr.length(); i++)
        {
            boolean isHeadLoc = m_tape.headLocation() == i + startPos;
            paintTapeCell(g, tapeStr.charAt(i), isHeadLoc, x + xAccumulator, y, ascent, descent);
            xAccumulator += CHAR_WIDTH + 2 * CELLPADDING_X;
        }
    }
    
    /** 
     * Paint a single cell of the tape.
     * @param g The graphics object to render onto.
     * @param c The character contained in this cell.
     * @param isHeadLocation true if the read/write head is in this cell, false otherwise.
     * @param x The X ordinate of the baseline of the text when painted.
     * @param y The Y ordinate of the baseline of the text when painted.
     * @param ascent Distance in pixels the font can extend above the baseline of the text.
     * @param descent Distance in pixels the font can extend below the baseline of the text.
     */
    public void paintTapeCell(Graphics g, char c, boolean isHeadLocation, int x, int y, int ascent, int descent)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g2d.getFontMetrics();
        
        g2d.setColor(Color.BLACK);
        g2d.draw(new Rectangle2D.Float(x - CELLPADDING_X, y - CELLPADDING_Y - ascent, CHAR_WIDTH + CELLPADDING_X * 2, ascent + descent + CELLPADDING_Y * 2));
        if (!isHeadLocation)
        {
            g2d.drawString("" + c, x, y);
        }
        else
        {
            g2d.fill(new Rectangle2D.Float(x - CELLPADDING_X, y - CELLPADDING_Y - ascent, CELL_WIDTH + CELLPADDING_X * 2, ascent + descent + CELLPADDING_Y * 2));
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + c, x, y);
        } 
    }
    
    /** 
     * A helper function that calculates how many cells will fit on the viewing panel at one time,
     * rounded down to the nearest whole number.
     * @param g The graphics object used to measure text.
     * @return The number of cells that will fit on the viewing panel.
     */
    private int numCellsViewable(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metrics = g2d.getFontMetrics();
        return getWidth() / (CHAR_WIDTH + 2 * CELLPADDING_X); // Integer division, rounding down.
    }
    
    /**
     * Get the tape currently associated with this panel.
     * @return The tape associated with this panel.
     */
    public Tape getTape()
    {
        return m_tape;
    }
    
    /**
     * Change the tape currently associated with this panel.
     * @param t The new tape associated with this panel.
     */
    public void setTape(Tape t)
    {
        m_tape = t;
    }
    
    /**
     * Handle a keystroke
     * @param e The generating event.
     * @return true if the event caused a change to the tape, false otherwise.
     */
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
            try { getTape().headLeft(); }
            catch (Exception e2) { }
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
           try { getTape().headLeft(); }
           catch (Exception e2) { }
           repaint();
           return true;
       }
       return false;
    }
    
    /** 
     * Serialize and save the tape to persistent storage.
     * @param file The file to save to.
     * @return true if the tape was saved successfully, false otherwise.
     */
    public boolean saveTapeAs(File file)
    {
        m_file = file;
        return Tape.saveTape(m_tape, file);
    }
    
    /**
     * Serialize and save the tape to persistent storage, using the associated file.
     * @return true if the tape was saved successfully, false otherwise.
     */
    public boolean saveTape()
    {
        if (m_file == null)
        {
            return false;
        }
        if (m_tape == null) // Shouldnt happen
        {
            return false;
        }
        return Tape.saveTape(m_tape, m_file);
    }

    /**
     * Load and deserialize a tape from persistent storage.
     * @param file The file of the saved tape.
     * @return true if the tape was loaded successfully, false otherwise.
     */
    public boolean loadTape(File file)
    {
        Tape t = Tape.loadTape(file);
        if (t == null)
        {
            return false;
        }
        m_tape.copyOther(t);
        return true;
    }
    
    /**
     * Get the file associated with this tape.
     * @return The file associated with this tape.
     */
    public File getFile()
    {
        return m_file;
    }
    
    /**
     * Set the file associated with this tape.
     * @param file The new file associated with this tape.
     */
    public void setFile(File file)
    {
        m_file = file;
    }
    
    /** 
     * Reload the tape from the file it was loaded from or last saved as, or clear the tape if it
     * was never loaded or saved at all.
     */
    public void reloadTape()
    {
        if (m_file == null)
        {
            m_tape.clearTape();
            return;
        }
        if (!loadTape(m_file))
        {
            JOptionPane.showMessageDialog(null, "Error reloading " + m_file.toString(), "Loading tape", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Set whether editing is enabled.
     * @param isEnabled true if editing is enabled, false otherwise.
     */
    public void setEditingEnabled(boolean isEnabled)
    {
        m_isEditingEnabled = isEnabled;
    }
    
    /**
     * The underlying tape.
     */
    private Tape m_tape;

    /**
     * The associated file.
     */
    private File m_file = null;

    /**
     * Whether editing is enabled.
     */
    private boolean m_isEditingEnabled = true;
}
