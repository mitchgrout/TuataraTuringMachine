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

import java.awt.Font;
import javax.swing.ImageIcon;

/**
 * A global utility class containing values and functions which are used by several distinct types,
 * but not specific enough to belong to any of them in particular. This class should not be
 * instantiated.
 */
public final class Global
{
    // Undocumented intentionally. This class should not be instantiated.
    private Global() { }

    /**
     * Font used for rendering text.
     */
    public static final Font FONT_DIALOG = new Font(Font.DIALOG, Font.PLAIN, 12);

    /**
     * Font used for rendering machine-related text.
     */
    public static final Font FONT_MONOSPACE = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    /**
     * Character used to separate machine configuration steps.
     */
    public static final char CONFIG_TEE = '\u02Eb';

    /**
     * Create an ImageIcon based off of the given filename. The images/ directory is prepended to
     * the given filename.
     * @param fname The filename of the image, found in the images/ directory.
     * @return An ImageIcon representing the loaded image.
     */
    public static ImageIcon loadIcon(String fname)
    {
        return new ImageIcon(Global.class.getResource("images/" + fname));
    }
}
