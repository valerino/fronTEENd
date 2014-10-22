package com.valerino.fronteend;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

    private String _keyBuffer = "";

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

        infoWeb.getEngine().setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
        infoWeb.getEngine().load(url);
    }

    /**
     * show custom parameters box
     * @param params the current emulator parameters
     * @return
     */
    private String getCustomParameters(final String params) {
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
     * show the set selection box
     * @param list list to be shown
     * @return
     */
    private List<RomTreeItem> getSetBoxSelection(final List<RomTreeItem> list) {
        final Stage st = new Stage(StageStyle.UNIFIED);
        st.setWidth(640);
        final ListView<RomTreeItem> lv = new ListView<RomTreeItem>();
        if (emuCombo.getValue().allowMultiSelect()) {
            lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        }
        else {
            lv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        lv.getStylesheets().add(this.getClass().getResource("hide_empty.css").toExternalForm());

        // add sets
        for (final RomTreeItem r : list) {
            lv.getItems().add(r);
        }

        final List<RomTreeItem> returnList = new ArrayList<RomTreeItem>();

        // handle doubleclicks
        lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    // on doubleclick, add items to list
                    ObservableList<RomTreeItem> l = lv.getSelectionModel().getSelectedItems();
                    for (final RomTreeItem i : l) {
                        returnList.add(i);
                    }
                    st.close();
                }
            }
        });

        // handle keypress
        lv.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    // run emulator
                    lv.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                            MouseButton.PRIMARY, 2, false, false, false, false, true, false, false, false, false, false, null));
                    return;
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
        st.showAndWait();
        return returnList;
    }

    /**
     * get the romstree selection, decompress if needed and return an array of the found sets
     * @return
     */
    private List<RomTreeItem> getRomsTreeSelection() {
        Emulator emu = emuCombo.getValue();
        List<RomTreeItem> selection = new ArrayList<RomTreeItem>();
        ObservableList<TreeItem<RomTreeItem>> l = romsTree.getSelectionModel().getSelectedItems();

        // check if the emulator has a predefined romset
        if (!emu.roms().isEmpty() || emu.isMame()) {
            // emulator has a predefined set, multiselection is not allowed.
            // we just get the sets into the selection list
            RomTreeItem it = l.get(0).getValue();
            for (final String s : it.sets()) {
                RomTreeItem i = new RomTreeItem(s);
                selection.add(i);
            }
        }
        else {
            // check each selection File and copy them to the temporary folder
            final File dstFolder = Settings.getInstance().tmpFolder();
            Utils.clearFolder(dstFolder);
            for (TreeItem<RomTreeItem> item : l) {
                final RomTreeItem it = item.getValue();

                // dump to rw folder
                if (Utils.isCompressed(it.file())) {
                    // decompress
                    _rootStage.getScene().setCursor(Cursor.WAIT);
                    final int[] res = {0};
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            res[0] = Utils.decompress(Settings.getInstance().sevenZipPath(), it.file(), dstFolder);
                            _rootStage.getScene().setCursor(Cursor.DEFAULT);
                        }
                    };
                    try {
                        Utils.runAndWait(r);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (res[0] != 0) {
                            return null;
                        }
                    }
                }
                else {
                    // just copy
                    try {
                        Utils.copyFile(it.file(),new File (dstFolder,it.file().getName()),false);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }

            // list the files in the tmp folder
            File[] files = dstFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    final String s = name.toLowerCase();
                    final String[] skipExts = { ".txt", ".diz", ".nfo" };
                    for (final String ext : skipExts) {
                        if (s.endsWith(ext)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            if (files != null && files.length > 0) {
                for (File f : files) {
                    RomTreeItem i = new RomTreeItem(f);
                    selection.add(i);
                }
            }
        }

        // sort the selection
        selection.sort(new Comparator<RomTreeItem>() {
            @Override
            public int compare(RomTreeItem o1, RomTreeItem o2) {
                return o1.name().toLowerCase().compareTo(o2.name().toLowerCase());
            }
        });

        // for more than 1 set, show the selection box
        if (selection.size() > 1) {
            // show set selection box
            selection = getSetBoxSelection(selection);
        }

        return selection;
    }

    /**
     * load emulator with the choosen rom/set
     * @param emu the emulator
     * @throws IOException
     */
    private void runEmulator(final Emulator emu) throws IOException {
        // get the selected items
        List<RomTreeItem> l = getRomsTreeSelection();
        if (l == null || l.isEmpty()) {
            return;
        }

        if (emu.supportRw()) {
            // handle r/w support
            for (RomTreeItem i : l) {
                File f = new File (emu.rwFolder(), i.file().getName());
                boolean overwrite = true;
                if (f.exists()) {
                    // already found, ask to load it instead
                    Alert al = new Alert(Alert.AlertType.CONFIRMATION, f.getName() + " found in the r/w folder. Use it ?");
                    Optional<ButtonType> res = al.showAndWait();
                    if (res.get() == ButtonType.OK) {
                        i.setFile(f);
                        overwrite = false;
                    }
                    else {
                        // ask to delete (only if checkbox is not selected)
                        if (!rwCheckBox.isSelected()) {
                            al = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + f.getName() + "\nfrom the r/w folder ?");
                            res = al.showAndWait();
                            if (res.get() == ButtonType.OK) {
                                // delete file and update clear button status
                                f.delete();
                                if (Utils.isFolderEmpty(f.getParentFile())) {
                                    clearRwButton.setVisible(false);
                                }
                                else {
                                    clearRwButton.setVisible(true);
                                }
                            }
                        }
                    }
                }
                if (overwrite && rwCheckBox.isSelected()) {
                    // copy to r/w folder
                    boolean r = Utils.copyFile(i.file(),f,true);
                    if (r == false) {
                        Alert al = new Alert(Alert.AlertType.ERROR, "Error copying to rw folder:\n" + i.file().getAbsolutePath());
                        Optional<ButtonType> res = al.showAndWait();
                        return;
                    }

                    // change path to the rw folder one
                    i.setFile(f);
                }
            }
        }

        // show custom parameters box if needed
        String params;
        if (customParamsCheckBox.isSelected()) {
            // we must show the custom parameters box
            customParamsCheckBox.setSelected(false);
            params = getCustomParameters(emu.emuParams());
        }
        else {
            params = emu.emuParams();
        }

        // build commandline
        String path = "";
        if (emu.stripPath()) {
            // path is always set to the first selection's path
            path = l.get(0).file().getParent();
        }
        List<String> ar = new ArrayList<String>();
        ar.add(emu.emuBinary());
        String[] tokens = params.split(" ");
        int num = l.size();
        for (String s : tokens) {
            s = s.replace("%path%", path);
            s = s.replace("%pathsep%", path + File.separator);
            for (int i=0; i < num; i++) {
                final String placeHolder = "%" + (i+1) + "%";
                if (emu.isMame() || !emu.roms().isEmpty()) {
                    s = s.replace(placeHolder, l.get(i).name());
                }
                else {
                    if (emu.stripPath()) {
                        s = s.replace(placeHolder, l.get(i).file().getName());
                    }
                    else {
                        s = s.replace(placeHolder, l.get(i).file().getAbsolutePath());
                    }
                }
            }
            ar.add(s);
        }

        // check if there's more roms placeholder than the ones we selected
        for (final String s : ar) {
            if (s.startsWith("%") && s.endsWith("%")) {
                ar.remove(s);
            }
        }

        String[] cmdLine = { };
        cmdLine = ar.toArray(cmdLine);

        // run emulator
        Utils.runProcess(cmdLine, emu.noCheckReturn());
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
     */
    private void addRomsFs(Emulator emu) {
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
                final String p = pathname.getAbsolutePath().toLowerCase();
                if (p.endsWith(".txt") || p.endsWith(".log")) {
                    return false;
                }
                return true;
            }
        });
        if (files == null || files.length == 0) {
            return;
        }

        // fill tree
        ObservableList<TreeItem<RomTreeItem>> c = romsTree.getRoot().getChildren();
        for (File rom : files) {
            TreeItem<RomTreeItem> item = new TreeItem<RomTreeItem>(new RomTreeItem(rom));
            c.add(item);
        }
    }

    /**
     * add roms from mame
     * @param emu the Emulator
     */
    private void addRomsMame(Emulator emu) {
        ObservableList<TreeItem<RomTreeItem>> c = romsTree.getRoot().getChildren();
        c.clear();

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
                    TreeItem<RomTreeItem> item = new TreeItem<RomTreeItem>(new RomTreeItem(name,l));
                    c.add(item);
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
     */
    private void addRomsDefined(Emulator emu) {
        HashMap<String,List<String>> roms = emu.roms();
        boolean isMame = emu.isMame();
        if (isMame && roms.isEmpty() && !emu.emuBinary().isEmpty()) {
            // extract sets from mame
            addRomsMame(emu);
        }
        else {
            // add sets to list
            ObservableList<TreeItem<RomTreeItem>> c = romsTree.getRoot().getChildren();
            for (final String s : roms.keySet()) {
                TreeItem<RomTreeItem> item = new TreeItem<RomTreeItem>(new RomTreeItem(s, roms.get(s)));
                c.add(item);
            }
        }
    }

    /**
     * add roms to the tree
     * @param emu the Emulator
     */
    private void addRoms(final Emulator emu) {
        _rootStage.getScene().setCursor(Cursor.WAIT);
        final ObservableList<TreeItem<RomTreeItem>> c = romsTree.getRoot().getChildren();
        c.clear();
        romsTree.getSelectionModel().clearSelection();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // add items
                HashMap<String, List<String>> roms = emu.roms();

                if (!roms.isEmpty() || emu.isMame()) {
                    // there's a predefined romset
                    addRomsDefined(emu);
                } else {
                    // scan filesystem
                    addRomsFs(emu);
                }

                // finally sort the list
                c.sort(new Comparator<TreeItem<RomTreeItem>>() {
                    @Override
                    public int compare(TreeItem<RomTreeItem> o1, TreeItem<RomTreeItem> o2) {
                        return o1.getValue().name().toLowerCase().compareTo(o2.getValue().name().toLowerCase());
                    }
                });

                selectRomLabel.setText("Select romset (" + romsTree.getRoot().getChildren().size() + ")");
                romsTree.getRoot().setExpanded(true);
                _rootStage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    /**
     * refresh romset list
     * @param event the MouseEvent
     */
    private void refreshRomsClick(MouseEvent event) {
        if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
            Emulator emu = emuCombo.getValue();
            addRoms(emu);
        }
    }

    /**
     * clear rw folder
     * @param event the MouseEvent
     */
    private void clearRwClick(MouseEvent event) {
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

    /**
     * browser forward click
     * @param event the MouseEvent
     */
    private void browseNextClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            WebHistory h = infoWeb.getEngine().getHistory();
            try {
                h.go(+1);
            }
            catch (IndexOutOfBoundsException e) {

            }
        }
    }

    /**
     * browser back click
     * @param event the MouseEvent
     */
    private void browsePrevClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            WebHistory h = infoWeb.getEngine().getHistory();
            try {
                h.go(-1);
            }
            catch (IndexOutOfBoundsException e) {

            }
        }
    }

    /**
     * browse for emulator binary
     * @param event the MouseEvent
     */
    private void browseEmuBinaryClick(MouseEvent event) {
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

    /**
     * browse for roms
     * @param event the MouseEvent
     */
    private void browseRomsClick(MouseEvent event) {
        if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
            // browse for roms and update tree
            showSetFolderDialog(emuCombo.getValue());
            addRoms(emuCombo.getValue());
        }
    }

    /**
     * handle emu combobox change
     * @param oldValue the old Emulator value selected
     * @param newValue the new Emulator value selected
     */
    private void emuComboChange(Emulator oldValue, Emulator newValue) {
        if (oldValue == newValue) {
            return;
        }

        // write the new value to configuration
        Settings.getInstance().setLastSelectedEmu(newValue.name());
        try {
            Settings.getInstance().serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // setup ui elements
        if (newValue.allowMultiSelect()) {
            romsTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        }
        else {
            romsTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
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
            if (Utils.isFolderEmpty(newValue.rwFolder())) {
                clearRwButton.setVisible(false);
            }
            else {
                clearRwButton.setVisible(true);
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

    /**
     * handle emu binary text change
     * @param oldValue the old text
     * @param newValue the new text
     */
    private void emuBinTextChange(final String oldValue, final String newValue) {
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

    /**
     * handle emulator parameters text change
     * @param oldValue the old text
     * @param newValue the new text
     */
    private void emuParamsTextChange(final String oldValue, final String newValue) {
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

    /**
     * handle roms tree selection
     * @param c selections list
     */
    private void romsTreeIndexChange(ListChangeListener.Change<? extends Integer> c) {
        // consider the first selected item only
        TreeItem<RomTreeItem> item = romsTree.getSelectionModel().getSelectedItems().get(0);
        if (item != null) {
            searchInfo(item.getValue().name());
        }
    }

    /**
     * handle roms tree keypress (search)
     * @param event the KeyEvent
     */
    private void romsTreeKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // run emulator
            romsTree.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                    MouseButton.PRIMARY, 2, false, false, false, false, true, false, false, false, false, false, null));
            return;
        }
        else if (event.getCode().isLetterKey()) {
            if (_keyBuffer.isEmpty()) {
                // run a timer which will reset after 2 secs
                Timer t = new java.util.Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                      _keyBuffer = "";
                    }
                }, 2000);
            }
            _keyBuffer += event.getText();

            // search for an item starting with the pressed key
            ObservableList<TreeItem<RomTreeItem>> l = romsTree.getRoot().getChildren();
            FilteredList<TreeItem<RomTreeItem>> f = new FilteredList<TreeItem<RomTreeItem>>(l, new Predicate<TreeItem<RomTreeItem>>() {
                @Override
                public boolean test(TreeItem<RomTreeItem> romTreeItemTreeItem) {
                    if (romTreeItemTreeItem.getValue().name().toLowerCase().startsWith(_keyBuffer.toLowerCase())) {
                        return true;
                    }
                    return false;
                }
            });
            if (!f.isEmpty()) {
                // scroll to the first found item
                romsTree.scrollTo(f.getSourceIndex(0));
                romsTree.getSelectionModel().select(f.getSourceIndex(0));
            }
        }
    }

    /**
     * handle romstree clicks (doubleclick only, to run emulator)
     * @param event the MouseEvent
     */
    private void romsTreeClick(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }

        Emulator emu = emuCombo.getValue();
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

        // run emulator
        try {
            runEmulator(emu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize application
     * @param root the root stage
     * @return 0 on success
     */
    public int initController(Stage root) {
        _rootStage = root;

        // handle refresh roms button
        refreshTreeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshRomsClick(event);
            }
        });

        // handle clear rw button
        clearRwButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clearRwClick(event);
            }
        });

        // handle browse for emulator binary click / change text
        browseEmuBinaryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseEmuBinaryClick(event);
            }
        });

        // handle combo selection
        emuCombo.valueProperty().addListener(new ChangeListener<Emulator>() {
            @Override
            public void changed(ObservableValue<? extends Emulator> observable, Emulator oldValue, Emulator newValue) {
                emuComboChange(oldValue, newValue);
            }
        });

        emuBinText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                emuBinTextChange(oldValue, newValue);
            }
        });

        // handle browsefolder button click
        browseFolderButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseRomsClick(event);
            }
        });

        // handle params editing
        emuParamsText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                emuParamsTextChange(oldValue, newValue);
            }
        });

        romsTree.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                romsTreeIndexChange(c);
            }
        });

        // handle tree clicks
        romsTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                romsTreeClick(event);
            }
        });

        // simple search
        romsTree.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent event) {
                romsTreeKeyPressed(event);
            }
        });

        // back webview button
        backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browsePrevClick(event);
            }
        });

        // forward webview button
        fwdButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseNextClick(event);
            }
        });

        // init tree
        romsTree.setRoot(new TreeItem<RomTreeItem>());
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
