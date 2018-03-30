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
 * Abstract base class for iterating over series data in a chart.
 * <p>
 * 
 * This class is specialized for rendering series data.
 * <p>
 */
public class SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(SeriesIterator.class);

	/**
	 * Start handling a new segment.
	 * <p>
	 * Because of a &apos;break&apos; specified in an axis, axes may be divided
	 * in one or two segments (in fact only the API limits this now to two). The
	 * iterator will iterate all segments seperately, but each time with a
	 * different clipping region specified in the painter, corresponding to that
	 * segment.
	 * <p>
	 * The <i>currentSegmentArea</i> specifies the clipping area.
	 */
	public void startSegment(int currentXSegment, int currentYSegment,
			final WRectF currentSegmentArea) {
		this.currentXSegment_ = currentXSegment;
		this.currentYSegment_ = currentYSegment;
	}

	/**
	 * End handling a particular segment.
	 * <p>
	 * 
	 * @see SeriesIterator#startSegment(int currentXSegment, int
	 *      currentYSegment, WRectF currentSegmentArea)
	 */
	public void endSegment() {
	}

	/**
	 * Start iterating a particular series.
	 * <p>
	 * Returns whether the series values should be iterated. The
	 * <i>groupWidth</i> is the width (in pixels) of a single bar group. The
	 * chart contains <i>numBarGroups</i>, and the current series is in the
	 * <i>currentBarGroup</i>&apos;th group.
	 */
	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return true;
	}

	/**
	 * End iterating a particular series.
	 */
	public void endSeries() {
	}

	/**
	 * Process a value.
	 * <p>
	 * Processes a value with model coordinates (<i>x</i>, <i>y</i>). The y
	 * value may differ from the model&apos;s y value, because of stacked
	 * series. The y value here corresponds to the location on the chart, after
	 * stacking.
	 * <p>
	 * The <i>stackY</i> argument is the y value from the previous series (also
	 * after stacking). It will be 0, unless this series is stacked.
	 */
	public void newValue(final WDataSeries series, double x, double y,
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
	}

	/**
	 * Returns the current X segment.
	 */
	public int getCurrentXSegment() {
		return this.currentXSegment_;
	}

	/**
	 * Returns the current Y segment.
	 */
	public int getCurrentYSegment() {
		return this.currentYSegment_;
	}

	public static void setPenColor(final WPen pen, final WDataSeries series,
			int xRow, int xColumn, int yRow, int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			pen.setColor(color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WDataSeries series, int xRow, int xColumn, int yRow,
			int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			brush.setColor(color);
		}
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(v){function z(a,b,e,c){function d(f){return e?b[f]:b[l-1-f]}function j(f){for(;d(f)[2]===t||d(f)[2]===w;)f--;return f}var q=h;if(c)q=i;var l=b.length;c=Math.floor(l/2);c=j(c);var x=0,m=l,k=false;if(d(0)[q]>a)return e?-1:l;if(d(l-1)[q]<a)return e?l:-1;for(;!k;){var g=c+1;if(g<l&&(d(g)[2]===t||d(g)[2]===w))g+=2;if(d(c)[q]>a){m=c;c=Math.floor((m+x)/2);c=j(c)}else if(d(c)[q]===a)k=true;else if(g<l&&d(g)[q]>a)k=true;else if(g<l&&d(g)[q]=== a){c=g;k=true}else{x=c;c=Math.floor((m+x)/2);c=j(c)}}return e?c:l-1-c}function C(a,b){return b[0][a]<b[b.length-1][a]}var t=2,w=3,h=0,i=1,A=this;v=v.WT.gfxUtils;var D=v.rect_top,E=v.rect_bottom,B=v.rect_left,F=v.rect_right,G=v.transform_mult;this.findClosestPoint=function(a,b,e){var c=h;if(e)c=i;var d=C(c,b);e=z(a,b,d,e);if(e<0)e=0;if(e>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(e>=b.length)e=b.length-2;if(b[e][c]===a)return[b[e][h],b[e][i]];var j=d?e+1:e-1;if(d&&b[j][2]==t)j+=2;if(!d&& j<0)return[b[e][h],b[e][i]];if(!d&&j>0&&b[j][2]==w)j-=2;d=Math.abs(a-b[e][c]);a=Math.abs(b[j][c]-a);return d<a?[b[e][h],b[e][i]]:[b[j][h],b[j][i]]};this.minMaxY=function(a,b){b=b?h:i;for(var e=a[0][b],c=a[0][b],d=1;d<a.length;++d)if(a[d][2]!==t&&a[d][2]!==w&&a[d][2]!==5){if(a[d][b]>c)c=a[d][b];if(a[d][b]<e)e=a[d][b]}return[e,c]};this.projection=function(a,b){var e=Math.cos(a);a=Math.sin(a);var c=e*a,d=-b[0]*e-b[1]*a;return[e*e,c,c,a*a,e*d+b[0],a*d+b[1]]};this.distanceSquared=function(a,b){a=[b[h]- a[h],b[i]-a[i]];return a[h]*a[h]+a[i]*a[i]};this.distanceLessThanRadius=function(a,b,e){return e*e>=A.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=B(b)&&e<=F(b)&&a>=D(b)&&a<=E(b)};this.toDisplayCoord=function(a,b,e,c,d){if(e){a=[(a[h]-d[0])/d[2],(a[i]-d[1])/d[3]];c=[c[0]+a[i]*c[2],c[1]+a[h]*c[3]]}else{a=[(a[h]-d[0])/d[2],1-(a[i]-d[1])/d[3]];c=[c[0]+ a[h]*c[2],c[1]+a[i]*c[3]]}return G(b,c)};this.findYRange=function(a,b,e,c,d,j,q){if(a.length!==0){var l=A.toDisplayCoord([b,0],[1,0,0,1,0,0],c,d,j),x=A.toDisplayCoord([e,0],[1,0,0,1,0,0],c,d,j),m=c?i:h,k=c?h:i,g=C(m,a),f=z(l[m],a,g,c),n=z(x[m],a,g,c),o,p,r=Infinity,s=-Infinity,y=f===n&&f===a.length||f===-1&&n===-1;if(!y){if(g)if(f<0)f=0;else{f++;if(a[f]&&a[f][2]===t)f+=2}else if(f>=a.length-1)f=a.length-2;if(!g&&n<0)n=0;for(o=Math.min(f,n);o<=Math.max(f,n)&&o<a.length;++o)if(a[o][2]!==t&&a[o][2]!== w){if(a[o][k]<r)r=a[o][k];if(a[o][k]>s)s=a[o][k]}if(g&&f>0||!g&&f<a.length-1){if(g){p=f-1;if(a[p]&&a[p][2]===w)p-=2}else{p=f+1;if(a[p]&&a[p][2]===t)p+=2}o=(l[m]-a[p][m])/(a[f][m]-a[p][m]);f=a[p][k]+o*(a[f][k]-a[p][k]);if(f<r)r=f;if(f>s)s=f}if(g&&n<a.length-1||!g&&n>0){if(g){g=n+1;if(a[g][2]===t)g+=2}else{g=n-1;if(a[g][2]===w)g-=2}o=(x[m]-a[n][m])/(a[g][m]-a[n][m]);f=a[n][k]+o*(a[g][k]-a[n][k]);if(f<r)r=f;if(f>s)s=f}}var u;a=j[2]/(e-b);b=c?2:3;if(!y){u=d[b]/(s-r);u=d[b]/(d[b]/u+20);if(u>q[k])u=q[k]}c= c?[l[i]-D(d),!y?(r+s)/2-d[2]/u/2-B(d):0]:[l[h]-B(d),!y?-((r+s)/2+d[3]/u/2-E(d)):0];return{xZoom:a,yZoom:u,panPoint:c}}}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(na,E,v,m){function J(a){return a===undefined}function p(){return m.modelArea}function Ea(){return m.followCurve}function oa(){return m.crosshair||Ea()!==-1}function y(){return m.isHorizontal}function e(a){if(a===g)return m.xTransform;if(a===f)return m.yTransform}function i(){return m.area}function l(){return m.insideArea}function Y(a){return J(a)?m.series:m.series[a]}function T(a){return Y(a).transform}function $a(a){return y()? z([0,1,1,0,0,0],z(T(a),[0,1,1,0,0,0])):T(a)}function Fa(a){return Y(a).curve}function ab(){return m.seriesSelection}function bb(){return m.sliders}function cb(){return m.hasToolTips}function db(){return m.coordinateOverlayPadding}function wa(){return m.curveManipulation}function G(){return m.maxZoom}function F(){return m.pens}function ia(){return m.selectedCurve}function pa(a){a.preventDefault&&a.preventDefault()}function U(a,b){E.addEventListener(a,b)}function N(a,b){E.removeEventListener(a,b)}function B(a){return a.length} function Pa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ga(){if(n){if(n.tooltipTimeout){clearTimeout(n.tooltipTimeout);n.tooltipTimeout=null}if(!n.overTooltip)if(n.tooltipOuterDiv){document.body.removeChild(n.tooltipOuterDiv);n.tooltipEl=null;n.tooltipOuterDiv=null}}}function xa(){if(m.notifyTransform.x||m.notifyTransform.y){if(Ha){window.clearTimeout(Ha);Ha=null}Ha=setTimeout(function(){if(m.notifyTransform.x&&!eb(Qa,e(g))){na.emit(v.widget, \"xTransformChanged\");Z(Qa,e(g))}if(m.notifyTransform.y&&!eb(Ra,e(f))){na.emit(v.widget,\"yTransformChanged\");Z(Ra,e(f))}},ob)}}function aa(){var a,b;if(y()){a=o(i());b=t(i());return z([0,1,1,0,a,b],z(e(g),z(e(f),[0,1,1,0,-b,-a])))}else{a=o(i());b=u(i());return z([1,0,0,-1,a,b],z(e(g),z(e(f),[1,0,0,-1,-a,b])))}}function K(){return z(aa(),l())}function ba(a,b){if(J(b))b=false;a=b?a:z(ya(aa()),a);a=y()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]-i()[1])/i()[3]];return[p()[0]+ a[g]*p()[2],p()[1]+a[f]*p()[3]]}function Ia(a,b){if(J(b))b=false;return ca.toDisplayCoord(a,b?[1,0,0,1,0,0]:aa(),y(),i(),p())}function za(){var a,b;if(y()){a=(ba([0,t(i())])[0]-p()[0])/p()[2];b=(ba([0,u(i())])[0]-p()[0])/p()[2]}else{a=(ba([o(i()),0])[0]-p()[0])/p()[2];b=(ba([q(i()),0])[0]-p()[0])/p()[2]}var c;for(c=0;c<B(bb());++c){var h=$(\"#\"+bb()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function O(){Ga();if(cb()&&n.tooltipPosition)n.tooltipTimeout=setTimeout(function(){fb()},gb);da&&hb(function(){v.repaint(); oa()&&Sa()})}function Sa(){if(da){var a=D.getContext(\"2d\");a.clearRect(0,0,D.width,D.height);a.save();a.beginPath();a.moveTo(o(i()),t(i()));a.lineTo(q(i()),t(i()));a.lineTo(q(i()),u(i()));a.lineTo(o(i()),u(i()));a.closePath();a.clip();var b=z(ya(aa()),w),c=w[g],h=w[f];if(Ea()!==-1){b=pb(y()?b[f]:b[g],Fa(Ea()),y());h=z(aa(),z($a(Ea()),b));c=h[g];h=h[f];w[g]=c;w[f]=h}b=y()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[p()[0]+b[g]*p()[2],p()[1]+b[f]*p()[3]]; a.fillStyle=a.strokeStyle=m.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",q(i())-db()[0],t(i())+db()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(u(i()))+0.5);a.moveTo(Math.floor(o(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(q(i()))+0.5,Math.floor(h)+ 0.5);a.stroke();a.restore()}}function qb(a){return t(a)<=t(l())+Ja&&u(a)>=u(l())-Ja&&o(a)<=o(l())+Ja&&q(a)>=q(l())-Ja}function V(a){var b=K();if(y())if(a===qa)a=ra;else if(a===ra)a=qa;if(J(a)||a===qa)if(e(g)[0]<1){e(g)[0]=1;b=K()}if(J(a)||a===ra)if(e(f)[3]<1){e(f)[3]=1;b=K()}if(J(a)||a===qa){if(o(b)>o(l())){b=o(l())-o(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}if(q(b)<q(l())){b=q(l())-q(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}}if(J(a)||a===ra){if(t(b)>t(l())){b=t(l())- t(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b=K()}if(u(b)<u(l())){b=u(l())-u(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;K()}}xa()}function fb(){na.emit(v.widget,\"loadTooltip\",n.tooltipPosition[g],n.tooltipPosition[f])}function rb(){if(oa()&&(J(D)||v.canvas.width!==D.width||v.canvas.height!==D.height)){if(D){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",v.canvas.width);a.setAttribute(\"height\",v.canvas.height); a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}v.canvas.parentNode.appendChild(a);D=a;jQuery.data(E,\"oobj\",D)}else if(!J(D)&&!oa()){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}w=[(o(i())+q(i()))/2,(t(i())+u(i()))/2]}function ib(){return D?D:v.canvas}function Ta(a,b){if(sa){var c=Date.now();if(J(b))b=c-ea;var h={x:0,y:0},d=K(),k=sb;if(b> 2*Aa){da=false;var r=Math.floor(b/Aa-1),s;for(s=0;s<r;++s){Ta(a,Aa);if(!sa){da=true;O();return}}b-=r*Aa;da=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?fa:-fa;if(isFinite(j.x)){j.x/=1+jb*b;d[0]+=j.x*b;if(o(d)>o(l())){j.x+=-k*(o(d)-o(l()))*b;j.x*=0.7}else if(q(d)<q(l())){j.x+=-k*(q(d)-q(l()))*b;j.x*=0.7}if(Math.abs(j.x)<Ua)if(o(d)>o(l()))j.x=Ua;else if(q(d)<q(l()))j.x=-Ua;if(Math.abs(j.x)>fa)j.x=(j.x>0?1:-1)*fa;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?fa:-fa;if(isFinite(j.y)){j.y/= 1+jb*b;d[1]+=j.y*b;if(t(d)>t(l())){j.y+=-k*(t(d)-t(l()))*b;j.y*=0.7}else if(u(d)<u(l())){j.y+=-k*(u(d)-u(l()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(t(d)>t(l()))j.y=0.001;else if(u(d)<u(l()))j.y=-0.001;if(Math.abs(j.y)>fa)j.y=(j.y>0?1:-1)*fa;h.y=j.y*b}d=K();P(h,ta);a=K();if(o(d)>o(l())&&o(a)<=o(l())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(q(d)<q(l())&&q(a)>=q(l())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(t(d)>t(l())&&t(a)<=t(l())){j.y=0;P({x:0,y:-h.y},ta);V(ra)}if(u(d)<u(l())&&u(a)>=u(l())){j.y=0;P({x:0,y:-h.y}, ta);V(ra)}if(Math.abs(j.x)<kb&&Math.abs(j.y)<kb&&qb(a)){V();sa=false;A=null;j.x=0;j.y=0;ea=null;x=[]}else{ea=c;da&&Ka(Ta)}}}function La(){var a,b,c=lb(e(g)[0])-1;if(c>=B(F().x))c=B(F().x)-1;for(a=0;a<B(F().x);++a)if(c===a)for(b=0;b<B(F().x[a]);++b)F().x[a][b].color[3]=m.penAlpha.x[b];else for(b=0;b<B(F().x[a]);++b)F().x[a][b].color[3]=0;c=lb(e(f)[3])-1;if(c>=B(F().y))c=B(F().y)-1;for(a=0;a<B(F().y);++a)if(c===a)for(b=0;b<B(F().y[a]);++b)F().y[a][b].color[3]=m.penAlpha.y[b];else for(b=0;b<B(F().y[a]);++b)F().y[a][b].color[3]= 0}function P(a,b){if(J(b))b=0;var c=ba(w);if(y())a={x:a.y,y:-a.x};if(b&ta){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;xa()}else if(b&mb){b=K();if(o(b)>o(l())){if(a.x>0)a.x/=1+(o(b)-o(l()))*Ma}else if(q(b)<q(l()))if(a.x<0)a.x/=1+(q(l())-q(b))*Ma;if(t(b)>t(l())){if(a.y>0)a.y/=1+(t(b)-t(l()))*Ma}else if(u(b)<u(l()))if(a.y<0)a.y/=1+(u(l())-u(b))*Ma;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;xa()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;V()}a=Ia(c);w[g]=a[g];w[f]= a[f];O();za()}function Ba(a,b,c){var h=ba(w),d;d=y()?[a.y-t(i()),a.x-o(i())]:z(ya([1,0,0,-1,o(i()),u(i())]),[a.x,a.y]);a=d[0];d=d[1];var k=Math.pow(1.2,y()?c:b);b=Math.pow(1.2,y()?b:c);if(e(g)[0]*k>G()[g])k=G()[g]/e(g)[0];if(k<1||e(g)[0]!==G()[g])Na(e(g),z([k,0,0,1,a-k*a,0],e(g)));if(e(f)[3]*b>G()[f])b=G()[f]/e(f)[3];if(b<1||e(f)[3]!==G()[f])Na(e(f),z([1,0,0,b,0,d-b*d],e(f)));V();h=Ia(h);w[g]=h[g];w[f]=h[f];La();O();za()}jQuery.data(E,\"cobj\",this);var ga=this,C=na.WT;ga.config=m;var H=C.gfxUtils, z=H.transform_mult,ya=H.transform_inverted,Z=H.transform_assign,eb=H.transform_equal,tb=H.transform_apply,t=H.rect_top,u=H.rect_bottom,o=H.rect_left,q=H.rect_right,ca=C.chartCommon,ub=ca.minMaxY,pb=ca.findClosestPoint,vb=ca.projection,nb=ca.distanceLessThanRadius,lb=ca.toZoomLevel,ua=ca.isPointInRect,wb=ca.findYRange,Aa=17,Ka=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Aa)}}(),Va=false,hb=function(a){if(!Va){Va= true;Ka(function(){a();Va=false})}};if(window.MSPointerEvent||window.PointerEvent){E.style.touchAction=\"none\";v.canvas.style.msTouchAction=\"none\";v.canvas.style.touchAction=\"none\"}var ta=1,mb=2,qa=1,ra=2,g=0,f=1,ob=250,gb=500,jb=0.003,sb=2.0E-4,Ma=0.07,Ja=3,Ua=0.001,fa=1.5,kb=0.02,ja=jQuery.data(E,\"eobj2\");if(!ja){ja={};ja.contextmenuListener=function(a){pa(a);N(\"contextmenu\",ja.contextmenuListener)}}jQuery.data(E,\"eobj2\",ja);var Q={},ka=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ka= B(d)>0}function b(r){if(Pa(r)){pa(r);d.push(r);a();Q.start(E,{touches:d.slice(0)})}}function c(r){if(ka)if(Pa(r)){pa(r);var s;for(s=0;s<B(d);++s)if(d[s].pointerId===r.pointerId){d.splice(s,1);break}a();Q.end(E,{touches:d.slice(0),changedTouches:[]})}}function h(r){if(Pa(r)){pa(r);var s;for(s=0;s<B(d);++s)if(d[s].pointerId===r.pointerId){d[s]=r;break}a();Q.moved(E,{touches:d.slice(0)})}}var d=[],k=jQuery.data(E,\"eobj\");if(k)if(window.PointerEvent){N(\"pointerdown\",k.pointerDown);N(\"pointerup\",k.pointerUp); N(\"pointerout\",k.pointerUp);N(\"pointermove\",k.pointerMove)}else{N(\"MSPointerDown\",k.pointerDown);N(\"MSPointerUp\",k.pointerUp);N(\"MSPointerOut\",k.pointerUp);N(\"MSPointerMove\",k.pointerMove)}jQuery.data(E,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h});if(window.PointerEvent){U(\"pointerdown\",b);U(\"pointerup\",c);U(\"pointerout\",c);U(\"pointermove\",h)}else{U(\"MSPointerDown\",b);U(\"MSPointerUp\",c);U(\"MSPointerOut\",c);U(\"MSPointerMove\",h)}})();var D=jQuery.data(E,\"oobj\"),w=null,da=true,A=null,x=[],W=false, ha=false,M=null,Wa=null,Xa=null,j={x:0,y:0},S=null,ea=null,n=jQuery.data(E,\"tobj\");if(!n){n={overTooltip:false};jQuery.data(E,\"tobj\",n)}var va=null,sa=false,Ha=null,Qa=[0,0,0,0,0,0];Z(Qa,e(g));var Ra=[0,0,0,0,0,0];Z(Ra,e(f));var Na=function(a,b){Z(a,b);xa()};v.combinedTransform=aa;this.updateTooltip=function(a){Ga();if(a)if(n.tooltipPosition){n.toolTipEl=document.createElement(\"div\");n.toolTipEl.className=m.ToolTipInnerStyle;n.toolTipEl.innerHTML=a;n.tooltipOuterDiv=document.createElement(\"div\"); n.tooltipOuterDiv.className=m.ToolTipOuterStyle;document.body.appendChild(n.tooltipOuterDiv);n.tooltipOuterDiv.appendChild(n.toolTipEl);var b=C.widgetPageCoordinates(v.canvas);a=n.tooltipPosition[g]+b.x;b=n.tooltipPosition[f]+b.y;C.fitToWindow(n.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(n.toolTipEl).mouseenter(function(){n.overTooltip=true});$(n.toolTipEl).mouseleave(function(){n.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ga,200);if(!ka){var c=C.widgetCoordinates(v.canvas, b);if(ua(c,i())){if(!n.tooltipEl&&cb()){n.tooltipPosition=[c.x,c.y];n.tooltipTimeout=setTimeout(function(){fb()},gb)}if(A===null&&oa()&&da){w=[c.x,c.y];hb(Sa)}}}},0)};this.mouseOut=function(){setTimeout(Ga,200)};this.mouseDown=function(a,b){if(!ka){a=C.widgetCoordinates(v.canvas,b);if(ua(a,i()))A=a}};this.mouseUp=function(){ka||(A=null)};this.mouseDrag=function(a,b){if(!ka)if(A!==null){a=C.widgetCoordinates(v.canvas,b);if(ua(a,i())){if(C.buttons===1)if(wa()&&Y(ia())){b=ia();var c;c=y()?a.x-A.x:a.y- A.y;Z(T(b),z([1,0,0,1,0,c/e(f)[3]],T(b)));O()}else m.pan&&P({x:a.x-A.x,y:a.y-A.y});A=a}}};this.clicked=function(a,b){if(!ka)if(A===null)if(ab()){a=C.widgetCoordinates(v.canvas,b);na.emit(v.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=m.wheelActions[a];if(!J(c)){var h=C.widgetCoordinates(v.canvas,b);if(ua(h,i())){var d=C.normalizeWheel(b);if(a===0&&wa()){a=ia();var k=-d.spinY;if(Y(a)){c=$a(a);c=tb(c,Fa(a));c=ub(c,y()); c=(c[0]+c[1])/2;C.cancelEvent(b);b=Math.pow(1.2,k);Z(T(a),z([1,0,0,b,0,c-b*c],T(a)));O();return}}if((c===4||c===5||c===6)&&m.pan){a=e(g)[4];k=e(f)[5];if(c===6)P({x:-d.pixelX,y:-d.pixelY});else if(c===5)P({x:0,y:-d.pixelX-d.pixelY});else c===4&&P({x:-d.pixelX-d.pixelY,y:0});if(a!==e(g)[4]||k!==e(f)[5])C.cancelEvent(b)}else if(m.zoom){C.cancelEvent(b);k=-d.spinY;if(k===0)k=-d.spinX;if(c===1)Ba(h,0,k);else if(c===0)Ba(h,k,0);else if(c===2)Ba(h,k,k);else if(c===3)d.pixelX!==0?Ba(h,k,0):Ba(h,0,k)}}}}; var xb=function(){ab()&&na.emit(v.widget,\"seriesSelected\",A.x,A.y)};Q.start=function(a,b,c){W=B(b.touches)===1;ha=B(b.touches)===2;if(W){sa=false;a=C.widgetCoordinates(v.canvas,b.touches[0]);if(!ua(a,i()))return;va=oa()&&nb(w,[a.x,a.y],30)?1:0;ea=Date.now();A=a;if(va!==1){c||(S=window.setTimeout(xb,200));U(\"contextmenu\",ja.contextmenuListener)}C.capture(null);C.capture(ib())}else if(ha&&(m.zoom||wa())){if(S){window.clearTimeout(S);S=null}sa=false;x=[C.widgetCoordinates(v.canvas,b.touches[0]),C.widgetCoordinates(v.canvas, b.touches[1])].map(function(h){return[h.x,h.y]});if(!x.every(function(h){return ua(h,i())})){ha=null;return}C.capture(null);C.capture(ib());M=Math.atan2(x[1][1]-x[0][1],x[1][0]-x[0][0]);Wa=[(x[0][0]+x[1][0])/2,(x[0][1]+x[1][1])/2];c=Math.abs(Math.sin(M));a=Math.abs(Math.cos(M));M=c<Math.sin(0.125*Math.PI)?0:a<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(M)>0?Math.PI/4:-Math.PI/4;Xa=vb(M,Wa)}else return;pa(b)};Q.end=function(a,b){if(S){window.clearTimeout(S);S=null}window.setTimeout(function(){N(\"contextmenu\", ja.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),h=B(c)===0;h||function(){var d;for(d=0;d<B(b.changedTouches);++d)(function(){for(var k=b.changedTouches[d].identifier,r=0;r<B(c);++r)if(c[r].identifier===k){c.splice(r,1);return}})()}();h=B(c)===0;W=B(c)===1;ha=B(c)===2;if(h){Oa=null;if(va===0&&(isFinite(j.x)||isFinite(j.y))&&m.rubberBand){ea=Date.now();sa=true;Ka(Ta)}else{va===1&&ga.mouseUp(null,null);c=[];ea=Xa=Wa=M=null}va=null}else if(W||ha)Q.start(a,b,true)};var Oa=null, la=null,Ya=null;Q.moved=function(a,b){if(W||ha)if(!(W&&A==null)){pa(b);la=C.widgetCoordinates(v.canvas,b.touches[0]);if(B(b.touches)>1)Ya=C.widgetCoordinates(v.canvas,b.touches[1]);if(W&&S&&!nb([la.x,la.y],[A.x,A.y],3)){window.clearTimeout(S);S=null}Oa||(Oa=setTimeout(function(){if(W&&wa()&&Y(ia())){var c=ia();if(Y(c)){var h=la,d;d=y()?(h.x-A.x)/e(f)[3]:(h.y-A.y)/e(f)[3];T(c)[5]+=d;A=h;O()}}else if(W){h=la;d=Date.now();c={x:h.x-A.x,y:h.y-A.y};var k=d-ea;ea=d;if(va===1){w[g]+=c.x;w[f]+=c.y;oa()&&da&& Ka(Sa)}else if(m.pan){j.x=c.x/k;j.y=c.y/k;P(c,m.rubberBand?mb:0)}A=h}else if(ha&&wa()&&Y(ia())){var r=y()?g:f;d=[la,Ya].map(function(I){return y()?[I.x,ma]:[Ca,I.y]});c=Math.abs(x[1][r]-x[0][r]);k=Math.abs(d[1][r]-d[0][r]);var s=c>0?k/c:1;if(k===c)s=1;var ma=z(ya(aa()),[0,(x[0][r]+x[1][r])/2])[1],Da=z(ya(aa()),[0,(d[0][r]+d[1][r])/2])[1];c=ia();if(Y(c)){Z(T(c),z([1,0,0,s,0,-s*ma+Da],T(c)));A=h;O();x=d}}else if(ha&&m.zoom){h=ba(w);var Ca=(x[0][0]+x[1][0])/2;ma=(x[0][1]+x[1][1])/2;d=[la,Ya].map(function(I){return M=== 0?[I.x,ma]:M===Math.PI/2?[Ca,I.y]:z(Xa,[I.x,I.y])});c=Math.abs(x[1][0]-x[0][0]);k=Math.abs(d[1][0]-d[0][0]);var X=c>0?k/c:1;if(k===c||M===Math.PI/2)X=1;var Za=(d[0][0]+d[1][0])/2;c=Math.abs(x[1][1]-x[0][1]);k=Math.abs(d[1][1]-d[0][1]);s=c>0?k/c:1;if(k===c||M===0)s=1;Da=(d[0][1]+d[1][1])/2;y()&&function(){var I=X;X=s;s=I;I=Za;Za=Da;Da=I;I=Ca;Ca=ma;ma=I}();if(e(g)[0]*X>G()[g])X=G()[g]/e(g)[0];if(e(f)[3]*s>G()[f])s=G()[f]/e(f)[3];if(X!==1&&(X<1||e(g)[0]!==G()[g]))Na(e(g),z([X,0,0,1,-X*Ca+Za,0],e(g))); if(s!==1&&(s<1||e(f)[3]!==G()[f]))Na(e(f),z([1,0,0,s,0,-s*ma+Da],e(f)));V();h=Ia(h);w[g]=h[g];w[f]=h[f];x=d;La();O();za()}Oa=null},1))}};this.setXRange=function(a,b,c,h){b=p()[0]+p()[2]*b;c=p()[0]+p()[2]*c;if(o(p())>q(p())){if(b>o(p()))b=o(p());if(c<q(p()))c=q(p())}else{if(b<o(p()))b=o(p());if(c>q(p()))c=q(p())}a=Fa(a);a=wb(a,b,c,y(),i(),p(),G());b=a.xZoom;c=a.yZoom;a=a.panPoint;var d=ba(w);e(g)[0]=b;if(c&&h)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c&&h)e(f)[5]=-a[f]*c;xa();h=Ia(d);w[g]=h[g];w[f]=h[f];V();La(); O();za()};this.getSeries=function(a){return Fa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))m[b]=a[b];rb();La();O();za()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ga.touchStart=Q.start;ga.touchEnd=Q.end;ga.touchMoved=Q.moved}else{H=function(){};ga.touchStart=H;ga.touchEnd=H;ga.touchMoved=H}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
