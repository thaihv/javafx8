package com.uitgis.geonuris.sdk.sample;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.uitgis.geonuris.sdk.controls.MapControl;
import com.uitgis.geonuris.sdk.controls.TocControl;
import com.uitgis.geonuris.sdk.controls.ToolbarControl;
import com.uitgis.geonuris.sdk.controls.tools.FXCalculateArea;
import com.uitgis.geonuris.sdk.controls.tools.FXCalculateDistance;
import com.uitgis.geonuris.sdk.controls.tools.FXFixedZoomIn;
import com.uitgis.geonuris.sdk.controls.tools.FXFixedZoomOut;
import com.uitgis.geonuris.sdk.controls.tools.FXFullExtent;
import com.uitgis.geonuris.sdk.controls.tools.FXLoadGDX;
import com.uitgis.geonuris.sdk.controls.tools.FXPan;
import com.uitgis.geonuris.sdk.controls.tools.FXSelect;
import com.uitgis.geonuris.sdk.controls.tools.FXZoomIn;
import com.uitgis.geonuris.sdk.controls.tools.FXZoomOut;

public class SDKSampleApp extends Application {

	private MapControl mMapControl;
	private TocControl toc;
	private ToolbarControl toolbar;

	@Override
	public void init() throws Exception {
		super.init();

		// 지도 화면 생성
		mMapControl = new MapControl();
		
		// 상단 툴바 생성
		toolbar = new ToolbarControl(mMapControl, false);
		toolbar.setSpacing(0);
		
		// Separator 추가
		toolbar.addCommand(null);
		
		ButtonBase button = toolbar.addCommand(new FXLoadGDX());
		button.setGraphic(new ImageView("/open.png"));
		button.getStyleClass().add("toolable");
		
		// Separator 추가
		toolbar.addCommand(null);
		
		button = toolbar.addCommand(new FXFullExtent());
		button.setGraphic(new ImageView("/fullextent.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addCommand(new FXFixedZoomIn());
		button.setGraphic(new ImageView("/fixedzoomin.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addCommand(new FXFixedZoomOut());
		button.setGraphic(new ImageView("/fixedzoomout.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addTool(new FXPan());
		button.setGraphic(new ImageView("/pan.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addTool(new FXZoomIn());
		button.setGraphic(new ImageView("/zoomin.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addTool(new FXZoomOut());
		button.setGraphic(new ImageView("/zoomout.png"));
		button.getStyleClass().add("toolable");
		
		// Separator 추가
		toolbar.addTool(null);
				
		button = toolbar.addTool(new FXCalculateDistance());
		button.setGraphic(new ImageView("/measurelength.png"));
		button.getStyleClass().add("toolable");
		
		button = toolbar.addTool(new FXCalculateArea());
		button.setGraphic(new ImageView("/measurearea.png"));
		button.getStyleClass().add("toolable");
		
		// 상단 툴바에 새로운 툴(선택툴) 추가
		button = toolbar.addTool(new FXSelect());
		button.setGraphic(new ImageView("/select.png"));
		button.getStyleClass().add("toolable");
		
		// 추가 된 툴바의 적용을 위한 refresh
		toolbar.refresh();
		
		// 좌측 TOC 생성
		toc = new TocControl();
		toc.setMapControl(mMapControl);
		
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		
		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(toc, mMapControl);
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
