package com.uitgis.plugin.tilegenerator;

import java.net.URL;

import com.uitgis.maple.application.ContentID;
import com.uitgis.maple.application.Main;
import com.uitgis.maple.plugin.MaplePlugin;
import com.uitgis.maple.plugin.PlugInUtil;

import framework.i18n.I18N;
import framework.ribbon.RibbonMenu;
import framework.ribbon.RibbonPane;
import framework.ribbon.RibbonTab;
import framework.ribbon.constants.ItemType;
import framework.ribbon.item.RibbonActionItem;

public class TileGenerator extends MaplePlugin {

	public String getID() {
		return "tilegenerator";
	}

	public String getTitle() {
		return "TileGenerator";
	}

	public String getDescription() {
		return null;
	}

	public void init(RibbonMenu menu) {
		
//		FrameworkManager.getStyleSheets().add("styles/tilemap.css");
		RibbonTab tab = PlugInUtil.getRibbonTab(menu, ContentID.MENU_DATATOOL, "Tool");								
		ClassLoader loader = getClass().getClassLoader();
		URL url = loader.getResource("icons/tilegenerator.png");
		
		System.out.println(I18N.getText("menu.group.title"));
		System.out.println(I18N.getText("menu.button.title"));
		
		RibbonPane newPane = new RibbonPane(I18N.getText("menu.group.title"));
		newPane.addItem(new RibbonActionItem(I18N.getText("menu.button.title"), url.toString(), new OpenGenerator(), ItemType.BIG_Button));

		tab.addPane(newPane);

	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Main maple = new Main();
		maple.main(args);
	}

}
