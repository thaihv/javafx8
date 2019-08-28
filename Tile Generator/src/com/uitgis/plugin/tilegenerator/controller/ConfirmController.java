package com.uitgis.plugin.tilegenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.uitgis.maple.common.util.Noti;
import com.uitgis.plugin.tilegenerator.model.WizardData;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.NumberStringConverter;

public class ConfirmController {

	private Logger log = LoggerFactory.getLogger(ConfirmController.class);

	@FXML
	TextField tfThreadNum;

	@FXML
	Label lblRunnerTitle;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {
		lblRunnerTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
		tfThreadNum.textProperty().bindBidirectional(model.threadNumProperty(), new NumberStringConverter());
	}

	@Validate
	public boolean validate() throws Exception {

		String numbericPattern = "^[1-9]\\d*$";

		if (tfThreadNum.getText() == null || tfThreadNum.getText().isEmpty()
				|| !tfThreadNum.getText().matches(numbericPattern)) {
			Noti.showAlert("Wrong value input", "The number of threads must is a positive interger.");
			return false;
		}

		return true;
	}

	@Submit
	public void submit() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("[SUBMIT] the user has completed step Thread Setting ");
		}
	}

}
