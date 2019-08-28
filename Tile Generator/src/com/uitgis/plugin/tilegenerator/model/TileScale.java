package com.uitgis.plugin.tilegenerator.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class TileScale {

	private IntegerProperty level;
	private DoubleProperty scale;
	private BooleanProperty active;

	public TileScale(boolean bActive, int nLevel, double dScale) {
		setActive(bActive);
		setLevel(nLevel);
		setScale(dScale);
	}

	@Override
	public String toString() {
		return "TileScale: " + active.getValue() + " " + level.getValue() + " " + scale.getValue();
	}

	public BooleanProperty activeProperty() {
		if (this.active == null)
			this.active = new SimpleBooleanProperty(true);
		return this.active;

	}

	public boolean isActive() {
		return this.activeProperty().get();
	}

	public void setActive(boolean active) {
		this.activeProperty().set(active);
	}

	public IntegerProperty levelProperty() {
		if (this.level == null)
			this.level = new SimpleIntegerProperty(0);
		return this.level;
	}

	public int getLevel() {
		return this.levelProperty().get();
	}

	public void setLevel(int level) {
		this.levelProperty().set(level);
	}

	public DoubleProperty scaleProperty() {
		if (this.scale == null)
			this.scale = new SimpleDoubleProperty(Double.MIN_VALUE);
		return this.scale;
	}

	public double getScale() {
		return this.scaleProperty().get();
	}

	public void setScale(double scale) {
		this.scaleProperty().set(scale);
	}

}