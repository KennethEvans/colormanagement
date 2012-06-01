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
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * TestICC1: This version is similar to code found on the web.<br>
 * <br>
 * 
 * <a
 * href=http://java-programmer.itgroups.info/1/5/thread-79492.html>http://java
 * -programmer.itgroups.info/1/5/thread-79492.html</a>.<br>
 * <br>
 * 
 * It has not been tested.
 * 
 * @author Kenneth Evans, Jr.
 */
public class TestICC1 extends JPanel
{
    private static final long serialVersionUID = 1L;
    public static final String LS = System.getProperty("line.separator");
    private static final String IMAGE = "";
    private static final String PROFILE = "";

    public TestICC1() {
        uiInit();
    }

    /**
     * Initializes the user interface.
     */
    void uiInit() {
        this.setLayout(null);

        ImageIcon ii = new ImageIcon(IMAGE);
        JLabel srcLabel = new JLabel(ii);
        this.add(srcLabel, BorderLayout.WEST);

        ICC_Profile profiles[] = new ICC_Profile[1];
        // Color2Gray (1)
        // profiles[0] =
        // ICC_ProfileGray.getInstance(ICC_ColorSpace.CS_GRAY);
        // Color2ICC (2)
        try {
            profiles[0] = ICC_Profile.getInstance(new FileInputStream(PROFILE));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        getInfo(profiles[0]);
        BufferedImage src = new BufferedImage(ii.getIconWidth(),
            ii.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        src.getGraphics().drawImage(ii.getImage(), 0, 0, this);
        ColorConvertOp cco = new ColorConvertOp(profiles, null);
        BufferedImage dest = cco.filter(src, null);
        JLabel destLabel = new JLabel(new ImageIcon(dest));
        this.add(destLabel, BorderLayout.EAST);
    }

    // public void TestICC1() {
    // try {
    // JFrame frame = new JFrame("testicc");
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // JPanel main = new JPanel(new BorderLayout());
    // frame.setContentPane(main);
    // ImageIcon ii = new ImageIcon(IMAGE);
    // JLabel srcLabel = new JLabel(ii);
    // main.add(srcLabel, BorderLayout.WEST);
    // frame.pack();
    // frame.setVisible(true);
    // System.out.println("loading profile");
    // ICC_Profile profiles[] = new ICC_Profile[1];
    // // Color2Gray (1)
    // // profiles[0] =
    // // ICC_ProfileGray.getInstance(ICC_ColorSpace.CS_GRAY);
    // // Color2ICC (2)
    // profiles[0] = ICC_Profile.getInstance(new FileInputStream(PROFILE));
    // getInfo(profiles[0]);
    // BufferedImage src = new BufferedImage(ii.getIconWidth(), ii
    // .getIconHeight(), BufferedImage.TYPE_INT_RGB);
    // src.getGraphics().drawImage(ii.getImage(), 0, 0, this);
    // ColorConvertOp cco = new ColorConvertOp(profiles, null);
    // BufferedImage dest = cco.filter(src, null);
    // JLabel destLabel = new JLabel(new ImageIcon(dest));
    // main.add(destLabel, BorderLayout.EAST);
    // frame.pack();
    // } catch(Exception e) {
    // e.printStackTrace();
    // // System.err.println("erreur lecture profile");
    // }
    // }

    public String getInfo(ICC_Profile profile) {
        String info = "";
        if(profile instanceof ICC_ProfileRGB) {
            ICC_ProfileRGB iccPRGB = (ICC_ProfileRGB)profile;
            int i, j;
            for(i = 0; i < iccPRGB.getNumComponents(); i++) {
                info += "GAMMA COMPONENT [" + i + "] [" + iccPRGB.getGamma(i)
                    + "]" + LS;
            }
            float mwp[] = iccPRGB.getMediaWhitePoint();
            for(i = 0; i < mwp.length; i++) {
                info += "WHITE POINT [" + i + "] [" + mwp[i] + "]" + LS;
            }
            float matrice[][] = iccPRGB.getMatrix();
            StringBuffer sb = new StringBuffer();
            info += "MATRIX" + LS;
            for(i = 0; i < matrice.length; i++) {
                sb.setLength(0);
                sb.append("|");
                for(j = 0; j < matrice[i].length; j++) {
                    sb.append(matrice[i][j] + "\t");
                }
                sb.append("|");
                info += sb.toString() + LS;
            }
        }
        return info;
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
            JFrame frame = new JFrame("Midi Synthesizer");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // Add this panel to the frame
            Container contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(this, BorderLayout.CENTER);

            // Display the window
            frame.setBounds(20, 20, 825, 275);
            frame.setVisible(true);
            // printLocations();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
