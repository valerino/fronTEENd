package com.valerino.fronteend;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.*;

/**
 * generic utilities
 * Created by valerino on 16/10/14.
 */
public class Utils {
    /**
     * decompress file
     * @param sevenZipPath path to 7z binary
     * @param what what to decompress
     * @param to destination folder
     * @return
     */
    public static int decompress(final String sevenZipPath, final File what, final File to) {
        String[] cmdLine = new String[] { sevenZipPath, "x", what.getAbsolutePath(), "-y", "-o" + to.getAbsolutePath()};
        int r = Utils.runProcess(cmdLine, true);
        return r;
    }

    /**
     * check if a file is compressed, based on extension
     * @param f the File
     * @return
     */
    public static boolean isCompressed(File f) {
        if (f == null) {
            return false;
        }

        String exts[] = {".7z", ".rar", ".zip"};
        final String path = f.getAbsolutePath().toLowerCase();
        for (String s : exts) {
            if (path.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * copy file from source to destination (will be overwritten)
     * @param src the source file
     * @param dst the destination file
     * @param overwrite true to overwrite
     * @return boolean
     * @throws java.io.IOException
     */
    public static boolean copyFile (File src, File dst, boolean overwrite) throws IOException {
        if (!overwrite) {
            if (dst.exists()) {
                return false;
            }
        }
        dst.delete();

        FileInputStream fIn = new FileInputStream(src);
        FileOutputStream fOut = new FileOutputStream(dst);
        while (true) {
            int b = fIn.read();
            if (b == -1) {
                break;
            }
            fOut.write(b);
        }
        return true;
    }

    /**
     * clear a folder (without deleting it)
     * @param folder a folder File
     */
    public static void clearFolder(File folder) {
        if (folder.exists()) {
            File[] t = folder.listFiles();
            if (t != null && t.length != 0) {
                for (File f : t) {
                    f.delete();
                }
            }
        }
    }

    /**
     * check if a folder is empty
     * @param folder a folder File
     * @return
     */
    public static boolean isFolderEmpty (File folder) {
        if (!folder.exists()) {
            return true;
        }

        File[] t = folder.listFiles();
        if (t == null || t.length == 0) {
            return true;
        }
        return false;
    }
    /**
     * run a process and wait for completion
     * @param cmdLine the commandline to be run
     * @param checkReturn true to check for exitcode != 0 (wait for execution), false to not wait
     * @return
     */
    public static int runProcess(String[] cmdLine, boolean checkReturn) {
        int r = 0;
        try {
            Process p = Runtime.getRuntime().exec(cmdLine, null, new File(cmdLine[0]).getParentFile());
            int res = 0;
            if (checkReturn) {
                // wait and check return code
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((reader.readLine()) != null) {}
                res = p.waitFor();
                reader.close();
            }

            if (res != 0) {
                String cmd = "";
                for (final String s : cmdLine) {
                    cmd += (s + " ");
                }
                cmd.trim();
                Alert al = new Alert(Alert.AlertType.ERROR, "exitcode: " + p.exitValue() + "\ncommandline:\n" + cmd);
                al.setResizable(true);
                al.setWidth(320);
                al.showAndWait();
                r = p.exitValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert al = new Alert(Alert.AlertType.ERROR,e.getMessage());
            al.showAndWait();
            r = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            r = 1;
        }
        return r;
    }

    /**
     * run a process without waiting for execution result
     * @param cmdLine the commandline to be run
     * @return
     */
    public static int runProcess(String[] cmdLine) {
        return runProcess(cmdLine, false);
    }

    /**
     * click on a node (button, ...)
     * @param node the Node to be clicked
     * @param mb the mouse button to click
     * @param clicks how many clicks
     */
    public static void nodeClick (Node node, MouseButton mb, int clicks) {
        if (clicks == 0) {
            return;
        }
        node.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                mb, clicks, false, false, false, false, true, false, false, false, false, false, null));
    }
}
