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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.NamingScheme;

/**
 * An abstraction of an object which represents the structure of some finite state machine. This
 * class should store only the structure, nothing about the execution state, which is handled by
 * Simulator.
 */
public abstract class Machine<
    PREACTION extends PreAction,
    TRANSITION extends Transition<PREACTION, STATE, ?, SIMULATOR>,
    STATE extends State<PREACTION, TRANSITION, ?, SIMULATOR>,
    SIMULATOR extends Simulator<PREACTION, TRANSITION, STATE, ?>> implements Serializable
{
    /**
     * Serialization version.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Does not match anything; instead, this is used to indicate that an action has not yet been
     * assigned a value.
     */
    public static final char UNDEFINED_SYMBOL = '!';

    /**
     * Creates an instance of Machine.
     * @param alphabet The tape alphabet.
     */
    public Machine(Alphabet alphabet)
    {
        m_alphabet = alphabet;
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
     * Get the naming scheme for this machine. By default, this always returns GENERAL. Machines
     * which use different naming schemes should @Override this.
     * @return The naming scheme for this machine.
     */
    public NamingScheme getNamingScheme()
    {
        return NamingScheme.GENERAL;
    }

    /**
     * Set the naming scheme for this machine. By default, this does nothing. Machines which use
     * different naming schemes should @Override this.
     * @param scheme The new naming scheme.
     */
    public void setNamingScheme(NamingScheme scheme)
    {
        // Do nothing
    }

    /**
     * Create a hashset, containing every state label.
     * @return A hashset containing every state label.
     */
    public HashSet<String> getLabelHashset()
    {
        HashSet<String> result = new HashSet<String>();
        for (STATE state : getStates())
        {
            result.add(state.getLabel());
        }
        return result;
    } 

    /**
     * Determine if this machine is consistent with the alphabet, i.e. does not have any actions not
     * present in the alphabet.
     * @param a The alphabet to check consistency with.
     * @return true if there are no actions which are not in the alphabet, false otherwise.
     */
    public boolean isConsistentWithAlphabet(Alphabet a)
    {
        for (TRANSITION t : getTransitions())
        {
            if (!isConsistentWithAlphabet(t.getAction(), a))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine all transitions which are deemed inconsistent with the given alphabet.
     * @param a The alphabet to check consistency with.
     * @return A collection of transitions which are inconsistent with the alphabet.
     */
    public final ArrayList<TRANSITION> getInconsistentTransitions(Alphabet a)
    {
        ArrayList<TRANSITION> result = new ArrayList<TRANSITION>();
        for (TRANSITION t : getTransitions())
        {
            if (!isConsistentWithAlphabet(t.getAction(), a))
            {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Finds the transitions whose 'to' state is the given state.
     * A new ArrayList will be generated each time this is called.
     * @param s The state which transitions are connected to.
     * @return An ArrayList of transitions connected to the specified state.
     */
    public final ArrayList<TRANSITION> getTransitionsTo(STATE s)
    {
        ArrayList<TRANSITION> result = new ArrayList<TRANSITION>();
        for (TRANSITION t : getTransitions())
        {
            if (t.getToState() == s)
            {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Returns a set of states that are at least partially container within the specified selection
     * rectangle.
     * @param topLeftX The top-left X ordinate of the selection rectangle.
     * @param topLeftY The top-left Y ordinate of the selection rectangle.
     * @param width A non-negative integer representing the width of the selection rectangle.
     * @param height A non-negative integer representing the height of the selection rectangle.
     * @return A set of states contained within the specified selection rectangle.
     */
    public final HashSet<STATE> getSelectedStates(int topLeftX, int topLeftY, int width, int height)
    {
        HashSet<STATE> result = new HashSet<STATE>();
        Rectangle2D container = new Rectangle2D.Float(
                topLeftX - STATE.STATE_RENDERING_WIDTH, topLeftY - STATE.STATE_RENDERING_WIDTH,
                width + STATE.STATE_RENDERING_WIDTH, height + STATE.STATE_RENDERING_WIDTH);
        for (STATE s : getStates())
        {
            if (container.contains(s.getX(), s.getY()))
            {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Determine if there is a state at the given coordinates.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @return The topmost state at the given coordinates, or null if there is no such state.
     */
    public STATE getStateClickedOn(int clickX, int clickY)
    {
        STATE result = null;
        for (STATE s : getStates())
        {
            if (s.containsPoint(clickX, clickY))
            {
                result = s;
            }
        }
        return result;
    }

    /**
     * Determine if there is a state label at the given coordinates.
     * @param g The graphics object, used to measure the label's dimensions.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @return The topmost state at the given coordinates, or null if there is no such state.
     */
    public STATE getStateLabelClickedOn(Graphics g, int clickX, int clickY)
    {
        STATE result = null;
        for (STATE s : getStates())
        {
            if (s.nameContainsPoint(g, clickX, clickY))
            {
                result = s;
            }
        }
        return result;
    }

    /**
     * Determine if there is a transition a the given coordinates.
     * @param clickX The X ordinate.
     * @param clickY The Y ordinate.
     * @param g The graphics object, used to measure the transition's dimensions.
     * @return The topmost transition at the given coordinates, or null if there is no such transition.
     */
    public TRANSITION getTransitionClickedOn(int clickX, int clickY, Graphics g)
    {
        TRANSITION result = null;
        for (TRANSITION t : getTransitions())
        {
            if (t.actionContainsPoint(clickX, clickY, g) || 
                t.arrowContainsPoint(clickX, clickY, g))
            {
                result = t;
            }
        }
        return result;
    }


    /**
     * Returns the set of transitions which are connected only to states within selectedStates.
     * @param selectedStates The set of states to check.
     * @return A set of transitions, such that every transition is connected to exactly two states
     *         from selectedStates.
     */
    public final HashSet<TRANSITION> getSelectedTransitions(HashSet<STATE> selectedStates)
    {
        HashSet<TRANSITION> result = new HashSet<TRANSITION>();
        for (TRANSITION t : getTransitions())
        {
            if (selectedStates.contains(t.getFromState()) && selectedStates.contains(t.getToState()))
            {
                result.add(t);
            }
        }
        return result;
    }

    /** 
     * Returns the set of transitions which are connected only at one end to states within
     * selectedStates. These transitions would not be copied but would be deleted during a cut or
     * delete selected operation, and need to be replaced when a delete operation is undone.
     * @param selectedStates The set of states to check.
     * @return A set of transitions, such that every transition is connected to at least one state
     *         from selectedStates
     */
    public HashSet<TRANSITION> getHalfSelectedTransitions(Collection<STATE> selectedStates)
    {
        HashSet<TRANSITION> result = new HashSet<TRANSITION>();
        for (TRANSITION t : getTransitions())
        {
            if ((selectedStates.contains(t.getFromState()) && !selectedStates.contains(t.getToState())) ||
                    (!selectedStates.contains(t.getFromState()) && selectedStates.contains(t.getToState())))
            {
                result.add(t);
            }
        }
        return result;
    }

    /** 
     * Render the machine to a graphics object.
     * @param g The graphics object to render to.
     * @param selectedStates The set of states which are selected by the user.
     * @param selectedTransitions The set of transitions which are selected by the user.
     * @param simulator The current machine simulator.
     */
    public void paint(Graphics g, Collection<STATE> selectedStates,
                      Collection<TRANSITION> selectedTransitions, SIMULATOR simulator)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLUE);

        for (TRANSITION tr : getTransitions())
        {
            tr.paint(g, selectedTransitions, simulator);
        }

        for (STATE state : getStates())
        {
            state.paint(g, selectedStates);
        }
    }

    /** 
     * Serialize a machine, and write it to persistent storage.
     * @param machine The machine to serialize.
     * @param file The file to write to.
     * @return true if the machine is saved successfully, false otherwise.
     */
    public static final boolean saveMachine(Machine machine, File file)
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(machine);
            out.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Load and deserialize a machine from persistent storage.
     * @param file The file where the machine was serialized and written to.
     * @return The deserialized machine.
     */
    public static final Machine loadMachine(File file)
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            Machine result = (Machine)in.readObject();
            in.close();
            return result;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Determine whether this machine is valid, in terms of its formal definition.
     * @throws NondeterministicException If the machine is considered nondeterministic.
     */
    public abstract void validate() throws NondeterministicException;

    /**
     * Mark this machine as invalid. The effect of this should be that the next call to validate()
     * should force all relevant checks to occur.
     */
    public abstract void invalidate();

    /** 
     * Given a current state and tape, determine the next state the machine should move to, and
     * perform any relevant actions.
     * @param tape The current tape.
     * @param currentState The state that this machine is currently in.
     * @param currentNextTransition The next transition to be taken, determined by the value at the
     *                              tape, and the current state.
     * @return The new state that the machine is in after this step.
     * @throws TapeBoundsException If the action causes the read/write head to fall off the tape.
     * @throws UndefinedTransitionException If there is no transition to take.
     * @throws ComputationCompletedException If after this step, we have finished execution, and the
     *                                       machine accepts the input.
     * @throws ComputationFailedException If after this step, we have finished execution, but the
     *                                    machine fails to accept the input
     */
    public abstract STATE step(Tape tape, STATE currentState, TRANSITION currentNextTransition)
        throws TapeBoundsException, UndefinedTransitionException,
               ComputationCompletedException, ComputationFailedException;

    /**
     * Get a collection containing all states in this machine.
     * @return A collection of all states in this machine.
     */
    public abstract Collection<STATE> getStates();

    /**
     * Get a collection containing all transitions in this machine.
     * @return A collection of all transitions in this machine.
     */
    public abstract Collection<TRANSITION> getTransitions();


    /** 
     * Get the unique start state. May assume the machine is valid.
     * @return The unique start state of the machine.
     */
    public abstract STATE getStartState();

    /**
     * Get a set containing all final states.
     * @return A non-null collection of all final states in this machine.
     */
    public abstract Collection<STATE> getFinalStates();

    /**
     * Add a state to the machine.
     * @param state The state to add.
     */
    public abstract void addState(STATE state);

    /**
     * Delete a state from the machine.
     * @param state The state to delete.
     * @return true if the state is successfully removed, false otherwise.
     */
    public abstract boolean deleteState(STATE state);

    /** 
     * Adds a transition to the machine. Should also add the transition as outgoing from the from state.
     * @param transition The transition to add.
     */
    public abstract void addTransition(TRANSITION transition);

    /**
     * Deletes a transition from the machine, and removes it as an outgoing transition from the from state.
     * @param transition The transition to delete.
     * @return true if the transition is successfully removed, false otherwise.
     */
    public abstract boolean deleteTransition(TRANSITION transition);

    /**
     * Removes all transitions associated with a state.
     * @param state The state which removed transitions are connected to, either incoming or outgoing.
     */
    protected abstract void removeTransitionsConnectedTo(STATE state);

    /**
     * Determine whether a given action is consistent with an alphabet.
     * @param act The action to test the consistency of.
     * @param alph The alphabet to test against.
     * @return true if the action is considered consistent with the alphabet.
     */
    protected abstract boolean isConsistentWithAlphabet(PREACTION act, Alphabet alph);

    /**
     * The alphabet for the machine.
     */
    protected Alphabet m_alphabet;
}
