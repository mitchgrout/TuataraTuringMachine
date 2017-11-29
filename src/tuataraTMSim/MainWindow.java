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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;
import tuataraTMSim.commands.*;
import tuataraTMSim.exceptions.*;
import tuataraTMSim.TM.*;

/**
 * The main window of the program. An MDI interface for building and running turing machines.
 * This class is the main entry point into the program.
 * @author Jimmy
 */
public class MainWindow extends JFrame
{
    /**
     * Font used for rendering text.
     */
    public static final Font FONT_USED = new Font("Dialog", Font.PLAIN, 12);
    
    /**
     * The layer used for internal frames containing machines.
     */
    public static final int MACHINE_WINDOW_LAYER = 50;
   
    /**
     * File extension for tapes.
     */
    public static final String TAPE_EXTENSION = ".tap";
    /**
     * File extension for machines.
     */
    public static final String MACHINE_EXTENSION = ".tm";
    
    /**
     * Internal timer.
     */
    private final java.util.Timer m_timer = new java.util.Timer(true);
    
    /**
     * Error string for nondeterministic errors.
     */
    public static final String NONDET_ERR_STR = "The machine could not be validated.";

    /**
     * Error string for undefined transition errors.
     */
    public static final String TRANS_UNDEF_ERR_STR = "The computation did not complete successfully.";

    /**
     * Error string for tape bound errors.
     */
    public static final String TAPE_BOUNDS_ERR_STR= "The machine r/w head went past the start of the tape.";

    /**
     * Error string for unknown errors.
     */
    public static final String OTHER_ERROR_STR = "Error!";

    /**
     * String for execution halting.
     */
    public static final String HALTED_MESSAGE_TITLE_STR  = "Machine halted!";

    /**
     * String for computation completion.
     */
    public static final String COMPUTATION_COMPLETED_STR = "The machine halted correctly with the r/w head parked.";
    
    /**
     * Delay between steps for slow execution speed.
     */
    public static final int SLOW_EXECUTE_SPEED_DELAY = 1200;

    /**
     * Delay between steps for medium execution speed.
     */
    public static final int MEDIUM_EXECUTE_SPEED_DELAY = 800;

    /**
     * Delay between steps for fast execution speed.
     */
    public static final int FAST_EXECUTE_SPEED_DELAY = 400;

    /**
     * Delay between steps for superfast execution speed.
     */
    public static final int SUPERFAST_EXECUTE_SPEED_DELAY = 200;

    /**
     * Delay between steps for ultrafast execution speed.
     */
    public static final int ULTRAFAST_EXECUTE_SPEED_DELAY = 10;
    
    /**
     * Width of the machine canvas.
     */
    public static final int MACHINE_CANVAS_SIZE_X = 2000;

    /**
     * Height of the machine canvas.
     */
    public static final int MACHINE_CANVAS_SIZE_Y = 2000;
   
    /**
     * Horizontal translation of states to avoid stacking.
     */
    public static final int TRANSLATE_TO_AVOID_STACKING_X = TM_State.STATE_RENDERING_WIDTH * 2;

    /**
     * Vertical translation of states to avoid stacking.
     */
    public static final int TRANSLATE_TO_AVOID_STACKING_Y = TM_State.STATE_RENDERING_WIDTH * 2;
    
    /**
     * Maximum horizontal ratio for new window location.
     */
    public static final double maxHorizontalRatioForNewWindowLoc = 0.3;

    /**
     * Maximum vertical ratio for new window location.
     */
    public static final double maxVerticalRatioForNewWindowLoc = 0.3;

    /**
     * Minimum distance between two new windows.
     */
    public static int minDistanceForNewWindowLoc = 50;

    /**
     * Random step between distances between new windows.
     * Considered for removal.
     */
    public static int windowLocRandomStepSize = 10;
    
    /**
     * Random number generator used for state and transition placement.
     * Considered for removal.
     */
    private Random myRandom = new Random();
    
    /**
     * Creates a new instance of MainWindow.
     */
    public MainWindow()
    {
        initComponents();
        
        // Handle global keyboard input
        final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        
        kfm.addKeyEventPostProcessor(new KeyEventPostProcessor()
        {
           public boolean postProcessKeyEvent(KeyEvent e) 
           {       
               if (!m_keyboardEnabled)
               {
                   return false;
               }
               TMGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
               if (gfxPanel != null && !gfxPanel.getKeyboardEnabled())
               {
                   return false;
               }
               // Ignore anything with a ctrl or alt, as this may conflict with menu keyboard
               // shortcuts/accelerators
               if (e.isAltDown() || e.isControlDown())
               {
                   return false;
               }
               if (e.getID() == KeyEvent.KEY_TYPED ||
                    (e.getID() == KeyEvent.KEY_PRESSED &&
                      (e.isActionKey() ||
                       e.getKeyCode() == KeyEvent.VK_DELETE ||
                       e.getKeyCode() == KeyEvent.VK_BACK_SPACE)))
               {
                   if (m_asif.isVisible())
                   {
                       m_asif.handleKeyEvent(e);
                       return false;
                   }
                   char c = e.getKeyChar();

                   if (gfxPanel != null)
                   {
                       if (gfxPanel.handleKeyEvent(e))
                           gfxPanel.repaint();
                       else if (tapeDisp != null)
                       {
                           tapeDisp.handleKeyEvent(e);
                       }
                   }
                   // No graphics panel, just a tape
                   else if (tapeDisp != null) 
                   {
                       tapeDisp.handleKeyEvent(e);
                   }
               }
               return false;
           }
        });
        
        // Check for unsaved machines on exit.
        addWindowListener(new WindowAdapter()
        {
             public void windowClosing(WindowEvent e)
             {
                 userRequestToExit();   
             }
        });
    }
    
    /**
     * Program entry point.
     * @param args Command line arguments. Currently, all are ignored.
     */
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new MainWindow().setVisible(true);
            }
        });
    }

    /**
     * Create an ImageIcon based off of the given filename. The images/ directory is prepended to
     * the given filename.
     * @param fname The filename of the image, found in the images/ directory.
     * @return An ImageIcon representing the loaded image.
     */
    private static ImageIcon loadIcon(String fname)
    {
        return new ImageIcon(MainWindow.class.getResource("images/" + fname));
    }

    /** 
     * Selects the user interface interaction mode and notifies all internal windows accordingly.
     * This determines the results of user interactions such as clicking on the state diagrams.
     * @param mode The new GUI mode.
     */
    public void setUIMode(TM_GUI_Mode mode)
    {
        m_currentMode = mode;
        if (m_desktopPane == null)
        {
            return;
        }

        JInternalFrame[] internalFrames = m_desktopPane.getAllFramesInLayer(MACHINE_WINDOW_LAYER);
        for (JInternalFrame frame : internalFrames)
        {
            try
            {
                TMInternalFrame tmif = (TMInternalFrame)frame;
                TMGraphicsPanel panel = tmif.getGfxPanel();
                panel.setUIMode(mode);
            }
            catch (ClassCastException e)
            {
                // Not the right type of window, ignore it.
            }
        }

        for (GUIModeButton b : m_toolbarButtons)
        {
            if (b.getGUI_Mode() == mode)
            {
                b.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
            else
            {
                b.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            }
        }
    }
    
    /** 
     * Builds the main window and its components.
     */
    private void initComponents()
    {
        // Set up the main window
        setSize(new Dimension(640, 480));
        setTitle("Tuatara Turing Machine Simulator");
        setIconImage(loadIcon("tuatara.gif").getImage());

        // Omnibus will be the panel which contains everything barring the toolbar
        JPanel omnibus = new JPanel();
 
        // Desktop pane holds all internal frames
        m_desktopPane = new JDesktopPane();
        m_desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        // Set up the tape and associated controllers
        m_tape.setWindow(this);
        tapeDisp = new TMTapeDisplayPanel(m_tape);
        tapeDispController = new TMTapeDisplayControllerPanel(tapeDisp, this, 
                m_headToStartAction, m_eraseTapeAction, m_reloadTapeAction); 
        tapeDispController.setBounds(0, getHeight() - tapeDispController.getHeight(), getWidth(),100); 
        tapeDispController.setVisible(true);
        
        // Set up the file choosers; FQN is required as the compiler sees `FileFilter` as ambiguous
        fcMachine.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().endsWith(MACHINE_EXTENSION);
            }
            
            public String getDescription()
            {
                return "Turing Machine files";
            }
        });
        
        fcTape.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().endsWith(TAPE_EXTENSION);
            }
            
            public String getDescription()
            {
                return "Tape files";
            }
        });
        
        // Set up menus
        setJMenuBar(createMenus());
        
        // Set up toolbars
        ToolBarPanel toolbars = new ToolBarPanel(this, new FlowLayout(FlowLayout.LEFT));
        for (JToolBar tb : createToolbar())
        {
            toolbars.add(tb);
        }

        // Attach the toolbar to the top of the window
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(toolbars);

        // Add the desktop pane and tape controller to the omnibus; add that to the bottom of the window
        omnibus.setLayout(new BorderLayout());
        omnibus.add(m_desktopPane, BorderLayout.CENTER);
        omnibus.add(tapeDispController, java.awt.BorderLayout.SOUTH);
        getContentPane().add(omnibus);
       

        // Maximize on startup
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        
        // Make a state diagram window as the default for the desktop pane
        JInternalFrame iFrame = newMachineWindow(new TMachine(), null);
        m_desktopPane.add(iFrame);      
        m_desktopPane.setSelectedFrame(iFrame);
        m_desktopPane.getDesktopManager().activateFrame(iFrame);
        
        // Create the alphabet configuration internal frame
        m_asif = new AlphabetSelectorInternalFrame(this);
        m_asif.pack();

        // Create the scheme selector internal frame
        m_ssif = new SchemeSelectorInternalFrame(this);
        m_ssif.pack();
        
        // Make the internal frames quasi-modal.
        JPanel glass = new JPanel();
        glass.setOpaque(false);
        glass.add(m_asif);
        glass.add(m_ssif);
        setGlassPane(glass);
        
        // The ModalAdapter intercepts all mouse events when the glass pane is visible.
        ModalAdapter adapter = new ModalAdapter(glass);
        m_asif.addInternalFrameListener(adapter);
        m_ssif.addInternalFrameListener(adapter);

        setVisible(true);

        try
        {
            // setSelected only works when the component is already displayed, so this must be done
            // after the this.setVisible call.
            iFrame.moveToFront();
            iFrame.setSelected(true);
        }
        catch (PropertyVetoException e2) { }
        
        updateUndoActions();
    }
    
    /** 
     * Construct the menus and return the master JMenuBar object.
     * @return A menu bar containing all menus.
     */
    private JMenuBar createMenus()
    {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        
        fileMenu.add(new JMenuItem(m_newMachineAction));
        fileMenu.add(new JMenuItem(m_openMachineAction));
        fileMenu.add(new JMenuItem(m_saveMachineAction));
        fileMenu.add( new JMenuItem(m_saveMachineAsAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(m_newTapeAction));
        fileMenu.add(new JMenuItem(m_openTapeAction));
        fileMenu.add(new JMenuItem(m_saveTapeAction));
        fileMenu.add(new JMenuItem(m_saveTapeAsAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(m_exitAction));


        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);
        
        editMenu.add(new JMenuItem(m_undoAction));
        editMenu.add(new JMenuItem(m_redoAction));
        editMenu.add(new JMenuItem(m_cutAction));
        editMenu.add(new JMenuItem(m_copyAction));
        editMenu.add(new JMenuItem(m_pasteAction));
        editMenu.add(new JMenuItem(m_deleteAction));
        

        // Mode menu
        JMenu modeMenu = new JMenu("Mode");
        modeMenu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(modeMenu);

        ButtonGroup modeMenuItems = new ButtonGroup();
 
        JRadioButtonMenuItem m_addNodesMenuItem = new JRadioButtonMenuItem(m_addNodesAction);
        m_addNodesAction.setMenuItem(m_addNodesMenuItem);
        modeMenu.add(m_addNodesMenuItem);
        modeMenuItems.add(m_addNodesMenuItem);
       
        JRadioButtonMenuItem m_addTransitionsMenuItem = new JRadioButtonMenuItem(m_addTransitionsAction);
        m_addTransitionsAction.setMenuItem(m_addTransitionsMenuItem);
        modeMenu.add(m_addTransitionsMenuItem);
        modeMenuItems.add(m_addTransitionsMenuItem);
        
        JRadioButtonMenuItem m_makeSelectionMenuItem = new JRadioButtonMenuItem(m_selectionAction);
        m_selectionAction.setMenuItem(m_makeSelectionMenuItem);
        modeMenu.add(m_makeSelectionMenuItem);
        modeMenuItems.add(m_makeSelectionMenuItem);
        
        JRadioButtonMenuItem m_eraserMenuItem = new JRadioButtonMenuItem(m_eraserAction);
        m_eraserAction.setMenuItem(m_eraserMenuItem);
        modeMenu.add(m_eraserMenuItem);
        modeMenuItems.add(m_eraserMenuItem);
        
        JRadioButtonMenuItem m_chooseStartMenuItem = new JRadioButtonMenuItem(m_chooseStartAction);
        m_chooseStartAction.setMenuItem(m_chooseStartMenuItem);
        modeMenu.add(m_chooseStartMenuItem);
        modeMenuItems.add(m_chooseStartMenuItem);
        
        JRadioButtonMenuItem m_chooseAcceptingMenuItem = new JRadioButtonMenuItem(m_chooseAcceptingAction);
        m_chooseAcceptingAction.setMenuItem(m_chooseAcceptingMenuItem);
        modeMenu.add(m_chooseAcceptingMenuItem);
        modeMenuItems.add(m_chooseAcceptingMenuItem);
        
        JRadioButtonMenuItem m_chooseCurrentStateMenuItem = new JRadioButtonMenuItem(m_chooseCurrentStateAction);
        m_chooseCurrentStateAction.setMenuItem(m_chooseCurrentStateMenuItem);
        modeMenu.add(m_chooseCurrentStateMenuItem);
        modeMenuItems.add(m_chooseCurrentStateMenuItem);
 
        m_addNodesMenuItem.setSelected(true);
        

        // Machine menu
        JMenu machineMenu = new JMenu("Machine");
        machineMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(machineMenu);
        
        machineMenu.add(new JMenuItem(m_stepAction));
        machineMenu.add(new JMenuItem(m_fastExecuteAction));
        machineMenu.add(new JMenuItem(m_pauseExecutionAction));
        machineMenu.add(new JMenuItem(m_stopMachineAction));
        machineMenu.addSeparator();
        
        ButtonGroup executeSpeedMenuItems = new ButtonGroup();
        
        JRadioButtonMenuItem m_slowExecuteSpeed = new JRadioButtonMenuItem(m_slowExecuteSpeedAction);
        machineMenu.add(m_slowExecuteSpeed);
        executeSpeedMenuItems.add(m_slowExecuteSpeed);
        
        JRadioButtonMenuItem m_mediumExecuteSpeed = new JRadioButtonMenuItem(m_mediumExecuteSpeedAction);
        machineMenu.add(m_mediumExecuteSpeed);
        executeSpeedMenuItems.add(m_mediumExecuteSpeed);
        
        JRadioButtonMenuItem m_fastExecuteSpeed = new JRadioButtonMenuItem(m_fastExecuteSpeedAction);
        machineMenu.add(m_fastExecuteSpeed);
        executeSpeedMenuItems.add(m_fastExecuteSpeed);
        
        JRadioButtonMenuItem m_superFastExecuteSpeed = new JRadioButtonMenuItem(m_superFastExecuteSpeedAction);
        machineMenu.add(m_superFastExecuteSpeed);
        executeSpeedMenuItems.add(m_superFastExecuteSpeed);
        
        JRadioButtonMenuItem m_ultraFastExecuteSpeed = new JRadioButtonMenuItem(m_ultraFastExecuteSpeedAction);
        machineMenu.add(m_ultraFastExecuteSpeed);
        executeSpeedMenuItems.add(m_ultraFastExecuteSpeed);
        
        m_fastExecuteSpeed.setSelected(true);
        m_executionDelayTime = FAST_EXECUTE_SPEED_DELAY;
        
       
        // Tape menu
        JMenu tapeMenu = new JMenu("Tape");
        tapeMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(tapeMenu);
 
        tapeMenu.add(new JMenuItem(m_headToStartAction));
        tapeMenu.add(new JMenuItem(m_reloadTapeAction));
        tapeMenu.add(new JMenuItem(m_eraseTapeAction));
      

        // Config menu
        JMenu configMenu = new JMenu("Configuration");
        configMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(configMenu);

        configMenu.add(new JMenuItem(m_configureAlphabetAction));
        configMenu.add(new JMenuItem(m_configureSchemeAction));
        
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        helpMenu.add(new JMenuItem(m_showConsoleAction));
        helpMenu.add(new JMenuItem(m_helpAction));
        helpMenu.add(new JMenuItem(m_aboutAction));

        return menuBar;
    }
    
    /**
     * Set up a toolbar for quick access to common actions.
     * @return An array of created toolbars.
     */
    private JToolBar[] createToolbar()
    {
        // Every toolbar button will be registered here for iteration purposes
        m_toolbarButtons = new ArrayList<GUIModeButton>();

        // Entire toolstrip will be composed of three toolbars
        JToolBar[] returner = new JToolBar[3];

        // Tape
        JButton newTapeToolBarButton = new JButton(m_newTapeAction);
        newTapeToolBarButton.setFocusable(false);
        newTapeToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        newTapeToolBarButton.setText("");
        
        JButton openTapeToolBarButton = new JButton(m_openTapeAction);
        openTapeToolBarButton.setFocusable(false);
        openTapeToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        openTapeToolBarButton.setText("");
        
        JButton saveTapeToolBarButton = new JButton(m_saveTapeAction);
        saveTapeToolBarButton.setFocusable(false);
        saveTapeToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        saveTapeToolBarButton.setText("");

        // Machine
        JButton newMachineToolBarButton = new JButton(m_newMachineAction);
        newMachineToolBarButton.setFocusable(false);
        newMachineToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        newMachineToolBarButton.setText("");
        
        JButton openMachineToolBarButton = new JButton(m_openMachineAction);
        openMachineToolBarButton.setFocusable(false);
        openMachineToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        openMachineToolBarButton.setText("");
        
        JButton saveMachineToolBarButton = new JButton(m_saveMachineAction);
        saveMachineToolBarButton.setFocusable(false);
        saveMachineToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        saveMachineToolBarButton.setText("");

        // Edit       
        JButton cutToolBarButton = new JButton(m_cutAction);
        cutToolBarButton.setFocusable(false);
        cutToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cutToolBarButton.setText("");
        
        JButton copyToolBarButton = new JButton(m_copyAction);
        copyToolBarButton.setFocusable(false);
        copyToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        copyToolBarButton.setText("");
        
        JButton pasteToolBarButton = new JButton(m_pasteAction);
        pasteToolBarButton.setFocusable(false);
        pasteToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        pasteToolBarButton.setText("");
        
        JButton deleteToolBarButton = new JButton(m_deleteAction);
        deleteToolBarButton.setFocusable(false);
        deleteToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        deleteToolBarButton.setText("");
        
        m_undoToolBarButton = new JButton(m_undoAction);
        m_undoToolBarButton.setFocusable(false);
        m_undoToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        m_undoToolBarButton.setText("");
        
        m_redoToolBarButton = new JButton(m_redoAction);
        m_redoToolBarButton.setFocusable(false);
        m_redoToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        m_redoToolBarButton.setText("");
               
        // Configuration
        JButton configureAlphabetToolBarButton = new JButton(m_configureAlphabetAction);
        configureAlphabetToolBarButton.setFocusable(false);
        configureAlphabetToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        configureAlphabetToolBarButton.setText("");
       
        JButton configureSchemeToolBarButton = new JButton(m_configureSchemeAction);
        configureSchemeToolBarButton.setFocusable(false);
        configureSchemeToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        configureSchemeToolBarButton.setText("");

        // GUI mode
        GUIModeButton addNodesToolBarButton = new GUIModeButton(m_addNodesAction, TM_GUI_Mode.ADDNODES);
        m_toolbarButtons.add(addNodesToolBarButton);
        
        GUIModeButton addTransitionsToolBarButton = new GUIModeButton(m_addTransitionsAction, TM_GUI_Mode.ADDTRANSITIONS);
        m_toolbarButtons.add(addTransitionsToolBarButton);
        
        GUIModeButton selectionToolBarButton = new GUIModeButton(m_selectionAction,TM_GUI_Mode.SELECTION);
        m_toolbarButtons.add(selectionToolBarButton);
        
        GUIModeButton eraserToolBarButton = new GUIModeButton(m_eraserAction, TM_GUI_Mode.ERASER);
        m_toolbarButtons.add(eraserToolBarButton);
        
        GUIModeButton startStatesToolBarButton = new GUIModeButton(m_chooseStartAction, TM_GUI_Mode.CHOOSESTART);
        m_toolbarButtons.add(startStatesToolBarButton);
        
        GUIModeButton acceptingStatesToolBarButton = new GUIModeButton(m_chooseAcceptingAction, TM_GUI_Mode.CHOOSEACCEPTING);
        m_toolbarButtons.add(acceptingStatesToolBarButton);
        
        GUIModeButton chooseCurrentStateToolBarButton = new GUIModeButton(m_chooseCurrentStateAction, TM_GUI_Mode.CHOOSECURRENTSTATE);
        m_toolbarButtons.add(chooseCurrentStateToolBarButton);
 
        // Machine
        JButton stepToolBarButton = new JButton(m_stepAction);
        stepToolBarButton.setFocusable(false);
        stepToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        stepToolBarButton.setText("");
        
        JButton resetMachineToolBarButton = new JButton(m_stopMachineAction);
        resetMachineToolBarButton.setFocusable(false);
        resetMachineToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        resetMachineToolBarButton.setText("");

        JButton fastExecute = new JButton(m_fastExecuteAction);
        fastExecute.setFocusable(false);
        fastExecute.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        fastExecute.setText("");
        
        JButton stopExecutionToolBarButton = new JButton(m_pauseExecutionAction);
        stopExecutionToolBarButton.setFocusable(false);
        stopExecutionToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        stopExecutionToolBarButton.setText("");

        // Attach everything to the correct toolbars
        returner[0] = new JToolBar("File/Edit/Configure");
        returner[0].setRollover(true);
        returner[0].add(newTapeToolBarButton);
        returner[0].add(openTapeToolBarButton);
        returner[0].add(saveTapeToolBarButton);
        returner[0].add(newMachineToolBarButton);
        returner[0].add(openMachineToolBarButton);
        returner[0].add(saveMachineToolBarButton);
        returner[0].add(cutToolBarButton);
        returner[0].add(copyToolBarButton);
        returner[0].add(pasteToolBarButton);
        returner[0].add(deleteToolBarButton);
        returner[0].add(m_undoToolBarButton);
        returner[0].add(m_redoToolBarButton);
        returner[0].add(configureAlphabetToolBarButton);
        returner[0].add(configureSchemeToolBarButton); 

        returner[1] = new JToolBar("Mode");
        returner[1].setRollover(true);
        returner[1].add(addNodesToolBarButton);
        returner[1].add(addTransitionsToolBarButton);
        returner[1].add(selectionToolBarButton);
        returner[1].add(eraserToolBarButton);
        returner[1].add(startStatesToolBarButton);
        returner[1].add(acceptingStatesToolBarButton);
        returner[1].add(chooseCurrentStateToolBarButton);
        
        returner[2] = new JToolBar("Machine");
        returner[2].setRollover(true);
        returner[2].add(stepToolBarButton);
        returner[2].add(fastExecute);
        returner[2].add(stopExecutionToolBarButton);
        returner[2].add(resetMachineToolBarButton);
       
        // Default mode
        setUIMode(TM_GUI_Mode.ADDNODES);
        
        return returner;
    }
    
    /**
     * Creates a new window displaying a machine.
     * @param myTM The machine to display.
     * @param file The file associated with the machine.
     * @return A frame used to render the machine.
     */
    private TMInternalFrame newMachineWindow(TMachine myTM, File file)
    {
        final TMGraphicsPanel gfxPanel = new TMGraphicsPanel(myTM, m_tape, file, this);
        gfxPanel.setUIMode(m_currentMode);
        
        final TMInternalFrame returner = new TMInternalFrame(gfxPanel);
        gfxPanel.setWindow(returner);
        gfxPanel.setPreferredSize(new Dimension(MACHINE_CANVAS_SIZE_X, MACHINE_CANVAS_SIZE_Y));
        returner.setSize(new Dimension(640, 480));
        Point2D loc = nextWindowLocation();
        returner.setLocation((int)loc.getX(), (int)loc.getY());
        JScrollPane scroller = new JScrollPane(gfxPanel);
        scroller.setPreferredSize(new Dimension(300, 250));
        scroller.revalidate();
        returner.add(scroller);
        returner.setScrollPane(scroller);
        
        returner.setVisible(true);
        returner.setLayer(MACHINE_WINDOW_LAYER);
        returner.moveToFront();
        returner.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        
        returner.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameClosed(InternalFrameEvent e)
            {
                handleLostFocus();
            }
            
            public void internalFrameClosing(InternalFrameEvent e)
            {
                userConfirmSaveModifiedThenClose(returner);
            }
            
            public void internalFrameActivated(InternalFrameEvent e)
            {
                setEnabledActionsThatRequireAMachine(true);
            }
        });
        
        return returner;
    }
   
    /**
     * Compute the next location to place a new window.
     * @return The next location to place a new window.
     */
    Point2D.Float nextWindowLocation()
    {
        int x = lastNewWindowLocX + windowLocStepSize;
        int y = lastNewWindowLocY + windowLocStepSize;
        
        if (x > maxHorizontalRatioForNewWindowLoc * this.getWidth())
        {
            x %= maxHorizontalRatioForNewWindowLoc * this.getWidth();
            y = x;
            
            windowLocStepSize = minDistanceForNewWindowLoc + myRandom.nextInt(3)
                * windowLocRandomStepSize;
        }
        
        if (y > maxHorizontalRatioForNewWindowLoc * this.getWidth())
        {
            y %= maxHorizontalRatioForNewWindowLoc * this.getWidth();
            x = y;
            
            windowLocStepSize = minDistanceForNewWindowLoc + myRandom.nextInt(3)
                * windowLocRandomStepSize;
        }
        lastNewWindowLocX = x;
        lastNewWindowLocY = y;
        
        return new Point2D.Float((float)x, (float)y);
    }
    
    /** 
     * Ask the user to confirm whether they wish to save a modified machine. If they agree,
     * correctly handle the saving. Afterwards, close the window associated with the machine.
     * @param iFrame The frame being closed.
     * @return false if the user cancelled, true otherwise.
     */
    private boolean userConfirmSaveModifiedThenClose(TMInternalFrame iFrame)
    {
        TMGraphicsPanel gfxPanel = iFrame.getGfxPanel();
        iFrame.moveToFront();
        if (gfxPanel.isModifiedSinceSave())
        {
            // TODO: specify which machine
            String name = iFrame.getTitle();
            if (name.startsWith("* "))
            {
                name = name.substring(2);
            }
            int result = JOptionPane.showConfirmDialog(null,"The machine '" + name + "' is unsaved.  Do you wish to save it?", "Closing window",JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                TMachine machine = gfxPanel.getSimulator().getMachine();
                File outFile = gfxPanel.getFile();
                boolean saveSuccessful = false;
                if (outFile != null)
                {
                    saveSuccessful = TMachine.saveTMachine(machine, outFile.toString());
                }
                else
                {
                    saveSuccessful = saveMachineAs(gfxPanel);
                }
                if (saveSuccessful)
                {
                    iFrame.dispose();
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (result == JOptionPane.NO_OPTION)
            {
                iFrame.dispose();
                return true;
            }
            // On cancel, do nothing
            return false;
        }
        else
        {
            iFrame.dispose();
            return true;
        }
    }
    
    /**
     * When there is no focus owner, the focus needs to be redirected to a valid component so that
     * we can trap keyboard events. This method finds the best component to give the focus, and
     * transfers the focus to that component.
     */
    public void handleLostFocus()
    {
        if (m_desktopPane != null)
        {
            JInternalFrame selected = m_desktopPane.getSelectedFrame();

            JInternalFrame[] frames = m_desktopPane.getAllFrames();
            ArrayList<JInternalFrame> visibleFrames = new ArrayList<JInternalFrame>();
            for (JInternalFrame fr : frames)
            {
                if (fr.isVisible())
                {
                    visibleFrames.add(fr);
                }
            }
            if (visibleFrames.size() == 0)
            {
                m_desktopPane.requestFocusInWindow();
            }
            else
            {
                JInternalFrame frontMost = visibleFrames.get(0);
                
                for (JInternalFrame fr : visibleFrames)
                {
                    if (m_desktopPane.getIndexOf(fr) < m_desktopPane.getIndexOf(frontMost))
                    {
                        frontMost = fr;
                    }
                }
                m_desktopPane.setSelectedFrame(frontMost);
                try { frontMost.setSelected(true); }
                catch (Exception e) { }
            }
        }
        
        if (m_desktopPane.getSelectedFrame() == null)
        {
            setEnabledActionsThatRequireAMachine(false);
        }
        updateUndoActions();
    }
    
    /** 
     * Set the enabled/disabled status of Actions (ie toolbars and menu items) that
     * need a machine to apply to, however editing operations will only be enabled if
     * the isEditingEnabled() currently returns true.
     * @param isEnabled true if controls should be enabled, false otherwise.
     */
    private void setEnabledActionsThatRequireAMachine(boolean isEnabled)
    {
        m_stopMachineAction.setEnabled(isEnabled);
        m_pauseExecutionAction.setEnabled(isEnabled);
        
        if (isEditingEnabled() || isEnabled == false)
        {
            m_stepAction.setEnabled(isEnabled);
            m_configureAlphabetAction.setEnabled(isEnabled);
            m_configureSchemeAction.setEnabled(isEnabled);
            m_saveMachineAsAction.setEnabled(isEnabled);
            m_saveMachineAction.setEnabled(isEnabled);
            m_cutAction.setEnabled(isEnabled);
            m_copyAction.setEnabled(isEnabled);
            m_pasteAction.setEnabled(isEnabled);
            m_deleteAction.setEnabled(isEnabled);
            m_fastExecuteAction.setEnabled(isEnabled);
            
            m_addNodesAction.setEnabled(isEnabled);
            m_addTransitionsAction.setEnabled(isEnabled);
            m_eraserAction.setEnabled(isEnabled);
            m_selectionAction.setEnabled(isEnabled);
            m_chooseStartAction.setEnabled(isEnabled);
            m_chooseAcceptingAction.setEnabled(isEnabled);
            m_chooseCurrentStateAction.setEnabled(isEnabled);
            
            m_slowExecuteSpeedAction.setEnabled(isEnabled);
            m_mediumExecuteSpeedAction.setEnabled(isEnabled);
            m_fastExecuteSpeedAction.setEnabled(isEnabled);
            m_superFastExecuteSpeedAction.setEnabled(isEnabled);
            m_ultraFastExecuteSpeedAction.setEnabled(isEnabled);
        }
    }
    
    /**
     * Set whether or not all controls are to be enabled or not.
     * @param isEnabled true if all controls are to be enabled, false otherwise.
     */
    private void setEditingActionsEnabledState(boolean isEnabled)
    {
        m_stepAction.setEnabled(isEnabled);
        m_configureAlphabetAction.setEnabled(isEnabled);
        m_configureSchemeAction.setEnabled(isEnabled);
        m_cutAction.setEnabled(isEnabled);
        m_copyAction.setEnabled(isEnabled);
        m_pasteAction.setEnabled(isEnabled);
        m_undoAction.setEnabled(isEnabled);
        m_redoAction.setEnabled(isEnabled);
        m_deleteAction.setEnabled(isEnabled);
        m_fastExecuteAction.setEnabled(isEnabled);
        
        m_addNodesAction.setEnabled(isEnabled);
        m_addTransitionsAction.setEnabled(isEnabled);
        m_eraserAction.setEnabled(isEnabled);
        m_selectionAction.setEnabled(isEnabled);
        m_chooseStartAction.setEnabled(isEnabled);
        m_chooseAcceptingAction.setEnabled(isEnabled);
        m_chooseCurrentStateAction.setEnabled(isEnabled);
        
        m_newMachineAction.setEnabled(isEnabled);
        m_openMachineAction.setEnabled(isEnabled);
        m_saveMachineAsAction.setEnabled(isEnabled);
        m_saveMachineAction.setEnabled(isEnabled);
        m_newTapeAction.setEnabled(isEnabled);
        m_openTapeAction.setEnabled(isEnabled);
        m_saveTapeAsAction.setEnabled(isEnabled);
        m_saveTapeAction.setEnabled(isEnabled);
        
        m_slowExecuteSpeedAction.setEnabled(isEnabled);
        m_mediumExecuteSpeedAction.setEnabled(isEnabled);
        m_fastExecuteSpeedAction.setEnabled(isEnabled);
        m_superFastExecuteSpeedAction.setEnabled(isEnabled);
        m_ultraFastExecuteSpeedAction.setEnabled(isEnabled);
        
        m_headToStartAction.setEnabled(isEnabled);
        m_eraseTapeAction.setEnabled(isEnabled);
        m_reloadTapeAction.setEnabled(isEnabled);
    }

    /**
     * Compute the next transition for every simulator currently loaded.
     */
    public void updateAllSimulators()
    {
        if (m_desktopPane == null)
        {
            return;
        }
        JInternalFrame[] gfxFrames = m_desktopPane.getAllFramesInLayer(MACHINE_WINDOW_LAYER);
        for (JInternalFrame frame : gfxFrames)
        {
            try
            {
                TMInternalFrame tmif = (TMInternalFrame)frame;
                TMGraphicsPanel panel = tmif.getGfxPanel();
                if (panel != null)
                {
                    panel.getSimulator().computeNextTransition();
                    panel.repaint();
                }
            }
            catch (ClassCastException e)
            {
                // Wrong window type ignore it
                continue;
            }
        }
    }
    
    /**
     * Stop execution of the current machine.
     * @return true if the currently executing machine is stopped, false otherwise.
     */
    public boolean stopExecution()
    {
        if (m_timerTask != null)
        {
            return m_timerTask.cancel();
        }
        return false;
    }
 
    /**
     * Save the current machine, by displaying a file dialog.
     * @param panel The current graphics panel.
     * @return true if the machine is saved successfully, false otherwise.
     */
    public boolean saveMachineAs(TMGraphicsPanel panel)
    {
        while (true)
        {
            fcMachine.setDialogTitle("Save machine");
            m_keyboardEnabled = false; // Disable keyboard input in the main window/tape.
            int returnVal = fcMachine.showSaveDialog(MainWindow.this);
            m_keyboardEnabled = true;

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File outFile = fcMachine.getSelectedFile();
                try
                {
                    if (panel != null)
                    {
                        TMachine machine = panel.getSimulator().getMachine();
                        if (machine != null)
                        {
                            if (!outFile.toString().endsWith(MACHINE_EXTENSION))
                            {
                                outFile = new File(outFile.toString() + MACHINE_EXTENSION);
                            }
                            if (outFile.exists())
                            {
                                int overwrite = JOptionPane.showConfirmDialog(MainWindow.this, "The file '" + outFile.getName()
                                + "' already exists.  Overwrite?", "Save As", JOptionPane.YES_NO_CANCEL_OPTION);
                                if (overwrite == JOptionPane.CANCEL_OPTION)
                                    return false;
                                else if (overwrite == JOptionPane.NO_OPTION)
                                    continue; // Show dialogue again
                            }
                            boolean result = TMachine.saveTMachine(machine, outFile.toString());

                            // TODO: Assignment bug?
                            if (result = false)
                            {
                                throw new IOException(outFile.toString());
                            }
                            else
                            {
                                panel.setModifiedSinceSave(false);
                                panel.setFile(outFile);
                            }
                        }
                    }
                } 
                catch (Exception e2)
                {
                    JOptionPane.showMessageDialog(MainWindow.this, "An error occurred.  Your file has not been saved!");
                    return false;
                }
            }
            else
            {
                // User chose not to save
                return false;
            }
            return true;
        }
    }
    
    /** 
     * Determine if editing the machine or tape is enabled
     * @return true if editing is enabled, false otherwise.
     */
    public boolean isEditingEnabled()
    {
        return m_editingEnabled;
    }
    
    /**
     * Set whether editing the machine or tape is enabled.
     * @param isEnabled true if editing is enabled, false otherwise.
     */
    public void setEditingEnabled(boolean isEnabled)
    {
        m_editingEnabled = isEnabled;
        m_keyboardEnabled = isEnabled;
        if (m_desktopPane == null)
        {
            return; // Shouldnt happen.
        }
        JInternalFrame[] iFrames = m_desktopPane.getAllFrames();
        for (JInternalFrame f : iFrames)
        {
            try
            {
                TMInternalFrame tmif = (TMInternalFrame)f;
                TMGraphicsPanel gfxPanel = tmif.getGfxPanel();
                gfxPanel.setEditingEnabled(isEnabled);
                tmif.setClosable(isEnabled);
                m_exitAction.setEnabled(isEnabled); 
            }
            catch (ClassCastException e)
            {
                // Wrong window type
                continue;
            }
        }
        setEditingActionsEnabledState(isEnabled);
        tapeDispController.setEditingEnabled(isEnabled);
    }
 
    /**
     * Handle when a user requests to exit the program.
     */
    public void userRequestToExit()
    {
        if (m_desktopPane == null)
        {
            System.exit(0);
        }
        if (!m_editingEnabled)
        {
            // TODO: Can't close when running, somehow grey out the close button or something
            return;
        }

        JInternalFrame[] iFrames = m_desktopPane.getAllFrames();

        for (JInternalFrame f : iFrames)
        {
            try
            {
                TMInternalFrame tmif = (TMInternalFrame)f;
                TMGraphicsPanel panel = tmif.getGfxPanel();
                if (panel.isModifiedSinceSave())
                {
                    if (!userConfirmSaveModifiedThenClose(tmif))
                    {
                        return;
                    }
                }
                else
                {
                    tmif.dispose();
                }
            }
            catch (ClassCastException e2)
            {
                // Wrong window type
                continue;
            }
        }
        System.exit(0);
    }
 
    /**
     * Compute the centroid of the given set of states, and move the centroid of the machine to the
     * center of the window.
     * @param states The set of states.
     * @param transitions The set of transitions.
     * @param centreOfWindow The centre of the frame.
     * @param lastPastedLoc The last pasted location.
     * @param numTimesPastedToLastLoc The number of times an item has been pasted to the last pasted location.
     * @param panel The current graphics panel.
     */
    private void translateCentroidToMiddleOfWindow(Collection<TM_State> states,
            Collection<TM_Transition> transitions, Point2D centreOfWindow,
            Point2D lastPastedLoc, int numTimesPastedToLastLoc, TMGraphicsPanel panel)
    {
        if (states.size() == 0)
        {
            return;
        }
        Point2D centroid = computeCentroid(states);

        // TODO: Ensure that we dont go off the edge of the map.
        int rightMostX = Integer.MIN_VALUE;
        int leftMostX = Integer.MAX_VALUE;
        int bottomMostY = Integer.MIN_VALUE;
        int topMostY = Integer.MAX_VALUE;

        for (TM_State s : states)
        {
            if (s.getX() > rightMostX)
            {
                rightMostX = s.getX();
            }
            if (s.getY() > bottomMostY)
            {
                bottomMostY = s.getY();
            }
            if (s.getX() < leftMostX)
            {
                leftMostX = s.getX();
            }
            if (s.getY() < topMostY)
            {
                topMostY = s.getY();
            }
        }

        int translateVectorX = (int)(centreOfWindow.getX() - centroid.getX());
        int translateVectorY = (int)(centreOfWindow.getY() - centroid.getY());


        if (leftMostX + translateVectorX < 0)
        {
            translateVectorX -= leftMostX + translateVectorX;
        }

        if (topMostY + translateVectorY < 0)
        {
            translateVectorY -= topMostY + translateVectorY;
        }

        if (rightMostX + translateVectorX > MACHINE_CANVAS_SIZE_X - TM_State.STATE_RENDERING_WIDTH)
        {
            translateVectorX -= rightMostX + translateVectorX - (MACHINE_CANVAS_SIZE_X - TM_State.STATE_RENDERING_WIDTH);
        }

        if (bottomMostY + translateVectorY >  MACHINE_CANVAS_SIZE_Y - TM_State.STATE_RENDERING_WIDTH)
        {
            translateVectorY -= bottomMostY + translateVectorY - (MACHINE_CANVAS_SIZE_Y - TM_State.STATE_RENDERING_WIDTH);
        }

        if (lastPastedLoc != null && ((int)lastPastedLoc.getX() == (int)centreOfWindow.getX() + translateVectorX
                    &&(int)lastPastedLoc.getY() == (int)centreOfWindow.getY() + translateVectorY))
        {
            translateVectorX += TRANSLATE_TO_AVOID_STACKING_X * numTimesPastedToLastLoc;
            translateVectorY += TRANSLATE_TO_AVOID_STACKING_Y * numTimesPastedToLastLoc;
            panel.incrementNumPastesToSameLocation();
        }
        else
        {
            panel.setLastPastedLocation(new Point2D.Float((int)centreOfWindow.getX() + translateVectorX, 
                        (int)centreOfWindow.getY() + translateVectorY));
        }


        for (TM_State s : states)
        {
            int newX = (int)(s.getX() + translateVectorX);
            int newY = (int)(s.getY() + translateVectorY);
            s.setPosition(newX, newY);
        }

        for (TM_Transition t : transitions)
        {
            int newX = (int)(t.getControlPoint().getX() + translateVectorX);
            int newY = (int)(t.getControlPoint().getY() + translateVectorY);
            t.setControlPoint(newX, newY);
        }
    }
  
    /**
     * Computes the centroid (centre of mass) of the positions of a collection of states.
     * @param states The set of states.
     * @return The centroid of the states.
     */
    private static Point2D computeCentroid(Collection<TM_State> states)
    {
        float totalX = 0;
        float totalY = 0;

        for (TM_State s : states)
        {
            totalX += s.getX() + TM_State.STATE_RENDERING_WIDTH / 2; // Use middle of state
            totalY += s.getY() + TM_State.STATE_RENDERING_WIDTH / 2; // instead of top-left
        }
        return new Point2D.Float(totalX / states.size(), totalY / states.size());
    }
  
    /**
     * Update the undo/redo buttons with the new undo/redo command names.
     */
    public void updateUndoActions()
    {
        TMGraphicsPanel panel = getSelectedGraphicsPanel();
        if (panel != null && isEditingEnabled())
        {
            String undoCommandName = panel.undoCommandName();
            if (undoCommandName != null)
            {
                m_undoAction.putValue(Action.NAME, "Undo " + undoCommandName);
                m_undoAction.setEnabled(true);
            }
            else
            {
                m_undoAction.setEnabled(false);
                m_undoAction.putValue(Action.NAME, "Undo");
            }

            String redoCommandName = panel.redoCommandName();
            if (redoCommandName != null)
            {
                m_redoAction.putValue(Action.NAME, "Redo " + redoCommandName);
                m_redoAction.setEnabled(true);
            }
            else
            {
                m_redoAction.setEnabled(false);
                m_redoAction.putValue(Action.NAME, "Redo");
            }
        }
        else
        {
            m_undoAction.setEnabled(false);
            m_undoAction.putValue(Action.NAME, "Undo");
            m_redoAction.setEnabled(false);
            m_redoAction.putValue(Action.NAME, "Redo");
        }

        m_undoToolBarButton.setText("");
        m_redoToolBarButton.setText("");
    }
    
    /** 
     * Gets the graphics panel for the currently selected machine diagram window.
     * @return A reference to the currently selected graphics panel, or null if there is no such panel.
     */
    private TMGraphicsPanel getSelectedGraphicsPanel()
    {
        if (m_desktopPane == null)
        {
            return null;
        }
        JInternalFrame selected = m_desktopPane.getSelectedFrame();
        if (selected == null)
        {
            return null;
        }
        try
        {
            TMInternalFrame tmif = (TMInternalFrame)selected;
            return tmif.getGfxPanel();
        }
        catch (ClassCastException e)
        {
            // Wrong window type
            return null;
        }
    }
 
    /**
     * Action for creating a new machine in a new window
     */
    class NewMachineAction extends AbstractAction
    {
       /**
         * Creates a new instance of NewMachineAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public NewMachineAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Create a new machine, in a new frame.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (m_desktopPane != null)
            {
                JInternalFrame iFrame = newMachineWindow(new TMachine(), null);
                m_desktopPane.add(iFrame);
                try { iFrame.setSelected(true); }
                catch (PropertyVetoException e2) { }
            }
        }
    }
    
    /** 
     * Action for opening/loading a machine diagram.
     */
    class OpenMachineAction extends AbstractAction
    {
        /**
         * Creates a new instance of OpenMachineAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public OpenMachineAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Open a dialog to select a machine file, load the file, and display in new frame.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            fcMachine.setDialogTitle("Open machine");
            m_keyboardEnabled = false; // Disable keyboard input in the main window/tape.
            int returnVal = fcMachine.showOpenDialog(MainWindow.this);
            m_keyboardEnabled = true;
            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File inFile = fcMachine.getSelectedFile();
                if (!inFile.exists())
                {
                    // Try with extension
                    inFile = new File(inFile.toString() + MACHINE_EXTENSION);
                }
                if (!inFile.exists()) // Still no
                {
                    JOptionPane.showMessageDialog(MainWindow.this, "Cannot find file \"" + inFile.toString() + "\"");
                }
                else
                {
                    try
                    {
                        TMachine machine = TMachine.loadTMachine(inFile.toString());
                        if (machine == null)
                        {
                            throw new IOException(inFile.toString());
                        }
                        JInternalFrame iFrame = newMachineWindow(machine, inFile);
                        m_desktopPane.add(iFrame);
                        try
                        {
                            iFrame.setSelected(true);
                        }
                        catch (PropertyVetoException e2) {}
                    }
                    catch (Exception e2)
                    {
                        JOptionPane.showMessageDialog(MainWindow.this, "Error opening file \"" + inFile.toString() + "\"");
                    }
                }
            }
            else
            {
                System.err.println("Approve option didnt occur");
            }
        }
    }
       
    /**
     * Action for saving a machine diagram.
     */
    class SaveMachineAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveMachineAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public SaveMachineAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Save the machine to its associated file. If it does not have an associated file, display
         * a dialog to choose a file.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            try
            {
                TMGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    TMachine machine = panel.getSimulator().getMachine();
                    File outFile = panel.getFile();
                    if (machine != null)
                    {
                        if (outFile == null)
                        {
                            m_saveMachineAsAction.actionPerformed(e);
                        }
                        else
                        {
                            boolean result = TMachine.saveTMachine(machine, outFile.toString());
                            if (result = false)
                            {
                                throw new IOException(outFile.toString());
                            }
                            else
                            {
                                panel.setModifiedSinceSave(false);
                            }
                        }
                    }
                }
            } 
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(MainWindow.this, "An error occurred. Your file has not been saved!");
            }
        }
    }
 
    /**
     * Action for saving a machine diagram with a new name.
     */
    class SaveMachineAsAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveMachineAsAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public SaveMachineAsAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Open a dialog to select a machine file, and save the machine to the given filename.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            saveMachineAs(panel);
        }
    }
 
    /** 
     * Action for creating a new tape, which will be displayed in the tape display panel and used by
     * all machines.
     */
    class NewTapeAction extends AbstractAction
    {
        /**
         * Creates a new instance of NewTapeAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public NewTapeAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Display a confirmation dialog, then clear the tape.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            m_keyboardEnabled = false; // Disable keyboard input in the main window/tape.
            Object[] options = {"Ok", "Cancel"};
            int result = JOptionPane.showOptionDialog(null, "This will erase the tape.  Do you want to continue?", "Clear tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            m_keyboardEnabled = true;
            if (result == JOptionPane.YES_OPTION)
            {
                m_tape.copyOther(new CA_Tape());
                tapeDisp.repaint();
            }
        }
    }
    
    /**
     * Action for opening a tape.
     */
    class OpenTapeAction extends AbstractAction
    {
        /**
         * Creates a new instance of OpenTapeAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public OpenTapeAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Display a dialog, and load the selected tape file.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            fcTape.setDialogTitle("Open tape");
            m_keyboardEnabled = false; // Disable keyboard input in the main window/tape.
            int returnVal = fcTape.showOpenDialog(MainWindow.this);
            m_keyboardEnabled = true;
            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File inFile = fcTape.getSelectedFile();
                if (!inFile.exists())
                {
                    // Try with extension
                    inFile = new File(inFile.toString() + TAPE_EXTENSION);
                }
                if (!inFile.exists()) // Still no
                {
                    JOptionPane.showMessageDialog(MainWindow.this, "Cannot find file \"" + inFile.toString() + "\"");
                }
                else
                {
                    try
                    {
                        Tape tape = Tape.loadTape(inFile.toString());
                        if (tape == null)
                        {
                            throw new IOException(inFile.toString());
                        }
                        tapeDisp.getTape().copyOther(tape);
                        tapeDisp.setFile(inFile);
                        tapeDisp.repaint();
                    }
                    catch (Exception e2)
                    {
                        JOptionPane.showMessageDialog(MainWindow.this, "Error opening file \"" + inFile.toString() + "\"");
                        System.err.println("error opening file");
                    }
                }
            }
        }
    }
    
    /**
     * Action for saving a tape. 
     */
    class SaveTapeAsAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveTapeAsAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public SaveTapeAsAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Display a dialog, and save the tape to the specified file.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            fcTape.setDialogTitle("Save tape");
            m_keyboardEnabled = false; // Disable keyboard input in the main window/tape.
            int returnVal = fcTape.showSaveDialog(MainWindow.this);
            m_keyboardEnabled = true;
            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File outFile = fcTape.getSelectedFile();
                if (!outFile.exists())
                {
                    if (!outFile.toString().endsWith(TAPE_EXTENSION))
                    {
                        outFile = new File(outFile.toString() + TAPE_EXTENSION);
                    }
                }
                
                try
                {
                    boolean result = Tape.saveTape(tapeDisp.getTape(), outFile.toString());

                    if (result = false)
                    {
                        throw new IOException(outFile.toString());
                    }
                    tapeDisp.setFile(outFile);
                }
                catch (Exception e2)
                {
                    JOptionPane.showMessageDialog(MainWindow.this, "An error occurred.  Your file has not been saved!");
                }
            }
        }
    }
    
    /** 
     * Action for saving a tape.
     */
    class SaveTapeAction extends AbstractAction
    {
        /**
         * Creates a new instance of SaveTapeAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public SaveTapeAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Save the tape to its associated file. If it does not have an associated file, display a
         * dialog.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            try
            {
                Tape tape = tapeDisp.getTape();
                File outFile = tapeDisp.getFile();
                if (tape != null)
                {
                    if (outFile == null)
                    {
                        m_saveTapeAsAction.actionPerformed(e);
                    }
                    else
                    {
                        boolean result = Tape.saveTape(tape, outFile.toString());
                        if (result = false)
                            throw new IOException(outFile.toString());
                    }
                }
            } catch (Exception e2)
            {
                JOptionPane.showMessageDialog(MainWindow.this, "An error occurred.  Your file has not been saved!");
            }
        }
    }

    /**
     * Action for exiting the program.
     */
    class ExitAction extends AbstractAction
    {
        /**
         * Creates a new instance of ExitAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public ExitAction(String text, ImageIcon icon)
        {
            super(text);
            // putValue(ACCELERATOR_KEY, ...);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        /**
         * Prompt the user to exit the program.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            userRequestToExit();
        }
    }
        
    /**
     * Action for undoing a command.
     */
    class UndoAction extends AbstractAction
    {
        /**
         * Creates a new instance of UndoAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public UndoAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Undo the last action on the undo stack.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                panel.undoCommand();
                updateUndoActions();
                panel.repaint();
            }
        }
    }
    
    /**
     * Action for redoing a command.
     */
    class RedoAction extends AbstractAction
    {
        /**
         * Creates a new instance of RedoAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public RedoAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Redo the last command on the redo stack
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                panel.redoCommand();
                updateUndoActions();
                panel.repaint();
            }
        }
    }

    /**
     * Action for cutting states and transitions from a machine.
     */
    class CutSelectedAction extends AbstractAction
    {
        /**
         * Creates a new instance of CutSelectedAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public CutSelectedAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Cut the selected states and transitions from the machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            m_copyAction.actionPerformed(e);
            
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                HashSet<TM_State> selectedStatesCopy = (HashSet<TM_State>)panel.getSelectedStates().clone();
                HashSet<TM_Transition> selectedTransitionsCopy = (HashSet<TM_Transition>)panel.getSelectedTransitions().clone();
                panel.doCommand(new CutCommand(panel, selectedStatesCopy, selectedTransitionsCopy));
                updateUndoActions();
            }
        }
    }
    
    /**
     * Action for copying states and transitions from a machine.
     */
    class CopySelectedAction extends AbstractAction
    {
        /**
         * Creates a new instance of CopySelectedAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public CopySelectedAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Copy the selected states and transitions from a machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                copiedData = panel.copySelectedToByteArray();
            }
        }
    }
    
    
    /**
     * Action for pasting states and transitions into a machine.
     */
    class PasteAction extends AbstractAction
    {
        /**
         * Creates a new instance of PasteAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public PasteAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Paste previously cut/copied states and transitions into the machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            try
            {
                if (copiedData == null)
                {
                    // Abort
                    return;
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(copiedData);
                ObjectInputStream restore = new ObjectInputStream(bais);
                HashSet<TM_State> selectedStates = (HashSet<TM_State>)restore.readObject();
                HashSet<TM_Transition> selectedTransitions = (HashSet<TM_Transition>)restore.readObject();
                Point2D centroid = computeCentroid(selectedStates);
                TMInternalFrame tmif = (TMInternalFrame)m_desktopPane.getSelectedFrame();
                if (tmif == null)
                {
                    // Abort
                    return;
                }
                Point2D centreOfWindow = tmif.getCenterOfViewPort();
                translateCentroidToMiddleOfWindow(selectedStates, selectedTransitions, centreOfWindow,
                        tmif.getGfxPanel().getLastPastedLocation(), tmif.getGfxPanel().getNumPastesToSameLocation(),
                        tmif.getGfxPanel());
                TMGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    TMachine machine = panel.getSimulator().getMachine();
                    switch (panel.getSimulator().getMachine().getNamingScheme())
                    {
                        case GENERAL:
                            panel.doCommand(new PasteCommand(panel, selectedStates, selectedTransitions));
                            break;

                        case NORMALIZED:
                            panel.doJoinCommand(
                                new PasteCommand(panel, selectedStates, selectedTransitions),
                                new SchemeRelabelCommand(panel, NamingScheme.NORMALIZED));
                            break;
                    }
                    updateUndoActions();
                }
            }
            catch (Exception e2)
            {
                System.err.println("deserialization error!");
                e2.printStackTrace();
            }
        }
    }
    
    /**
     * Action for deleting selected states and transitions from a machine.
     */
    class DeleteSelectedAction extends AbstractAction
    {
        /**
         * Creates a new instance of DeleteSelectedAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public DeleteSelectedAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Delete the selected states and transitions from the machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {   
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                panel.deleteAllSelected();
            }
        }
    }
 
    /**
     * An action for selecting user interface interaction modes.
     * @author Jimmy
     */
    class GUI_ModeSelectionAction extends AbstractAction
    {
        /**
         * Creates a new instance of GUI_ModeSelectionAction.
         * @param text Description of the action.
         * @param mode Mode the action puts the GUI into.
         * @param icon Icon for the action.
         * @param keyShortcut Shortcut associated with the action.
         */
        public GUI_ModeSelectionAction(String text, TM_GUI_Mode mode, ImageIcon icon, KeyStroke keyShortcut)
        {
            super(text);
            m_mode = mode;
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
            putValue(ACCELERATOR_KEY, keyShortcut);
        }

        /**
         * Change the UI mode, and select the relevant menu item.
         */
        public void actionPerformed(ActionEvent e)
        {
            setUIMode(m_mode);
            if (m_menuItem != null)
            {
                m_menuItem.setSelected(true);
            }
        }
        
        /**
         * Set the associated menu item.
         * @param menuItem The menu item associated with this action.
         */
        public void setMenuItem(JRadioButtonMenuItem menuItem)
        {
            m_menuItem = menuItem;
        }

        /**
         * The GUI mode associated with this action.
         */
        private TM_GUI_Mode m_mode;

        /**
         * The menu item associated with this action.
         */
        private JRadioButtonMenuItem m_menuItem = null;
    }
   
    /** 
     * Action for stepping the selected machine one iteration.
     */
    class StepAction extends AbstractAction
    {
        /**
         * Creates a new instance of StepAction.
         * @param text Description of the action. 
         * @param parentComponent The owner component.
         * @param icon Icon for the action.
         */
        public StepAction(String text, Component parentComponent, ImageIcon icon)
        {
            super(text);
            m_parentComponent = parentComponent;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Step through one iteration of the machine simulation.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                TMGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
                if (gfxPanel != null)
                {
                    gfxPanel.getSimulator().step();
                    tapeDisp.repaint();
                }
            }
            catch (NondeterministicException e2)
            {
                JOptionPane.showMessageDialog(m_parentComponent, MainWindow.NONDET_ERR_STR + " " + e2.getMessage(), MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE); 
            }
            catch (UndefinedTransitionException e2)
            {
                JOptionPane.showMessageDialog(m_parentComponent,MainWindow.TRANS_UNDEF_ERR_STR + " " + e2.getMessage(), MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
            }
            catch (TapeBoundsException e2)
            {
                JOptionPane.showMessageDialog(m_parentComponent,MainWindow.TAPE_BOUNDS_ERR_STR, MainWindow.HALTED_MESSAGE_TITLE_STR,JOptionPane.WARNING_MESSAGE);
            }
            catch (ComputationCompletedException e2)
            {
                JOptionPane.showMessageDialog(m_parentComponent,MainWindow.COMPUTATION_COMPLETED_STR, MainWindow.HALTED_MESSAGE_TITLE_STR, JOptionPane.WARNING_MESSAGE);
                TMGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
                if (gfxPanel != null)
                {
                    gfxPanel.getSimulator().resetMachine();
                    gfxPanel.repaint();
                }
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(m_parentComponent,MainWindow.OTHER_ERROR_STR);
                e2.printStackTrace();
            }
            repaint();
        }

        /**
         * The owning component.
         */
        private Component m_parentComponent;
    }

    /**
     * Action for executing a machine.
     */
    class FastExecuteAction extends AbstractAction
    {
        /**
         * Creates a new instance of FastExecuteAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public FastExecuteAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Begin simulating the machine at the specified delay.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        { 
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                if (m_timerTask != null)
                {
                    m_timerTask.cancel();
                }
                setEditingEnabled(false);
                m_timerTask = new TMExecutionTimerTask(panel, tapeDisp, MainWindow.this);
                m_timer.scheduleAtFixedRate(m_timerTask, 0, m_executionDelayTime);
            }
        }
    }
    
    /**
     * Action for pausing execution of a machine.
     */
    class PauseExecutionAction extends AbstractAction
    {
        /**
         * Creates a new instance of PauseExecutionAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public PauseExecutionAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
       
        /**
         * Pause the execution of the machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            stopExecution();
            updateUndoActions();
        }
    }
    
    /** 
     * Action for resetting the selected machine.
     */
    class StopMachineAction extends AbstractAction
    {
        /**
         * Creates a new instance of StopMachineAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public StopMachineAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        
        /**
         * Stop the execution of the current machine, resetting all associated state.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            TMGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
            boolean wasRunning = stopExecution();
            if (gfxPanel != null)
            {
                // TODO: reset it even if not running
                if (m_timerTask == null || !wasRunning || gfxPanel == m_timerTask.getPanel())
                {
                    gfxPanel.getSimulator().resetMachine();
                    gfxPanel.repaint();
                }
            }
            updateUndoActions();
        }
    }

    /**
     * An action for selecting speeds for automatic execution of machines.
     * @author Jimmy
     */
    class ExecutionSpeedSelectionAction extends AbstractAction
    {
        /**
         * Creates a new instance of ExecutionSpeedSelectionAction.
         * @param text Description of the action.
         * @param delay The new execution delay for the machine.
         * @param keyShortcut Shortcut associated with the action.
        */
        public ExecutionSpeedSelectionAction(String text, int delay, KeyStroke keyShortcut)
        {
            super(text);
            putValue(Action.SHORT_DESCRIPTION, text);
            putValue(ACCELERATOR_KEY, keyShortcut);
            m_delay = delay;
        }

        /**
         * Change the execution delay of the machine.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
           m_executionDelayTime = m_delay;
        }
        
        /**
         * The delay for execution of the machine.
         */
        private int m_delay;
    }

    /**
     * Action for moving the read/write head to the start of the tape.
     */
    class HeadToStartAction extends AbstractAction
    {
        /**
         * Create a new instance of HeadToStartAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public HeadToStartAction(String text, Icon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, "Move the read/write head to the start of the tape.");
        }

        /**
         * Move the read/write head to the leftmost end of the tape.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e) 
        {
            // Move r/w head to the left end of the tape
            tapeDisp.getTape().resetRWHead();
            tapeDispController.repaint();
        }
    }

    /**
     * Action for reloading the tape.
     */
    class ReloadTapeAction extends AbstractAction
    {
        /**
         * Create a new instance of HeadToStartAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public ReloadTapeAction(String text, Icon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, "Reload the tape from disk, discarding any changes since the last save.");
        }

        public void actionPerformed(ActionEvent e) 
        {
            // Wipe the tape.
            Object[] options = {"Ok", "Cancel"};
            // TODO: should disable keyboard here
            // TODO: BUG, CANCELLATION STILL CLEARS TAPE
            int result = 0;
            if (tapeDisp.getFile() == null)
            {
                JOptionPane.showOptionDialog(null, "This will erase the tape.  Do you want to continue?", "Reload tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            }
            else
            {
                JOptionPane.showOptionDialog(null, "This will reload the tape, discarding any changes.  Do you want to continue?", "Reload tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            }
            if (result == JOptionPane.YES_OPTION)
            {
                tapeDisp.reloadTape();
                tapeDispController.repaint();
            }
        }
    }
    
    /**
     * Action for erasing the tape.
     */
    class EraseTapeAction extends AbstractAction
    {
        /**
         * Create a new instance of EraseTapeAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public EraseTapeAction(String text, Icon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, "Erase the tape.");
        }

        /**
         * Erase the tape.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e) 
        {
            // Wipe the tape.
            Object[] options = {"Ok", "Cancel"};
            // TODO: should disable keyboard here
            int result = JOptionPane.showOptionDialog(null, "This will erase the tape.  Do you want to continue?", "Clear tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (result == JOptionPane.YES_OPTION)
            {
                tapeDisp.getTape().clearTape();
                tapeDispController.repaint();
            }
        }
    }

    /**
     * Action for configuring the current alphabet.
     */
    class ConfigureAlphabetAction extends AbstractAction
    {
        /**
         * Creates a new instance of ConfigureAlphabetAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public ConfigureAlphabetAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                m_asif.setPanel(panel);
                m_asif.show();
                getGlassPane().setVisible(true);
            }
        }
    }

    /**
     * Action for configuring the current naming scheme.
     */
    class ConfigureSchemeAction extends AbstractAction
    {
        /**
         * Creates a new instance of ConfigureSchemeAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public ConfigureSchemeAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e)
        {
            TMGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel != null)
            {
                m_ssif.setPanel(panel);
                m_ssif.show();
                getGlassPane().setVisible(true);
            }
        }
    }
  
    /**
     * Action for displaying the shared console.
     */
    class ShowConsoleAction extends AbstractAction
    {
        /**
         * Creates a new instance of ShowConsoleAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public ShowConsoleAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        /**
         * Display the console.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (m_console == null)
            {
                m_console = new ConsoleInternalFrame();
                m_console.setLayer(60); // !!!
            }
            if (!m_console.isVisible())
            {
                m_desktopPane.add(m_console);
                m_console.setVisible(true);
            }
            m_console.moveToFront();
            try { m_console.setSelected(true); }
            catch (PropertyVetoException e2) { }
        }
    }

    /**
     * Action for displaying help contents.
     */
    class HelpAction extends AbstractAction
    {
        /**
         * Creates a new instance of HelpAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */
        public HelpAction(String text, ImageIcon icon)
        {
            super(text);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        /**
         * Display a new frame which renders help documentation.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (m_helpDisp == null)
            {
                m_helpDisp = new TMHelpDisplayer();
                m_helpDisp.setLayer(60);
            }
            if (!m_helpDisp.isVisible())
            {
                m_desktopPane.add(m_helpDisp);
                m_helpDisp.setVisible(true);
            }
            m_helpDisp.moveToFront();
            try { m_helpDisp.setSelected(true); }
            catch (PropertyVetoException e2) { }
        }
    }

    /**
     * Action for displaying the about contents of the program.
     */
    class AboutAction extends AbstractAction
    {
        /**
         * Creates a new instances of AboutAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         */ 
        public AboutAction(String text, ImageIcon icon)
        {
            super(text);
            // putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_???, KeyEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        /**
         * Display a message box specifying some meta information about the program.
         */
        public void actionPerformed(ActionEvent e)
        {
            JOptionPane.showMessageDialog(MainWindow.this,
                    "Tuatara Turing Machine Simulator 1.0 was written by Jimmy Foulds in 2006-2007, " + 
                    "and extended by Mitchell Grout in 2017-2018, with funding from the " +
                    "Department of Mathematics at the University of Waikato, New Zealand.");
        }
    }
    
    /** 
     * This class is designed to intercept mouse events in order to make a window modal.
     * It is borrowed from the Sun developer tech tips article at
     * http://java.sun.com/developer/JDCTechTips/2001/tt1220.html .
     */
    class ModalAdapter extends InternalFrameAdapter
    {
        /**
         * Create an instance of ModalAdapter.
         * @param glass The underlying component.
         */
        public ModalAdapter(Component glass)
        {
            this.glass = glass;

            // Associate dummy mouse listeners
            // Otherwise mouse events pass through
            MouseInputAdapter adapter = new MouseInputAdapter() { };
            glass.addMouseListener(adapter);
            glass.addMouseMotionListener(adapter);
        }

        /**
         * Set the underlying component to be invisible when this closes.
         * @param e The generating event.
         */
        public void internalFrameClosed(InternalFrameEvent e)
        {
            glass.setVisible(false);
        }
 
        /**
         * Underlying component
         */
        private Component glass;
    }
    
    /**
     * Current GUI mode.
     */
    private TM_GUI_Mode m_currentMode;
    
    /**
     * Whether the keyboard is currently enabled.
     */
    private boolean m_keyboardEnabled = true;

    /**
     * Whether editing is currently enabled.
     */
    private boolean m_editingEnabled = true;

    /**
     * Dialog for choosing a file, specifically for machines.
     */
    private final JFileChooser fcMachine = new JFileChooser();

    /**
     * Dialog for choosing a file, specifically for tapes.
     */
    private final JFileChooser fcTape = new JFileChooser();

    /**
     * Timer task used for stepping through a machine on a delay.
     */
    private TMExecutionTimerTask m_timerTask;
    
    /**
     * Simulation delay associated with the machine, used by m_timerTask.
     */
    private int m_executionDelayTime;

    /**
     * Data which has been copied, used for pasting.
     */
    private byte[] copiedData = null;

    /**
     * X ordinate of the last new frame.
     */
    private int lastNewWindowLocX = 0;

    /**
     * Y ordinate of the last new frame.
     */
    private int lastNewWindowLocY = 0;

    /**
     * Distance between new frames.
     */
    private int windowLocStepSize = minDistanceForNewWindowLoc;
    
    /**
     * List of buttons which have an associated GUI mode and action.
     */
    private ArrayList<GUIModeButton> m_toolbarButtons;

    /**
     * Tape display panel.
     */
    private TMTapeDisplayPanel tapeDisp;

    /**
     * Tape controller.
     */
    private TMTapeDisplayControllerPanel tapeDispController;

    /**
     * Main shared tape.
     */
    private final Tape m_tape = new CA_Tape();

    /**
     * List of copied states.
     */ 
    private ArrayList<TM_State> copiedStates;

    /**
     * List of copied transitions.
     */
    private ArrayList<TM_Transition> copiedTransitions;
      
    /**
     * Toolbar button for undoing an action.
     */
    private JButton m_undoToolBarButton;

    /**
     * Toolbar button for redoing an action.
     */
    private JButton m_redoToolBarButton;

    /**
     * Desktop pane for the window, containing all frames.
     */
    private JDesktopPane m_desktopPane;

    /**
     * Frame for selecting the current alphabet.
     */
    private AlphabetSelectorInternalFrame m_asif;

    /**
     * Frame for selecting the current naming scheme.
     */
    private SchemeSelectorInternalFrame m_ssif;

    /**
     * Frame for displaying help information as HTML.
     */
    private TMHelpDisplayer m_helpDisp;

    /**
     * Frame for displaying a console window for logging information.
     */
    private ConsoleInternalFrame m_console;

    /**
     * Action for creating a new machine.
     */
    private final Action m_newMachineAction = new NewMachineAction("New Machine", loadIcon("newMachine.gif"));

    /**
     * Action for opening a machine.
     */
    private final Action m_openMachineAction = new OpenMachineAction("Open Machine", loadIcon("openMachine.gif"));;

    /**
     * Action for saving a machine to an associated file.
     */
    private final Action m_saveMachineAction = new SaveMachineAction("Save Machine", loadIcon("saveMachine.gif"));

    /**
     * Action for saving a machine to a selected file.
     */
    private final Action m_saveMachineAsAction = new SaveMachineAsAction("Save Machine As", loadIcon("emptyIcon.gif"));

    /**
     * Action for creating a new tape.
     */
    private final Action m_newTapeAction = new NewTapeAction("New Tape", loadIcon("newTape.gif"));

    /**
     * Action for opening a tape.
     */
    private final Action m_openTapeAction = new OpenTapeAction("Open Tape", loadIcon("openTape.gif"));

    /**
     * Action for saving a tape to an associated file.
     */
    private final Action m_saveTapeAction = new SaveTapeAction("Save Tape", loadIcon("saveTape.gif"));
    
    /**
     * Action for saving a tape to a selected file.
     */
    private final Action m_saveTapeAsAction = new SaveTapeAsAction("Save Tape As", loadIcon("emptyIcon.gif"));

    /**
     * Action for exiting the program.
     */
    private final Action m_exitAction = new ExitAction("Exit", loadIcon("emptyIcon.gif"));

    /**
     * Action for undoing a command.
     */
    private final Action m_undoAction = new UndoAction("Undo", loadIcon("undoIcon.gif"));

    /**
     * Action for redoing a command
     */
    private final Action m_redoAction = new RedoAction("Redo", loadIcon("redoIcon.gif"));

    /**
     * Action for cutting selected states and transitions.
     */
    private final Action m_cutAction = new CutSelectedAction("Cut", loadIcon("cut.gif"));

    /**
     * Action for copying selected states and transitions.
     */
    private final Action m_copyAction = new CopySelectedAction("Copy", loadIcon("copy.gif"));

    /**
     * Action for pasting selected states and transitions.
     */
    private final Action m_pasteAction = new PasteAction("Paste", loadIcon("paste.gif"));

    /**
     * Action for deleting selected states and transitions.
     */
    private final Action m_deleteAction = new DeleteSelectedAction("Delete Selected Items", loadIcon("delete.gif"));

    /**
     * Action associated with ADDNODES.
     */
    private final GUI_ModeSelectionAction m_addNodesAction = new GUI_ModeSelectionAction("Add States", TM_GUI_Mode.ADDNODES,
            loadIcon("state.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));

    /**
     * Action associated with ADDTRANSITIONS.
     */
    private final GUI_ModeSelectionAction m_addTransitionsAction = new GUI_ModeSelectionAction("Add Transitions", TM_GUI_Mode.ADDTRANSITIONS,
            loadIcon("transition.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));

    /**
     * Action associated with SELECTION.
     */
    private final GUI_ModeSelectionAction m_selectionAction = new GUI_ModeSelectionAction("Make Selection", TM_GUI_Mode.SELECTION,
            loadIcon("selection.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F4,0));

    /**
     * Action associated with ERASER.
     */
    private final GUI_ModeSelectionAction m_eraserAction = new GUI_ModeSelectionAction("Eraser", TM_GUI_Mode.ERASER, 
            loadIcon("eraser.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

    /**
     * Action associated with CHOOSESTART.
     */
    private final GUI_ModeSelectionAction m_chooseStartAction = new GUI_ModeSelectionAction("Choose Start State", TM_GUI_Mode.CHOOSESTART, 
            loadIcon("startState.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));

    /**
     * Action associated with CHOOSEACCEPTING.
     */
    private final GUI_ModeSelectionAction m_chooseAcceptingAction = new GUI_ModeSelectionAction("Choose Accepting State", TM_GUI_Mode.CHOOSEACCEPTING,
            loadIcon("finalState.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F7,0));

    /**
     * Action associated with CHOOSECURRENTSTATE.
     */
    private final GUI_ModeSelectionAction m_chooseCurrentStateAction = 
        new GUI_ModeSelectionAction("Choose Current State", TM_GUI_Mode.CHOOSECURRENTSTATE,
                loadIcon("currentState.gif"), KeyStroke.getKeyStroke(KeyEvent.VK_F8,0));

    /**
     * Action for stepping through execution.
     */
    private final Action m_stepAction = new StepAction("Step", this, loadIcon("step.gif"));

    /**
     * Action for starting simulation of the machine.
     */
    private final Action m_fastExecuteAction = new FastExecuteAction("Execute", loadIcon("fastExecute.gif"));

    /**
     * Action for pausing simulation of the machine.
     */
    private final Action m_pauseExecutionAction = new PauseExecutionAction("Pause Execution", loadIcon("pause.gif"));

    /**
     * Action for stopping a simulation.
     */
    private final Action m_stopMachineAction = new StopMachineAction("Stop Execution", loadIcon("stop.gif"));

    /**
     * Action to set execution speed to slow.
     */
    private final ExecutionSpeedSelectionAction m_slowExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Slow", SLOW_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to medium.
     */
    private final ExecutionSpeedSelectionAction m_mediumExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Medium", MEDIUM_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to fast.
     */
    private final ExecutionSpeedSelectionAction m_fastExecuteSpeedAction =
        new ExecutionSpeedSelectionAction("Fast", FAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to superfast.
     */
    private final ExecutionSpeedSelectionAction m_superFastExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Super Fast", SUPERFAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to ultrafast.
     */
    private final ExecutionSpeedSelectionAction m_ultraFastExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Ultra Fast", ULTRAFAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK)); 

    /**
     * Action for moving the read/write head to the start of the tape.
     */
    private final HeadToStartAction m_headToStartAction = new HeadToStartAction("Move Read/Write Head to Start of Tape", loadIcon("tapeStart.gif"));

    /**
     * Action for reloading the tape.
     */
    private final ReloadTapeAction m_reloadTapeAction = new ReloadTapeAction("Reload Tape", loadIcon("tapeReload.gif"));

    /**
     * Action for erasing the tape.
     */
    private final EraseTapeAction m_eraseTapeAction = new EraseTapeAction("Erase Tape", loadIcon("tapeClear.gif"));

    /**
     * Action for configuring the alphabet.
     */
    private final Action m_configureAlphabetAction = new ConfigureAlphabetAction("Configure Alphabet", loadIcon("configureAlphabet.gif")); 

    /**
     * Action for configuring the naming scheme.
     */
    private final Action m_configureSchemeAction = new ConfigureSchemeAction("Configure Naming Scheme", loadIcon("scheme.gif"));

    /**
     * Action for displaying the shared console.
     */
    private final Action m_showConsoleAction = new ShowConsoleAction("Show Console", loadIcon("emptyIcon.gif"));

    /**
     * Action for displaying help documentation.
     */
    private final Action m_helpAction = new HelpAction("Help", loadIcon("tuatara.gif"));

    /**
     * Action for displaying meta information about the program.
     */
    private final Action m_aboutAction = new AboutAction("About", loadIcon("emptyIcon.gif"));
}
