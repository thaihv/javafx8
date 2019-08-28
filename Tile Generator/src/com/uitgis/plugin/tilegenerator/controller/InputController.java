package com.uitgis.plugin.tilegenerator.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.uitgis.maple.application.ContentID;
import com.uitgis.maple.common.util.Noti;
import com.uitgis.maple.contents.map.ui.MapTabPane;
import com.uitgis.maple.contents.toolbox.ui.ToolboxHelper;
import com.uitgis.plugin.tilegenerator.LabelEngine;
import com.uitgis.plugin.tilegenerator.model.WizardData;
import com.uitgis.sdk.controls.MapControl;
import com.uitgis.sdk.gdx.GDX;
import com.uitgis.sdk.gdx.GDXHelper;
import com.uitgis.sdk.layer.ILayer;
import com.vividsolutions.jts.geom.Envelope;

import framework.FrameworkManager;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class InputController {

	@FXML
	TextField tfGdxFile, tfLeft, tfTop, tfBottom, tfRight;

	@FXML
	Label lblInputTitle;

	@FXML
	Button btnGdxBrowser;

	@FXML
	RadioButton rbSelectMap, rbSelectGDX, rbFullExtent, rbCurrExtent, rbUsrDefineExtent;

	@FXML
	ToggleGroup tglGroupExtent, tglGroupMapControl;

	@FXML
	ComboBox<String> cmbMapControls;

	@Inject
	WizardData model;

	private Logger log = LoggerFactory.getLogger(InputController.class);
	public ObservableList<String> mapControlList = FXCollections.observableArrayList();
	private MapTabPane mapTab = (MapTabPane) FrameworkManager.getUserContent(ContentID.MAP_TAB_KEY);
	private ArrayList<MapControl> maps = mapTab.getAllMapControls();
	private MapControl mc = ToolboxHelper.getCurrentMapControl();

	@FXML
	public void initialize() {

		lblInputTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

		mapControlList.addAll(maps.stream().map(c -> c.getMapTitle()).collect(Collectors.toList()));
		tfLeft.textProperty().bindBidirectional(model.leftExtentProperty());
		tfTop.textProperty().bindBidirectional(model.topExtentProperty());
		tfBottom.textProperty().bindBidirectional(model.bottomExtentProperty());
		tfRight.textProperty().bindBidirectional(model.rightExtentProperty());

		tfLeft.disableProperty().bind(Bindings.or(rbFullExtent.selectedProperty(), rbCurrExtent.selectedProperty()));
		tfTop.disableProperty().bind(Bindings.or(rbFullExtent.selectedProperty(), rbCurrExtent.selectedProperty()));
		tfBottom.disableProperty().bind(Bindings.or(rbFullExtent.selectedProperty(), rbCurrExtent.selectedProperty()));
		tfRight.disableProperty().bind(Bindings.or(rbFullExtent.selectedProperty(), rbCurrExtent.selectedProperty()));

		tfGdxFile.disableProperty().bind(rbSelectMap.selectedProperty());
		btnGdxBrowser.disableProperty().bind(rbSelectMap.selectedProperty());
		cmbMapControls.disableProperty().bind(rbSelectGDX.selectedProperty());

		cmbMapControls.getItems().addAll(mapControlList);
		cmbMapControls.getSelectionModel().select(mc.getMapTitle());

		// For the first initiation of Display using a GDX from MapControl
		populateModelWithMapControlGDX(mc);
		rbSelectMap.setOnAction(event -> {
			if (!tfGdxFile.getText().isEmpty())
				tfGdxFile.clear();
			Optional<MapControl> mc = maps.stream().filter(p -> p.getMapTitle().equals(cmbMapControls.getValue()))
					.findFirst();
			populateModelWithMapControlGDX(mc.get());
		});
		// If using GDX file instead of MapControl by select
		btnGdxBrowser.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open GDX file");
			fileChooser.getExtensionFilters()
					.add(new FileChooser.ExtensionFilter("Geography Markup Language", "*.xml"));
			File file = fileChooser.showOpenDialog(btnGdxBrowser.getScene().getWindow());
			if (file != null) {
				tfGdxFile.setText(file.getPath());
				try {
					model.setGDX(GDXHelper.loadGDX(file));
					if (!model.getGDX().isEmpty()) {
						// Get out properties from header file
						Properties properties = model.getGDX().getGDXHeader().getProperties();
						// Hide label overlapped
						String value = (String) properties.get("LABEL_ENGINE");
						model.setEliminateLabelQuality(!LabelEngine.DISPLAY_ALL.name().equals(value));
						// Anti aliasing for text
						value = (String) properties.get("TEXT_ANTIALIASING");
						model.setImproveLabelQuality(value == null || !value.equalsIgnoreCase("FALSE"));
						// Background Color
						value = (String) properties.get("MAP_BACKGROUND");
						int[] background = new int[3];
						try {
							boolean hasAlpha = value.length() >= 8;
							if (hasAlpha) {
								background[0] = Integer.parseInt(value.substring(2, 4), 16);
								background[1] = Integer.parseInt(value.substring(4, 6), 16);
								background[2] = Integer.parseInt(value.substring(6, 8), 16);
							} else {
								background[0] = Integer.parseInt(value.substring(0, 2), 16);
								background[1] = Integer.parseInt(value.substring(2, 4), 16);
								background[2] = Integer.parseInt(value.substring(4, 6), 16);
							}
						} catch (Throwable t) {
							background[0] = background[1] = background[2] = 255;
						}
						model.setColorBackground(Color.rgb(background[0], background[1], background[2]));
						// Anti aliasing for shape
						value = (String) properties.get("ANTIALIASING");
						model.setAntialiasing(value != null && value.equalsIgnoreCase("TRUE"));
						if (rbFullExtent.isSelected()) {
							setTileEnvelope(calcfullExtentFromGDX(model.getGDX()));
						} else {
							if (rbCurrExtent.isSelected())
								setTileEnvelope(model.getGDX().getEnvelope());
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		tglGroupExtent.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldVal, Toggle newVal) {

				int selected = tglGroupExtent.getToggles().indexOf(tglGroupExtent.getSelectedToggle());

				if (selected == 0) { // Full extent
					if (model.getGDX() != null) {
						setTileEnvelope(calcfullExtentFromGDX(model.getGDX()));
					}

				} else {
					if (selected == 1) { // Current Extent
						if (model.getGDX() != null) {
							setTileEnvelope(model.getGDX().getEnvelope());
						}
					}

				}

			}
		});

	}

	@Reset
	public void reset() {

		// Select GDX from Map Control
		rbSelectMap.setSelected(true);
		// Set current map for combo box
		cmbMapControls.getSelectionModel().select(mc.getMapTitle());
		tfGdxFile.clear();
		// Set extent in full mode
		rbFullExtent.setSelected(true);
		// Set data model for full extent from Map Control
		populateModelWithMapControlGDX(mc);

	}

	@Validate
	public boolean validate() throws Exception {

		String numbericPattern = "-?\\d+(\\.\\d+E?\\d*)?"; // -? as no or one - ; \d+ as one or many number; E is for
															// exponent present, E9 = 10^9?

		if (tfLeft.getText() == null || tfLeft.getText().isEmpty() || !tfLeft.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "Left Extent field is required.");
			return false;
		}

		if (tfTop.getText() == null || tfTop.getText().isEmpty() || !tfTop.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "Top Extent field is required.");
			return false;
		}

		if (tfBottom.getText() == null || tfBottom.getText().isEmpty()
				|| !tfBottom.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "Bottom Extent field is required.");
			return false;
		}
		if (tfRight.getText() == null || tfRight.getText().isEmpty() || !tfRight.getText().matches(numbericPattern)) {
			Noti.showAlert("Missing Field", "Right Extent field is required.");
			return false;
		}
		return true;
	}

	@Submit
	public void submit() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("[SUBMIT] the user has completed step Input Configuration");
		}
	}

	@FXML
	public void selectMapAction(ActionEvent event) {

		Optional<MapControl> mc = maps.stream().filter(p -> p.getMapTitle().equals(cmbMapControls.getValue()))
				.findFirst();

		if (mc.isPresent()) {
			MapControl mapCtl = mc.get();
			populateModelWithMapControlGDX(mapCtl);
		}

	}

	private void populateModelWithMapControlGDX(MapControl mc) {

		if (!mc.gdxEmpty()) {
			model.setGDX(mc.getGDX());
			model.setEliminateLabelQuality(model.getGDX().isHideLabelOverlaps());
			model.setImproveLabelQuality(model.getGDX().isLabelAntiAlasing());
			model.setAntialiasing(model.getGDX().isAntiAliasing());
			int r = model.getGDX().getBackgroundColor().getRed();
			int g = model.getGDX().getBackgroundColor().getGreen();
			int b = model.getGDX().getBackgroundColor().getBlue();
			int a = model.getGDX().getBackgroundColor().getAlpha();
			double opacity = a / 255.0;
			Color colorbackground = Color.rgb(r, g, b, opacity);
			model.setColorBackground(colorbackground);
			if (rbFullExtent.isSelected()) {
				setTileEnvelope(calcfullExtentFromGDX(model.getGDX()));
			} else {
				if (rbCurrExtent.isSelected())
					setTileEnvelope(model.getGDX().getEnvelope());
			}
		} else
			model.reset();

	}

	private void setTileEnvelope(Envelope ev) {

		if (ev != null) {
			this.model.leftExtentProperty().set(Double.toString(ev.getMinX()));
			this.model.rightExtentProperty().set(Double.toString(ev.getMaxX()));
			this.model.topExtentProperty().set(Double.toString(ev.getMaxY()));
			this.model.bottomExtentProperty().set(Double.toString(ev.getMinY()));
			this.model.setTargetEnvelope(ev);
		}
	}

	public Envelope calcfullExtentFromGDX(GDX gDX) {

		Envelope envelope = new Envelope();
		for (int i = 0; i < gDX.getLayerCount(); i++) {
			ILayer layer = gDX.getLayer(i);
			if (layer.isVisible() && layer.isDataUsable())
				if (!layer.getDataEnvelope().isEmpty())
					envelope.expandToInclude(layer.getDataEnvelope());
		}
		envelope.setCoordinateReferenceSystem(gDX.getMapCRS());
		return envelope;

	}
}
