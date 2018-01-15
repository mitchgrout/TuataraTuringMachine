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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import tuataraTMSim.commands.RemoveInconsistentTransitionsCommand;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;
import tuataraTMSim.TMGraphicsPanel;

/**
 * An implementation of a Turing machine. This class stores the specification of the machine, in
 * particular the set of states and transitions, but does not contain any configuration (execution
 * state) information. All simulation interaction should be done via TM_Simulator.  The machine is
 * not guaranteed to be deterministic at all times, but can be assured to be deterministic by
 * calling TM_Machine.validate().
 * We will use the following definition for a Turing machine:
 * M = (Q, d, q0, qf, X), where
 * - Q is a finite set of states
 * - X is a finite alphabet, consisting of input and tape characters.
 * - d is a partial function, with QxX mapping to Qx(Yu{L,R})
 * - q0 is the unique start state
 * - qf is the unique halt state
 * - d(qf, x) is undefined for all x in X
 * @author Jimmy
 */
public class TM_Machine extends Machine<TM_Action, TM_Transition, TM_State, TM_Simulator>
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 2L;

    /**
     * Matches symbols which do not have a transition. Multiple OTHERWISE transitions are not permitted.
     */
    public static final char OTHERWISE_SYMBOL = '?';

    /**
     * Epsilon; Represents a do-nothing action
     **/
    public static final char EMPTY_ACTION_SYMBOL = (char)0x03B5;
    
    /**
     * Creates a new instance of TM_Machine.
     * @param states The set of states.
     * @param transitions The set of transitions.
     * @param alphabet The tape alphabet.
     */
    public TM_Machine(ArrayList<TM_State> states, ArrayList<TM_Transition> transitions, Alphabet alphabet)
    {
        super(alphabet);
        m_states = states;
        m_transitions = transitions;
        m_validated = false;
    }
    
    /**
     * Creates a new instance of TM_Machine, with no states, transitions, and a default alphabet.
     */
    public TM_Machine()
    {
        this(new ArrayList<TM_State>(), new ArrayList<TM_Transition>(), new Alphabet());
    }

    /**
     * Ensures this TM_Machine is valid. This must include the following checks:
     * - There is a unique start state.
     * - There is a unique halt state.
     * - No transitions leave the halt state.
     * - No state has more than one transition for every symbol in the alphabet, including ?.
     * - No state has a transition with input !.
     * - No state has a transition with action !.
     * - No state has a transition with input outside the alphabet.
     * - No state has a transition with action outside the alphabet.
     * @throws NondeterministicException If the machine is considered nondeterministic, i.e. any of
     *                                   the prior conditions are violated.
     */
    public void validate() throws NondeterministicException
    {
        // If we have already validated the machine, and we havent mutated our machine, then we do
        // not need to perform this computation again.
        if (m_validated)
        {
            return;
        }

        // Otherwise, pre-emptively assume it is no longer valid.
        m_validated = false;

        boolean visitedStart = false,
                visitedFinal = false;

        for (TM_State st : m_states)
        {
            // List of transitions for this state
            ArrayList<TM_Transition> transitions = st.getTransitions();

            // Ensure submachines are valid
            if (st.getSubmachine() != null)
            {
                st.getSubmachine().validate();
            }
            
            // Ensure a unique start state
            if (st.isStartState())
            {
                if (!visitedStart)
                {
                    visitedStart = true;
                }
                // Duplicate start state
                else
                {
                    throw new NondeterministicException("Machine has more than one start state.");
                }
            }

            // Ensure a unique final state
            if (st.isFinalState())
            {
                if (!visitedFinal)
                {
                    visitedFinal = true;
                    if (transitions.size() != 0)
                    {
                        throw new NondeterministicException(String.format(
                                    "Machine has a transition leaving the final state leaving %s.",
                                    st.getLabel())); 
                    }
                }
                // Duplicate final state
                else
                {
                    throw new NondeterministicException("Machine has more than one final state.");
                }
            }

            // Ensure no transitions are undefined (TM_Machine.UNDEFINED_SYMBOL), no duplicate transitions,
            // no transitions outside of our alphabet.
            ArrayList<Character> usedSymbols = new ArrayList<Character>();
            for (TM_Transition tr : transitions)
            {
                // Get input/output for this transition
                char inp = tr.getAction().getInputChar();
                char out = tr.getAction().getOutputChar();

                // Undefined input
                if (inp == UNDEFINED_SYMBOL)
                {
                    throw new NondeterministicException(String.format(
                                "Transition %s has an undefined input.", tr.toString()));
                }
                // Undefined output
                if (out == UNDEFINED_SYMBOL)
                {
                    throw new NondeterministicException(String.format(
                                "Transition %s has an undefined action.", tr.toString()));
                }
                // Duplicate input
                if (usedSymbols.contains(inp))
                {
                    throw new NondeterministicException(String.format(
                                "State %s has more than one transition with input %c.", st.getLabel(), inp));
                }
                // Input not in the alphabet
                if (!m_alphabet.containsSymbol(inp) && inp != OTHERWISE_SYMBOL)
                {
                    throw new NondeterministicException(String.format(
                                "Transition %s has an input which is not in the alphabet.", tr.toString()));
                }
                // Output not in the alphabet
                if (!tr.getAction().movesHead() && !m_alphabet.containsSymbol(out) && out != EMPTY_ACTION_SYMBOL)
                {
                    throw new NondeterministicException(String.format(
                                "Transition %s has an action which is not in the alphabet.", tr.toString()));
                }
                // Keep track of this symbol, to check for duplicate inputs.
                usedSymbols.add(inp);
            }
        }

        if (!visitedStart)
        {
            throw new NondeterministicException("Machine has no start state.");
        }

        if (!visitedFinal)
        {
            throw new NondeterministicException("Machine has no final state.");
        }

        // Our machine is valid
        m_validated = true;
    }

    /**
     * Change the state of this TM_Machine to invalid. This means that the next call to validate()
     * will force the entire check to be performed, instead of being predicated on m_validated.
     * This should be called outside this class when one of the underlying states is toggled to be a
     * start state or accepting state.
     */
    public void invalidate()
    {
        m_validated = false;
    }

    /** 
     * Given the current execution state and tape, perform the current action, and update the state.
     * @param tape The current tape.
     * @param currentState The state that this machine is currently in.
     * @param currentNextTransition The next transition to be taken, determined by the value at the
     *                              tape, and the current state.
     * @return The new state that the machine is in after this step.
     * @throws TapeBoundsException If the action causes the read/write head to fall off the tape.
     * @throws UndefinedTransitionException If there is no transition to take.
     * @throws ComputationCompletedException If after this step, we have reached a final state.
     */
    public TM_State step(Tape tape, TM_State currentState, TM_Transition currentNextTransition)
        throws TapeBoundsException, UndefinedTransitionException, ComputationCompletedException
    {
        TM_State tmState = (TM_State)currentState;
        TM_Transition tmTrans = (TM_Transition)currentNextTransition;

        // TODO: Remove currentNextTransition, as we can figure it out deterministically.

        // TODO: handle submachines
        char currentChar = tape.read();
        ArrayList<TM_Transition> currentTransitions = tmState.getTransitions();

        if (tmTrans != null)
        {
            // Sanity check
            if (tmState.getTransitions().contains(tmTrans))
            {
                tmTrans.getAction().performAction(tape);
                return tmTrans.getToState();
            }
        }

        // Halted
        if (tape.isParked() && tmState.isFinalState())
        {
            throw new ComputationCompletedException();
        }

        String message = "";
        if (!tape.isParked() && !tmState.isFinalState())
        {
            message = "The r/w head was not parked, and the last state was not an accepting state.";
        }
        else if (!tape.isParked())
        {
            message = "The r/w head was not parked.";
        }
        else
        {
            message = "The last state was not an accepting state.";
        }      
        throw new UndefinedTransitionException(message);
    }

    /**
     * Return a collection containing all states in this machine.
     * @return A collection of all states in this machine.
     */
    public ArrayList<TM_State> getStates()
    {
        return m_states;
    }

    /**
     * Return a collection containing all transitions in this machine.
     * @return A collection of all transitions in this machine.
     */
    public ArrayList<TM_Transition> getTransitions()
    {
        return m_transitions;
    }

    /** 
     * Get the unique start state. 
     * @return The unique start state of the machine.
     */
    public TM_State getStartState()
    {
        for (TM_State st : m_states) 
        {
            if (st.isStartState())
            {
                return st;
            }
        }
        return null;
    }
    
    /**
     * Get a set containing all final states.
     * @return A non-null collection of all final states in this machine.
     */
    public HashSet<TM_State> getFinalStates()
    {
        HashSet<TM_State> result = new HashSet<TM_State>();
        for (TM_State s : m_states)
        {
            if (s.isFinalState())
            {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Determine whether or not this type of machine should have a unique halt state. If true, then
     * getFinalStates() should never return more than one element.
     * @return true if this machine should have a unique halt state, false otherwise.
     */
    public boolean hasUniqueFinalState()
    {
        return true;
    }

    /**
     * Add a state to the machine.
     * @param state The state to add.
     */
    public void addState(TM_State state)
    {
        // Adding a new state potentially makes our machine invalid.
        invalidate();
         
        m_states.add(state);
    }
    
    /**
     * Delete a state from the machine.
     * @param state The state to delete.
     * @return true if the state is successfully removed, false otherwise.
     */
    public boolean deleteState(TM_State state)
    {
        // Removing an existing state potentially makes our machine invalid.
        invalidate();
        
        if (m_states.remove(state))
        {
            removeTransitionsConnectedTo(state);
            return true;
        }
        return false;
    }
    
    /** 
     * Adds a transition to the machine, and also adds it as an outgoing transition to the 'from'
     * state.
     * @param transition The transition to add.
     */
    public void addTransition(TM_Transition transition)
    {
        // Adding transitions potentially makes our machine invalid.
        invalidate();

        m_transitions.add(transition);
        transition.getFromState().addTransition(transition);
    }
    
    /**
     * Deletes a transition from the machine, and removes it as an outgoing transition from the
     * 'from' state.
     * @param transition The transition to delete.
     * @return true if the transition is successfully removed, false otherwise.
     */
    public boolean deleteTransition(TM_Transition transition)
    {
        // Removing transitions from a valid machine preserves validity; 
        // resetting m_validated is not necessary.
        
        if (m_transitions.remove(transition))
        {
            transition.getFromState().removeTransition(transition);
            return true;
        }
        return false;
    }
    
    /**
     * Removes all transitions associated with a state.
     * @param state The state which removed transitions are connected to, either incoming or outgoing.
     */
    protected void removeTransitionsConnectedTo(TM_State state)
    {
        // Removing transitions from a valid machine preserves validity;
        // invalidation is not necessary.

        for (Iterator<TM_Transition> i = m_transitions.iterator(); i.hasNext(); )
        {
            TM_Transition current = i.next();
            if (current.getFromState() == state || current.getToState() == state)
            {
                current.getFromState().removeTransition(current);
                i.remove();
            }
        }
    }
   
    /**
     * Determine whether a given action is consistent with an alphabet.
     * @param act The action to test the consistency of.
     * @param alph The alphabet to test against.
     * @return true if the action is considered consistent with the alphabet, i.e. does not contain
     *         input or output which is not present in the alphabet and is not a special character,
     *         false otherwise.
     */
    protected boolean isConsistentWithAlphabet(TM_Action act, Alphabet alph)
    {
        char inp = act.getInputChar();
        char out = act.getOutputChar();
        
        // Determine if input is inconsistent
        if (!alph.containsSymbol(inp) &&
            inp != TM_Machine.UNDEFINED_SYMBOL &&
            inp != TM_Machine.OTHERWISE_SYMBOL)
        {
            return false;
        }
        
        // Determine if action is inconsistent
        if (act.movesHead())
        {
            return true;
        }
        else if (!alph.containsSymbol(out) &&
                 out != TM_Machine.UNDEFINED_SYMBOL &&
                 out != TM_Machine.EMPTY_ACTION_SYMBOL)
        {
            return false;
        }

        // Otherwise consistent
        return true;
    }

    /**
     * The set of states in the machine.
     */
    protected ArrayList<TM_State> m_states;
    
    /**
     * The set of transitions in the machine.
     */
    protected ArrayList<TM_Transition> m_transitions;

    /**
     * Determine if this machine has been deemed valid or not.  Will be set to true after successful
     * calls to validate(), and set to false after mutations have occured to the machine.
     */
    protected transient boolean m_validated = false;
}
