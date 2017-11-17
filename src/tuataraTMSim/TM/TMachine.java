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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import tuataraTMSim.commands.RemoveInconsistentTransitionsCommand;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.TMGraphicsPanel;

/**
 * An implementation of a Turing machine. This class stores the specification of the machine, in
 * particular the set of states and transitions, but does not contain any configuration (execution
 * state) information.  The machine is not guaranteed to be deterministic at all times, but can be
 * assured to be deterministic by calling TMachine.validate()
 * @author Jimmy
 */
public class TMachine implements Serializable
{
    /**
     * Does not match any symbols; instead, this is used to indicate that a transition has not yet
     * been assigned a value.
     */
    public static final char UNDEFINED_SYMBOL = '!';

    /**
     * Matches symbols which do not have a transition. Multiple OTHERWISE transitions are not permitted.
     */
    public static final char OTHERWISE_SYMBOL = '?';

    /**
     * Epsilon; Represents a do-nothing action
     **/
    public static final char EMPTY_ACTION_SYMBOL = (char)0x03B5;
    
    /**
     * Creates a new instance of TMachine
     * @param states The set of states.
     * @param transitions The set of transitions.
     * @param alphabet The tape alphabet.
     */
    public TMachine(ArrayList<TM_State> states, ArrayList<TM_Transition> transitions, Alphabet alphabet)
    {
        m_states = states;
        m_transitions = transitions;
        m_alphabet = alphabet;
    }
    
    /**
     * Creates a new instance of TMachine, with no states, transitions, and a default alphabet.
     */
    public TMachine()
    {
        m_states = new ArrayList<TM_State>();
        m_transitions = new ArrayList<TM_Transition>();
        m_alphabet = new Alphabet();
    }

    /**
     * Ensures this TMachine is valid. This means there is a unique start and final state, there are
     * no duplicate transitions, there are no undefined transitions, and the final state has no
     * transitions leaving it. Should be called before a machine is run.
     * @throws NondeterministicException If the machine is considered nondeterministic, i.e. any of
     *                                   the prior conditions are violated.
     */
    public void validate() throws NondeterministicException
    {
        // TODO: See if we can cache this result, so long as the machine has not been modified, to
        //       reduce overhead from repeat calls. As the doc states, this should be called before
        //       running the machine, which in the simulator corresponds to running before every step.
        boolean visitedStart = false,
                visitedFinal = false;

        for(TM_State st : m_states)
        {
            // List of transitions for this state
            ArrayList<TM_Transition> transitions = st.getTransitions();

            // Ensure a unique start state
            if(st.isStartState())
            {
                if(!visitedStart)
                {
                    visitedStart = true;
                }
                // Duplicate start state
                else
                {
                    throw new NondeterministicException("Machine has more than one start state");
                }
            }

            // Ensure a unique final state
            if(st.isFinalState())
            {
                if(!visitedFinal)
                {
                    visitedFinal = true;
                    if(transitions.size() != 0)
                    {
                        throw new NondeterministicException("Machine has a transition leaving the final state " + st.getLabel());
                    }
                }
                // Duplicate final state
                else
                {
                    throw new NondeterministicException("Machine has more than one final state");
                }
            }

            // Ensure no transitions are undefined (TMachine.UNDEFINED_SYMBOL), and no duplicate transitions
            ArrayList<Character> usedSymbols = new ArrayList<Character>();
            for(TM_Transition tr : transitions)
            {
                char c = tr.getSymbol();
                if(c == UNDEFINED_SYMBOL)
                {
                    throw new NondeterministicException("State " + st.getLabel() + " has an undefined transition");
                }
                if(usedSymbols.contains(c))
                {
                    throw new NondeterministicException("State " + st.getLabel() + " has a duplicate transition for the input " + c);
                }
                usedSymbols.add(c);
            }
        }

        if(!visitedStart)
        {
            throw new NondeterministicException("Machine has no start state");
        }

        if(!visitedFinal)
        {
            throw new NondeterministicException("Machine has no final state");
        }
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
        // TODO: Remove currentNextTransition, as we can figure it out deterministically.

        // TODO: handle submachines
        char currentChar = tape.read();
        ArrayList<TM_Transition> currentTransitions = currentState.getTransitions();

        if (currentNextTransition != null)
        {
            // TODO: Justify this conditional
            if (currentState.getTransitions().contains(currentNextTransition))
            {
                currentNextTransition.getAction().performAction(tape);
                return currentNextTransition.getToState();
            }
        }

        // Halted
        if (tape.isParked() && currentState.isFinalState())
        {
            throw new ComputationCompletedException();
        }

        String message = "";
        if (!tape.isParked() && !currentState.isFinalState())
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
     * Get the start state. Assumes that validate() has been called.
     * @return The unique start state of the machine.
     */
    public TM_State getStartState()
    {
        for (TM_State st : m_states) 
        {
            if(st.isStartState())
            {
                return st;
            }
        }

        // Can never reach this code due to validate() ensuring a unique start state
        return null;
    }
    
    /**
     * Get the final state. Assumes that validate() has been called.
     * @return The unique final state of the machine.
     */
    public TM_State getFinalState()
    {
        for (TM_State st : m_states)
        {
            if(st.isFinalState())
            {
                return st;
            }
        }

        // Can never reach this code due to validate() ensuring a unique final state
        return null;
    }
    
    // Methods to update/modify the machine:
    /**
     * Add a state to the machine.
     * @param state The state to add.
     */
    public void addState(TM_State state)
    {
        m_states.add(state);
    }
    
    /**
     * Delete a state from the machine.
     * @param state The state to delete.
     * @return true if the state is successfully removed, false otherwise.
     */
    public boolean deleteState(TM_State state)
    {
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
    private void removeTransitionsConnectedTo(TM_State state)
    {
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
    
    // Static methods for serialization:
    /** 
     * Serialize a machine, and write it to persistent storage.
     * @param machine The machine to serialize.
     * @param file The filename to write to.
     * @return true if the serialization and write was successful, false otherwise.
     */
    public static boolean saveTMachine(TMachine machine, String file)
    {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
           fos = new FileOutputStream(file);
           out = new ObjectOutputStream(fos);
           out.writeObject(machine);
           out.close();
        }
        catch(IOException ex)
        {
           ex.printStackTrace();
           return false;
        }
        return true;
    }
    
    /**
     * Load and deserialize a machine from persistent storage.
     * @param file The filename where the machine was serialized and written to.
     * @return The deserialized machine, or null if the machine was not successfully loaded.
     */
    public static TMachine loadTMachine(String file)
    {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        TMachine returner = null;
        try
        {
            fis = new FileInputStream(file);
            in = new ObjectInputStream(fis);
            returner = (TMachine)in.readObject();
            in.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
        } 
        return returner;
   }
    
    // Methods related to graphics and user interface interaction:
    /** 
     * Render the machine to a graphics object.
     * @param g The graphics object to render to.
     * @param selectedStates The set of states which are selected by the user.
     * @param selectedTransitions The set of transitions which are selected by the user.
     * @param simulator The current machine simulator.
     */
    public void paint(Graphics g, Collection<TM_State> selectedStates,
            Collection<TM_Transition> selectedTransitions, TM_Simulator simulator)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLUE);
        
        for (TM_Transition tr : m_transitions)
        {
            tr.paint(g, selectedTransitions, simulator);
        }
        
        for (TM_State state : m_states)
        {
            state.paint(g, selectedStates);
        }
    }
   
    /**
     * Determine if there is a state at the given coordinates.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @return The topmost state at the given coordinates, or null if there is no such state.
     */
    public TM_State getStateClickedOn(int clickX, int clickY)
    {
        TM_State returner = null;
        // Gets the last one, which should also be the last drawn one, i.e. the topmost state.
        for (TM_State state : m_states)
        {
            if (state.containsPoint(clickX, clickY))
            {
                returner = state;
            }
        }
        return returner;
    }
    
    /**
     * Determine if there is a state label at the given coordinates.
     * @param g The graphics object, used to measure the label's dimensions.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @return The topmost state at the given coordinates, or null if there is no such state.
     */
    public TM_State getStateNameClickedOn(Graphics g, int clickX, int clickY)
    {
        TM_State returner = null;
        // Gets the last one, which should also be the last drawn one, i.e. the topmost state.
        for (TM_State state : m_states)
        {
            if (state.nameContainsPoint(g, clickX, clickY))
            {
                returner = state;
            }
        }
        return returner;
    }
   
    /**
     * Determine if there is a transition a the given coordinates.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @param g The graphics object, used to measure the transition's dimensions.
     * @return The topmost transition at the given coordinates, or null if there is no such transition.
     */
    public TM_Transition getTransitionClickedOn(int clickX, int clickY, Graphics g)
    {
        TM_Transition returner = null;
        // Gets the last one, which should also be the last drawn one, i.e. the topmost state.
        for (TM_Transition transition : m_transitions)
        {
            if (transition.actionContainsPoint(clickX, clickY, g))
            {
                returner = transition;
            }
            if (transition.arrowContainsPoint(clickX, clickY, g))
            {
                returner = transition;
            }
        }
        return returner;
    }
    
    /**
     * Create a hashtable, mapping every state label to itself.
     * @return A hashtable containing every state label, mapped to itself.
     */
    public Hashtable<String, String> createLabelsHashtable()
    {
        Hashtable<String, String> returner = new Hashtable<String, String>();
        for (TM_State state : m_states)
        {
            String label = state.getLabel();
            returner.put(label, label);
        }
        return returner;
    }
   
    /**
     * Get the alphabet associated with this machine.
     * @return The alphabet associated with this machine.
     */
    public Alphabet getAlphabet()
    {
        return m_alphabet;
    }
    
    /**
     * Set the alphabet associated with this machine.
     * @param alphabet The new alphabet associated with this machine.
     */
    public void setAlphabet(Alphabet alphabet)
    {
        m_alphabet = alphabet;
    }
    
    /** 
     * Determine if this machine is consistent with the alphabet, i.e. does not have any transitions
     * with inputs not present in the alphabet.
     * @param a The alphabet to check consistency with.
     * @return true if there are no transitions with symbols not in the alphabet, false otherwise.
     */
    public boolean isConsistentWithAlphabet(Alphabet a)
    {
        for (TM_Transition t: m_transitions)
        {
            TM_Action act = t.getAction();
            if (!a.containsSymbol(t.getSymbol()) &&
                t.getSymbol() != TMachine.UNDEFINED_SYMBOL &&
                t.getSymbol() != TMachine.EMPTY_ACTION_SYMBOL &&
                t.getSymbol() != TMachine.OTHERWISE_SYMBOL)
            {
                return false;
            }
            else if (act.getDirection() == 0 &&
                     !a.containsSymbol(act.getChar()) &&
                     act.getChar() != TMachine.UNDEFINED_SYMBOL &&
                     act.getChar() != TMachine.EMPTY_ACTION_SYMBOL &&
                     act.getChar() != TMachine.OTHERWISE_SYMBOL)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Delete transitions that are not consistent with this machine's alphabet.
     * @param panel The current graphics panel.
     */
    public void removeInconsistentTransitions(TMGraphicsPanel panel)
    {
        ArrayList<TM_Transition> purge = new ArrayList<TM_Transition>();
        for (TM_Transition t: m_transitions)
        {
            TM_Action act = t.getAction();
            if (!m_alphabet.containsSymbol(t.getSymbol()) &&
                t.getSymbol() != TMachine.EMPTY_ACTION_SYMBOL &&
                t.getSymbol() != TMachine.UNDEFINED_SYMBOL &&
                t.getSymbol() != TMachine.OTHERWISE_SYMBOL)
            {
                purge.add(t);
            }
            else if (act.getDirection() == 0 &&
                    !m_alphabet.containsSymbol(act.getChar()) &&
                    act.getChar() != TMachine.EMPTY_ACTION_SYMBOL &&
                    act.getChar() != TMachine.UNDEFINED_SYMBOL &&
                    act.getChar() != TMachine.OTHERWISE_SYMBOL)
            {
                purge.add(t);
            }
        }
        
        panel.doCommand(new RemoveInconsistentTransitionsCommand(panel, purge));
    }
    
    /** 
     * Finds the transitions whose 'to' state is the given state.
     * A new ArrayList will be generated each time this is called.
     * The running time complexity is O(m), where m is the number of transitions in the machine.
     * @param state The state which transitions are connected to.
     * @return An array list of transitions connected to the specified state.
     */
    public ArrayList<TM_Transition> getTransitionsTo(TM_State state)
    {
        ArrayList<TM_Transition> returner = new ArrayList<TM_Transition>();
        for (TM_Transition t: m_transitions)
        {
            if (t.getToState() == state)
            {
                returner.add(t);
            }
        }
        return returner;
    }
    
    /**
     * Returns a set of states that are at least partially contained within the specified selection
     * rectangle.
     * @param topLeftX The top-left X ordinate of the selection rectangle.
     * @param topLeftY The top-left Y ordinate of the selection rectagnle.
     * @param width A non-negative integer, representing the width of the selection rectangle.
     * @param height A non-negative integer, representing the height of the selection rectangle.
     * @return The set of states contained within the specified selection rectangle.
     */
    public HashSet<TM_State> getSelectedStates(int topLeftX, int topLeftY, int width, int height)
    {
        HashSet<TM_State> returner = new HashSet<TM_State>();
        Rectangle2D container = new Rectangle2D.Float(topLeftX - TM_State.STATE_RENDERING_WIDTH,
                topLeftY - TM_State.STATE_RENDERING_WIDTH, width + TM_State.STATE_RENDERING_WIDTH,
                height+ TM_State.STATE_RENDERING_WIDTH);
        
        for (TM_State t : m_states)
        {
            if (container.contains(t.getX(), t.getY()))
            {
                returner.add(t);
            }
        }
        return returner;
    }
    
    /** 
     * Returns the set of transitions which are connected only to states within selectedStates.
     * @param selectedStates The set of states to check.
     * @return A set of transitions, such that every transition is connected to exactly two states
     *         from selectedStates.
     */
    public HashSet<TM_Transition> getSelectedTransitions(HashSet<TM_State> selectedStates)
    {
        HashSet<TM_Transition> returner = new HashSet<TM_Transition>();
        for (TM_Transition t: m_transitions)
        {
            if (selectedStates.contains(t.getFromState()) && selectedStates.contains(t.getToState()))
            {
                returner.add(t);
            }
        }
        return returner;
    }
    
    /** 
     * Returns the set of transitions which are connected only at one end to states within
     * selectedStates. These transitions would not be copied but would be deleted during a cut or
     * delete selected operation, and need to be replaced when a delete operation is undone.
     * @param selectedStates The set of states to check.
     * @return A set of transitions, such that every transition is connected to at least one state
     *         from selectedStates
     */
    public HashSet<TM_Transition> getHalfSelectedTransitions(Collection<TM_State> selectedStates)
    {
        HashSet<TM_Transition> returner = new HashSet<TM_Transition>();
        for (TM_Transition t: m_transitions)
        {
            if ((selectedStates.contains(t.getFromState()) && !selectedStates.contains(t.getToState())) ||
                (!selectedStates.contains(t.getFromState()) && selectedStates.contains(t.getToState())))
            {
                returner.add(t);
            }
        }
        return returner;
    }
   
    /**
     * The set of states in the machine.
     */
    private ArrayList<TM_State> m_states;
    
    /**
     * The set of transitions in the machine.
     */
    private ArrayList<TM_Transition> m_transitions;

    /**
     * The alphabet for the machine.
     */
    private Alphabet m_alphabet;
}
