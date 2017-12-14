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

package tuataraTMSim.machine.DFSA;

import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;

/**
 * An abstraction of an object which simulates the execution of a given machine.
 */
public class DFSA_Simulator extends Simulator<DFSA_Action, DFSA_Transition, DFSA_State, DFSA_Machine>
{  
    /**
     * Creates a new instance of DFSA_Simulator.
     * @param machine The machine to simulate.
     * @param tape The tape which the machine will read from.
     */
    public DFSA_Simulator(DFSA_Machine machine, Tape tape)
    {
        super(tape);
        m_machine = machine;
    }

    /**
     * Gets the machine that is being simulated.
     * @return The machine being simulated.
     */
    public DFSA_Machine getMachine()
    {
        return m_machine;
    }
   
    /**
     * Gets the current state that the machine is in.
     * @return The current state.
     */
    public DFSA_State getCurrentState()
    {
        return m_state;
    }
    
    /**
     * Sets the current state that the machine is in.
     * @param state The new current state.
     */
    public void setCurrentState(DFSA_State state)
    {
        m_state = state;
    }

    /** 
     * Determine if the machine has finished executing.
     * @return true if the machine is has finished executing, false otherwise.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean isHalted() throws NondeterministicException
    {
        // Halt condition for a DFSA is no more input
        return m_tape.read() == Tape.BLANK_SYMBOL;
    } 
    
    /**
     * Determine if the machine is in an accepting state.
     * @return true if the machine is in an accepting state, false otherwise.
     */
    public boolean isAccepted() throws NondeterministicException
    {
        // Accepted input means no more input, and last state is accepting
        return isHalted() && m_state.isFinalState();
    }
 
    /**
     * Get the next possible transition that the machine can take in the next execution step. This
     * will either be a valid transition, or null if there is no next transition to take.
     * @return The next possible transition that the machine can take.
     */
    public DFSA_Transition getNextTransition()
    {
        // Since the machine is assumed valid, there is always a transition, unless we are not running
        if (m_state == null)
        {
            return null;
        }

        for (DFSA_Transition trans : m_state.getTransitions())
        {
            if (trans.getAction().getInputChar() == m_tape.read())
            {
                return trans;
            }
        }

        // Unreachable
        return null;
    }

    /**
     * Gets a string representation of the current configuration of the machine.
     * @return A string representation of the current configuration.
     */
    public String getConfiguration()
    {
        return m_state.getLabel();
    }
 
    /**
     * Perform an iteration of the machine. If the machine is stopped, loads the unique start state.
     * @throws TapeBoundsException If the read/write head falls off the tape.
     * @throws UndefinedTransitionException If there is no transition for the machine to take.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public void step() 
        throws TapeBoundsException, UndefinedTransitionException, ComputationCompletedException,
               ComputationFailedException, NondeterministicException
    {
        m_machine.validate();
        if (m_state == null)
        {
            m_state = m_machine.getStartState();       
        }
        else
        {
            m_state = m_machine.step(m_tape, m_state, getNextTransition());
        }
    }
 
    /** 
     * Runs until the machine halts.
     * @param maxSteps The maximum number of iterations allowed for the computation. A value of zero
     *                 represents no limit. If this number is reached, simulation is aborted.
     */
    public boolean runUntilHalt(int maxSteps) 
        throws TapeBoundsException, UndefinedTransitionException, ComputationCompletedException,
               ComputationFailedException, NondeterministicException
    {
        int currentStep = 0;
        while (!isHalted())
        {
            step();
            currentStep++;
            if (currentStep >= maxSteps && maxSteps != 0)
            {
                break;
            }
        }
        return currentStep < maxSteps && isAccepted();
    }
    
    /** 
     * End the current computation, if any, and reset to initial state.
     */
    public void resetMachine()
    {
        m_state = null;
    }

    /**
     * The machine being simulated.
     */
    protected DFSA_Machine m_machine;

    /**
     * The current state the machine is in.
     */
    protected DFSA_State m_state;
}
