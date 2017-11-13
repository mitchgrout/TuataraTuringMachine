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

import java.util.TimerTask;
import javax.swing.JOptionPane;
import tuataraTMSim.UndefinedTransitionException;
import tuataraTMSim.ComputationCompletedException;
import tuataraTMSim.NoStartStateException;
import tuataraTMSim.TapeBoundsException;

/**
 *
 * @author Jimmy
 */
public class TMExecutionTimerTask extends TimerTask
{
    
    /** Creates a new instance of TMExecutionTimerTask */
    public TMExecutionTimerTask(TMGraphicsPanel panel, TMTapeDisplayPanel tapeDisp, MainWindow window)
    {
        m_panel = panel;
        m_tapeDisp = tapeDisp;
        m_mainWindow = window;
    }
    
    public void run()
    {
        try
        {
            m_panel.getSimulator().step();
            m_panel.repaint();
            m_tapeDisp.repaint();
        }
        catch (TapeBoundsException e)
        {
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.TAPE_BOUNDS_ERR_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (UndefinedTransitionException e)
        {
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.TRANS_UNDEF_ERR_STR + " " + e.getMessage(), MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (NoStartStateException e)
        {
            cancel();
            
            m_mainWindow.stopExecution();
            
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.START_STATE_ERR_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (ComputationCompletedException e)
        {
            cancel();
            
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.COMPUTATION_COMPLETED_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
            m_panel.getSimulator().resetMachine();
            m_panel.repaint();
        }
    }
    
    public TMGraphicsPanel getPanel()
    {
        return m_panel;
    }
    
    public boolean cancel()
    {
        boolean returner = super.cancel();
        m_mainWindow.setEditingEnabled(true);
        return returner;
    }
    
    private TMGraphicsPanel m_panel;
    private TMTapeDisplayPanel m_tapeDisp;
    private MainWindow m_mainWindow;
}
