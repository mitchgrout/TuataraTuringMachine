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
import java.io.*;

/**
 *
 * @author Jimmy
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        
        //create a simple machine
        TMachine myTM = new TMachine();
        /*TM_State q0 = new TM_State("q0", true, false);
        TM_State q1 = new TM_State("q1", false, true);
        q0.addTransition(new TM_Transition(q0, q0, '0', new TM_Action(0,'1')));
        q0.addTransition(new TM_Transition(q0, q1, '1', new TM_Action(0,'1')));
        myTM.addState(q0);
        myTM.addState(q1);*/
        
        
        //Turing machine for unary successor function
        TM_State q0 = new TM_State("q0", true, false);
        TM_State q1 = new TM_State("q1", false, false);
        TM_State q2 = new TM_State("q2", false, false);
        TM_State q3 = new TM_State("q3", false, false);
        TM_State q4 = new TM_State("q4", false, true);
        myTM.addTransition(new TM_Transition(q0, q1, '1', new TM_Action(0,'1', '_')));
        myTM.addTransition(new TM_Transition(q1, q2, '_', new TM_Action(1,'_', '_')));
        myTM.addTransition(new TM_Transition(q2, q2, '1', new TM_Action(1,'1', '_')));
        myTM.addTransition(new TM_Transition(q2, q3, '_', new TM_Action(0,'_', '1')));
        myTM.addTransition(new TM_Transition(q3, q3, '1', new TM_Action(-1,'1', '_')));
        myTM.addTransition(new TM_Transition(q3, q4, '_', new TM_Action(0,'_', '1')));
        myTM.addState(q0);
        myTM.addState(q1);
        myTM.addState(q2);
        myTM.addState(q3);
        myTM.addState(q4);
        
        TMachine.saveTMachine(myTM, "machine1.tm");
        myTM = TMachine.loadTMachine("machine1.tm");
        
        
        
        Tape myTape = new CA_Tape("111");
        Tape.saveTape(myTape, "tape1.tap");
        myTape = Tape.loadTape("tape1.tap");
        TM_Simulator mySim = new TM_Simulator(myTM, myTape);

        mySim.runUntilHalt(1000, true);
        
    }
    
}
