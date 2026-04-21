package com.example;

import javafx.scene.control.TextField;

public class FieldTest {
    @FXML
    private TextField nameField;
    
    @FXML
    @Inject
    private TableView<Resident> residentTable;
    
    private String regularField;
    
    public static final int CONSTANT = 42;
}
