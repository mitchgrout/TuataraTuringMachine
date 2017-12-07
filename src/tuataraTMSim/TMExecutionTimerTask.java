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

import java.util.TimerTask;
import javax.swing.JOptionPane;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.Simulator;

/**
 * An extension of a timer task which simulates a machine on a timer. 
 * @author Jimmy
 */
public class TMExecutionTimerTask extends TimerTask
{
    /**
     * Creates a new instance of TMExecutionTimerTask.
     * @param panel The current graphics panel.
     * @param tapeDisp The current tape panel.
     * @param window The main window.
     */
    public TMExecutionTimerTask(MachineGraphicsPanel panel, TMTapeDisplayPanel tapeDisp, MainWindow window)
    {
        m_panel = panel;
        m_tapeDisp = tapeDisp;
        m_mainWindow = window;
    }
   
    /**
     * Run a step of the machine. If the machine throws any exception, it is caught, a messagebox
     * containing the relevant message is shown, and execution is halted.
     */
    public void run()
    {
        try
        {
            Simulator sim = m_panel.getSimulator();
            sim.step();
            m_panel.repaint();
            m_tapeDisp.repaint();
            if (sim.getCurrentState().isFinalState())
            {
                m_mainWindow.getConsole().logPartial(m_panel, sim.getConfiguration());
            }
            else
            {
                m_mainWindow.getConsole().logPartial(m_panel, String.format("%s %c ", sim.getConfiguration(), '\u02Eb')); 
            }
        }
        catch (TapeBoundsException e)
        {
            m_mainWindow.getConsole().logPartial(m_panel, e.getMessage());
            m_mainWindow.getConsole().endPartial();
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.TAPE_BOUNDS_ERR_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (UndefinedTransitionException e)
        {
            m_mainWindow.getConsole().logPartial(m_panel, e.getMessage());
            m_mainWindow.getConsole().endPartial();
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.TRANS_UNDEF_ERR_STR + " " + e.getMessage(), MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (NondeterministicException e)
        {
            m_mainWindow.getConsole().logPartial(m_panel, e.getMessage());
            m_mainWindow.getConsole().endPartial();
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.NONDET_ERR_STR + " " + e.getMessage(), MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
        }
        catch (ComputationCompletedException e)
        {
            m_mainWindow.getConsole().endPartial();
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.COMPUTATION_COMPLETED_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
            m_panel.getSimulator().resetMachine();
            m_panel.repaint();
        }
        catch (ComputationFailedException e)
        {
            m_mainWindow.getConsole().endPartial();
            cancel();
            m_mainWindow.stopExecution();
            JOptionPane.showMessageDialog(m_mainWindow,MainWindow.COMPUTATION_FAILED_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
            m_panel.getSimulator().resetMachine();
            m_panel.repaint();
        }
    }
   
    /**
     * Get the current graphics panel.
     * @return The current graphics panel.
     */
    public MachineGraphicsPanel getPanel()
    {
        return m_panel;
    }
    
    /**
     * Cancels the timer, stopping execution.
     * @return true if the timer is stopped successfully, false otherwise.
     */
    public boolean cancel()
    {
        boolean returner = super.cancel();
        m_mainWindow.setEditingEnabled(true);
        return returner;
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The current tape panel.
     */
    private TMTapeDisplayPanel m_tapeDisp;

    /**
     * The main window.
     */
    private MainWindow m_mainWindow;
}
