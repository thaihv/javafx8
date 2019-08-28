package com.uitgis.plugin.tilegenerator;

public class TileRange {

	private int mMinimumX;
	private int mMaximumX;

	private int mMinimumY;
	private int mMaximumY;

	public TileRange(int minx, int maxx, int miny, int maxy) {
		this.mMinimumX = minx;
		this.mMaximumX = maxx;
		this.mMinimumY = miny;
		this.mMaximumY = maxy;
	}

	public int getMinimumX() {
		return mMinimumX;
	}

	public void setMinimumX(int x) {
		this.mMinimumX = x;
	}

	public int getMaximumX() {
		return mMaximumX;
	}

	public void setMaximumX(int x) {
		this.mMaximumX = x;
	}

	public int getMinimumY() {
		return mMinimumY;
	}

	public void setMinimumY(int y) {
		this.mMinimumY = y;
	}

	public int getMaximumY() {
		return mMaximumY;
	}

	public void setMaximumY(int y) {
		this.mMaximumY = y;
	}

	public int getNumberOfAllTiles() {
		return (mMaximumX - mMinimumX + 1) * (mMaximumY - mMinimumY + 1);
	}

	public int getNumberOfTilesOnXAXIS() {
		return mMaximumX - mMinimumX + 1;
	}

	public int getNumberOfTilesOnYAXIS() {
		return mMaximumY - mMinimumY + 1;
	}

}