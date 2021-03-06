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

package io.github.mzmine.modules.batchmode;

import io.github.mzmine.modules.MZmineModule;
import java.util.Objects;

/**
 * A simple wrapper providing the toString() method for adding modules to combo boxes in batch mode.
 */
public class BatchModuleWrapper {

  private MZmineModule module;

  public BatchModuleWrapper(MZmineModule module) {
    this.module = module;
  }

  public MZmineModule getModule() {
    return module;
  }

  @Override
  public String toString() {
    return module.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BatchModuleWrapper)) {
      return false;
    }
    BatchModuleWrapper that = (BatchModuleWrapper) o;
    return getModule().equals(that.getModule());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getModule());
  }
}
