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

package io.github.mzmine.gui.chartbasics.gui.swing.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import io.github.mzmine.gui.chartbasics.gui.swing.EChartPanel;
import io.github.mzmine.util.io.ClipboardWriter;

public class JMenuExportToClipboard extends JMenuItem implements MenuExport {
  private static final long serialVersionUID = 1L;

  private EChartPanel chart;

  public JMenuExportToClipboard(String menuTitle, EChartPanel chart) {
    super(menuTitle);
    this.chart = chart;
    this.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        exportDataToClipboard();
      }
    });
  }

  public void exportDataToClipboard() {
    Object[][] model = chart.getDataArrayForExport();
    if (model != null)
      ClipboardWriter.writeToClipBoard(model, false);
  }
}
