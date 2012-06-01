package ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.color.ICC_Profile;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import misc.AboutBoxEvansPanel;
import net.kenevans.imagemodel.ImageModel;
import net.kenevans.imagemodel.ScrolledImagePanel;
import net.kenevans.imagemodel.utils.Utils;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.icc.IccProfileInfo;
import org.apache.sanselan.icc.IccProfileParser;

import testicc.TestICC;

/**
 * SanselanImageViewer: A program to read ICC profiles.<br>
 * <br>
 * ICC profiles are big Endian and Java is also Big Endian, so no conversions
 * are necessary.
 * 
 * @author Kenneth Evans, Jr.
 */
public class SanselanImageViewer extends JFrame
{
    private static final String FILENAME = "C:/users/evans/Pictures/ImageBrowser Test/D7A_0670.jpg";
    // private static final String FILENAME =
    // "C:/users/evans/Pictures/ImageBrowser Test/Breault2.ProPhoto.jpg";
    // private static final String FILENAME =
    // "C:/users/evans/Pictures/Digital Photos/Better/DSC_5895a.jpg";

    public static final boolean USE_GUI = true;
    public static final boolean USE_START_FILE_NAME = true;
    private static final boolean USE_STATUS_BAR = true;
    public static final String LS = System.getProperty("line.separator");
    private static final long serialVersionUID = 1L;
    private static final String title = "Test Sanselan";
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static final int MAIN_PANE_DIVIDER_LOCATION = HEIGHT / 3;
    // private String defaultPath = "C:/users/evans/Pictures/ImageBrowser Test";
    // private String defaultPath =
    // "C:/users/evans/Pictures/Digital Photos/Better";
    private String defaultPath = "C:/users/evans/Pictures";
    private File file;

    private Container contentPane = this.getContentPane();
    private JTextArea textArea;
    private JPanel displayPanel = new JPanel();
    private JPanel textPanel = new JPanel();
    private JPanel mainPanel = new JPanel();
    private ScrolledImagePanel imagePanel = null;
    private JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        displayPanel, textPanel);
    private JMenuBar menuBar;

    private ImageModel imageModel = new ImageModel();

    public SanselanImageViewer() {
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
        imagePanel = new ScrolledImagePanel(imageModel, USE_STATUS_BAR);
        displayPanel.add(imagePanel);

        // Create the text area used for output. Request
        // enough space for 5 rows and 30 columns.
        textArea = new JTextArea(25, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel.setLayout(new BorderLayout());
        textPanel.add(scrollPane, BorderLayout.CENTER);

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
        menuItem.setText("Embedded Profile Info...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                showEmbeddedProfileInfo();
            }
        });
        menu.add(menuItem);

        // Help
        menu = new JMenu();
        menu.setText("Help");
        menuBar.add(menu);

        menuItem = new JMenuItem();
        menuItem.setText("About");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null, new AboutBoxEvansPanel(
                    title), "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        menu.add(menuItem);
    }

    // private BufferedImage getImage(File file) {
    // BufferedImage image = null;
    // try {
    // image = Sanselan.getBufferedImage(file);
    // } catch(IOException ex) {
    // Utils.excMsg("IO Error getting image", ex);
    // } catch(ImageReadException ex) {
    // Utils.excMsg("Error reading image", ex);
    // }
    // return image;
    // }

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
                File file = new File(FILENAME);
                loadFile(file);
            }
        } catch(Throwable t) {
            Utils.excMsg("Error running SanselanImageViewer", t);
        }
    }

    /**
     * The implementation of Sanselan.getICCProfile(File file, Map params)
     * returns null if the profile is sRGB. This version returns the sRGB
     * profile in that case.
     * 
     * @param file
     * @throws ImageReadException
     * @throws IOException
     */
    public static ICC_Profile getICCProfile(File file)
        throws ImageReadException, IOException {
        byte bytes[] = Sanselan.getICCProfileBytes(file);
        if(bytes == null) return null;

        // IccProfileParser parser = new IccProfileParser();
        // IccProfileInfo info = parser.getICCProfileInfo(bytes);
        // if(info.issRGB()) return null;

        ICC_Profile icc = ICC_Profile.getInstance(bytes);
        return icc;
    }

    /**
     * Loads a new file.
     * 
     * @param fileName
     */
    private void loadFile(final File file) {
        this.file = file;
        if(file == null) {
            Utils.errMsg("File is null");
            return;
        }

        // Temporary fix for not getting a wait cursor to work
        setText("Reading " + file.getPath() + Utils.LS + Utils.LS);
        setTitle(file.getName());
        imageModel.reset();

        // Needs to be done this way to allow the text to change before reading
        // the image.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Use the ImageModel for the image since
                    // Sanselan can't handle images from JPEGS
                    imageModel.readImage(file);
                    imagePanel.zoomFitIfLarger();

                    // // Sanselan can't handle images from JPEGS
                    // BufferedImage image = getImage(file);
                    // if(image == null) {
                    // Utils.errMsg("Image is null");
                    // return;
                    // }
                    // imageModel.replaceImage(image);

                    // Show the selected info
                    setTitle(file.getName());
                    setText(file.getPath() + Utils.LS + Utils.LS);
                    showFileInfo();
                    showIccProfileInfo();
                    showMetaDataInfo();
                    showImageInfo();
                } catch(Exception ex) {
                    String msg = "Error loading file: " + file.getPath();
                    final String fullMsg = msg + LS + "Exception: " + ex + LS
                        + ex.getMessage();
                    // Utils.excMsg(msg, ex);
                    appendText(LS + fullMsg);
                } catch(Error err) {
                    String msg = "Error loading file: " + file.getPath();
                    final String fullMsg = msg + LS + "Exception: " + err + LS
                        + err.getMessage();
                    // Utils.excMsg(msg, err);
                    appendText(fullMsg);
                }
            }
        });
    }

    // /**
    // * Loads a new file.
    // *
    // * @param fileName
    // */
    // private void loadFile(File file) {
    // this.file = file;
    // if(file == null) {
    // Utils.errMsg("File is null");
    // return;
    // }
    //
    // this.setTitle(file.getName());
    // setText(file.getPath() + Utils.LS + Utils.LS);
    //
    // // Use the ImageModel for the image since
    // // Sanselan can't handle images from JPEGS
    // imageModel.readImage(file);
    // imagePanel.zoomFitIfLarger();
    //
    // // // Sanselan can't handle images from JPEGS
    // // BufferedImage image = getImage(file);
    // // if(image == null) {
    // // Utils.errMsg("Image is null");
    // // return;
    // // }
    // // imageModel.replaceImage(image);
    //
    // // Show the selected info
    // showIccProfileInfo();
    // showMetaDataInfo();
    // showImageInfo();
    // }

    private void showMetaDataInfo() {
        String info = "Metadata" + LS;
        try {
            info += getMetadataInfo(file) + LS;
            appendText(info);
        } catch(ImageReadException ex) {
            Utils.excMsg("Error reading metadata from file", ex);
        } catch(IOException ex) {
            Utils.excMsg("Error getting metadata from file", ex);
        }
    }

    private void showFileInfo() {
        if(file == null) {
            return;
        }
        String info = "File Information" + LS;
        info += getFileInfo(file) + LS;
        appendText(info);
    }

    private void showIccProfileInfo() {
        String info = "ICC Profile" + LS;
        try {
            info += getIccProfileInfo(file) + LS;
            appendText(info);
        } catch(ImageReadException ex) {
            Utils.excMsg("Error reading ICC profile from file", ex);
        } catch(IOException ex) {
            Utils.excMsg("Error getting ICC profile from file", ex);
        }
    }

    private void showImageInfo() {
        String info = "Image Info" + LS;
        try {
            info += getImageInfo(file) + LS;
            appendText(info);
        } catch(ImageReadException ex) {
            Utils.excMsg("Error reading Image Info from file", ex);
        } catch(IOException ex) {
            Utils.excMsg("Error getting Image Info from file", ex);
        }
    }

    /**
     * Returns info about this file.
     * 
     * @param profile
     * @return
     */
    public String getInfo() {
        String info = "";
        info = imageModel.getInfo();
        return info;
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
            // Set the cursor in case it takes a long time
            // This isn't working. The cursor apparently doesn't get set
            // until after it is done.
            Cursor oldCursor = getCursor();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadFile(file);
            } finally {
                setCursor(oldCursor);
            }
        }
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Gets information from Sanselan.getMetadata.
     * 
     * @param file
     * @return
     * @throws ImageReadException
     * @throws IOException
     */
    public static String getMetadataInfo(File file) throws ImageReadException,
        IOException {
        String info = "";
        // Get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        // org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
        IImageMetadata metadata = Sanselan.getMetadata(file);
        if(metadata == null) {
            return "  Metadata is null" + LS;
        }
        ArrayList<?> items = metadata.getItems();
        if(items == null) {
            info += "  Metadata items is null" + LS;
        }
        info += "There are " + items.size() + " metadata items" + LS;
        for(int i = 0; i < items.size(); i++) {
            Item item = (Item)items.get(i);
            if(item instanceof TiffImageMetadata.Item) {
                TiffImageMetadata.Item item1 = (TiffImageMetadata.Item)items
                    .get(i);
                info += "  TIFF " + item1 + LS;
            } else if(item instanceof ImageMetadata.Item) {
                ImageMetadata.Item item1 = (ImageMetadata.Item)items.get(i);
                info += "  IMAGE " + item1 + LS;
            } else {
                info += "  " + item.getClass().getName() + " " + item + LS;
            }
        }
        // if(metadata instanceof JpegImageMetadata) {
        // JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;
        // metadata.
        // ArrayList<?> items = jpegMetadata.getItems();
        // if(items == null) {
        // info += "Metadata items is null" + LS;
        // }
        // info += "There are " + items.size() + " metadata items" + LS;
        // for(int i = 0; i < items.size(); i++) {
        // Item item = (Item)items.get(i);
        // if(item instanceof TiffImageMetadata.Item) {
        // TiffImageMetadata.Item item1 = (TiffImageMetadata.Item)items
        // .get(i);
        // info += "  TIFF " + item1 + LS;
        // } else {
        // info += "  " + item + LS;
        // }
        // }
        // }
        return info;
    }

    /**
     * Gets file information from the given file.
     * 
     * @param file
     * @return
     */
    public static String getFileInfo(File file) {
        String info = "";
        if(file == null) {
            return info;
        }
        long length = file.length();
        info += String.format(
            "  Length: %d Bytes = %.2f KB = %.2f MB = %.2f GB" + LS, length,
            length / 1024., length / 1024. / 1024.,
            length / 1024. / 1024. / 1024.);
        long lastModified = file.lastModified();
        info += "  Last Modified: " + new Date(lastModified) + LS;
        return info;
    }

    /**
     * Gets information from Sanselan.getICCProfile.
     * 
     * @param file
     * @return
     * @throws ImageReadException
     * @throws IOException
     */
    public static String getIccProfileInfo(File file)
        throws ImageReadException, IOException {
        String info = "";
        // Get information from the profile
        ICC_Profile profile = SanselanImageViewer.getICCProfile(file);
        if(profile == null) {
            info += "  Embedded Profile: null" + LS;
            return info;
        }
        info += "  Embedded Profile: " + TestICC.getProfileName(profile) + LS;
        info += "  Rendering Intent: "
            + TestICC.getRenderingIntent(profile.getData()) + LS;
        info += "  Version: " + profile.getMajorVersion() + "."
            + profile.getMinorVersion() + LS;

        // Get information from the Sanselan iccProfileInfo
        byte bytes[] = Sanselan.getICCProfileBytes(file);
        if(bytes != null) {
            IccProfileParser parser = new IccProfileParser();
            IccProfileInfo iccProfileInfo = parser.getICCProfileInfo(bytes);
            if(iccProfileInfo != null) {
                // KLUDGE
                // iccProfileInfo.toString() prints to System.out
                // Get around that by redirecting to a temporary PrintStream
                // Doesn't work to set System.out to null
                // (System.out.println() gives exception in that case)
                PrintStream oldOut = System.out;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                System.setOut(ps);
                String infoString = iccProfileInfo.toString();
                ps.close();
                System.setOut(oldOut);
                // End KLUDGE
                String[] tokens = infoString.split(LS);
                for(String token : tokens) {
                    info += "  " + token + LS;
                }
            }
        }
        return info;
    }

    /**
     * Gets information from Sanselan.getImageInfo.
     * 
     * @param file
     * @return
     * @throws ImageReadException
     * @throws IOException
     */
    public static String getImageInfo(File file) throws ImageReadException,
        IOException {
        String info = "";
        ImageInfo imageInfo = Sanselan.getImageInfo(file);
        if(imageInfo == null) {
            info += "  ImageInfo is null" + LS;
            return info;
        } else {
            String infoString = imageInfo.toString();
            String[] tokens = infoString.split(LS);
            for(String token : tokens) {
                info += "  " + token + LS;
            }
        }
        return info;
    }

    /**
     * Brings up a JFrame to show the embedded profile info if there is any.
     */
    public void showEmbeddedProfileInfo() {
        try {
            ICC_Profile profile = SanselanImageViewer.getICCProfile(file);
            if(profile == null) {
                Utils.errMsg("There is no embedded profile");
                return;
            }
            byte[] data = profile.getData();
            ICCProfileViewer profileViewer = new ICCProfileViewer();
            profileViewer.run(data);
        } catch(ImageReadException ex) {
            Utils.excMsg("Error reading embedded profile", ex);
            return;
        } catch(IOException ex) {
            Utils.excMsg("Error getting embedded profile", ex);
            return;
        }
    }

    /**
     * Console version of main.
     * 
     * @param args
     */
    public static void main1(String[] args) {
        final File file = new File(FILENAME);
        //        final SanselanImageViewer app = new SanselanImageViewer();

        System.out.print(file.getPath() + LS + LS);

        if(true) {
            // MetaData Example
            String info = "Metadata Example" + LS;
            try {
                info += getMetadataInfo(file) + LS;
                System.out.print(info);
            } catch(ImageReadException ex) {
                ex.printStackTrace();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        if(true) {
            // ICC Profile Example
            String info = "ICC Profile Example" + LS;
            try {
                info += getIccProfileInfo(file);
                System.out.print(info);
            } catch(ImageReadException ex) {
                ex.printStackTrace();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * GUI version of main.
     * 
     * @param args
     */
    public static void main(String[] args) {
        final SanselanImageViewer app = new SanselanImageViewer();

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
