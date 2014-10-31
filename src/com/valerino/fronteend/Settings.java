package com.valerino.fronteend;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * the application settings
 * Created by valerino on 08/10/14.
 */
public class Settings {
    private static Settings _instance = null;

    private File _settingsFile = null;

    private File _emuDefsFolder = null;

    private File _baseFolder = null;

    private File _tmpFolder = null;

    private SettingsInternal _settings = null;

    private static class SettingsInternal {
        public SettingsInternal() {

        }

        public String sevenZipPath = "";

        public String lastSelectedEmu = "";

        public boolean showExitConfirmation = true;
    }

    protected Settings () {
        _settings = new SettingsInternal();
        // create settings dir
        final String home = System.getProperty("user.home");
        _baseFolder = new File (home,".fronteend");
        _baseFolder.mkdirs();
        _settingsFile = new File (_baseFolder, "cfg.json");
        _emuDefsFolder = new File (_settingsFile.getParentFile(), "emudefs");
        _tmpFolder = new File (_baseFolder, "tmp");
        _emuDefsFolder.mkdirs();
        _tmpFolder.mkdirs();
    }

    /**
     * path to 7zip binary
     * @return
     */
    public String sevenZipPath() {
        return _settings.sevenZipPath;
    }

    /**
     * the emulator definitions folder (~/.fronteend/emudefs)
     * @return
     */
    public File defsFolder() {
        return _emuDefsFolder;
    }

    /**
     * the temporary folder (~/.fronteend/tmp)
     * @return
     */
    public File tmpFolder() {
        return _tmpFolder;
    }

    /**
     * the settings base folder (~/.fronteend)
     * @return
     */
    public File baseFolder() {
        return _baseFolder;
    }

    /**
     * show the exit confirmation box ?
     * @return
     */
    public boolean showExitConfirmation () {
        return _settings.showExitConfirmation;
    }

    /**
     * wether to show the exit confirmation box or not
     * @param show true to show
     */
    public void setShowExitConfirmation (boolean show) {
        _settings.showExitConfirmation = show;
    }

    /**
     * last selected emulator name (must be present among emudefs)
     * @return
     */
    public String lastSelectedEmu() {
        return _settings.lastSelectedEmu;
    }

    public void setLastSelectedEmu(final String s) {
        _settings.lastSelectedEmu = s;
    }

    /**
     * write settings to file
     * @throws IOException
     */
    public void serialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // create json file
        mapper.writer(new DefaultPrettyPrinter()).writeValue(_settingsFile, _settings);
    }

    /**
     * read settings from file
     * @throws IOException
     */
    public void deserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        _settings = mapper.readValue(_settingsFile, SettingsInternal.class);
    }

    /**
     * dump emulator definitions to home folder
     */
    private void dumpEmuDefs() {
        JarFile jf = null;
        try {
            jf = new JarFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        } catch (IOException e) {
            // this happens when called from ide
            File srcFolder = new File (this.getClass().getResource("emudefs").getFile());
            File[] emus = srcFolder.listFiles();
            for (File f : emus) {
                File n = new File (_emuDefsFolder, f.getName());
                try {
                    // dump definitions (without overwriting)
                    Utils.copyFile(f,n,false);
                } catch (IOException ee) {
                    e.printStackTrace();
                    return;
                }
            }
            return;
        }

        // when run fron jar, extract from jar
        final Enumeration<JarEntry> entries = jf.entries();
        while(entries.hasMoreElements()) {
            final String entryName = entries.nextElement().getName().toLowerCase();
            if (entryName.endsWith(".json") && entryName.contains("/emudefs/")) {
                // dump to folder
                File src = new File (entryName);
                InputStream is = this.getClass().getResourceAsStream("/com/valerino/fronteend/emudefs/" + src.getName());
                File tgt = new File (Settings.getInstance()._emuDefsFolder, src.getName());
                try {
                    if (!tgt.exists()) {
                        Files.copy(is,tgt.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            jf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ask to setup the 7zip binary
     * @return
     */
    public int setup7z() {
        if (!_settings.sevenZipPath.isEmpty()) {
            // 7z is already set
            return 0;
        }

        Alert al = new Alert(Alert.AlertType.INFORMATION,"Please navigate to 7z binary");
        al.showAndWait();

        FileChooser fc = new FileChooser();
        File szip = fc.showOpenDialog(al.getOwner());
        if (szip == null) {
            // show a warning box
            al = new Alert(Alert.AlertType.WARNING, "Beware, without 7zip you will not be able to run compressed sets unless the emulator natively supports that!");
            al.showAndWait();
            return 1;
        }
        else {
            // set path to 7zip binary
            _settings.sevenZipPath = szip.getAbsolutePath();
            try {
                serialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /** q
     * initialize settings
     * @throws IOException
     */
    public void initialize() throws IOException {
        try {
            // read settings
            deserialize();

            // dump any missing emulator defs from the internal package
            dumpEmuDefs();
        } catch (IOException e) {
            // 1st run, create the configuration file
            serialize();

            // dump emulator definitions aswell in the home folder
            dumpEmuDefs();
        }
    }

    /**
     * get singleton instance
     * @return Settings
     */
    public static Settings getInstance () {
        if (_instance == null) {
            _instance = new Settings();
        }
        return _instance;
    }
}
