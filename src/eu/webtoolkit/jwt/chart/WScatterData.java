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

/**
 * Class representing a collection of points for on a 3D chart.
 * <p>
 * 
 * General information can be found at {@link WAbstractDataSeries3D}. The model
 * should be structured as a table where every row represents a point. In the
 * simplest case, there are three columns representing the x-, y- and z-values.
 * By default, this is column 0 for X, column 1 for Y and column 2 for Z. It is
 * also possible to provide an additional column containing information on the
 * color for each point. The same is possible for the size. Color-information in
 * the model should be present as a {@link WColor}.
 * <p>
 * If these extra columns are not included, the MarkerBrushColorRole and
 * MarkerScaleFactorRole can still be used to style individual points. These
 * dataroles should be set on the values in the column containing the z-values.
 * <p>
 * The figure below shows an upward spiral of points, with droplines enabled and
 * a pointsize of 5 pixels.
 * <p>
 * <div align="center"> <img src="doc-files//spiral.png"
 * alt="An example of WScatterData">
 * <p>
 * <strong>An example of WScatterData</strong>
 * </p>
 * </div>
 */
public class WScatterData extends WAbstractDataSeries3D {
	private static Logger logger = LoggerFactory.getLogger(WScatterData.class);

	/**
	 * Constructor.
	 */
	public WScatterData(WAbstractItemModel model) {
		super(model);
		this.XSeriesColumn_ = 0;
		this.YSeriesColumn_ = 1;
		this.ZSeriesColumn_ = 2;
		this.colorColumn_ = -1;
		this.asColorRole_ = ItemDataRole.MarkerPenColorRole;
		this.asSizeRole_ = ItemDataRole.MarkerScaleFactorRole;
		this.sizeColumn_ = -1;
		this.droplinesEnabled_ = false;
		this.droplinesPen_ = new WPen();
		this.xRangeCached_ = false;
		this.yRangeCached_ = false;
		this.vertexPosBuffer_ = new WGLWidget.Buffer();
		this.vertexSizeBuffer_ = new WGLWidget.Buffer();
		this.vertexPosBuffer2_ = new WGLWidget.Buffer();
		this.vertexSizeBuffer2_ = new WGLWidget.Buffer();
		this.vertexColorBuffer2_ = new WGLWidget.Buffer();
		this.lineVertBuffer_ = new WGLWidget.Buffer();
		this.colormapTexture_ = new WGLWidget.Texture();
		this.vertexShader_ = new WGLWidget.Shader();
		this.colVertexShader_ = new WGLWidget.Shader();
		this.linesVertShader_ = new WGLWidget.Shader();
		this.fragmentShader_ = new WGLWidget.Shader();
		this.colFragmentShader_ = new WGLWidget.Shader();
		this.linesFragShader_ = new WGLWidget.Shader();
		this.shaderProgram_ = new WGLWidget.Program();
		this.colShaderProgram_ = new WGLWidget.Program();
		this.linesProgram_ = new WGLWidget.Program();
		this.posAttr_ = new WGLWidget.AttribLocation();
		this.posAttr2_ = new WGLWidget.AttribLocation();
		this.posAttrLines_ = new WGLWidget.AttribLocation();
		this.sizeAttr_ = new WGLWidget.AttribLocation();
		this.sizeAttr2_ = new WGLWidget.AttribLocation();
		this.colorAttr2_ = new WGLWidget.AttribLocation();
		this.mvMatrixUniform_ = new WGLWidget.UniformLocation();
		this.mvMatrixUniform2_ = new WGLWidget.UniformLocation();
		this.mvMatrixUniform3_ = new WGLWidget.UniformLocation();
		this.pMatrixUniform_ = new WGLWidget.UniformLocation();
		this.pMatrixUniform2_ = new WGLWidget.UniformLocation();
		this.pMatrixUniform3_ = new WGLWidget.UniformLocation();
		this.cMatrixUniform_ = new WGLWidget.UniformLocation();
		this.cMatrixUniform2_ = new WGLWidget.UniformLocation();
		this.cMatrixUniform3_ = new WGLWidget.UniformLocation();
		this.lineColorUniform_ = new WGLWidget.UniformLocation();
		this.samplerUniform_ = new WGLWidget.UniformLocation();
		this.offsetUniform_ = new WGLWidget.UniformLocation();
		this.scaleFactorUniform_ = new WGLWidget.UniformLocation();
	}

	/**
	 * Enables or disables droplines for all points.
	 * <p>
	 * Enabling droplines will cause a line to be drawn from every point to the
	 * the ground-plane of the chart&apos;s plotcube. By default the droplines
	 * are disabled.
	 * <p>
	 * 
	 * @see WScatterData#setDroplinesPen(WPen pen)
	 */
	public void setDroplinesEnabled(boolean enabled) {
		if (this.droplinesEnabled_ != enabled) {
			this.droplinesEnabled_ = enabled;
			if (this.chart_ != null) {
				this.chart_.updateChart(EnumSet
						.of(WCartesian3DChart.ChartUpdates.GLContext));
			}
		}
	}

	/**
	 * Enables or disables droplines for all points.
	 * <p>
	 * Calls {@link #setDroplinesEnabled(boolean enabled)
	 * setDroplinesEnabled(true)}
	 */
	public final void setDroplinesEnabled() {
		setDroplinesEnabled(true);
	}

	/**
	 * Returns whether droplines are enabled.
	 * <p>
	 * 
	 * @see WScatterData#setDroplinesEnabled(boolean enabled)
	 * @see WScatterData#setDroplinesPen(WPen pen)
	 */
	public boolean isDroplinesEnabled() {
		return this.droplinesEnabled_;
	}

	/**
	 * Sets the pen that is used to draw droplines.
	 * <p>
	 * The default pen is a default constructed {@link WPen}.
	 * <p>
	 * Note: only the width and color of the pen are used.
	 * <p>
	 * 
	 * @see WScatterData#setDroplinesEnabled(boolean enabled)
	 */
	public void setDroplinesPen(final WPen pen) {
		this.droplinesPen_ = pen;
		if (this.chart_ != null) {
			this.chart_.updateChart(EnumSet
					.of(WCartesian3DChart.ChartUpdates.GLContext));
		}
	}

	/**
	 * Returns the pen that is used to draw droplines.
	 * <p>
	 * 
	 * @see WScatterData#setDroplinesEnabled(boolean enabled)
	 * @see WScatterData#setDroplinesPen(WPen pen)
	 */
	public WPen getDroplinesPen() {
		return this.droplinesPen_;
	}

	/**
	 * Sets the column-index from the model that is used for the x-coordinate of
	 * all points.
	 * <p>
	 * The default X column index is 0.
	 */
	public void setXSeriesColumn(int columnNumber) {
		this.XSeriesColumn_ = columnNumber;
	}

	/**
	 * Returns the column-index from the model that is used for the x-coordinate
	 * of all points.
	 * <p>
	 * 
	 * @see WScatterData#setXSeriesColumn(int columnNumber)
	 */
	public int XSeriesColumn() {
		return this.XSeriesColumn_;
	}

	/**
	 * Sets the column-index from the model that is used for the y-coordinate of
	 * all points.
	 * <p>
	 * The default X column index is 1.
	 */
	public void setYSeriesColumn(int columnNumber) {
		this.YSeriesColumn_ = columnNumber;
	}

	/**
	 * Returns the column-index from the model that is used for the y-coordinate
	 * of all points.
	 * <p>
	 * 
	 * @see WScatterData#setYSeriesColumn(int columnNumber)
	 */
	public int YSeriesColumn() {
		return this.YSeriesColumn_;
	}

	/**
	 * Sets the column-index from the model that is used for the z-coordinate of
	 * all points.
	 * <p>
	 * The default Z column index is 2.
	 * <p>
	 * Note that this column is also used to check for a MarkerBrushColorRole
	 * and a MarkerScaleFactorRole is no color-column or size-column are set.
	 * <p>
	 */
	public void setZSeriesColumn(int columnNumber) {
		this.ZSeriesColumn_ = columnNumber;
	}

	/**
	 * Returns the column-index from the model that is used for the z-coordinate
	 * of all points.
	 * <p>
	 * 
	 * @see WScatterData#setZSeriesColumn(int columnNumber)
	 */
	public int ZSeriesColumn() {
		return this.ZSeriesColumn_;
	}

	public void setColorColumn(int columnNumber, int role) {
		this.colorColumn_ = columnNumber;
		this.asColorRole_ = role;
	}

	public final void setColorColumn(int columnNumber) {
		setColorColumn(columnNumber, ItemDataRole.DisplayRole);
	}

	public void setSizeColumn(int columnNumber, int role) {
		this.sizeColumn_ = columnNumber;
		this.asSizeRole_ = role;
	}

	public final void setSizeColumn(int columnNumber) {
		setSizeColumn(columnNumber, ItemDataRole.DisplayRole);
	}

	public double minimum(Axis axis) {
		if (axis == Axis.XAxis_3D) {
			if (!this.xRangeCached_) {
				this.findXRange();
			}
			return this.xMin_;
		} else {
			if (axis == Axis.YAxis_3D) {
				if (!this.yRangeCached_) {
					this.findYRange();
				}
				return this.yMin_;
			} else {
				if (axis == Axis.ZAxis_3D) {
					if (!this.rangeCached_) {
						this.findZRange();
					}
					return this.zMin_;
				} else {
					throw new WException("WScatterData: unknown Axis-type");
				}
			}
		}
	}

	public double maximum(Axis axis) {
		if (axis == Axis.XAxis_3D) {
			if (!this.xRangeCached_) {
				this.findXRange();
			}
			return this.xMax_;
		} else {
			if (axis == Axis.YAxis_3D) {
				if (!this.yRangeCached_) {
					this.findYRange();
				}
				return this.yMax_;
			} else {
				if (axis == Axis.ZAxis_3D) {
					if (!this.rangeCached_) {
						this.findZRange();
					}
					return this.zMax_;
				} else {
					throw new WException("WScatterData: unknown Axis-type");
				}
			}
		}
	}

	public void initializeGL() {
	}

	public void paintGL() {
		if (this.hidden_) {
			return;
		}
		this.chart_.disable(WGLWidget.GLenum.CULL_FACE);
		this.chart_.enable(WGLWidget.GLenum.DEPTH_TEST);
		if (!this.vertexPosBuffer_.isNull()) {
			this.chart_.useProgram(this.shaderProgram_);
			this.chart_.uniformMatrix4(this.cMatrixUniform_, this.chart_
					.getJsMatrix());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffer_);
			this.chart_.vertexAttribPointer(this.posAttr_, 3,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.posAttr_);
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
						this.vertexSizeBuffer_);
				this.chart_.vertexAttribPointer(this.sizeAttr_, 1,
						WGLWidget.GLenum.FLOAT, false, 0, 0);
				this.chart_.enableVertexAttribArray(this.sizeAttr_);
			}
			this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE0);
			this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D,
					this.colormapTexture_);
			this.chart_.uniform1i(this.samplerUniform_, 0);
			this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0,
					this.vertexBufferSize_ / 3);
			this.chart_.disableVertexAttribArray(this.posAttr_);
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.chart_.disableVertexAttribArray(this.sizeAttr_);
			}
		}
		if (!this.vertexPosBuffer2_.isNull()) {
			this.chart_.useProgram(this.colShaderProgram_);
			this.chart_.uniformMatrix4(this.cMatrixUniform2_, this.chart_
					.getJsMatrix());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffer2_);
			this.chart_.vertexAttribPointer(this.posAttr2_, 3,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.posAttr2_);
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
						this.vertexSizeBuffer2_);
				this.chart_.vertexAttribPointer(this.sizeAttr2_, 1,
						WGLWidget.GLenum.FLOAT, false, 0, 0);
				this.chart_.enableVertexAttribArray(this.sizeAttr2_);
			}
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexColorBuffer2_);
			this.chart_.vertexAttribPointer(this.colorAttr2_, 4,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.colorAttr2_);
			this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0,
					this.vertexBuffer2Size_ / 3);
			this.chart_.disableVertexAttribArray(this.posAttr2_);
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.chart_.disableVertexAttribArray(this.sizeAttr2_);
			}
			this.chart_.disableVertexAttribArray(this.colorAttr2_);
		}
		if (this.droplinesEnabled_) {
			this.chart_.useProgram(this.linesProgram_);
			this.chart_.uniformMatrix4(this.cMatrixUniform3_, this.chart_
					.getJsMatrix());
			this.chart_.uniform4f(this.lineColorUniform_,
					(float) this.droplinesPen_.getColor().getRed(),
					(float) this.droplinesPen_.getColor().getGreen(),
					(float) this.droplinesPen_.getColor().getBlue(),
					(float) this.droplinesPen_.getColor().getAlpha());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.lineVertBuffer_);
			this.chart_.vertexAttribPointer(this.posAttrLines_, 3,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.posAttrLines_);
			this.chart_
					.lineWidth(this.droplinesPen_.getWidth().getValue() == 0 ? 1.0
							: this.droplinesPen_.getWidth().getValue());
			this.chart_.drawArrays(WGLWidget.GLenum.LINES, 0,
					this.lineVertBufferSize_ / 3);
			this.chart_.disableVertexAttribArray(this.posAttrLines_);
		}
		this.chart_.enable(WGLWidget.GLenum.CULL_FACE);
		this.chart_.disable(WGLWidget.GLenum.DEPTH_TEST);
	}

	public void updateGL() {
		int N = this.model_.getRowCount();
		int cnt = this.getCountSimpleData();
		java.nio.ByteBuffer simplePtsArray = WebGLUtils
				.newByteBuffer(4 * (3 * cnt));
		java.nio.ByteBuffer simplePtsSize = WebGLUtils.newByteBuffer(4 * (cnt));
		java.nio.ByteBuffer coloredPtsArray = WebGLUtils
				.newByteBuffer(4 * (3 * (N - cnt)));
		java.nio.ByteBuffer coloredPtsSize = WebGLUtils
				.newByteBuffer(4 * (N - cnt));
		java.nio.ByteBuffer coloredPtsColor = WebGLUtils
				.newByteBuffer(4 * (4 * (N - cnt)));
		this.dataFromModel(simplePtsArray, simplePtsSize, coloredPtsArray,
				coloredPtsSize, coloredPtsColor);
		if (simplePtsArray.capacity() / 4 != 0) {
			this.vertexPosBuffer_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffer_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					simplePtsArray, WGLWidget.GLenum.STATIC_DRAW, true);
			this.vertexBufferSize_ = simplePtsArray.capacity() / 4;
			this.vertexSizeBuffer_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexSizeBuffer_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					simplePtsSize, WGLWidget.GLenum.STATIC_DRAW, true);
		}
		if (coloredPtsArray.capacity() / 4 != 0) {
			this.vertexPosBuffer2_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffer2_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					coloredPtsArray, WGLWidget.GLenum.STATIC_DRAW, true);
			this.vertexBuffer2Size_ = coloredPtsArray.capacity() / 4;
			this.vertexSizeBuffer2_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexSizeBuffer2_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					coloredPtsSize, WGLWidget.GLenum.STATIC_DRAW, true);
			this.vertexColorBuffer2_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexColorBuffer2_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					coloredPtsColor, WGLWidget.GLenum.STATIC_DRAW, true);
		}
		if (this.droplinesEnabled_) {
			java.nio.ByteBuffer dropLineVerts = WebGLUtils
					.newByteBuffer(4 * (2 * 3 * N));
			this.dropLineVertices(simplePtsArray, dropLineVerts);
			this.dropLineVertices(coloredPtsArray, dropLineVerts);
			this.lineVertBuffer_ = this.chart_.createBuffer();
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.lineVertBuffer_);
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					dropLineVerts, WGLWidget.GLenum.STATIC_DRAW, true);
			this.lineVertBufferSize_ = dropLineVerts.capacity() / 4;
		}
		this.colormapTexture_ = this.getColorTexture();
		this.chart_.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
				WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
		this.chart_.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
				WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
		this.chart_
				.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
						WGLWidget.GLenum.TEXTURE_WRAP_S,
						WGLWidget.GLenum.CLAMP_TO_EDGE);
		this.chart_
				.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
						WGLWidget.GLenum.TEXTURE_WRAP_T,
						WGLWidget.GLenum.CLAMP_TO_EDGE);
		this.initShaders();
		this.chart_.useProgram(this.shaderProgram_);
		this.chart_.uniformMatrix4(this.mvMatrixUniform_, this.mvMatrix_);
		this.chart_.uniformMatrix4(this.pMatrixUniform_, this.chart_
				.getPMatrix());
		this.chart_.useProgram(this.colShaderProgram_);
		this.chart_.uniformMatrix4(this.mvMatrixUniform2_, this.mvMatrix_);
		this.chart_.uniformMatrix4(this.pMatrixUniform2_, this.chart_
				.getPMatrix());
		this.chart_.useProgram(this.linesProgram_);
		this.chart_.uniformMatrix4(this.mvMatrixUniform3_, this.mvMatrix_);
		this.chart_.uniformMatrix4(this.pMatrixUniform3_, this.chart_
				.getPMatrix());
		this.chart_.useProgram(this.shaderProgram_);
		float text_min;
		float text_max;
		if (this.colormap_ != null) {
			text_min = (float) this.chart_.toPlotCubeCoords(this.colormap_
					.getMinimum(), Axis.ZAxis_3D);
			text_max = (float) this.chart_.toPlotCubeCoords(this.colormap_
					.getMaximum(), Axis.ZAxis_3D);
			this.chart_.uniform1f(this.offsetUniform_, text_min);
			this.chart_.uniform1f(this.scaleFactorUniform_,
					1.0 / (text_max - text_min));
		} else {
			this.chart_.uniform1f(this.offsetUniform_, 0.0);
			this.chart_.uniform1f(this.scaleFactorUniform_, 1.0);
		}
	}

	public void resizeGL() {
		if (!this.shaderProgram_.isNull()) {
			this.chart_.useProgram(this.shaderProgram_);
			this.chart_.uniformMatrix4(this.pMatrixUniform_, this.chart_
					.getPMatrix());
		}
		if (!this.colShaderProgram_.isNull()) {
			this.chart_.useProgram(this.colShaderProgram_);
			this.chart_.uniformMatrix4(this.pMatrixUniform2_, this.chart_
					.getPMatrix());
		}
		if (!this.linesProgram_.isNull()) {
			this.chart_.useProgram(this.linesProgram_);
			this.chart_.uniformMatrix4(this.pMatrixUniform3_, this.chart_
					.getPMatrix());
		}
	}

	public void deleteAllGLResources() {
		if (!this.shaderProgram_.isNull()) {
			this.chart_.detachShader(this.shaderProgram_, this.vertexShader_);
			this.chart_.detachShader(this.shaderProgram_, this.fragmentShader_);
			this.chart_.deleteShader(this.vertexShader_);
			this.chart_.deleteShader(this.fragmentShader_);
			this.chart_.deleteProgram(this.shaderProgram_);
			this.shaderProgram_.clear();
		}
		if (!this.colShaderProgram_.isNull()) {
			this.chart_.detachShader(this.colShaderProgram_,
					this.colVertexShader_);
			this.chart_.detachShader(this.colShaderProgram_,
					this.colFragmentShader_);
			this.chart_.deleteShader(this.colVertexShader_);
			this.chart_.deleteShader(this.colFragmentShader_);
			this.chart_.deleteProgram(this.colShaderProgram_);
			this.colShaderProgram_.clear();
		}
		if (!this.linesProgram_.isNull()) {
			this.chart_.detachShader(this.linesProgram_, this.linesVertShader_);
			this.chart_.detachShader(this.linesProgram_, this.linesFragShader_);
			this.chart_.deleteShader(this.linesVertShader_);
			this.chart_.deleteShader(this.linesFragShader_);
			this.chart_.deleteProgram(this.linesProgram_);
			this.linesProgram_.clear();
		}
		if (!this.vertexPosBuffer_.isNull()) {
			this.chart_.deleteBuffer(this.vertexPosBuffer_);
			this.chart_.deleteBuffer(this.vertexSizeBuffer_);
			this.vertexPosBuffer_.clear();
		}
		if (!this.vertexPosBuffer2_.isNull()) {
			this.chart_.deleteBuffer(this.vertexPosBuffer2_);
			this.chart_.deleteBuffer(this.vertexSizeBuffer2_);
			this.chart_.deleteBuffer(this.vertexColorBuffer2_);
			this.vertexPosBuffer2_.clear();
		}
		if (!this.lineVertBuffer_.isNull()) {
			this.chart_.deleteBuffer(this.lineVertBuffer_);
			this.lineVertBuffer_.clear();
		}
		if (!this.colormapTexture_.isNull()) {
			this.chart_.deleteTexture(this.colormapTexture_);
			this.colormapTexture_.clear();
		}
	}

	private int getCountSimpleData() {
		int result;
		result = 0;
		int N = this.model_.getRowCount();
		if (this.colorColumn_ != -1) {
			result = 0;
		} else {
			for (int i = 0; i < N; i++) {
				if ((this.model_.getData(i, this.ZSeriesColumn_,
						ItemDataRole.MarkerBrushColorRole) == null)) {
					result++;
				}
			}
		}
		return result;
	}

	private void dataFromModel(final java.nio.ByteBuffer simplePtsArray,
			final java.nio.ByteBuffer simplePtsSize,
			final java.nio.ByteBuffer coloredPtsArray,
			final java.nio.ByteBuffer coloredPtsSize,
			final java.nio.ByteBuffer coloredPtsColor) {
		int N = this.model_.getRowCount();
		double xMin = this.chart_.axis(Axis.XAxis_3D).getMinimum();
		double xMax = this.chart_.axis(Axis.XAxis_3D).getMaximum();
		double yMin = this.chart_.axis(Axis.YAxis_3D).getMinimum();
		double yMax = this.chart_.axis(Axis.YAxis_3D).getMaximum();
		double zMin = this.chart_.axis(Axis.ZAxis_3D).getMinimum();
		double zMax = this.chart_.axis(Axis.ZAxis_3D).getMaximum();
		for (int i = 0; i < N; i++) {
			if (this.colorColumn_ == -1
					&& (this.model_.getData(i, this.ZSeriesColumn_,
							ItemDataRole.MarkerBrushColorRole) == null)) {
				simplePtsArray
						.putFloat((float) ((StringUtils.asNumber(this.model_
								.getData(i, this.XSeriesColumn_)) - xMin) / (xMax - xMin)));
				simplePtsArray
						.putFloat((float) ((StringUtils.asNumber(this.model_
								.getData(i, this.YSeriesColumn_)) - yMin) / (yMax - yMin)));
				simplePtsArray
						.putFloat((float) ((StringUtils.asNumber(this.model_
								.getData(i, this.ZSeriesColumn_)) - zMin) / (zMax - zMin)));
			} else {
				if (this.colorColumn_ == -1) {
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.XSeriesColumn_)) - xMin) / (xMax - xMin)));
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.YSeriesColumn_)) - yMin) / (yMax - yMin)));
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.ZSeriesColumn_)) - zMin) / (zMax - zMin)));
					WColor color = (WColor) this.model_.getData(i,
							this.ZSeriesColumn_,
							ItemDataRole.MarkerBrushColorRole);
					coloredPtsColor.putFloat((float) color.getRed());
					coloredPtsColor.putFloat((float) color.getGreen());
					coloredPtsColor.putFloat((float) color.getBlue());
					coloredPtsColor.putFloat((float) color.getAlpha());
				} else {
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.XSeriesColumn_)) - xMin) / (xMax - xMin)));
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.YSeriesColumn_)) - yMin) / (yMax - yMin)));
					coloredPtsArray
							.putFloat((float) ((StringUtils
									.asNumber(this.model_.getData(i,
											this.ZSeriesColumn_)) - zMin) / (zMax - zMin)));
					WColor color = (WColor) this.model_.getData(i,
							this.colorColumn_, this.asColorRole_);
					coloredPtsColor.putFloat((float) color.getRed());
					coloredPtsColor.putFloat((float) color.getGreen());
					coloredPtsColor.putFloat((float) color.getBlue());
					coloredPtsColor.putFloat((float) color.getAlpha());
				}
			}
			final java.nio.ByteBuffer sizeArrayAlias = this.colorColumn_ == -1
					&& (this.model_.getData(i, this.ZSeriesColumn_,
							ItemDataRole.MarkerBrushColorRole) == null) ? simplePtsSize
					: coloredPtsSize;
			if (this.sizeColumn_ == -1
					&& (this.model_.getData(i, this.ZSeriesColumn_,
							ItemDataRole.MarkerScaleFactorRole) == null)) {
				sizeArrayAlias.putFloat((float) this.pointSize_);
			} else {
				if (this.sizeColumn_ == -1) {
					sizeArrayAlias.putFloat((float) StringUtils
							.asNumber(this.model_.getData(i,
									this.ZSeriesColumn_,
									ItemDataRole.MarkerScaleFactorRole)));
				} else {
					sizeArrayAlias.putFloat((float) StringUtils
							.asNumber(this.model_.getData(i, this.sizeColumn_,
									this.asSizeRole_)));
				}
			}
		}
	}

	private void dropLineVertices(final java.nio.ByteBuffer dataPoints,
			final java.nio.ByteBuffer verticesOUT) {
		int size = dataPoints.capacity() / 4;
		int index;
		for (int i = 0; i < size / 3; i++) {
			index = 3 * i;
			verticesOUT.putFloat(dataPoints.getFloat(4 * (index)));
			verticesOUT.putFloat(dataPoints.getFloat(4 * (index + 1)));
			verticesOUT.putFloat(dataPoints.getFloat(4 * (index + 2)));
			verticesOUT.putFloat(dataPoints.getFloat(4 * (index)));
			verticesOUT.putFloat(dataPoints.getFloat(4 * (index + 1)));
			verticesOUT.putFloat(0.0f);
		}
	}

	private void initShaders() {
		this.fragmentShader_ = this.chart_
				.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
		this.chart_.shaderSource(this.fragmentShader_, ptFragShaderSrc);
		this.chart_.compileShader(this.fragmentShader_);
		this.vertexShader_ = this.chart_
				.createShader(WGLWidget.GLenum.VERTEX_SHADER);
		this.chart_.shaderSource(this.vertexShader_, ptVertexShaderSrc);
		this.chart_.compileShader(this.vertexShader_);
		this.shaderProgram_ = this.chart_.createProgram();
		this.chart_.attachShader(this.shaderProgram_, this.vertexShader_);
		this.chart_.attachShader(this.shaderProgram_, this.fragmentShader_);
		this.chart_.linkProgram(this.shaderProgram_);
		this.posAttr_ = this.chart_.getAttribLocation(this.shaderProgram_,
				"aVertexPosition");
		if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
			this.sizeAttr_ = this.chart_.getAttribLocation(this.shaderProgram_,
					"aPointSize");
		}
		this.mvMatrixUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uMVMatrix");
		this.pMatrixUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uPMatrix");
		this.cMatrixUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uCMatrix");
		this.samplerUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uSampler");
		this.offsetUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uOffset");
		this.scaleFactorUniform_ = this.chart_.getUniformLocation(
				this.shaderProgram_, "uScaleFactor");
		this.colFragmentShader_ = this.chart_
				.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
		this.chart_.shaderSource(this.colFragmentShader_, colPtFragShaderSrc);
		this.chart_.compileShader(this.colFragmentShader_);
		this.colVertexShader_ = this.chart_
				.createShader(WGLWidget.GLenum.VERTEX_SHADER);
		this.chart_.shaderSource(this.colVertexShader_, colPtVertexShaderSrc);
		this.chart_.compileShader(this.colVertexShader_);
		this.colShaderProgram_ = this.chart_.createProgram();
		this.chart_.attachShader(this.colShaderProgram_, this.colVertexShader_);
		this.chart_.attachShader(this.colShaderProgram_,
				this.colFragmentShader_);
		this.chart_.linkProgram(this.colShaderProgram_);
		this.posAttr2_ = this.chart_.getAttribLocation(this.colShaderProgram_,
				"aVertexPosition");
		if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
			this.sizeAttr2_ = this.chart_.getAttribLocation(
					this.colShaderProgram_, "aPointSize");
		}
		this.colorAttr2_ = this.chart_.getAttribLocation(
				this.colShaderProgram_, "aColor");
		this.mvMatrixUniform2_ = this.chart_.getUniformLocation(
				this.colShaderProgram_, "uMVMatrix");
		this.pMatrixUniform2_ = this.chart_.getUniformLocation(
				this.colShaderProgram_, "uPMatrix");
		this.cMatrixUniform2_ = this.chart_.getUniformLocation(
				this.colShaderProgram_, "uCMatrix");
		this.linesFragShader_ = this.chart_
				.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
		this.chart_.shaderSource(this.linesFragShader_, meshFragShaderSrc);
		this.chart_.compileShader(this.linesFragShader_);
		this.linesVertShader_ = this.chart_
				.createShader(WGLWidget.GLenum.VERTEX_SHADER);
		this.chart_.shaderSource(this.linesVertShader_, meshVertexShaderSrc);
		this.chart_.compileShader(this.linesVertShader_);
		this.linesProgram_ = this.chart_.createProgram();
		this.chart_.attachShader(this.linesProgram_, this.linesVertShader_);
		this.chart_.attachShader(this.linesProgram_, this.linesFragShader_);
		this.chart_.linkProgram(this.linesProgram_);
		this.posAttrLines_ = this.chart_.getAttribLocation(this.linesProgram_,
				"aVertexPosition");
		this.mvMatrixUniform3_ = this.chart_.getUniformLocation(
				this.linesProgram_, "uMVMatrix");
		this.pMatrixUniform3_ = this.chart_.getUniformLocation(
				this.linesProgram_, "uPMatrix");
		this.cMatrixUniform3_ = this.chart_.getUniformLocation(
				this.linesProgram_, "uCMatrix");
		this.lineColorUniform_ = this.chart_.getUniformLocation(
				this.linesProgram_, "uColor");
	}

	private void findXRange() {
		int N = this.model_.getRowCount();
		double minSoFar = Double.MAX_VALUE;
		double maxSoFar = -Double.MAX_VALUE;
		double xVal;
		for (int i = 0; i < N; i++) {
			xVal = StringUtils.asNumber(this.model_.getData(i,
					this.XSeriesColumn_));
			if (xVal < minSoFar) {
				minSoFar = xVal;
			}
			if (xVal > maxSoFar) {
				maxSoFar = xVal;
			}
		}
		this.xMin_ = minSoFar;
		this.xMax_ = maxSoFar;
		this.xRangeCached_ = true;
	}

	private void findYRange() {
		int N = this.model_.getRowCount();
		double minSoFar = Double.MAX_VALUE;
		double maxSoFar = -Double.MAX_VALUE;
		double yVal;
		for (int i = 0; i < N; i++) {
			yVal = StringUtils.asNumber(this.model_.getData(i,
					this.YSeriesColumn_));
			if (yVal < minSoFar) {
				minSoFar = yVal;
			}
			if (yVal > maxSoFar) {
				maxSoFar = yVal;
			}
		}
		this.yMin_ = minSoFar;
		this.yMax_ = maxSoFar;
		this.yRangeCached_ = true;
	}

	private void findZRange() {
		int N = this.model_.getRowCount();
		double minSoFar = Double.MAX_VALUE;
		double maxSoFar = -Double.MAX_VALUE;
		double zVal;
		for (int i = 0; i < N; i++) {
			zVal = StringUtils.asNumber(this.model_.getData(i,
					this.ZSeriesColumn_));
			if (zVal < minSoFar) {
				minSoFar = zVal;
			}
			if (zVal > maxSoFar) {
				maxSoFar = zVal;
			}
		}
		this.zMin_ = minSoFar;
		this.zMax_ = maxSoFar;
		this.rangeCached_ = true;
	}

	private int XSeriesColumn_;
	private int YSeriesColumn_;
	private int ZSeriesColumn_;
	private int colorColumn_;
	private int asColorRole_;
	private int asSizeRole_;
	private int sizeColumn_;
	private boolean droplinesEnabled_;
	private WPen droplinesPen_;
	private double xMin_;
	private double xMax_;
	private double yMin_;
	private double yMax_;
	private boolean xRangeCached_;
	private boolean yRangeCached_;
	private WGLWidget.Buffer vertexPosBuffer_;
	private WGLWidget.Buffer vertexSizeBuffer_;
	private WGLWidget.Buffer vertexPosBuffer2_;
	private WGLWidget.Buffer vertexSizeBuffer2_;
	private WGLWidget.Buffer vertexColorBuffer2_;
	private WGLWidget.Buffer lineVertBuffer_;
	private int vertexBufferSize_;
	private int vertexBuffer2Size_;
	private int lineVertBufferSize_;
	private WGLWidget.Texture colormapTexture_;
	private WGLWidget.Shader vertexShader_;
	private WGLWidget.Shader colVertexShader_;
	private WGLWidget.Shader linesVertShader_;
	private WGLWidget.Shader fragmentShader_;
	private WGLWidget.Shader colFragmentShader_;
	private WGLWidget.Shader linesFragShader_;
	private WGLWidget.Program shaderProgram_;
	private WGLWidget.Program colShaderProgram_;
	private WGLWidget.Program linesProgram_;
	private WGLWidget.AttribLocation posAttr_;
	private WGLWidget.AttribLocation posAttr2_;
	private WGLWidget.AttribLocation posAttrLines_;
	private WGLWidget.AttribLocation sizeAttr_;
	private WGLWidget.AttribLocation sizeAttr2_;
	private WGLWidget.AttribLocation colorAttr2_;
	private WGLWidget.UniformLocation mvMatrixUniform_;
	private WGLWidget.UniformLocation mvMatrixUniform2_;
	private WGLWidget.UniformLocation mvMatrixUniform3_;
	private WGLWidget.UniformLocation pMatrixUniform_;
	private WGLWidget.UniformLocation pMatrixUniform2_;
	private WGLWidget.UniformLocation pMatrixUniform3_;
	private WGLWidget.UniformLocation cMatrixUniform_;
	private WGLWidget.UniformLocation cMatrixUniform2_;
	private WGLWidget.UniformLocation cMatrixUniform3_;
	private WGLWidget.UniformLocation lineColorUniform_;
	private WGLWidget.UniformLocation samplerUniform_;
	private WGLWidget.UniformLocation offsetUniform_;
	private WGLWidget.UniformLocation scaleFactorUniform_;
	private static final String barFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nuniform sampler2D uSampler;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t) );\n}\n";
	private static final String barVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute vec2 aTextureCoord;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vTextureCoord = aTextureCoord;\n  vPos = aVertexPosition;\n}\n";
	private static final String colBarFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec3 vPos;\nvarying vec4 vColor;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
	private static final String colBarVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute vec4 aVertexColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aVertexColor/255.0;\n  vPos = aVertexPosition;\n}\n";
	private static final String ptFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying float x, y, z;\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (z - uOffset) ) );\n}\n";
	private static final String ptVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute float aPointSize;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n  gl_PointSize = aPointSize;\n}\n";
	private static final String colPtFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec4 vColor;\nvarying float x, y, z;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
	private static final String colPtVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute float aPointSize;\nattribute vec4 aColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aColor/255.0;\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n  gl_PointSize = aPointSize;\n}\n";
	private static final String surfFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying float x, y, z;\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (z - uOffset) ) );\n}\n";
	private static final String surfVertexShaderSrc = "attribute vec3 aVertexPosition;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n}\n";
	private static final String meshFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform vec4 uColor;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = uColor/255.0;\n}\n";
	private static final String meshVertexShaderSrc = "attribute vec3 aVertexPosition;\n\nvarying vec3 vPos;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vPos = aVertexPosition;\n}\n";
}
