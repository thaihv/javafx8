package com.uitgis.plugin.tilegenerator;

import com.uitgis.maple.application.ContentID;
import com.uitgis.maple.application.Main;
import com.uitgis.maple.common.util.Util;
import com.uitgis.maple.contents.map.ui.MapTabPane;
import com.uitgis.sdk.controls.MapControl;

import framework.FrameworkManager;
import framework.ribbon.RibbonMenu;
import framework.ribbon.RibbonMenu.FileMenuSection;
import framework.ribbon.event.RibbonEventHandler;
import javafx.collections.MapChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OpenGenerator extends RibbonEventHandler {

	private boolean opened;

	public String getContentID() {
		return "tilegenerator";
	}

	public void statusChanged(Change<String, Object> arg) {

	}

	public void doAction(ActionEvent event) {
		if (opened) {
			return;
		}

		MapTabPane mapTabPane = (MapTabPane) FrameworkManager.getUserContent(ContentID.MAP_TAB_KEY);
		MapControl mapControl = mapTabPane.getSelectedMapControl();

		if (mapControl == null) {
			mapControl = Util.newMap();
		}

		TileMainPane tileGenPane = new TileMainPane(mapControl);
		Stage tileStage = new TileGenDialogSkin(tileGenPane, true, IconResources.ICON_TILEGENERATOR).getDialog();
		tileStage.initModality(Modality.NONE);
		tileStage.initOwner(Main.getPrimaryStage());
		
		
		tileStage.setOnShown(e -> {
			opened = true;

			Stage stage = Main.getPrimaryStage();
			double x = stage.getX() + stage.getWidth() / 2 - tileStage.getWidth() / 2;
			double y = stage.getY() + stage.getHeight() / 2 - tileStage.getHeight() / 2;
			tileStage.setX(x);
			tileStage.setY(y);

			mapTabPane.setHold(true);
			System.out.println("SHOW");
			RibbonMenu menu = FrameworkManager.getRibbon();
			menu.setMenuEnablement(FileMenuSection.NewSection, false);
			menu.setMenuEnablement(FileMenuSection.OpenSection, false);
		});

		tileStage.setOnHidden(e -> {
			mapTabPane.setHold(false);

			RibbonMenu menu = FrameworkManager.getRibbon();
			menu.setMenuEnablement(FileMenuSection.NewSection, true);
			menu.setMenuEnablement(FileMenuSection.OpenSection, true);

			System.out.println("CLOSE");
			opened = false;
		});

		tileStage.show();
	}

}
