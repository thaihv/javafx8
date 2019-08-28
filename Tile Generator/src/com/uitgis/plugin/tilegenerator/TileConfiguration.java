package com.uitgis.plugin.tilegenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.uitgis.plugin.tilegenerator.model.TileScale;
import com.uitgis.plugin.tilegenerator.model.WizardData;
import com.uitgis.sdk.datamodel.AbstractStore;
import com.uitgis.sdk.layer.GroupLayer;
import com.uitgis.sdk.reference.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.geom.Envelope;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class TileConfiguration {

	public static final int OUTPUT_PNG = 0;
	public static final int OUTPUT_JPEG = 1;

	public static final int ORDER_ASCENDING = 0;
	public static final int ORDER_DESCENDING = 1;

	public static final int TYPE_FILE_TILEMAP = 0;
	public static final int TYPE_GSS_TILEMAP = 1;

	public static class LevelSpec {
		public int level;
		public double scale;
		public boolean isOn;

		public LevelSpec(int level, double scale, boolean isOn) {
			this.level = level;
			this.scale = scale;
			this.isOn = isOn;
		}
	}

	private String mTileMapName = "";

	private int mTileFormat;

	private int mTileMapType;

	private File mDestinationFolder = new File(System.getProperty("user.home"));;

	private AbstractStore mGSSStore;

	private int mTileWidth = 512;

	private int mTileHeight = 512;

	private String mTilePathExpression = "/{$L}/Y{$Y}/X{$X}";

	private Point2D mOrigin;

	private boolean mAllowEmptyTile;

	private boolean mAllowOverwrite;

	private GroupLayer mSource;

	private Envelope mTargetEnvelope;

	private int mLevelOrder;

	private int mNumberOfLevels;

	private LevelSpec[] mLevelSpecs;

	private boolean mTransparentBackground;

	private Color mBackground;

	private boolean mAntialiasing;

	private boolean mImproveLabelQuality;

	private boolean mEliminateLabelOverlaps;

	private int mNumberOfWorkerThreads = 1;

	public BackendDBMSInfo mBackendDBMSInfo;

	public TileConfiguration(WizardData model) {

		try {
			String path = model.getDestinationFolder();
			if (path != null) {
				mDestinationFolder = new File(path);
			}
		} catch (Throwable t) {
		}

		try {

			mNumberOfLevels = model.getListTileScale().size();
			ObservableList<TileScale> levels = model.getListTileScale();
			List<LevelSpec> levelSpecList = new ArrayList<LevelSpec>();

			for (int i = 0; i < mNumberOfLevels; i++) {

				int level = levels.get(i).getLevel();
				double scale = levels.get(i).getScale();
				boolean isOn = levels.get(i).isActive();

				levelSpecList.add(new LevelSpec(level, scale, isOn));
			}
			mLevelSpecs = levelSpecList.toArray(new TileConfiguration.LevelSpec[levelSpecList.size()]);

			mLevelOrder = model.isOrderLevelAscIsUsed() ? 0 : 1;
			mImproveLabelQuality = model.isImproveLabelQuality();
			mEliminateLabelOverlaps = model.isEliminateLabelQuality();
			mAntialiasing = model.isAntialiasing();
			mTransparentBackground = model.isTransparentBackground();
			mBackground = model.getColorBackground();
			mOrigin = new Point2D(Double.parseDouble(model.getOriginX()), Double.parseDouble(model.getOriginY()));
			mTileMapType = model.getTileMapType();
			mAllowOverwrite = model.isOverWriteAllowed();
			mAllowEmptyTile = model.isGenerateEmptyTile();
			mTileFormat = model.getTileFormat();
			mTileWidth = model.getTileWidth();
			mTileHeight = model.getTileHeight();
		} catch (Throwable t) {
			System.out.println(t);
		}
		try {
			Envelope ev = model.getTargetEnvelope();
			if (ev != null) {
				mTargetEnvelope = ev;
			}
		} catch (Throwable t) {
		}
		try {
			String expr = model.getPathExpression();
			if (expr != null) {
				mTilePathExpression = expr;
			}
		} catch (Throwable t) {
		}

		try {
			String expr = model.getTileName();
			if (expr != null) {
				mTileMapName = expr;
			}
		} catch (Throwable t) {
		}
	}

	public int getNumberOfWorkerThreads() {
		return mNumberOfWorkerThreads;
	}

	public void setNumberOfWorkerThreads(int number) {
		this.mNumberOfWorkerThreads = number;
	}

	public int getTileWidth() {
		return mTileWidth;
	}

	public void setTileWidth(int tileWidth) {
		mTileWidth = tileWidth;
	}

	public int getTileHeight() {
		return mTileHeight;
	}

	public void setTileHeight(int tileHeight) {
		mTileHeight = tileHeight;
	}

	public String getTilePathExpression() {
		return mTilePathExpression;
	}

	public void setTilePathExpression(String expression) {
		this.mTilePathExpression = expression;
	}

	public GroupLayer getSource() {
		return mSource;
	}

	public void setSource(GroupLayer source) {
		mSource = source;
	}

	public File getDestinationFolder() {
		return mDestinationFolder;
	}

	public void setDestinationFolder(File destinationFolder) {
		mDestinationFolder = destinationFolder;
	}

	public Envelope getTargetEnvelope() {
		return mTargetEnvelope;
	}

	public CoordinateReferenceSystem getTargetCRS() {
		if (mTargetEnvelope == null) {
			return null;
		}
		return mTargetEnvelope.getCoordinateReferenceSystem();
	}

	public void setTargetEnvelope(Envelope targetEnvelope) {
		mTargetEnvelope = targetEnvelope;
	}

	public Point2D getOrigin() {
		return mOrigin;
	}

	public void setOrigin(Point2D origin) {
		this.mOrigin = origin;
	}

	public int getLevelOrder() {
		return mLevelOrder;
	}

	public void setLevelOrder(int order) {
		this.mLevelOrder = order;
	}

	public int getNumberOfLevels() {
		return mNumberOfLevels;
	}

	public void setNumberOfLevels(int numberOfLevels) {
		mNumberOfLevels = numberOfLevels;
	}

	public LevelSpec[] getLevels() {
		return mLevelSpecs;
	}

	public void setLevels(LevelSpec[] levels) {
		this.mLevelSpecs = levels;
	}

	public int getTileFormat() {
		return mTileFormat;
	}

	public void setTileFormat(int outputType) {
		mTileFormat = outputType;
	}

	public String getOutputTypeAsString() {
		switch (mTileFormat) {
		case OUTPUT_JPEG:
			return "jpg";
		default:
			return "png";
		}
	}

	public AbstractStore getGSSStore() {
		return mGSSStore;
	}

	public void setGSSStore(AbstractStore store) {
		mGSSStore = store;
	}

	public String getTileMapName() {
		return mTileMapName;
	}

	public void setTileMapName(String name) {
		this.mTileMapName = name;
	}

	public boolean isTransparentBackground() {
		return mTransparentBackground;
	}

	public void setTransparentBackground(boolean transparentBackground) {
		mTransparentBackground = transparentBackground;
	}

	public Color geColorBackground() {
		return mBackground;
	}

	public void seColorBackground(Color mBackground) {
		this.mBackground = mBackground;
	}

	public boolean isImproveLabelQuality() {
		return mImproveLabelQuality;
	}

	public void setImproveLabelQuality(boolean improveLabelQuality) {
		mImproveLabelQuality = improveLabelQuality;
	}

	public boolean isEliminateLabelOverlaps() {
		return mEliminateLabelOverlaps;
	}

	public void setEliminateLabelOverlaps(boolean eliminateLabelOverlaps) {
		mEliminateLabelOverlaps = eliminateLabelOverlaps;
	}

	public boolean getAntialiasing() {
		return mAntialiasing;
	}

	public void setAntialiasing(boolean antialiasing) {
		this.mAntialiasing = antialiasing;
	}

	public boolean emptyTileAllowed() {
		return mAllowEmptyTile;
	}

	public void allowEmptyTile(boolean allow) {
		this.mAllowEmptyTile = allow;
	}

	public boolean overwriteAllowed() {
		return mAllowOverwrite;
	}

	public void allowOverwrite(boolean allow) {
		this.mAllowOverwrite = allow;
	}

	public int getTypeOfTileMap() {
		return mTileMapType;
	}

	public void setTypeOfTileMap(int type) {
		this.mTileMapType = type;
	}

}