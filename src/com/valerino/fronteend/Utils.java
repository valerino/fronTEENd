package com.valerino.fronteend;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        String[] cmdLine = new String[] { sevenZipPath, "x", what.getAbsolutePath(), "-o" + to.getAbsolutePath()};
        int r = Utils.runProcess(cmdLine);
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
        final String path = f.getAbsolutePath();
        for (String s : exts) {
            if (path.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * runs in the ui thread and wait for completion
     * @param run the Runnable
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void runAndWait(final Runnable run) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            try {
                run.run();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        } else {
            final Lock lock = new ReentrantLock();
            final Condition condition = lock.newCondition();
            final Throwable[] t = {null};
            lock.lock();
            try {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        lock.lock();
                        try {
                            run.run();
                        } catch (Throwable e) {
                            t[0] = e;
                        } finally {
                            try {
                                condition.signal();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                });
                condition.await();
                if (t[0] != null) {
                    throw new ExecutionException(t[0]);
                }
            } finally {
                lock.unlock();
            }
        }
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
     * run a process
     * @param cmdLine the commandline to be run
     * @param noCheckReturn true to not check for exitcode
     * @return
     */
    public static int runProcess(String[] cmdLine, boolean noCheckReturn) {
        int r = 0;
        try {
            Process p = Runtime.getRuntime().exec(cmdLine, null, new File(cmdLine[0]).getParentFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((reader.readLine()) != null) {}
            int res = p.waitFor();
            reader.close();

            if (res != 0 && !noCheckReturn) {
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
     * run a process
     * @param cmdLine the commandline to be run
     * @return
     */
    public static int runProcess(String[] cmdLine) {
        return runProcess(cmdLine, false);
    }

}
