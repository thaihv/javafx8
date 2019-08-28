package com.uitgis.plugin.tilegenerator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.inject.Inject;
import com.uitgis.maple.common.util.Noti;
import com.uitgis.plugin.tilegenerator.model.WizardData;
import com.uitgis.sdk.controls.MapTransform;
import com.uitgis.sdk.datamodel.table.IResultSet;
import com.uitgis.sdk.datamodel.table.IResultSetIterator;
import com.uitgis.sdk.filter.spatial.Intersects;
import com.uitgis.sdk.layer.AbstractLayer;
import com.uitgis.sdk.layer.FeatureLayer;
import com.uitgis.sdk.layer.GroupLayer;
import com.uitgis.sdk.layer.ILayer;
import com.uitgis.sdk.layer.RasterLayer;
import com.uitgis.sdk.layer.ScreenOffset;
import com.uitgis.sdk.layer.TileMapLayer;
import com.uitgis.sdk.layer.WMSLayer;
import com.uitgis.sdk.reference.crs.CoordinateReferenceSystem;
import com.uitgis.sdk.reference.crs.GeographicCRS;
import com.uitgis.sdk.reference.datum.GeodeticDatum;
import com.uitgis.sdk.style.symbol.Context;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import framework.i18n.I18N;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;

public class TileGenTask extends Task<Void> {

	private static final String KEY_LEVEL = "{$L}";
	private static final String KEY_X = "{$X}";
	private static final String KEY_Y = "{$Y}";

	private Thread mainThread;
	private int numberOfThread;
	private int imageType;
	private int totalWork;
	private int count = 0;
	Semaphore lock = new Semaphore(100);

	private LevelDefinition[] levelDefs;
	private TileConfiguration tileConfiguration;
	private boolean isOverwriteMode;

	@Inject
	WizardData model;

	private synchronized void amountSync() {
		count = count + 1;
	}

	public TileGenTask(int mThreadNum) {
		super();
		this.numberOfThread = mThreadNum;
	}

	public int getmThreadNum() {
		return numberOfThread;
	}

	public void setmThreadNum(int mThreadNum) {
		this.numberOfThread = mThreadNum;
	}

	@Override
	protected Void call() throws Exception {

		// Initialize a thread pool & task list
		mainThread = Thread.currentThread();
		if (numberOfThread < 0)
			return null;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		List<Future<?>> tasks = new ArrayList<>();

		// Seting up Tile Configuration & check condition to gen
		tileConfiguration = new TileConfiguration(model);
		isOverwriteMode = false;
		boolean buildable = false;

		int type = tileConfiguration.getTypeOfTileMap();
		if (type == TileConfiguration.TYPE_FILE_TILEMAP) {
			buildable = checkFTMDuplication();
		} else if (type == TileConfiguration.TYPE_GSS_TILEMAP) {
			// buildable = checkGTMDuplication();
		}

		if (!buildable) {
			return null;
		}

		// Check grouplayer is existed?
		GroupLayer group = model.getGDX().getRootGroupLayer();
		List<ILayer> layers = group.getLayers();
		if (layers.isEmpty()) {
			Platform.runLater(() -> {
				Noti.showInfo(I18N.getText("err.message.1"));
			});
			return null;
		}

		// Get levels to calculate
		TileConfiguration.LevelSpec[] levels = tileConfiguration.getLevels();
		if (levels.length == 0) {
			Platform.runLater(() -> {
				Noti.showInfo(I18N.getText("err.message.2"));
			});
			return null;
		}

		CoordinateReferenceSystem targetCRS = tileConfiguration.getTargetCRS();
		Envelope envelope = tileConfiguration.getTargetEnvelope();

		Point2D origin = tileConfiguration.getOrigin();

		double factor = 1;
		if (targetCRS instanceof GeographicCRS) {
			GeodeticDatum datum = (GeodeticDatum) ((GeographicCRS) targetCRS).getDatum();
			double semiMajorAxis = datum.getEllipsoid().getSemiMajorAxis();
			double circumference = 2D * Math.PI * semiMajorAxis;
			factor = 360D / circumference;
		}

		int numOfLevels = tileConfiguration.getNumberOfLevels();
		levelDefs = new LevelDefinition[numOfLevels];

		int totalTiles = 0;

		for (int i = 0; i < numOfLevels; i++) {
			if (!levels[i].isOn) {
				continue;
			}

			int level = tileConfiguration.getLevelOrder() == TileConfiguration.ORDER_DESCENDING ? levels.length - i - 1
					: i;
			double hdistance = model.getTileWidth() * levels[i].scale * factor;
			double vdistance = model.getTileHeight() * levels[i].scale * factor;
			int hStart = getGridCellNo(envelope.getMinX() - origin.getX(), hdistance, false);
			int hEnd = getGridCellNo(envelope.getMaxX() - origin.getX(), hdistance, true);
			int vStart = getGridCellNo(envelope.getMinY() - origin.getY(), vdistance, false);
			int vEnd = getGridCellNo(envelope.getMaxY() - origin.getY(), vdistance, true);

			levelDefs[i] = new LevelDefinition(level, new TileRange(hStart, hEnd, vStart, vEnd), hdistance, vdistance);

			totalTiles += (hEnd - hStart + 1) * (vEnd - vStart + 1);
		}
		if (type == TileConfiguration.TYPE_FILE_TILEMAP) {
			ensureFTMPrerequisite();
		} else if (type == TileConfiguration.TYPE_GSS_TILEMAP) {
			// ensureGTMPrerequisite();
		}
		// Ok, Done to calculate Total of Works, let display to status
		totalWork = totalTiles;
		updateProgress(0, totalWork);
		updateMessage("Building ... (total number of tiles: " + totalWork + ")");

		// Divide the task to units and assign to thread
		long time = System.currentTimeMillis();

		// Calculate by Level (Z in case Google XYZ)
		for (int i = 0; i < numOfLevels; i++) {
			if (levelDefs[i] == null) {
				continue;
			}
			TileRange range = levelDefs[i].getTileRange();
			// Calculate by Y
			for (int vindex = range.getMinimumY() - 1, vEnd = range.getMaximumY(); vindex < vEnd; vindex++) {
				double y = origin.getY() + vindex * levelDefs[i].getTileDistanceOnYAXIS();
				// Calculate by X
				for (int hindex = range.getMinimumX() - 1, hEnd = range.getMaximumX(); hindex < hEnd; hindex++) {
					if (isCancelled()) {
						break;
					}

					double x = origin.getX() + hindex * levelDefs[i].getTileDistanceOnXAXIS();

					Envelope bbox = new Envelope(x, x + levelDefs[i].getTileDistanceOnXAXIS(), y,
							y + levelDefs[i].getTileDistanceOnYAXIS(), tileConfiguration.getTargetCRS());

					// Transparent for tile background or not
					imageType = tileConfiguration.isTransparentBackground() ? BufferedImage.TYPE_INT_ARGB
							: BufferedImage.TYPE_INT_RGB;

					BufferedImage bi = new BufferedImage(tileConfiguration.getTileWidth(),
							tileConfiguration.getTileHeight(), imageType);
					Graphics g = bi.getGraphics();

					// Not transparent, fill a selected color
					if (imageType == BufferedImage.TYPE_INT_RGB) {
						javafx.scene.paint.Color fx = tileConfiguration.geColorBackground();
						g.setColor(new Color((float) fx.getRed(), (float) fx.getGreen(), (float) fx.getBlue(),
								(float) fx.getOpacity()));
						g.fillRect(0, 0, tileConfiguration.getTileWidth(), tileConfiguration.getTileHeight());
					}

					Context ctx = new Context(model.getGDX(), (Graphics2D) g);
					// Semaphore acquired to control access resources to limit 100 concurrencies
					lock.acquire();

					// If Google tile map indexes setting, reserve vindex by doing assign it to:
					// vEnd - vindex - 1
					// TileGenCallable callable = new TileGenCallable(ctx, bbox, layers, bi,
					// levelDefs[i].getLevel(), hindex, vEnd - vindex - 1)

					TileGenCallable callable = new TileGenCallable(ctx, bbox, layers, bi, levelDefs[i].getLevel(),
							hindex, vindex); // Use Default TMS indexed

					Future<?> task = executor.submit(callable);
					tasks.add(task);
				} // X
			} // Y
		} // LEVEL
			// Check if all is done to message to user
		boolean allDone = true;
		for (Future<?> fut : tasks) {
			try {

				System.out.println(new Date() + "::" + fut.get());
				allDone &= fut.isDone();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();

		if (allDone) {
			Platform.runLater(() -> {
				Noti.showInfo(I18N.getText("Msg_GenTileCompleted"));
			});
		}
		System.out.println(
				(System.currentTimeMillis() - time) + "ms has been elapsed to build " + totalTiles + " tiles.");
		done();

		return null;
	}

	@Override
	protected void cancelled() {
		try {
			mainThread.interrupt();
			lock.release();
			Platform.runLater(() -> {
				Noti.showInfo(I18N.getText("Msg_GenTileCancelled"));
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.cancelled();
	}

	private int getGridCellNo(double coordinate, double gridSize, boolean excludeTouch) {
		double d = coordinate / gridSize;
		if (d < 0) {
			return (int) Math.round(d - 0.5D) + 1;
		}
		return (!excludeTouch || d > (int) d) ? (int) d + 1 : (int) d;
	}

	private boolean checkFTMDuplication() {
		File tileMapFolder = new File(tileConfiguration.getDestinationFolder(), tileConfiguration.getTileMapName());
		File tileMapFile = new File(tileMapFolder, "tilemap.xml"); //$NON-NLS-1$
		boolean buildable = true;

		if (tileMapFile.exists()) {
			if (tileConfiguration.overwriteAllowed()) {
				isOverwriteMode = true;
			} else {
				Platform.runLater(() -> {
					Noti.showAlert(I18N.getText("err.title.5"), I18N.getText("err.message.5"));
				});
				buildable = false;
			}
		}

		return buildable;
	}

	private void ensureFTMPrerequisite() {
		if (isOverwriteMode) {
			return;
		}

		File tileMapFolder = new File(model.getDestinationFolder(), tileConfiguration.getTileMapName());
		File tileMapFile = new File(tileMapFolder, "tilemap.xml"); //$NON-NLS-1$

		tileMapFolder.mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(tileMapFile);

			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setNamespaceAware(true);
			fac.setValidating(false);
			fac.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", //$NON-NLS-1$
					"http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$
			DocumentBuilder builder = fac.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.newDocument();
			TileMapDomHelper.encodeTileMapXML(doc, tileConfiguration);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			t.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$

			DOMSource doms = new DOMSource(doc);
			StreamResult sr = new StreamResult(out);

			t.transform(doms, sr);

			out.flush();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {
			try {
				out.close();
			} catch (Throwable t) {
			}
		}
	}

	private File getTileFile(TileConfiguration configuration, int level, int xIndex, int yIndex) {
		String expression = configuration.getTilePathExpression();
		expression = replaceVariables(expression, KEY_LEVEL, String.valueOf(level));
		expression = replaceVariables(expression, KEY_X, String.valueOf(xIndex));
		expression = replaceVariables(expression, KEY_Y, String.valueOf(yIndex));

		File tileMapFolder = new File(configuration.getDestinationFolder(), configuration.getTileMapName());
		return new File(tileMapFolder, expression + "." + configuration.getOutputTypeAsString());
	}

	private String replaceVariables(String source, String key, String replaceValue) {
		int index = -1;
		while ((index = source.indexOf(key)) > -1) {
			String pre = source.substring(0, index);
			String sub = source.substring(index + key.length());
			source = pre + replaceValue + sub;
		}
		return source;
	}

	protected void save(BufferedImage bi, File tileFile, String format) throws IOException {
		FileOutputStream out = null;
		try {
			tileFile.getParentFile().mkdirs();

			out = new FileOutputStream(tileFile);
			writeRaster(bi, format, out);
		} finally {
			try {
				out.close();
			} catch (Throwable t) {
			}
		}
	}

	protected void writeRaster(BufferedImage bi, String format, OutputStream out) throws IOException {
		ImageIO.write(bi, format, out);
	}

	public boolean getMapImage(Context context, List<ILayer> layers) {

		boolean hasContent = false;

		if (context.getEnvelope() != null) {
			MapTransform transform = new MapTransform(context.getEnvelope(), context.getWidth(), context.getHeight(),
					context.getMapCRS());
			context.setMapToScreenTransform(transform);
			// Anti-aliasing for text
			context.getGraphics().setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					tileConfiguration.isImproveLabelQuality() ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
							: RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			// Anti-alias for shape
			context.getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					tileConfiguration.getAntialiasing() ? RenderingHints.VALUE_ANTIALIAS_ON
							: RenderingHints.VALUE_ANTIALIAS_OFF);
			// Hide overlapped for label.
			context.setHideLabelOverlaps(tileConfiguration.isEliminateLabelOverlaps());

			if (layers == null || layers.size() == 0) {
				for (int i = model.getGDX().getLayerCount() - 1; i >= 0; i--) {
					ILayer layer = model.getGDX().getLayer(i);
					context.setDrawAllVertices(true);
					drawLayer(context, layer);
					if (context.isStopped()) {
						break;
					}
				}

				for (int i = model.getGDX().getLayerCount() - 1; i >= 0; i--) {
					ILayer layer = model.getGDX().getLayer(i);
					drawLabels(context, layer);
					if (context.isStopped()) {
						break;
					}
				}
			} else {
				// Check empty tiles
				for (int i = layers.size() - 1; i >= 0; i--) {
					ILayer layer = layers.get(i);
					boolean b = isNotEmptyTile(context, layer);
					hasContent = hasContent || b;
					if (context.isStopped()) {
						break;
					}

				}
				// Drawing...
				for (int i = layers.size() - 1; i >= 0; i--) {
					ILayer layer = layers.get(i);
					drawLayer(context, layer);
					if (context.isStopped()) {
						break;
					}
				}

				for (int i = layers.size() - 1; i >= 0; i--) {
					ILayer layer = layers.get(i);
					drawLabels(context, layer);
					if (context.isStopped()) {
						break;
					}
				}
			}
		}
		return hasContent;
	}

	private void drawLayer(Context ctx, ILayer layer) {

		if (!layer.isVisible()) {
			return;
		}

		if (ctx.getEnvelope() == null) {
			return;
		}
		if (layer instanceof GroupLayer) {
			GroupLayer grouplayer = (GroupLayer) layer;

			ArrayList<ILayer> layers = new ArrayList<ILayer>();
			int cnt = grouplayer.getLayerCount();
			for (int i = 0; i < cnt; i++) {
				layers.add(grouplayer.getLayer(i));
			}
			if (isDrawableLayer(ctx, grouplayer)) {
				for (int i = layers.size() - 1; i >= 0; i--) {
					ILayer sublayer = layers.get(i);
					drawLayer(ctx, sublayer);
				}
			}
		} else if (isDrawableLayer(ctx, layer)) {
			if (layer instanceof FeatureLayer) {

				FeatureLayer fl = (FeatureLayer) layer;
				ScreenOffset sOffset = fl.getScreenOffset();
				if (sOffset != null) {
					ctx.getGraphics().translate(sOffset.getPixelOffsetX(ctx.getScale()),
							sOffset.getPixelOffsetY(ctx.getScale()));
				}

				fl.drawLayer(ctx);

				if (sOffset != null) {
					ctx.getGraphics().translate(sOffset.getPixelOffsetX(ctx.getScale()) * -1,
							sOffset.getPixelOffsetY(ctx.getScale()) * -1);
				}
			} else if (layer instanceof RasterLayer) {
				((RasterLayer) layer).drawLayer(ctx);
			} else if (layer instanceof WMSLayer) {
				((WMSLayer) layer).drawLayer(ctx);
			} else if (layer instanceof TileMapLayer) {
				((TileMapLayer) layer).drawLayer(ctx);
			}
		}
	}

	private boolean isDrawableLayer(Context ctx, ILayer layer) {
		return layer.isVisible() && layer.getOpacity() > 0 && ctx.getScale() >= ((AbstractLayer) layer).getMinScale()
				&& ctx.getScale() < ((AbstractLayer) layer).getMaxScale() && ((AbstractLayer) layer).isDataUsable();
	}

	private boolean isNotEmptyTile(Context ctx, ILayer layer) {
		if (layer instanceof FeatureLayer) {

			FeatureLayer ly = (FeatureLayer) layer;
			Intersects filter = new Intersects((ly).getFeatureTable().getGeometryName(),
					new GeometryFactory().toGeometry(ctx.getEnvelope()));

			IResultSet rs = null;
			IResultSetIterator iterator = null;
			try {
				rs = ly.getFeatures(filter);
				iterator = rs.iterator();

				if (!iterator.hasNext()) {
					// System.out.println("EMPTY TILE........" + "> Level:" + ctx.getScale() + " X:"
					// + ctx.getX() + " Y:" + ctx.getY());
					return false;
				}
			} catch (Exception e) {

			} finally {
				if (iterator != null) {
					iterator.close();
				}

				if (rs != null) {
					rs.close();
				}
			}
			return true;
		}
		return true;
	}

	private void drawLabels(Context ctx, ILayer layer) {
		if (layer instanceof GroupLayer) {
			GroupLayer grouplayer = (GroupLayer) layer;
//			ArrayList<ILayer> layers = grouplayer.getLayers();
			ArrayList<ILayer> layers = new ArrayList<ILayer>();
			int cnt = grouplayer.getLayerCount();
			for (int i = 0; i < cnt; i++) {
				layers.add(grouplayer.getLayer(i));
			}

			if (isDrawableLayer(ctx, grouplayer)) {
				for (int i = 0; i < layers.size(); i++) {
					ILayer sublayer = layers.get(i);
					drawLabels(ctx, sublayer);
				}
			}
		} else {
			if (layer instanceof FeatureLayer) {
				if (isDrawableLayer(ctx, layer)) {
					((FeatureLayer) layer).drawLabel(ctx);
				}
			}
		}
	}

	public class TileGenCallable implements Callable<String> {

		private Context context;
		private Envelope envelope;
		private int level;
		private int xTileIndex;
		private int yTileIndex;
		private List<ILayer> Ilayers;
		private BufferedImage bi;

		public TileGenCallable(Context context, Envelope envelope, List<ILayer> Ilayers, BufferedImage bi, int level,
				int xTileIndex, int yTileIndex) {
			super();
			this.context = context;
			this.envelope = envelope;
			this.level = level;
			this.xTileIndex = xTileIndex;
			this.yTileIndex = yTileIndex;
			this.Ilayers = Ilayers;
			this.bi = bi;
		}

		public List<ILayer> getIlayers() {
			return Ilayers;
		}

		public void setIlayers(List<ILayer> ilayers) {
			Ilayers = ilayers;
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public Envelope getEnvelope() {
			return envelope;
		}

		public void setEnvelope(Envelope envelope) {
			this.envelope = envelope;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public int getxTileIndex() {
			return xTileIndex;
		}

		public void setxTileIndex(int xTileIndex) {
			this.xTileIndex = xTileIndex;
		}

		public int getyTileIndex() {
			return yTileIndex;
		}

		public void setyTileIndex(int yTileIndex) {
			this.yTileIndex = yTileIndex;
		}

		@Override
		public String call() throws Exception {
			if (isCancelled()) {
				return null;
			}
			try {
				// Setting context to the respective tile
				context.setEnvelope(envelope);
				context.setWidth(tileConfiguration.getTileWidth());
				context.setHeight(tileConfiguration.getTileHeight());
				context.setScale(tileConfiguration.getLevels()[level].scale);

				MapTransform transform = new MapTransform(context.getEnvelope(), context.getWidth(),
						context.getHeight(), context.getMapCRS());
				context.setMapToScreenTransform(transform);
				context.setX(xTileIndex);
				context.setY(yTileIndex);

				boolean hasDrawn = getMapImage(context, Ilayers);

				if (hasDrawn || tileConfiguration.emptyTileAllowed()) {
					File tileFile = getTileFile(tileConfiguration, level, xTileIndex, yTileIndex);
/*
					if (tileConfiguration.getTileFormat() == TileConfiguration.OUTPUT_JPEG
							&& tileConfiguration.isTransparentBackground()) { // OpenJDK has not support transparent
																				// channel for JPEG in native encoder, this
																				// conversation is a work around
						BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(),
								BufferedImage.TYPE_3BYTE_BGR);
						convertedImg.getGraphics().drawImage(bi, 0, 0, null);
						convertedImg.getGraphics().dispose();
						save(convertedImg, tileFile, tileConfiguration.getOutputTypeAsString());

						// Using external library to create Jpeg
//						System.out.println(tileFile.getName() + " Encode to : " +  tileConfiguration.getOutputTypeAsString());
//						JAI.create("filestore", convertedImg, "C:\\data\\Tiles\\DDD\\" + tileFile.getName(), "JPEG");

					} else */
					
					save(bi, tileFile, tileConfiguration.getOutputTypeAsString());
				}
				amountSync();
				updateMessage(I18N.getText("Msg_GenTileProcess") + ": Completed " + count + " / " + totalWork);
				System.out.println(Thread.currentThread().getName() + "> Level:" + level + " X:" + xTileIndex + " Y:"
						+ yTileIndex);
			} catch (Throwable t) {
				System.err.println("ERROR [Level:" + level + " X:" + xTileIndex + " Y:" + yTileIndex + "]");
				t.printStackTrace(System.err);
			} finally {
				lock.release();
			}
			return Thread.currentThread().getName();
		}

	};
}