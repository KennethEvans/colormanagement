/*
 * Created on Jul 8, 2010
 * By Kenneth Evans, Jr.
 */

package testicc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * TestICC2: This version uses ICCProfile.getData(). The data returned is
 * different from what is in the file, and gives errors. There is a testicc that
 * demonstrates this.
 * 
 * @author Kenneth Evans, Jr.
 */
public class TestICC2 extends JPanel
{
    private static final Boolean DO_TEST = true;
    private static final Boolean USE_HEX = true;
    private static final Boolean BYTES_AS_NUMBERS = false;
    private static final Boolean BYTES_SHOW_NULL = true;
    private static final long serialVersionUID = 1L;
    public static final String LS = System.getProperty("line.separator");
    private static final String IMAGE = "";
    private static final String PROFILE = "C:/Windows/System32/spool/drivers/color/AlienwareCustom.icm";
    private ICC_Profile profile;
    private Tag[] tags;

    private class Tag
    {
        private int id;
        private String name;

        Tag(int id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         * @return The value of id.
         */
        public int getId() {
            return id;
        }

        /**
         * @return The value of name.
         */
        public String getName() {
            return name;
        }
    }

    public TestICC2() {
        makeTags();
        uiInit();
    }

    /**
     * Initializes the user interface.
     */
    void uiInit() {
        this.setLayout(new BorderLayout());

        // Create the text area used for output. Request
        // enough space for 5 rows and 30 columns.
        JTextArea textArea = new JTextArea(25, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane, BorderLayout.CENTER);

        profile = openProfile(PROFILE);

        String info = getInfo(profile);
        textArea.setText(info);
        //
        //
        // ImageIcon ii = new ImageIcon(IMAGE);
        // JLabel srcLabel = new JLabel(ii);
        // this.add(srcLabel, BorderLayout.WEST);
        //
        // ICC_Profile profiles[] = new ICC_Profile[1];
        // // Color2Gray (1)
        // // profiles[0] =
        // // ICC_ProfileGray.getInstance(ICC_ColorSpace.CS_GRAY);
        // // Color2ICC (2)
        // try {
        // profiles[0] = ICC_Profile.getInstance(new FileInputStream(PROFILE));
        // } catch(IOException ex) {
        // ex.printStackTrace();
        // }
        // getInfo(profiles[0]);
        // BufferedImage src = new BufferedImage(ii.getIconWidth(), ii
        // .getIconHeight(), BufferedImage.TYPE_INT_RGB);
        // src.getGraphics().drawImage(ii.getImage(), 0, 0, this);
        // ColorConvertOp cco = new ColorConvertOp(profiles, null);
        // BufferedImage dest = cco.filter(src, null);
        // JLabel destLabel = new JLabel(new ImageIcon(dest));
        // this.add(destLabel, BorderLayout.EAST);
    }

    public void TestICCNotImplemented() {
        try {
            JFrame frame = new JFrame("testicc");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel main = new JPanel(new BorderLayout());
            frame.setContentPane(main);
            ImageIcon ii = new ImageIcon(IMAGE);
            JLabel srcLabel = new JLabel(ii);
            main.add(srcLabel, BorderLayout.WEST);
            frame.pack();
            frame.setVisible(true);
            System.out.println("loading profile");
            ICC_Profile profiles[] = new ICC_Profile[1];
            // Color2Gray (1)
            // profiles[0] =
            // ICC_ProfileGray.getInstance(ICC_ColorSpace.CS_GRAY);
            // Color2ICC (2)
            profiles[0] = ICC_Profile.getInstance(new FileInputStream(PROFILE));
            getInfo(profiles[0]);
            BufferedImage src = new BufferedImage(ii.getIconWidth(), ii
                .getIconHeight(), BufferedImage.TYPE_INT_RGB);
            src.getGraphics().drawImage(ii.getImage(), 0, 0, this);
            ColorConvertOp cco = new ColorConvertOp(profiles, null);
            BufferedImage dest = cco.filter(src, null);
            JLabel destLabel = new JLabel(new ImageIcon(dest));
            main.add(destLabel, BorderLayout.EAST);
            frame.pack();
        } catch(Exception e) {
            e.printStackTrace();
            // System.err.println("erreur lecture profile");
        }
    }

    public ICC_Profile openProfile(String fileName) {
        ICC_Profile profile = null;
        try {
            profile = ICC_Profile.getInstance(new FileInputStream(fileName));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        // DEBUG
        if(true) {
            byte[] data = profile.getData();
            int i = 15;
            int n = 132 + 12 * i;
            String numbers = "";
            for(int j = n; j < n + 12; j++) {
                byte b = data[j];
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(i + "    " + numbers);
        }
        return profile;
    }

    public static void test() {
        // Open the profile using ICC_Profile
        String fileName = PROFILE;
        byte[] data = null;
        ;
        ICC_Profile profile = null;
        try {
            profile = ICC_Profile.getInstance(new FileInputStream(fileName));
            data = profile.getData();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        // Open the profile by reading the file
        File file = new File(fileName);
        int len1 = (int)file.length();
        byte[] data1 = new byte[len1];
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(data1);
            dis.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        // Check
        if(true) {
            int i = 15;
            int n = 132 + 12 * i;

            System.out.println("data:");
            byte[] temp = getSlice(data, n, n + 12);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(i + "    " + numbers);

            System.out.println("data1:");
            temp = getSlice(data1, n, n + 12);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(i + "    " + numbers);
        }
        if(true) {
            int len = data.length;
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
            byte[] temp = getSlice(data, 0, 128);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data     " + numbers);

            System.out.println("data1:");
            temp = getSlice(data1, 0, 128);
            numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println("data1    " + numbers);
        }
    }

    public static String getStringFromByte(byte b) {
        // if(hex) {
        // return Integer.toHexString(b & 0xff);
        // } else {
        // return getStringFromByte(b));
        // }
        if(USE_HEX) {
            return String.format("%02x", b);
        } else {
            return String.format("%3d", b);
        }
    }

    public String getInfo(ICC_Profile profile) {
        String info = "";
        if(profile == null) {
            info += "ICC profile is null" + LS;
            return info;
        }
        if(!(profile instanceof ICC_ProfileRGB)) {
            info += "ICC profile is not an RGB profile" + LS;
            return info;
        }
        ICC_ProfileRGB iccRGB = (ICC_ProfileRGB)profile;
        int i, j, id;

        // ProfileDescription
        info += "desc: ";
        id = ICC_Profile.icSigProfileDescriptionTag;
        info += getData(profile, id);
        info += LS;

        // Copyright
        info += "cprt: ";
        id = ICC_Profile.icSigCopyrightTag;
        info += getData(profile, id);
        info += LS;

        // Gamma
        info += "Gamma: ";
        for(i = 0; i < iccRGB.getNumComponents(); i++) {
            if(i != 0) {
                info += ", ";
            }
            info += iccRGB.getGamma(i);
        }
        info += LS;

        // White point
        float mwp[] = iccRGB.getMediaWhitePoint();
        info += "White Point: ";
        for(i = 0; i < mwp.length; i++) {
            if(i != 0) {
                info += ", ";
            }
            info += mwp[i];
        }
        info += LS;

        // Matrix
        float matrix[][] = iccRGB.getMatrix();
        info += "Matrix:" + LS;
        for(i = 0; i < matrix.length; i++) {
            for(j = 0; j < matrix[i].length; j++) {
                info += matrix[i][j] + "\t";
            }
            info += LS;
        }

        // TagTable
        info += "Tag Table:" + LS;
        info += getTagTable(profile);
        info += LS;

        // Tags
        if(false) {
            info += "Tags:" + LS;
            String data;
            for(Tag tag : tags) {
                data = getData(profile, tag.getId());
                if(data != null) {
                    info += tag.getName() + ":" + LS;
                    info += data;
                    info += LS;
                }
            }
        }

        return info;
    }

    public static String getTagTable(ICC_Profile profile) {
        String info = "";
        if(profile == null) {
            info += "ICC profile is null" + LS;
            return info;
        }
        info += "    #\t Sig\t  Offset\t     End" + LS;
        byte[] data = profile.getData();
        // DEBUG
        if(false) {
            int i = 15;
            int n = 132 + 12 * i;
            byte[] temp = getSlice(data, n, n + 12);
            String numbers = "";
            for(byte b : temp) {
                String stringVal = getStringFromByte(b) + " ";
                numbers += stringVal;
            }
            System.out.println(i + "    " + numbers);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(128); // Header
            int nFields = dis.readInt();
            String sig;
            byte[] sigBytes = new byte[4];
            int offset, end;
            for(int i = 0; i < nFields; i++) {
                dis.readFully(sigBytes);
                sig = new String(sigBytes);
                offset = dis.readInt();
                end = dis.readInt();
                info += String.format("  %3d\t%4s\t%8d\t%8d", i, sig, offset,
                    end)
                    + LS;
                // DEBUG
                if(true) {
                    int n = 132 + 12 * i;
                    byte[] temp = getSlice(data, n, n + 12);
                    String numbers = "";
                    for(byte b : temp) {
                        int val = (int)b;
                        String stringVal = Integer.toHexString(val) + " ";
                        numbers += stringVal;
                    }
                    System.out.println(i + "    " + numbers);
                }
            }
            dis.close();
        } catch(IOException ex) {
            info += LS + "Error reading tag table" + LS + ex.getMessage() + LS;
        }
        return info;
    }

    private static String getData(ICC_Profile profile, int tag) {
        byte[] bytes = profile.getData(tag);
        if(bytes == null) {
            return null;
        }
        if(BYTES_AS_NUMBERS) {
            String numbers = "";
            for(byte b : bytes) {
                int val = (int)b;
                String stringVal = Integer.toHexString(val) + " ";
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
     * Gets the slice of the input array starting at start and ending at end -1.
     * [a,b,c,d], 1, 4 -> [b,c,d].
     * 
     * @param data
     * @param start
     * @param end
     * @return
     */
    public static byte[] getSlice(byte[] data, int start, int end) {
        int len = end - start;
        if(len > data.length) {
            len = data.length;
        }
        if(len < 0) {
            return null;
        }
        byte[] val = new byte[len];
        for(int i = 0; i < len; i++) {
            val[i] = data[start + i];
        }
        return val;
    }

    private void makeTags() {
        ArrayList<Tag> tagsList = new ArrayList<Tag>();
        tagsList.add(new Tag(ICC_Profile.icSigAToB0Tag, "A2B0"));
        tagsList.add(new Tag(ICC_Profile.icSigAToB1Tag, "A2B1"));
        tagsList.add(new Tag(ICC_Profile.icSigAToB2Tag, "A2B2"));
        tagsList.add(new Tag(ICC_Profile.icSigBlueColorantTag, "bXYZ"));
        tagsList.add(new Tag(ICC_Profile.icSigBlueTRCTag, "bTRC"));
        tagsList.add(new Tag(ICC_Profile.icSigBToA0Tag, "B2A0"));
        tagsList.add(new Tag(ICC_Profile.icSigBToA1Tag, "B2A1"));
        tagsList.add(new Tag(ICC_Profile.icSigBToA2Tag, "B2A2"));
        tagsList.add(new Tag(ICC_Profile.icSigCalibrationDateTimeTag, "calt"));
        tagsList.add(new Tag(ICC_Profile.icSigCharTargetTag, "targ"));
        tagsList.add(new Tag(ICC_Profile.icSigChromaticityTag, "chrm"));
        tagsList.add(new Tag(ICC_Profile.icSigCrdInfoTag, "crdi"));
        tagsList.add(new Tag(ICC_Profile.icSigCopyrightTag, "cprt"));
        tagsList.add(new Tag(ICC_Profile.icSigDeviceModelDescTag, "dmdd"));
        tagsList.add(new Tag(ICC_Profile.icSigDeviceSettingsTag, "devs"));
        tagsList.add(new Tag(ICC_Profile.icSigGamutTag, "gamt"));
        tagsList.add(new Tag(ICC_Profile.icSigGrayTRCTag, "kTRC"));
        tagsList.add(new Tag(ICC_Profile.icSigGreenColorantTag, "gXYZ"));
        tagsList.add(new Tag(ICC_Profile.icSigGreenTRCTag, "gTRC"));
        tagsList.add(new Tag(ICC_Profile.icSigLuminanceTag, "lumi"));
        tagsList.add(new Tag(ICC_Profile.icSigMeasurementTag, "meas"));
        tagsList.add(new Tag(ICC_Profile.icSigMediaBlackPointTag, "bkpt"));
        tagsList.add(new Tag(ICC_Profile.icSigMediaWhitePointTag, "wtpt"));
        tagsList.add(new Tag(ICC_Profile.icSigNamedColor2Tag, "ncl2"));
        tagsList.add(new Tag(ICC_Profile.icSigOutputResponseTag, "resp"));
        tagsList.add(new Tag(ICC_Profile.icSigPreview0Tag, "pre0"));
        tagsList.add(new Tag(ICC_Profile.icSigPreview1Tag, "pre1"));
        tagsList.add(new Tag(ICC_Profile.icSigPreview2Tag, "pre2"));
        tagsList.add(new Tag(ICC_Profile.icSigProfileDescriptionTag, "desc"));
        tagsList.add(new Tag(ICC_Profile.icSigProfileSequenceDescTag, "pseq"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2CRD0Tag, "psd0"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2CRD1Tag, "psd1"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2CRD2Tag, "psd2"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2CRD3Tag, "psd3"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2CSATag, "ps2s"));
        tagsList.add(new Tag(ICC_Profile.icSigPs2RenderingIntentTag, "ps2i"));
        tagsList.add(new Tag(ICC_Profile.icSigRedColorantTag, "rXYZ"));
        tagsList.add(new Tag(ICC_Profile.icSigRedTRCTag, "rTRC"));
        tagsList.add(new Tag(ICC_Profile.icSigScreeningDescTag, "scrd"));
        tagsList.add(new Tag(ICC_Profile.icSigScreeningTag, "scrn"));
        tagsList.add(new Tag(ICC_Profile.icSigTechnologyTag, "tech"));
        tagsList.add(new Tag(ICC_Profile.icSigUcrBgTag, "bfd "));
        tagsList.add(new Tag(ICC_Profile.icSigViewingCondDescTag, "vued"));
        tagsList.add(new Tag(ICC_Profile.icSigViewingConditionsTag, "view"));
        tags = new Tag[tagsList.size()];
        tagsList.toArray(tags);
    }

    /**
     * Method to put the panel in a JFrame and run the JFrame.
     * 
     */
    public void run() {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame("Test ICC Profile");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Add this panel to the frame
            Container contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(this, BorderLayout.CENTER);

            // Display the window
            frame.setBounds(20, 20, 825, 275);
            frame.setVisible(true);
        } catch(Throwable t) {
            t.printStackTrace();
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
                TestICC2 app = new TestICC2();
                app.run();
            }
        });
    }

}
