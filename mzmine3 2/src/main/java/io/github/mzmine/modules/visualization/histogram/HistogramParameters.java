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

package io.github.mzmine.modules.visualization.histogram;

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.IntegerParameter;
import io.github.mzmine.parameters.parametertypes.MultiChoiceParameter;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsParameter;
import io.github.mzmine.util.ExitCode;

public class HistogramParameters extends SimpleParameterSet {

  public static final FeatureListsParameter featureList = new FeatureListsParameter(1, 1);

  public static final MultiChoiceParameter<RawDataFile> dataFiles =
      new MultiChoiceParameter<RawDataFile>("Raw data files", "Column of features to be plotted",
          new RawDataFile[0]);

  public static final HistogramRangeParameter dataRange = new HistogramRangeParameter();

  public static final IntegerParameter numOfBins =
      new IntegerParameter("Number of bins", "The plot is divides into this number of bins", 10);

  /**
   * Windows size and position
   */
  public static final WindowSettingsParameter windowSettings = new WindowSettingsParameter();

  public HistogramParameters() {
    super(new Parameter[] {featureList, dataFiles, dataRange, numOfBins, windowSettings});
  }

  @Override
  public ExitCode showSetupDialog(boolean valueCheckRequired) {
    FeatureList selectedFeatureLists[] =
        getParameter(HistogramParameters.featureList).getValue().getMatchingFeatureLists();
    RawDataFile dataFiles[];
    if ((selectedFeatureLists == null) || (selectedFeatureLists.length != 1)) {
      dataFiles = MZmineCore.getProjectManager().getCurrentProject().getDataFiles();
    } else {
      dataFiles = selectedFeatureLists[0].getRawDataFiles().toArray(RawDataFile[]::new);
    }
    getParameter(HistogramParameters.dataFiles).setChoices(dataFiles);
    return super.showSetupDialog(valueCheckRequired);
  }

}
