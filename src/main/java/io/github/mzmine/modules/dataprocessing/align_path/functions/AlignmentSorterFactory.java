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

package io.github.mzmine.modules.dataprocessing.align_path.functions;

import io.github.mzmine.datamodel.features.FeatureListRow;
import java.util.Comparator;

public class AlignmentSorterFactory {

  public static enum SORT_MODE {

    name {

      public String toString() {
        return "name";
      }
    },
    peaks {

      public String toString() {
        return "number of peaks";
      }
    },
    rt {

      public String toString() {
        return "RT";
      }
    },
    none {

      public String toString() {
        return "nothing";
      }
    };

    public abstract String toString();
  }

  public static Comparator<FeatureListRow> getComparator(final SORT_MODE mode) {
    return getComparator(mode, true);
  }

  /**
   * Return a comparator that <b>is</b> inconsistent with equals.
   * 
   * @param mode
   * @param ascending
   * @return
   */
  public static Comparator<FeatureListRow> getComparator(final SORT_MODE mode,
      final boolean ascending) {
    switch (mode) {
      case name:
        return getNameComparator(ascending);
      case peaks:
        return getPeakCountComparator(ascending);
      case rt:
        return getDoubleValComparator(ascending, mode);
      default:
        return nullComparator();
    }
  }

  private static Comparator<FeatureListRow> getNameComparator(final boolean ascending) {
    return new Comparator<FeatureListRow>() {

      public int compare(FeatureListRow o1, FeatureListRow o2) {
        int comparison = 0;
        comparison = o1.getPreferredFeatureIdentity().getName()
            .compareToIgnoreCase(o2.getPreferredFeatureIdentity().getName());

        return ascending ? comparison : -comparison;
      }
    };
  }

  private static Comparator<FeatureListRow> getPeakCountComparator(final boolean ascending) {
    return new Comparator<FeatureListRow>() {

      public int compare(FeatureListRow o1, FeatureListRow o2) {
        int comp = (Integer) o1.getNumberOfFeatures() - (Integer) o2.getNumberOfFeatures();
        return ascending ? comp : -comp;
      }
    };
  }

  private static Comparator<FeatureListRow> getDoubleValComparator(final boolean ascending,
      final SORT_MODE mode) {
    return new Comparator<FeatureListRow>() {

      public int compare(FeatureListRow o1, FeatureListRow o2) {
        int comparison = 0;
        double val1 = 0.0;
        double val2 = 0.0;
        if (mode == SORT_MODE.rt)
          val1 = (double) o1.getAverageRT();
        if (val1 < val2) {
          comparison = -1;
        }
        if (val1 > val2) {
          comparison = 1;
        }
        return ascending ? comparison : -comparison;
      }
    };
  }

  private static Comparator<FeatureListRow> nullComparator() {
    return new Comparator<FeatureListRow>() {

      public int compare(FeatureListRow o1, FeatureListRow o2) {
        return 0;
      }
    };
  }
}
