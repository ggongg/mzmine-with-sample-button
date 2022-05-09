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

package io.github.mzmine.modules.dataprocessing.featdet_recursiveimsbuilder;

import static io.github.mzmine.modules.dataprocessing.featdet_mobilogram_summing.MobilogramBinningParameters.DEFAULT_DTIMS_BIN_WIDTH;
import static io.github.mzmine.modules.dataprocessing.featdet_mobilogram_summing.MobilogramBinningParameters.DEFAULT_TIMS_BIN_WIDTH;
import static io.github.mzmine.modules.dataprocessing.featdet_mobilogram_summing.MobilogramBinningParameters.DEFAULT_TWIMS_BIN_WIDTH;

import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.IntegerParameter;
import io.github.mzmine.parameters.parametertypes.OptionalParameter;

public class RecursiveIMSBuilderAdvancedParameters extends SimpleParameterSet {

  public static final OptionalParameter<IntegerParameter> timsBinningWidth = new OptionalParameter<>(
      new IntegerParameter("Override default TIMS binning width (Vs/cm²)",
          "The binning width in mobility units of the selected raw data file.\n"
              + " The default binning width is " + DEFAULT_TIMS_BIN_WIDTH + ".",
          DEFAULT_TIMS_BIN_WIDTH, 1, 1000));

  public static final OptionalParameter<IntegerParameter> twimsBinningWidth = new OptionalParameter(
      new IntegerParameter(
          "Travelling wave binning width (ms)",
          "The binning width in mobility units of the selected raw data file."
              + "The default binning width is " + DEFAULT_TWIMS_BIN_WIDTH + ".",
          DEFAULT_TWIMS_BIN_WIDTH, 1, 1000));

  public static final OptionalParameter<IntegerParameter> dtimsBinningWidth = new OptionalParameter<>(
      new IntegerParameter(
          "Drift tube binning width (ms)",
          "The binning width in mobility units of the selected raw data file.\n"
              + "The default binning width is " + DEFAULT_TIMS_BIN_WIDTH + ".",
          DEFAULT_DTIMS_BIN_WIDTH, 1, 1000));

  public RecursiveIMSBuilderAdvancedParameters() {
    super(new Parameter[]{timsBinningWidth, dtimsBinningWidth, twimsBinningWidth});
  }
}

