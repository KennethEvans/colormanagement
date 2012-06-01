package misc;

public class HexDump
{
    private static final int N_TEST_BYTES = 266;
    public static final String LS = System.getProperty("line.separator");
    /** The length of each line */
    private static final int LINE_LENGTH = 75;
    private byte[] curBytes = new byte[LINE_LENGTH];
    private byte[] bytes;
    private int nBytes;
    private int nLines;
    private String[] lines;

    public HexDump(byte[] bytes) {
        this.bytes = bytes;
        nBytes = bytes.length;
        nLines = nBytes / 16;
        if(bytes.length % 16 != 0) {
            nLines++;
        }
        lines = new String[nLines];
        parseBytes();
    }

    /**
     * Parses all the bytes.
     */
    private void parseBytes() {
        int nLine = 0, nPos = 0;
        for(int i = 0; i < nBytes; i++) {
            nLine = i / 16;
            nPos = i % 16;
            // Start a new line
            if(nPos == 0) {
                if(nLine > 0) {
                    lines[nLine - 1] = new String(curBytes);
                }
                clearCurBytes();
                writeLineNumber(nLine);
            }
            writeByte(bytes[i], nPos);
        }
        // Fill in a partial line
        if(nPos > 0) {
            lines[nLine] = new String(curBytes);
        }
    }

    /**
     * Writes a byte into the appropriate places in the curLine.
     * 
     * @param b
     * @param nPos
     */
    public void writeByte(byte b, int nPos) {
        // Write the hex part
        int offset = 10 + 3 * nPos;
        if(nPos > 7) {
            offset++;
        }
        String string = String.format("%02X:", b);
        for(int i = 0; i < 2; i++) {
            curBytes[offset + i] = (byte)string.charAt(i);
        }
        // Write the ASCII part
        offset = 59 + nPos;
        int c = b & 0xff;
        if(c < 0x20 || c >= 0x100) {
            curBytes[offset] = '.';
        } else {
            curBytes[offset] = b;
        }
    }

    /**
     * Writes the line number at the first of the curLine;
     * 
     * @param nLine
     */
    public void writeLineNumber(int nLine) {
        String string = String.format("%8X:", 16 * nLine);
        for(int i = 0; i < string.length(); i++) {
            curBytes[i] = (byte)string.charAt(i);
        }
    }

    /**
     * Fills the curBytes with spaces.
     */
    private void clearCurBytes() {
        for(int i = 0; i < curBytes.length; i++) {
            curBytes[i] = ' ';
        }
    }

    /**
     * Makes an array of n bytes with values increasing from zero.
     * 
     * @param n
     * @return
     */
    public static byte[] makeTestBytes(int n) {
        byte[] testBytes = new byte[n];
        for(int i = 0; i < n; i++) {
            testBytes[i] = (byte)i;
        }
        return testBytes;
    }

    /**
     * Returns all of the lines as one string;
     * 
     * @return
     */
    public String getString() {
        StringBuffer sb = new StringBuffer();
        for(String line : lines) {
            sb.append(line + LS);
        }
        return sb.toString();
    }

    /**
     * Returns a hex dum string ot the given bytes.
     * 
     * @param bytes
     * @return
     */
    public static String getString(byte[] bytes) {
        HexDump hd = new HexDump(bytes);
        return hd.getString();
    }

    /**
     * @return The value of bytes.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @return The value of nBytes.
     */
    public int getnBytes() {
        return nBytes;
    }

    /**
     * @return The value of nLines.
     */
    public int getnLines() {
        return nLines;
    }

    /**
     * @return The value of lines.
     */
    public String[] getLines() {
        return lines;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        byte[] testBytes = makeTestBytes(N_TEST_BYTES);
        if(true) {
            // Use the static method
            System.out.println(HexDump.getString(testBytes));
        } else {
            // Create an instance
            HexDump hd = new HexDump(testBytes);
            String[] lines = hd.getLines();
            if(false) {
                for(String line : lines) {
                    System.out.println(line);
                }
            } else {
                System.out.println(hd.getString());
            }
        }
        System.out.println("\nAll done");
    }

}
