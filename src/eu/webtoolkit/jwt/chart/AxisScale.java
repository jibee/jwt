package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that indicates a scale for an axis.
 * <p>
 * The scale determines how values are mapped onto an axis.
 * <p>
 * 
 * @see WAxis#setScale(AxisScale scale)
 */
public enum AxisScale {
	/**
	 * <p>
	 * A category scale is set as the scale for the X axis in a
	 * {@link ChartType#CategoryChart CategoryChart}, and is only applicable
	 * there. It lists all values, evenly spaced, and consecutively in the order
	 * of the model.
	 */
	CategoryScale(0),
	/**
	 * <p>
	 * A linear scale is the default scale for all axes, except for the X scale
	 * in a CategoryScale. It maps values in a linear fashion on the axis.
	 */
	LinearScale(1),
	/**
	 * <p>
	 * A logarithmic scale is useful for plotting values with of a large range,
	 * but only works for positive values.
	 */
	LogScale(2),
	/**
	 * <p>
	 * A date scale is a special linear scale, which is useful for the X axis in
	 * a ScatterPlot, when the X series contain dates (of type
	 * {@link eu.webtoolkit.jwt.WDate}).
	 */
	DateScale(3);

	private int value;

	AxisScale(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
