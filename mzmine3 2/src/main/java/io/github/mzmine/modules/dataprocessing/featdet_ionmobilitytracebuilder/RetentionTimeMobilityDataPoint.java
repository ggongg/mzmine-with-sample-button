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

package io.github.mzmine.modules.dataprocessing.featdet_ionmobilitytracebuilder;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Frame;
import io.github.mzmine.datamodel.MobilityScan;
import org.jetbrains.annotations.NotNull;

public class RetentionTimeMobilityDataPoint implements DataPoint, Comparable {

  private final double mz;
  private final double intensity;
  private final MobilityScan mobilityScan;

  public RetentionTimeMobilityDataPoint(MobilityScan mobilityScan, double mz, double intensity) {
    this.mz = mz;
    this.intensity = intensity;
    this.mobilityScan = mobilityScan;
  }

  public double getMobility() {
    return mobilityScan.getMobility();
  }

  public double getMZ() {
    return mz;
  }

  public float getRetentionTime() {
    return mobilityScan.getRetentionTime();
  }

  public double getIntensity() {
    return intensity;
  }

  public Frame getFrame() {
    return mobilityScan.getFrame();
  }

  public MobilityScan getMobilityScan() {
    return mobilityScan;
  }

  @Override
  public int compareTo(@NotNull Object o) {
    if (o instanceof RetentionTimeMobilityDataPoint) {
      int i = Double.compare(getIntensity(), ((RetentionTimeMobilityDataPoint) o).getIntensity());
      if (i != 0) {
        return i * -1; // descending, most intense first
      }
      int f = Integer.compare(getFrame().getFrameId(),
          ((RetentionTimeMobilityDataPoint) o).getFrame().getFrameId());
      if (f != 0) {
        return f;
      }
      int m = Integer.compare(getMobilityScan().getMobilityScanNumber(),
          ((RetentionTimeMobilityDataPoint) o).getMobilityScan().getMobilityScanNumber());
      if(m != 0) {
        return m;
      }
      return Double.compare(getMZ(), ((RetentionTimeMobilityDataPoint) o).getMZ());
    }
    return -1;
  }

  /*@Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(intensity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(mobility);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(mz);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((retentionTime == null) ? 0 : retentionTime.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RetentionTimeMobilityDataPoint other = (RetentionTimeMobilityDataPoint) obj;
    if (Double.doubleToLongBits(intensity) != Double.doubleToLongBits(other.intensity)) {
      return false;
    }
    if (Double.doubleToLongBits(mobility) != Double.doubleToLongBits(other.mobility)) {
      return false;
    }
    if (Double.doubleToLongBits(mz) != Double.doubleToLongBits(other.mz)) {
      return false;
    }
    if (retentionTime == null) {
      if (other.retentionTime != null) {
        return false;
      }
    } else if (!retentionTime.equals(other.retentionTime)) {
      return false;
    }
    return true;
  }*/

}
