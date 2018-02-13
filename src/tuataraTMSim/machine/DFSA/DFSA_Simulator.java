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

import java.util.ArrayList;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.Global;
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
     */
    public boolean isHalted()
    {
        // Halt condition for a DFSA is no more input
        return (m_tape.read() == Tape.BLANK_SYMBOL);
    } 
    
    /**
     * Determine if the machine is in a final state.
     * @return true if the machine is in a final state, false otherwise.
     */
    public boolean isAccepted()
    {
        // Accepted input means no more input, and last state is final
        return isHalted() && m_state != null && m_state.isFinalState();
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
     * @throws ComputationCompletedException If execution halts successfully.
     * @throws ComputationFailedException If execution halts unexpectedly.
     */
    public void step() 
        throws ComputationCompletedException, ComputationFailedException
    {
        // Machine has just started
        if (m_state == null)
        {
            ArrayList<DFSA_State> startStates = m_machine.getStartStates();
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
                m_state = Global.promptSelection(startStates, "Please choose which start state to use", DFSA_State::getLabel);
                // User cancelled
                if (m_state == null)
                {
                    throw new ComputationFailedException("No start state chosen");
                }
            }
        }
        // Already running
        else
        {
            // Since our DFSA could contain a lambda edge, we need to do a linear pass to check if
            // any lambda edges are in next. If so, user *must* be prompted to select which edge.
            ArrayList<DFSA_Transition> next = getNextTransitions();
            boolean hasLambda = false;
            for (DFSA_Transition t : next)
            {
                if (t.getAction().getInputChar() == Machine.EMPTY_INPUT_SYMBOL)
                {
                    hasLambda = true;
                    break;
                }
            }
            if (hasLambda)
            {
                // t could be null if the user cancels, if so this simply halts execution
                DFSA_Transition t = Global.promptSelection(next, "Please select which transition to use, if any", DFSA_Transition::toString);
                m_state = m_machine.step(m_tape, m_state, t);
            }
            else if (next.size() == 0)
            {
                m_state = m_machine.step(m_tape, m_state, null);
            }
            else if (next.size() == 1)
            {
                m_state = m_machine.step(m_tape, m_state, next.get(0));
            }
            else
            {
                DFSA_Transition t = Global.promptSelection(next, "Please select which transition to use", DFSA_Transition::toString);
                if (t == null)
                {
                    throw new ComputationFailedException("No transition chosen");
                }
                m_state = m_machine.step(m_tape, m_state, t);
            }
        }
    }
 
    /** 
     * Runs until the machine halts.
     * @param maxSteps The maximum number of iterations allowed for the computation. A value of zero
     *                 represents no limit. If this number is reached, simulation is aborted.
     */
    public boolean runUntilHalt(int maxSteps) 
        throws ComputationCompletedException, ComputationFailedException 
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
