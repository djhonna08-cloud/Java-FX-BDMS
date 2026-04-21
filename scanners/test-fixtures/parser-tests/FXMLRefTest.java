package com.example;

public class FXMLRefTest {
    public void loadView() {
        FXMLLoader.load(getClass().getResource("main-view.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/views/dialog.fxml"));
    }
}
