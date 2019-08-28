package com.uitgis.plugin.tilegenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.uitgis.maple.common.util.Noti;
import com.uitgis.plugin.tilegenerator.model.WizardData;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.NumberStringConverter;

public class TileController {

	private Logger log = LoggerFactory.getLogger(TileController.class);

	@FXML
	TextField tfTileWidth, tfTileHeight, tfOriginX, tfOriginY;

	@FXML
	CheckBox ckbOverwrite, ckbCreateEmptyTile;

	@FXML
	ToggleGroup tglOriginPoint;

	@FXML
	Label lblTileTitle;

	@FXML
	RadioButton rbExtentPoint, rbDefinedPoint;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {

		lblTileTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

		model.originXProperty().bindBidirectional(model.leftExtentProperty());
		model.originYProperty().bindBidirectional(model.bottomExtentProperty());

		tfTileWidth.textProperty().bindBidirectional(model.tileWidthProperty(), new NumberStringConverter());
		tfTileHeight.textProperty().bindBidirectional(model.tileHeightProperty(), new NumberStringConverter());

		tfOriginX.textProperty().bindBidirectional(model.originXProperty());
		tfOriginY.textProperty().bindBidirectional(model.originYProperty());
		ckbOverwrite.selectedProperty().bindBidirectional(model.overWriteAllowedProperty());
		ckbCreateEmptyTile.selectedProperty().bindBidirectional(model.generateEmptyTileProperty());

		tfOriginX.disableProperty().bind(rbExtentPoint.selectedProperty());
		tfOriginY.disableProperty().bind(rbExtentPoint.selectedProperty());

		rbExtentPoint.selectedProperty().bindBidirectional(model.pointOfExtentIsUsedProperty());

	}

	@Validate
	public boolean validate() throws Exception {

		String numbericPattern = "-?\\d+(\\.\\d+E?\\d*)?"; // -? as no or one - ; \d+ as one or many number; E is for
															// exponent present, E9 = 10^9?

		if (tfOriginX.getText() == null || tfOriginX.getText().isEmpty()
				|| !tfOriginX.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "X Coordinate Origin field is required.");
			return false;
		}

		if (tfOriginY.getText() == null || tfOriginY.getText().isEmpty()
				|| !tfOriginY.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "Y Coordinate Origin field is required.");
			return false;
		}

		return true;
	}

	@Submit
	public void submit() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("[SUBMIT] the user has completed step Tile Configuration");
		}
	}
}
