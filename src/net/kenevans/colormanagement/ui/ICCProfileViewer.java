package net.kenevans.colormanagement.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.color.ICC_ProfileRGB;
import java.awt.color.ProfileDataException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jfree.chart.ChartColor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import net.kenevans.colormanagement.model.ICCProfileModel;
import net.kenevans.core.utils.AboutBoxPanel;
import net.kenevans.core.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;
import net.kenevans.jfreechart.jfreechartutils.PlotXY;

public class ICCProfileViewer extends JFrame
{
    private static final String NAME = "ICC Profile Viewer";
    private static final String VERSION = "1.0.0";
    private static final String HELP_TITLE = NAME + " " + VERSION;
    private static final String AUTHOR = "Written by Kenneth Evans, Jr.";
    private static final String COPYRIGHT = "Copyright (c) 2012-2017 Kenneth Evans";
    private static final String COMPANY = "kenevans.net";

    public static final boolean USE_START_FILE_NAME = false;
    private static final long serialVersionUID = 1L;
    private static final String TITLE = NAME;
    // private static final String DEFAULT_PROFILE =DEFAULT_PROFILE
    // "C:/Windows/System32/spool/drivers/color/AlienwareCustom.icm";
    // private static final String DEFAULT_PROFILE =
    // "C:/Windows/System32/spool/drivers/color/Cosmos-2016-12-13-ColorMunki.icm";
    private static final String DEFAULT_PROFILE = "C:/Windows/System32/spool/drivers/color/xRite-2012-05-04-6500-2.2-090.icc";
    // private static final String DEFAULT_PROFILE =
    // "C:/Users/evans/Pictures/ImageBrowser Test/BGR-Wcs-RBG-Icc-Test.icc";
    // private static final String DEFAULT_PROFILE =
    // "C:/Windows/System32/spool/drivers/color/xRite-2010-07-08-6500-2.2-090.ICC";

    /** URL for online help. */
    // private static final String HELP_URL =
    // "file:///C:/Users/evans/Documents/Web%20Pages/Local/localHomePage.html";
    // private static final String HELP_URL = "http://kenevans.net";

    private String defaultPath = "C:/Windows/System32/spool/drivers/color";
    // private String defaultPath =
    // "C:/Users/evans/Documents/Visual Studio Projects/Xcalib";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 600;
    public static final String LS = System.getProperty("line.separator");

    private ICCProfileModel profileModel = new ICCProfileModel();

    private JTextArea textArea;
    private JMenuBar menuBar;

    public ICCProfileViewer() {
        uiInit();
    }

    /**
     * Initializes the user interface.
     */
    void uiInit() {
        this.setLayout(new BorderLayout());

        // Create the text area used for output. Request
        // enough space for 5 rows and 30 columns.
        textArea = new JTextArea(25, 30);
        textArea.setEditable(false);
        // // DEBUG
        // String lafName = UIManager.getSystemLookAndFeelClassName();
        // Font font = textArea.getFont();
        // System.out.println("JTextArea:");
        // System.out.println(" Name: " + font.getName());
        // System.out.println(" FontName: " + font.getFontName());
        // System.out.println(" Family: " + font.getFamily());
        // System.out.println(" Size: " + font.getSize());
        // System.out.println(" Style: " + font.getStyle());
        // System.out.println(" LAF Name: " + lafName);
        // String[] keys = {"TextArea.font", "Dialog.font"};
        // for(String key : keys) {
        // System.out.println(" Key: " + key);
        // Font uiFont = UIManager.getFont(key);
        // if(uiFont != null) {
        // System.out.println(" Name: " + uiFont.getName());
        // System.out.println(" FontName: " + uiFont.getFontName());
        // System.out.println(" Family: " + uiFont.getFamily());
        // System.out.println(" Size: " + uiFont.getSize());
        // System.out.println(" Style: " + uiFont.getStyle());
        // }
        // }
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Initializes the menus.
     */
    private void initMenus() {
        // Menu
        menuBar = new JMenuBar();

        // File
        JMenu menu = new JMenu();
        menu.setText("File");
        menuBar.add(menu);

        // File Open
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText("Open...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                open();
            }
        });
        menu.add(menuItem);

        JSeparator separator = new JSeparator();
        menu.add(separator);

        // File Exit
        menuItem = new JMenuItem();
        menuItem.setText("Exit");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                quit();
            }
        });
        menu.add(menuItem);

        // Tools
        menu = new JMenu();
        menu.setText("Tools");
        menuBar.add(menu);

        // Tools VCGT
        menuItem = new JMenuItem();
        menuItem.setText("VCGT Plot...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                plotVcgt();
            }
        });
        menu.add(menuItem);

        // Tools TRC
        menuItem = new JMenuItem();
        menuItem.setText("TRC Plot...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                plotTrc();
            }
        });
        menu.add(menuItem);

        // Help
        menu = new JMenu();
        menu.setText("Help");
        menuBar.add(menu);

        // menuItem = new JMenuItem();
        // menuItem.setText("Contents");
        // menuItem.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent ae) {
        // try {
        // java.awt.Desktop.getDesktop()
        // .browse(java.net.URI.create(HELP_URL));
        // } catch(IOException ex) {
        // Utils.excMsg("Cannot open help contents", ex);
        // }
        // }
        // });
        // menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setText("About");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null,
                    new AboutBoxPanel(HELP_TITLE, AUTHOR, COMPANY, COPYRIGHT),
                    "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        menu.add(menuItem);
    }

    /**
     * Loads new data.
     * 
     * @param data
     */
    private void loadData(byte[] data) {
        if(data == null) {
            Utils.errMsg("Data is null");
            return;
        }
        setText("");
        this.setTitle(TITLE);

        try {
            profileModel.readProfile(data);
        } catch(Exception ex) {
            Utils.excMsg("Error reading profile data", ex);
            return;
        }

        this.setTitle(profileModel.getProfileName());
        showIccProfileInfo();
    }

    /**
     * Loads a new file.
     * 
     * @param fileName
     */
    private void loadFile(File file) {
        if(file == null) {
            Utils.errMsg("File is null");
            return;
        }
        setText("");
        this.setTitle(file.getName());

        try {
            profileModel.readProfile(file);
        } catch(Exception ex) {
            Utils.excMsg("Error reading profile for " + file.getName(), ex);
            return;
        }

        showIccProfileInfo();
    }

    /**
     * Sets the text in the TextArea.
     * 
     * @param text
     */
    public void setText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    /**
     * Appends to the text in the TextArea.
     * 
     * @param text
     */
    public void appendText(String text) {
        String oldText = textArea.getText();
        if(oldText == null) {
            textArea.setText(text);
        } else {
            textArea.setText(oldText + text);

        }
        textArea.setCaretPosition(0);
    }

    /**
     * Brings up a JFileChooser to open a file.
     */
    private void open() {
        JFileChooser chooser = new JFileChooser();
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Save the selected path for next time
            defaultPath = chooser.getSelectedFile().getParentFile().getPath();
            // Process the file
            File file = chooser.getSelectedFile();
            loadFile(file);
        }
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Plots the VCGT curves.
     */
    public void plotVcgt() {
        double[][] table = null;
        Integer gammaType = profileModel.getVcgtGammaType();
        if(gammaType != null) {
            if(gammaType == 0) {
                // Table
                table = profileModel.getVcgtTable();
            } else {
                // Formula
                table = profileModel.getVcgtFormulaTable();
            }
        }
        if(table == null) {
            Utils.errMsg("VCGT table is not available");
            return;
        }
        if(table.length == 0) {
            Utils.errMsg("VCGT table is empty");
            return;
        }
        int nComponents = table.length;
        int nEntries = table[0].length;

        // Generate the x values
        double[][] xVals = new double[1][nEntries];
        for(int i = 0; i < nEntries; i++) {
            xVals[0][i] = 255.f * i / (nEntries - 1);
        }

        // Run the plot
        final PlotXY app = new PlotXY("VGCT Curves", "Input", "Output", xVals,
            table);
        // Change the default colors to red, green, blue in that order
        JFreeChart chart = app.getChart();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)chart
            .getXYPlot().getRenderer();
        if(nComponents == 1) {
            renderer.setSeriesPaint(0, ChartColor.black);
        } else {
            renderer.setSeriesPaint(0, ChartColor.red);
            renderer.setSeriesPaint(1, ChartColor.green);
            renderer.setSeriesPaint(2, ChartColor.blue);
        }
        // Change the axis limits to 0,255
        chart.getXYPlot().getRangeAxis().setRange(0, 255);
        chart.getXYPlot().getDomainAxis().setRange(0, 255);
        // String title = "VCGT Curves";
        String title = profileModel.getDisplayName();
        app.run(title);
        JFrame frame = app.getFrame();
        frame.setSize(new Dimension(WIDTH, WIDTH));
        // Set the icon
        ImageUtils.setIconImageFromResource(frame,
            "/resources/ICC Profile Viewer.256x256.png");
    }

    /**
     * Plots the TRC curves.
     */
    public void plotTrc() {
        // Check if it is an RGB profile
        ICC_ProfileRGB iccRGB = profileModel.getProfileRGB();
        if(iccRGB == null) {
            Utils.errMsg("Is not an RGB profile so has no TRC");
            return;
        }
        // See if it is Gamma or TRC
        try {
            iccRGB.getGamma(0);
            Utils.errMsg("Has Gamma, not TRC");
            return;
        } catch(ProfileDataException ex) {
            // Do nothing
        }

        // Check if there is a table
        try {
            short[] table = iccRGB.getTRC(0);
            if(table == null) {
                Utils.errMsg("TRC table is not available");
                return;
            }
        } catch(ProfileDataException ex) {
            Utils.errMsg("TRC table is not available");
            return;
        }
        int nComponents = iccRGB.getNumComponents();
        int nEntries = 0;
        short shortVal;
        double[][] yVals = new double[nComponents][];
        double[][] xVals = null;
        short[] table;
        // black=0, red=1, green=2, blue=3
        for(int i = 0; i < nComponents; i++) {
            table = iccRGB.getTRC(i);
            if(i == 0) {
                nEntries = table.length;
                xVals = new double[1][nEntries];
            }
            yVals[i] = new double[nEntries];
            for(int j = 0; j < nEntries; j++) {
                shortVal = table[j];
                // These are really unsigned shorts, not shorts
                yVals[i][j] = (float)(shortVal & 0xFFFF) / (float)0xFFFF;
                // Fill in the x values from the first component
                if(i == 0) {
                    xVals[0][j] = 255.f * j / (nEntries - 1);
                }
            }
        }

        // Run the plot
        final PlotXY app = new PlotXY("TRC Curves", "Input", "Output", xVals,
            yVals);
        // Change the default colors to red, green, blue in that order
        JFreeChart chart = app.getChart();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)chart
            .getXYPlot().getRenderer();
        if(nComponents == 1) {
            renderer.setSeriesPaint(0, ChartColor.black);
        } else {
            renderer.setSeriesPaint(0, ChartColor.red);
            renderer.setSeriesPaint(1, ChartColor.green);
            renderer.setSeriesPaint(2, ChartColor.blue);
        }
        // Change the axis limits to 0,255
        chart.getXYPlot().getRangeAxis().setRange(0, 1);
        chart.getXYPlot().getDomainAxis().setRange(0, 255);
        // String title = "VCGT Curves";
        String title = profileModel.getDisplayName();
        app.run(title);
        JFrame frame = app.getFrame();
        frame.setSize(new Dimension(WIDTH, WIDTH));
        // Set the icon
        ImageUtils.setIconImageFromResource(frame,
            "/resources/VCGT Viewer.256x256.png");
    }

    private void showIccProfileInfo() {
        if(profileModel == null) {
            return;
        }
        String info = "ICC Profile" + LS;
        // String infoString = profileModel.getInfo();
        // String[] tokens = infoString.split(LS);
        // for(String token : tokens) {
        // info += " " + token + LS;
        // }
        info += profileModel.getInfo() + LS;
        appendText(info);
    }

    /**
     * Puts the panel in a JFrame and runs the JFrame for the given profile
     * data.
     * 
     * @param data The profile data.
     */
    public void run(byte[] data) {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // SwingUtilities.updateComponentTreeUI(this);
            this.setTitle(TITLE);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Has to be done here. The menus are not part of the JPanel.
            initMenus();
            this.setJMenuBar(menuBar);

            // Display the window
            this.setBounds(20, 20, WIDTH, HEIGHT);
            this.setVisible(true);
            loadData(data);
        } catch(Throwable t) {
            Utils.excMsg("Error running " + NAME, t);
        }
    }

    /**
     * Puts the panel in a JFrame and runs the JFrame.
     */
    public void run() {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // SwingUtilities.updateComponentTreeUI(this);
            this.setTitle(TITLE);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Set the icon
            ImageUtils.setIconImageFromResource(this,
                "/resources/ICC Profile Viewer.256x256.png");

            // Has to be done here. The menus are not part of the JPanel.
            initMenus();
            this.setJMenuBar(menuBar);

            // Display the window
            this.setBounds(20, 20, WIDTH, HEIGHT);
            this.setVisible(true);
            if(USE_START_FILE_NAME) {
                File file = new File(DEFAULT_PROFILE);
                loadFile(file);
            }
        } catch(Throwable t) {
            Utils.excMsg("Error running ICCProfileViewer", t);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final ICCProfileViewer app = new ICCProfileViewer();

        try {
            // Set window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);

            // Set the native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Throwable t) {
            Utils.excMsg("Error setting Look & Feel", t);
        }

        // Make the job run in the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(app != null) {
                    app.run();
                }
            }
        });
    }

}
