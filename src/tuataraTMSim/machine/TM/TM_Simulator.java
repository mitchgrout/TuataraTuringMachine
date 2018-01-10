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
import tuataraTMSim.machine.*;
import tuataraTMSim.MachineInternalFrame;
import tuataraTMSim.MainWindow;
import tuataraTMSim.TMGraphicsPanel;

/**
 * Encapsulates the whole system for a Turing machine, including the machine and its configuration.
 * In particular, this class handles the simulation of the machine, including validation of the
 * machine prior to execution.
 * @author Jimmy
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
     * Determine if the machine is in an accepting state.
     * @return true if the machine is in an accepting state, false otherwise.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean isHalted() throws NondeterministicException
    {
        // Guarantee that there is a unique halting state, hence guarantee the result makes sense.
        m_machine.validate();
        return m_state != null && m_state.isFinalState();
    }

    /**
     * Determine if the machine is in an accepting state, and the read/write head of the tape is in
     * the first cell of the tape.
     * @return true if the machine is in an accepting state, with the read/write head parked, false
     *         otherwise.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean isAccepted() throws NondeterministicException
    {
        // Guarantee that there is a unique halting state, hence guarantee the result makes sense.
        m_machine.validate();
        return (isHalted() && m_tape.isParked());
    }

    /**
     * Get the next possible transition that the machine can take in the next execution step.
     * This will either be a valid transition, or null.
     */
    public TM_Transition getNextTransition()
    {
        // Since the machine is assumed valid, there are three possibilities:
        // - No transition
        // - An exact match
        // - A default route via OTHERWISE_SYMBOL

        if (getCurrentState() == null)
        {
            return null;
        }

        TM_Transition otherwise = null;
        ArrayList<TM_Transition> out = getCurrentState().getTransitions();
        char currentInputSymbol = m_tape.read();

        for (TM_Transition t : m_state.getTransitions())
        {
            char inp = t.getAction().getInputChar();

            // An exact, guaranteed unique match
            if (inp == currentInputSymbol)
            {
                return t;
            }
            // A non-exact match; we will keep track of this
            else if (inp == TM_Machine.OTHERWISE_SYMBOL)
            {
                otherwise = t;
            }
        }
        // If we are here, there is no exact match, either a default route, or no transition.
        // These are both represented by the current value of otherwise
        // null -- no transition, non-null -- default route
        return otherwise;
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
     * @throws TapeBoundsException If the read/write head falls off the tape.
     * @throws UndefinedTransitionException If there is no transition for the machine to take.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public void step()
        throws TapeBoundsException, UndefinedTransitionException, 
               ComputationCompletedException, NondeterministicException
    {
        // Validation is cached, so long as no invalidating mutations are made, so this is not an
        // expensive call in general.
        m_machine.validate();

        // Machine has just started
        if (m_state == null)
        {
            // Guaranteed to exist by m_machine.validate()
            m_state = m_machine.getStartState();
        }
        // Already running
        else try
        {
            // No problems with regular states
            if (m_state.getSubmachine() == null)
            {
                m_state = m_machine.step(m_tape, m_state, getNextTransition());
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
                    m_state = m_machine.step(m_tape, m_state, getNextTransition());   
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
            if (m_panel.getParentPanel() == null)
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
     * @throws TapeBoundsException If the read/write head falls off the tape.
     * @throws UndefinedTransitionException If there is no transition for the machine to take.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean runUntilHalt(int maxSteps)
        throws TapeBoundsException, UndefinedTransitionException, 
               ComputationCompletedException, NondeterministicException 
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
