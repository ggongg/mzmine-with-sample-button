/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */


package io.github.mzmine.project.parameterssetup;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.gui.helpwindow.HelpWindow;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.StringParameter;
import io.github.mzmine.project.parameterssetup.ProjectMetadataParameters.AvailableTypes;
import io.github.mzmine.project.parameterssetup.columns.DoubleMetadataColumn;
import io.github.mzmine.project.parameterssetup.columns.DateMetadataColumn;
import io.github.mzmine.project.parameterssetup.columns.MetadataColumn;
import io.github.mzmine.project.parameterssetup.columns.StringMetadataColumn;
import io.github.mzmine.util.ExitCode;
import java.time.LocalDateTime;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

public class ProjectParametersSetupDialogController {

  private Logger logger = Logger.getLogger(this.getClass().getName());
  private final MZmineProject currentProject = MZmineCore.getProjectManager().getCurrentProject();
  private final MetadataTable metadataTable = currentProject.getProjectMetadata();
  private Stage currentStage;
  private RawDataFile[] fileList;

  @FXML
  private TableView<ObservableList<StringProperty>> parameterTable;

  @FXML
  private void initialize() {
    parameterTable.setEditable(true);
    parameterTable.getSelectionModel().setCellSelectionEnabled(true);
    fileList = currentProject.getDataFiles();
    updateParametersToTable();
  }

  public void setStage(Stage stage) {
    currentStage = stage;
    stage.setOnCloseRequest(we -> {
      logger.info("Parameters are not updated");
    });
  }

  /**
   * Render the table using the data from the project parameters structure.
   */
  private void updateParametersToTable() {
    parameterTable.getItems().clear();
    parameterTable.getColumns().clear();

    int columnsNumber = metadataTable.getColumns().size();
    if (columnsNumber == 0) {
      return;
    }

    // display the columns
    TableColumn[] tableColumns = new TableColumn[columnsNumber + 1];
    tableColumns[0] = createColumn(0, "Data File");
    var columns = metadataTable.getColumns();
    int columnId = 1;
    for (var col : columns) {
      tableColumns[columnId] = createColumn(columnId, col.getTitle());
      columnId++;
    }
    parameterTable.getColumns().addAll(tableColumns);

    // display each row of the table
    ObservableList<ObservableList<StringProperty>> tableRows = FXCollections.observableArrayList();
    for (RawDataFile rawFile : fileList) {
      ObservableList<StringProperty> fileParametersValue = FXCollections.observableArrayList();
      fileParametersValue.add(new SimpleStringProperty(rawFile.getName()));
      for (MetadataColumn<?> column : columns) {
        // either convert parameter value to string or display an empty string in case if it's unset
        Object value = metadataTable.getValue(column, rawFile);
        fileParametersValue.add(new SimpleStringProperty(value == null ? "" : value.toString()));
      }
      tableRows.add(fileParametersValue);
    }
    parameterTable.getItems().addAll(tableRows);
  }

  private TableColumn<ObservableList<StringProperty>, String> createColumn(final int columnIndex,
      String columnTitle) {
    // validate the column title (assign the default value in case if it's empty)
    TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
    String title;
    if (columnTitle == null || columnTitle.trim().length() == 0) {
      title = "Column " + (columnIndex + 1);
    } else {
      title = columnTitle;
    }

    column.setText(title);
    // define what the cell value would be
    column.setCellValueFactory(cellDataFeatures -> {
      ObservableList<StringProperty> values = cellDataFeatures.getValue();
      if (columnIndex >= values.size()) {
        return new SimpleStringProperty("");
      } else {
        return cellDataFeatures.getValue().get(columnIndex);
      }
    });

    // won't be applied for the first column, because it contains the file name
    if (columnIndex != 0) {
      column.setCellFactory(TextFieldTableCell.forTableColumn());
      column.setOnEditCommit(event -> {
        String parameterValueNew = event.getNewValue();
        String parameterName = event.getTableColumn().getText().trim();
        MetadataColumn<?> parameter = metadataTable.getColumnByName(parameterName);

        // define RawDataFile name
        int rowNumber = parameterTable.getSelectionModel().selectedIndexProperty().get();
        String fileName = parameterTable.getItems().get(rowNumber).get(0).getValue();
        RawDataFile rawDataFile = null;
        for (RawDataFile file : fileList) {
          if (file.getName().equals(fileName)) {
            rawDataFile = file;
            break;
          }
        }

        // pattern match the metadata column type
        // derive the example value from the parameter's type
        String parameterMatchedType = "undef";
        String parameterMatchedExample = "undef";
        Object parameterMatchedDefaultValue = null;
        MetadataColumn parameterMatched = switch (parameter) {
          case StringMetadataColumn stringMetadataColumn -> {
            parameterMatchedType = "String";
            parameterMatchedExample = "\"String\"";
            parameterMatchedDefaultValue = "defaultString";
            yield stringMetadataColumn;
          }
          case DoubleMetadataColumn doubleMetadataColumn -> {
            parameterMatchedType = "Double";
            parameterMatchedExample = "\"1.46\"";
            parameterMatchedDefaultValue = 1.621;
            yield doubleMetadataColumn;
          }
          case DateMetadataColumn dateMetadataColumn -> {
            parameterMatchedType = "Datetime";
            parameterMatchedExample = "\"2022-12-24T10:11:36\"";
            parameterMatchedDefaultValue = LocalDateTime.now();
            yield dateMetadataColumn;
          }
        };

        // if the parameter value is in the right format then save it to the metadata table,
        // otherwise show alert dialog
        Object convertedParameterInput = parameterMatched.convert(parameterValueNew,
            parameterMatchedDefaultValue);
        if (parameter.checkInput(convertedParameterInput)) {
          metadataTable.setValue(parameterMatched, rawDataFile, convertedParameterInput);
        } else {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Wrong parameter value format");
          alert.setHeaderText(null);
          alert.setContentText(
              "Please respect the " + parameterMatchedType + " parameter value format, e.g. "
                  + parameterMatchedExample);
          alert.showAndWait();
        }
        // need to render
        updateParametersToTable();
      });
    }

    column.setMinWidth(175.0);

    return column;
  }

  @FXML
  public void addPara(ActionEvent actionEvent) {
    ProjectMetadataParameters projectMetadataParameters = new ProjectMetadataParameters();
    ExitCode exitCode = projectMetadataParameters.showSetupDialog(true);

    StringParameter parameterTitle = projectMetadataParameters.getParameter(
        ProjectMetadataParameters.title);
    ComboParameter<String> parameterType = projectMetadataParameters.getParameter(
        ProjectMetadataParameters.valueType);

    if (exitCode == ExitCode.OK) {
      // in case if the new parameter is not unique
      if (metadataTable.getColumnByName(parameterTitle.getValue()) != null) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parameter already present");
        alert.setHeaderText(null);
        alert.setContentText("Please enter unique parameter name.");
        alert.showAndWait();
        return;
      }

      // add the new column to the parameters table
      switch (AvailableTypes.valueOf(parameterType.getValue())) {
        case TEXT -> {
          metadataTable.addColumn(new StringMetadataColumn(parameterTitle.getValue()));
        }
        case DOUBLE -> {
          metadataTable.addColumn(new DoubleMetadataColumn(parameterTitle.getValue()));
        }
        case DATETIME -> {
          metadataTable.addColumn(new DateMetadataColumn(parameterTitle.getValue()));
        }
      }
      // need to render
      updateParametersToTable();
    }
  }

  @FXML
  public void importPara(ActionEvent actionEvent) {
    ProjectParametersImporter importer = new ProjectParametersImporter(currentStage);
    if (importer.importParameters()) {
      logger.info("Successfully imported parameters from file");
      updateParametersToTable();
    } else {
      logger.info("Importing parameters from file failed");
    }
  }

  @FXML
  public void removePara(ActionEvent actionEvent) {
    TableColumn column = parameterTable.getFocusModel().getFocusedCell().getTableColumn();
    if (column == null) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No cell selected");
      alert.setHeaderText(null);
      alert.setContentText("Please select at least one cell.");
      alert.showAndWait();
      return;
    }
    String parameterName = column.getText();
    if (parameterName.equals("Data File")) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Cannot remove Raw Data File Column");
      alert.setHeaderText(null);
      alert.setContentText("Please select cell from another column.");
      alert.showAndWait();
      return;
    }
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remove parameter " + parameterName);
    alert.setTitle("Remove Parameter?");
    alert.setHeaderText(null);
    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      MetadataColumn<?> tbdParameter = metadataTable.getColumnByName(
          parameterName);//ToBeDeletedParameter
      if (tbdParameter != null) {
        metadataTable.removeColumn(tbdParameter);
      }
      updateParametersToTable();
    }
  }

  @FXML
  public void onClickOK(ActionEvent actionEvent) {
    currentStage.close();
  }

  @FXML
  public void onClickHelp(ActionEvent actionEvent) {
    final URL helpPage = this.getClass().getResource("ParametersSetupHelp.html");
    HelpWindow helpWindow = new HelpWindow(helpPage.toString());
    helpWindow.show();
  }
}
