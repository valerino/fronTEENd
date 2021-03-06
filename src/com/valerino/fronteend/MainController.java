package com.valerino.fronteend;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
import java.util.function.Predicate;

/**
 * controller for the main dialog
 * Created by valerino on 09/10/14.
 */
public class MainController {
    @FXML
    private WebView infoWeb;

    @FXML
    private ListView<RomItem> romsList;

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
    private Button refreshRomsButton;

    @FXML
    private Button clearRwButton;

    @FXML
    private CheckBox rwCheckBox;

    @FXML
    private Accordion cfgAccordion;

    @FXML
    private Button rescanButton;

    @FXML
    private Button romSearchButton;

    @FXML
    private TextField systemText;

    @FXML
    private Button romSearchNextButton;

    @FXML
    private TextField romSearchText;

    private Stage _rootStage;

    private String _keyBuffer = "";

    private FilteredList<RomItem> _searchList = null;

    private int _searchCurrentIndex = 0;

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

        infoWeb.getEngine().setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
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
        vb.setMargin(customParams, new Insets(4, 4, 4, 4));
        vb.getChildren().add(okButton);
        vb.setMargin(okButton, new Insets(4, 4, 4, 4));

        // set button to close dialog
        final String[] newParams = {params};
        okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    newParams[0] = customParams.getText();
                    st.close();
                }
            }
        });

        // do the same with the enter key pressed on button
        okButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    // run emulator
                    Utils.nodeClick(okButton, MouseButton.PRIMARY, 1);
                    return;
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

        st.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
        st.setScene(new Scene(vb));
        st.setResizable(false);
        st.setTitle("Custom parameters");
        st.initModality(Modality.WINDOW_MODAL);
        st.initOwner(_rootStage.getScene().getWindow());
        st.centerOnScreen();
        st.showAndWait();

        // return parameters
        return newParams[0];
    }

    /**
     * show the set selection box
     * @param list list to be shown
     * @return
     */
    private List<RomItem> getSetBoxSelection(final List<RomItem> list) {
        final Stage st = new Stage(StageStyle.UNIFIED);
        st.setWidth(640);
        final ListView<RomItem> lv = new ListView<RomItem>();
        if (emuCombo.getValue().allowMultiSelect()) {
            lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        }
        else {
            lv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

        // add sets
        for (final RomItem r : list) {
            lv.getItems().add(r);
        }

        final List<RomItem> returnList = new ArrayList<RomItem>();

        // handle doubleclicks
        lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // doubleclick
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() != 2) {
                    return;
                }
                // middle click (for multiple selection)
                if (event.getButton() == MouseButton.MIDDLE && event.getClickCount() != 1) {
                    return;
                }

                // add items to list and close
                ObservableList<RomItem> l = lv.getSelectionModel().getSelectedItems();
                for (final RomItem i : l) {
                    returnList.add(i);
                }
                st.close();
            }
        });

        // handle keypress
        lv.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    // run emulator
                    Utils.nodeClick(lv, MouseButton.PRIMARY, 2);
                    return;
                }
            }
        });

        // show
        VBox vb = new VBox();
        vb.getChildren().add(lv);
        st.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
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
     * get the roms list selection, decompress if needed and return an array of the found sets
     * @return
     */
    private List<RomItem> getRomsListSelection() {
        Emulator emu = emuCombo.getValue();
        List<RomItem> selection = new ArrayList<RomItem>();
        ObservableList<RomItem> l = romsList.getSelectionModel().getSelectedItems();

        // check if the emulator has a predefined romset
        if (!emu.roms().isEmpty() || emu.isMame()) {
            // emulator has a predefined set, multiselection is not allowed.
            // we just get the sets into the selection list
            RomItem it = l.get(0);
            for (final String s : it.sets()) {
                RomItem i = new RomItem(s);
                selection.add(i);
            }
        }
        else {
            // check each selection File and copy them to the temporary folder
            final File dstFolder = Settings.getInstance().tmpFolder();
            Utils.clearFolder(dstFolder);
            for (RomItem it : l) {
                // dump to rw folder
                if (Utils.isCompressed(it.file())) {
                    // ask for 7z if needed
                    int sevenZipAvailable = Settings.getInstance().setup7z();
                    if (sevenZipAvailable == 0) {
                        // decompress
                        int res = Utils.decompress(Settings.getInstance().sevenZipPath(), it.file(), dstFolder);
                        if (res != 0) {
                            return null;
                        }
                    }
                    else {
                        // just copy, may not work if the emulator do not support compression
                        try {
                            Utils.copyFile(it.file(),new File (dstFolder,it.file().getName()),false);
                        } catch (IOException e) {
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
                    RomItem i = new RomItem(f);
                    selection.add(i);
                }
            }
        }

        // sort the selection
        selection.sort(new Comparator<RomItem>() {
            @Override
            public int compare(RomItem o1, RomItem o2) {
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
     * enable/disable the clear rw folder button
     * @param emu the currently selected emulator
     */
    private void evaluateRwFolder(Emulator emu) {
        if (emu.rwFolder() != null) {
            if (Utils.isFolderEmpty(emu.rwFolder())) {
                clearRwButton.setVisible(false);
            }
            else {
                clearRwButton.setVisible(true);
            }
        }
    }

    /**
     * load emulator with the choosen rom/set
     * @param emu the emulator
     * @throws IOException
     */
    private void runEmulator(final Emulator emu) throws IOException {
        romsList.setCursor(Cursor.WAIT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // get the selected items
                List<RomItem> l = getRomsListSelection();
                romsList.setCursor(Cursor.DEFAULT);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if (l == null || l.isEmpty()) {
                    return;
                }

                if (emu.supportRw()) {
                    // handle r/w support
                    for (RomItem i : l) {
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
                                        evaluateRwFolder(emu);
                                    }
                                }
                            }
                        }
                        if (overwrite && rwCheckBox.isSelected()) {
                            // copy to r/w folder
                            boolean r = false;
                            try {
                                r = Utils.copyFile(i.file(), f, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                List<String> arFinal = new ArrayList<String>();
                boolean removeNext = false;
                for (int i = 0; i < ar.size(); i++) {
                    final String s = ar.get(i);
                    if (s.endsWith("%")) {
                        if (emu.removeUnmatchedRomPrefix()) {
                            // remove the string before aswell
                            arFinal.remove(i -1);
                        }
                    }
                    else {
                        arFinal.add(s);
                    }
                }

                String[] cmdLine = { };
                cmdLine = arFinal.toArray(cmdLine);

                // run emulator
                _rootStage.getScene().setCursor(Cursor.WAIT);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final String[] finalCmdLine = cmdLine;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Utils.runProcess(finalCmdLine, !emu.noCheckReturn());
                        _rootStage.getScene().setCursor(Cursor.DEFAULT);

                        // evaluate the rwfolder again
                        evaluateRwFolder(emu);
                    }
                });
            }
        });
    }

    /**
     * show the browse for folder dialog
     * @param emu the Emulator
     */
    private void getRomsFolder(Emulator emu) {
        FileChooser fc = new FileChooser();
        DirectoryChooser dc = new DirectoryChooser();
        if (!emu.lastFolder().isEmpty()) {
            dc.setInitialDirectory(new File (emu.lastFolder()));
        }
        File lastFolder = null;
        dc.setTitle("Select roms folder for " + emu.name());
        try {
            lastFolder = dc.showDialog(_rootStage.getOwner());
        }
        catch (IllegalArgumentException ex) {
            // folder was not existent, reset and reissue
            dc.setInitialDirectory(null);
            lastFolder = dc.showDialog(_rootStage.getOwner());
        }
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
     * add roms to the listview (from filesystem)
     * @param emu the Emulator
     */
    private void addRomsFs(Emulator emu) {
        if (emu.lastFolder().isEmpty()) {
            // we have to set lastfolder first
            getRomsFolder(emu);
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

        // fill listview
        ObservableList<RomItem> c = romsList.getItems();
        for (File rom : files) {
            RomItem item = new RomItem(rom);
            c.add(item);
        }
    }

    /**
     * add roms from mame
     * @param emu the Emulator
     */
    private void addRomsMame(Emulator emu) {
        ObservableList<RomItem> c = romsList.getItems();
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
                    RomItem item = new RomItem(name,l);
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
     * add roms to the listview (from defined sets)
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
            ObservableList<RomItem> c = romsList.getItems();
            for (final String s : roms.keySet()) {
                RomItem item = new RomItem(s, roms.get(s));
                c.add(item);
            }
        }
    }

    /**
     * add roms to the listview
     * @param emu the Emulator
     */
    private void addRoms(final Emulator emu) {
        _rootStage.getScene().setCursor(Cursor.WAIT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ObservableList<RomItem> c = romsList.getItems();
        c.clear();
        romsList.getSelectionModel().clearSelection();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // show the warning box if present
                showWarningBox(emu);

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
                c.sort(new Comparator<RomItem>() {
                    @Override
                    public int compare(RomItem o1, RomItem o2) {
                        return o1.name().toLowerCase().compareTo(o2.name().toLowerCase());
                    }
                });

                selectRomLabel.setText("Select romset (" + romsList.getItems().size() + ")");
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
     * rescan emulators
     * @param event the MouseEvent
     */
    private void rescanButtonClick(MouseEvent event) {
        if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
            emuCombo.getItems().clear();
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
                Utils.clearFolder(emuCombo.getValue().rwFolder());
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
            // browse for roms and update listview
            getRomsFolder(emuCombo.getValue());
            addRoms(emuCombo.getValue());
        }
    }

    /**
     * search for rom containing text
     * @param event the MouseEvent
     */
    private void searchRomClick(MouseEvent event) {
        if (event.getButton().compareTo(MouseButton.PRIMARY) == 0 && !romSearchText.getText().isEmpty()) {
            // search for items containing the given text
            ObservableList<RomItem> l = romsList.getItems();
            _searchList = new FilteredList<RomItem>(l, new Predicate<RomItem>() {
                @Override
                public boolean test(RomItem item) {
                    if (item.name().toLowerCase().contains(romSearchText.getText().toLowerCase())) {
                        return true;
                    }
                    return false;
                }
            });

            if (_searchList.isEmpty()) {
                // no roms found
                Alert al = new Alert(Alert.AlertType.WARNING, romSearchText.getText() + "\nnot found");
                al.showAndWait();
                return;
            }

            // go to the first found item
            _searchCurrentIndex = 0;
            if (_searchList.size() > 1) {
                // show the next button
                romSearchNextButton.setVisible(true);
            }
            else {
                // we don't need the next button
                romSearchNextButton.setVisible(false);
            }
            searchRomInternal(_searchCurrentIndex);
        }
    }

    /**
     * search for the next rom containing text
     * @param event the MouseEvent
     */
    private void searchNextClick(MouseEvent event) {
        if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
            // go to the next item if it exists, or overlap
            _searchCurrentIndex++;
            searchRomInternal(_searchCurrentIndex);
        }
    }

    /**
     * internal search routine (for advanced rom search)
     * @param index index in the search list
     */
    private void searchRomInternal (int index) {
        // scroll to the item index, or revert to 0
        int idx = index;
        if (index >= _searchList.size()) {
            idx = 0;
            _searchCurrentIndex = idx;
        }
        romsList.scrollTo(_searchList.getSourceIndex(idx));
        romsList.getSelectionModel().select(_searchList.getSourceIndex(idx));
    }

    /**
     * handle emu combobox change
     * @param oldValue the old Emulator value selected
     * @param newValue the new Emulator value selected
     */
    private void emuComboChange(Emulator oldValue, Emulator newValue) {
        if (oldValue == newValue || newValue == null) {
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
            romsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        }
        else {
            romsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        emuBinText.setText(newValue.emuBinary());
        emuParamsText.setText(newValue.emuParams());
        customParamsCheckBox.setSelected(false);
        systemText.setText(newValue.system());

        if (!newValue.roms().isEmpty() || newValue.isMame()) {
            browseFolderButton.setVisible(false);
        }
        else {
            browseFolderButton.setVisible(true);
        }

        if (!newValue.roms().isEmpty()) {
            refreshRomsButton.setVisible(false);
        }
        else {
            refreshRomsButton.setVisible(true);
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
        }

        if (newValue.isMame() && newValue.emuBinary().isEmpty()) {
            // mame and no binary set -> we can't query the emulator for roms
            Utils.nodeClick(browseEmuBinaryButton, MouseButton.PRIMARY, 1);
        }

        // fill the roms listview
        Utils.nodeClick(refreshRomsButton, MouseButton.PRIMARY, 1);
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
     * handle system search string text change
     * @param oldValue the old text
     * @param newValue the new text
     */
    private void systemTextChange(final String oldValue, final String newValue) {
        if (newValue.compareTo(oldValue) != 0) {
            Emulator emu = emuCombo.getValue();
            emu.setSystemSearchString(newValue);
            try {
                emu.serialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * handle roms listview selection
     * @param c selections list
     */
    private void romsListIndexChange(ListChangeListener.Change<? extends Integer> c) {
        // consider the first selected item only
        RomItem item = romsList.getSelectionModel().getSelectedItems().get(0);
        if (item != null) {
            searchInfo(item.name());
        }
    }

    /**
     * handle roms list keypress (search)
     * @param event the KeyEvent
     */
    private void romsListKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // run emulator
            Utils.nodeClick(romsList, MouseButton.PRIMARY, 2);
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
            ObservableList<RomItem> l = romsList.getItems();
            FilteredList<RomItem> f = new FilteredList<RomItem>(l, new Predicate<RomItem>() {
                @Override
                public boolean test(RomItem item) {
                    if (item.name().toLowerCase().startsWith(_keyBuffer.toLowerCase())) {
                        return true;
                    }
                    return false;
                }
            });
            if (!f.isEmpty()) {
                // scroll to the first found item
                romsList.scrollTo(f.getSourceIndex(0));
                //romsList.getSelectionModel().select(f.getSourceIndex(0));
            }
        }
    }

    /**
     * handle roms listview clicks (doubleclick only, to run emulator)
     * @param event the MouseEvent
     */
    private void romsListDoubleClick(MouseEvent event) {
        // doubleclick
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() != 2) {
            return;
        }
        // middle click (for multiple selection)
        if (event.getButton() == MouseButton.MIDDLE && event.getClickCount() != 1) {
            return;
        }

        Emulator emu = emuCombo.getValue();
        if (emu.emuBinary().isEmpty()) {
            Alert al = new Alert(Alert.AlertType.WARNING, "Please select an emulator binary first");
            al.showAndWait();
            cfgAccordion.setExpandedPane(cfgAccordion.getPanes().get(1));
            Utils.nodeClick(browseEmuBinaryButton, MouseButton.PRIMARY, 1);
            return;
        }
        if (emu.emuParams().isEmpty()) {
            Alert al = new Alert(Alert.AlertType.WARNING, "Please fill emulator parameters first");
            al.showAndWait();
            cfgAccordion.setExpandedPane(cfgAccordion.getPanes().get(1));
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
     * application onclose handler
     * @param event the event
     */
    private void onAppClose(WindowEvent event) {
        if (!Settings.getInstance().showExitConfirmation()) {
            // do not show
            return;
        }

        // use a modified alert here
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, null);
        VBox vb = new VBox(8);
        Label l = new Label("Are you sure you want to quit ?");
        final CheckBox cb = new CheckBox("Do not show anymore");
        vb.getChildren().add(l);
        vb.getChildren().add(cb);
        alert.getDialogPane().setContent(vb);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.get() == ButtonType.OK) {
            // update the show confirmation state, cleanup and exit
            Settings.getInstance().setShowExitConfirmation(!cb.isSelected());
            try {
                Settings.getInstance().serialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.clearFolder(Settings.getInstance().tmpFolder());
            return;
        }
        else {
            // ignore
            event.consume();
        }
    }

    /**
     * show the warning box (for particular settings, ...) if present
     * @param emu the Emulator
     */
    private void showWarningBox(final Emulator emu) {
        if (emu.warningText().isEmpty() || !emu.warningTextShow()) {
            // no warning
            return;
        }

        // use a modified alert here
        final Alert alert = new Alert(Alert.AlertType.WARNING, null);
        VBox vb = new VBox(8);
        Label l = new Label(emu.warningText());
        final CheckBox cb = new CheckBox("Do not show anymore");
        vb.getChildren().add(l);
        vb.getChildren().add(cb);
        alert.getDialogPane().setContent(vb);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Optional<ButtonType> res = alert.showAndWait();
                if (res.get() == ButtonType.OK) {
                    // update the show warning state
                    emu.setWarningTextShow(!cb.isSelected());
                    try {
                        emu.serialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        });
    }

    /**
     * initialize application
     * @param root the root stage
     * @return 0 on success
     */
    public int initController(Stage root) {
        _rootStage = root;

        // set onclose button
        _rootStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                onAppClose(event);
            }
        });

        // handle refresh roms button
        refreshRomsButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshRomsClick(event);
            }
        });
        refreshRomsButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(refreshRomsButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle clear rw button
        clearRwButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clearRwClick(event);
            }
        });
        clearRwButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(clearRwButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle browse for emulator binary click / change text
        browseEmuBinaryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseEmuBinaryClick(event);
            }
        });
        browseEmuBinaryButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(browseEmuBinaryButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle combo selection
        emuCombo.valueProperty().addListener(new ChangeListener<Emulator>() {
            @Override
            public void changed(ObservableValue<? extends Emulator> observable, Emulator oldValue, Emulator newValue) {
                emuComboChange(oldValue, newValue);
            }
        });

        // emulator binary path change
        emuBinText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                emuBinTextChange(oldValue, newValue);
            }
        });

        // emulator system search string change
        systemText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                systemTextChange(oldValue, newValue);
            }
        });

        // handle browsefolder button click
        browseFolderButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseRomsClick(event);
            }
        });
        browseFolderButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(browseFolderButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle params editing
        emuParamsText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                emuParamsTextChange(oldValue, newValue);
            }
        });

        // handle websearch on selection
        romsList.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                romsListIndexChange(c);
            }
        });

        // handle rescan button clicks
        rescanButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                rescanButtonClick(event);
            }
        });
        rescanButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(rescanButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle search rom clicks
        romSearchButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                searchRomClick(event);
            }
        });
        romSearchButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(romSearchButton, MouseButton.PRIMARY, 1);
                }
            }
        });
        romSearchNextButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                searchNextClick(event);
            }
        });
        romSearchNextButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(romSearchNextButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // handle listview clicks
        romsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                romsListDoubleClick(event);
            }
        });

        // simple search
        romsList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent event) {
                romsListKeyPressed(event);
            }
        });

        // back webview button
        backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browsePrevClick(event);
            }
        });
        backButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(backButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // forward webview button
        fwdButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                browseNextClick(event);
            }
        });
        fwdButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Utils.nodeClick(fwdButton, MouseButton.PRIMARY, 1);
                }
            }
        });

        // load settings
        try {
            Settings.getInstance().initialize();
        }
        catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        // scan emulators
        Utils.nodeClick(rescanButton, MouseButton.PRIMARY, 1);
        return 0;
    }
}
