package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;
import eu.webtoolkit.jwt.utils.MathUtils;

/**
 * A paint device for rendering using the VML pseudo-standard.
 * <p>
 * 
 * The WVmlImage is used by {@link WPaintedWidget} to render to the browser
 * using the Vector Markup Language (VML) (to support graphics on Internet
 * Explorer browsers).
 * <p>
 * <p>
 * <i><b>Note:</b>The current implementation has only limited support for
 * clipping: only rectangular areas aligned with the X/Y axes can be used as
 * clipping path. </i>
 * </p>
 */
public class WVmlImage implements WVectorImage {
	/**
	 * Create a VML paint device.
	 */
	public WVmlImage(WLength width, WLength height) {
		super();
		this.width_ = width;
		this.height_ = height;
		this.painter_ = null;
		this.paintFlags_ = EnumSet.noneOf(PaintFlag.class);
		this.clippingChanged_ = false;
		this.currentBrush_ = new WBrush();
		this.currentPen_ = new WPen();
		this.activePaths_ = new ArrayList<WVmlImage.ActivePath>();
		this.rendered_ = "";
		this.currentRect_ = new WRectF();
	}

	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags) {
		if (!EnumUtils.mask(
				flags,
				EnumSet.of(WPaintDevice.ChangeFlag.Pen,
						WPaintDevice.ChangeFlag.Brush)).isEmpty()) {
			this.penBrushChanged_ = true;
		}
		if (!EnumUtils.mask(flags, WPaintDevice.ChangeFlag.Clipping).isEmpty()) {
			this.clippingChanged_ = true;
		}
	}

	public final void setChanged(WPaintDevice.ChangeFlag flag,
			WPaintDevice.ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		this.getPainter().save();
		this.getPainter().translate(rect.getCenter().getX(),
				rect.getCenter().getY());
		this.getPainter().scale(1., rect.getHeight() / rect.getWidth());
		WPainterPath path = new WPainterPath();
		path.arcMoveTo(0, 0, rect.getWidth() / 2., startAngle);
		path.arcTo(0, 0, rect.getWidth() / 2., startAngle, spanAngle);
		this.getPainter().drawPath(path);
		this.getPainter().restore();
	}

	public void drawImage(WRectF rect, String imgUri, int imgWidth,
			int imgHeight, WRectF sourceRect) {
		this.finishPaths();
		this.processClipping();
		StringWriter tmp = new StringWriter();
		WTransform t = this.getPainter().getCombinedTransform();
		WPointF tl = t.map(rect.getTopLeft());
		tmp.append("<v:group style=\"width:").append(
				String.valueOf(Z * this.getWidth().getValue())).append(
				"px;height:").append(
				String.valueOf(Z * this.getHeight().getValue())).append("px;");
		double cx = 1;
		double cy = 1;
		if (t.getM11() != 1.0 || t.getM22() != 1.0 || t.getM12() != 0.0
				|| t.getM21() != 0.0) {
			cx = this.getWidth().getValue() / rect.getWidth();
			cy = this.getHeight().getValue() / rect.getHeight();
			tmp.append("filter:progid:DXImageTransform.Microsoft.Matrix(M11='")
					.append(String.valueOf(t.getM11() / cx)).append("',M12='")
					.append(String.valueOf(t.getM21() / cy)).append("',M21='")
					.append(String.valueOf(t.getM12() / cx)).append("',M22='")
					.append(String.valueOf(t.getM22() / cy)).append("',Dx='")
					.append(String.valueOf(tl.getX())).append("',Dy='").append(
							String.valueOf(tl.getY())).append(
							"',sizingmethod='clip');");
		} else {
			tmp.append("top:").append(String.valueOf(Z * tl.getY())).append(
					"px;left:").append(String.valueOf(Z * tl.getX())).append(
					"px;");
		}
		tmp.append("\"><v:image src=\"").append(imgUri).append(
				"\" style=\"width:").append(
				String.valueOf(Z * rect.getWidth() * cx)).append("px;height:")
				.append(String.valueOf(Z * rect.getHeight() * cy)).append(
						"px\" cropleft=\"").append(
						String.valueOf(sourceRect.getX() / imgWidth)).append(
						"\" croptop=\"").append(
						String.valueOf(sourceRect.getY() / imgHeight)).append(
						"\" cropright=\"").append(
						String.valueOf((imgWidth - sourceRect.getRight())
								/ imgWidth)).append("\" cropbottom=\"").append(
						String.valueOf((imgHeight - sourceRect.getBottom())
								/ imgHeight)).append("\"/></v:group>");
		this.rendered_ += tmp.toString();
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		WPainterPath path = new WPainterPath();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		WBrush oldBrush = this.getPainter().getBrush();
		this.getPainter().setBrush(new WBrush());
		this.drawPath(path);
		this.getPainter().setBrush(oldBrush);
	}

	public void drawPath(WPainterPath path) {
		if (this.penBrushChanged_) {
			if (!this.currentPen_.equals(this.getPainter().getPen())
					|| !this.currentBrush_.equals(this.getPainter().getBrush())) {
				this.finishPaths();
			}
		}
		if (this.clippingChanged_) {
			if (!this.activePaths_.isEmpty()) {
				this.finishPaths();
			}
			this.processClipping();
		}
		WTransform transform = this.getPainter().getCombinedTransform();
		WRectF bbox = transformBbox(transform, path.getControlPointRect());
		int thisPath = -1;
		if (!this.activePaths_.isEmpty()) {
			for (int i = 0; i < this.activePaths_.size(); ++i) {
				if (!this.activePaths_.get(i).bbox.intersects(bbox)) {
					thisPath = i;
					break;
				}
			}
		}
		if (this.activePaths_.isEmpty()) {
			this.currentPen_ = this.getPainter().getPen();
			this.currentBrush_ = this.getPainter().getBrush();
			this.penBrushChanged_ = false;
		}
		StringWriter tmp = new StringWriter();
		List<WPainterPath.Segment> segments = path.getSegments();
		if (thisPath == -1) {
			tmp.append("<v:shape style=\"width:").append(
					String.valueOf(Z * this.currentRect_.getWidth())).append(
					"px;height:").append(
					String.valueOf(Z * this.currentRect_.getHeight())).append(
					"px;\" path=\"m0,0l0,0");
			this.activePaths_.add(new WVmlImage.ActivePath());
			thisPath = this.activePaths_.size() - 1;
		}
		if (segments.size() > 0
				&& segments.get(0).getType() != WPainterPath.Segment.Type.MoveTo) {
			tmp.append("m0,0");
		}
		for (int i = 0; i < segments.size(); ++i) {
			final WPainterPath.Segment s = segments.get(i);
			if (i == segments.size() - 1
					&& s.getType() == WPainterPath.Segment.Type.MoveTo) {
				break;
			}
			double x = s.getX();
			double y = s.getY();
			if (s.getType() == WPainterPath.Segment.Type.ArcC) {
				double cx = segments.get(i).getX();
				double cy = segments.get(i).getY();
				double rx = segments.get(i + 1).getX();
				double ry = segments.get(i + 1).getY();
				double theta1 = -WTransform.degreesToRadians(segments
						.get(i + 2).getX());
				double deltaTheta = -WTransform.degreesToRadians(segments.get(
						i + 2).getY());
				i += 2;
				WPointF c = transform.map(new WPointF(cx, cy));
				WPointF p1 = new WPointF(rx * Math.cos(theta1) + cx, ry
						* Math.sin(theta1) + cy);
				WPointF p2 = new WPointF(rx * Math.cos(theta1 + deltaTheta)
						+ cx, ry * Math.sin(theta1 + deltaTheta) + cy);
				rx *= norm(new WPointF(transform.getM11(), transform.getM12()));
				ry *= norm(new WPointF(transform.getM21(), transform.getM22()));
				WPointF a = new WPointF(c.getX() - rx, c.getY() - ry);
				WPointF b = new WPointF(c.getX() + rx, c.getY() + ry);
				p1 = transform.map(p1);
				p2 = transform.map(p2);
				tmp.append(deltaTheta < 0 ? "at" : "wa").append(
						String.valueOf(myzround(a.getX()))).append(",").append(
						String.valueOf(myzround(a.getY()))).append(",").append(
						String.valueOf(myzround(b.getX()))).append(",").append(
						String.valueOf(myzround(b.getY()))).append(",").append(
						String.valueOf(myzround(p1.getX()))).append(",")
						.append(String.valueOf(myzround(p1.getY())))
						.append(",")
						.append(String.valueOf(myzround(p2.getX())))
						.append(",")
						.append(String.valueOf(myzround(p2.getY())));
			} else {
				switch (s.getType()) {
				case MoveTo:
					tmp.append("m");
					break;
				case LineTo:
					tmp.append("l");
					break;
				case CubicC1:
					tmp.append("c");
					break;
				case CubicC2:
				case CubicEnd:
					tmp.append(",");
					break;
				case QuadC: {
					WPointF current = path.getPositionAtSegment(i);
					final double cpx = s.getX();
					final double cpy = s.getY();
					final double xend = segments.get(i + 1).getX();
					final double yend = segments.get(i + 1).getY();
					final double cp1x = current.getX() + 2.0 / 3.0
							* (cpx - current.getX());
					final double cp1y = current.getY() + 2.0 / 3.0
							* (cpy - current.getY());
					final double cp2x = cp1x + (xend - current.getX()) / 3.0;
					final double cp2y = cp1y + (yend - current.getY()) / 3.0;
					WPointF cp1 = new WPointF(cp1x, cp1y);
					cp1 = transform.map(cp1);
					tmp.append("c")
							.append(String.valueOf(myzround(cp1.getX())))
							.append(",").append(
									String.valueOf(myzround(cp1.getY())))
							.append(",");
					x = cp2x;
					y = cp2y;
					break;
				}
				case QuadEnd:
					tmp.append(",");
					break;
				default:
					assert false;
				}
				WPointF p = new WPointF(x, y);
				p = transform.map(p);
				tmp.append(String.valueOf(myzround(p.getX()))).append(",")
						.append(String.valueOf(myzround(p.getY())));
			}
		}
		this.activePaths_.get(thisPath).path += tmp.toString();
		this.activePaths_.get(thisPath).bbox = this.activePaths_.get(thisPath).bbox
				.united(bbox);
	}

	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags,
			CharSequence text) {
		this.finishPaths();
		EnumSet<AlignmentFlag> horizontalAlign = EnumUtils.mask(flags,
				AlignmentFlag.AlignHorizontalMask);
		EnumSet<AlignmentFlag> verticalAlign = EnumUtils.mask(flags,
				AlignmentFlag.AlignVerticalMask);
		StringWriter tmp = new StringWriter();
		double fontSize;
		switch (this.getPainter().getFont().getSize()) {
		case FixedSize:
			fontSize = this.getPainter().getFont().getFixedSize().toPixels();
			break;
		default:
			fontSize = 16;
		}
		double y = rect.getCenter().getY();
		switch (EnumUtils.enumFromSet(verticalAlign)) {
		case AlignTop:
			y = rect.getTop() + fontSize * 0.55;
			break;
		case AlignMiddle:
			y = rect.getCenter().getY();
			break;
		case AlignBottom:
			y = rect.getBottom() - fontSize * 0.45;
			break;
		default:
			break;
		}
		tmp.append("<v:shape style=\"width:").append(
				String.valueOf(Z * this.currentRect_.getWidth())).append(
				"px;height:").append(
				String.valueOf(Z * this.currentRect_.getHeight())).append(
				"px;\"><v:path textpathok=\"True\" v=\"m").append(
				String.valueOf(myzround(rect.getLeft(), false))).append(',')
				.append(String.valueOf(myzround(y, false))).append('l').append(
						String.valueOf(myzround(rect.getRight(), false)))
				.append(',').append(String.valueOf(myzround(y, false))).append(
						"m0,0l0,0e\"/><v:fill on=\"True\" ").append(
						colorAttributes(this.getPainter().getPen().getColor()))
				.append("/><v:stroke on=\"False\"/>").append(
						this.skewElement(this.getPainter()
								.getCombinedTransform())).append(
						"<v:textpath on=\"True\" string=\"");
		{
			EscapeOStream attr = new EscapeOStream(tmp);
			attr.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
			attr.append(text.toString());
		}
		tmp.append("\" style=\"v-text-align:");
		switch (EnumUtils.enumFromSet(horizontalAlign)) {
		case AlignLeft:
			tmp.append("left");
			break;
		case AlignCenter:
			tmp.append("center");
			break;
		case AlignRight:
			tmp.append("right");
			break;
		default:
			break;
		}
		WApplication app = WApplication.getInstance();
		WFont textFont = this.getPainter().getFont();
		textFont.setSize(textFont.getSize(), WLength.multiply(textFont
				.getFixedSize(), app.getEnvironment().getDpiScale()));
		tmp.append(';').append(textFont.getCssText()).append("\"/></v:shape>");
		this.rendered_ += tmp.toString();
	}

	public void init() {
		this.currentBrush_ = this.getPainter().getBrush();
		this.currentPen_ = this.getPainter().getPen();
		this.penBrushChanged_ = true;
		this.startClip(new WRectF(0, 0, this.getWidth().getValue(), this
				.getHeight().getValue()));
	}

	public void done() {
		this.finishPaths();
	}

	public boolean isPaintActive() {
		return this.painter_ != null;
	}

	public String getRendered() {
		this.stopClip();
		if (!EnumUtils.mask(this.paintFlags_, PaintFlag.PaintUpdate).isEmpty()) {
			return this.rendered_;
		} else {
			StringWriter s = new StringWriter();
			s.append("<div style=\"position:relative;width:").append(
					this.getWidth().getCssText()).append(";height:").append(
					this.getHeight().getCssText()).append(
					";overflow:hidden;\">").append(this.rendered_).append(
					"</div>");
			return s.toString();
		}
	}

	public WLength getWidth() {
		return this.width_;
	}

	public WLength getHeight() {
		return this.height_;
	}

	public EnumSet<PaintFlag> getPaintFlags() {
		return this.paintFlags_;
	}

	public WPainter getPainter() {
		return this.painter_;
	}

	public void setPainter(WPainter painter) {
		this.painter_ = painter;
	}

	public void setPaintFlags(EnumSet<PaintFlag> paintFlags) {
		this.paintFlags_.clear();
	}

	public final void setPaintFlags(PaintFlag paintFlag,
			PaintFlag... paintFlags) {
		setPaintFlags(EnumSet.of(paintFlag, paintFlags));
	}

	private WLength width_;
	private WLength height_;
	private WPainter painter_;
	private EnumSet<PaintFlag> paintFlags_;
	private boolean penBrushChanged_;
	private boolean clippingChanged_;
	private WBrush currentBrush_;
	private WPen currentPen_;

	private static class ActivePath {
		public String path;
		public WRectF bbox;

		public ActivePath() {
			this.path = "";
			this.bbox = new WRectF();
		}
	}

	private List<WVmlImage.ActivePath> activePaths_;
	private String rendered_;

	private void finishPaths() {
		for (int i = 0; i < this.activePaths_.size(); ++i) {
			this.rendered_ += this.activePaths_.get(i).path + "e\">"
					+ this.strokeElement(this.currentPen_)
					+ this.fillElement(this.currentBrush_) + "</v:shape>";
		}
		this.activePaths_.clear();
	}

	private void processClipping() {
		if (this.clippingChanged_) {
			if (this.getPainter().hasClipping()) {
				WRectF rect = new WRectF();
				if (this.getPainter().getClipPath().asRect(rect)) {
					WTransform t = this.getPainter().getClipPathTransform();
					WPointF tl = t.map(rect.getTopLeft());
					WPointF tr = t.map(rect.getTopRight());
					WPointF bl = t.map(rect.getBottomLeft());
					WPointF br = t.map(rect.getBottomRight());
					double tlx = 0;
					double tly = 0;
					double brx = 0;
					double bry = 0;
					boolean ok = false;
					if (fequal(tl.getY(), tr.getY())) {
						tlx = Math.min(tl.getX(), tr.getX());
						brx = Math.max(tl.getX(), tr.getX());
						tly = Math.min(tl.getY(), bl.getY());
						bry = Math.max(tl.getY(), br.getY());
						ok = true;
					} else {
						if (fequal(tl.getX(), tr.getX())) {
							tlx = Math.min(tl.getX(), bl.getX());
							brx = Math.max(tl.getX(), bl.getX());
							tly = Math.min(tl.getY(), tr.getY());
							bry = Math.max(tl.getY(), tr.getY());
							ok = true;
						}
					}
					if (ok) {
						this.stopClip();
						this.startClip(new WRectF(tlx, tly, brx - tlx, bry
								- tly));
					} else {
						WApplication
								.getInstance()
								.log("warn")
								.append(
										"VML only supports rectangle clipping with rectangles aligned to the window");
					}
				} else {
					WApplication.getInstance().log("warn").append(
							"VML only supports rectangle clipping");
				}
			} else {
				this.stopClip();
				this.startClip(new WRectF(0, 0, this.getWidth().getValue(),
						this.getHeight().getValue()));
			}
			this.clippingChanged_ = false;
		}
	}

	private String fillElement(WBrush brush) {
		if (brush.getStyle() != WBrushStyle.NoBrush) {
			return "<v:fill " + colorAttributes(brush.getColor()) + "/>";
		} else {
			return "<v:fill on=\"false\" />";
		}
	}

	private String strokeElement(WPen pen) {
		if (pen.getStyle() != PenStyle.NoPen) {
			String result = "";
			result = "<v:stroke " + colorAttributes(pen.getColor());
			switch (pen.getCapStyle()) {
			case FlatCap:
				result += " endcap=\"flat\"";
				break;
			case SquareCap:
				result += " endcap=\"square\"";
				break;
			case RoundCap:
				break;
			}
			switch (pen.getJoinStyle()) {
			case MiterJoin:
				result += " joinstyle=\"miter\"";
				break;
			case BevelJoin:
				result += " joinstyle=\"bevel\"";
				break;
			case RoundJoin:
				break;
			}
			switch (pen.getStyle()) {
			case NoPen:
				break;
			case SolidLine:
				break;
			case DashLine:
				result += " dashstyle=\"dash\"";
				break;
			case DotLine:
				result += " dashstyle=\"dot\"";
				break;
			case DashDotLine:
				result += " dashstyle=\"dashdot\"";
				break;
			case DashDotDotLine:
				result += " dashstyle=\"2 2 0 2 0 2\"";
				break;
			}
			WLength w = this.getPainter().normalizedPenWidth(pen.getWidth(),
					false);
			if (!w.equals(new WLength(1))) {
				result += " weight=" + quote(w.getCssText());
			}
			return result + "/>";
		} else {
			return "<v:stroke on=\"false\" />";
		}
	}

	private String skewElement(WTransform t) {
		if (!t.isIdentity()) {
			char[] buf = new char[30];
			StringWriter s = new StringWriter();
			s.append("<v:skew on=\"true\" matrix=\"").append(
					MathUtils.round(t.getM11(), 5)).append(',');
			s.append(MathUtils.round(t.getM21(), 5)).append(',');
			s.append(MathUtils.round(t.getM12(), 5)).append(',');
			s.append(MathUtils.round(t.getM22(), 5)).append(
					",0,0\" origin=\"-0.5 -0.5\" offset=\"");
			s
					.append(
							MathUtils.round(t.getDx() + Math.abs(t.getM11())
									* 0.5, 5)).append("px,");
			s
					.append(
							MathUtils.round(t.getDy() + Math.abs(t.getM22())
									* 0.5, 5)).append("px\"/>");
			return s.toString();
		} else {
			return "";
		}
	}

	private static String colorAttributes(WColor color) {
		String result = " color=" + quote(color.getCssText());
		if (color.getAlpha() != 255) {
			result += " opacity=" + quote(color.getAlpha() / 255.);
		}
		return result;
	}

	private static String quote(double d) {
		char[] buf = new char[30];
		return quote(MathUtils.round(d, 5));
	}

	private static String quote(String s) {
		return '"' + s + '"';
	}

	private void startClip(WRectF rect) {
		StringWriter s = new StringWriter();
		s.append("<div style=\"position:absolute;left:").append(
				String.valueOf(rect.getLeft())).append("px;top:").append(
				String.valueOf(rect.getTop())).append("px;width:").append(
				String.valueOf(rect.getWidth())).append("px;height:").append(
				String.valueOf(rect.getHeight())).append(
				"px;overflow:hidden;\"").append(
				" onselectstart=\"return false;\">").append(
				"<v:group style=\"position:absolute;left:0px;top:0px;width:")
				.append(String.valueOf(rect.getWidth())).append("px;height:")
				.append(String.valueOf(rect.getHeight())).append(
						"px\" coordorigin=\"").append(
						String.valueOf(0.5 * rect.getLeft() * Z)).append(",")
				.append(String.valueOf(0.5 * rect.getTop() * Z)).append(
						"\" coordsize=\"").append(
						String.valueOf(rect.getWidth() * Z)).append(",")
				.append(String.valueOf(rect.getHeight() * Z)).append("\">");
		this.rendered_ += s.toString();
		this.currentRect_ = rect;
	}

	private void stopClip() {
		this.rendered_ += "</v:group></div>";
	}

	private WRectF currentRect_;
	static final int Z = 10;

	static int myzround(double a, boolean doScale) {
		WApplication app = WApplication.getInstance();
		double dpiScale = doScale ? app.getEnvironment().getDpiScale() : 1.0;
		return (int) (dpiScale * (Z * a - Z / 2 + 0.5));
	}

	private static final int myzround(double a) {
		return myzround(a, true);
	}

	static boolean fequal(double d1, double d2) {
		return Math.abs(d1 - d2) < 1E-5;
	}

	static double norm(WPointF p) {
		return Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
	}

	static WRectF transformBbox(WTransform t, WRectF r) {
		double minX;
		double minY;
		double maxX;
		double maxY;
		WPointF p = t.map(r.getTopLeft());
		minX = maxX = p.getX();
		minY = maxY = p.getY();
		for (int i = 0; i < 3; ++i) {
			WPointF p2 = t.map(i == 0 ? r.getBottomLeft() : i == 1 ? r
					.getTopRight() : r.getBottomRight());
			minX = Math.min(minX, p2.getX());
			maxX = Math.max(maxX, p2.getX());
			minY = Math.min(minY, p2.getY());
			maxY = Math.max(maxY, p2.getY());
		}
		return new WRectF(minX, minY, maxX - minX, maxY - minY);
	}
}
