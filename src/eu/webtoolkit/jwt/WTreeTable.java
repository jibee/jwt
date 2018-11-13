/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * A table with a navigatable tree in the first column.
 * <p>
 * 
 * A WTreeTable implements a tree table, where additional data associated is
 * associated with tree items, which are organized in columns.
 * <p>
 * Unlike the MVC-based {@link WTreeView} widget, the tree renders a widget
 * hierarchy, rather than a hierarhical standard model. This provides extra
 * flexibility (as any widget can be used as contents), at the cost of
 * server-side, client-side and bandwidth resources (especially for large tree
 * tables).
 * <p>
 * The actual data is organized and provided by {@link WTreeTableNode} widgets.
 * <p>
 * To use the tree table, you need <b>first</b> to call
 * {@link WTreeTable#addColumn(CharSequence header, WLength width) addColumn()}
 * to specify the additional data columns. Next, you need to set the tree root
 * using {@link WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
 * setTreeRoot()} and bind additional information (text or other widgets) in
 * each node using
 * {@link WTreeTableNode#setColumnWidget(int column, WWidget widget)
 * WTreeTableNode#setColumnWidget()}. Thus, you cannot change the number of
 * columns once the tree root has been set.
 * <p>
 * The table cannot be given a height using CSS style rules, instead you must
 * use layout managers, or use
 * {@link WCompositeWidget#resize(WLength width, WLength height)
 * WCompositeWidget#resize()}.
 * <p>
 * A screenshot of the treetable: <div align="center"> <img
 * src="doc-files//WTreeTable-default-1.png"
 * alt="An example WTreeTable (default)">
 * <p>
 * <strong>An example WTreeTable (default)</strong>
 * </p>
 * </div> <div align="center"> <img src="doc-files//WTreeTable-polished-1.png"
 * alt="An example WTreeTable (polished)">
 * <p>
 * <strong>An example WTreeTable (polished)</strong>
 * </p>
 * </div>
 * 
 * @see WTreeTableNode
 * @see WTreeView
 */
public class WTreeTable extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WTreeTable.class);

	/**
	 * Creates a new tree table.
	 * <p>
	 * The {@link WTreeTable#getTreeRoot() getTreeRoot()} is <code>null</code>.
	 * The table should first be properly dimensioned using
	 * {@link WTreeTable#addColumn(CharSequence header, WLength width)
	 * addColumn()} calls, and then data using
	 * {@link WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
	 * setTreeRoot()}.
	 */
	public WTreeTable(WContainerWidget parent) {
		super(parent);
		this.columnWidths_ = new ArrayList<WLength>();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setStyleClass("Wt-treetable");
		this.setPositionScheme(PositionScheme.Relative);
		this.headers_ = new WContainerWidget(this.impl_);
		this.headers_.setStyleClass("Wt-header header");
		WContainerWidget spacer = new WContainerWidget(this.headers_);
		spacer.setStyleClass("Wt-sbspacer");
		this.headerContainer_ = new WContainerWidget(this.headers_);
		this.headerContainer_.setFloatSide(Side.Right);
		this.headers_.addWidget(new WText());
		this.columnWidths_.add(WLength.Auto);
		WContainerWidget content = new WContainerWidget(this.impl_);
		content.setStyleClass("Wt-content");
		if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
			content.setOverflow(WContainerWidget.Overflow.OverflowAuto);
		} else {
			content.setAttributeValue("style",
					"overflow-y: auto; overflow-x: hidden; zoom: 1");
		}
		content.addWidget(this.tree_ = new WTree());
		this.tree_.setMargin(new WLength(3), EnumSet.of(Side.Top));
		this.tree_.resize(new WLength(100, WLength.Unit.Percentage),
				WLength.Auto);
	}

	/**
	 * Creates a new tree table.
	 * <p>
	 * Calls {@link #WTreeTable(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTreeTable() {
		this((WContainerWidget) null);
	}

	/**
	 * Adds an extra column.
	 * <p>
	 * Add an extra column, specifying the column header and a column width. The
	 * extra columns are numbered from 1 as column 0 contains the tree itself.
	 * The header for column 0 (the tree itself) is specified in
	 * {@link WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
	 * setTreeRoot()}, and the width of column 0 takes the remaining available
	 * width.
	 */
	public void addColumn(final CharSequence header, final WLength width) {
		if (this.getTreeRoot() != null) {
			throw new WException(
					"WTreeTable::addColumn(): must be called before setTreeRoot()");
		}
		WText t = new WText(header);
		t.resize(width, WLength.Auto);
		t.setInline(false);
		t.setFloatSide(Side.Left);
		this.headerContainer_.addWidget(t);
		this.columnWidths_.add(width);
	}

	/**
	 * Returns the number of columns in this table.
	 * <p>
	 * Returns the number of columns in the table, including in the count column
	 * 0 (which contains the tree).
	 * <p>
	 * 
	 * @see WTreeTable#addColumn(CharSequence header, WLength width)
	 */
	public int getColumnCount() {
		return (int) this.columnWidths_.size();
	}

	/**
	 * Sets the tree root.
	 * <p>
	 * Sets the data for the tree table, and specify the header for the first
	 * column.
	 * <p>
	 * The initial <code>root</code> is <code>null</code>.
	 * <p>
	 * 
	 * @see WTreeTable#getTreeRoot()
	 * @see WTreeTable#setTree(WTree root, CharSequence h)
	 */
	public void setTreeRoot(WTreeTableNode root, final CharSequence h) {
		this.tree_.setTreeRoot(root);
		this.header(0).setText((h.length() == 0) ? "&nbsp;" : h);
		root.setTable(this);
	}

	/**
	 * Returns the tree root.
	 */
	public WTreeTableNode getTreeRoot() {
		return ((this.tree_.getTreeRoot()) instanceof WTreeTableNode ? (WTreeTableNode) (this.tree_
				.getTreeRoot()) : null);
	}

	/**
	 * Sets the tree which provides the data for the tree table.
	 * <p>
	 * 
	 * @see WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
	 */
	public void setTree(WTree root, final CharSequence h) {
		WContainerWidget parent = ((this.tree_.getParent()) instanceof WContainerWidget ? (WContainerWidget) (this.tree_
				.getParent()) : null);
		if (this.tree_ != null)
			this.tree_.remove();
		parent.addWidget(this.tree_ = root);
		this.header(0).setText(h);
		this.tree_.resize(new WLength(100, WLength.Unit.Percentage),
				WLength.Auto);
		this.getTreeRoot().setTable(this);
	}

	/**
	 * Returns the tree that provides the data this table.
	 * <p>
	 * 
	 * @see WTreeTable#setTree(WTree root, CharSequence h)
	 */
	public WTree getTree() {
		return this.tree_;
	}

	/**
	 * Returns the column width for the given column.
	 * <p>
	 * The width of the first column (with index 0), containing the tree, is
	 * implied by the width set for the table minus the width of all other
	 * columns.
	 * <p>
	 * 
	 * @see WTreeTable#addColumn(CharSequence header, WLength width)
	 * @see WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
	 */
	public WLength columnWidth(int column) {
		return this.columnWidths_.get(column);
	}

	/**
	 * Returns the header for the given column.
	 * <p>
	 * 
	 * @see WTreeTable#addColumn(CharSequence header, WLength width)
	 * @see WTreeTable#setTreeRoot(WTreeTableNode root, CharSequence h)
	 */
	public WText header(int column) {
		if (column == 0) {
			return (((((this.impl_.getChildren().get(0)) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
					.getChildren().get(0)) : null)).getChildren().get(2)) instanceof WText ? (WText) ((((this.impl_
					.getChildren().get(0)) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
					.getChildren().get(0)) : null)).getChildren().get(2))
					: null);
		} else {
			return ((this.headerContainer_.getChildren().get(column - 1)) instanceof WText ? (WText) (this.headerContainer_
					.getChildren().get(column - 1)) : null);
		}
	}

	/**
	 * Returns the header widget.
	 * <p>
	 * This is the widget that contains the column headers.
	 */
	public WWidget getHeaderWidget() {
		return this.headers_;
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
			this.setJavaScriptMember(WT_RESIZE_JS, "$('#" + this.getId()
					+ "').data('obj').wtResize");
			this.resize(this.getWidth(), this.getHeight());
			WApplication.getInstance().addAutoJavaScript(
					"{var obj = $('#" + this.getId()
							+ "').data('obj');if (obj) obj.autoJavaScript();}");
		}
		super.render(flags);
	}

	private WContainerWidget impl_;
	private WContainerWidget headers_;
	private WContainerWidget headerContainer_;
	private WTree tree_;
	private List<WLength> columnWidths_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WTreeTable.js", wtjs1());
		this.setJavaScriptMember(" WTreeTable", "new Wt3_3_12.WTreeTable("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ");");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTreeTable",
				"function(g,a){jQuery.data(a,\"obj\",this);var h=this,i=g.WT,f=$(a).find(\".Wt-content\").get(0),j=$(a).find(\".Wt-sbspacer\").get(0);this.wtResize=function(b,d,c){d=c>=0;b.style.height=d?c+\"px\":\"\";var e=b.lastChild;c-=$(b.firstChild).outerHeight();if(d&&c>0){if(e.style.height!=c+\"px\")e.style.height=c+\"px\"}else e.style.height=\"\"};this.autoJavaScript=function(){if(a.parentNode){j.style.display=f.scrollHeight>f.offsetHeight?\"block\":\"none\";var b=i.pxself(a, \"height\");b&&h.wtResize(a,0,b,true)}}}");
	}
}
