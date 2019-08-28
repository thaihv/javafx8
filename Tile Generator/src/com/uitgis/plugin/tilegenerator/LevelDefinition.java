package com.uitgis.plugin.tilegenerator;


import com.vividsolutions.jts.geom.Envelope;

import javafx.geometry.Point2D;

public class LevelDefinition {

	private int mLevel;
	
	private TileRange mRange;
	
	private double mTileDistanceOnXAXIS;
	
	private double mTileDistanceOnYAXIS;
	
	public LevelDefinition(int level, TileRange range, double distanceOnXAXIS, double distanceOnYAXIS) {
		this.mLevel = level;
		this.mRange = range;
		this.mTileDistanceOnXAXIS = distanceOnXAXIS;
		this.mTileDistanceOnYAXIS = distanceOnYAXIS;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	public TileRange getTileRange() {
		return mRange;
	}
	
	public double getTileDistanceOnXAXIS() {
		return mTileDistanceOnXAXIS;
	}
	
	public double getTileDistanceOnYAXIS() {
		return mTileDistanceOnYAXIS;
	}
	
	public Envelope getEnvelope(Point2D origin) {
		double minx = origin.getX() + (mRange.getMinimumX() - 1) * mTileDistanceOnXAXIS;
		double miny = origin.getY() + (mRange.getMinimumY() - 1) * mTileDistanceOnYAXIS;
		double maxx = minx + mRange.getNumberOfTilesOnXAXIS() * mTileDistanceOnXAXIS;
		double maxy = miny + mRange.getNumberOfTilesOnYAXIS() * mTileDistanceOnYAXIS;
		
		return new Envelope(minx, miny, maxx, maxy);
	}

}