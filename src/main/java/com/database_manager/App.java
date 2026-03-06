package com.database_manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) {
        database_manager db = new database_manager();
        UIHandler ui = new UIHandler(db);

        Scene scene = new Scene(ui.buildRoot(), 900, 600);
        stage.setTitle("MooseDB Manager"); // I have named the project
        stage.setScene(scene);
        
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("Save before exiting?");
            alert.setContentText("Do you want to save your changes before closing?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ui.onSaveProject();
            }
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class database_manager {

    private Database database = new Database(); // will not be final, because it can be loaded from a file.

    // handle updates through ui handler
    public boolean add_new_table(String tableName) {
        if (database.table_names().contains(tableName)) { // check to make sure we're not overwriting a table
            return false;
        }
        database.new_table(tableName);
        return true;
    }

    public boolean remove_table(String tableName) {
        if (database.table_names().contains(tableName)) {
            database.delete_table(tableName);
            return true;
        }
        return false;
    }

    public Map get_values(String tableName) throws NoSuchElementException {
        if (database.table_names().contains(tableName)) {
            return database.get_values(tableName);
        }
        throw new NoSuchElementException();
    }

    public Set<String> get_tablenames(String tableName) {
        return database.table_names();
    }

    public void set_database(Database d) {
        this.database = d;
    }
    public Database get_Database() {
        return this.database;
    }

}


class Database {
    Map<String, Map<String, Data>> tables = new HashMap<>(); // creates a set of tables.

    public void new_table(String tableName) {
        tables.put(tableName, new HashMap<>()); // should make a new empty table
    }
    public void delete_table(String tableName) {
        tables.remove(tableName);
    }
    public Map<String, Data> get_values(String tableName) { // gets the table from the tableName
        return tables.get(tableName);
    }
    public Set<String> table_names() {
        return tables.keySet();
    }
    public int table_length(String tableName) {
        return get_values(tableName).size(); // should return an int equal to the length of specified table
    }

    public Vector<String> get_main_table() {
        Vector<String> all_names = new Vector<>();

        for (String key : table_names()) {
            all_names.add(key);
        }
        return all_names;
    }
    public Map<String, Data> get_table(String tablename) {
        return tables.get(tablename);
    }


    public void add_to_table(String tableName, String key, Data data) {
        tables.get(tableName).put(key, data);
    }

    public void remove_from_table(String tableName, int index) {
        tables.get(tableName).remove(String.valueOf(index));
    }

    public void remove_from_table(String tableName, String index) { // overload function just incase
        tables.get(tableName).remove(index);
    }

    public Data pop_back(String tableName) {
        String index = String.valueOf(table_length(tableName)-1);
        Data temp = tables.get(tableName).get(index);
        remove_from_table(tableName, index);
        return temp;
    }
}

// Data Variations and Classes

abstract class Data {
    // contains abstract functions that allow for inheritance
    protected // proteected, not private.
        int index; // index is not set yet

    
    public int get_index() {
        return this.index;
    }

    public void update_index(int index) {
        this.index = index; // may cause issues later on. 
    }
    public abstract String get_type();
    public abstract String get_value();

}

class Data_Integer extends Data {
    private
        int value;

    public Data_Integer(int value, int index) {
        this.value = value;
        this.index = index; // set index according to available indexes, function found when making a dataint in database manager.
    }
    @Override
    public String get_value() {
        return "" + this.value;
    }
    @Override
    public String get_type() {
        return "Integer";
    }

    public void set_value(int value) {
        this.value = value;
    }
}

class Data_String extends Data {
    private
        String value;
    
    public Data_String(String value, int index) {
        this.value = value;
        this.index = index; // set index according to available indexes, function found when making a dataint in database manager.
    }
    
    @Override
    public String get_value() {
        return this.value;
    }
    @Override
    public String get_type() {
        return "String";
    }
    public void set_value(String value) {
        this.value = value;
    }
    
}

class Data_Double extends Data {
    private
        double value;
    
    public Data_Double(Double value, int index) {
        this.value = value;
        this.index = index; // set index according to available indexes, function found when making a dataint in database manager.
    }
    
    @Override
    public String get_value() {
        return "" + this.value;
    }
    @Override
    public String get_type() {
        return "Double";
    }
    public void set_value(Double value) {
        this.value = value;
    }
    
}

class Data_Paragraph extends Data {
    private
        String[] value;
    
    public Data_Paragraph(String[] value, int index) {
        this.value = value;
        this.index = index; // set index according to available indexes, function found when making a dataint in database manager.
    }
    
    @Override
    public String get_value() {
        String total = "";
        for (String line : value) {
            total += line + "\n";
        }
        return total;
    }
    @Override
    public String get_type() {
        return "Paragraph";
    }
    public void set_value(String[] value) {
        this.value = value;
    }

    public String get_line(int index) {
        return this.value[index];
    }
    
}

// removed data array: would have to make types for each datatype.

class RowData {

    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleStringProperty value;

    public RowData(String name, String type, String value) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.value = new SimpleStringProperty(value);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public String getName() {
        return name.get();
    }
    public String getType() {
        return type.get();
    }
    public String getValue() {
        return value.get();
    }
    public Boolean isTable() {
        return getType().equals("Table");
    }
}

class UIHandler {

    private TableView<RowData> tableView; // tableview is a list of the row data for now.

    private final database_manager dbm; // allow access to the database manager for calling the functions inside
    private String current_table = "main";
    private Button newTableBtn;
    private Button newValueBtn;
    private Text currentDirectory = new Text("Table: " + current_table);

    public UIHandler(database_manager db) {
        this.dbm = db;
    }

    // for making the ui on first load
    public BorderPane buildRoot() {
        BorderPane root = new BorderPane();

        root.setTop(buildTopBar());
        root.setCenter(buildCenterTable());

        return root;
    }

    // the top bar that contains the buttons
    private ToolBar buildTopBar() {
        ToolBar toolBar = new ToolBar();

        Button saveProjectBtn = new Button("Save Project");
        Button loadProjectBtn = new Button("Load Project");
        newTableBtn = new Button("New Table");
        newValueBtn = new Button("New Value");
        Button homeBtn = new Button("Main Directory");
        Button modifyButton = new Button("Modify");

        // get presses of buttons and map them to functions
        saveProjectBtn.setOnAction(e -> onSaveProject());
        loadProjectBtn.setOnAction(e -> onLoadProject());
        newTableBtn.setOnAction(e -> onNewTable());
        newValueBtn.setOnAction(e -> onNewValue());
        homeBtn.setOnAction(eh->{
            current_table="main";
            refreshTable();
        });
        
        
        modifyButton.setOnAction(e -> onModifyButton());

        
        // very nicely decorated toolbar with buttons
        toolBar.getItems().addAll(
                saveProjectBtn,
                loadProjectBtn,
                new Separator(),
                newTableBtn,
                newValueBtn,
                new Separator(),
                homeBtn,
                currentDirectory,
                modifyButton
        );

        return toolBar;
    }


    @SuppressWarnings("deprecation") // we shall in fact dwell on things of the past because it helps make my code work
    private TableView<RowData> buildCenterTable() {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RowData, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<RowData, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> data.getValue().typeProperty());

        TableColumn<RowData, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(data -> data.getValue().valueProperty());

        tableView.getColumns().addAll(nameCol, typeCol, valueCol);

        refreshTable();

        setupRowDoubleClick();

        return tableView;
    }

    private void refreshTable() {
        if (current_table.equals("main")) {
            newTableBtn.setDisable(false);
            newValueBtn.setDisable(true);
        }
        else {
            newValueBtn.setDisable(false);
            newTableBtn.setDisable(true);
        }

        currentDirectory.setText("Table: " + current_table);
        try {
            ObservableList<RowData> tabledata = buildFromArray();
            if (tabledata != null) {
                tableView.setItems(tabledata);
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    private void setupRowDoubleClick() {
        tableView.setRowFactory(tv -> {
            TableRow<RowData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()
                        && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2) {

                    onRowDoubleClick(row.getItem());
                }
            });
            return row;
        });
    }

    private ObservableList<RowData> buildFromArray() {
        // gets values from dbm.database and then adds them to observable list to be built into the table
        ObservableList<RowData> list = FXCollections.observableArrayList();
        if (current_table.equals("main")) { // do not use == for strings. very annoying.
            for (String table : dbm.get_Database().get_main_table()) {
                list.add(new RowData(table, "Table", ""+dbm.get_Database().table_length(table)+" rows"));
            }
        }
        else {
            Map<String, Data> tableVector = dbm.get_Database().get_table(current_table);
            for (String data_key : tableVector.keySet()) {
                list.add(new RowData(data_key, tableVector.get(data_key).get_type(), tableVector.get(data_key).get_value()));
            }
        }
        return list;
    }

    public void onSaveProject() {
        System.out.println("Save Project clicked");
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Data.class, new DataTypeAdapter()).create();
        String fileName = saveDatabasePopup();
        if (fileName.isEmpty()) {
            System.out.println("Database name is empty: Could not save database.");
        }

        try (FileWriter writer = new FileWriter(fileName + ".json")) {
            // convert to java string and write it to a file
            gson.toJson(dbm.get_Database(), writer);
            System.out.println("Data saved to "+ fileName + ".json");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void onLoadProject() {
        FileChooser fileChooser = new FileChooser();
        String currentDir = System.getProperty("user.dir");
        File initialDir = new File(currentDir);
        fileChooser.setInitialDirectory(initialDir);

        fileChooser.setTitle("Load Project");
        Gson gson = new GsonBuilder().registerTypeAdapter(Data.class, new DataTypeAdapter()).create();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            System.out.println("Loading project from: " + selectedFile.getAbsolutePath());
            try {
                dbm.set_database(gson.fromJson(new FileReader(selectedFile), Database.class));
            }
            catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
        refreshTable();
    }

    private void onNewTable() {
        System.out.println("New Table clicked");
        newTablePopup();
        refreshTable();
    }
    private void onNewValue() {
        Map<String, Data> to_add = newPopup();
        for (String name : to_add.keySet()) {
            if (to_add.get(name) != null && !name.isEmpty()) {
            dbm.get_Database().add_to_table(current_table, name, to_add.get(name));
            refreshTable();
        
        }
        }
        
    }
    private void onModifyButton() {
        RowData selected_row = (RowData) tableView.getSelectionModel().getSelectedItem();
        if (selected_row != null) {
            if (selected_row.isTable()) {
                modifyTablePopup(selected_row.getName());
            }
            else {
                onRowDoubleClick(selected_row);
            }
        }
        refreshTable();
    }

    private void onRowDoubleClick(RowData row) {
        
        if (row.isTable()) {
            current_table = row.getName();
            System.out.println("Opened table: " + row.getName());
        }
        else {
            // modify row data with popup window
            System.out.println("Modifying value: " + row.getName());
            Map<String, Data> newRowMap = newPopup(row.getName(), row.getType(), row.getValue());
            for (String key : newRowMap.keySet()) {
                if (!key.isEmpty()) {
                    dbm.get_Database().get_table(current_table).remove(row.getName()); // remove old instance
                    dbm.get_Database().get_table(current_table).put(key, newRowMap.get(key)); // add new instance of it
                }
            }

        }
        
        refreshTable();
    }
    public Map<String, Data> newPopup() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Value to Table " + current_table);
        dialog.setHeaderText("Enter details for the new value:");

        Map<String, Data> a = new HashMap<>();


        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("String", "Integer", "Double", "Paragraph");
        typeCombo.setValue("String");

        TextField valueField = new TextField();
        valueField.setPromptText("Value");

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Value:"), 0, 2);
        grid.add(valueField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPadding(new Insets(10));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText();
            String type = typeCombo.getValue();
            String value = valueField.getText();

            Data to_add = null;
            switch (type) {
                case "String":
                    to_add = new Data_String(value, 0);
                    break;
                case "Integer":
                    try {
                        int intVal = Integer.parseInt(value);
                        to_add = new Data_Integer(intVal, 0);
                    } catch (NumberFormatException e) {
                        // Handle error, perhaps show alert
                    }
                    break;
                case "Double":
                    try {
                        double doubleVal = Double.parseDouble(value);
                        to_add = new Data_Double(doubleVal, 0);
                    } catch (NumberFormatException e) {
                        // Handle error
                    }
                    break;
                case "Paragraph":
                    String[] lines = value.split("\n");
                    to_add = new Data_Paragraph(lines, 0);
                    break;
            }
            
            a.put(name, to_add);
            
        }
        return a;
    }
    public Map<String, Data> newPopup(String name, String type, String value) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Value to Table " + current_table);
        dialog.setHeaderText("Enter details for the new value:");

        Map<String, Data> a = new HashMap<>();


        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setText(name);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("String", "Integer", "Double", "Paragraph");
        typeCombo.setValue(type);

        TextField valueField = new TextField();
        valueField.setPromptText("Value");
        valueField.setText(value);

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Value:"), 0, 2);
        grid.add(valueField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPadding(new Insets(10));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nameF = nameField.getText();
            String typeF = typeCombo.getValue();
            String valueF = valueField.getText();

            Data to_add = null;
            switch (typeF) {
                case "String":
                    to_add = new Data_String(valueF, 0);
                    break;
                case "Integer":
                    try {
                        int intVal = Integer.parseInt(valueF);
                        to_add = new Data_Integer(intVal, 0);
                    } catch (NumberFormatException e) {
                        // Handle error, perhaps show alert
                    }
                    break;
                case "Double":
                    try {
                        double doubleVal = Double.parseDouble(valueF);
                        to_add = new Data_Double(doubleVal, 0);
                    } catch (NumberFormatException e) {
                        // Handle error
                    }
                    break;
                case "Paragraph":
                    String[] lines = valueF.split("\n");
                    to_add = new Data_Paragraph(lines, 0);
                    break;
            }
            
            a.put(nameF, to_add);
            
        }
        return a;
    }
    public void newTablePopup() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Table " + current_table);
        dialog.setHeaderText("Enter details for the new table:");


        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPadding(new Insets(10));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nameF = nameField.getText();

            if (dbm.get_Database().get_table(nameF) == null) {
                dbm.add_new_table(nameF);
            }
            else {
                System.out.println("TABLE EXISTS: WILL NOT OVERRIDE DATA");
            }
            
        }
    }
    public void modifyTablePopup(String name) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modify Table " + name);
        dialog.setHeaderText("Enter details for table:");


        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setText(name);

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPadding(new Insets(10));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nameF = nameField.getText();

            if (dbm.get_Database().get_table(nameF) == null) {
                if (!nameF.equals(name)) { // if not new
                    Map<String, Data> tabledata = dbm.get_Database().get_table(name);
                    dbm.remove_table(name);
                    dbm.add_new_table(nameF);
                    for (String key : tabledata.keySet()) {
                        dbm.get_Database().get_table(nameF).put(key, tabledata.get(key));
                    }
                }
            } else {
                System.out.println("TABLE WITH NAME " + nameF + " EXISTS: Cancelling name change.");
            }
            
            
            
            
        }
    }
    public String saveDatabasePopup() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Save Database to file");
        dialog.setHeaderText("Enter a name for the saved database:");


        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Text(".json"), 2, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPadding(new Insets(10));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nameF = nameField.getText();

            return nameF;
            
        }
        return null;
    }
}

class DataTypeAdapter implements JsonSerializer<Data>, com.google.gson.JsonDeserializer<Data> {
    @Override
    public JsonElement serialize(Data src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getClass().getSimpleName());
        jsonObject.addProperty("index", src.get_index());
        jsonObject.addProperty("value", src.get_value());
        return jsonObject;
    }

    @Override
    public Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        int index = jsonObject.get("index").getAsInt();
        String value = jsonObject.get("value").getAsString();

        switch (type) {
            case "Data_String":
                return new Data_String(value, index);
            case "Data_Integer":
                return new Data_Integer(Integer.parseInt(value), index);
            case "Data_Double":
                return new Data_Double(Double.parseDouble(value), index);
            case "Data_Paragraph":
                String[] lines = value.split("\\n");
                return new Data_Paragraph(lines, index);
            default:
                throw new JsonParseException("Unknown type: " + type);
        }
    }
}