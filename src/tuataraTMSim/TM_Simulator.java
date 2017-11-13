/*
 * TM_Simulator.java
 *
 * Created on November 7, 2006, 3:20 PM
 *
 */

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
import java.util.*;

/** Encapsulates the whole system for a Turing machine, including the machine and its configuration.
 *  In particular, this class handles the simulation of the machine.
 * @author Jimmy
 */
public class TM_Simulator {
    
    /** Creates a new instance of TM_Simulator */
    public TM_Simulator(TMachine turingMachine, Tape tape)
    {
        m_machine =  turingMachine;
        m_tape = tape;
        m_random = new Random();
        computePotentialTransitions(true);
    }
    
    /** Perform an iteration of the machine.
     */
    public void step() throws TapeBoundsException, UndefinedTransitionException, NoStartStateException, ComputationCompletedException
    {
        if (m_state == null)
        {
            m_state = m_machine.getStartState();
            if (m_state == null) //still!
            {
                throw new NoStartStateException();
            }
        }
        else
            m_state = m_machine.step(m_tape, m_state, m_currentNextTransition);
        computePotentialTransitions(true);
    }
    
    /** End the current computation (if any), setting the machine to its initial state.
     */
    public void resetMachine()
    {
        m_state = null;
        computePotentialTransitions(true);
    }
    
    
    /** Runs until the machine halts.  Returns true only if the machine terminates successfully with the
     *  head parked within the specified number of steps.
     *  @param maxSteps  The maximum number of machine iterations allowed for the computation (0 for no limit).  
     *                   If this number is reached, the computation will be aborted and will return false.
     *  @param doConsoleOutput  Determines if we output the configurations and computation result to standard out console.
     */
    public boolean runUntilHalt(int maxSteps, boolean doConsoleOutput) throws TapeBoundsException, UndefinedTransitionException, NoStartStateException, ComputationCompletedException
    {
        int currentStep = 0;
        
        while (!isHalted())
        {
            if (doConsoleOutput)
                System.out.println("step " + currentStep + ", " + getConfiguration());
            
            step();
            
            currentStep++;
            if (currentStep >= maxSteps && !(maxSteps == 0))
            {
                break;
            }
        }
        
        if (doConsoleOutput)
            System.out.println("step " + currentStep + ", " + getConfiguration());
        
        if (currentStep >= maxSteps)
        {
            if (doConsoleOutput)
                    System.out.println("went too long - exiting");
            return false;
        }
            
        if (!isHaltedWithHeadParked())
        {
            if (doConsoleOutput)
                System.out.println("The system did not halt with the head parked");
            return false;
        }
        if (doConsoleOutput)
            System.out.println("The system successfully halted with the head parked");
        return true;
    }
    
    /** Returns true IFF the machine is in an accepting state.
     */
    public boolean isHalted() throws NoStartStateException
    {
        if (m_state == null)
            throw new NoStartStateException();
        return m_state.isFinalState();
    }
    
    /** Returns true IFF the machine is in an accepting state, with its read/write head
     *  parked at the beginning of the tape.
     */
    public boolean isHaltedWithHeadParked() throws NoStartStateException
    {
        if (m_state == null)
            throw new NoStartStateException();
        return (isHalted() && m_tape.isParked());
    }
    
    /** Gets a string representing current configuration ('state') of the machine.
     */
    public String getConfiguration()
    {
        return "\""+ m_tape.toString() + "\", " + m_state.getLabel();
    }
    
    //getters 
    
    /** Gets the tape.
     */
    public Tape getTape()
    {
        return m_tape;
    }
    
    /** Gets the current state that the machine is in.
     */
    public TM_State getCurrentState()
    {
        return m_state;
    }
    
    /** Sets the current state that the machine is in, and
     *  calls computePotentialTransitions(true) to set up
     *  the selected next transition.
     */
    public void setCurrentState(TM_State state)
    {
        m_state = state;
        computePotentialTransitions(true);
    }
    
    /** Gets the Turing machine that is being simulated.
     */
    public TMachine getMachine()
    {
        return m_machine;
    }
    
    /** Computes the list of possible transitions that the machine could take in
     *  the next execution step, and randomly selects one of them as a default
     *  transition.
     */
    public void computePotentialTransitions(boolean randomizeChosenTransition)
    {
        m_potentialTransitions = new ArrayList<TM_Transition>();
        ArrayList<TM_Transition> otherwiseTransitions = new ArrayList<TM_Transition>();
        
        if (getCurrentState() == null)
        {
            m_currentNextTransition = null;
            return;
        }
        
        ArrayList<TM_Transition> out = getCurrentState().getTransitions();
        char currentInputSymbol = m_tape.read();
       
        for (TM_Transition t : out)
        {
            if (t.getSymbol() == currentInputSymbol ||
                    t.getSymbol() == TMachine.WILDCARD_INPUT_SYMBOL) //universal transitions
                m_potentialTransitions.add(t);
            else if (t.getSymbol() == TMachine.OTHERWISE_SYMBOL)
                otherwiseTransitions.add(t);
        }
        if (randomizeChosenTransition || m_currentNextTransition == null
                || !m_potentialTransitions.contains(m_currentNextTransition))
        {
            if (m_potentialTransitions.size() > 0)
            {
                int index = m_random.nextInt(m_potentialTransitions.size());
                m_currentNextTransition = m_potentialTransitions.get(index);
            }
            else if (otherwiseTransitions.size() > 0) //pick an 'otherwise' transition
            {
                m_potentialTransitions = otherwiseTransitions;
                int index = m_random.nextInt(m_potentialTransitions.size());
                m_currentNextTransition = m_potentialTransitions.get(index);
            }
            else
                m_currentNextTransition = null;
        }
    }
    
    /** Get the list of transitions that the machine could follow in the next
     *  execution step, given the current configuration of the machine/tape.
     *  If the machine is deterministic, there can be at most one element in
     *  the array.
     *  If no transitions are possible, an empty array is returned.
     */
    public ArrayList<TM_Transition> getPotentialTransitions()
    {
        return m_potentialTransitions;
    }
    
    /**  Get the currently selected transition for the machine to execute in
     *   the next execution step.
     */
    public TM_Transition getCurrentNextTransition()
    {
        return m_currentNextTransition;
    }
    
    /**  Set the currently selected transition for the machine to execute in
     *   the next execution step if the given transition is a potential
     *   next transition, otherwise has no effect.
     */
    public void setCurrentNextTransition(TM_Transition t)
    {
        if (m_state != null)
        {
            if (getPotentialTransitions().contains(t))
                m_currentNextTransition = t;
        }
    }
    
    
    private TMachine m_machine;
    private Tape m_tape;
    private TM_State m_state;
    private ArrayList<TM_Transition> m_potentialTransitions;
    private TM_Transition m_currentNextTransition;
    private Random m_random;
}
