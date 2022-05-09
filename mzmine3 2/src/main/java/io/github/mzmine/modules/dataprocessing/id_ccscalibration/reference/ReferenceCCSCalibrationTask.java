package io.github.mzmine.modules.dataprocessing.id_ccscalibration.reference;

import io.github.mzmine.datamodel.IMSRawDataFile;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_ccscalibration.CCSCalculator;
import io.github.mzmine.modules.dataprocessing.id_ccscalibration.CCSCalibration;
import io.github.mzmine.modules.dataprocessing.id_ccscalibration.setcalibration.SetCCSCalibrationModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.MemoryMapStorage;
import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReferenceCCSCalibrationTask extends AbstractTask {

  private static final Logger logger = Logger.getLogger(
      ReferenceCCSCalibrationTask.class.getName());

  private final CCSCalculator ccsCalculator;
  private final ParameterSet ccsCalculatorParameters;
  private final RawDataFile[] files;
  private final ModularFeatureList[] flists;
  private double progress = 0;
  private int processed = 0;

  public ReferenceCCSCalibrationTask(@Nullable MemoryMapStorage storage,
      @NotNull Instant moduleCallDate, ParameterSet ccsCalculatorParameters) {
    super(storage, moduleCallDate);
    this.ccsCalculator = MZmineCore.getModuleInstance(ReferenceCCSCalibrationModule.class);
    this.ccsCalculatorParameters = ccsCalculatorParameters;
    files = ccsCalculatorParameters.getValue(ReferenceCCSCalibrationParameters.files)
        ? ccsCalculatorParameters.getParameter(ReferenceCCSCalibrationParameters.files)
        .getEmbeddedParameter().getValue().getMatchingRawDataFiles() : new RawDataFile[0];
    flists = ccsCalculatorParameters.getValue(ReferenceCCSCalibrationParameters.flists)
        .getMatchingFeatureLists();
  }

  @Override
  public String getTaskDescription() {
    return "CCS calibration task using feature lists " + Arrays.toString(flists);
  }

  @Override
  public double getFinishedPercentage() {
    return progress;
  }

  @Override
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    // either we have no raw data files and feature lists, or we have 1 feature list and multiple raw files.
    if (files.length != 0 && flists.length > 1) {
      setErrorMessage(
          "Invalid parameter selection. Either select one feature list and >= 1 raw data file or no raw data files. (Reference calibration)");
      setStatus(TaskStatus.CANCELED);
      return;
    }

    // set calibration to feature list's raw data files
    for (ModularFeatureList flist : flists) {
      final CCSCalibration calibration = ccsCalculator.getCalibration(flist,
          ccsCalculatorParameters);
      if (calibration == null) {
        logger.info("No calibration found using " + ccsCalculator + " "
            + ccsCalculatorParameters.toString());
        setErrorMessage("No calibration found.");
        setStatus(TaskStatus.CANCELED);
        return;
      }
      logger.info(() -> "Found ccs calibration " + calibration);

      flist.getRawDataFiles().stream().filter(f -> f instanceof IMSRawDataFile)
          .forEach(f -> ((IMSRawDataFile) f).setCCSCalibration(calibration));
      SetCCSCalibrationModule.setCalibrationToFiles(
          flist.getRawDataFiles().toArray(RawDataFile[]::new), calibration);
      processed++;
      progress = processed / (double) flists.length;

      // set calibration to additional raw files
      for (RawDataFile file : files) {
        if (file instanceof IMSRawDataFile imsFile && !flist.hasRawDataFile(file)) {
          imsFile.setCCSCalibration(calibration);
          processed++;
          progress = processed / (double) files.length;
        }
      }
      // for reproducibility, this might take a bit longer.
      SetCCSCalibrationModule.setCalibrationToFiles(files, calibration);
    }
    setStatus(TaskStatus.FINISHED);
  }
}
