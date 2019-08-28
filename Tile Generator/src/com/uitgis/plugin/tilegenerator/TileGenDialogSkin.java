package com.uitgis.plugin.tilegenerator;

import java.util.ArrayList;

import com.uitgis.maple.application.Main;

import framework.i18n.I18N;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TileGenDialogSkin extends Stage {

	private double xOffset = 0;
	private double yOffset = 0;

	private Stage d;

	private Button closeBtn = null;

	// Resize a UNDECORATED stage is under construction
	public TileGenDialogSkin(Pane tileGenPane, boolean resize, Image titleImage) {

		d = new Stage();

		HBox titleBar = new HBox();
		ImageView image = new ImageView();
		Label label = new Label(I18N.getText("window.title"));
		label.setTextFill(Color.WHITE);

		HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		closeBtn = new Button();
		closeBtn.setOnAction(e -> {
			d.close();
		});

		titleBar.getChildren().addAll(image, label, spacer, closeBtn);

		Separator sep = new Separator(Orientation.HORIZONTAL);
		sep.setMinHeight(1);
		sep.setMaxHeight(1);

		HBox sepCon = new HBox(sep);
		HBox.setHgrow(sep, Priority.ALWAYS);

		VBox box = new VBox();

		HBox contents = new HBox(tileGenPane);

		box.getChildren().addAll(titleBar, sepCon, contents);
		VBox.setVgrow(contents, Priority.ALWAYS);

		titleBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		titleBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				d.setX(event.getScreenX() - xOffset);
				d.setY(event.getScreenY() - yOffset);
			}
		});

		box.setStyle("-fx-border-color:#CCCCCC;");
		sepCon.setPadding(new Insets(0, 5, 0, 5));
		contents.setPadding(new Insets(5));
		box.setPadding(new Insets(0));
		titleBar.setPadding(new Insets(5));
		titleBar.setAlignment(Pos.CENTER);
		titleBar.setSpacing(5);

		if (titleImage == null) {
			image.setId("mapleskin-titleImage");
		} else {
			image.setImage(titleImage);
		}
		
		titleBar.setId("mapleskin-titlebar");
		closeBtn.setId("mapleskin-closebutton");
		contents.setId("mapleskin-contents");
		sep.setId("mapleskin-underline");

		Scene scene = new Scene(box);
		
		ArrayList<String> arrSheets = Main.getStyleSheets();
		if(arrSheets != null) {
			scene.getStylesheets().addAll(arrSheets);
		}
		d.setScene(scene);
		d.initStyle(StageStyle.UNDECORATED);
		

	}

	public double getxOffset() {
		return xOffset;
	}

	public void setxOffset(double xOffset) {
		this.xOffset = xOffset;
	}

	public double getyOffset() {
		return yOffset;
	}

	public void setyOffset(double yOffset) {
		this.yOffset = yOffset;
	}

	public Stage getDialog() {
		return d;
	}

}