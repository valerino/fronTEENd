package com.valerino.fronteend;

import java.io.File;
import java.util.List;

/**
 * an item in the list view
 * Created by valerino on 09/10/14.
 */
public class RomItem {
    private List<String> _sets = null;

    private String _name = "";

    private File _file = null;

    /**
     * get display name
     * @return
     */
    public String name() {
        return _name;
    }

    /**
     * get defined sets
     * @return
     */
    public List<String> sets() {
        return _sets;
    }

    /**
     * get the rom File
     * @return
     */
    public File file() {
        return _file;
    }

    @Override
    public String toString() {
        if (_file == null) {
            return _name;
        }
        else {
            return _file.getName();
        }
    }

    public RomItem(final String name, final List<String> sets) {
        _name = name;
        _sets = sets;
    }

    /**
     * set the rom file
     * @param f the File
     */
    public void setFile(File f) {
        _file = f;
    }

    public RomItem(File path) {
        _name = path.getName();
        _file = path;
    }

    public RomItem(final String name, final File file) {
        _name = name;
        _file = file;
    }

    public RomItem(final String name) {
        _name = name;
    }
}
