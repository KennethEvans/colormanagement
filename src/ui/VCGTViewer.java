package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import misc.AboutBoxEvansPanel;
import model.ICCProfileModel;
import net.kenevans.imagemodel.utils.Utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A viewer to view the Vcgt tables for several profiles simultaneously.
 * 
 * ICC profiles are big Endian and Java is also Big Endian, so no conversions
 * are necessary.
 * 
 * @author Kenneth Evans, Jr.
 */
public class VCGTViewer extends JFrame
{
    public static final boolean USE_START_FILE_NAME = true;
    private static final long serialVersionUID = 1L;
    private static final String title = "ICC VCGT Table Viewer";
    // private static final String DEFAULT_PROFILE =
    // "C:/Windows/System32/spool/drivers/color/AlienwareCustom.icm";
    private static final String DEFAULT_PROFILE = "C:/Windows/System32/spool/drivers/color/xRite-2012-05-04-6500-2.2-090.icc";
    // private static final String DEFAULT_PROFILE =
    // "C:/Users/evans/Pictures/ImageBrowser Test/BGR-Wcs-RBG-Icc-Test.icc";
    // private static final String DEFAULT_PROFILE =
    // "C:/Windows/System32/spool/drivers/color/xRite-2010-07-08-6500-2.2-090.ICC";

    private String defaultPath = "C:/Windows/System32/spool/drivers/color";
    // private String defaultPath =
    // "C:/Users/evans/Documents/Visual Studio Projects/Xcalib";
    private static final int WIDTH = 600;
    private static final int HEIGHT = 875;
    public static final String LS = System.getProperty("line.separator");
    public static final double[][] emptyTable = new double[1][0];

    private static final int MAIN_PANE_DIVIDER_LOCATION = 2 * HEIGHT / 3;

    private static enum SortOrder {
        NAME, LAST_MOFIFIED
    };

    SortOrder sortOrder = SortOrder.NAME;

    private Container contentPane = this.getContentPane();
    private JPanel listPanel = new JPanel();
    private DefaultListModel listModel = new DefaultListModel();
    private JList list = new JList(listModel);
    private JScrollPane listScrollPane;
    private JPanel displayPanel = new JPanel();
    private ChartPanel chartPanel;
    private JPanel mainPanel = new JPanel();
    private JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        displayPanel, listPanel);
    private JMenuBar menuBar;

    private ArrayList<Profile> profiles = new ArrayList<Profile>();
    private XYSeriesCollection dataset = new XYSeriesCollection();

    public VCGTViewer() {
        uiInit();
    }

    /**
     * Initializes the user interface.
     */
    void uiInit() {
        this.setLayout(new BorderLayout());

        // Display panel
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT / 2));
        JFreeChart jfreechart = createChart();
        chartPanel = new ChartPanel(jfreechart);
        chartPanel.setPreferredSize(new Dimension(600, 270));
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        javax.swing.border.CompoundBorder compoundborder = BorderFactory
            .createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createEtchedBorder());
        chartPanel.setBorder(compoundborder);
        displayPanel.add(chartPanel);

        // List panel
        listScrollPane = new JScrollPane(list);
        listPanel.setLayout(new BorderLayout());
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
                // Internal implementation
                onListItemSelected(ev);
            }
        });

        list.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
                // Use the file name from the profile as the label
                Profile profile = (Profile)value;
                label.setText(profile.getFile().getPath());
                if(profile.isChecked()) {
                    label.setBackground(Color.WHITE);
                } else {
                    label.setBackground(Color.LIGHT_GRAY);
                }
                return label;
            }
        });

        // Main split pane
        mainPane.setContinuousLayout(true);
        mainPane.setDividerLocation(MAIN_PANE_DIVIDER_LOCATION);
        if(false) {
            mainPane.setOneTouchExpandable(true);
        }

        // Main panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainPane, BorderLayout.CENTER);

        // Content pane
        // For the drag behavior to work correctly, the tool bar must be in a
        // container that uses the BorderLayout layout manager. The component
        // that
        // the tool bar affects is generally in the center of the container. The
        // tool bar must be the only other component in the container, and it
        // must
        // not be in the center.
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the menus.
     */
    private void initMenus() {
        // Menu
        menuBar = new JMenuBar();

        // File
        JMenu menuFile = new JMenu();
        menuFile.setText("File");
        menuBar.add(menuFile);

        // File Open
        JMenuItem menuFileOpen = new JMenuItem();
        menuFileOpen.setText("Open...");
        menuFileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                open();
            }
        });
        menuFile.add(menuFileOpen);

        JSeparator separator = new JSeparator();
        menuFile.add(separator);

        // File Exit
        JMenuItem menuFileExit = new JMenuItem();
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                quit();
            }
        });
        menuFile.add(menuFileExit);

        // Help
        JMenu menuHelp = new JMenu();
        menuHelp.setText("Help");
        menuBar.add(menuHelp);

        JMenuItem menuHelpAbout = new JMenuItem();
        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null, new AboutBoxEvansPanel(
                    title), "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        menuHelp.add(menuHelpAbout);
    }

    /**
     * Puts the panel in a JFrame and runs the JFrame.
     */
    public void run() {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.setTitle(title);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Has to be done here. The menus are not part of the JPanel.
            initMenus();
            this.setJMenuBar(menuBar);

            // Display the window
            this.setBounds(20, 20, WIDTH, HEIGHT);
            this.setVisible(true);
            if(USE_START_FILE_NAME) {
                File file = new File(DEFAULT_PROFILE);
                File[] files = new File[] {file};
                loadFiles(files);
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Loads a new file.
     * 
     * @param fileName
     */
    private void loadFiles(File[] files) {
        if(files == null) {
            Utils.errMsg("Files array is null");
            return;
        }
        if(files.length == 0) {
            Utils.errMsg("Files array is empty");
            return;
        }

        // Process the files
        clearPlot();
        profiles.clear();
        // setText("");
        for(File file : files) {
            Profile profile = null;
            try {
                profile = new Profile(file);
            } catch(Exception ex) {
                Utils.excMsg("Error reading profile for " + file.getName(), ex);
                continue;
            }
            // Set the checked state to true unless there is no VCGT table
            ICCProfileModel profileModel = profile.getProfileModel();
            Integer gammaType = profileModel.getVcgtGammaType();
            profile.setChecked(gammaType == null ? false : true);
            profiles.add(profile);
            // appendText(file.getPath() + LS);
        }
        Collections.sort(profiles);

        // Populate the list
        populateList();

        // // Update the text
        // for(Profile profile : profiles) {
        // appendText(profile.getFile().getPath() + LS);
        // }

        // Update the plot
        refresh();
    }

    /**
     * Populates the list from the list of profiles.
     */
    private void populateList() {
        list.setEnabled(false);
        listModel.removeAllElements();
        for(Profile profile : profiles) {
            listModel.addElement(profile);
        }
        list.validate();
        mainPane.validate();
        list.setEnabled(true);
    }

    /**
     * Updates the plot.
     */
    private void refresh() {
        clearPlot();
        int nProfiles = profiles.size();
        int iSeries = 0;
        for(Profile profile : profiles) {
            addProfileToChart(profile, iSeries++, nProfiles);
        }
    }

    private JFreeChart createChart() {
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("VGCT Curves",
            "Input", "Output", null, PlotOrientation.VERTICAL, // BasicImagePlot
                                                               // Orientation
            false, // Show Legend
            false, // Use tooltips
            false // Configure chart to generate URLs?
            );
        // XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)chart
        // .getXYPlot().getRenderer();
        // if(nComponents == 1) {
        // renderer.setSeriesPaint(0, ChartColor.black);
        // } else {
        // renderer.setSeriesPaint(0, ChartColor.red);
        // renderer.setSeriesPaint(1, ChartColor.green);
        // renderer.setSeriesPaint(2, ChartColor.blue);
        // }
        // Change the axis limits to 0,255
        chart.getXYPlot().getRangeAxis().setRange(0, 255);
        chart.getXYPlot().getDomainAxis().setRange(0, 255);
        XYPlot plot = chart.getXYPlot();
        // Set the dataset. We will mostly deal with the dataset later.
        plot.setDataset(dataset);
        return chart;
    }

    private void clearPlot() {
        try {
            dataset.removeAllSeries();
        } catch(Exception ex) {
            Utils.excMsg("Error clearing plot", ex);
        }
    }

    private void addProfileToChart(Profile profile, int iSeries, int nSeries) {
        try {
            double[][] table = null;
            ICCProfileModel profileModel = profile.getProfileModel();
            Integer gammaType = profileModel.getVcgtGammaType();
            if(gammaType != null) {
                if(gammaType == 0) {
                    // Table
                    table = profileModel.getVcgtTable();
                } else {
                    // Formula
                    table = profileModel.getVcgtFormulaTable();
                }
            } else {
                table = emptyTable;
            }
            if(table == null) {
                // if(profile.isChecked()) {
                // Utils.errMsg("VCGT table is not available for "
                // + profile.getFile().getName());
                // }
                table = emptyTable;
            }
            if(table.length == 0) {
                Utils.errMsg("VCGT table is empty");
                table = emptyTable;
            }
            int nComponents = table.length;
            int nEntries = table[0].length;

            // Generate the x values
            double[][] xVals = new double[1][nEntries];
            for(int i = 0; i < nEntries; i++) {
                xVals[0][i] = 255.f * i / (nEntries - 1);
            }

            // Don't plot anything if the profile is not checked
            if(!profile.isChecked()) {
                nEntries = 0;
            }

            // Get the renderer
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)chartPanel
                .getChart().getXYPlot().getRenderer();

            // Calculate the color values intensity
            float val = 1;
            if(nSeries > 1) {
                float fract = 0.8f;
                // Earliest in the list are darker (faded)
                val = fract + iSeries / (nSeries - 1.0f) * (1.0f - fract);
                // Insure it is in range in case there is roundoff
                if(val < 0) {
                    val = 0;
                }
                if(val > 1.0f) {
                    val = 1.0f;
                }
            }

            // Create the series
            int seriesStart = dataset.getSeriesCount();
            // System.out.println("seriesNum=" + iSeries + " nSeries=" + nSeries
            // + " val=" + val + " seriesStart=" + seriesStart);
            XYSeries[] seriesArray = new XYSeries[nComponents];
            for(int i = 0; i < nComponents; i++) {
                XYSeries series = seriesArray[i];
                series = new XYSeries("Series " + (i + 1));
                for(int j = 0; j < nEntries; j++) {
                    series.add(xVals[0][j], table[i][j]);
                }
                dataset.addSeries(series);
                if(nComponents == 1) {
                    renderer.setSeriesPaint(seriesStart, new Color(val, val,
                        val));
                } else {
                    switch(i) {
                    case 0:
                        renderer.setSeriesPaint(seriesStart, new Color(val, 0,
                            0));
                        break;
                    case 1:
                        renderer.setSeriesPaint(seriesStart + 1, new Color(0,
                            val, 0));
                        break;
                    case 2:
                        renderer.setSeriesPaint(seriesStart + 2, new Color(0,
                            0, val));
                        break;
                    }
                }
            }
        } catch(Exception ex) {
            Utils.excMsg("Error adding profile to plot", ex);
        }
    }

    /**
     * Handler for the list. Toggles the checked state.
     * 
     * @param ev
     */
    private void onListItemSelected(ListSelectionEvent ev) {
        if(ev.getValueIsAdjusting()) return;
        Profile profile = (Profile)list.getSelectedValue();
        if(profile == null) {
            return;
        }
        if(profile == null) return;
        if(!profile.hasVcgtTable()) {
            Utils.errMsg("VCGT table is not available for "
                + profile.getFile().getName());
            profile.setChecked(false);
        } else {
            // Toggle the checked state
            profile.setChecked(!profile.isChecked());
        }
        list.clearSelection();
        refresh();
    }

    /**
     * Brings up a JFileChooser to open a file.
     */
    private void open() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Save the selected path for next time
            defaultPath = chooser.getSelectedFile().getParentFile().getPath();
            // Process the file
            File[] files = chooser.getSelectedFiles();
            loadFiles(files);
        }
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * @return The value of sortOrder.
     */
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder The new value for sortOrder.
     */
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Profile is a container to hold an ICC profile file.
     * 
     * @author Kenneth Evans, Jr.
     */
    private class Profile implements Comparable<Profile>
    {
        private File file;
        private ICCProfileModel profileModel = new ICCProfileModel();
        private boolean checked = false;

        public Profile(File file) throws Exception {
            this.file = file;
            profileModel.readProfile(file);
        }

        /**
         * @return The value of checked.
         */
        public boolean isChecked() {
            return checked;
        }

        /**
         * @param checked The new value for checked.
         */
        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        /**
         * @return The value of file.
         */
        public File getFile() {
            return file;
        }

        /**
         * @return The value of profileModel.
         */
        public ICCProfileModel getProfileModel() {
            return profileModel;
        }

        public boolean hasVcgtTable() {
            if(profileModel == null) {
                return false;
            }
            Integer gammaType = profileModel.getVcgtGammaType();
            return gammaType == null ? false : true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(Profile other) {
            // Add error checking
            if(sortOrder == SortOrder.NAME) {
                return file.getName().compareTo(other.getFile().getName());
            } else if(sortOrder == SortOrder.LAST_MOFIFIED) {
                long lastModified = file.lastModified();
                long lastModifiedOther = other.getFile().lastModified();
                if(lastModified > lastModifiedOther) {
                    return 1;
                } else if(lastModified < lastModifiedOther) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

    /**
     * GUI version of main.
     * 
     * @param args
     */
    public static void main(String[] args) {
        final VCGTViewer app = new VCGTViewer();

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
