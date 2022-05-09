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
package io.github.mzmine.modules.dataprocessing.id_formulaprediction;

import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.taskcontrol.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class ResultWindowFX extends Stage {

    private ResultWindowController controller;

    public ResultWindowFX(String title, FeatureListRow peakListRow, double searchedMass, int charge,
                          Task searchTask){

        try{

            FXMLLoader root = new FXMLLoader(getClass().getResource("ResultWindowFX.fxml"));
            Parent rootPane = root.load();
            Scene scene = new Scene(rootPane);
            setScene(scene);
            controller = root.getController();
            controller.initValues(title, peakListRow, searchedMass, charge, searchTask);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    public void addNewListItem(final ResultFormula formula) {
        controller.addNewListItem(formula);
    }

}
