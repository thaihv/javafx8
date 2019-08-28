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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import com.uitgis.geonuris.sdk.controls.MapControl;
import com.uitgis.geonuris.sdk.controls.tools.IFXTool;
import com.uitgis.geonuris.sdk.graphic.UserGraphicLine;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class DrawLinestringTool implements IFXTool {

	private MapControl mMapControl;
	
	private Path mPath;
	
	private LineTo mGuideLine;
	
	private boolean mIsEditing;
	
	private List<Point2D> mPressedPoints = new ArrayList<Point2D>();
	
	// list to keep graphics which is used to represents user inputs by mouse.
	private List<Node> mTemporarilyAddedNodes = new ArrayList<Node>();
	
	
	public void dispose() {
		for (Node node : mTemporarilyAddedNodes) {
			mMapControl.removeGraphicNode(node);
		}
		mTemporarilyAddedNodes.clear();
	}

	public Cursor getCursor() {
		return Cursor.CROSSHAIR;
	}

	public Image getImageIcon() {
		InputStream is = DrawLinestringTool.class.getResourceAsStream("/draw-linestring.png");
		return new Image(is);
	}

	public String getTooltipMessage() {
		return "draw linestring";
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
			
			if (mPressedPoints.size() >= 2) {
				List<Coordinate> coordinates = new ArrayList<Coordinate>();
				for (Point2D point : mPressedPoints) {
					Coordinate objectivePoint = mMapControl.getObjectivePosition((int)point.getX(), (int)point.getY());
					coordinates.add(objectivePoint);
				}
				
				LineString geometry = new GeometryFactory().createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
				
				// Actually, We don't have to add user graphic.
				// Just for good understanding, user graphic has been used here.
				// Instead of below, we should create feature using above geometry and insert the feature into data-store.
				UserGraphicLine ugLine = new UserGraphicLine(geometry);
				mMapControl.addUserGraphic(ugLine);
				
				System.out.println(geometry.toText());
			}
			
			mPressedPoints.clear();
			mIsEditing = false;
			mPath = null;
			mGuideLine = null;
		}
			
	}

	public void onMouseMoved(MouseEvent e) {
		if (mMapControl.gdxEmpty()) {
			return;
		}
		
		if (mIsEditing && mPressedPoints.size() > 0) {
			Point movePoint = new Point((int) e.getX(), (int) e.getY());
			if (mGuideLine == null) {
				mGuideLine = new LineTo(movePoint.x, movePoint.y);
				mPath.getElements().add(mGuideLine);
			}
			else {
				mGuideLine.setX(movePoint.x);
				mGuideLine.setY(movePoint.y);
			}
		}
	}


	public void onMousePressed(MouseEvent e) {
		if ( mMapControl.gdxEmpty() || e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		if (mPath == null) {
			mPath = new Path();
			MoveTo moveTo = new MoveTo(e.getX(), (int) e.getY());
			mPath.getElements().add(moveTo);
			
			mPath.setStroke(Color.DARKORANGE);
			mPath.setStrokeWidth(2);
			mPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
			mPath.setStrokeLineCap(StrokeLineCap.ROUND);
			mPath.setMouseTransparent(true);
			
			mMapControl.addGraphicNode(mPath);
			mTemporarilyAddedNodes.add(mPath);
		}
		else {
			mGuideLine = null;
		}
		
		mIsEditing = true;
		Point2D p = new Point2D.Double(e.getX(), e.getY());
		mPressedPoints.add(p);
		
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
	

	public void setMapControl(MapControl control) {
		mMapControl = control;
	}

	public void setCursor(Cursor cursor) {
	}

}
