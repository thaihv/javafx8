package com.uitgis.plugin.tilegenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.uitgis.maple.common.util.Noti;
import com.uitgis.plugin.tilegenerator.model.TileScale;
import com.uitgis.plugin.tilegenerator.model.WizardData;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.NumberStringConverter;

public class PyramidController {

	@FXML
	TextField tfMultipleNum;

	@FXML
	Label lblPyramidTitle;

	@FXML
	Button btnCalcScale;

	@FXML
	ToggleGroup tglLevelOrder;

	@FXML
	Spinner<Integer> spnNumLevels;

	@FXML
	RadioButton rbAsc, rbDesc;

	@FXML
	TableView<TileScale> tblScale;

	@FXML
	TableColumn<TileScale, Boolean> clnActive;

	@FXML
	TableColumn<TileScale, Number> clnLevel;

	@FXML
	TableColumn<TileScale, Double> clnScale;

	@Inject
	WizardData model;

	private Logger log = LoggerFactory.getLogger(PyramidController.class);

	@FXML
	public void initialize() {

		lblPyramidTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

//		btnCalcScale.disableProperty().bind(model.getListTileScale().get(0).scaleProperty().lessThanOrEqualTo(0));
		spnNumLevels.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99));

		spnNumLevels.getValueFactory().valueProperty().bindBidirectional(model.numberOfLevelsProperty().asObject());
		spnNumLevels.valueProperty().addListener((obs, oldValue, newValue) -> {
			btnCalcScale.disableProperty().bind(model.getListTileScale().get(0).scaleProperty().lessThanOrEqualTo(0));
			int numLevels = spnNumLevels.getValue();
			int currentNum = model.getListTileScale().size();
			if (currentNum < numLevels) {
				int gap = numLevels - currentNum;
				for (int i = 0; i < gap; i++) {
					model.getListTileScale().addAll(new TileScale(true, currentNum + i, 0));
				}

				if (rbDesc.isSelected()) {
					for (int i = 0, size = model.getListTileScale().size(); i < size; i++) {
						model.getListTileScale().get(i).setLevel(size - i - 1);
					}
				}
			} else if (currentNum > numLevels) {
				int gap = currentNum - numLevels;
				for (int i = 0; i < gap; i++) {
					model.getListTileScale().remove(--currentNum);
				}
				if (rbDesc.isSelected()) {
					for (int i = 0, size = model.getListTileScale().size(); i < size; i++) {
						model.getListTileScale().get(i).setLevel(size - i - 1);
					}
				}
			}

		});

		tblScale.setItems(model.getListTileScale());
		clnLevel.setCellValueFactory(cd -> cd.getValue().levelProperty());
		clnScale.setCellValueFactory(cd -> cd.getValue().scaleProperty().asObject());
		clnScale.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

		clnActive.setCellValueFactory(cd -> cd.getValue().activeProperty());
		final Callback<TableColumn<TileScale, Boolean>, TableCell<TileScale, Boolean>> cellFactory = CheckBoxTableCell
				.forTableColumn(clnActive);
		clnActive.setCellFactory(new Callback<TableColumn<TileScale, Boolean>, TableCell<TileScale, Boolean>>() {
			@Override
			public TableCell<TileScale, Boolean> call(TableColumn<TileScale, Boolean> column) {
				TableCell<TileScale, Boolean> cell = cellFactory.call(column);
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		rbAsc.selectedProperty().bindBidirectional(model.orderLevelAscIsUsedProperty());
		tglLevelOrder.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldVal, Toggle newVal) {

				int selected = tglLevelOrder.getToggles().indexOf(tglLevelOrder.getSelectedToggle());

				if (selected == 0) {
					model.setOrderLevelAscIsUsed(true);
					for (int i = 0, size = model.getListTileScale().size(); i < size; i++) {

						model.getListTileScale().get(i).setLevel(i);
					}

				} else {
					model.setOrderLevelAscIsUsed(false);
					for (int i = 0, size = model.getListTileScale().size(); i < size; i++) {
						model.getListTileScale().get(i).setLevel(size - i - 1);
					}

				}

			}
		});
		tfMultipleNum.textProperty().bindBidirectional(model.numberOfLevelMultipleProperty(),
				new NumberStringConverter());
	}

	@Validate
	public boolean validate() throws Exception {

		boolean scaleDone = true;
		for (int i = 0, size = model.getListTileScale().size(); i < size; i++) {
			if (model.getListTileScale().get(i).scaleProperty().lessThanOrEqualTo(0).getValue())
				scaleDone = false;
		}
		if (!scaleDone) {
			Noti.showAlert("Missing Field",
					"All values in Scale field are required! Input value for first level and calculate it.");
			return false;
		}
		return true;
	}

	@Submit
	public void submit() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("[SUBMIT] the user has completed step Pyramid Configuration");
		}
	}

	@FXML
	public void calcScales() {

		int multiple = Integer.parseInt(tfMultipleNum.getText().trim());
		if (multiple > 0) {
			double scale = model.getListTileScale().get(0).getScale();
			int level = model.getListTileScale().size();
			for (int i = 1; i < level; i++) {
				scale = scale / multiple;
				model.getListTileScale().get(i).setScale(scale);
			}
		} else {
			Noti.showAlert("Wrong value input", "The multiple number must is a positive interger.");
		}

	}

}
