/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package io.github.mzmine.gui.chartbasics.simplechart.providers.impl.series;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.IMSRawDataFile;
import io.github.mzmine.datamodel.features.ModularFeature;
import io.github.mzmine.datamodel.features.types.numbers.MobilityRangeType;
import io.github.mzmine.datamodel.features.types.numbers.MobilityType;
import io.github.mzmine.gui.chartbasics.simplechart.providers.PlotXYZDataProvider;
import io.github.mzmine.gui.preferences.UnitFormat;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.IonMobilityUtils;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.renderer.PaintScale;

public class FeaturesToMobilityMzHeatmapProvider implements
    PlotXYZDataProvider {

  private final String seriesKey;
  private final NumberFormat rtFormat;
  private final NumberFormat mzFormat;
  private final NumberFormat mobilityFormat;
  private final NumberFormat intensityFormat;
  private final UnitFormat unitFormat;
  private final List<ModularFeature> features;
  private double boxWidth;
  private double boxHeight;

  public FeaturesToMobilityMzHeatmapProvider(@NotNull final List<ModularFeature> f) {
    features = f;
    seriesKey = (f.isEmpty()) ? "No features found" : f.get(0).getFeatureList().getName();

    rtFormat = MZmineCore.getConfiguration().getRTFormat();
    mzFormat = MZmineCore.getConfiguration().getMZFormat();
    mobilityFormat = MZmineCore.getConfiguration().getMobilityFormat();
    intensityFormat = MZmineCore.getConfiguration().getIntensityFormat();
    unitFormat = MZmineCore.getConfiguration().getUnitFormat();
  }

  @NotNull
  @Override
  public Color getAWTColor() {
    return Color.BLACK;
  }

  @NotNull
  @Override
  public javafx.scene.paint.Color getFXColor() {
    return javafx.scene.paint.Color.BLACK;
  }

  @Nullable
  @Override
  public String getLabel(int index) {
    return null;
  }

  @Nullable
  @Override
  public PaintScale getPaintScale() {
    return null;
  }

  @NotNull
  @Override
  public Comparable<?> getSeriesKey() {
    return seriesKey;
  }

  @Nullable
  @Override
  public String getToolTipText(int itemIndex) {
    ModularFeature f = features.get(itemIndex);

    StringBuilder sb = new StringBuilder();
    sb.append("m/z:");
    sb.append(mzFormat.format(f.getRawDataPointsMZRange().lowerEndpoint()));
    sb.append(" - ");
    sb.append(mzFormat.format(f.getRawDataPointsMZRange().upperEndpoint()));
    sb.append("\n");
    sb.append(unitFormat.format("Retention time", "min"));
    sb.append(": ");
    sb.append(rtFormat.format(f.getRawDataPointsRTRange().lowerEndpoint()));
    sb.append(" - ");
    sb.append(rtFormat.format(f.getRawDataPointsRTRange().upperEndpoint()));
    sb.append("\n");
    if (f.getRawDataFile() instanceof IMSRawDataFile) {
      sb.append(((IMSRawDataFile) f.getRawDataFile()).getMobilityType().getAxisLabel());
      sb.append(": ");
      Range<Float> mobrange = f.get(MobilityRangeType.class);
      sb.append(mobilityFormat.format(mobrange.lowerEndpoint()));
      sb.append(" - ");
      sb.append(mobilityFormat.format(mobrange.upperEndpoint()));
      sb.append("\n");
    }
    sb.append("Height: ");
    sb.append(intensityFormat.format(f.getHeight()));
    sb.append("MS data file: ");
    sb.append(f.getRawDataFile().getName());
    return sb.toString();
  }

  @Override
  public void computeValues(SimpleObjectProperty<TaskStatus> status) {
    int numSamples = Math.min(features.size(), 100);
    double width = 0d;
    for (int i = 0; i < features.size(); i += (features.size() / numSamples)) {
      Range<Double> mzRange = features.get(i).getRawDataPointsMZRange();
      width += mzRange.upperEndpoint() - mzRange.lowerEndpoint();

    }
    width /= numSamples;
    boxWidth = width;

    if (!features.isEmpty()) {
      boxHeight = IonMobilityUtils.getSmallestMobilityDelta(
          ((IMSRawDataFile) features.get(0).getRawDataFile()).getFrame(0)) * 3;
    }

  }

  @Override
  public double getDomainValue(int index) {
    return features.get(index).getMZ();
  }

  @Override
  public double getRangeValue(int index) {
    return Objects.requireNonNullElse(features.get(index).get(MobilityType.class), 0f);
  }

  @Override
  public int getValueCount() {
    return features.size();
  }

  @Override
  public double getComputationFinishedPercentage() {
    return 1d;
  }

  @Override
  public double getZValue(int index) {
    return features.get(index).getHeight();
  }

  @Nullable
  @Override
  public Double getBoxHeight() {
    return boxHeight;
  }

  @Nullable
  @Override
  public Double getBoxWidth() {
    return boxWidth;
  }

  @Nullable
  public List<ModularFeature> getSourceFeatures() {
    return features;
  }
}
