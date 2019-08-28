package com.uitgis.geonuris.sdk.sample;

import java.io.InputStream;

import javafx.scene.image.Image;

import com.uitgis.geonuris.sdk.controls.MapControl;
import com.uitgis.geonuris.sdk.controls.tools.IFXCommand;

public class RemoveDrawingTool implements IFXCommand {

	public void execute(MapControl mapControl) {
		mapControl.removeAllUserGraphic();
	}

	public Image getImageIcon() {
		InputStream is = DrawLinestringTool.class.getResourceAsStream("/erase-drawing.png");
		return new Image(is);
	}

	public String getTooltipMessage() {
		return "remove all graphics";
	}

}
