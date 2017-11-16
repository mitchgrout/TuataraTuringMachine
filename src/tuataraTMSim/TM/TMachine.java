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
 * An implementation of a turing machine.  This class stores the
 * specification of the machine (effectively its transition table), but
 * does not contain any configuration (execution state) information.
 * The machine is not guaranteed to be deterministic at all times,
 * but can be assured to be deterministic by calling TMachine.validate()
 *
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
     */
    public TMachine(ArrayList<TM_State> states, ArrayList<TM_Transition> transitions, Alphabet alphabet)
    {
        m_states = states;
        m_transitions = transitions;
        m_alphabet = alphabet;
    }
    
    /**
     * Creates a new instance of TMachine
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
     */
    public void validate() throws NondeterministicException
    {
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
     * Update the given tape with the result produced by an iteration of an instance of this
     * abstract machine that is currently in the given state.
     */
    public TM_State step(Tape tape, TM_State currentState, TM_Transition currentNextTransition)
        throws TapeBoundsException, UndefinedTransitionException, ComputationCompletedException
    {
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
     * Gets the start state. Assumes that validate() has been called.
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
     * Gets the final state. Assumes that validate() has been called.
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
    public void addState(TM_State state)
    {
        m_states.add(state);
    }
    
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
     * Adds a transition to the machine, and also adds it as an outgoing transition to the 'from' state.
     */
    public void addTransition(TM_Transition transition)
    {
        m_transitions.add(transition);
        transition.getFromState().addTransition(transition);
    }
    
    public boolean deleteTransition(TM_Transition transition)
    {
        if (m_transitions.remove(transition))
        {
            transition.getFromState().removeTransition(transition);
            return true;
        }
        return false;
    }
    
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
     * Save (serialize) a tape to persistent storage.
     * @returns true IFF the serialization was successful.
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
     * Load (deserialize) a Turing machine from persistent storage.
     * @param file     The file where the tape is stored.
     * @returns        The tape that was loaded, or null
     *                 if the tape was not successfully
     *                 loaded.
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
     */
    public void paint(Graphics g, Collection<TM_State> selectedStates, Collection<TM_Transition> selectedTransitions, TM_Simulator simulator)
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
    
    public Alphabet getAlphabet()
    {
        return m_alphabet;
    }
    
    public void setAlphabet(Alphabet alphabet)
    {
        m_alphabet = alphabet;
    }
    
    /** 
     * Returns true IFF there are no transition actions that contain symbols that are not in the alphabet.
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
     */
    public void removeInconsistentTransitions(TMGraphicsPanel panel)
    {
        ArrayList<TM_Transition> purge = new ArrayList<TM_Transition>();
        for (TM_Transition t: m_transitions)
        {
            TM_Action act = t.getAction();
            if (!m_alphabet.containsSymbol(t.getSymbol()) &&
                t.getSymbol() != TMachine.EMPTY_ACTION_SYMBOL &&
                t.getSymbol() != TMachine.OTHERWISE_SYMBOL)
            {
                purge.add(t);
            }
            else if (act.getDirection() == 0 &&
                    !m_alphabet.containsSymbol(act.getChar()) &&
                    act.getChar() != TMachine.EMPTY_ACTION_SYMBOL &&
                    act.getChar() != TMachine.OTHERWISE_SYMBOL)
            {
                purge.add(t);
            }
        }
        
        panel.doCommand(new RemoveInconsistentTransitionsCommand(panel, purge));
    }
    
    /** 
     * Finds the transitions who's 'to' state is the given state.
     * A new ArrayList will be generated each time this is called.
     * The running time complexity is O(m), where m is the number of transitions in the machine.
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
     * Returns a set of states that are at least partially contained within the specified selection region.
     * @pre width, height are positive numbers
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
     * Returns the set of transitions which are connected only at one end to states within selectedStates.
     * These transitions would not be copied but would be deleted during a cut or delete selected operation,
     * and need to be replaced when a delete operation is undone.
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
    
    ArrayList<TM_State> m_states;
    ArrayList<TM_Transition> m_transitions;
    Alphabet m_alphabet;
}
