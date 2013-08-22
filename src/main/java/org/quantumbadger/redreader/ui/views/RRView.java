package org.quantumbadger.redreader.ui.views;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import org.quantumbadger.redreader.common.General;
import org.quantumbadger.redreader.ui.views.touch.RROffsetClickHandler;
import org.quantumbadger.redreader.ui.views.touch.RRSingleTouchHandlerProvider;

public abstract class RRView implements RRViewParent, RRSingleTouchHandlerProvider {

	private RRViewParent parent;
	private TouchEventHandler touchEventHandler;

	protected int paddingTop, paddingBottom, paddingLeft, paddingRight;
	protected Paint paddingPaint = null, backgroundPaint = null;

	private int width = -1, height = -1;

	public static final int HOVER_START = 1, HOVER_HIGHLIGHT = 2, HOVER_LONGCLICK = 3, HOVER_CANCEL = 4, TAP = 5;
	public static final int UNSPECIFIED = -1;

	private boolean unrenderable = true;
	private static final Paint unrenderablePaint = General.createPaint(Color.RED);

	public final synchronized void draw(final Canvas canvas, final int desiredWidth) {

		if(unrenderable || Looper.getMainLooper().getThread() == Thread.currentThread()) {
			final int size = 20;
			canvas.drawLine(0, 0, 0, size, unrenderablePaint);
			canvas.drawLine(0, size, size, size, unrenderablePaint);
			canvas.drawLine(0, 0, size, 0, unrenderablePaint);
			canvas.drawLine(size, size, size, 0, unrenderablePaint);
			canvas.drawLine(0, 0, size, size, unrenderablePaint);
			canvas.drawLine(0, size, size, 0, unrenderablePaint);
		}

		if(unrenderable) {
			return;
		}

		if(width != desiredWidth) setWidth(desiredWidth);

		if(paddingPaint != null) {
			canvas.drawRect(0, 0, width, paddingTop, paddingPaint);
			canvas.drawRect(0, height - paddingBottom, width, height, paddingPaint);
			canvas.drawRect(0, paddingTop, paddingLeft, height - paddingBottom, paddingPaint);
			canvas.drawRect(width - paddingRight, paddingTop, width, height - paddingBottom, paddingPaint);
		}

		if(backgroundPaint != null) {
			canvas.drawRect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom, backgroundPaint);
		}

		canvas.save();
		canvas.translate(paddingLeft, paddingTop);

		onRender(canvas);

		canvas.restore();
	}

	protected abstract void onRender(Canvas canvas);

	public final void rrInvalidate() {
		parent.rrInvalidate();
	}

	public final void rrRequestLayout() {
		parent.rrRequestLayout();
	}

	// Implements the method in RRSingleTouchHandlerProvider, but forces the offset
	public abstract RROffsetClickHandler getClickHandler(float x, float y);

	public final synchronized int setWidth(final int width) {

		if(this.width == width && !unrenderable) return height;

		final int widthMinusPadding = width - paddingLeft - paddingRight;

		if(widthMinusPadding < 0) {
			unrenderable = true;
			return 0;
		}

		final int fixedWidth = getFixedWidth();

		if(fixedWidth != UNSPECIFIED && fixedWidth != widthMinusPadding) {
			throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.WIDTH_IS_FIXED);
		}

		final int heightMinusPadding = onMeasureByWidth(widthMinusPadding);

		height = heightMinusPadding + paddingTop + paddingBottom;
		this.width = width;

		unrenderable = false;

		return height;
	}

	public final synchronized int setHeight(final int height) {

		if(this.height == height && !unrenderable) return width;

		final int fixedHeight = getFixedHeight();

		final int heightMinusPadding = height - paddingTop - paddingBottom;

		if(heightMinusPadding < 0) {
			unrenderable = true;
			return 0;
		}

		if(fixedHeight != UNSPECIFIED && fixedHeight != heightMinusPadding) {
			throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.HEIGHT_IS_FIXED);
		}

		final int widthMinusPadding = onMeasureByHeight(heightMinusPadding);

		width = widthMinusPadding + paddingLeft + paddingRight;
		this.height = height;

		unrenderable = false;

		return width;
	}

	public final int getInnerWidth() {
		if(width < 0) throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.NOT_MEASURED_YET);
		return width - paddingLeft - paddingRight;
	}

	public final int getInnerHeight() {
		if(height < 0) throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.NOT_MEASURED_YET);
		return height - paddingTop - paddingBottom;
	}

	public final int getOuterWidth() {
		if(width < 0) throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.NOT_MEASURED_YET);
		return width;
	}

	public final int getOuterHeight() {
		if(height < 0) throw new MeasurementException(this, MeasurementException.InvalidMeasurementType.NOT_MEASURED_YET);
		return height;
	}

	protected abstract int onMeasureByWidth(int width);
	protected abstract int onMeasureByHeight(int height);

	protected int getFixedWidth() {
		return UNSPECIFIED;
	}

	protected int getFixedHeight() {
		return UNSPECIFIED;
	}

	protected int getMinWidth() {
		return getFixedWidth();
	}

	protected int getMinHeight() {
		return getFixedHeight();
	}

	public void setParent(RRViewParent parent) {
		this.parent = parent;
	}

	public synchronized void setPadding(final int padding) {
		paddingLeft = padding;
		paddingRight = padding;
		paddingTop = padding;
		paddingBottom = padding;
	}
}
