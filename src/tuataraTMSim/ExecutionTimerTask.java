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
public class ExecutionTimerTask extends TimerTask
{
    /**
     * Creates a new instance of ExecutionTimerTask.
     * @param panel The current graphics panel.
     * @param tapeDisp The current tape panel.
     */
    public ExecutionTimerTask(MachineGraphicsPanel panel, TapeDisplayPanel tapeDisp)
    {
        m_panel = panel;
        m_tapeDisp = tapeDisp;
    }
   
    /**
     * Run a step of the machine. If the machine throws any exception, it is caught, a messagebox
     * containing the relevant message is shown, and execution is halted.
     */
    public void run()
    {
        MainWindow inst = MainWindow.getInstance();

        try
        {
            Simulator sim = m_panel.getSimulator();
            // Pre-validate the machine
            String result = sim.getMachine().hasUndefinedSymbols();
            if (result != null)
            {
                inst.getConsole().log(result);
                JOptionPane.showMessageDialog(inst, result, "Undefined transition", JOptionPane.WARNING_MESSAGE);
                return;
            }
            sim.step();
            m_panel.repaint();
            m_tapeDisp.repaint();
            if (sim.isHalted())
            {
                inst.getConsole().logPartial(m_panel, sim.getConfiguration());
            }
            else
            {
                inst.getConsole().logPartial(m_panel, "%s %c ", sim.getConfiguration(), Global.CONFIG_TEE);
            }
        }
        // Machine halted as expected
        catch (ComputationCompletedException e)
        {
            cancel();
            inst.stopExecution();

            String msg = m_panel.getErrorMessage(e);
            inst.getConsole().log(msg);
            JOptionPane.showMessageDialog(inst, msg,
                    MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
            m_panel.getSimulator().resetMachine();
            m_panel.repaint();
        }
        // Machine halted unexpectedly
        catch (Exception e)
        {
            cancel();
            inst.stopExecution();

            String msg = m_panel.getErrorMessage(e);
            inst.getConsole().log(msg);
            JOptionPane.showMessageDialog(inst, msg,
                    MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);           
        }
        inst.repaint();
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
        MainWindow.getInstance().setEditingEnabled(true);
        return returner;
    }
    
    /**
     * The current graphics panel.
     */
    private MachineGraphicsPanel m_panel;
    
    /**
     * The current tape panel.
     */
    private TapeDisplayPanel m_tapeDisp;
}
