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

package tuataraTMSim.machine;

import java.util.ArrayList;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.Machine;

/**
 * An abstraction of an object which simulates the execution of a given machine.
 */
public abstract class Simulator<
    PREACTION extends PreAction,
    TRANSITION extends Transition<PREACTION, STATE, MACHINE, ?>,
    STATE extends State<PREACTION, TRANSITION, MACHINE, ?>,
    MACHINE extends Machine<PREACTION, TRANSITION, STATE, ?>>
{  
    /**
     * Creates an instance of Simulator.
     * @param tape The underlying tape.
     */
    public Simulator(Tape tape)
    {
        m_tape = tape;
    }

    /**
     * Get the tape being used in this simulation.
     * @return The current tape.
     */
    public Tape getTape()
    {
        return m_tape;
    }

    /**
     * Set the tape being used in this simulation.
     * @param t The new tape.
     */
    public void setTape(Tape t)
    {
        m_tape = t;
    }

    /**
     * Get the set of possible transitions that the machine can take in the next execution step.
     * @return The set of possible transitions that the machine can take. This is guaranteed to be
     *         non-null.
     */
    public ArrayList<TRANSITION> getNextTransitions()
    {
        // Grab our current state
        STATE s = getCurrentState();
        
        // No state => no transition
        if (s == null)
        {
            return new ArrayList<TRANSITION>();
        }

        // Set of transitions which we *could* take
        ArrayList<TRANSITION> otherwise = new ArrayList<TRANSITION>(),
                              matched   = new ArrayList<TRANSITION>();
        char inputSym = m_tape.read();
        for (TRANSITION t : s.getTransitions())
        {
            char transSym = t.getAction().getInputChar();
            
            // Exact match
            if (transSym == inputSym)
            {
                matched.add(t);
            }
            // Non-exact match
            else if (transSym == Machine.OTHERWISE_SYMBOL)
            {
                otherwise.add(t);
            }
            // Lambda edge
            else if (transSym == Machine.EMPTY_INPUT_SYMBOL)
            {
                matched.add(t);
            }
        }

        // Return the exact matches if any, otherwise the default matches, otherwise nothing.
        return matched.size()   != 0? matched   :
               otherwise.size() != 0? otherwise :
                                      matched; 
        // 'matched' and 'otherwise' are synonymous with an empty ArrayList
    }

    /**
     * Gets the machine that is being simulated.
     * @return The machine being simulated.
     */
    public abstract MACHINE getMachine();
   
    /**
     * Gets the current state that the machine is in.
     * @return The current state.
     */
    public abstract STATE getCurrentState();
    
    /**
     * Sets the current state that the machine is in.
     * @param state The new current state.
     */
    public abstract void setCurrentState(STATE state);

    /** 
     * Determine if the machine has finished executing.
     * @return true if the machine is has finished executing, false otherwise.
     */
    public abstract boolean isHalted();
    
    /**
     * Determine if the machine is in an accepting state.
     * @return true if the machine is in an accepting state, false otherwise.
     */
    public abstract boolean isAccepted();
 
    /**
     * Gets a string representation of the current configuration of the machine.
     * @return A string representation of the current configuration.
     */
    public abstract String getConfiguration();
 
    /**
     * Perform an iteration of the machine. If the machine is stopped, loads the unique start state.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws ComputationFailedException If execution halts, but the input is not accepted.
     */
    public abstract void step() 
        throws ComputationCompletedException, ComputationFailedException;
 
    /** 
     * Runs until the machine halts.
     * @param maxSteps The maximum number of iterations allowed for the computation. A value of zero
     *                 represents no limit. If this number is reached, simulation is aborted.
     * @return true if the machine halts in a finite amount of steps up until maxSteps, false otherwise.
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws ComputationFailedException If execution halts, but the input is not accepted.
     */
    public abstract boolean runUntilHalt(int maxSteps) 
        throws ComputationCompletedException, ComputationFailedException;
    
    /** 
     * End the current computation, if any, and reset to initial state.
     */
    public abstract void resetMachine();
   
    /**
     * The current tape.
     */
    protected Tape m_tape;
}
