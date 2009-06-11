package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates how items may be selected.
 * 
 * @see WTreeView#setSelectionMode(SelectionMode mode)
 */
public enum SelectionMode {
	/**
	 * No selections.
	 */
	NoSelection(0),
	/**
	 * Single selection only.
	 */
	SingleSelection(1),
	/**
	 * Multiple selection.
	 */
	ExtendedSelection(3);

	private int value;

	SelectionMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}