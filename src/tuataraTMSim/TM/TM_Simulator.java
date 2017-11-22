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

package tuataraTMSim.TM;

import java.util.*;
import tuataraTMSim.exceptions.*;

/**
 * Encapsulates the whole system for a Turing machine, including the machine and its configuration.
 * In particular, this class handles the simulation of the machine, including validation of the
 * machine prior to execution.
 * @author Jimmy
 */
public class TM_Simulator
{  
    /**
     * Creates a new instance of TM_Simulator.
     * @param machine The machine to simulate.
     * @param tape The tape which the machine will read from.
     */
    public TM_Simulator(TMachine machine, Tape tape)
    {
        m_machine = machine;
        m_tape = tape;
        computeNextTransition();
    }
    
    /**
     * Perform an iteration of the machine.
     * @throws TapeBoundsException If the read/write head falls off the tape.
     * @throws UndefinedTransitionException If there is no transition for the machine to take.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public void step() throws TapeBoundsException, UndefinedTransitionException,
                              ComputationCompletedException, NondeterministicException
    {
        // Validation is cached, so long as no invalidating mutations are made, so this is not an
        // expensive call in general.
        m_machine.validate();

        if (m_state == null)
        {
            // Guaranteed to exist by m_machine.validate()
            m_state = m_machine.getStartState();
        }
        else
        {
            m_state = m_machine.step(m_tape, m_state, m_currentNextTransition);
        }
        computeNextTransition();
    }
    
    /** 
     * End the current computation, if any, setting the machine to its initial state.
     */
    public void resetMachine()
    {
        m_state = null;
        computeNextTransition();
    }
    
    /** 
     * Runs until the machine halts.
     * @param maxSteps The maximum number of iterations allowed for the computation. A value of zero
     *                 represents no limit. If this number is reached, simulation is aborted.
     * @param doConsoleOutput Determines if configuration information should be written to stdout.
     * @return true if the machine halts in a finite amount of steps up until maxSteps, false otherwise.
     * @throws TapeBoundsException If the read/write head falls off the tape.
     * @throws UndefinedTransitionException If there is no transition for the machine to take.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean runUntilHalt(int maxSteps, boolean doConsoleOutput)
        throws TapeBoundsException, UndefinedTransitionException,
               ComputationCompletedException, NondeterministicException 
    {
        int currentStep = 0;
        
        while (!isHalted())
        {
            if (doConsoleOutput)
            {
                System.out.println("step " + currentStep + ", " + getConfiguration());
            }
            
            step();
            currentStep++;
            if (currentStep >= maxSteps && !(maxSteps == 0))
            {
                break;
            }
        }
        
        if (doConsoleOutput)
        {
            System.out.println("step " + currentStep + ", " + getConfiguration());
        }

        if (currentStep >= maxSteps)
        {
            if (doConsoleOutput)
            {
                System.out.println("went too long - exiting");
            }
            return false;
        }
            
        if (!isHaltedWithHeadParked())
        {
            if (doConsoleOutput)
            {
                System.out.println("The system did not halt with the head parked");
            }
            return false;
        }
        if (doConsoleOutput)
        {
            System.out.println("The system successfully halted with the head parked");
        }
        return true;
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
        return m_state.isFinalState();
    }
    
    /**
     * Determine if the machine is in an accepting state, and the read/write head of the tape is in
     * the first cell of the tape.
     * @return true if the machine is in an accepting state, with the read/write head parked, false
     *         otherwise.
     * @throws NondeterministicException If the machine is deemed nondeterministic.
     */
    public boolean isHaltedWithHeadParked() throws NondeterministicException
    {
        // Guarantee that there is a unique halting state, hence guarantee the result makes sense.
        m_machine.validate();
        return (isHalted() && m_tape.isParked());
    }
    
    /**
     * Gets a string representation of the current configuration ('state') of the machine.
     * @return A string representation of the current configuration.
     */
    public String getConfiguration()
    {
        return "\""+ m_tape.toString() + "\", " + m_state.getLabel();
    }
    
    /**
     * Get the tape.
     * @return The current tape.
     */
    public Tape getTape()
    {
        return m_tape;
    }
    
    /**
     * Gets the current state that the machine is in.
     * @return The current state.
     */
    public TM_State getCurrentState()
    {
        return m_state;
    }
    
    /**
     * Sets the current state that the machine is in, and calls computeNextTransition() to set up
     * the selected next transition.
     * @param state The new current state.
     */
    public void setCurrentState(TM_State state)
    {
        m_state = state;
        computeNextTransition();
    }
    
    /**
     * Gets the Turing machine that is being simulated.
     * @return The machine being simulated.
     */
    public TMachine getMachine()
    {
        return m_machine;
    }
    
    /**
     * Get the next possible transition that the machine can take in the next execution step, and
     * set this to be m_currentNextTransition. This will either be a valid transition, or null.
     * Assumes that m_machine.validate() has been called.
     */
    public void computeNextTransition()
    {
        // Since the machine is assumed valid, there are three possibilities:
        // - No transition
        // - An exact match
        // - A default route via OTHERWISE_SYMBOL

        TM_Transition nextTransition = null;
        
        // No state, no transitions
        if (getCurrentState() == null)
        {
            m_currentNextTransition = null;
            return;
        }

        ArrayList<TM_Transition> out = getCurrentState().getTransitions();
        char currentInputSymbol = m_tape.read();
       
        for (TM_Transition t : out)
        {
            char inp = t.getAction().getInputChar();

            // An exact, guaranteed unique match
            if (inp == currentInputSymbol)
            {
                m_currentNextTransition = t;
                return;
            }
            // A non-exact match; we will keep track of this
            else if (inp == TMachine.OTHERWISE_SYMBOL)
            {
                nextTransition = t;
            }
        }

        // If we are here, there is no exact match, either a default route, or no transition.
        // These are both represented by the current value of nextTransition
        // null -- no transition, non-null -- default route
        m_currentNextTransition = nextTransition;
    }
    
    /**
     * Get the currently selected transition for the machine to execute in the next execution step.
     * @return The next transition to be taken.
     */
    public TM_Transition getCurrentNextTransition()
    {
        return m_currentNextTransition;
    }
   
    /**
     * The machine being simulated.
     */
    private TMachine m_machine;
    
    /**
     * The current tape.
     */
    private Tape m_tape;
    
    /**
     * The current state the machine is in.
     */
    private TM_State m_state;

    /**
     * The next transition to be taken.
     */
    private TM_Transition m_currentNextTransition;
}
