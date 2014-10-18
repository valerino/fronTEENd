package com.valerino.fronteend;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

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
     * initialize settings
     * @throws IOException
     */
    public void initialize() throws IOException {
        try {
            // read settings
            deserialize();
        } catch (IOException e) {
            // 1st run, create the configuration file
            Alert al = new Alert(Alert.AlertType.INFORMATION,"First run, navigate to 7z binary");
            al.showAndWait();

            FileChooser fc = new FileChooser();
            File szip = null;
            while (szip == null) {
                szip = fc.showOpenDialog(al.getOwner());
            }

            // set path to 7zip binary
            _settings.sevenZipPath = szip.getAbsolutePath();

            // write to file
            serialize();

            // dump emulator definitions aswell in the home folder
            File srcFolder = new File (this.getClass().getResource("emudefs").getFile());
            File[] emus = srcFolder.listFiles();
            for (File f : emus) {
                File n = new File (_emuDefsFolder, f.getName());
                Utils.copyFile(f,n,false);
            }
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
