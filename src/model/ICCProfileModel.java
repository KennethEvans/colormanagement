package model;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.awt.color.ProfileDataException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import misc.HexDump;
import net.kenevans.imagemodel.utils.Utils;

public class ICCProfileModel
{
    public static final String LS = System.getProperty("line.separator");
    /**
     * Represent bytes as numbers in a string using this format. Used in
     * getNumberFromByte.
     * 
     * @see #getNumberFromByte
     */
    private static final String STRING_BYTE_FORMAT = "%02x";
    /**
     * Show bytes in strings as numbers using STRING_BYTE_FORMAT. Use for
     * debugging. Used in getTagDataString.
     * 
     * @see #getTagDataString
     * @see #STRING_BYTE_FORMAT
     */
    public Boolean BYTES_AS_NUMBERS = false;
    /**
     * Convert 0 bytes in Strings to ".". Otherwise that show as " ".
     */
    public Boolean BYTES_SHOW_NULL = true;
    /**
     * Adobe VCGT tables appear to be written as though the element size is 2,
     * rather than 1 as the nSize field states. Use this to use 2 for the size.
     */
    public boolean FIX_ADOBE_VCGT_SIZE = true;
    /**
     * Use to determine the digits after the decimal point to use in showing
     * tables, such as the Tone Response Curve ot VCGT. -1 indicates to just
     * show the number.
     */
    /**
     * Format the VCGT table as a column for each component rather than a row
     * for each component.
     */
    public int N_VCGT_FORMULA_POINTS = 255;
    public boolean FORMAT_VCGT_TABLE_AS_COLUMNS = false;
    public int showTableValuesPrecision = 4;

    private File file;
    private byte[] data;
    private ICC_Profile profile;
    private TagTableEntry[] tagsArray;

    // Getters and setters

    /**
     * @return The value of file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file The new value for file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return The value of data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data The new value for data.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return The value of profile.
     */
    public ICC_Profile getProfile() {
        return profile;
    }

    /**
     * @param profile The new value for profile.
     */
    public void setProfile(ICC_Profile profile) {
        this.profile = profile;
    }

    /**
     * @return The value of tagsArray.
     */
    public TagTableEntry[] getTagsArray() {
        return tagsArray;
    }

    /**
     * @param tagsArray The new value for tagsArray.
     */
    public void setTagsArray(TagTableEntry[] tagsArray) {
        this.tagsArray = tagsArray;
    }

    // Profile routines

    /**
     * Reads an ICC profile from the given file and sets the variables for this
     * instance.
     * 
     * @param file
     * @throws Exception
     */
    public void readProfile(File file) throws Exception {
        this.file = file;
        if(file == null) return;
        if(file.length() == 0) {
            Utils.warnMsg("File is empty");
            return;
        }

        // Get the profile
        this.profile = openProfile(file);
        this.data = profile.getData();
        // Make the tags array
        makeFileTagsArray();
    }

    /**
     * Reads an ICC profile from the given data and sets the variables for this
     * instance.
     * 
     * @param data Profile data.
     * @throws Exception
     */
    public void readProfile(byte[] data) throws Exception {
        // Set the file to null
        this.file = null;

        // Get the profile
        profile = ICC_Profile.getInstance(data);
        // This should be the same as the input data
        // TODO check this
        this.data = profile.getData();
        // Make the tags array
        makeFileTagsArray();
    }

    /**
     * Reads the file with the given filename using ICC_Profile.getInstance()
     * and returns the profile.
     * 
     * @param fileName
     * 
     * @param fileName The file name of the file to be read.
     * @return The ICC_profile or null on failure.
     * @throws IOException
     */
    public static ICC_Profile openProfile(String fileName) throws IOException {
        File file = new File(fileName);
        return openProfile(file);
    }

    /**
     * Reads the given file using ICC_Profile.getInstance() and returns the
     * profile.
     * 
     * @param file The file to read.
     * 
     * @param fileName The file name of the file to be read.
     * @return The ICC_profile or null on failure.
     * @throws IOException
     */
    public static ICC_Profile openProfile(File file) throws IOException {
        ICC_Profile profile = null;
        profile = ICC_Profile.getInstance(new FileInputStream(file));
        return profile;
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
            Utils.excMsg("Error making tags array", ex);
            return null;
        }
        TagTableEntry[] tagsArray = new TagTableEntry[tagsList.size()];
        tagsList.toArray(tagsArray);
        return tagsArray;
    }

    /**
     * Make the array of tag table entries from the data.
     */
    public void makeFileTagsArray() {
        if(data == null) {
            tagsArray = null;
            return;
        }
        tagsArray = makeTagsArray(data);
    }

    /**
     * Gets the tag table from the given data..
     * 
     * @param data Data for the whole profile.
     * @param tagArray Tags array (must be derived from the data).
     * @return
     */
    public static String getTagTable(byte[] data, TagTableEntry[] tagsArray) {
        String info = "";
        if(data == null) {
            info += "ICC profile data is null" + LS;
            return info;
        }
        if(tagsArray == null) {
            info += "Tags array is null" + LS;
            return info;
        }
        info += "    #\t Sig\t  Offset\t     Len" + LS;
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
     * Gets the tag table for this instance.
     * 
     * @param data
     * @return
     */
    public String getTagTable(byte[] data) {
        if(data == null) {
            tagsArray = null;
            return null;
        }
        return getTagTable(data, tagsArray);
    }

    // /**
    // * Gets the data associated with a tag from a profile.
    // *
    // * @param profile
    // * @param tag
    // * @return
    // */
    // public static String getTagDataFromProfile(ICC_Profile profile, int tag)
    // {
    // byte[] bytes = profile.getData(tag);
    // if(bytes == null) {
    // return null;
    // }
    // if(BYTES_AS_NUMBERS) {
    // String numbers = "";
    // for(byte b : bytes) {
    // String stringVal = getStringFromByte(b) + " ";
    // numbers += stringVal;
    // }
    // return numbers;
    // }
    // if(BYTES_SHOW_NULL) {
    // // Have to do it this way as the String will replace 0 with " "
    // for(int i = 0; i < bytes.length; i++) {
    // if(bytes[i] == 0) {
    // bytes[i] = '.';
    // }
    // }
    // }
    // String string = new String(bytes);
    // return string;
    // }

    /**
     * Gets the data associated with a tag.
     * 
     * @param profile
     * @param tag
     * @return
     */
    public String getTagDataString(String signature) {
        byte[] bytes = getTagData(signature);
        if(bytes == null) {
            return "No tag found for " + signature;
        }
        if(BYTES_AS_NUMBERS) {
            String numbers = "";
            for(byte b : bytes) {
                String stringVal = getNumberFromByte(b) + " ";
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
     * Get the tag data for the given signature for this instance.
     * 
     * @param signature
     * @return
     */
    private byte[] getTagData(String signature) {
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

    /**
     * Get the tag data for the given signature in the given tag table for the
     * given data.
     * 
     * @param data
     * @param tagsArray The tag table to use.
     * @param signature The signature.
     * @return
     */
    public static byte[] getTagData(byte[] data, TagTableEntry[] tagsArray,
        String signature) {
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

    /**
     * Gets the tag from the first 4 bytes of the given bytes.
     * 
     * @param bytes The bytes corresponding to a signature.
     * @return
     */
    public static String getTag(byte[] bytes) {
        String tag = new String(bytes).substring(0, 4);
        return tag;
    }

    /**
     * Get the data as a hex dump.
     * 
     * @param bytes Some bytes.
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
     * Parses an XYZType ('xyz '). The XYZType contains an array of three
     * encoded values for PCSXYZ, CIEXYZ, or nCIEXYZ values. The number of sets
     * of values is determined from the size of the tag.
     * 
     * @param bytes The bytes corresponding to the XYZType.
     * @return An array of the values.
     * @throws IOException
     */
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
     * Get a String number representation of a byte. Uses STRING_BTYE_FORMAT
     * which should be hard-coded as "%2x".
     * 
     * @see #STRING_BYTE_FORMAT
     * 
     * @param b
     * @return
     */
    public static String getNumberFromByte(byte b) {
        return String.format(STRING_BYTE_FORMAT, b);
    }

    /**
     * Gets a string from a textType (text), profileDescriptionTag (desc), or
     * multiLocalizedUnicodeType (mluc). Used for desc, cprt, dmnd, dmdd, and
     * others.
     * 
     * @param bytes The bytes corresponding to a signature.
     * @return
     */
    public static String getStringType(byte[] bytes) {
        if(bytes == null) {
            return "Not found";
        }
        String tag = new String(bytes).substring(0, 4);
        String string;
        if(tag.equals("text")) {
            // This method seems to work most of the time. It just starts at
            // byte 8 and finds the first null-terminated string.
            string = "Cannot parse";
            if(bytes != null && bytes.length > 8) {
                string = new String(bytes).substring(8);
                // Find any nulls
                int pos = string.indexOf('\0');
                if(pos > -1) {
                    string = string.substring(0, pos);
                }
            }
        } else if(tag.equals("desc")) {
            // This method seems to work most of the time. It just starts at
            // byte 12 and finds the first null-terminated string. It ignores
            // any other unicode strings.
            string = "Cannot parse";
            if(bytes != null && bytes.length > 12) {
                string = new String(bytes).substring(12);
                // Find any nulls
                int pos = string.indexOf('\0');
                if(pos > -1) {
                    string = string.substring(0, pos);
                }
            }
        } else if(tag.equals("mluc")) {
            string = "MultiLocalizedUnicodeType: (not handled yet)";
        } else {
            string = "Unknown tag type: " + tag;
        }
        return string;
    }

    /**
     * Gets the profile name for this instance.
     * 
     * @return
     */
    public String getProfileName() {
        return getStringType(getTagData("desc"));
    }

    /**
     * Gets the display name for this instance. The name is the file path name
     * if there is a file and the profile name otherwise. Use getProfileName to
     * get the profile name independent of the file.
     * 
     * @return
     * @see #getProfileName
     */
    public String getDisplayName() {
        String name = null;
        if(file != null) {
            name = file.getPath();
        } else {
            name = getStringType(getTagData("desc"));
        }
        return name;
    }

    /**
     * Gets the short display name for this instance. The name is the file short
     * name if there is a file and the profile name otherwise. Use
     * getProfileName to get the profile name independent of the file.
     * 
     * @return
     * @see #getProfileName
     */
    public String getShortDisplayName() {
        String name = null;
        if(file != null) {
            name = file.getName();
        } else {
            name = getStringType(getTagData("desc"));
        }
        return name;
    }

    /**
     * Gets the version as an array {major, minor, bugFix}.
     * 
     * @param data Entire data.
     * @return
     * @throws IOException
     */
    public static int[] getVersion(byte[] data) throws IOException {
        byte[] versionBytes = getSlice(data, 8, 4);
        // // Debug
        // if(false) {
        // for(byte b : versionBytes) {
        // System.out.print(getStringFromByte(b) + " ");
        // }
        // System.out.println();
        // }
        int major = versionBytes[0] & 0xff;
        int minor = (versionBytes[1] & 0xf0) >> 4;
        int bugFix = versionBytes[1] & 0x0f;
        return new int[] {major, minor, bugFix};
    }

    /**
     * Gets the version as an integer.
     * 
     * @param data Entire data.
     * @return
     * @throws IOException
     */
    public static int getVersionInteger(byte[] data) throws IOException {
        byte[] versionBytes = getSlice(data, 8, 4);
        // // DEBUG
        // if(false) {
        // for(byte b : versionBytes) {
        // System.out.print(getStringFromByte(b) + " ");
        // }
        // System.out.println();
        // }
        // We could make this a short and use dis.readShort
        // This seems to duplicate what ICC Profile Inspector does
        int val;
        if(false) {
            // TODO Find why this doesn't work (when val is a short)
            // It is What the Javadoc says it does
            val = (short)((versionBytes[0] << 8) | (data[1] & 0xff));
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(versionBytes);
            DataInputStream dis = new DataInputStream(bais);
            val = dis.readInt(); // Just read the first four bytes
            dis.close();
        }
        return val;
    }

    /**
     * Gets the version as a String.
     * 
     * @param data Entire data.
     * @return
     * @throws IOException
     */
    public String getVersionString(byte[] data) throws IOException {
        int[] val = getVersion(data);
        return val[0] + "." + val[1] + "." + val[2];
    }

    /**
     * Gets the profile / device class.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getProfileDeviceClass(byte[] data) {
        byte[] bytes = getSlice(data, 12, 15);
        return getTag(bytes);
    }

    /**
     * Gets the profile / device class.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getColorSpace(byte[] data) {
        byte[] bytes = getSlice(data, 16, 19);
        return getTag(bytes);
    }

    /**
     * Gets the profile connection space.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getProfileConnectionSpace(byte[] data) {
        byte[] bytes = getSlice(data, 20, 23);
        return getTag(bytes);
    }

    /**
     * Gets the primary platform.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getPrimaryPlatform(byte[] data) {
        byte[] bytes = getSlice(data, 40, 43);
        return getTag(bytes);
    }

    /**
     * Gets the rendering intent.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getRenderingIntent(byte[] data) throws IOException {
        byte[] numberBytes = getSlice(data, 64, 67);
        ByteArrayInputStream bais = new ByteArrayInputStream(numberBytes);
        DataInputStream dis = new DataInputStream(bais);
        int val = dis.readInt(); // Just read the first four bytes
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
     * Gets the creator.
     * 
     * @param data All of the data.
     * @return
     * @throws IOException
     */
    public static String getCreator(byte[] data) {
        byte[] bytes = getSlice(data, 80, 83);
        return getTag(bytes);
    }

    /**
     * Gets the VCGT gamma type (0 for table, 1 for formula).
     * 
     * @return The type or null on error.
     */
    public Integer getVcgtGammaType() {
        byte[] bytes = getTagData("vcgt");
        if(bytes == null) {
            return null;
        }
        int gammaType;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(8); // tag, 4 reserved
            gammaType = dis.readInt();
            return gammaType;
        } catch(IOException ex) {
            Utils.excMsg("Error getting VGCT gamma type", ex);
            return null;
        } finally {
            try {
                if(dis != null) {
                    dis.close();
                }
            } catch(Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Get the VCGT table parameters.
     * <ul>
     * <li>nChannels: Number of channels (1 or 3)</li>
     * <li>nEntries: Number of entries per channel</li>
     * <li>nSize: Size of each entry</li>
     * </ul>
     * 
     * @return Array {nChannels, nEntries, nSize} or null on failure.
     */
    public int[] getVcgtTableParameters() {
        byte[] bytes = getTagData("vcgt");
        if(bytes == null) {
            return null;
        }
        int gammaType, nChannels, nEntries, nSize;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(8); // tag, 4 reserved
            gammaType = dis.readInt();
            if(gammaType != 0) {
                // Is a formula, not a table
                return null;
            }
            nChannels = dis.readShort();
            nEntries = dis.readShort();
            nSize = dis.readShort();
            return new int[] {nChannels, nEntries, nSize};
        } catch(IOException ex) {
            Utils.excMsg("Error making tags array", ex);
            return null;
        } finally {
            try {
                if(dis != null) {
                    dis.close();
                }
            } catch(Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Get the VCGT table. Entries are normalized to be in the range [0, 255].
     * 
     * @return The table or null on failure.
     */
    public double[][] getVcgtTable() {
        double[][] table = null;
        byte[] bytes = getTagData("vcgt");
        if(bytes == null) {
            return null;
        }
        int gammaType, nChannels, nEntries, nSize;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(8); // tag, 8 reserved
            gammaType = dis.readInt();
            if(gammaType != 0) {
                // Is a formula, not a table
                return null;
            }
            nChannels = dis.readShort();
            nEntries = dis.readShort();
            nSize = dis.readShort();
            if(nSize != 1 && nSize != 2) {
                Utils.errMsg("Cannot handle table with entries of size "
                    + nSize);
                return null;
            }
            table = new double[nChannels][nEntries];
            boolean fixAdobeSize = false;
            // Adobe RGB makes profiles with the wrong size parameter. They are
            // really nSize = 2.
            // Note that bytes.length comes in as 1584 in this case even though
            // the length in the tag table is 1584.
            // System.out.println("bytes.length=" + bytes.length);
            if(FIX_ADOBE_VCGT_SIZE && bytes.length == 1583 && nChannels == 3
                && nEntries == 256 && nSize == 1) {
                fixAdobeSize = true;
            }
            if(nSize == 2 || fixAdobeSize) {
                for(int i = 0; i < nChannels; i++) {
                    for(int j = 0; j < nEntries; j++) {
                        // Normalize to be in the range 0-255
                        table[i][j] = (dis.readShort() & 0xFFFF)
                            / (double)(0xFFFF) * 255.;
                    }
                }
            } else if(nSize == 1) {
                for(int i = 0; i < nChannels; i++) {
                    for(int j = 0; j < nEntries; j++) {
                        // Values in the range 0-255
                        table[i][j] = dis.read();
                    }
                }
            }
            return table;
        } catch(IOException ex) {
            Utils.excMsg("Error making tags array", ex);
            return null;
        } finally {
            try {
                if(dis != null) {
                    dis.close();
                }
            } catch(Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Get the VCGT formula parameters.
     * <ul>
     * <li>rGamma</li>
     * <li>rMin</li>
     * <li>rMax</li>
     * <li>gGamma</li>
     * <li>gMin</li>
     * <li>gMax</li>
     * <li>bGamma</li>
     * <li>bMin</li>
     * <li>bMax</li>
     * </ul>
     * 
     * @return Array {rGamma, rMin, rMax, gGamma, gMin, gMax, gGamma, gMin,
     *         gMax} or null on failure.
     */
    public float[] getVcgtFormulaParameters() {
        byte[] bytes = getTagData("vcgt");
        if(bytes == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            dis.skip(8); // tag, 4 reserved
            Integer gammaType = dis.readInt();
            if(gammaType != 1) {
                // Is a table, not a formula
                return null;
            }
            float rGamma = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float rMin = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float rMax = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float gGamma = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float gMin = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float gMax = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float bGamma = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float bMin = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            float bMax = (dis.readInt() & 0xFFFFFFFF) / 65536.f;
            return new float[] {rGamma, rMin, rMax, gGamma, gMin, gMax, bGamma,
                bMin, bMax};
        } catch(IOException ex) {
            Utils.excMsg("Error making VCGT table array", ex);
            return null;
        } finally {
            try {
                if(dis != null) {
                    dis.close();
                }
            } catch(Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Get a VCGT table of calculated values for a formula. Entries are
     * normalized to be in the range [0, 255].
     * 
     * @return The table or null on failure.
     */
    public double[][] getVcgtFormulaTable() {
        double[][] table = null;
        float[] params = getVcgtFormulaParameters();
        if(params == null) {
            return null;
        }
        double[] gamma = new double[3];
        double[] min = new double[3];
        double[] max = new double[3];
        gamma[0] = params[0];
        min[0] = params[1];
        max[0] = params[2];
        gamma[1] = params[3];
        min[1] = params[4];
        max[1] = params[5];
        gamma[2] = params[6];
        min[2] = params[7];
        max[2] = params[8];
        int nChannels = 3; // Fixed
        int nEntries = N_VCGT_FORMULA_POINTS;
        table = new double[nChannels][nEntries];
        double input;
        for(int j = 0; j < nEntries; j++) {
            input = (double)j / (double)(nEntries - 1);
            for(int i = 0; i < nChannels; i++) {
                // Values in the range 0-255
                table[i][j] = 255. * (min[i] + Math.pow(input, gamma[i])
                    * (max[i] - min[i]));
            }
        }
        return table;
    }

    /**
     * Makes an error string for an Exception.
     * 
     * @param text Text to be displayed before the exception message.
     * @param ex The exception.
     * @return
     */
    public String stringExcMsg(String text, Exception ex) {
        return text + LS + "  " + ex.getMessage() + LS;
    }

    /**
     * Returns info about this profile.
     * 
     * @param profile
     * @return
     */
    public String getInfo() {
        String info = "";
        if(profile == null) {
            info += "ICC profile is null" + LS;
            return info;
        }

        if(file != null) {
            info += file.getPath() + LS;
        }

        // info += "Profile Name: " + getProfileName(profile) + LS;
        info += "Profile Name: " + getStringType(getTagData("desc")) + LS;
        info += "Copyright: " + getStringType(getTagData("cprt")) + LS;
        info += "Manufacturer: " + getStringType(getTagData("dmnd")) + LS;
        info += "Manufacturer Model: " + getStringType(getTagData("dmdd")) + LS;

        // Check if it is an RGB profile
        ICC_ProfileRGB iccRGB = null;
        if(!(profile instanceof ICC_ProfileRGB)) {
            info += "ICC profile is not an RGB profile" + LS;
        } else {
            iccRGB = (ICC_ProfileRGB)profile;
        }
        int i, j;
        byte[] bytes;

        // Size
        info += "Profile size: " + profile.getData().length + LS;
        if(file != null) {
            info += "File size: " + file.length() + LS;
        }

        // Version
        info += "Version: " + profile.getMajorVersion() + "."
            + profile.getMinorVersion() + LS;

        // Version integer from header
        try {
            info += "  Version integer: "
                + String.format("0x%x", getVersionInteger(data)) + LS;
        } catch(Exception ex) {
            info += stringExcMsg("Error reading version", ex);
        }

        // Version dump from data
        info += "  Version data: ";
        bytes = getSlice(data, 8, 4);
        info += getDataDump(bytes);

        // // ProfileDescription
        // info += "Description: ";
        // info += getTagDataString("desc");
        // info += LS;

        // Rendering intent
        info += "Rendering Intent: ";
        try {
            info += getRenderingIntent(data);
        } catch(Exception ex) {
            info += stringExcMsg("Error reading rendering intent", ex);
        }
        info += LS;

        // Luminance
        info += "Luminance: ";
        bytes = getTagData("lumi");
        if(bytes != null) {

        }
        if(bytes == null) {
            info += "Tag not found [lumi]";
        } else {
            double[] vals = null;
            try {
                vals = getXYZType(bytes);
            } catch(IOException ex) {
                info += stringExcMsg("Error getting lumi values", ex);
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

        // Primary platform
        info += "Profile / Device Class: " + getProfileDeviceClass(data) + LS;

        // Color Space
        info += "Color Space: " + getColorSpace(data) + LS;

        // Profile Connection Space
        info += "Profile Connection Space: " + getProfileConnectionSpace(data)
            + LS;

        // Primary platform
        info += "Primary Platform: " + getPrimaryPlatform(data) + LS;

        // Creator
        info += "Creator: " + getCreator(data) + LS;

        // The following require an RGB profile
        if(iccRGB != null) {
            // Get gamma or tone response curve
            boolean doGamma = true;
            // See if it is Gamma or TRC
            try {
                iccRGB.getGamma(0);
            } catch(ProfileDataException ex) {
                doGamma = false;
            }
            if(doGamma) {
                info += "Gamma: ";
                for(i = 0; i < profile.getNumComponents(); i++) {
                    if(i != 0) {
                        info += ", ";
                    }
                    info += iccRGB.getGamma(i);
                }
            } else {
                short[] table;
                info += "Tone Response Curve [TRC]: " + LS;
                for(i = 0; i < profile.getNumComponents(); i++) {
                    if(i == ICC_ProfileRGB.REDCOMPONENT) {
                        info += "  Red:  \t";
                    } else if(i == ICC_ProfileRGB.GREENCOMPONENT) {
                        info += "  Green:\t";
                    } else if(i == ICC_ProfileRGB.BLUECOMPONENT) {
                        info += "  Blue: \t";
                    } else {
                        info += "Unknown";
                    }
                    table = iccRGB.getTRC(i);
                    if(showTableValuesPrecision < 0) {
                        info += "[" + table.length + " values]";
                    } else {
                        for(short val : table) {
                            // These are really unsigned shorts, not shorts
                            info += " "
                                + String.format("%." + showTableValuesPrecision
                                    + "f", (float)(val & 0xFFFF)
                                    / (float)0xFFFF);
                        }
                    }
                    info += LS;
                }
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
                info += stringExcMsg("Error getting white point", ex);
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
                info += stringExcMsg("Error getting matrix", ex);
            }
        }

        // TagTable
        try {
            info += "Tag Table:" + LS;
            info += getTagTable(data);
        } catch(Exception ex) {
            info += stringExcMsg("Error getting tag table", ex);
        }
        info += LS;

        // VGCT
        // info += getTagDataString("vcgt");
        // bytes = getTagData("vcgt");
        // info += getDataDump(bytes);
        Integer gammaType = getVcgtGammaType();
        if(gammaType != null) {
            double[][] table = null;
            if(gammaType == 0) {
                // Table
                int[] params = getVcgtTableParameters();
                if(params != null) {
                    info += "VCGT: " + LS;
                    info += "  Table:";
                    info += " nChannels=" + params[0] + " nEntries="
                        + params[1] + " nSize=" + params[2] + LS;
                }
                table = getVcgtTable();
            } else {
                // Formula
                float[] params = getVcgtFormulaParameters();
                if(params != null) {
                    info += "VCGT: " + LS;
                    info += "  Formula:" + LS;
                    info += String.format("   rGamma=%.4f rMin=%.4f rMax=%.4f",
                        params[0], params[1], params[2]) + LS;
                    info += String.format("   gGamma=%.4f gMin=%.4f gMax=%.4f",
                        params[3], params[4], params[5]) + LS;
                    info += String.format("   bGamma=%.4f bMin=%.4f bMax=%.4f",
                        params[6], params[7], params[8]) + LS;
                }
                table = getVcgtFormulaTable();
            }
            if(table != null && table.length > 0) {
                int nChannels = table.length;
                int nEntries = table[0].length;
                if(FORMAT_VCGT_TABLE_AS_COLUMNS) {
                    for(j = 0; j < nEntries; j++) {
                        for(i = 0; i < nChannels; i++) {
                            info += String.format("\t%8.2f", table[i][j]);
                        }
                        info += LS;
                    }
                } else {
                    for(i = 0; i < nChannels; i++) {
                        if(i == ICC_ProfileRGB.REDCOMPONENT) {
                            if(nChannels == 1) {
                                info += "  All:  \t";
                            } else {
                                info += "  Red:  \t";
                            }
                        } else if(i == ICC_ProfileRGB.GREENCOMPONENT) {
                            info += "  Green:\t";
                        } else if(i == ICC_ProfileRGB.BLUECOMPONENT) {
                            info += "  Blue: \t";
                        } else {
                            info += "Unknown";
                        }
                        if(showTableValuesPrecision < 0) {
                            info += "[" + table[i].length + " values]";
                        } else {
                            for(j = 0; j < nEntries; j++) {
                                info += " "
                                    + String.format("%."
                                        + showTableValuesPrecision + "f",
                                        table[i][j]);
                            }
                        }
                        info += LS;
                    }
                }
            } else {
                // Formula
            }
        } // End block gammaType != null
        return info;
    }

}
