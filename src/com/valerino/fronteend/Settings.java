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

    private static String SETTINGS_FILE = "./fronteend.json";

    private static class SettingsInternal {
        public SettingsInternal() {

        }

        public String sevenZipPath = "";

        public String lastSelectedEmu = "";
    }

    private SettingsInternal _settings = null;

    protected Settings () {
        _settings = new SettingsInternal();
    }

    /**
     * path to 7zip binary
     * @return
     */
    public String sevenZipPath() {
        return _settings.sevenZipPath;
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
        File f = new File (SETTINGS_FILE);

        // create json file
        mapper.writer(new DefaultPrettyPrinter()).writeValue(f, _settings);
//        mapper.writeValue(f, _settings);
    }

    /**
     * read settings from file
     * @throws IOException
     */
    public void deserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File ("./fronteend.json");
        _settings = mapper.readValue(f, SettingsInternal.class);
    }

    /**
     * initialize settings
     * @return 0 on success
     * @throws IOException
     */
    public int initialize() throws IOException {
        try {
            // read settings
            deserialize();
        } catch (IOException e) {
            // settings not found/error
            File f = new File ("./fronteend.json");
            if (f.exists()) {
                // error, exit
                e.printStackTrace();
                return 1;
            }

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
        }
        return 0;
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
