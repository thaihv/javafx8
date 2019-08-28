package com.uitgis.plugin.tilegenerator.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.uitgis.maple.application.Main;
import com.uitgis.maple.common.ui.MapleDialogSkin;
import com.uitgis.maple.common.ui.UIUtil;
import com.uitgis.plugin.tilegenerator.TileGenTask;
import com.uitgis.plugin.tilegenerator.model.WizardData;

import framework.FrameworkManager;
import framework.dialog.DialogManager;
import framework.dialog.ProgressDialog;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class WizardController {

	private final int INDICATOR_RADIUS = 8;

	private final String CONTROLLER_KEY = "controller";

	@FXML
	VBox contentPanel;

	@FXML
	HBox hboxIndicators;

	@FXML
	Button btnNext, btnBack, btnFinish, btnReset;

	@Inject
	Injector injector;

	@Inject
	WizardData model;

	private final List<Parent> steps = new ArrayList<>();

	private final IntegerProperty currentStep = new SimpleIntegerProperty(-1);

	@FXML
	public void initialize() throws Exception {

		buildSteps();

		initButtons();

		buildIndicatorCircles();

		setInitialContent();
	}

	private void initButtons() {
		btnBack.disableProperty().bind(currentStep.lessThanOrEqualTo(0));
		btnNext.disableProperty().bind(currentStep.greaterThanOrEqualTo(steps.size() - 1));
		btnFinish.disableProperty().bind(currentStep.lessThan(steps.size() - 1));
		btnReset.disableProperty().bind(currentStep.lessThanOrEqualTo(0));
	}

	private void setInitialContent() {
		currentStep.set(0); // first element
		contentPanel.getChildren().add(steps.get(currentStep.get()));
	}

	private void buildIndicatorCircles() {
		for (int i = 0; i < steps.size(); i++) {
			hboxIndicators.getChildren().add(createIndicatorCircle(i));
		}
	}

	private void buildSteps() throws java.io.IOException {

		final JavaFXBuilderFactory bf = new JavaFXBuilderFactory();

		final Callback<Class<?>, Object> cb = (clazz) -> injector.getInstance(clazz);

		ResourceBundle bundle = ResourceBundle.getBundle("tilegenerator");

		FXMLLoader fxmlLoaderStep1 = new FXMLLoader(WizardController.class.getResource("/fxml/InputConfiguration.fxml"),
				bundle, bf, cb);
		Parent step1 = fxmlLoaderStep1.load();
		step1.getProperties().put(CONTROLLER_KEY, fxmlLoaderStep1.getController());

		FXMLLoader fxmlLoaderStep2 = new FXMLLoader(
				WizardController.class.getResource("/fxml/OutputConfiguration.fxml"), bundle, bf, cb);
		Parent step2 = fxmlLoaderStep2.load();
		step2.getProperties().put(CONTROLLER_KEY, fxmlLoaderStep2.getController());

		FXMLLoader fxmlLoaderStep3 = new FXMLLoader(
				WizardController.class.getResource("/fxml/DrawingConfiguration.fxml"), bundle, bf, cb);
		Parent step3 = fxmlLoaderStep3.load();
		step3.getProperties().put(CONTROLLER_KEY, fxmlLoaderStep3.getController());

		FXMLLoader fxmlLoaderStep4 = new FXMLLoader(WizardController.class.getResource("/fxml/TileConfiguration.fxml"),
				bundle, bf, cb);
		Parent step4 = fxmlLoaderStep4.load();
		step4.getProperties().put(CONTROLLER_KEY, fxmlLoaderStep4.getController());

		FXMLLoader fxmlLoaderStep5 = new FXMLLoader(
				WizardController.class.getResource("/fxml/PyramidConfiguration.fxml"), bundle, bf, cb);
		Parent step5 = fxmlLoaderStep5.load();
		step5.getProperties().put(CONTROLLER_KEY, fxmlLoaderStep5.getController());

		FXMLLoader fxmlLoaderCompleted = new FXMLLoader(WizardController.class.getResource("/fxml/Completed.fxml"),
				bundle, bf, cb);
		Parent completed = fxmlLoaderCompleted.load();
		completed.getProperties().put(CONTROLLER_KEY, fxmlLoaderCompleted.getController());

		steps.addAll(Arrays.asList(step1, step2, step3, step4, step5, completed));
	}

	private Circle createIndicatorCircle(int i) {

		Circle circle = new Circle(INDICATOR_RADIUS, Color.WHITE);
		circle.setStroke(Color.BLACK);

		circle.fillProperty()
				.bind(new When(currentStep.greaterThanOrEqualTo(i)).then(Color.web("#51c1c6")).otherwise(Color.WHITE));

		return circle;
	}

	@FXML
	public void next() {

		Parent p = steps.get(currentStep.get());
		Object controller = p.getProperties().get(CONTROLLER_KEY);

		// validate
		Method v = getMethod(Validate.class, controller);
		if (v != null) {
			try {
				Object retval = v.invoke(controller);
				if (retval != null && ((Boolean) retval) == false) {
					return;
				}

			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		// submit
		Method sub = getMethod(Submit.class, controller);
		if (sub != null) {
			try {
				sub.invoke(controller);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		if (currentStep.get() < (steps.size() - 1)) {
			contentPanel.getChildren().remove(steps.get(currentStep.get()));
			currentStep.set(currentStep.get() + 1);
			contentPanel.getChildren().add(steps.get(currentStep.get()));
		}
	}

	@FXML
	public void back() {

		if (currentStep.get() > 0) {
			contentPanel.getChildren().remove(steps.get(currentStep.get()));
			currentStep.set(currentStep.get() - 1);
			contentPanel.getChildren().add(steps.get(currentStep.get()));
		}
	}

	@FXML
	public void finish() {

		// Validation for last step
		Parent p = steps.get(currentStep.get());
		Object controller = p.getProperties().get(CONTROLLER_KEY);
		Method v = getMethod(Validate.class, controller);
		if (v != null) {
			try {
				Object retval = v.invoke(controller);
				if (retval != null && ((Boolean) retval) == false) {
					return;
				}

			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		// Going to execute
		Stage stage = (Stage) btnFinish.getScene().getWindow();
		stage.close();

		Task<Void> tileGenTask = new TileGenTask(model.getThreadNum());
		injector.injectMembers(tileGenTask);

		try {
			ProgressDialog dlg = DialogManager.getProgressDialog(Main.getPrimaryStage(), Modality.NONE,
					StageStyle.DECORATED, "Tile Generator", null, tileGenTask);
			MapleDialogSkin skin = UIUtil.applyMapleDialogSkin(dlg, false);
			skin.show();
		} catch (Exception e) {
			FrameworkManager.log(tileGenTask.getClass(), e.getMessage(), e.getCause(), true);
		}

	}

	@FXML
	public void reset() {

		// Remove current step and display first step
		contentPanel.getChildren().remove(steps.get(currentStep.get()));
		currentStep.set(0);
		contentPanel.getChildren().add(steps.get(currentStep.get()));
		// Set initialized for data model.
		model.reset();

		// Setup options for displaying of first step by calling to Reset method from
		// step controller
		Parent p = steps.get(currentStep.get());
		Object controller = p.getProperties().get(CONTROLLER_KEY);
		Method v = getMethod(Reset.class, controller);
		if (v != null) {
			try {
				v.invoke(controller);

			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

	private Method getMethod(Class<? extends Annotation> an, Object obj) {

		if (an == null) {
			return null;
		}

		if (obj == null) {
			return null;
		}

		Method[] methods = obj.getClass().getMethods();
		if (methods != null && methods.length > 0) {
			for (Method m : methods) {
				if (m.isAnnotationPresent(an)) {
					return m;
				}
			}
		}
		return null;
	}
}
