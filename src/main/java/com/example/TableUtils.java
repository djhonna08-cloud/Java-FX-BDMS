package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.function.Function;
import java.util.function.Predicate;

public class TableUtils {
    
    /**
     * Enhanced table container with filtering and sorting
     */
    public static class EnhancedTable<T> {
        private final TableView<T> tableView;
        private final FilteredList<T> filteredData;
        private final SortedList<T> sortedData;
        private final VBox container;
        private final HBox filterBox;
        private final TextField globalSearchField;
        
        public EnhancedTable(TableView<T> tableView, ObservableList<T> data) {
            this.tableView = tableView;
            this.filteredData = new FilteredList<>(data);
            this.sortedData = new SortedList<>(filteredData);
            
            // Bind sorted data to table
            this.tableView.setItems(sortedData);
            this.sortedData.comparatorProperty().bind(tableView.comparatorProperty());
            
            // Configure table appearance
            this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            this.tableView.setPrefHeight(500);
            this.tableView.setMinHeight(400);
            
            // Create container
            this.container = new VBox();
            this.container.getStyleClass().add("enhanced-table-container");
            
            // Create filter controls
            this.filterBox = createFilterControls();
            this.globalSearchField = (TextField) filterBox.getChildren().get(1); // Second child is search field
            
            // Add components to container
            this.container.getChildren().addAll(filterBox, tableView);
            VBox.setVgrow(tableView, Priority.ALWAYS);
        }
        
        private HBox createFilterControls() {
            HBox controls = new HBox();
            controls.setAlignment(Pos.CENTER_LEFT);
            controls.getStyleClass().add("table-search-controls");
            
            Label searchLabel = new Label("Search:");
            searchLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 15px; -fx-text-fill: #374151; -fx-min-width: 60px;");
            
            TextField searchField = new TextField();
            searchField.setPromptText("Search all columns...");
            searchField.setPrefWidth(400);
            searchField.setMaxWidth(400);
            
            Button clearBtn = new Button("Clear");
            clearBtn.setGraphic(new FontIcon(FontAwesomeSolid.TIMES));
            clearBtn.getStyleClass().addAll("button-secondary");
            clearBtn.setPrefWidth(100);
            clearBtn.setOnAction(e -> {
                searchField.clear();
                clearAllFilters();
            });
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button exportBtn = new Button("Export");
            exportBtn.setGraphic(new FontIcon(FontAwesomeSolid.DOWNLOAD));
            exportBtn.getStyleClass().addAll("button-info");
            exportBtn.setPrefWidth(110);
            exportBtn.setTooltip(new Tooltip("Export table data"));
            
            controls.getChildren().addAll(searchLabel, searchField, clearBtn, spacer, exportBtn);
            
            return controls;
        }
        
        public void setGlobalFilter(Function<T, String> searchFunction) {
            globalSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(item -> {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return true;
                    }
                    
                    String searchText = newValue.toLowerCase();
                    String itemText = searchFunction.apply(item);
                    return itemText != null && itemText.toLowerCase().contains(searchText);
                });
            });
        }
        
        public void addColumnFilter(TableColumn<T, ?> column, Function<T, String> valueExtractor) {
            // Simple column header with just sorting capability
            Label headerLabel = new Label(column.getText());
            headerLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #374151;");
            
            HBox headerBox = new HBox(headerLabel);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(0, 8, 0, 8));
            headerBox.getStyleClass().add("table-filter-header");
            column.setGraphic(headerBox);
            column.setText("");
            
            // Make header clickable for sorting
            headerBox.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    // Toggle sort
                    if (tableView.getSortOrder().contains(column)) {
                        TableColumn.SortType currentSort = column.getSortType();
                        column.setSortType(currentSort == TableColumn.SortType.ASCENDING ? 
                            TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING);
                    } else {
                        tableView.getSortOrder().clear();
                        column.setSortType(TableColumn.SortType.ASCENDING);
                        tableView.getSortOrder().add(column);
                    }
                }
            });
        }
        
        private void clearAllFilters() {
            // Simple implementation - just clear the global search
            // Individual column filters are removed for simplicity
        }
        
        public VBox getContainer() {
            return container;
        }
        
        public TableView<T> getTableView() {
            return tableView;
        }
        
        public void refreshData(ObservableList<T> newData) {
            filteredData.setAll(newData);
        }
    }
    
    /**
     * Create an enhanced table with filtering and sorting
     */
    public static <T> EnhancedTable<T> createEnhancedTable(TableView<T> tableView, ObservableList<T> data) {
        return new EnhancedTable<>(tableView, data);
    }
}