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
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.Simulator;
import tuataraTMSim.machine.Tape;

/**
 * An extension of a timer task which simulates a machine on a timer. 
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
        // TODO: Since sim.step() has the potential to block [by prompting the user for a choice],
        //       we may end up with the simulation 'skipping' a state; this is because the timer
        //       queues the next run of the task while this is blocking, and immediately run after
        //       the prompt is finished.

        MainWindow inst = MainWindow.getInstance();

        try
        {
            Simulator sim = m_panel.getSimulator();
            // Pre-validate the machine
            String result = sim.getMachine().hasUndefinedSymbols();
            if (result != null)
            {
                inst.getConsole().log("Cannot simulate %s: %s.",
                        m_panel.getFrame().getTitle(), result);
                Global.showErrorMessage("Execute", "Cannot simulate: %s.", result);
                cancel();
                return;
            }
            // If we are just starting, write out the input on the tape
            if (sim.getCurrentState() == null)
            {
                Tape tape = inst.getTape();
                // Issue a minor warning to the console if the r/w head is not in the leftmost cell;
                // continue execution
                if (tape.headLocation() != 0)
                {
                    inst.getConsole().log("Warning: Tape head has not been reset.");
                }
                inst.getConsole().logPartial(m_panel, "Input: %s\n",
                        tape.getPartialString(tape.headLocation(),
                                              tape.getLength() - tape.headLocation()));
            }
            sim.step();
            m_panel.repaint();
            m_tapeDisp.repaint();
            if (sim.isHalted())
            {
                inst.getConsole().logPartial(m_panel, sim.getConfiguration());
                inst.getConsole().endPartial();
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
            inst.getConsole().log("Simulation of %s finished: %s.",
                    m_panel.getFrame().getTitle(), msg);
            Global.showInfoMessage(MainWindow.HALTED_MESSAGE_TITLE_STR,
                    "Simulation finished: %s.", msg);
            m_panel.getSimulator().resetMachine();
            m_panel.repaint();
        }
        // Machine halted unexpectedly
        catch (Exception e)
        {
            cancel();
            inst.stopExecution();

            String msg = m_panel.getErrorMessage(e);
            inst.getConsole().log("Simulation of %s halted unexpectedly: %s.",
                    m_panel.getFrame().getTitle(), msg);
            Global.showErrorMessage(MainWindow.HALTED_MESSAGE_TITLE_STR,
                    "Simulation halted unexpectedly: %s.", msg);
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
