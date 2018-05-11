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
import tuataraTMSim.machine.*;
import tuataraTMSim.machine.TM.*;
import tuataraTMSim.machine.DFSA.*;

/**
 * The main window of the program. An MDI interface for building and running turing machines.
 * This class is the main entry point into the program.
 */
public class MainWindow extends JFrame
{ 
    /**
     * The layer used for internal frames containing machines.
     */
    protected static final int MACHINE_WINDOW_LAYER = 50;
       
    /**
     * String for execution halting.
     */
    protected static final String HALTED_MESSAGE_TITLE_STR  = "Machine halted!";

    /**
     * Delay between steps for slow execution speed.
     */
    protected static final int SLOW_EXECUTE_SPEED_DELAY = 1200;

    /**
     * Delay between steps for medium execution speed.
     */
    protected static final int MEDIUM_EXECUTE_SPEED_DELAY = 800;

    /**
     * Delay between steps for fast execution speed.
     */
    protected static final int FAST_EXECUTE_SPEED_DELAY = 400;

    /**
     * Delay between steps for superfast execution speed.
     */
    protected static final int SUPERFAST_EXECUTE_SPEED_DELAY = 200;

    /**
     * Delay between steps for ultrafast execution speed.
     */
    protected static final int ULTRAFAST_EXECUTE_SPEED_DELAY = 10;
    
    /**
     * Width of the machine canvas.
     */
    protected static final int MACHINE_CANVAS_SIZE_X = 2000;

    /**
     * Height of the machine canvas.
     */
    protected static final int MACHINE_CANVAS_SIZE_Y = 2000;
   
    /**
     * Horizontal translation of states to avoid stacking.
     */
    protected static final int TRANSLATE_TO_AVOID_STACKING_X = State.STATE_RENDERING_WIDTH * 2;

    /**
     * Vertical translation of states to avoid stacking.
     */
    protected static final int TRANSLATE_TO_AVOID_STACKING_Y = State.STATE_RENDERING_WIDTH * 2;
    
    /**
     * Maximum horizontal ratio for new window location.
     */
    protected static final double maxHorizontalRatioForNewWindowLoc = 0.3;

    /**
     * Maximum vertical ratio for new window location.
     */
    protected static final double maxVerticalRatioForNewWindowLoc = 0.3;

    /**
     * Minimum distance between two new windows.
     */
    protected static int minDistanceForNewWindowLoc = 50;

    /**
     * Random step between distances between new windows.
     * Considered for removal.
     */
    protected static int windowLocRandomStepSize = 10;
    
    /**
     * Random number generator used for state and transition placement.
     * Considered for removal.
     */
    protected Random myRandom = new Random();
    
    /**
     * Creates a new instance of MainWindow.
     */
    public MainWindow()
    {
        m_instance = this;
        initComponents();
       
        // Whenever a mouse click occurs, deselect the selected action. If the action was clicked on
        // again, it will regain focus.
        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                MachineGraphicsPanel gfx = getSelectedGraphicsPanel();
                if (gfx != null)
                {
                    gfx.deselectSymbol();
                }
            }
        });

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
               MachineGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
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
                       {
                           gfxPanel.repaint();
                       }
                       else if (m_tapeDisp != null)
                       {
                           m_tapeDisp.handleKeyEvent(e);
                       }
                   }
                   // No graphics panel, just a tape
                   else if (m_tapeDisp != null) 
                   {
                       m_tapeDisp.handleKeyEvent(e);
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
        // Choose the look-and-feel for the program before running everything
        try
        {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e)
        {
            // Unable to change look-and-feel; ignore
        }

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                (new MainWindow()).setVisible(true);
            }
        });
    }

    /**
     * Get the desktop pane in use by the window.
     * @return The desktop pane in use.
     */
    public JDesktopPane getDesktopPane()
    {
        return m_desktopPane;
    }

    /**
     * Get the current console.
     * @return The current console.
     */
    public ConsolePanel getConsole()
    {
        return m_console;
    }

    /** 
     * Gets the graphics panel for the currently selected machine diagram window.
     * @return A reference to the currently selected graphics panel, or null if there is no such panel.
     */
    public MachineGraphicsPanel getSelectedGraphicsPanel()
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
            MachineInternalFrame tmif = (MachineInternalFrame)selected;
            return tmif.getGfxPanel();
        }
        catch (ClassCastException e)
        {
            // Wrong window type
            return null;
        }
    }

    /**
     * Get the tape currently in use.
     * @return The tape.
     */
    public Tape getTape()
    {
        return m_tape;
    }

    /**
     * Get a reference to the current instance of MainWindow
     * @return A reference to the current instance of MainWindow
     */
    public static MainWindow getInstance()
    {
        return m_instance;
    }

    /** 
     * Selects the user interface interaction mode and notifies all internal windows accordingly.
     * This determines the results of user interactions such as clicking on the state diagrams.
     * @param mode The new GUI mode.
     */
    public void setUIMode(GUI_Mode mode)
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
                MachineInternalFrame tmif = (MachineInternalFrame)frame;
                MachineGraphicsPanel panel = (MachineGraphicsPanel)tmif.getGfxPanel();
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
        setMinimumSize(new Dimension(640, 480));
        setTitle("Tuatara Turing Machine Simulator");
        setIconImage(Global.loadIcon("tuatara.png").getImage());

        // Omnibus will be the panel which contains everything barring the toolbar
        JPanel omnibus = new JPanel();
 
        // Desktop pane holds all internal frames
        m_desktopPane = new JDesktopPane();
        m_desktopPane.setMinimumSize(new Dimension(200, 400));
        m_desktopPane.setPreferredSize(new Dimension(1600, 1200));
        m_desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Console is the global point for logging
        m_console = new ConsolePanel();
        m_console.setMinimumSize(new Dimension(200, 100));
        m_console.setPreferredSize(new Dimension(200, 100));
        
        // The desktop pane and console go together in a horizontal-split pane so the console may be
        // resized veritcally only. Additionally requires that components be continuously redrawn
        // when resized. setMinimumSize(0,0) ensures that the tape display is always shown.
        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_desktopPane, m_console);
        mainPane.setMinimumSize(new Dimension(0,0));
        mainPane.setOneTouchExpandable(true);
        mainPane.setResizeWeight(0.9D);
        
        // Set up the tape and associated controllers
        m_tape = new CA_Tape();
        m_tapeDisp = new TapeDisplayPanel(m_tape);
        m_tapeDispController = 
            new TapeDisplayControllerPanel(m_tapeDisp, m_headToStartAction, m_eraseTapeAction, m_reloadTapeAction); 
        m_tapeDispController.setBounds(0, getHeight() - m_tapeDispController.getHeight(), getWidth(),100); 
        m_tapeDispController.setVisible(true);
        
        // Set up the file choosers
        m_fcMachine.setDialogTitle("Save machine");
        m_fcMachine.addChoosableFileFilter(TMGraphicsPanel.FILE_FILTER);
        m_fcMachine.addChoosableFileFilter(DFSAGraphicsPanel.FILE_FILTER);
       
        m_fcTape.setDialogTitle("Save tape");
        m_fcTape.addChoosableFileFilter(Tape.FILE_FILTER);
        
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
        omnibus.add(mainPane, BorderLayout.CENTER);
        omnibus.add(m_tapeDispController, java.awt.BorderLayout.SOUTH);
        getContentPane().add(omnibus);

        // Maximize on startup
        this.setExtendedState(Frame.MAXIMIZED_BOTH);

        // Create the alphabet configuration internal frame
        m_asif = new AlphabetSelectorInternalFrame();
        m_asif.pack();

        // Make the internal frames quasi-modal.
        JPanel glass = new JPanel();
        glass.setOpaque(false);
        glass.add(m_asif);
        setGlassPane(glass);
        
        // The ModalAdapter intercepts all mouse events when the glass pane is visible.
        ModalAdapter adapter = new ModalAdapter(glass);
        m_asif.addInternalFrameListener(adapter);

        // Disable all toolbars (no default machine)
        setEnabledActionsThatRequireAMachine(false); 
        
        setVisible(true);
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
      
        JMenu newSubmenu = new JMenu("New Machine");
        newSubmenu.setIcon(Global.loadIcon("newMachine.png"));
        newSubmenu.setMnemonic(KeyEvent.VK_N);
        newSubmenu.add(new JMenuItem(m_newTuringMachineAction));
        newSubmenu.add(new JMenuItem(m_newDFSAAction));
        fileMenu.add(newSubmenu);

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
        
        JRadioButtonMenuItem m_chooseFinalMenuItem = new JRadioButtonMenuItem(m_chooseFinalAction);
        m_chooseFinalAction.setMenuItem(m_chooseFinalMenuItem);
        modeMenu.add(m_chooseFinalMenuItem);
        modeMenuItems.add(m_chooseFinalMenuItem);
        
        JRadioButtonMenuItem m_chooseCurrentStateMenuItem = new JRadioButtonMenuItem(m_chooseCurrentStateAction);
        m_chooseCurrentStateAction.setMenuItem(m_chooseCurrentStateMenuItem);
        modeMenu.add(m_chooseCurrentStateMenuItem);
        modeMenuItems.add(m_chooseCurrentStateMenuItem);
 
        m_addNodesMenuItem.setSelected(true);
        

        // Machine menu
        JMenu machineMenu = new JMenu("Machine");
        machineMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(machineMenu);
       
        machineMenu.add(new JMenuItem(m_validateAction));
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
        
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

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
        
        // Machine
        // SPECIAL: newMachine causes a JPopupMenu to show, which contains all new***MachineAction's
        JButton newMachineToolBarButton = new JButton(Global.loadIcon("newMachine.png"));
        JPopupMenu machineMenu = new JPopupMenu();
        machineMenu.add(m_newTuringMachineAction);
        machineMenu.add(m_newDFSAAction);
        newMachineToolBarButton.addMouseListener(new MouseAdapter()
        {
            // Show the popup menu when clicked
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    machineMenu.show(e.getComponent(), 
                            newMachineToolBarButton.getX() - newMachineToolBarButton.getWidth() / 2, 
                            newMachineToolBarButton.getY() + newMachineToolBarButton.getHeight());
                }
            }
        });
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
       
        // GUI mode
        GUIModeButton addNodesToolBarButton = new GUIModeButton(m_addNodesAction, GUI_Mode.ADDNODES);
        m_toolbarButtons.add(addNodesToolBarButton);
        
        GUIModeButton addTransitionsToolBarButton = new GUIModeButton(m_addTransitionsAction, GUI_Mode.ADDTRANSITIONS);
        m_toolbarButtons.add(addTransitionsToolBarButton);
        
        GUIModeButton selectionToolBarButton = new GUIModeButton(m_selectionAction,GUI_Mode.SELECTION);
        m_toolbarButtons.add(selectionToolBarButton);
        
        GUIModeButton eraserToolBarButton = new GUIModeButton(m_eraserAction, GUI_Mode.ERASER);
        m_toolbarButtons.add(eraserToolBarButton);
        
        GUIModeButton startStatesToolBarButton = new GUIModeButton(m_chooseStartAction, GUI_Mode.CHOOSESTART);
        m_toolbarButtons.add(startStatesToolBarButton);
        
        GUIModeButton finalStatesToolBarButton = new GUIModeButton(m_chooseFinalAction, GUI_Mode.CHOOSEFINAL);
        m_toolbarButtons.add(finalStatesToolBarButton);
        
        GUIModeButton chooseCurrentStateToolBarButton = new GUIModeButton(m_chooseCurrentStateAction, GUI_Mode.CHOOSECURRENTSTATE);
        m_toolbarButtons.add(chooseCurrentStateToolBarButton);
 
        // Machine
        JButton validateToolBarButton = new JButton(m_validateAction);
        validateToolBarButton.setFocusable(false);
        validateToolBarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        validateToolBarButton.setText("");

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
        returner[0].add(newMachineToolBarButton);
        returner[0].add(openMachineToolBarButton);
        returner[0].add(saveMachineToolBarButton);
        returner[0].add(newTapeToolBarButton);
        returner[0].add(openTapeToolBarButton);
        returner[0].add(saveTapeToolBarButton);
        returner[0].add(cutToolBarButton);
        returner[0].add(copyToolBarButton);
        returner[0].add(pasteToolBarButton);
        returner[0].add(deleteToolBarButton);
        returner[0].add(m_undoToolBarButton);
        returner[0].add(m_redoToolBarButton);
        returner[0].add(configureAlphabetToolBarButton);

        returner[1] = new JToolBar("Mode");
        returner[1].setRollover(true);
        returner[1].add(addNodesToolBarButton);
        returner[1].add(addTransitionsToolBarButton);
        returner[1].add(selectionToolBarButton);
        returner[1].add(eraserToolBarButton);
        returner[1].add(startStatesToolBarButton);
        returner[1].add(finalStatesToolBarButton);
        returner[1].add(chooseCurrentStateToolBarButton);
        
        returner[2] = new JToolBar("Machine");
        returner[2].setRollover(true);
        returner[2].add(validateToolBarButton);
        returner[2].add(stepToolBarButton);
        returner[2].add(fastExecute);
        returner[2].add(stopExecutionToolBarButton);
        returner[2].add(resetMachineToolBarButton);
       
        // Default mode
        setUIMode(GUI_Mode.ADDNODES);
        
        return returner;
    }
   
    /**
     * Creates a new window displaying a machine.
     * @param gfxPanel The underlying graphics panel.
     * @return A frame containing the graphics panel.
     */
    public MachineInternalFrame newMachineWindow(MachineGraphicsPanel gfxPanel)
    {
        gfxPanel.setUIMode(m_currentMode);
        final MachineInternalFrame returner = new MachineInternalFrame(gfxPanel, ++m_windowCount);
        gfxPanel.setFrame(returner);
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
     * Add an internal frame to the window. 
     * @param frame The frame to add.
     */
    public void addFrame(JInternalFrame frame)
    {
        // Unselect any existing frame
        JInternalFrame currentFrame = m_desktopPane.getSelectedFrame();
        if (currentFrame != null)
        {
            try { currentFrame.setSelected(false); }
            catch (PropertyVetoException e) { }
        }

        // Add and select
        m_desktopPane.add(frame);
        m_desktopPane.setSelectedFrame(frame);
        m_desktopPane.getDesktopManager().activateFrame(frame);
        frame.setVisible(true);
        try { frame.setSelected(true); }
        catch (PropertyVetoException e) { }

    }
    
    /**
     * Remove an internal frame from the window.
     * @param frame The frame to remove.
     */
    public void removeFrame(MachineInternalFrame frame)
    {
        frame.dispose();
        // Is this necessary?
        m_desktopPane.remove(frame);
    }
   
    /**
     * Compute the next location to place a new window.
     * @return The next location to place a new window.
     */
    private Point2D.Float nextWindowLocation()
    {
        int x = m_lastNewWindowLocX + m_windowLocStepSize;
        int y = m_lastNewWindowLocY + m_windowLocStepSize;
        
        if (x > maxHorizontalRatioForNewWindowLoc * this.getWidth())
        {
            x %= maxHorizontalRatioForNewWindowLoc * this.getWidth();
            y = x;
            
            m_windowLocStepSize = minDistanceForNewWindowLoc + myRandom.nextInt(3)
                * windowLocRandomStepSize;
        }
        
        if (y > maxHorizontalRatioForNewWindowLoc * this.getWidth())
        {
            y %= maxHorizontalRatioForNewWindowLoc * this.getWidth();
            x = y;
            
            m_windowLocStepSize = minDistanceForNewWindowLoc + myRandom.nextInt(3)
                * windowLocRandomStepSize;
        }
        m_lastNewWindowLocX = x;
        m_lastNewWindowLocY = y;
        
        return new Point2D.Float((float)x, (float)y);
    }
    
    /** 
     * Ask the user to confirm whether they wish to save a modified machine. If they agree,
     * correctly handle the saving. Afterwards, close the window associated with the machine.
     * @param iFrame The frame being closed.
     * @return false if the user cancelled, true otherwise.
     */
    private boolean userConfirmSaveModifiedThenClose(MachineInternalFrame iFrame)
    {
        MachineGraphicsPanel gfxPanel = iFrame.getGfxPanel();
        iFrame.moveToFront();
        if (gfxPanel.isModifiedSinceSave())
        {
            // Get the title of the frame, including the machine type
            String name = iFrame.getTitle();
            if (name.startsWith("* "))
            {
                name = name.substring(2);
            }
            
            int result = JOptionPane.showConfirmDialog(null, 
                    String.format("The machine '%s' is unsaved. Do you wish to save it?", name),
                    "Closing window", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                Machine machine = gfxPanel.getSimulator().getMachine();
                File outFile = gfxPanel.getFile();
                boolean saveSuccessful = false;
                if (outFile == null)
                {
                    outFile = chooseSaveFile(m_fcMachine, "Save Machine", gfxPanel.getMachineExt());
                    if (outFile == null)
                    {
                        // Cancelled by user
                        return false;
                    }
                }

                try
                {
                    Machine.saveMachine(machine, outFile);
                    iFrame.dispose();
                    return true;
                }
                catch (Exception e)
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
            m_validateAction.setEnabled(isEnabled);
            m_stepAction.setEnabled(isEnabled);
            m_configureAlphabetAction.setEnabled(isEnabled);
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
            m_chooseFinalAction.setEnabled(isEnabled);
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
    public void setEditingActionsEnabledState(boolean isEnabled)
    {
        m_validateAction.setEnabled(isEnabled);
        m_stepAction.setEnabled(isEnabled);
        m_configureAlphabetAction.setEnabled(isEnabled);
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
        m_chooseFinalAction.setEnabled(isEnabled);
        m_chooseCurrentStateAction.setEnabled(isEnabled);
        
        m_newTuringMachineAction.setEnabled(isEnabled);
        m_newDFSAAction.setEnabled(isEnabled);
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
                MachineInternalFrame iFrame = (MachineInternalFrame)frame;
                MachineGraphicsPanel panel = iFrame.getGfxPanel();
                if (panel != null)
                {
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
     * A general function used for displaying save file dialogs. This keeps all behaviours for file
     * choosing consistent across types.
     * @param fc The file chooser to be used.
     * @param title The title of the dialog.
     * @param ext The file extension.
     * @return The chosen file if one was picked, otherwise null if the user cancelled.
     */
    public File chooseSaveFile(JFileChooser fc, String title, String ext)
    {
        do
        {
            // Prevent the program from reading from the keyboard while the file dialog is active
            m_keyboardEnabled = false;
            fc.setDialogTitle(title);
            int returnVal = fc.showSaveDialog(MainWindow.this);
            m_keyboardEnabled = true;
            if (returnVal != JFileChooser.APPROVE_OPTION)
            {
                return null;
            }
            File outfile = fc.getSelectedFile();
            // If it's a new file, and doesn't have our extension, append the extension
            if (!outfile.exists() && !outfile.toString().endsWith(ext))
            {
                outfile = new File(outfile.toString() + ext);
            }
            // If it exists, confirm overwrite
            if (outfile.exists())
            {
                int overwrite = JOptionPane.showConfirmDialog(MainWindow.this,
                        String.format("The file '%s' already exists. Overwrite?", outfile.getName()),
                        "Save As", JOptionPane.YES_NO_CANCEL_OPTION);
                switch (overwrite)
                {
                    case JOptionPane.CANCEL_OPTION: 
                        return null;
                    case JOptionPane.NO_OPTION:
                        continue;
                    case JOptionPane.YES_OPTION:
                        return outfile;
                    default:
                        return null;
                }
            }
            // Otherwise return the file chosen
            return outfile;
        }
        while (true);
    }

    /**
     * A general function used for displaying load file dialogs. This keeps all behaviours for file
     * choosing consistent across types.
     * @param fc The file chooser to be used.
     * @param title The title of the dialog.
     * @param ext The file extension.
     * @return The chosen file if one was picked, otherwise null if the user cancelled.
     */
    public File chooseLoadFile(JFileChooser fc, String title, String ext)
    {
        do
        {
            // Prevent the program from reading from the keyboard while the file dialog is active
            m_keyboardEnabled = false;
            fc.setDialogTitle(title);
            int returnVal = fc.showOpenDialog(MainWindow.this);
            m_keyboardEnabled = true;
            if (returnVal != JFileChooser.APPROVE_OPTION)
            {
                return null;
            }
            File infile = fc.getSelectedFile();
            // If it doesn't exist, try with extension
            if (!infile.exists())
            {
                infile = new File(infile.toString() + ext);
            }
            // Still nothing, prompt again
            if (!infile.exists())
            {
                Global.showWarningMessage("Load File", "Cannot find file %s.", infile.toString());
                continue;
            }
            // Otherwise return the file chosen
            return infile;
        }
        while (true);
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
        
        for (JInternalFrame f : m_desktopPane.getAllFrames())
        {
            try
            {
                MachineInternalFrame iFrame = (MachineInternalFrame)f;
                MachineGraphicsPanel gfxPanel = iFrame.getGfxPanel();
                gfxPanel.setEditingEnabled(isEnabled);
                iFrame.setClosable(isEnabled);
                m_exitAction.setEnabled(isEnabled); 
            }
            catch (ClassCastException e)
            {
                // Wrong window type
                continue;
            }
        }
        setEditingActionsEnabledState(isEnabled);
        m_tapeDispController.setEditingEnabled(isEnabled);
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

        for (JInternalFrame f : m_desktopPane.getAllFrames())
        {
            try
            {
                MachineInternalFrame iFrame = (MachineInternalFrame)f;
                MachineGraphicsPanel panel = iFrame.getGfxPanel();
                if (panel.isModifiedSinceSave())
                {
                    if (!userConfirmSaveModifiedThenClose(iFrame))
                    {
                        return;
                    }
                }
                else
                {
                    iFrame.dispose();
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
    private void translateCentroidToMiddleOfWindow(Collection<? extends State> states,
            Collection<? extends Transition> transitions, Point2D centreOfWindow,
            Point2D lastPastedLoc, int numTimesPastedToLastLoc, MachineGraphicsPanel panel)
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

        for (State s : states)
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

        if (rightMostX + translateVectorX > MACHINE_CANVAS_SIZE_X - State.STATE_RENDERING_WIDTH)
        {
            translateVectorX -= rightMostX + translateVectorX - (MACHINE_CANVAS_SIZE_X - State.STATE_RENDERING_WIDTH);
        }

        if (bottomMostY + translateVectorY >  MACHINE_CANVAS_SIZE_Y - State.STATE_RENDERING_WIDTH)
        {
            translateVectorY -= bottomMostY + translateVectorY - (MACHINE_CANVAS_SIZE_Y - State.STATE_RENDERING_WIDTH);
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


        for (State s : states)
        {
            int newX = (int)(s.getX() + translateVectorX);
            int newY = (int)(s.getY() + translateVectorY);
            s.setPosition(newX, newY);
        }

        for (Transition t : transitions)
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
    private static Point2D computeCentroid(Collection<? extends State> states)
    {
        float totalX = 0;
        float totalY = 0;

        for (State s : states)
        {
            totalX += s.getX() + State.STATE_RENDERING_WIDTH / 2; // Use middle of state
            totalY += s.getY() + State.STATE_RENDERING_WIDTH / 2; // instead of top-left
        }
        return new Point2D.Float(totalX / states.size(), totalY / states.size());
    }
  
    /**
     * Update the undo/redo buttons with the new undo/redo command names.
     */
    public void updateUndoActions()
    {
        MachineGraphicsPanel panel = getSelectedGraphicsPanel();
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
     * A wrapper around AbstractAction which exposes a more useful constructor, more easily allowing
     * for anonymous actions.
     */
    protected abstract class MenuAction extends AbstractAction
    {
        /**
         * Creates a new instance of MenuAction
         * @param text A name for the action.
         * @param icon A image for the action.
         * @param desc A description of the action.
         * @param accel The accelerator key for this action. Null if no accelerator.
         */
        public MenuAction(String text, ImageIcon icon, String desc, KeyStroke accel)
        {
            super(text);
            putValue(SMALL_ICON, icon);
            putValue(SHORT_DESCRIPTION, desc != null? desc : text);
            putValue(ACCELERATOR_KEY, accel);
        }
    }

    /**
     * Action for saving a machine diagram.
     */
    class SaveMachineAction extends MenuAction
    {
        /**
         * Creates a new instance of SaveMachineAction.
         * @param text Description of the action.
         * @param icon Icon for the action.
         * @param forceDialog Whether or not this action should always show a file chooser.
         *                    Setting this to true creates a save-as action, while setting it to
         *                    false creates a save action.
         */
        public SaveMachineAction(String text, ImageIcon icon, boolean forceDialog)
        {
            super(text, icon, null, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
            m_force = forceDialog;
        }
       
        /**
         * Save the machine to file. If forceDialog is set to true, or the machine has no associated
         * file, a dialog is shown. Otherwise, the file is saved to its associated file.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            MachineGraphicsPanel panel = getSelectedGraphicsPanel();
            if (panel == null)
            {
                // Whatever we are looking at isn't a graphics panel
                return;
            }

            Machine machine = panel.getSimulator().getMachine();
            File outFile = panel.getFile();
            try
            {
                if (m_force || outFile == null)
                {
                    // !!!
                    outFile = chooseSaveFile(m_fcMachine, "Save Machine", panel.getMachineExt());
                    if (outFile == null)
                    {
                        // Cancelled by user 
                        return;
                    }
                }
                
                Machine.saveMachine(machine, outFile);
                panel.setModifiedSinceSave(false);
                panel.setFile(outFile);
                m_console.log("Successfully saved machine %s.", panel.getFrame().getTitle());
            }
            catch (IOException ex)
            {
                m_console.log("Encountered an error when saving the machine %s.\nMessage: %s.",
                              panel.getFrame().getTitle(), ex.getMessage());
                Global.showErrorMessage("Save Machine", "Error saving machine %s.", 
                        panel.getFrame().getTitle());
            }
        }

        /**
         * Whether or not a file chooser should always be displayed.
         */
        private final boolean m_force;
    }
 
    /** 
     * Action for saving a tape.
     */
    class SaveTapeAction extends MenuAction
    {
        /**
         * Creates a new instance of SaveTapeAction. 
         * @param text Description of the action.
         * @param icon Icon for the action.
         * @param forceDialog Whether or not this action should always show a file chooser.
         *                    Setting this to true creates a save-as action, while setting it to
         *                    false creates a save action.
         */
        public SaveTapeAction(String text, ImageIcon icon, boolean forceDialog)
        {
            super(text, icon, null, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            m_force = forceDialog;
        }
        
        /**
         * Save the tape to its associated file. If it does not have an associated file, display a
         * dialog.
         * @param e The generating event.
         */
        public void actionPerformed(ActionEvent e)
        {
            Tape tape = m_tapeDisp.getTape();
            File outFile = m_tapeDisp.getFile();

            try
            {
                if (m_force || outFile == null)
                {
                    outFile = chooseSaveFile(m_fcTape, "Save Tape", Tape.TAPE_EXTENSION);
                    if (outFile == null)
                    {
                        // Cancelled by user 
                        return;
                    }
                }
                
                Tape.saveTape(m_tapeDisp.getTape(), outFile);
                m_tapeDisp.setFile(outFile);
                m_console.log("Successfully saved tape to %s.", outFile.toString());
            }
            catch (IOException ex)
            {
                m_console.log("Encountered an error when saving the tape to %s.\nMessage: %s.",
                              outFile.toString(), ex.getMessage());
                Global.showErrorMessage("Save Tape", "Error saving tape to %s.", outFile.toString());
            }
        }

        /**
         * Whether or not a file chooser should always be displayed.
         */
        private final boolean m_force;
    }

    /**
     * An action for selecting user interface interaction modes.
     */
    class GUI_ModeSelectionAction extends MenuAction
    {
        /**
         * Creates a new instance of GUI_ModeSelectionAction.
         * @param text Description of the action.
         * @param mode Mode the action puts the GUI into.
         * @param icon Icon for the action.
         * @param keyShortcut Shortcut associated with the action.
         */
        public GUI_ModeSelectionAction(String text, GUI_Mode mode, ImageIcon icon, KeyStroke keyShortcut)
        {
            super(text, icon, null, keyShortcut);
            m_mode = mode;
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
        private GUI_Mode m_mode;

        /**
         * The menu item associated with this action.
         */
        private JRadioButtonMenuItem m_menuItem = null;
    }
   
    /**
     * An action for selecting speeds for automatic execution of machines.
     */
    class ExecutionSpeedSelectionAction extends MenuAction
    {
        /**
         * Creates a new instance of ExecutionSpeedSelectionAction.
         * @param text Description of the action.
         * @param delay The new execution delay for the machine.
         * @param keyShortcut Shortcut associated with the action.
        */
        public ExecutionSpeedSelectionAction(String text, int delay, KeyStroke keyShortcut)
        {
            super(text, null, null, keyShortcut);
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
     * Singleton instance of MainWindow.
     */
    private static /*final*/ MainWindow m_instance;

    /**
     * Current GUI mode.
     */
    private GUI_Mode m_currentMode;
    
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
    private final JFileChooser m_fcMachine = new JFileChooser();

    /**
     * Dialog for choosing a file, specifically for tapes.
     */
    private final JFileChooser m_fcTape = new JFileChooser();

    /**
     * Internal timer for repeatedly calling m_timerTask.
     */
    protected final java.util.Timer m_timer = new java.util.Timer(true);

    /**
     * Timer task used for stepping through a machine on a delay.
     */
    private ExecutionTimerTask m_timerTask;
    
    /**
     * Simulation delay associated with the machine, used by m_timerTask.
     */
    private int m_executionDelayTime;

    /**
     * How many machine internal frames have been created since the program started.
     */
    private int m_windowCount = 0;

    /**
     * Data which has been copied, used for pasting.
     */
    private byte[] m_copiedData = null;

    /**
     * X ordinate of the last new frame.
     */
    private int m_lastNewWindowLocX = 0;

    /**
     * Y ordinate of the last new frame.
     */
    private int m_lastNewWindowLocY = 0;

    /**
     * Distance between new frames.
     */
    private int m_windowLocStepSize = minDistanceForNewWindowLoc;
    
    /**
     * List of buttons which have an associated GUI mode and action.
     */
    private ArrayList<GUIModeButton> m_toolbarButtons;

    /**
     * Tape display panel.
     */
    private TapeDisplayPanel m_tapeDisp;

    /**
     * Tape controller.
     */
    private TapeDisplayControllerPanel m_tapeDispController;

    /**
     * Main shared tape.
     */
    private Tape m_tape;

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
     * Frame for displaying help information as HTML.
     */
    private HelpDisplayer m_helpDisp;

    /**
     * Frame for displaying a console window for logging information.
     */
    private ConsolePanel m_console;

    /**
     * Action for creating a new Turing Machine.
     */
    public final Action m_newTuringMachineAction = 
        new MenuAction("New Turing Machine", Global.loadIcon("newMachine.png"), null, null)
        {
            public void actionPerformed(ActionEvent e)
            {
                if (m_desktopPane != null)
                {
                    TMGraphicsPanel panel = new TMGraphicsPanel(new TM_Machine(), m_tape, null);
                    MachineInternalFrame frame = newMachineWindow(panel);
                    panel.setFrame(frame);
                    addFrame(frame);
                }
            }
        };

    /**
     * Action for creating a new DFSA.
     */
    public final Action m_newDFSAAction = 
        new MenuAction("New DFSA", Global.loadIcon("newMachine.png"), null, null)
        {
            public void actionPerformed(ActionEvent e)
            {
                if (m_desktopPane != null)
                {
                    addFrame(newMachineWindow(new DFSAGraphicsPanel(new DFSA_Machine(), m_tape, null)));
                }
            }
        };

    /**
     * Action for opening a machine.
     */
    public final Action m_openMachineAction = 
        new MenuAction("Open Machine", Global.loadIcon("openMachine.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                // Choose the file to load
                File inFile = chooseLoadFile(m_fcMachine, "Load Machine", "");
                if (inFile == null)
                {
                    // Cancelled by user
                    return;
                }
                try
                {
                    Machine machine = Machine.loadMachine(inFile);

                    // TODO: Can we make this nicer?
                    JInternalFrame iFrame = null;
                    if (machine instanceof TM_Machine)
                    {
                        TMGraphicsPanel panel = new TMGraphicsPanel((TM_Machine)machine, m_tape, inFile);
                        MachineInternalFrame frame = newMachineWindow(panel);
                        panel.setFrame(frame);
                        addFrame(frame);
                    }
                    else if (machine instanceof DFSA_Machine)
                    {
                        addFrame(newMachineWindow(new DFSAGraphicsPanel((DFSA_Machine)machine, m_tape, inFile)));
                    }
                    m_console.log("Successfully loaded machine file %s.", inFile.toString());
                }
                catch (Exception ex)
                {
                    m_console.log("Encountered an error when opening machine file %s.\nMessage: %s.", 
                                  inFile.toString(), ex.getMessage());
                    Global.showErrorMessage("Open Machine", "Error opening machine file %s.", inFile.toString()); 
                }
            }
        };

    /**
     * Action for saving a machine to an associated file.
     */
    public final Action m_saveMachineAction = 
        new SaveMachineAction("Save Machine", Global.loadIcon("saveMachine.png"), false);

    /**
     * Action for saving a machine to a selected file.
     */
    public final Action m_saveMachineAsAction = 
        new SaveMachineAction("Save Machine As", Global.loadIcon("emptyIcon.png"), true);

    /**
     * Action for creating a new tape.
     */
    public final Action m_newTapeAction = 
        new MenuAction("New Tape", Global.loadIcon("newTape.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                // Prevent the program from reading from the keyboard while the file dialog is active
                m_keyboardEnabled = false;
                Object[] options = { "Ok", "Cancel" };
                int result = JOptionPane.showOptionDialog(MainWindow.this, 
                        "This will erase the tape. Do you want to continue?", "Clear tape",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
                        options, options[1]);
                m_keyboardEnabled = true;

                if (result == JOptionPane.YES_OPTION)
                {
                    m_tape.copyOther(new CA_Tape());
                    m_tapeDisp.repaint();
                }
            }
        };

    /**
     * Action for opening a tape.
     */
    public final Action m_openTapeAction = 
        new MenuAction("Open Tape", Global.loadIcon("openTape.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                File inFile = chooseLoadFile(m_fcTape, "Load Tape", Tape.TAPE_EXTENSION);
                if (inFile == null)
                {
                    // Cancelled by user
                    return;
                }

                try
                {
                    Tape tape = Tape.loadTape(inFile);
                    m_tapeDisp.getTape().copyOther(tape);
                    m_tapeDisp.setFile(inFile);
                    m_tapeDisp.repaint();
                    m_console.log("Successfully loaded tape file %s.", inFile.toString());
                }
                catch (Exception ex)
                {
                    m_console.log("Encountered an error when opening tape file %s.\nMessage: %s.",
                                  inFile.toString(), ex.getMessage());
                    Global.showErrorMessage("Open Tape", "Error opening tape file %s.", inFile.toString());
                }
            }
        };

    /**
     * Action for saving a tape to an associated file.
     */
    public final Action m_saveTapeAction = 
        new SaveTapeAction("Save Tape", Global.loadIcon("saveTape.png"), false);
    
    /**
     * Action for saving a tape to a selected file.
     */
    public final Action m_saveTapeAsAction = 
        new SaveTapeAction("Save Tape As", Global.loadIcon("emptyIcon.png"), true);

    /**
     * Action for exiting the program.
     */
    public final Action m_exitAction = 
        new MenuAction("Exit", Global.loadIcon("emptyIcon.png"), null, null)
        {
            public void actionPerformed(ActionEvent e)
            {
                userRequestToExit();
            }
        };

    /**
     * Action for undoing a command.
     */
    public final Action m_undoAction = 
        new MenuAction("Undo", Global.loadIcon("undoIcon.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    panel.undoCommand();
                    updateUndoActions();
                    panel.repaint();
                }
            }
        };

    /**
     * Action for redoing a command
     */
    public final Action m_redoAction = 
        new MenuAction("Redo", Global.loadIcon("redoIcon.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {   
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    panel.redoCommand();
                    updateUndoActions();
                    panel.repaint();
                }
            }
        };

    /**
     * Action for cutting selected states and transitions.
     */
    public final Action m_cutAction = 
        new MenuAction("Cut", Global.loadIcon("cut.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {   
                m_copyAction.actionPerformed(e); 
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    panel.doCommand(new CutCommand(panel, 
                                (HashSet<? extends State>)panel.getSelectedStates().clone(),
                                (HashSet<? extends Transition>)panel.getSelectedTransitions().clone()));
                    updateUndoActions();
                }
            }
        };

    /**
     * Action for copying selected states and transitions.
     */
    public final Action m_copyAction = 
        new MenuAction("Copy", Global.loadIcon("copy.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {   
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    m_copiedData = panel.copySelectedToByteArray();
                }
            }
        };

    /**
     * Action for pasting selected states and transitions.
     */
    public final Action m_pasteAction = 
        new MenuAction("Paste", Global.loadIcon("paste.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {   
                try
                {
                    MachineInternalFrame iFrame = (MachineInternalFrame)m_desktopPane.getSelectedFrame();
                    if (m_copiedData == null || iFrame == null)
                    {
                        // Abort
                        return;
                    }

                    // Translate our byte[] back into real data
                    ObjectInputStream restore = new ObjectInputStream(new ByteArrayInputStream(m_copiedData));
                    HashSet<State> selectedStates = (HashSet<State>)restore.readObject();
                    HashSet<Transition> selectedTransitions = (HashSet<Transition>)restore.readObject();

                    // Figure out roughly the centre-of-mass of the copied data
                    Point2D centroid = computeCentroid(selectedStates);

                    // Find the centre of the frame
                    Point2D centreOfWindow = iFrame.getCenterOfViewPort();
                    translateCentroidToMiddleOfWindow(selectedStates, selectedTransitions,
                            centreOfWindow, iFrame.getGfxPanel().getLastPastedLocation(),
                            iFrame.getGfxPanel().getNumPastesToSameLocation(), iFrame.getGfxPanel());

                    MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                    if (panel != null)
                    {
                        Machine machine = panel.getSimulator().getMachine();
                        panel.doCommand(new PasteCommand(panel, selectedStates, selectedTransitions)); 
                        updateUndoActions();
                    }
                }
                catch (Exception e2) { }
            }
        };

    /**
     * Action for deleting selected states and transitions.
     */
    public final Action m_deleteAction = 
        new MenuAction("Delete Selected Items", Global.loadIcon("delete.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
        {
            public void actionPerformed(ActionEvent e)
            {   
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    panel.deleteAllSelected();
                }
            }
        };

    /**
     * Action associated with ADDNODES.
     */
    public final GUI_ModeSelectionAction m_addNodesAction = 
        new GUI_ModeSelectionAction("Add States", GUI_Mode.ADDNODES,
            Global.loadIcon("state.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));

    /**
     * Action associated with ADDTRANSITIONS.
     */
    public final GUI_ModeSelectionAction m_addTransitionsAction = 
        new GUI_ModeSelectionAction("Add Transitions", GUI_Mode.ADDTRANSITIONS,
            Global.loadIcon("transition.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));

    /**
     * Action associated with SELECTION.
     */
    public final GUI_ModeSelectionAction m_selectionAction = 
        new GUI_ModeSelectionAction("Make Selection", GUI_Mode.SELECTION,
            Global.loadIcon("selection.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F4,0));

    /**
     * Action associated with ERASER.
     */
    public final GUI_ModeSelectionAction m_eraserAction = 
        new GUI_ModeSelectionAction("Eraser", GUI_Mode.ERASER, 
            Global.loadIcon("eraser.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

    /**
     * Action associated with CHOOSESTART.
     */
    public final GUI_ModeSelectionAction m_chooseStartAction = 
        new GUI_ModeSelectionAction("Choose Start State", GUI_Mode.CHOOSESTART, 
            Global.loadIcon("startState.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));

    /**
     * Action associated with CHOOSEFINAL.
     */
    public final GUI_ModeSelectionAction m_chooseFinalAction = 
        new GUI_ModeSelectionAction("Choose Final State", GUI_Mode.CHOOSEFINAL,
            Global.loadIcon("finalState.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F7,0));

    /**
     * Action associated with CHOOSECURRENTSTATE.
     */
    public final GUI_ModeSelectionAction m_chooseCurrentStateAction = 
        new GUI_ModeSelectionAction("Choose Current State", GUI_Mode.CHOOSECURRENTSTATE,
            Global.loadIcon("currentState.png"), KeyStroke.getKeyStroke(KeyEvent.VK_F8,0));

    /**
     * Action for validating the machine.
     */
    public final Action m_validateAction =
        new MenuAction("Validate", Global.loadIcon("validate.png"), null,
                       KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                MachineGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
                if (gfxPanel == null)
                {
                    return;
                }

                String result = gfxPanel.getSimulator().getMachine().isDeterministic();
                if (result == null)
                {
                    m_console.log("%s is deterministic.",
                            gfxPanel.getFrame().getTitle());
                    Global.showInfoMessage("Validation", "Machine is deterministic."); 
                }
                else
                {
                    m_console.log("%s is nondeterministic: %s.", 
                            gfxPanel.getFrame().getTitle(), result);
                    Global.showErrorMessage("Validation", "Machine is nondeterministic: %s.",  result);
                }
            }
        };

    /**
     * Action for stepping through execution.
     */
    public final Action m_stepAction = 
        new MenuAction("Step", Global.loadIcon("step.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                MachineGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
                if (gfxPanel == null)
                {
                    return;
                }

                try
                {
                    Simulator sim = gfxPanel.getSimulator();
                    // Pre-validate the machine
                    String result = sim.getMachine().hasUndefinedSymbols();
                    if (result != null)
                    {
                        m_console.log("Cannot simulate %s: %s.", 
                                gfxPanel.getFrame().getTitle(), result);
                        Global.showErrorMessage("Step", "Cannot simulate: %s.", result);
                        return;
                    }
                    // If we are just starting, write out the input on the tape
                    if (sim.getCurrentState() == null)
                    {
                        // Issue a minor warning to the console if the r/w head is not in the
                        // leftmost cell; continue execution
                        if (m_tape.headLocation() != 0)
                        {
                            m_console.log("Warning: Tape head has not been reset.");
                        }
                        m_console.logPartial(gfxPanel, "Input: %s\n", 
                                m_tape.getPartialString(m_tape.headLocation(), 
                                                        m_tape.getLength() - m_tape.headLocation()));
                    }
                    sim.step();
                    m_tapeDisp.repaint();
                    if (sim.isHalted())
                    {
                        m_console.logPartial(gfxPanel, sim.getConfiguration());
                        m_console.endPartial();
                    }
                    else
                    {
                        m_console.logPartial(gfxPanel, "%s %c ", sim.getConfiguration(), Global.CONFIG_TEE);
                    }
                }
                // Machine halted as expected
                catch (ComputationCompletedException e2)
                {
                    String msg = gfxPanel.getErrorMessage(e2);
                    m_console.log("Simulation of %s finished: %s.", 
                            gfxPanel.getFrame().getTitle(), msg);
                    Global.showInfoMessage(MainWindow.HALTED_MESSAGE_TITLE_STR, 
                            "Simulation finished: %s.", msg);
                    gfxPanel.getSimulator().resetMachine();
                }
                // Machine halted unexpectedly
                catch (Exception e2)
                {
                    String msg = gfxPanel.getErrorMessage(e2);
                    m_console.log("Simulation of %s halted unexpectedly: %s.",
                            gfxPanel.getFrame().getTitle(), msg);
                    Global.showErrorMessage(MainWindow.HALTED_MESSAGE_TITLE_STR,
                            "Simulation halted unexpectedly: %s.", msg);
                }
                repaint();
            }
        };

    /**
     * Action for starting simulation of the machine.
     */
    public final Action m_fastExecuteAction = 
        new MenuAction("Execute", Global.loadIcon("fastExecute.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            { 
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    if (m_timerTask != null)
                    {
                        m_timerTask.cancel();
                    }
                    setEditingEnabled(false);
                    m_timerTask = new ExecutionTimerTask(panel, m_tapeDisp);
                    m_timer.schedule(m_timerTask, 0, m_executionDelayTime);
                }
            }
        };

    /**
     * Action for pausing simulation of the machine.
     */
    public final Action m_pauseExecutionAction = 
        new MenuAction("Pause Execution", Global.loadIcon("pause.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                stopExecution();
                updateUndoActions();
            }
        };

    /**
     * Action for stopping a simulation.
     */
    public final Action m_stopMachineAction = 
        new MenuAction("Stop Execution", Global.loadIcon("stop.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                MachineGraphicsPanel gfxPanel = getSelectedGraphicsPanel();
                boolean wasRunning = stopExecution();
                if (gfxPanel != null)
                {
                    // TODO: reset it even if not running
                    if (m_timerTask == null || !wasRunning || gfxPanel == m_timerTask.getPanel())
                    {
                        gfxPanel.getSimulator().resetMachine();
                        gfxPanel.repaint();
                        m_console.log("Stopped executing %s.", gfxPanel.getFrame().getTitle());
                    }
                }
                updateUndoActions();
            }
        };

    /**
     * Action to set execution speed to slow.
     */
    public final ExecutionSpeedSelectionAction m_slowExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Slow", SLOW_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to medium.
     */
    public final ExecutionSpeedSelectionAction m_mediumExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Medium", MEDIUM_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to fast.
     */
    public final ExecutionSpeedSelectionAction m_fastExecuteSpeedAction =
        new ExecutionSpeedSelectionAction("Fast", FAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to superfast.
     */
    public final ExecutionSpeedSelectionAction m_superFastExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Super Fast", SUPERFAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK));

    /**
     * Action to set execution speed to ultrafast.
     */
    public final ExecutionSpeedSelectionAction m_ultraFastExecuteSpeedAction = 
        new ExecutionSpeedSelectionAction("Ultra Fast", ULTRAFAST_EXECUTE_SPEED_DELAY,
                KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK)); 

    /**
     * Action for moving the read/write head to the start of the tape.
     */
    public final Action m_headToStartAction = 
        new MenuAction("Reset Read/Write Head", Global.loadIcon("tapeStart.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e) 
            {
                // Move r/w head to the left end of the tape
                m_tapeDisp.getTape().resetRWHead();
                m_tapeDispController.repaint();
            }
        };

    /**
     * Action for reloading the tape.
     */
    public final Action m_reloadTapeAction = 
        new MenuAction("Reload Tape", Global.loadIcon("tapeReload.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e) 
            {
                Object[] options = {"Ok", "Cancel"};
                File tfile = m_tapeDisp.getFile();

                // TODO: should disable keyboard here
                if (tfile == null)
                {
                    int result = JOptionPane.showOptionDialog(null,
                            "This will erase the tape. Do you want to continue?", "Reload tape",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            options, options[1]);
                    if (result == JOptionPane.YES_OPTION)
                    {
                        m_tape.clearTape();
                    }
                }
                else
                {
                    int result = JOptionPane.showOptionDialog(null, 
                            "This will reload the tape, discarding any changes. Do you want to continue?", 
                            "Reload tape", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[1]);
                    if (result == JOptionPane.YES_OPTION) try
                    {
                        m_tape = Tape.loadTape(tfile);
                        m_tapeDisp.getTape().copyOther(m_tape);
                        m_tapeDisp.setFile(tfile);
                        m_tapeDisp.repaint();
                        m_console.log("Reloaded tape from file %s.", tfile.toString());
                    }
                    catch (Exception ex)
                    {
                        m_console.log("Encountered an error when loading the tape %s.\nMessage: %s.", 
                                      tfile.toString(), ex.getMessage());
                        Global.showWarningMessage("Reload Tape", "Error opening tape file %s.", tfile.toString());
                    }
                }
            }
        };

    /**
     * Action for erasing the tape.
     */
    public final Action m_eraseTapeAction = 
        new MenuAction("Erase Tape", Global.loadIcon("tapeClear.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e) 
            {
                // Wipe the tape.
                Object[] options = {"Ok", "Cancel"};
                // TODO: should disable keyboard here
                int result = JOptionPane.showOptionDialog(null, 
                        "This will erase the tape. Do you want to continue?", "Clear tape", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
                        options, options[1]);
                if (result == JOptionPane.YES_OPTION)
                {
                    m_tapeDisp.getTape().clearTape();
                    m_tapeDisp.setFile(null);
                    m_tapeDispController.repaint();
                }
            }
        };

    /**
     * Action for configuring the alphabet.
     */
    public final Action m_configureAlphabetAction = 
        new MenuAction("Configure Alphabet", Global.loadIcon("configureAlphabet.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK))
        {
            public void actionPerformed(ActionEvent e)
            {
                MachineGraphicsPanel panel = getSelectedGraphicsPanel();
                if (panel != null)
                {
                    m_asif.setPanel(panel);
                    m_asif.show();
                    getGlassPane().setVisible(true);
                }
            }
        };

    /**
     * Action for displaying help documentation.
     */
    public final Action m_helpAction = 
        new MenuAction("Help", Global.loadIcon("help.png"), null, 
                       KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
        {
            public void actionPerformed(ActionEvent e)
            {
                if (m_helpDisp == null)
                {
                    m_helpDisp = new HelpDisplayer();
                    m_helpDisp.setLayer(60);
                }
                if (!m_helpDisp.isVisible())
                {
                    addFrame(m_helpDisp);
                }
                else
                {
                    m_helpDisp.moveToFront();
                    try { m_helpDisp.setSelected(true); }
                    catch (PropertyVetoException e2) { }
                }
            }
        };

    /**
     * Action for displaying meta information about the program.
     */
    public final Action m_aboutAction = 
        new MenuAction("About", Global.loadIcon("tuataraSmall.png"), null, null)
        {
            public void actionPerformed(ActionEvent e)
            {
                Global.showInfoMessage("About Tuatara",
                        "Tuatara Turing Machine Simulator %s was written by Jimmy Foulds in 2006-2007,\n" + 
                        "and extended by Mitchell Grout in 2017-2018, with funding from the\n"            +
                        "Department of Mathematics at the University of Waikato, New Zealand.\n"          +
                        "Graphics were kindly provided by Justin Bedggood.", Global.VERSION);
            }
        };
}
