package com.valerino.fronteend;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.List;

/**
 * a tree item in the roms treeview
 * Created by valerino on 09/10/14.
 */
public class RomTreeItem extends TreeItem {
    private List<String> _sets = null;

    private String _name = "";

    private File _file = null;

    private File _parent = null;

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
    public File romFile() {
        return _file;
    }

    public RomTreeItem(final String rom, final List<String> sets) {
        _name = rom;
        _sets = sets;
        setValue(rom);
    }

    /**
     * get the parent folder
     * @return
     */
    public File parent() {
        return _parent;
    }

    public RomTreeItem(final String rom, final List<String> sets, File parent) {
        _name = rom;
        _sets = sets;
        _parent = parent;
        setValue(rom);
    }

    public RomTreeItem(File rom) {
        _name = rom.getName();
        setValue(_name);
        _file = rom;
    }

    public RomTreeItem() {

    }
}
