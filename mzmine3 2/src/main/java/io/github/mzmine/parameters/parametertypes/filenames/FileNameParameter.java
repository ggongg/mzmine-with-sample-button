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

package io.github.mzmine.parameters.parametertypes.filenames;

import io.github.mzmine.parameters.UserParameter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.stage.FileChooser.ExtensionFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple Parameter implementation
 */
public class FileNameParameter implements UserParameter<File, FileNameComponent> {

  private static final String CURRENT_FILE_ELEMENT = "current_file";
  private static final String LAST_FILE_ELEMENT = "last_file";
  private final String name;
  private final String description;
  private final FileSelectionType type;
  private final List<ExtensionFilter> filters;
  private File value;
  private List<File> lastFiles;
  private int textfield_columns = 15;
  private boolean allowEmptyString = true;

  /**
   * @param name
   * @param description
   * @param type        FileSelectionType.OPEN to open a file, FileSelectionType.SAVE to save to a
   *                    file.
   */
  public FileNameParameter(String name, String description, FileSelectionType type) {
    this(name, description, List.of(), type);
  }

  /**
   * @param name
   * @param description
   * @param type             FileSelectionType.OPEN to open a file, FileSelectionType.SAVE to save
   *                         to a file.
   * @param allowEmptyString
   */
  public FileNameParameter(String name, String description, FileSelectionType type,
      boolean allowEmptyString) {
    this(name, description, List.of(), type, allowEmptyString);
  }

  /**
   * @param name
   * @param description
   * @param filters
   * @param type        FileSelectionType.OPEN to open a file, FileSelectionType.SAVE to save to a
   *                    file.
   */
  public FileNameParameter(String name, String description, List<ExtensionFilter> filters,
      FileSelectionType type) {
    this.name = name;
    this.description = description;
    this.filters = filters;
    lastFiles = new ArrayList<>();
    this.type = type;
  }

  /**
   * @param name
   * @param description
   * @param filters
   * @param type             FileSelectionType.OPEN to open a file, FileSelectionType.SAVE to save
   *                         to a file.
   * @param allowEmptyString
   */
  public FileNameParameter(String name, String description, List<ExtensionFilter> filters,
      FileSelectionType type, boolean allowEmptyString) {
    this.name = name;
    this.description = description;
    this.filters = filters;
    lastFiles = new ArrayList<>();
    this.type = type;
    this.allowEmptyString = allowEmptyString;
  }

  /**
   * @param name
   * @param description
   * @param filters
   * @param textfield_columns
   * @param type              FileSelectionType.OPEN to open a file, FileSelectionType.SAVE to save
   *                          to a file.
   */
  public FileNameParameter(String name, String description, List<ExtensionFilter> filters,
      int textfield_columns, FileSelectionType type) {
    this.name = name;
    this.description = description;
    this.filters = filters;
    this.textfield_columns = textfield_columns;
    lastFiles = new ArrayList<>();
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public FileNameComponent createEditingComponent() {
    return new FileNameComponent(textfield_columns, lastFiles, type, filters);
  }

  @Override
  public File getValue() {
    return value;
  }

  @Override
  public void setValue(File value) {
    this.value = value;
  }

  public List<File> getLastFiles() {
    return lastFiles;
  }

  public void setLastFiles(List<File> lastFiles) {
    this.lastFiles = lastFiles;
  }

  @Override
  public FileNameParameter cloneParameter() {
    FileNameParameter copy = new FileNameParameter(name, description, type, allowEmptyString);
    copy.setValue(this.getValue());
    copy.setLastFiles(new ArrayList<>(lastFiles));
    return copy;
  }

  @Override
  public void setValueFromComponent(FileNameComponent component) {
    File compValue = component.getValue(allowEmptyString);
    if (compValue == null) {
      this.value = compValue;
      return;
    }

    // add to last files if not already inserted
    lastFiles.remove(compValue);
    lastFiles.add(0, compValue);
    setLastFiles(lastFiles);

    this.value = compValue;
  }

  @Override
  public void setValueToComponent(FileNameComponent component, File newValue) {
    component.setValue(newValue);
  }

  @Override
  public void loadValueFromXML(Element xmlElement) {
    NodeList current = xmlElement.getElementsByTagName(CURRENT_FILE_ELEMENT);
    if (current.getLength() == 1) {
      setValue(new File(current.item(0).getTextContent()));
    }
    // add all still existing files
    lastFiles = new ArrayList<>();

    NodeList last = xmlElement.getElementsByTagName(LAST_FILE_ELEMENT);
    for (int i = 0; i < last.getLength(); i++) {
      Node n = last.item(i);
      if (n.getTextContent() != null) {
        File f = new File(n.getTextContent());
        if (f.exists()) {
          lastFiles.add(f);
        }
      }
    }
    setLastFiles(lastFiles);
  }

  @Override
  public void saveValueToXML(Element xmlElement) {
    // add new element for each file
    Document parentDocument = xmlElement.getOwnerDocument();
    if (value != null) {
      Element paramElement = parentDocument.createElement(CURRENT_FILE_ELEMENT);
      paramElement.setTextContent(value.getAbsolutePath());
      xmlElement.appendChild(paramElement);
    }

    if (lastFiles != null) {
      for (File f : lastFiles) {
        Element paramElement = parentDocument.createElement(LAST_FILE_ELEMENT);
        paramElement.setTextContent(f.getAbsolutePath());
        xmlElement.appendChild(paramElement);
      }
    }
  }

  @Override
  public boolean checkValue(Collection<String> errorMessages) {
    if (value == null) {
      errorMessages.add(name + " is not set properly");
      return false;
    }
    return true;
  }

}
