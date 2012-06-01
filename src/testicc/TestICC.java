package testicc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import misc.AboutBoxEvansPanel;
import misc.HexDump;

/**
 * TestICC: A program to read ICC profiles.<br>
 * <br>
 * This version uses the bytes from reading the file directly and also from
 * reading the profile. It uses static variables from ICC_Profile.<br>
 * <br>
 * The bytes obtained from ICC_Profile.getData() are different from those in the
 * file; however, each is self-consistent. The data from profile.getData() in at
 * least one case (a file made from Adobe RGB) reuses the storage for rTRC,
 * gTRC, and bTRC when they are the same, whereas the file has separate storage
 * for each one. This leads to a different size for the data and different
 * offsets in the tag table. There is a testicc routine to demonstrate this.<br>
 * <br>
 * It has not been tested whether this works on both big endian and little
 * endian. The file is supposed to be written in big endian. <br>
 * <br>
 * The model should be separated from the view.
 * 
 * @author Kenneth Evans, Jr.
 */
public class TestICC extends JPanel
{
    /**
     * Flag to do a testicc of the differences between the file bytes and the
     * ICC_Profile.getBytes().
     */
    private static final Boolean DO_TEST = false;
    private static final Boolean TAG_DATA_FROM_FILE = true;
    private static final Boolean BYTES_AS_NUMBERS = false;
    private static final Boolean BYTES_SHOW_NULL = true;
    public static final String LS = System.getProperty("line.separator");
    /** Format used to convert a byte to a string. */
    private static final String STRING_BYTE_FORMAT = "%02x";
    private static final long serialVersionUID = 1L;
    private static final String title = "Test ICC Profile";
    private static final String DEFAULT_PROFILE = "C:/Windows/System32/spool/drivers/color/AlienwareCustom.icm";
    // private static final String DEFAULT_PROFILE =
    // "C:/Windows/System32/spool/drivers/color/xRite-2010-07-08-6500-2.2-090.ICC";
    private String defaultPath = "C:/Windows/System32/spool/drivers/color";

    private ICC_Profile profile;
    private byte[] data;
    private byte[] profileData;
    private TagTableEntry[] tagsArray;
    private TagTableEntry[] profileTagsArray;

    private String fileName;

    private JTextArea textArea;
    private JMenuBar menuBar;

    public TestICC() {
        uiInit();

        // TODO Fix later to do this optionally for testing
        loadFile(DEFAULT_PROFILE);
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
     * Loads a new file.
     * 
     * @param fileName
     */
    private void loadFile(String fileName) {
        this.fileName = fileName;

        // Get the profile
        try {
            profile = openProfile(fileName);
            profileData = profile.getData();
        } catch(Exception ex) {
            System.out.println("Error getting profile");
            System.out.println(ex.getMessage());
        }

        // Get the file data.
        try {
            data = openFile(fileName);
        } catch(Exception ex) {
            System.out.println("Error opening file");
            System.out.println(ex.getMessage());
        }

        // Make the tags array
        makeFileTagsArray();
        makeProfileTagsArray();

        // Refresh the textArea
        textArea.setText("");
        String info = getInfo(profile);
        textArea.setText(info);
        textArea.setCaretPosition(0);
    }

    /**
     * Reads the file using ICC_Profile.getInstance(). The data would be
     * obtained by ICC_Profile.getData(). The data so obtained seems to be in
     * error and different from what is in the file. Other parts of the
     * ICC_Profile may work.
     * 
     * @param fileName
     * @return The profile.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static ICC_Profile openProfile(String fileName)
        throws FileNotFoundException, IOException {
        ICC_Profile profile = null;
        profile = ICC_Profile.getInstance(new FileInputStream(fileName));
        return profile;
    }

    /**
     * Reads the file using a DataInputStream.
     * 
     * @param fileName
     * @return The bytes in the file.
     * @throws IOException
     */
    public static byte[] openFile(String fileName) throws IOException {
        File file = new File(fileName);
        int len1 = (int)file.length();
        byte[] data = new byte[len1];
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        dis.readFully(data);
        dis.close();
        return data;
    }

    /**
     * Get a String representation of a byte. Uses STRING_BTYE_FORMAT which
     * should be hard-coded as "%2x".
     * 
     * @see #STRING_BYTE_FORMAT
     * 
     * @param b
     * @return
     */
    public static String getStringFromByte(byte b) {
        return String.format(STRING_BYTE_FORMAT, b);
    }

    /**
     * Returns info about this profile.
     * 
     * @param profile
     * @return
     */
    public String getInfo(ICC_Profile profile) {
        String info = "";
        if(fileName == null) {
            info += "No file loaded" + LS;
            return info;

        }
        info += fileName + LS;
        if(profile == null) {
            info += "ICC profile is null" + LS;
            return info;
        }
        // Check if it is an RGB profile
        ICC_ProfileRGB iccRGB = null;
        if(!(profile instanceof ICC_ProfileRGB)) {
            info += "ICC profile is not an RGB profile" + LS;
        } else {
            iccRGB = (ICC_ProfileRGB)profile;
        }
        int i, j, id;
        byte[] bytes;

        // Profile 
        info += "Profile Name: ";
        info += getProfileName(profile) + LS;

        // Size
        info += "Profile size: " + profile.getData().length + LS;
        info += "File size: " + data.length + LS;

        // Version
        info += "Version: " + LS;
        info += "  Using ICC_Profile API: ";
        int major = profile.getMajorVersion();
        int minor = profile.getMinorVersion();
        info += major + "." + minor + LS;

        // Version from profile header
        try {
            info += "  From profile: " + getVersionString(profileData) + LS;
        } catch(Exception ex) {
            info += errMsg("Error reading version from profile", ex);
        }

        // Version from file header
        try {
            info += "  From file: " + getVersionString(data) + LS;
        } catch(Exception ex) {
            info += errMsg("Error reading version from file", ex);
        }

        // Version integer from file header
        try {
            info += "  Integer from file: "
                + String.format("0x%x", getVersionInteger(data)) + LS;
        } catch(Exception ex) {
            info += errMsg("Error reading version from file", ex);
        }

        // Version dump from file
        info += "  Data from file" + LS;
        bytes = getSlice(data, 8, 4);
        info += getDataDump(bytes);
        
        // ProfileDescription
        info += "Description: ";
        if(TAG_DATA_FROM_FILE) {
            info += getTagDataString("desc");
        } else {
            id = ICC_Profile.icSigProfileDescriptionTag;
            info += getTagDataFromProfile(profile, id);
        }
        info += LS;

        // Copyright
        info += "Copyright: ";
        if(TAG_DATA_FROM_FILE) {
            info += getTagDataString("cprt");
        } else {
            id = ICC_Profile.icSigCopyrightTag;
            info += getTagDataFromProfile(profile, id);
        }
        info += LS;

        // Rendering intent
        info += "Rendering Intent: ";
        if(TAG_DATA_FROM_FILE) {
            try {
                info += getRenderingIntent(data);
            } catch(Exception ex) {
                info += errMsg("Error reading rendering intent from file", ex);
            }
        } else {
            try {
                info += getRenderingIntent(profileData);
            } catch(Exception ex) {
                info += errMsg("Error reading rendering intent from file", ex);
            }
        }
        info += LS;

        // Luminance
        info += "Luminance: ";
        if(TAG_DATA_FROM_FILE) {
            bytes = getTagDataFromFile("lumi");
            if(bytes != null) {

            }
        } else {
            id = ICC_Profile.icSigCopyrightTag;
            bytes = profile.getData(id);
        }
        if(bytes == null) {
            info += "lumi tag not found";
        } else {
            double[] vals = null;
            try {
                vals = getXYZType(bytes);
            } catch(IOException ex) {
                info += errMsg("Error getting lumi values", ex);
            }
            if(vals == null) {
                info += "no lumi values found" + LS;
            } else {
                if(false) {
                    // Do all values
                    for(double val : vals) {
                        info += val + " ";
                    }
                } else {
                    // The X and Z values should be zero. Innore them in any
                    // case
                    // and take the Y value.
                    info += vals[1] + " cd/m^2";
                }
            }
        }
        info += LS;

        // The following require an RGB profile
        if(iccRGB != null) {
            // Gamma
            try {
                info += "Gamma: ";
                for(i = 0; i < profile.getNumComponents(); i++) {
                    if(i != 0) {
                        info += ", ";
                    }
                    info += iccRGB.getGamma(i);
                }
                info += LS;
            } catch(Exception ex) {
                info += errMsg("Error getting gamma", ex);
            }

            // White point
            try {
                float mwp[] = iccRGB.getMediaWhitePoint();
                info += "White Point: ";
                for(i = 0; i < mwp.length; i++) {
                    if(i != 0) {
                        info += ", ";
                    }
                    info += mwp[i];
                }
                info += LS;
            } catch(Exception ex) {
                info += errMsg("Error getting white point", ex);
            }

            // Matrix
            try {
                float matrix[][] = iccRGB.getMatrix();
                info += "Matrix:" + LS;
                for(i = 0; i < matrix.length; i++) {
                    for(j = 0; j < matrix[i].length; j++) {
                        info += matrix[i][j] + "\t";
                    }
                    info += LS;
                }
                info += LS;
            } catch(Exception ex) {
                info += errMsg("Error getting matrix", ex);
            }
        }

        // File TagTable
        try {
            info += LS;
            info += "File Tag Table:" + LS;
            info += getFileTagTable();
        } catch(Exception ex) {
            info += errMsg("Error getting file tag table", ex);
        }

        // Profile TagTable
        try {
            info += LS;
            info += "Profile Tag Table:" + LS;
            info += getProfileTagTable();
        } catch(Exception ex) {
            info += errMsg("Error getting profile tag table", ex);
        }

        return info;
    }

    /**
     * Class to manage a tag table entry. TagTableEntry
     * 
     * @author Kenneth Evans, Jr.
     */
    public class TagTableEntry
    {
        // TODO Check use of int vs unsigned
        private String signature;
        private int offset;
        private int length;

        TagTableEntry(String signature, int offset, int length) {
            this.signature = signature;
            this.offset = offset;
            this.length = length;
        }

        /**
         * @return The value of signature.
         */
        public String getSignature() {
            return signature;
        }

        /**
         * @return The value of offset.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * @return The value of length.
         */
        public int getLength() {
            return length;
        }

    }

    /**
     * Make the array of tag table entries from the file.
     */
    public TagTableEntry[] makeTagsArray(byte[] data) {
        ArrayList<TagTableEntry> tagsList = new ArrayList<TagTableEntry>();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(128); // Header
            int nFields = dis.readInt();
            String sig;
            byte[] sigBytes = new byte[4];
            int offset, length;
            for(int i = 0; i < nFields; i++) {
                dis.readFully(sigBytes);
                sig = new String(sigBytes);
                offset = dis.readInt();
                length = dis.readInt();
                tagsList.add(new TagTableEntry(sig, offset, length));
            }
            dis.close();
        } catch(IOException ex) {
            System.out.println("Error making tags array");
            System.out.println(ex.getMessage());
        }
        TagTableEntry[] tagsArray = new TagTableEntry[tagsList.size()];
        tagsList.toArray(tagsArray);
        return tagsArray;
    }

    /**
     * Make the array of tag table entries from the file.
     */
    public void makeFileTagsArray() {
        if(data == null) {
            tagsArray = null;
            return;
        }
        tagsArray = makeTagsArray(data);
    }

    /**
     * Make the array of tag table entries from the profile.
     */
    public void makeProfileTagsArray() {
        if(profile == null) {
            profileTagsArray = null;
            return;
        }
        byte[] data = profile.getData();
        if(data == null) {
            profileTagsArray = null;
            return;
        }
        profileTagsArray = makeTagsArray(data);
    }

    /**
     * Gets the tag table from the given data for the whole file.
     * 
     * @param data Data for the whole file.
     * @param tagArray Tags array (must be derived from the data).
     * @return
     */
    public String getTagTable(byte[] data, TagTableEntry[] tagsArray) {
        String info = "";
        if(data == null) {
            info += "ICC profile data is null" + LS;
            return info;
        }
        if(tagsArray == null) {
            info += "Tags array is null" + LS;
            return info;
        }
        info += "    #\t Sig\t  Offset\t     End" + LS;
        TagTableEntry entry = null;
        for(int i = 0; i < tagsArray.length; i++) {
            entry = tagsArray[i];
            info += String.format("  %3d\t%4s\t%8d\t%8d", i,
                entry.getSignature(), entry.getOffset(), entry.getLength())
                + LS;
        }
        return info;
    }

    /**
     * Gets the tag table using the file data directly, not the profile.
     * 
     * @param data
     * @return
     */
    public String getFileTagTable() {
        if(data == null) {
            tagsArray = null;
            return null;
        }
        return getTagTable(data, tagsArray);
    }

    /**
     * Gets the tag table from the profile.
     * 
     * @param data
     * @return
     */
    public String getProfileTagTable() {
        if(profile == null) {
            profileTagsArray = null;
            return null;
        }
        data = profile.getData();
        if(data == null) {
            profileTagsArray = null;
            return null;
        }
        return getTagTable(data, profileTagsArray);
    }

    /**
     * Gets the data associated with a tag from a profile.
     * 
     * @param profile
     * @param tag
     * @return
     */
    private static String getTagDataFromProfile(ICC_Profile profile, int tag) {
        byte[] bytes = profile.getData(tag);
        if(bytes == null) {
            return null;
        }
        if(BYTES_AS_NUMBERS) {
            String numbers = "";
            for(byte b : bytes) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            return numbers;
        }
        if(BYTES_SHOW_NULL) {
            // Have to do it this way as the String will replace 0 with " "
            for(int i = 0; i < bytes.length; i++) {
                if(bytes[i] == 0) {
                    bytes[i] = '.';
                }
            }
        }
        String string = new String(bytes);
        return string;
    }

    /**
     * Gets the data associated with a tag.
     * 
     * @param profile
     * @param tag
     * @return
     */
    private String getTagDataString(String signature) {
        byte[] bytes = getTagDataFromFile(signature);
        if(bytes == null) {
            return "No tag found for " + signature;
        }
        if(BYTES_AS_NUMBERS) {
            String numbers = "";
            for(byte b : bytes) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            return numbers;
        }
        if(BYTES_SHOW_NULL) {
            // Have to do it this way as the String will replace 0 with " "
            for(int i = 0; i < bytes.length; i++) {
                if(bytes[i] == 0) {
                    bytes[i] = '.';
                }
            }
        }
        String string = new String(bytes);
        return string;
    }

    private byte[] getTagDataFromFile(String signature) {
        byte[] bytes = null;
        // Find the tag in the tagsArray
        for(TagTableEntry entry : tagsArray) {
            if(entry.getSignature().equals(signature)) {
                bytes = getSlice(data, entry.getOffset(), entry.getLength());
                break;
            }
        }
        return bytes;
    }

    public static double[] getXYZType(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        byte[] sigBytes = new byte[4];
        String sig;
        dis.readFully(sigBytes);
        sig = new String(sigBytes);
        if(!sig.equalsIgnoreCase("xyz ")) {
            return null;
        }
        dis.readFully(sigBytes); // Nulls
        int nVals = (bytes.length - 8) / 4;
        double[] dVals = new double[nVals];
        for(int i = 0; i < nVals; i++) {
            // TODO Check that this is right
            dVals[i] = (double)dis.readInt() / 0x10000;
        }
        dis.close();
        return dVals;
    }

    /**
     * Gets the profile name from the given profile.
     * 
     * @param profile The profile.
     * @return
     */
    public static String getProfileName(ICC_Profile profile) {
        String desc = null;
        // Get the ICC profile tag
        // It is a Structure containing invariant and localizable
        // versions of the profile name for display.
        // Bytes 0-3 are "desc". Bytes 4-7 are nulls. Bytes 8-11 are the
        // length of the ASCII invariant profile name. The ASCII invariant
        // part starts at 12 and should end with a null.
        byte[] data = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        if(data != null && data.length > 12) {
            desc = new String(data).substring(12);
            // Find any nulls
            int pos = desc.indexOf('\0');
            if(pos > -1) {
                desc = desc.substring(0, pos);
            }
        }
        return desc;
    }

    /**
     * Gets the version as an array {major, minor, bugFix}.
     * 
     * @param bytes Entire data.
     * @return
     * @throws IOException
     */
    public static int[] getVersion(byte[] bytes) throws IOException {
        byte[] versionBytes = getSlice(bytes, 8, 4);
        if(false) {
            for(byte b : versionBytes) {
                System.out.print(getStringFromByte(b) + " ");
            }
            System.out.println();
        }
        int major = versionBytes[0] & 0xff;
        int minor = (versionBytes[1] & 0xf0) >> 4;
        int bugFix = versionBytes[1] & 0x0f;
        return new int[] {major, minor, bugFix};
    }

    public static String getRenderingIntent(byte[] bytes) throws IOException {
        byte[] numberBytes = getSlice(bytes, 64, 67);
        ByteArrayInputStream bais = new ByteArrayInputStream(numberBytes);
        DataInputStream dis = new DataInputStream(bais);
        int val = dis.readInt(); // Just read the first two bytes
        dis.close();
        switch(val) {
        case 0:
            return "Perceptual";
        case 1:
            return "Relative Colorimetric";
        case 2:
            return "Saturation";
        case 3:
            return "Absolute Colorimetric";
        default:
            return "Invalid [" + val + "]";
        }
    }

    /**
     * Gets the version as an integer.
     * 
     * @param bytes Entire data.
     * @return
     * @throws IOException
     */
    public static int getVersionInteger(byte[] bytes) throws IOException {
        byte[] versionBytes = getSlice(bytes, 8, 4);
        if(false) {
            for(byte b : versionBytes) {
                System.out.print(getStringFromByte(b) + " ");
            }
            System.out.println();
        }
        // We could make this a short and use dis.readShort
        // This seems to duplicate what ICC Profile Inspector does
        int val;
        if(false) {
            // TODO Find why this doesn't work (when val is a short)
            // It is What the Javadoc says it does
            val = (short)((versionBytes[0] << 8) | (bytes[1] & 0xff));
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(versionBytes);
            DataInputStream dis = new DataInputStream(bais);
            val = dis.readInt(); // Just read the first two bytes
            dis.close();
        }
        return val;
    }

    /**
     * Gets the version as a String.
     * 
     * @param bytes Entire data.
     * @return
     * @throws IOException
     */
    public String getVersionString(byte[] bytes) throws IOException {
        int[] val = getVersion(bytes);
        return val[0] + "." + val[1] + "." + val[2];
    }

    /**
     * Get the data as a hex dump.
     * 
     * @param bytes
     * @return
     */
    public String getDataDump(byte[] bytes) {
        return HexDump.getString(bytes);
    }

    /**
     * Gets the slice of the input array starting at start and ending at start +
     * length -1. If length is greater than the length of the data, the array
     * will only extend to the end of the data. If the data is null or start is
     * out of range, then null will be returned.<br>
     * <br>
     * [a,b,c,d], 1, 2 -> [b,c].<br>
     * [a,b,c,d], 1, 1000 -> [b,c,d].<br>
     * 
     * @param data
     * @param start
     * @param length
     * @return
     */
    public static byte[] getSlice(byte[] data, int start, int length) {
        if(data == null) {
            return null;
        }
        int dataLength = data.length;
        if(start >= dataLength) {
            return null;
        }
        if(length >= data.length - start) {
            length = data.length - start - 1;
        }
        if(length < 0) {
            return null;
        }
        byte[] val = new byte[length];
        for(int i = 0; i < length; i++) {
            val[i] = data[start + i];
        }
        return val;
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
            loadFile(file.getPath());
        }
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Makes an error string for an Exception.
     * 
     * @param text Text to be displayed before the exception message.
     * @param ex The exception.
     * @return
     */
    public String errMsg(String text, Exception ex) {
        return text + LS + "  " + ex.getMessage() + LS;
    }

    /**
     * Puts the panel in a JFrame and runs the JFrame.
     * 
     */
    public void run() {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Has to be done here. The menus are not part of the JPanel.
            initMenus();
            frame.setJMenuBar(menuBar);

            // Add this panel to the frame
            Container contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(this, BorderLayout.CENTER);

            // Display the window
            frame.setBounds(20, 20, 600, 800);
            frame.setVisible(true);
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * A static testicc routine to demonstrate the differences obtained from
     * ICC_Profile.getData() and the actual data in the file.
     */
    public static void test() {
        // Open the profile using ICC_Profile
        String fileName = DEFAULT_PROFILE;
        System.out.println("Testing " + fileName);
        System.out.println();
        System.out.println("data is data from ICC_Profile.getData()");
        System.out.println("data1 is the in the file");

        // data is the data from the profile
        byte[] data = null;
        ICC_Profile profile = null;
        try {
            profile = ICC_Profile.getInstance(new FileInputStream(fileName));
            data = profile.getData();
        } catch(IOException ex) {
            System.out.println("Error getting profile");
            System.out.println(ex.getMessage());
        }
        int len = data.length;
        // Open the profile by reading the file
        File file = new File(fileName);
        int len1 = (int)file.length();
        // data1 is the data from the file
        byte[] data1 = new byte[len1];
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(data1);
            dis.close();
        } catch(IOException ex) {
            System.out.println("Error reading file");
            System.out.println(ex.getMessage());
        }
        if(true) {
            System.out.println("\nData Summary");
            System.out.println("data  len=" + len);
            System.out.println("data1 len=" + len1);
            int len0 = (len < len1) ? len : len1;
            int nMismatch = 0;
            // len0 = 128;
            for(int i = 0; i < len0; i++) {
                if(data[i] != data1[i]) {
                    nMismatch++;
                }
            }
            System.out.println("nMismatch=" + nMismatch);
        }
        if(true) {
            System.out.println("\nData for first 128 bytes");
            byte[] temp = getSlice(data, 0, 128);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data     " + numbers);

            temp = getSlice(data1, 0, 120);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data1    " + numbers);
        }
        if(true) {
            System.out.println("\nData for last 128 bytes");
            byte[] temp = getSlice(data, len - 128, 128);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data     " + numbers);

            temp = getSlice(data1, len1 - 128, 128);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data1    " + numbers);
        }
        // Check the table entry for a given entry
        if(true) {
            int nEntry = 15;
            int n = 132 + 12 * nEntry;
            System.out.println("\nBytes for tag table entry " + nEntry);

            System.out.println("data:");
            byte[] temp = getSlice(data, n, 12);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(nEntry + "    " + numbers);

            System.out.println("data1:");
            temp = getSlice(data1, n, 12);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(nEntry + "    " + numbers);
        }
        // Check the data for a given entry
        if(true) {
            // Note this only works for the default file
            int nEntry = 15;
            // Is gTRC for the default file
            int id = ICC_Profile.icSigGreenTRCTag;
            System.out.println("\nData pointed to for tag table entry "
                + nEntry);

            System.out.println("data from getData(id="
                + String.format("%08xh", id) + "):");
            byte[] temp = profile.getData(id);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(nEntry + "    " + numbers);

            // Determine offset and size from bytes for tag table entry
            int offset = 0x0464; // bytes 4-7
            int size = 0x000e; // bytes 8-11
            System.out.println("data by slice at " + offset + " of length "
                + size + ":");
            // Determined by ICC Profile Inspector for gTRC for the default file
            temp = getSlice(data, offset, size);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(nEntry + "    " + numbers);

            offset = 0x0474; // bytes 4-7
            size = 0x000e; // bytes 8-11
            System.out.println("data1 by slice at " + offset + " of length "
                + size + ":");
            // Determined by ICC Profile Inspector for gTRC for the default file
            temp = getSlice(data1, offset, size);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(nEntry + "    " + numbers);

        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if(DO_TEST) {
            test();
            return;
        }
        // Make the job run in the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TestICC app = new TestICC();
                app.run();
            }
        });
    }

}
