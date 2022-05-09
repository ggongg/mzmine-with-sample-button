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
package io.github.mzmine.modules.visualization.spectra.multimsms.pseudospectra;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.features.Feature;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.util.scans.ScanUtils;
import java.awt.Color;
import java.text.NumberFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;

public class PseudoSpectrum {

  public static PseudoSpectrumDataSet createDataSet(FeatureListRow[] group, RawDataFile raw,
      boolean sum) {
    // data
    PseudoSpectrumDataSet series = new PseudoSpectrumDataSet(true, "pseudo");
    // add all isotopes as a second series:
    XYSeries isoSeries = new XYSeries("Isotopes", true);
    // raw isotopes in a different color
    XYSeries rawIsoSeries = new XYSeries("Raw isotope pattern", true);
    // for each row
    for (FeatureListRow row : group) {
      String annotation = null;
      // sum -> heighest peak
      if (sum)
        series.addDP(row.getAverageMZ(), row.getBestFeature().getHeight(), annotation);
      else {
        Feature f = raw == null ? row.getBestFeature() : row.getFeature(raw);
        if (f != null)
          series.addDP(f.getMZ(), f.getHeight(), null);
      }
      // add isotopes
      IsotopePattern pattern = row.getBestIsotopePattern();
      if (pattern != null) {
        for (DataPoint dp : ScanUtils.extractDataPoints(pattern))
          isoSeries.add(dp.getMZ(), dp.getIntensity());
      }
    }
    series.addSeries(isoSeries);
    series.addSeries(rawIsoSeries);
    return series;
  }

  public static EChartViewer createChartViewer(FeatureListRow[] group, RawDataFile raw, boolean sum,
      String title) {
    PseudoSpectrumDataSet data = createDataSet(group, raw, sum);
    if (data == null)
      return null;
    JFreeChart chart = createChart(data, raw, sum, title);
    if (chart != null) {
      EChartViewer pn = new EChartViewer(chart);
      XYItemRenderer renderer = chart.getXYPlot().getRenderer();
      PseudoSpectraItemLabelGenerator labelGenerator = new PseudoSpectraItemLabelGenerator(pn);
      renderer.setDefaultItemLabelsVisible(true);
      renderer.setDefaultItemLabelPaint(Color.BLACK);
      renderer.setSeriesItemLabelGenerator(0, labelGenerator);
      return pn;
    }

    return null;
  }

  public static JFreeChart createChart(PseudoSpectrumDataSet dataset, RawDataFile raw, boolean sum,
      String title) {
    //
    JFreeChart chart = ChartFactory.createXYLineChart(title, // title
        "m/z", // x-axis label
        "Intensity", // y-axis label
        dataset, // data set
        PlotOrientation.VERTICAL, // orientation
        true, // isotopeFlag, // create legend?
        true, // generate tooltips?
        false // generate URLs?
    );
    chart.setBackgroundPaint(Color.white);
    chart.getTitle().setVisible(false);
    // set the plot properties
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setAxisOffset(RectangleInsets.ZERO_INSETS);

    // set rendering order
    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

    // set crosshair (selection) properties
    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairVisible(false);

    NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();
    NumberFormat intensityFormat = MZmineCore.getConfiguration().getIntensityFormat();

    // set the X axis (retention time) properties
    NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
    xAxis.setNumberFormatOverride(mzFormat);
    xAxis.setUpperMargin(0.08);
    xAxis.setLowerMargin(0.00);
    xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 20, 20));
    xAxis.setAutoRangeIncludesZero(true);
    xAxis.setMinorTickCount(5);

    // set the Y axis (intensity) properties
    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
    yAxis.setNumberFormatOverride(intensityFormat);
    yAxis.setUpperMargin(0.20);

    PseudoSpectraRenderer renderer = new PseudoSpectraRenderer(Color.BLACK, false);
    plot.setRenderer(0, renderer);
    plot.setRenderer(1, renderer);
    plot.setRenderer(2, renderer);
    renderer.setSeriesVisibleInLegend(1, false);
    renderer.setSeriesPaint(2, Color.ORANGE);
    //
    return chart;
  }
}
