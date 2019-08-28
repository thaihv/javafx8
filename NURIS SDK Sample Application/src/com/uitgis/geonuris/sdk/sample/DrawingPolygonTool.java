package com.uitgis.geonuris.sdk.sample;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineJoin;

import com.uitgis.geonuris.sdk.controls.MapControl;
import com.uitgis.geonuris.sdk.controls.tools.IFXTool;
import com.uitgis.geonuris.sdk.graphic.UserGraphicPolygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class DrawingPolygonTool implements IFXTool {

	private MapControl mMapControl;
	
	private Polygon mPolygon;

	private boolean mIsEditing;
	
	private List<Point2D> mPressedPoints = new ArrayList<Point2D>();
	
	// list to keep graphics which is used to represents user inputs by mouse.
	private List<Node> mTemporarilyAddedNodes = new ArrayList<Node>();
	
	
	public void setMapControl(MapControl control) {
		mMapControl = control;
	}

	public void onMouseClicked(MouseEvent e) {
		if ( mMapControl.gdxEmpty() || e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		if (mIsEditing && e.getClickCount() == 2) {
			for (Node node : mTemporarilyAddedNodes) {
				mMapControl.removeGraphicNode(node);
			}
			mTemporarilyAddedNodes.clear();
			
			if (mPressedPoints.size() > 2) {
				List<Coordinate> coordinates = new ArrayList<Coordinate>();
				for (Point2D point : mPressedPoints) {
					Coordinate objectivePoint = mMapControl.getObjectivePosition((int)point.getX(), (int)point.getY());
					coordinates.add(objectivePoint);
				}
				coordinates.add(coordinates.get(0));
				
				com.vividsolutions.jts.geom.Polygon geometry = 
						new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
				
				// Actually, We don't have to add user graphic.
				// Just for good understanding, user graphic has been used here.
				// Instead of below, we should create feature using above geometry and insert the feature into data-store.
				UserGraphicPolygon ugPolygon = new UserGraphicPolygon(geometry);
				mMapControl.addUserGraphic(ugPolygon);
				
				System.out.println(geometry.toText());
			}
			
			mPressedPoints.clear();
			mIsEditing = false;
			mPolygon = null;
		}
	}

	public void onMouseMoved(MouseEvent e) {
		if (mMapControl.gdxEmpty()) {
			return;
		}
		
		if (mIsEditing && mPressedPoints.size() > 0) {
			Point movePoint = new Point((int) e.getX(), (int) e.getY());
			if (mPressedPoints.size() == 1) {
				if (mPolygon.getPoints().size() >= 4) {
					mPolygon.getPoints().remove(mPolygon.getPoints().size() - 1);
					mPolygon.getPoints().remove(mPolygon.getPoints().size() - 1);
				}
			}
			else {
				mPolygon.getPoints().remove(mPolygon.getPoints().size() - 1);
				mPolygon.getPoints().remove(mPolygon.getPoints().size() - 1);
			}
			
			mPolygon.getPoints().add(movePoint.getX());
			mPolygon.getPoints().add(movePoint.getY());
		}
	}

	public void onMousePressed(MouseEvent e) {
		if ( mMapControl.gdxEmpty() || e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		if (mPolygon == null) {
			mPolygon = new Polygon();
			mPolygon.setStroke(Color.DARKORANGE);
			mPolygon.setStrokeWidth(2);
			mPolygon.setStrokeLineJoin(StrokeLineJoin.ROUND);
			mPolygon.setFill(new Color(Color.DARKORANGE.getRed(), Color.DARKORANGE.getGreen(), Color.DARKORANGE.getBlue(), 0.3));
			mPolygon.setMouseTransparent(true);
			
			mMapControl.addGraphicNode(mPolygon);
			mTemporarilyAddedNodes.add(mPolygon);
		}
		
		mIsEditing = true;
		
		Point2D p = new Point2D.Double(e.getX(), e.getY());
		mPressedPoints.add(p);
		
		mPolygon.getPoints().add(p.getX());
		mPolygon.getPoints().add(p.getY());
		
		Ellipse ellipse = new Ellipse(p.getX(), p.getY(), 3, 3);
		ellipse.setStroke(Color.DARKORANGE);
		ellipse.setStrokeWidth(2);
		ellipse.setFill(Color.WHITE);
		ellipse.setMouseTransparent( true );
		
		mMapControl.addGraphicNode(ellipse);
		mTemporarilyAddedNodes.add(ellipse);
	}

	public void onMouseReleased(MouseEvent e) {
	}

	public void onMouseDragged(MouseEvent e) {
	}

	public void onMouseEntered(MouseEvent e) {
	}

	public void onMouseExited(MouseEvent e) {
	}

	public void onKeyPressed(KeyEvent e) {
	}

	public void onKeyReleased(KeyEvent e) {
	}

	public void onKeyTyped(KeyEvent e) {
	}

	public void dispose() {
		for (Node node : mTemporarilyAddedNodes) {
			mMapControl.removeGraphicNode(node);
		}
		mTemporarilyAddedNodes.clear();
	}

	public Image getImageIcon() {
		InputStream is = DrawLinestringTool.class.getResourceAsStream("/draw-polygon.png");
		return new Image(is);
	}

	public String getTooltipMessage() {
		return "draw polygon";
	}

	public Cursor getCursor() {
		return Cursor.CROSSHAIR;
	}

	public void setCursor(Cursor cursor) {
	}

}
