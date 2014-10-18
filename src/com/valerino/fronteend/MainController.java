package com.valerino.fronteend;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * controller for the main dialog
 * Created by valerino on 09/10/14.
 */
public class MainController {
    @FXML
    private WebView infoWeb;

    @FXML
    private TreeView<RomTreeItem> romsTree;

    @FXML
    private ComboBox<Emulator> emuCombo;

    @FXML
    private Button browseFolderButton;

    @FXML
    private CheckBox customParamsCheckBox;

    @FXML
    private TextField emuBinText;

    @FXML
    private TextField emuParamsText;

    @FXML
    private Button browseEmuBinaryButton;

    @FXML
    private Label selectRomLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button fwdButton;

    @FXML
    private Button refreshTreeButton;

    @FXML
    private Button clearRwButton;

    @FXML
    private CheckBox rwCheckBox;

    @FXML
    private Accordion cfgAccordion;

    private Stage _rootStage;

    /**
     * search info for a rom and display in the webview
     * @param rom the rom name (without extension)
     */
    private void searchInfo(final String rom) {
        Emulator emu = emuCombo.getValue();
        String r = rom;
        if (emu.roms().isEmpty() && !emu.isMame()) {
            // strip extension
            r = r.substring(0,rom.lastIndexOf("."));
        }
        final String url = emu.infoUrl(r);

        infoWeb.getEngine().load(url);
    }

    /**
     * show custom parameters box
     * @param params the current emulator parameters
     * @return
     */
    private String showCustomParameters(final String params) {
        final Stage st = new Stage(StageStyle.UNIFIED);

        // build ui
        VBox vb = new VBox();
        vb.setFillWidth(true);
        vb.setPrefWidth(640);
        final TextField customParams = new TextField(params);
        customParams.setTooltip(new Tooltip(params));
        final Button okButton = new Button("Ok");
        vb.getChildren().add(customParams);
        vb.getChildren().add(okButton);

        // set button to close dialog
        okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    st.close();
                }
            }
        });

        // add a tooltip
        customParams.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.compareTo(oldValue) != 0) {
                    customParams.setTooltip(new Tooltip(newValue));
                }
            }
        });

        // disable close button
        st.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });

        st.setScene(new Scene(vb));
        st.setResizable(false);
        st.setTitle("Custom parameters");
        st.initModality(Modality.WINDOW_MODAL);
        st.initOwner(_rootStage.getScene().getWindow());
        st.centerOnScreen();
        st.showAndWait();

        // return parameters
        return customParams.getText();
    }

    /**
     * load the selected set
     * @param emu the Emulator
     * @param item the rom
     */
    private void loadSet(Emulator emu, RomTreeItem item) {
        String params;
        if (customParamsCheckBox.isSelected()) {
            // we must show the custom parameters box
            customParamsCheckBox.setSelected(false);
            params = showCustomParameters(emu.emuParams());
        }
        else {
            params = emu.emuParams();
        }

        // get rom path and parent path if needed
        String toLoad = "";
        String path = "";
        if (emu.stripPath()) {
            if (item.sets() != null) {
                // there's defined sets, toLoad is the name and path is the parent folder
                File f = new File (item.sets().get(0));
                path = f.getParent();
                toLoad = f.getName();
            }
            else {
                // get path and name from the item's File
                path = item.romFile().getParent();
                toLoad = item.romFile().getName();
            }
        }
        else {
            if (item.sets() != null) {
                // toLoad is a full path already
                toLoad = item.sets().get(0);
            }
            else {
                // use file
                toLoad = item.romFile().getAbsolutePath();
            }
        }

        // check if the rom must be copied to/loaded from the rw folder before loading
        final String oldToLoad = toLoad;
        final String oldPath = path;
        if (emu.supportRw() || rwCheckBox.isSelected()) {
            // load rom from %home%/fronteend/%emuname%
            File dstFolder = new File (Settings.getInstance().baseFolder(), emu.name());
            dstFolder.mkdirs();

            // change paths
            File dst;
            File src;
            if (path.isEmpty()) {
                src = new File (toLoad);
                dst = new File (dstFolder,new File (toLoad).getName());
                toLoad = dst.getAbsolutePath(); // new toLoad path
            }
            else {
                src = new File (path, toLoad);
                dst = new File (dstFolder, toLoad);
                path = dstFolder.getAbsolutePath(); // toLoad is already a bare name
            }

            boolean useRw = rwCheckBox.isSelected();
            if (dst.exists() && !useRw) {
                // already found, ask to load it instead
                Alert al = new Alert(Alert.AlertType.CONFIRMATION, dst.getName() + " found in the r/w folder. Load it instead ?");
                Optional<ButtonType> res = al.showAndWait();
                if (res.get() == ButtonType.OK) {
                    useRw = true;
                }
                else {
                    al = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + dst.getName() + " from the r/w folder ?");
                    res = al.showAndWait();
                    if (res.get() == ButtonType.OK) {
                        dst.delete();
                    }
                }
            }

            if (useRw) {
                try {
                    // do not overwrite if it's already present
                    Utils.copyFile(src,dst,false);
                } catch (IOException e) {
                    Alert al = new Alert(Alert.AlertType.ERROR, "Error copying " + src.getAbsolutePath() + " to\n" + dst.getAbsolutePath() + "\n" + e.getMessage());
                    al.showAndWait();
                    return;
                }
            }
            else {
                toLoad = oldToLoad;
                path = oldPath;
            }
        }

        // hide rw button if the folder has been emptied
        if (emu.supportRw() && !Utils.isFolderEmpty(new File (Settings.getInstance().baseFolder(), emu.name()))) {
            clearRwButton.setVisible(true);
        }
        else {
            clearRwButton.setVisible(false);
        }

        // build commandline
        List<String> ar = new ArrayList<String>();
        ar.add(emu.emuBinary());
        String[] tokens = params.split(" ");
        for (String s : tokens) {
            s = s.replace("%rom%",toLoad);
            s = s.replace("%path%", path);
            s = s.replace("%pathsep%", path + File.separator);
            ar.add(s);
        }
        String[] cmdLine = { };
        cmdLine = ar.toArray(cmdLine);

        // run
        Utils.runProcess(cmdLine, emu.noCheckReturn());
    }

    /**
     * show the set selection box for multiple sets
     * e sets
     * @param emu the Emulator
     * @param rom the choosen rom
     */
    private void showSelectRomset(final Emulator emu, final RomTreeItem rom) {
        final Stage st = new Stage(StageStyle.UNIFIED);
        st.setWidth(640);
        final ListView<String> lv = new ListView<String>();
        lv.getStylesheets().add(this.getClass().getResource("hide_empty.css").toExternalForm());

        // add sets
        lv.getItems().addAll(rom.sets());

        // handle doubleclicks
        lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    // on doubleclick, load emulator with the selected set
                    final String s = lv.getSelectionModel().getSelectedItem();
                    if (s != null) {
                        final List<String> l = new ArrayList<String>();

                        if (rom.parent() == null) {
                            // plain
                            l.add(s);
                        } else {
                            // with parent (from compressed sets, i.e. /tmp/.../xxx.bin)
                            File f = new File(rom.parent(), s);
                            l.add(f.getAbsolutePath());
                        }
                        st.close();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                RomTreeItem r = new RomTreeItem(null, l); // use a dummy RomTreeItem here
                                loadSet(emu, r);
                            }
                        });
                    }
                }
            }
        });

        // show
        VBox vb = new VBox();
        vb.getChildren().add(lv);
        st.setScene(new Scene(vb));
        st.setResizable(false);
        st.setTitle("Choose set");
        st.initModality(Modality.WINDOW_MODAL);
        st.initOwner(_rootStage.getScene().getWindow());
        st.centerOnScreen();
        st.show();
    }

    /**
     * load emulator with the choosen rom/set
     * @param emu the emulator
     * @param rom the choosen rom
     * @throws IOException
     */
    private void runEmulator(Emulator emu, RomTreeItem rom) throws IOException {
        // check if the file is compressed
        if (Utils.isCompressed(rom.romFile())) {
            // decompress to a temp folder (clear it first)
            File tempFolder = Settings.getInstance().tmpFolder();
            Utils.clearFolder(tempFolder);
            String[] cmdLine = new String[] { Settings.getInstance().sevenZipPath(), "x", rom.romFile().getAbsolutePath(), "-o" + tempFolder.getAbsolutePath()};
            Utils.changeCursor(_rootStage, true);
            int r = Utils.runProcess(cmdLine);
            Utils.changeCursor(_rootStage, false);
            if (r != 0) {
                // error occurred
                return;
            }

            // create a new dummy romtreeitem with the new sets
            File[] sets = tempFolder.listFiles();
            if (sets != null && sets.length != 0) {
                List<String> l = new ArrayList<String>();
                for (File s : sets) {
                    l.add(s.getName());
                }
                rom = new RomTreeItem(null, l, tempFolder); // we set a parent folder here, the rom will be run here
            }
        }

        // check if there's defined rom sets (pre-defined or from decompression)
        if (rom.sets() != null && rom.sets().size() > 1) {
            showSelectRomset(emu, rom);
        }
        else {
            // decompressed and only 1 set, or direct load
            if (rom.parent() != null) {
                File f = new File (rom.parent(), rom.sets().get(0));
                rom = new RomTreeItem(f);
            }
            loadSet(emu, rom);
        }
    }

    /**
     * show the browse for folder dialog
     * @param emu the Emulator
     */
    private void showSetFolderDialog(Emulator emu) {
        FileChooser fc = new FileChooser();
        DirectoryChooser dc = new DirectoryChooser();
        if (!emu.lastFolder().isEmpty()) {
            dc.setInitialDirectory(new File (emu.lastFolder()));
        }
        dc.setTitle("Select roms folder for " + emu.name());
        File lastFolder = lastFolder = dc.showDialog(_rootStage.getOwner());
        if (lastFolder != null) {
            emu.setLastFolder(lastFolder.getAbsolutePath());
            try {
                // save back to def file
                emu.serialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * add roms to the tree (from filesystem)
     * @param emu the Emulator
     * @list the tree root's children
     */
    private void addRomsFs(Emulator emu, ObservableList<RomTreeItem> list) {
        if (emu.lastFolder().isEmpty()) {
            // we have to set lastfolder first
            showSetFolderDialog(emu);
        }
        final String folder = emu.lastFolder();
        File f = new File (folder);
        File[] files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // skip these
                if (pathname.isDirectory()) {
                    return false;
                }
                final String p = pathname.getAbsolutePath();
                if (p.endsWith(".txt")) {
                    return false;
                }
                return true;
            }
        });
        if (files == null || files.length == 0) {
            return;
        }

        // fill tree
        for (File rom : files) {
            RomTreeItem item = new RomTreeItem(rom);
            list.add(item);
        }
    }

    /**
     * add roms from mame
     * @param emu the Emulator
     * @param list the tree root's children
     */
    private void addRomsMame(Emulator emu, ObservableList<RomTreeItem> list) {
        // execute mame -listfull
        BufferedReader output = null;
        try {
            final String params = "-listfull";
            Process p = Runtime.getRuntime().exec(new String[] { emu.emuBinary(), params });
            // parse output
            output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            int count = 0;
            while ((line = output.readLine()) != null) {
                // format: set<spaces>"name", first line is a header to be skipped
                if (count != 0) {
                    // split set and name
                    String[] tokens = line.split("\\s+\"+");
                    final String set = tokens[0];
                    final String name = tokens[1].substring(0,tokens[1].length() - 1);
                    ArrayList<String> l = new ArrayList<String>();
                    l.add(set);

                    // add item
                    RomTreeItem item = new RomTreeItem(name, l);
                    list.add(item);
                }
                count++;
            }

            // check for error
            int res = p.waitFor();
            if (res != 0) {
                Alert al = new Alert(Alert.AlertType.ERROR, "exitcode: " + p.exitValue() + "\ncommandline:\n" + emu.emuBinary() + " " + params);
                al.showAndWait();
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert al = new Alert(Alert.AlertType.ERROR, e.getMessage());
            al.showAndWait();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * add roms to the tree (from defined sets)
     * @param emu the Emulator
     * @param list the tree root's children
     */
    private void addRomsDefined(Emulator emu, ObservableList<RomTreeItem> list) {
        HashMap<String,List<String>> roms = emu.roms();
        boolean isMame = emu.isMame();
        if (isMame && roms.isEmpty() && !emu.emuBinary().isEmpty()) {
            // extract sets from mame
            addRomsMame(emu, list);
        }
        else {
            // add sets to list
            for (final String s : roms.keySet()) {
                RomTreeItem item = new RomTreeItem(s, roms.get(s));
                list.add(item);

}
        }
    }

    /**
     * add roms to the tree
     * @param emu the Emulator
     */
    private void addRoms(Emulator emu) {
        // clear
        Utils.changeCursor(_rootStage, true);
        RomTreeItem root = (RomTreeItem) romsTree.getRoot();
        ObservableList<RomTreeItem> c = root.getChildren();
        c.clear();
        root.setExpanded(true);

        // add items
        HashMap<String, List<String>> roms = emu.roms();

        if (!roms.isEmpty() || emu.isMame()) {
            // there's a predefined romset
            addRomsDefined(emu, c);
        } else {
            // scan filesystem
            addRomsFs(emu, c);
        }

        // finally sort the list
        c.sort(new Comparator<RomTreeItem>() {
            @Override
            public int compare(RomTreeItem o1, RomTreeItem o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        selectRomLabel.setText("Select romset (" + ((RomTreeItem) romsTree.getRoot()).getChildren().size() + ")");
        Utils.changeCursor(_rootStage, false);
    }

    /**
     * initialize application
     * @param root the root stage
     * @return 0 on success
     */
    public int initController(Stage root) {
        _rootStage = root;

        // setup handlers

        // handle refresh roms button
        refreshTreeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Emulator emu = emuCombo.getValue();
                            addRoms(emu);
                        }
                    });
                }
            }
        });

        // handle clear rw button
        clearRwButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                    Alert al = new Alert(Alert.AlertType.CONFIRMATION, "Clear r/w folder for " + emuCombo.getValue().name() + " ?");
                    Optional<ButtonType> res = al.showAndWait();
                    if (res.get() == ButtonType.OK) {
                        File rwFolder = new File (Settings.getInstance().baseFolder(), emuCombo.getValue().name());
                        Utils.clearFolder(rwFolder);
                    }
                    al = new Alert(Alert.AlertType.INFORMATION, "Successfully cleared r/w folder for " + emuCombo.getValue().name());
                    al.showAndWait();
                    clearRwButton.setVisible(false);
                }
            }
        });

        // handle browse for emulator binary click / change text
        browseEmuBinaryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                    // choose emulator binary
                    Emulator emu = emuCombo.getValue();
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Choose emulator binary for " + emu.name());
                    File bin = fc.showOpenDialog(_rootStage.getOwner());
                    if (bin != null) {
                        emuBinText.setText(bin.getAbsolutePath());
                    }
                }
            }
        });

        // handle combo selection
        emuCombo.valueProperty().addListener(new ChangeListener<Emulator>() {
            @Override
            public void changed(ObservableValue<? extends Emulator> observable, Emulator oldValue, Emulator newValue) {
                if (newValue != null) {
                    // write the new value to configuration
                    Settings.getInstance().setLastSelectedEmu(newValue.name());
                    try {
                        Settings.getInstance().serialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // setup ui elements
                emuBinText.setText(newValue.emuBinary());
                emuParamsText.setText(newValue.emuParams());
                customParamsCheckBox.setSelected(false);
                if (!newValue.roms().isEmpty() || newValue.isMame()) {
                    browseFolderButton.setVisible(false);
                }
                else {
                    browseFolderButton.setVisible(true);
                }

                if (!newValue.roms().isEmpty()) {
                    refreshTreeButton.setVisible(false);
                }
                else {
                    refreshTreeButton.setVisible(true);
                }

                // check for r/w support
                if (newValue.supportRw()) {
                    rwCheckBox.setVisible(true);
                    // rw folder is empty ?
                    File rwFolder = new File (Settings.getInstance().baseFolder(), newValue.name());
                    boolean rwFolderEmpty = Utils.isFolderEmpty(rwFolder);
                    if (!rwFolderEmpty) {
                        clearRwButton.setVisible(true);
                    }
                    else {
                        clearRwButton.setVisible(false);
                    }
                }
                else {
                    rwCheckBox.setVisible(false);
                    clearRwButton.setVisible(false);
                }

                if (newValue.isMame() && newValue.emuBinary().isEmpty()) {
                    // mame and no binary set -> we can't query the emulator for roms
                    browseEmuBinaryButton.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                            MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null));
                }

                // fill the roms tree
                refreshTreeButton.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                        MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null));
            }
        });

        emuBinText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.compareTo(oldValue) != 0) {
                    Emulator emu = emuCombo.getValue();
                    emu.setEmuBinary(newValue);
                    emuBinText.setTooltip(new Tooltip(newValue));
                    try {
                        emu.serialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // handle browsefolder button click
        browseFolderButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                    // browse for roms and update tree
                    showSetFolderDialog(emuCombo.getValue());
                    addRoms(emuCombo.getValue());
                }
            }
        });

        // handle params editing
        emuParamsText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.compareTo(oldValue) != 0) {
                    Emulator emu = emuCombo.getValue();
                    emu.setEmuParams(newValue);
                    emuParamsText.setTooltip(new Tooltip(newValue));
                    try {
                        emu.serialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // handle roms tree selection
        romsTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<RomTreeItem>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<RomTreeItem>> observable, TreeItem<RomTreeItem> oldValue, TreeItem<RomTreeItem> newValue) {
                if (newValue != null) {
                    // search for rom name (google) in webview
                    RomTreeItem item = (RomTreeItem)newValue;
                    searchInfo(item.name());
                }
            }
        });

        // handle tree clicks
        romsTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    final Emulator emu = emuCombo.getValue();
                    if (emu.emuBinary().isEmpty()) {
                        Alert al = new Alert(Alert.AlertType.WARNING, "Please select an emulator binary first");
                        al.showAndWait();
                        cfgAccordion.setExpandedPane(cfgAccordion.getPanes().get(0));
                        browseEmuBinaryButton.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                                MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null));
                        return;
                    }
                    if (emu.emuParams().isEmpty()) {
                        Alert al = new Alert(Alert.AlertType.WARNING, "Please fill emulator parameters first");
                        al.showAndWait();
                        cfgAccordion.setExpandedPane(cfgAccordion.getPanes().get(0));
                        return;
                    }

                    final RomTreeItem item = (RomTreeItem) romsTree.getSelectionModel().getSelectedItem();
                    if (item != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    runEmulator(emu, item);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });

        // back webview button
        backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    WebHistory h = infoWeb.getEngine().getHistory();
                    try {
                        h.go(-1);
                    }
                    catch (IndexOutOfBoundsException e) {

                    }
                }
            }
        });

        // forward webview button
        fwdButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    WebHistory h = infoWeb.getEngine().getHistory();
                    try {
                        h.go(+1);
                    }
                    catch (IndexOutOfBoundsException e) {

                    }
                }
            }
        });

        // simple search
        romsTree.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent event) {
                ObservableList<TreeItem<RomTreeItem>> l = romsTree.getRoot().getChildren();
                FilteredList<TreeItem<RomTreeItem>> f = new FilteredList<TreeItem<RomTreeItem>>(l, new Predicate<TreeItem<RomTreeItem>>() {
                    @Override
                    public boolean test(TreeItem<RomTreeItem> romTreeItemTreeItem) {
                        if (((RomTreeItem) romTreeItemTreeItem).name().toLowerCase().startsWith(event.getText())) {
                            return true;
                        }
                        return false;
                    }
                });
                if (!f.isEmpty()) {
                    // select item
                    romsTree.scrollTo(f.getSourceIndex(0));
                }
            }
        });

        // init tree
        romsTree.setRoot(new RomTreeItem());
        romsTree.setShowRoot(false);

        // load settings
        try {
            Settings.getInstance().initialize();
        }
        catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        File[] defs = Settings.getInstance().defsFolder().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getAbsolutePath().endsWith(".json")) {
                    return true;
                }
                return false;
            }
        });
        if (defs == null || defs.length == 0) {
            Alert al = new Alert (Alert.AlertType.ERROR, "Can't continue, no emulators in\n" + Settings.getInstance().defsFolder().getAbsolutePath());
            al.showAndWait();
            return 1;
        }

        // fill combo with emulators
        for (File f : defs) {
            try {
                Emulator emu = new Emulator(f);
                emuCombo.getItems().add(emu);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        emuCombo.getItems().sort(new Comparator<Emulator>() {
            @Override
            public int compare(Emulator o1, Emulator o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        // check if there's a last selected emulator
        final String last = Settings.getInstance().lastSelectedEmu();
        if (!last.isEmpty()) {
            ObservableList<Emulator> items = emuCombo.getItems();
            for (Emulator e : items) {
                if (e.name().equals(last)) {
                    emuCombo.getSelectionModel().select(e);
                }
            }
        }
        return 0;
    }

    /**
     * cleanup
     */
    public void cleanupController() {
        Utils.clearFolder(Settings.getInstance().tmpFolder());
    }
}
