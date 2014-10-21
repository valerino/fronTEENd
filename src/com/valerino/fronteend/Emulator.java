package com.valerino.fronteend;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * represents an emulator
 * Created by valerino on 08/10/14.
 */
public class Emulator {
    private static class EmulatorInternal {
        public String name = "";

        public HashMap<String, List<String> > roms = new HashMap<String, List<String>>();

        public boolean isMame = false;

        public String system = "";

        public String lastFolder = "";

        public String emuBinary = "";

        public String emuParams = "";

        public boolean stripPath = false;

        public boolean noCheckReturn = false;

        public boolean supportRw = false;

        public boolean allowMultiSelect = false;
    }

    private EmulatorInternal _emulator;

    private File _defFile = null;

    private File _rwFolder = null;

    /**
     * get emulator configuration file
     * @return
     */
    public File defFile() {
        return _defFile;
    }

    /**
     * write emulator definition to file
     * @throws IOException
     */
    public void serialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // create json file
        mapper.writer(new DefaultPrettyPrinter()).writeValue(_defFile, _emulator);
    }

    /**
     * read emulator definition from file
     * @throws IOException
     */
    public void deserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        _emulator = mapper.readValue(_defFile, EmulatorInternal.class);
    }

    /**
     * get the info url for a rom
     * @return
     */
    public String infoUrl(final String rom) {
        final String google = "https://www.google.com/search?q=";

        // try to normalize name (strip substrings starting with ( and [ common in romsets)
        String[] tokens = rom.split("[\\(|\\[]");
        String normalized = tokens[0].trim();

        // replace characters in the normalized string
        normalized = normalized.replace("%","%25");
        normalized = normalized.replace("&","%26");
        normalized = normalized.replace("'","%27");
        normalized = normalized.replace(" ","+");

        final String url = google + "\"" + normalized + "\" " + _emulator.system;
        return url;
    }

    /**
     * tell if the emulator has an internal name->rom mapping (i.e. mame)
     * @return
     */
    public boolean isMame() { return _emulator.isMame; }

    /**
     * get configuration defined roms for emulator needing explicit named sets, but do not provide an internal
     * rom list (i.e. daphne)
     * @return
     */
    public HashMap<String, List<String>> roms() {
        return _emulator.roms;
    }

    /**
     * set the roms (for mame)
     * @param roms the rom list
     */
    public void setRoms(HashMap<String, List<String>> roms) { _emulator.roms = roms; }

    /**
     * get emulator display name
     * @return
     */
    public String name() {
        return _emulator.name;
    }

    /**
     * tell if the path must be stripped, leaving the rom name only
     * @return
     */
    public boolean stripPath() {
        return _emulator.stripPath;
    }

    /**
     * tell if the emulator uses rw media images
     * @return
     */
    public boolean supportRw() {
        return _emulator.supportRw;
    }

    /**
     * get the rw folder for this emulator (%home%/fronteend/defname)
     * @return
     */
    public File rwFolder() {
        return _rwFolder;
    }

    /**
     * tell if the emulator do not correctly return when closed (exitcode != 0)
     * @return
     */
    public boolean noCheckReturn() {
        return _emulator.noCheckReturn;
    }

    /**
     * tell if the emulator allow multiple selection (i.e. amiga)
     * @return
     */
    public boolean allowMultiSelect() {
        return _emulator.allowMultiSelect;
    }

    /**
     * get emulator system (i.e. 'arcade', 'c64', ...)
     * @return
     */
    public String system() {
        return _emulator.system;
    }

    /**
     * set the last (roms) folder used
     * @param s the folder absolute path
     */
    public void setLastFolder(final String s) {
        _emulator.lastFolder = s;
    }

    /**
     * get the last (roms) folder used
     * @return
     */
    public String lastFolder () {
        return _emulator.lastFolder;
    }

    /**
     * emulator binary path
     * @return
     */
    public String emuBinary() {
        return _emulator.emuBinary;
    }

    /**
     * set emulator binary path
     * @param s emulator binary absolute path
     */
    public void setEmuBinary (final String s) {
        _emulator.emuBinary = s;
    }

    /**
     * parameters to be used on commandline (use %n for rom to be loaded)
     * @return
     */
    public String emuParams() {
        return _emulator.emuParams;
    }

    /**
     * set emulator parameters
     * @param s parameters string
     */
    public void setEmuParams (final String s) {
        _emulator.emuParams = s;
    }

    public Emulator(File emuDef) throws IOException {
        _defFile = emuDef;

        // initialize the rw folder aswell
        String[] tokens = emuDef.getName().split("\\.");
        _rwFolder = new File (_defFile.getParentFile().getParentFile(),tokens[0]);
        _rwFolder.mkdirs();

        // read from json file
        deserialize();
    }

    @Override
    public String toString() {
        return name();
    }
}
