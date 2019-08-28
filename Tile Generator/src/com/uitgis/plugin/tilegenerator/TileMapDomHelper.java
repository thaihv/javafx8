package com.uitgis.plugin.tilegenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.uitgis.sdk.reference.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.geom.Envelope;

public class TileMapDomHelper {
	
	public static void encodeTileMapXML(Document doc, TileConfiguration configuration) {
		Element root = doc.createElement("TileMap");
//		root.setAttribute("allowOverwrite", String.valueOf(configuration.overwriteAllowed()));
		doc.appendChild(root);
		
//		Element sub1 = doc.createElement("Name");
//		sub1.setTextContent(configuration.getRasterName());
//		root.appendChild(sub1);
		
		Element sub1 = doc.createElement("Tile");
//		sub1.setAttribute("allowEmptyTile", String.valueOf(configuration.emptyTileAllowed()));
		root.appendChild(sub1);
		
		Element sub2 = doc.createElement("Width");
		sub2.setTextContent(String.valueOf(configuration.getTileWidth()));
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("Height");
		sub2.setTextContent(String.valueOf(configuration.getTileHeight()));
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("Transparent");
		sub2.setTextContent(String.valueOf(configuration.isTransparentBackground()));
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("PathExpression");
		sub2.setTextContent(configuration.getTilePathExpression());
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("Origin");
		sub2.setTextContent(configuration.getOrigin().getX() + " " + configuration.getOrigin().getY());
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("Format");
		sub2.setTextContent(configuration.getOutputTypeAsString());
		sub1.appendChild(sub2);
		
		sub1 = doc.createElement("Pyramid");
		sub1.setAttribute("numLevels", String.valueOf(configuration.getNumberOfLevels()));
		sub1.setAttribute("levelOrder", configuration.getLevelOrder()==TileConfiguration.ORDER_DESCENDING?"desc":"asc");
		root.appendChild(sub1);
		
		sub2 = doc.createElement("Resolutions");
		StringBuffer sb = new StringBuffer();
		TileConfiguration.LevelSpec[] levels = configuration.getLevels();
		for (TileConfiguration.LevelSpec l : levels) {
			sb.append(l.scale).append(' ');
		}
//		double[] scales = configuration.getScales();
//		for (double s : scales) {
//			sb.append(s).append(' ');
//		}
		sub2.setTextContent(sb.toString().trim());
		sub1.appendChild(sub2);
		
		sub1 = doc.createElement("Envelope");
		root.appendChild(sub1);
		
		Envelope envelope = configuration.getTargetEnvelope();
		sub2 = doc.createElement("LowerCorner");
		sub2.setTextContent(envelope.getMinX() + " " + envelope.getMinY());
		sub1.appendChild(sub2);
		
		sub2 = doc.createElement("UpperCorner");
		sub2.setTextContent(envelope.getMaxX() + " " + envelope.getMaxY());
		sub1.appendChild(sub2);
		
		sub1 = doc.createElement("CRS");
		CoordinateReferenceSystem crs = configuration.getTargetCRS();
		sub1.setTextContent(crs == null ? "unknown" : crs.toWKT());
		root.appendChild(sub1);
	}

}