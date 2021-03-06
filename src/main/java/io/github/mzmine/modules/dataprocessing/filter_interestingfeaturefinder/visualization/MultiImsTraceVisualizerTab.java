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

package io.github.mzmine.modules.dataprocessing.filter_interestingfeaturefinder.visualization;

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.ModularFeature;
import io.github.mzmine.gui.mainwindow.MZmineTab;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MultiImsTraceVisualizerTab extends MZmineTab {

  private final MultiImsTraceVisualiser visualiser = new MultiImsTraceVisualiser();

  public MultiImsTraceVisualizerTab() {
    super("Multiple IMS Traces");
    setContent(visualiser);
  }

  @Override
  public @NotNull Collection<? extends RawDataFile> getRawDataFiles() {
    return visualiser.getFeatures().stream().map(ModularFeature::getRawDataFile).toList();
  }

  @Override
  public @NotNull Collection<? extends FeatureList> getFeatureLists() {
    return Collections.emptyList();
  }

  @Override
  public @NotNull Collection<? extends FeatureList> getAlignedFeatureLists() {
    return Collections.emptyList();
  }

  @Override
  public void onRawDataFileSelectionChanged(Collection<? extends RawDataFile> rawDataFiles) {
    // do nothing
  }

  @Override
  public void onFeatureListSelectionChanged(Collection<? extends FeatureList> featureLists) {
    // do nothing
  }

  @Override
  public void onAlignedFeatureListSelectionChanged(Collection<? extends FeatureList> featureLists) {
    // do nothing
  }

  public void setFeatures(List<ModularFeature> features) {
    visualiser.setFeatures(features);
  }
}
