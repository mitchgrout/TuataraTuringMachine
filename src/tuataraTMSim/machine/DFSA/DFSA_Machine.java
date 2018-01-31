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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.machine.*;

/**
 * An implementation of a DFSA. This class stores the specification of the machine, in particular
 * the set of states and transitions, but does not contain any configuration information. All
 * simulation interaction should be done vial DFSA_Simulator. The machine is not guaranteed to be
 * deterministic at all times, but can be assured to be deterministic by calling
 * DFSA_Machine.isDeterministic().
 * We will use the following definition for a DFSA:
 * M = (Q, d, q0, F, X), where
 * - Q is a finite set of states
 * - X is a finite alphabet, consisting of input characters
 * - d is a total function, with QxX mapping to Q
 * - q0 is the unique start state
 * - F is a subset of Q, denoting the accepting states
 */
public class DFSA_Machine extends Machine<DFSA_Action, DFSA_Transition, DFSA_State, DFSA_Simulator>
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of DFSA_Machine.
     * @param states The set of states.
     * @param transitions The set of transitions.
     * @param alphabet The tape alphabet.
     */
    public DFSA_Machine(ArrayList<DFSA_State> states, ArrayList<DFSA_Transition> transitions, Alphabet alphabet)
    {
        super(alphabet);
        m_states = states;
        m_transitions = transitions;
    }

    /**
     *
     */
    public DFSA_Machine()
    {
        this(new ArrayList<DFSA_State>(), new ArrayList<DFSA_Transition>(), new Alphabet());
    }

    /**
     * Determine whether this machine is valid, in terms of its formal definition.
     * @return null if there are no nondeterministic features in the machine, otherwise a
     *         description of the object which is undefined in the machine.
     */
    public String isDeterministic()
    {
        int startCount = 0;
        for (DFSA_State state : m_states)
        {
            // Ensure a unique start state; note we use int to reduce LOC
            if (state.isStartState() && startCount++ > 0)
            {
                return "Machine has more than one start state";
            }

            // Check to see if there are any transitions with the same input
            HashSet<Character> matched = new HashSet<Character>();
            for (DFSA_Transition trans : state.getTransitions())
            {
                // Get the input char to match
                char inp = trans.getAction().getInputChar();

                // Undefined input
                if (inp == UNDEFINED_SYMBOL)
                {
                    return String.format("Transition %s has undefined input", trans.toString());
                }
                // Lambda edge
                if (inp == EMPTY_INPUT_SYMBOL)
                {
                    return String.format("Transition %s uses a lambda edge", trans.toString());
                }
                // Duplicate
                if (matched.contains(inp))
                {
                    return String.format("State %s has more than one transition with input %c", 
                                         state.getLabel(), inp);
                }
                // Input not in alphabet
                if (!m_alphabet.containsSymbol(inp))
                {
                    return String.format("Transition %s has an input which is not in the alphabet",
                                         trans.toString());
                }
                // Keep track of this symbol
                matched.add(inp);
            }

            // Ensure we matched the entire alphabet
            for (char c : m_alphabet.getSymbols())
            {
                if (!matched.contains(c))
                {
                    return String.format("State %s does not have a transition for input %c", 
                                         state.getLabel(), c);
                }
            }
        }
        // Did we find a single start state
        if (startCount == 0)
        {
            return "Machine has no start state";
        }

        return null;
    }

     /**
      * Given a current state and tape, determine the next state the machine should move to, and
      * perform any relevant actions.
      * @param tape The current tape.
      * @param currentState The state that this machine is currently in.  
      * @param currentNextTransition The next transition to be taken, determined by the value at the
      *                              tape, and the current state.  This is null if the transition is 
      *                              not defined.  
      * @return The new state that the machine is in after this step.
      * @throws ComputationCompletedException If, after this step, the machine halts.  
      * @throws ComputationFailedException If the machine halts unexpectedly.
      */
    public DFSA_State step(Tape tape, DFSA_State currentState, DFSA_Transition currentNextTransition)
        throws ComputationCompletedException, ComputationFailedException
    {
        char currentChar = tape.read();
        ArrayList<DFSA_Transition> currentTransitions = currentState.getTransitions();
      
        // Lambda edges can be run even if nothing on the tape
        if (currentNextTransition != null &&
            currentNextTransition.getAction().getInputChar() == EMPTY_INPUT_SYMBOL)
        {
            // No need to run performAction, as it will do nothing
            return currentNextTransition.getToState();
        }
        // Halted and accepted
        else if (currentChar == Tape.BLANK_SYMBOL && currentState.isFinalState())
        {
            // For convenience, reset after finishing
            tape.resetRWHead();
            throw new ComputationCompletedException("The input string was accepted");
        }
        // Halted but not accepted
        else if (currentChar == Tape.BLANK_SYMBOL)
        {
            // For convenience, reset after finishing
            tape.resetRWHead();
            throw new ComputationCompletedException("The input string was not accepted");
        }
        // No transition
        else  if (currentNextTransition == null)
        {
            throw new ComputationFailedException("Undefined transition");
        }
        // A regular transition
        else
        {
            // Move the head rightwards
            currentNextTransition.getAction().performAction(tape);
            return currentNextTransition.getToState();
        }
    }

    /**
     * Get a collection containing all states in this machine.
     * @return A collection of all states in this machine.
     */
    public Collection<DFSA_State> getStates()
    {
        return m_states;
    }

    /**
     * Get a collection containing all transitions in this machine.
     * @return A collection of all transitions in this machine.
     */
    public Collection<DFSA_Transition> getTransitions()
    {
        return m_transitions;
    }

    /**
     * Determine whether or not this type of machine should have a unique halt state. If true, then
     * getFinalStates() should never return more than one element.
     * @return true if this machine should have a unique halt state, false otherwise.
     */
    public boolean hasUniqueFinalState()
    {
        return false;
    }

    /**
     * Add a state to the machine.
     * @param state The state to add.
     */
    public void addState(DFSA_State state)
    {
        m_states.add(state);
    }

    /**
     * Delete a state from the machine.
     * @param state The state to delete.
     * @return true if the state is successfully removed, false otherwise.
     */
    public boolean deleteState(DFSA_State state)
    {
        if (m_states.remove(state))
        {
            removeTransitionsConnectedTo(state);
            return true;
        }
        return false;
    }

    /** 
     * Adds a transition to the machine. Should also add the transition as outgoing from the from state.
     * @param transition The transition to add.
     */
    public void addTransition(DFSA_Transition transition)
    {
        m_transitions.add(transition);
        transition.getFromState().addTransition(transition);
    }

    /**
     * Deletes a transition from the machine, and removes it as an outgoing transition from the from state.
     * @param transition The transition to delete.
     * @return true if the transition is successfully removed, false otherwise.
     */
    public boolean deleteTransition(DFSA_Transition transition)
    {
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
    protected void removeTransitionsConnectedTo(DFSA_State state)
    {
        for (Iterator<DFSA_Transition> i = m_transitions.iterator(); i.hasNext() ;)
        {
            DFSA_Transition current = i.next();
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
     * @return true if the action is considered consistent with the alphabet.
     */
    protected boolean isConsistentWithAlphabet(DFSA_Action act, Alphabet alph)
    {
        char inp = act.getInputChar();
        return alph.containsSymbol(inp)           || 
               inp == Machine.UNDEFINED_SYMBOL    || 
               inp == Machine.EMPTY_INPUT_SYMBOL;
    }

    /**
     * The set of states in the machine.
     */
    protected ArrayList<DFSA_State> m_states;

    /**
     * The set of transitions in the machine.
     */
    protected ArrayList<DFSA_Transition> m_transitions;
}
