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

package tuataraTMSim.machine.TM;

import java.beans.PropertyVetoException;
import java.util.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.Global;
import tuataraTMSim.machine.*;
import tuataraTMSim.MachineInternalFrame;
import tuataraTMSim.MainWindow;
import tuataraTMSim.TMGraphicsPanel;

/**
 * Encapsulates the whole system for a Turing machine, including the machine and its configuration.
 * In particular, this class handles the simulation of the machine, including validation of the
 * machine prior to execution.
 */
public class TM_Simulator extends Simulator<TM_Action, TM_Transition, TM_State, TM_Machine>
{  
    /**
     * Creates a new instance of TM_Simulator.
     * @param machine The machine to simulate.
     * @param tape The tape which the machine will read from.
     */
    public TM_Simulator(TM_Machine machine, Tape tape)
    {
        super(tape);
        m_machine = machine;
    }

    /**
     * Gets the machine that is being simulated.
     * @return The machine being simulated.
     */
    public TM_Machine getMachine()
    {
        return m_machine;
    }

    /**
     * Gets the current state that the machine is in.
     * @return The current state
     */
    public TM_State getCurrentState()
    {
        return m_state;
    }

    /**
     * Sets the current state that the machine is in.
     * @param state The new current state.
     */
    public void setCurrentState(TM_State state)
    {
        m_state = state;
    }

    /**
     * Set the graphics panel associated with this simulator.
     * @param panel The graphics panel associated with this simulator.
     */
    public void setPanel(TMGraphicsPanel panel)
    {
        m_panel = panel;
    }

    /** 
     * Determine if the machine is in a final state.
     * @return true if the machine is in a final state, false otherwise.
     */
    public boolean isHalted()
    {
        return m_state != null && m_state.isFinalState();
    }

    /**
     * Determine if the machine is in a final state, and the read/write head of the tape is in
     * the first cell of the tape.
     * @return true if the machine is in a final state, with the read/write head parked, false
     *         otherwise.
     */
    public boolean isAccepted()
    {
        return isHalted() && m_tape.isParked();
    }

    /**
     * Gets a string representation of the current configuration of the machine.
     * @return A string representation of the current configuration.
     */
    public String getConfiguration()
    {
        final String LAMBDA = "\u03BB";
        String head = m_tape.getPartialString(0, m_tape.headLocation());
        String tail = m_tape.getPartialString(m_tape.headLocation(), m_tape.getLength() - m_tape.headLocation());
        return String.format("(%s, %s, %s)", 
                head.length() == 0? LAMBDA : head,
                m_state.getLabel(),
                tail.length() == 0? LAMBDA : tail);
    }

    /**
     * Perform an iteration of the machine.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws ComputationFailedException If execution halts unexpectedly.
     */
    public void step()
        throws ComputationCompletedException, ComputationFailedException
    {
        // Machine has just started
        if (m_state == null)
        {
            ArrayList<TM_State> startStates = m_machine.getStartStates();
            if (startStates.size() == 0)
            {
                throw new ComputationFailedException("No start state");
            }
            else if (startStates.size() == 1)
            {
                m_state = startStates.get(0);
            }
            else
            {
                m_state = Global.promptSelection(startStates, "Please choose which start state to use", TM_State::getLabel);
                // User cancelled
                if (m_state == null)
                {
                    throw new ComputationFailedException("No start state chosen");
                }
            }
        }
        // Already running
        else try
        {
            ArrayList<TM_Transition> next = getNextTransitions();

            // No problems with regular states
            if (m_state.getSubmachine() == null)
            {
                if (next.size() == 0)
                {
                    m_state = m_machine.step(m_tape, m_state, null);
                }
                else if (next.size() == 1)
                {
                    m_state = m_machine.step(m_tape, m_state, next.get(0));
                }
                else
                {
                    TM_Transition t = Global.promptSelection(next, "Please select which transition to use", TM_Transition::toString);
                    if (t == null)
                    {
                        throw new ComputationFailedException("No transition chosen");
                    }
                    m_state = m_machine.step(m_tape, m_state, t);
                }
            }
            // Search for the frame for this submachine; if nonexistent, create one
            else
            {
                MainWindow inst = MainWindow.getInstance();
                TMGraphicsPanel gfx = null;
                for (TMGraphicsPanel child : m_panel.getChildren())
                {
                    if (child.getSimulator().getMachine() == m_state.getSubmachine())
                    {
                        gfx = child;
                        break;
                    }
                }

                if (gfx == null)
                {
                    gfx = new TMGraphicsPanel(m_state.getSubmachine(), inst.getTape(), null);
                    m_panel.addChild(gfx);
                    MachineInternalFrame frame = inst.newMachineWindow(gfx);
                    gfx.setFrame(frame);
                }

                // No reason to actually display the frame; the user may opt to show it if they wish
                // If the submachine has halted, we carry on in our machine, resetting the
                // submachine. Otherwise continue submachine execution.
                if (gfx.getSimulator().isHalted())
                {
                    if (next == null)
                    {
                        m_state = m_machine.step(m_tape, m_state, null);
                    }
                    else if (next.size() == 1)
                    {
                        m_state = m_machine.step(m_tape, m_state, next.get(0));
                    }
                    else
                    {
                        TM_Transition t = Global.promptSelection(next, "Please select which transition to use", TM_Transition::toString);
                        if (t == null)
                        {
                            throw new ComputationFailedException("No transition chosen");
                        }
                        m_state = m_machine.step(m_tape, m_state, t);
                    }
                    gfx.getSimulator().resetMachine();
                }
                else
                {
                    gfx.getSimulator().step();
                }
            }
        }
        catch (ComputationCompletedException e)
        {
            // Topmost machine should throw everything; submachines should not throw
            // ComputationCompletedException.
            if (m_panel.getParentPanel() == null || 
                m_panel.getParentPanel().getSimulator().m_state == null)
            {
                throw e;
            }
        }
    }

    /** 
     * Runs until the machine halts.
     * @param maxSteps The maximum number of iterations allowed for the computation. A value of zero
     *                 represents no limit. If this number is reached, simulation is aborted.
     * @return true if the machine halts in a finite amount of steps up until maxSteps, false otherwise.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws ComputationFailedException If execution halts, but the input is not accepted.
     */
    public boolean runUntilHalt(int maxSteps)
        throws ComputationCompletedException, ComputationFailedException 
    {
        // TODO: Not required by anything at this stage, however could be useful for almost-instant
        //       execution of machines. Considered for removal.

        int currentStep = 0;
        while (!isHalted())
        {
            step();
            currentStep++;
            if (currentStep >= maxSteps && !(maxSteps == 0))
            {
                break;
            }
        }
        return currentStep < maxSteps && isAccepted();
    }

    /** 
     * End the current computation, if any, setting the machine to its initial state.
     */
    public void resetMachine()
    {
        m_state = null;
    }

    /**
     * The machine being simulated.
     */
    protected TM_Machine m_machine;

    /**
     * The current state the machine is in.
     */
    protected TM_State m_state;

    /**
     * The owning graphics panel.
     */
    protected TMGraphicsPanel m_panel;
}
