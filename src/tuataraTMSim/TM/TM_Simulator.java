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
     * Creates a new instance of TM_Simulator
     */
    public TM_Simulator(TMachine turingMachine, Tape tape)
    {
        m_machine = turingMachine;
        m_tape = tape;
        m_random = new Random();
        computeNextTransition();
    }
    
    /**
     * Perform an iteration of the machine.
     */
    public void step() throws TapeBoundsException, UndefinedTransitionException,
                              ComputationCompletedException, NondeterministicException
    {
        // TODO: Prevent expensive calls to .validate()
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
     * End the current computation (if any), setting the machine to its initial state.
     */
    public void resetMachine()
    {
        m_state = null;
        computeNextTransition();
    }
    
    /** 
     * Runs until the machine halts. Returns true only if the machine terminates successfully with
     * the head parked within the specified number of steps.
     * @param maxSteps  The maximum number of machine iterations allowed for the computation (0 for no limit).  
     *                  If this number is reached, the computation will be aborted and will return false.
     * @param doConsoleOutput  Determines if we output the configurations and computation result to standard out console.
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
     * Returns true IFF the machine is in an accepting state.
     */
    public boolean isHalted() throws NondeterministicException
    {
        // TODO: Do we need to validate here?
        m_machine.validate();
        return m_state.isFinalState();
    }
    
    /**
     * Returns true IFF the machine is in an accepting state, with its read/write head parked at the
     * beginning of the tape.
     */
    public boolean isHaltedWithHeadParked() throws NondeterministicException
    {
        // TODO: Do we need to validate here?
        m_machine.validate();
        return (isHalted() && m_tape.isParked());
    }
    
    /**
     * Gets a string representing current configuration ('state') of the machine.
     */
    public String getConfiguration()
    {
        return "\""+ m_tape.toString() + "\", " + m_state.getLabel();
    }
    
    // Getter functions:    
    /**
     * Gets the tape.
     */
    public Tape getTape()
    {
        return m_tape;
    }
    
    /**
     * Gets the current state that the machine is in.
     */
    public TM_State getCurrentState()
    {
        return m_state;
    }
    
    /**
     * Sets the current state that the machine is in, and calls computeNextTransition() to set up
     * the selected next transition.
     */
    public void setCurrentState(TM_State state)
    {
        m_state = state;
        computeNextTransition();
    }
    
    /**
     * Gets the Turing machine that is being simulated.
     */
    public TMachine getMachine()
    {
        return m_machine;
    }
    
    /**
     * Get the next possible transition that the machine can take in the next execution step. This
     * will either be a valid transition, or null. Assumes that m_machine.validate() has been called.
     */
    public void computeNextTransition()
    {
        // Since the machine is assumed valid, there are three possibilities:
        // - No transition
        // - An exact match
        // - A default route via OTHERWISE_SYMBOL

        TM_Transition nextTransition = null;
        
        // No state, no transitions
        if(getCurrentState() == null)
        {
            m_currentNextTransition = null;
            return;
        }

        ArrayList<TM_Transition> out = getCurrentState().getTransitions();
        char currentInputSymbol = m_tape.read();
       
        for (TM_Transition t : out)
        {
            // An exact, guaranteed unique match
            if (t.getSymbol() == currentInputSymbol)
            {
                m_currentNextTransition = t;
                return;
            }
            // A non-exact match; we will keep track of this
            else if (t.getSymbol() == TMachine.OTHERWISE_SYMBOL)
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
     */
    public TM_Transition getCurrentNextTransition()
    {
        return m_currentNextTransition;
    }
    
    private TMachine m_machine;
    private Tape m_tape;
    private TM_State m_state;
    private TM_Transition m_currentNextTransition;
    private Random m_random;
}
