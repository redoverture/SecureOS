import java.io.*;

public class LnkParse {

    /** local drive */
    private String local = null;
    /** local path */
    private String path = null;
    /** local shareName */
    private String shareName = null;
    /** */
    private static boolean debug = false;

    /**
     * Nothing to do here yet...
     */
    public LnkParse() {
    }

    /**
     * Not intended for standalone. This is used for local
     * texting only.
     */
    public static void main(String[] args) {
        // debug = true;
        if (debug) {
            LnkParse lnkParse = new LnkParse();
            // add sample lnk files to test on your system here
            lnkParse.parse("Shortcut to h.lnk");
            lnkParse.parse("javaw.lnk");
            lnkParse.parse("Shortcut to New Folder.lnk");
        }
    }

    /**
     * Parse the Windows shortcut (lnk) file.
     * @param fName fileName or full path name to the shortcut file
     */
    public void parse(String fName) {
        try {
            BufferedInputStream in = new BufferedInputStream(new
                    FileInputStream(fName));
            int ch = -1;
            int offset;
            int offLocal;
            int offPath;
            // skip header
            for (int i = 0; i <= 76; i++) {
                ch = in.read();
            }
            offset = ch;
            if (ch < 0) return;
            // skip to offset of local path
            for (int i = 0; i <= offset; i++) {
                ch = in.read();
            }

            for (int i = 0; i <= 20; i++) {
                ch = in.read();
            }
            offPath = ch;
            if (ch < 0) return;
            // skip to offset of path
            for (int i = 0; i < 8; i++) {
                ch = in.read();
                offPath--;
            }
            offLocal = ch;
            if (ch < 0) return;

            // get local
            for (int i = 0; i < offLocal; i++) {
                ch = in.read();
                offPath--;
            }
            byte loc[] = new byte[256];
            int index = 0;
            loc[index++] = (byte)ch;
            while ( (ch = in.read()) != 0) {
                loc[index++] = (byte)ch;
                offPath--;
            }
            local = new String(loc);
            local = local.trim();
            if (offPath < 0) return;

            // getpath
            for (int i = 0; i < offPath; i++) {
                ch = in.read();
            }
            loc = new byte[256];
            index = 0;
            loc[index++] = (byte)ch;
            while ( (ch = in.read()) != 0) {
                loc[index++] = (byte)ch;
            }
            shareName = new String(loc);
            shareName = shareName.trim();
            // skip 0xh
            ch = in.read();
            loc = new byte[256];
            index = 0;
            loc[index++] = (byte)ch;
            while ( (ch = in.read()) != 0) {
                loc[index++] = (byte)ch;
            }
            path = new String(loc);
            path = path.trim();
            loc = null; // good habit to free for gc

            // make sure we have the correct values
            if (debug) {
                System.out.println("Share Name: <" + shareName + ">");
                System.out.println("Local Name: <" + local + ">");
                System.out.println("Path: <" + path + ">");
                System.out.println("Full path: <" + getFullPath() + ">");
                // Test if this link exists
                System.out.println("Is link? " + isLink());
                System.out.println("Not broken? " + notBroken());
                System.out.println("Is directory? " + isDirectory());
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    /**
     * Returns the full path name to the shortcut link
     * @returns fullPathName
     */
    public String getFullPath() {
        return local + path;
    }

    /**
     * Returns the local drive of the link
     * @returns local
     */
    public String getLocalDrive() {
        return local;
    }
    /**
     * Returns the path to the shortcut link relative to local
     * @returns path
     */
    public String getLocalPath() {
        return path;
    }
    /**
     * Returns the share name
     * @returns shareName
     */
    public String getShareName() {
        return path;
    }

    /**
     * Reports with good probability that this is a link
     * @returns true if it is likley a link
     */
    public boolean isLink() {
        if (getFullPath() == null)
            return /* can't be or parse failed.... */ false;

        // check for valid "drive letter and :\"
        String drives = "abcdefghijklmnopqrstuvwxyz";
        String drive = local.substring(0,1).toLowerCase();
        if (drives.indexOf(drive) < 0) {
            if (debug) System.out.println("not a drive");
            drives = null;
            drive = null;
            return false;
        }
        drives = null;
        drive = null;
        if (! local.substring(1,3).equals(":" + File.separator)) {
            if (debug) System.out.println("Not found :\\\" ");
            return false;
        }

        // check for any invalid characters
        String winInvalids[] = {"/", "*", "?", "\"", "<", ">", "|"};
        for (int i = 0; i < winInvalids.length; i++) {
            if (getFullPath().indexOf(winInvalids[i]) >= 0) {
                return false;
            }
        }
        winInvalids = null;

        // check for funny ascii values
        char chars[] = getFullPath().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ( (chars[i] < 32) || (chars[i] > 126) ) {
                chars = null;
                return false;
            }
        }
        chars = null;
        return /* could be */ true;
    }

    /**
     * Test if the link is not broken
     * @return true if link file is there
     */
    public boolean notBroken() {
        File test = new File(getFullPath());
        return test.exists();
    }

    /**
     * Test if this link represents a directory
     * @return true if links is a directory
     */
    public boolean isDirectory() {
        File test = new File(getFullPath());
        return test.isDirectory();
    }

}