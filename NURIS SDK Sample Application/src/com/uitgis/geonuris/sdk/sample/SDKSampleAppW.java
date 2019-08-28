package com.uitgis.geonuris.sdk.sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.uitgis.geonuris.license.exception.LicenseException;
import com.uitgis.geonuris.sdk.controls.MapControl;
import com.uitgis.geonuris.sdk.controls.TocControl;
import com.uitgis.geonuris.sdk.controls.tools.FXFixedZoomIn;
import com.uitgis.geonuris.sdk.controls.tools.FXFixedZoomOut;
import com.uitgis.geonuris.sdk.controls.tools.FXFullExtent;
import com.uitgis.geonuris.sdk.controls.tools.FXLoadGDX;
import com.uitgis.geonuris.sdk.controls.tools.FXPan;
import com.uitgis.geonuris.sdk.controls.tools.FXZoomIn;
import com.uitgis.geonuris.sdk.controls.tools.FXZoomOut;
import com.uitgis.geonuris.sdk.controls.tools.IFXCommand;
import com.uitgis.geonuris.sdk.controls.tools.IFXTool;

public class SDKSampleAppW extends Application {

	private MapControl mapControl;
	
	private TocControl toc;

	private ToolBar toolbar = new ToolBar();
	
	private ToggleGroup toolToggleGroup;

	
	public void init() throws Exception {
		super.init();

		mapControl = new MapControl();
		
		toolbar = new ToolBar();
		ObservableList<Node> toolbarItems = toolbar.getItems();
		
		toolToggleGroup = new ToggleGroup();
		toolToggleGroup.selectedToggleProperty().addListener((v, o, n) -> {
			if (n == null) {
				toolToggleGroup.selectToggle(o);
			}
		});
		
		// Separator
		toolbarItems.add(new Separator(Orientation.VERTICAL));
		
		ButtonBase button = createCommandButton(new FXLoadGDX());
		button.setGraphic(new ImageView("/open.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		// Separator
		toolbarItems.add(new Separator(Orientation.VERTICAL));
		
		button = createCommandButton(new FXFullExtent());
		button.setGraphic(new ImageView("/fullextent.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createCommandButton(new FXFixedZoomIn());
		button.setGraphic(new ImageView("/fixedzoomin.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createCommandButton(new FXFixedZoomOut());
		button.setGraphic(new ImageView("/fixedzoomout.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createToolButton(new FXPan());
		button.setGraphic(new ImageView("/pan.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createToolButton(new FXZoomIn());
		button.setGraphic(new ImageView("/zoomin.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createToolButton(new FXZoomOut());
		button.setGraphic(new ImageView("/zoomout.png"));
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		// Separator
		toolbarItems.add(new Separator(Orientation.VERTICAL));
		
//		button = createToolButton(new FXCalculateDistance());
//		button.setGraphic(new ImageView("/measurelength.png"));
//		button.getStyleClass().add("toolable");
//		toolbarItems.add(button);
//		
//		button = createToolButton(new FXCalculateArea());
//		button.setGraphic(new ImageView("/measurearea.png"));
//		button.getStyleClass().add("toolable");
//		toolbarItems.add(button);
//		
//		button = createToolButton(new FXSelect());
//		button.setGraphic(new ImageView("/select.png"));
//		button.getStyleClass().add("toolable");
//		toolbarItems.add(button);
		
		button = createToolButton(new DrawLinestringTool());
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createToolButton(new DrawingPolygonTool());
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		button = createCommandButton(new RemoveDrawingTool());
		button.getStyleClass().add("toolable");
		toolbarItems.add(button);
		
		// ÁÂÃø TOC »ý¼º
		toc = new TocControl();
		toc.setMapControl(mapControl);
		
	}
	
	private ToggleButton createToolButton(IFXTool tool) {
		ImageView iv = new ImageView(tool.getImageIcon());
		ToggleButton button = new ToggleButton(null, iv);		
		toolToggleGroup.getToggles().add(button);
		
		Platform.runLater(new Runnable() {
			public void run() {
				button.setTooltip(new Tooltip(tool.getTooltipMessage()));
			}
		});
		button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					mapControl.setTool(tool);
				}
				catch (LicenseException e) {
					System.err.println(e.getMessage());
				}
			}
		});
		
		return button;
	}
	
	private Button createCommandButton(IFXCommand cmd) {
		ImageView iv = new ImageView(cmd.getImageIcon());		
		Button button = new Button(null, iv);
		
		Platform.runLater(new Runnable() {
			public void run() {
				button.setTooltip(new Tooltip(cmd.getTooltipMessage()));
			}
		});
		button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					cmd.execute(mapControl);
				} 
				catch (LicenseException e) {
					System.err.println(e.getMessage());
				}
			}
		});
	
		return button;
	}
	
	public void start(Stage stage) throws Exception {
		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(toc, mapControl);
		splitPane.setDividerPositions(0.3f, 0.7f);
		
		BorderPane borderPane = new BorderPane();
		borderPane.setTop(toolbar);
		borderPane.setCenter(splitPane);
		
		Scene scene = new Scene(borderPane, 800, 600);
		scene.getStylesheets().add("com/uitgis/geonuris/sdk/sample/root.css");
		
		Rectangle2D bounds = Screen.getPrimary().getBounds();
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		
		int x, y;
		x = (int)(width / 2 - scene.getWidth() /  2);
		y = (int)(height / 2 - scene.getHeight() / 2);
		
		stage.setX(x);
		stage.setY(y);

		stage.setTitle("NURIS SDK Map Viewer");
		stage.setScene(scene);
		
		stage.show();
	
	}

	public static void main(String[] args) {
		launch(args);
	}
}
