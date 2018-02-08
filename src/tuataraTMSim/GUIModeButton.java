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

import javax.swing.Action;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * An extension of the JButton type, which has an associated action and GUI mode.
 */
public class GUIModeButton extends JButton
{
    /**
     * Creates a new instance of GUIModeButton.
     * @param act The abstract action associated with this button.
     * @param mode The mode to switch to after pressing this button.
     */
    public GUIModeButton(Action act, GUI_Mode mode)
    {
        super();
        setAction(act);
        setFocusable(false);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setText("");
        m_mode = mode;
    }
    
    /**
     * Get the GUI mode associated with this button.
     * @return The GUI mode associated with this button.
     */
    public GUI_Mode getGUI_Mode()
    {
        return m_mode;
    }
    
    /**
     * The GUI mode associated with this button.
     */
    private GUI_Mode m_mode;
}
