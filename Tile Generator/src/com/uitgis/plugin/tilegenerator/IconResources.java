package com.uitgis.plugin.tilegenerator;

import java.net.URL;

import javafx.scene.image.Image;

public class IconResources {

	public static final Image ICON_TILEGENERATOR;

	static {
		ClassLoader loader = TileGenerator.class.getClassLoader();

		URL url = loader.getResource("icons/tilegenerator.png");
		ICON_TILEGENERATOR = new Image(url.toString());

	}

}
