/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BarSeriesRenderer extends SeriesRenderer {
	private static Logger logger = LoggerFactory
			.getLogger(BarSeriesRenderer.class);

	public BarSeriesRenderer(final WCartesianChart chart,
			final WPainter painter, final WDataSeries series,
			final SeriesRenderIterator it, double groupWidth, int numGroups,
			int group) {
		super(chart, painter, series, it);
		this.groupWidth_ = groupWidth;
		this.numGroups_ = numGroups;
		this.group_ = group;
	}

	public void addValue(double x, double y, double stacky,
			final WModelIndex xIndex, final WModelIndex yIndex) {
		WPainterPath bar = new WPainterPath();
		final WAxis yAxis = this.chart_.getAxis(this.series_.getAxis());
		WPointF topMid = this.chart_.map(x, y, yAxis.getId(), this.it_
				.getCurrentXSegment(), this.it_.getCurrentYSegment());
		WPointF bottomMid = this.chart_.map(x, stacky, yAxis.getId(), this.it_
				.getCurrentXSegment(), this.it_.getCurrentYSegment());
		FillRangeType fr = this.series_.getFillRange();
		switch (fr) {
		case MinimumValueFill:
			bottomMid = new WPointF(this.chart_.map(x, stacky, yAxis.getId(),
					this.it_.getCurrentXSegment(),
					this.it_.getCurrentYSegment()).getX(),
					this.chart_.chartArea_.getBottom());
			break;
		case MaximumValueFill:
			bottomMid = new WPointF(this.chart_.map(x, stacky, yAxis.getId(),
					this.it_.getCurrentXSegment(),
					this.it_.getCurrentYSegment()).getX(),
					this.chart_.chartArea_.getTop());
			break;
		default:
			break;
		}
		double g = this.numGroups_ + (this.numGroups_ - 1)
				* this.chart_.getBarMargin();
		double width = this.groupWidth_ / g;
		double left = topMid.getX() - this.groupWidth_ / 2 + this.group_
				* width * (1 + this.chart_.getBarMargin());
		bar.moveTo(this.hv(crisp(left), crisp(topMid.getY())));
		bar.lineTo(this.hv(crisp(left + width), crisp(topMid.getY())));
		bar.lineTo(this.hv(crisp(left + width), crisp(bottomMid.getY())));
		bar.lineTo(this.hv(crisp(left), crisp(bottomMid.getY())));
		bar.closeSubPath();
		this.painter_.setShadow(this.series_.getShadow());
		WBrush brush = this.series_.getBrush().clone();
		SeriesIterator.setBrushColor(brush, xIndex, yIndex,
				ItemDataRole.BarBrushColorRole);
		this.painter_.fillPath(bar, brush);
		this.painter_.setShadow(new WShadow());
		WPen pen = this.series_.getPen().clone();
		SeriesIterator.setPenColor(pen, xIndex, yIndex,
				ItemDataRole.BarPenColorRole);
		this.painter_.strokePath(bar, pen);
		Object toolTip = yIndex.getData(ItemDataRole.ToolTipRole);
		if (!(toolTip == null)) {
			WTransform t = this.painter_.getWorldTransform();
			WPointF tl = t.map(segmentPoint(bar, 0));
			WPointF tr = t.map(segmentPoint(bar, 1));
			WPointF br = t.map(segmentPoint(bar, 2));
			WPointF bl = t.map(segmentPoint(bar, 3));
			double tlx = 0;
			double tly = 0;
			double brx = 0;
			double bry = 0;
			boolean useRect = false;
			if (fequal(tl.getY(), tr.getY())) {
				tlx = Math.min(tl.getX(), tr.getX());
				brx = Math.max(tl.getX(), tr.getX());
				tly = Math.min(tl.getY(), bl.getY());
				bry = Math.max(tl.getY(), br.getY());
				useRect = true;
			} else {
				if (fequal(tl.getX(), tr.getX())) {
					tlx = Math.min(tl.getX(), bl.getX());
					brx = Math.max(tl.getX(), bl.getX());
					tly = Math.min(tl.getY(), tr.getY());
					bry = Math.max(tl.getY(), tr.getY());
					useRect = true;
				}
			}
			WAbstractArea area;
			if (useRect) {
				area = new WRectArea(tlx, tly, brx - tlx, bry - tly);
			} else {
				WPolygonArea poly = new WPolygonArea();
				poly.addPoint(tl.getX(), tl.getY());
				poly.addPoint(tr.getX(), tr.getY());
				poly.addPoint(br.getX(), br.getY());
				poly.addPoint(bl.getX(), bl.getY());
				area = poly;
			}
			area.setToolTip(StringUtils.asString(toolTip));
			this.chart_.addDataPointArea(this.series_, xIndex, area);
		}
		double bTopMidY = this.it_.breakY(topMid.getY());
		double bBottomMidY = this.it_.breakY(bottomMid.getY());
		if (bTopMidY > topMid.getY() && bBottomMidY <= bottomMid.getY()) {
			WPainterPath breakPath = new WPainterPath();
			breakPath.moveTo(this.hv(left - 10, bTopMidY + 10));
			breakPath.lineTo(this.hv(left + width + 10, bTopMidY + 1));
			breakPath.lineTo(this.hv(left + width + 10, bTopMidY - 1));
			breakPath.lineTo(this.hv(left - 10, bTopMidY - 1));
			this.painter_.setPen(new WPen(PenStyle.NoPen));
			this.painter_.setBrush(this.chart_.getBackground());
			this.painter_.drawPath(breakPath);
			this.painter_.setPen(new WPen());
			this.painter_.drawLine(this.hv(left - 10, bTopMidY + 10), this.hv(
					left + width + 10, bTopMidY + 1));
		}
		if (bBottomMidY < bottomMid.getY() && bTopMidY >= topMid.getY()) {
			WPainterPath breakPath = new WPainterPath();
			breakPath.moveTo(this.hv(left + width + 10, bBottomMidY - 10));
			breakPath.lineTo(this.hv(left - 10, bBottomMidY - 1));
			breakPath.lineTo(this.hv(left - 10, bBottomMidY + 1));
			breakPath.lineTo(this.hv(left + width + 10, bBottomMidY + 1));
			this.painter_.setBrush(this.chart_.getBackground());
			this.painter_.setPen(new WPen(PenStyle.NoPen));
			this.painter_.drawPath(breakPath);
			this.painter_.setPen(new WPen());
			this.painter_.drawLine(this.hv(left - 10, bBottomMidY - 1), this
					.hv(left + width + 10, bBottomMidY - 10));
		}
	}

	public void paint() {
	}

	private static WPointF segmentPoint(final WPainterPath path, int segment) {
		final WPainterPath.Segment s = path.getSegments().get(segment);
		return new WPointF(s.getX(), s.getY());
	}

	private static boolean fequal(double d1, double d2) {
		return Math.abs(d1 - d2) < 1E-5;
	}

	private double groupWidth_;
	private int numGroups_;
	private int group_;
}
