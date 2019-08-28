package com.uitgis.plugin.tilegenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.uitgis.plugin.tilegenerator.model.WizardData;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DrawingController {

	private Logger log = LoggerFactory.getLogger(DrawingController.class);

	@FXML
	CheckBox ckbTransparent, ckbImprvLblQuality, ckbElimiateLblOverlaps;

	@FXML
	ColorPicker colorpkBackground;

	@FXML
	Label lblGraphicTitle;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {

		lblGraphicTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

		colorpkBackground.valueProperty().bindBidirectional(model.colorBackgroundProperty());
		ckbTransparent.selectedProperty().bindBidirectional(model.transparentBackgroundProperty());
		ckbImprvLblQuality.selectedProperty().bindBidirectional(model.improveLabelQualityProperty());
		ckbElimiateLblOverlaps.selectedProperty().bindBidirectional(model.eliminateLabelQualityProperty());

	}

	@Validate
	public boolean validate() throws Exception {
		return true;
	}

	@Submit
	public void submit() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("[SUBMIT] the user has completed step Drawing Configuration");
		}
	}
}
