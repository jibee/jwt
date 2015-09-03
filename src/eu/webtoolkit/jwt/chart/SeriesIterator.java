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
			double stackY, final WModelIndex xIndex, final WModelIndex yIndex) {
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

	public static void setPenColor(final WPen pen, final WModelIndex xIndex,
			final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			pen.setColor((WColor) color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WModelIndex xIndex, final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			brush.setColor((WColor) color);
		}
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(C,t,v,d){function W(){return d.crosshair||d.followCurve!==-1}function oa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function j(a){if(a===g)return d.xTransform;if(a===h)return d.yTransform}function X(){if(d.isHorizontal){var a=o(d.area),b=p(d.area);return z([0,1,1,0,a,b],z(j(g),z(j(h),[0,1,1,0,-b,-a])))}else{a=o(d.area);b=r(d.area);return z([1,0,0,-1,a,b],z(j(g),z(j(h),[1,0,0, -1,-a,b])))}}function E(){return z(X(),d.area)}function O(a,b){if(b===undefined)b=false;a=b?a:z(pa(X()),a);a=d.isHorizontal?[(a[h]-d.area[1])/d.area[3],(a[g]-d.area[0])/d.area[2]]:[(a[g]-d.area[0])/d.area[2],1-(a[h]-d.area[1])/d.area[3]];return[d.modelArea[0]+a[g]*d.modelArea[2],d.modelArea[1]+a[h]*d.modelArea[3]]}function U(a,b){if(b===undefined)b=false;if(d.isHorizontal){a=[(a[g]-d.modelArea[0])/d.modelArea[2],(a[h]-d.modelArea[1])/d.modelArea[3]];a=[d.area[0]+a[h]*d.area[2],d.area[1]+a[g]*d.area[3]]}else{a= [(a[g]-d.modelArea[0])/d.modelArea[2],1-(a[h]-d.modelArea[1])/d.modelArea[3]];a=[d.area[0]+a[g]*d.area[2],d.area[1]+a[h]*d.area[3]]}return b?a:z(X(),a)}function Ba(a,b){var f=g;if(d.isHorizontal)f=h;var e=qa(a,b);if(e<0)e=0;if(e>=b.length)e=b.length-2;if(b[e][f]===a)return[b[e][g],b[e][h]];var i=e+1;if(b[i][2]==Y)i+=2;return a-b[e][f]<b[i][f]-a?[b[e][g],b[e][h]]:[b[i][g],b[i][h]]}function qa(a,b){function f(q){b[q][2]===ra&&--q;b[q][2]===Y&&--q;return q}var e=g;if(d.isHorizontal)e=h;var i=b.length, k=Math.floor(i/2);k=f(k);var m=0,n=i,A=false;if(b[0][e]>a)return-1;if(b[i-1][e]<a)return i;for(;!A;){i=k+1;if(b[i][2]===Y)i+=2;if(b[k][e]>a){n=k;k=Math.floor((n+m)/2);k=f(k)}else if(b[k][e]===a)A=true;else if(b[i][e]>a)A=true;else if(b[i][e]===a){k=i;A=true}else{m=k;k=Math.floor((n+m)/2);k=f(k)}}return k}function fa(){var a,b;if(d.isHorizontal){a=(O([0,p(d.area)])[0]-d.modelArea[0])/d.modelArea[2];b=(O([0,r(d.area)])[0]-d.modelArea[0])/d.modelArea[2]}else{a=(O([o(d.area),0])[0]-d.modelArea[0])/d.modelArea[2]; b=(O([s(d.area),0])[0]-d.modelArea[0])/d.modelArea[2]}var f;for(f=0;f<d.sliders.length;++f){var e=$(\"#\"+d.sliders[f]);if(e)(e=e.data(\"sobj\"))&&e.changeRange(a,b)}}function Z(){P&&ga(function(){v.repaint();W()&&sa()})}function sa(){if(P){var a=B.getContext(\"2d\");a.clearRect(0,0,B.width,B.height);a.save();a.beginPath();a.moveTo(o(d.area),p(d.area));a.lineTo(s(d.area),p(d.area));a.lineTo(s(d.area),r(d.area));a.lineTo(o(d.area),r(d.area));a.closePath();a.clip();var b=z(pa(X()),u),f=u[g],e=u[h];if(d.followCurve!== -1){b=Ba(d.isHorizontal?b[h]:b[g],d.series[d.followCurve]);e=z(X(),b);f=e[g];e=e[h];u[g]=f;u[h]=e}b=d.isHorizontal?[(b[h]-d.area[1])/d.area[3],(b[g]-d.area[0])/d.area[2]]:[(b[g]-d.area[0])/d.area[2],1-(b[h]-d.area[1])/d.area[3]];b=[d.modelArea[0]+b[g]*d.modelArea[2],d.modelArea[1]+b[h]*d.modelArea[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var i=b[0].toFixed(2);b=b[1].toFixed(2);if(i==\"-0.00\")i=\"0.00\";if(b==\"-0.00\")b=\"0.00\";a.fillText(\"(\"+i+\",\"+b+\")\",s(d.area)-d.coordinateOverlayPadding[0], p(d.area)+d.coordinateOverlayPadding[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(f)+0.5,Math.floor(p(d.area))+0.5);a.lineTo(Math.floor(f)+0.5,Math.floor(r(d.area))+0.5);a.moveTo(Math.floor(o(d.area))+0.5,Math.floor(e)+0.5);a.lineTo(Math.floor(s(d.area))+0.5,Math.floor(e)+0.5);a.stroke();a.restore()}}function aa(a,b){var f;if(a.x!==undefined){f=a.x;a=a.y}else{f=a[0];a=a[1]}return f>=o(b)&&f<=s(b)&&a>=p(b)&&a<=r(b)}function Ca(a){return p(a)<=p(d.area)+ja&&r(a)>=r(d.area)- ja&&o(a)<=o(d.area)+ja&&s(a)>=s(d.area)-ja}function J(a){var b=E();if(d.isHorizontal)if(a===ba)a=ca;else if(a===ca)a=ba;if(a===undefined||a===ba)if(j(g)[0]<1){j(g)[0]=1;b=E()}if(a===undefined||a===ca)if(j(h)[3]<1){j(h)[3]=1;b=E()}if(a===undefined||a===ba){if(o(b)>o(d.area)){b=o(d.area)-o(b);if(d.isHorizontal)j(h)[5]=j(h)[5]+b;else j(g)[4]=j(g)[4]+b;b=E()}if(s(b)<s(d.area)){b=s(d.area)-s(b);if(d.isHorizontal)j(h)[5]=j(h)[5]+b;else j(g)[4]=j(g)[4]+b;b=E()}}if(a===undefined||a===ca){if(p(b)>p(d.area)){b= p(d.area)-p(b);if(d.isHorizontal)j(g)[4]=j(g)[4]+b;else j(h)[5]=j(h)[5]-b;b=E()}if(r(b)<r(d.area)){b=r(d.area)-r(b);if(d.isHorizontal)j(g)[4]=j(g)[4]+b;else j(h)[5]=j(h)[5]-b;E()}}}function Da(){if(W&&(B===undefined||v.canvas.width!==B.width||v.canvas.height!==B.height)){if(B){B.parentNode.removeChild(B);jQuery.removeData(t,\"oobj\");B=undefined}c=document.createElement(\"canvas\");c.setAttribute(\"width\",v.canvas.width);c.setAttribute(\"height\",v.canvas.height);c.style.position=\"absolute\";c.style.display= \"block\";c.style.left=\"0\";c.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){c.style.msTouchAction=\"none\";c.style.touchAction=\"none\"}v.canvas.parentNode.appendChild(c);B=c;jQuery.data(t,\"oobj\",B)}else if(B!==undefined&&!W()){B.parentNode.removeChild(B);jQuery.removeData(t,\"oobj\");B=undefined}if(u===null)u=U([(o(d.modelArea)+s(d.modelArea))/2,(p(d.modelArea)+r(d.modelArea))/2])}function Ea(a,b){var f=Math.cos(a);a=Math.sin(a);var e=f*a,i=-b[0]*f-b[1]*a;return[f*f,e,e,a*a,f*i+b[0],a*i+b[1]]} function Fa(a,b,f){a=[b[g]-a[g],b[h]-a[h]];return f*f>=a[g]*a[g]+a[h]*a[h]}function ta(a,b){if(da){var f=Date.now();if(b===undefined)b=f-K;var e={x:0,y:0},i=E(),k=Ga;if(b>2*ha){P=false;var m=Math.floor(b/ha-1),n;for(n=0;n<m;++n){ta(a,ha);if(!da){P=true;Z();return}}b-=m*ha;P=true}if(l.x===Infinity||l.x===-Infinity)l.x=l.x>0?Q:-Q;if(isFinite(l.x)){l.x/=1+xa*b;i[0]+=l.x*b;if(o(i)>o(d.area)){l.x+=-k*(o(i)-o(d.area))*b;l.x*=0.7}else if(s(i)<s(d.area)){l.x+=-k*(s(i)-s(d.area))*b;l.x*=0.7}if(Math.abs(l.x)< ua)if(o(i)>o(d.area))l.x=ua;else if(s(i)<s(d.area))l.x=-ua;if(Math.abs(l.x)>Q)l.x=(l.x>0?1:-1)*Q;e.x=l.x*b}if(l.y===Infinity||l.y===-Infinity)l.y=l.y>0?Q:-Q;if(isFinite(l.y)){l.y/=1+xa*b;i[1]+=l.y*b;if(p(i)>p(d.area)){l.y+=-k*(p(i)-p(d.area))*b;l.y*=0.7}else if(r(i)<r(d.area)){l.y+=-k*(r(i)-r(d.area))*b;l.y*=0.7}if(Math.abs(l.y)<0.001)if(p(i)>p(d.area))l.y=0.001;else if(r(i)<r(d.area))l.y=-0.001;if(Math.abs(l.y)>Q)l.y=(l.y>0?1:-1)*Q;e.y=l.y*b}i=E();H(e,ea);a=E();if(o(i)>o(d.area)&&o(a)<=o(d.area)){l.x= 0;H({x:-e.x,y:0},ea);J(ba)}if(s(i)<s(d.area)&&s(a)>=s(d.area)){l.x=0;H({x:-e.x,y:0},ea);J(ba)}if(p(i)>p(d.area)&&p(a)<=p(d.area)){l.y=0;H({x:0,y:-e.y},ea);J(ca)}if(r(i)<r(d.area)&&r(a)>=r(d.area)){l.y=0;H({x:0,y:-e.y},ea);J(ca)}if(Math.abs(l.x)<ya&&Math.abs(l.y)<ya&&Ca(a)){J();da=false;D=null;l.x=0;l.y=0;K=null;w=[]}else{K=f;P&&ga(ta)}}}function za(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1}function ka(){var a,b,f=za(j(g)[0])-1;if(f>=d.pens.x.length)f=d.pens.x.length-1;for(a=0;a<d.pens.x.length;++a)if(f=== a)for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]=d.penAlpha.x[b];else for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]=0;f=za(j(h)[3])-1;if(f>=d.pens.y.length)f=d.pens.y.length-1;for(a=0;a<d.pens.y.length;++a)if(f===a)for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=d.penAlpha.y[b];else for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=0}function H(a,b){var f=O(u);if(d.isHorizontal)a={x:a.y,y:-a.x};if(b&ea){j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y}else if(b&Aa){b=E(); if(o(b)>o(d.area)){if(a.x>0)a.x/=1+(o(b)-o(d.area))*la}else if(s(b)<s(d.area))if(a.x<0)a.x/=1+(s(d.area)-s(b))*la;if(p(b)>p(d.area)){if(a.y>0)a.y/=1+(p(b)-p(d.area))*la}else if(r(b)<r(d.area))if(a.y<0)a.y/=1+(r(d.area)-r(b))*la;j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;u[g]+=a.x;u[h]+=a.y}else{j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;u[g]+=a.x;u[h]+=a.y;J()}a=U(f);u[g]=a[g];u[h]=a[h];Z();fa()}function ia(a,b,f){var e=O(u),i;i=d.isHorizontal?[a.y-p(d.area),a.x-o(d.area)]:z(pa([1,0,0,-1,o(d.area),r(d.area)]), [a.x,a.y]);a=i[0];i=i[1];var k=Math.pow(1.2,d.isHorizontal?f:b);b=Math.pow(1.2,d.isHorizontal?b:f);if(j(g)[0]*k>d.maxZoom[g])k=d.maxZoom[g]/j(g)[0];if(k<1||j(g)[0]!==d.maxZoom[g])ma(j(g),z([k,0,0,1,a-k*a,0],j(g)));if(j(h)[3]*b>d.maxZoom[h])b=d.maxZoom[h]/j(h)[3];if(b<1||j(h)[3]!==d.maxZoom[h])ma(j(h),z([1,0,0,b,0,i-b*i],j(h)));J();e=U(e);u[g]=e[g];u[h]=e[h];ka();Z();fa()}var ha=17,ga=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame|| function(a){window.setTimeout(a,ha)}}();if(window.MSPointerEvent||window.PointerEvent){t.style.touchAction=\"none\";v.canvas.style.msTouchAction=\"none\";v.canvas.style.touchAction=\"none\"}var Y=2,ra=3,ea=1,Aa=2,ba=1,ca=2,g=0,h=1,I={},M=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){if(pointers.length>0&&!M)M=true;else if(pointers.length<=0&&M)M=false}function b(k){if(oa(k)){k.preventDefault();pointers.push(k);a();I.start(t,{touches:pointers.slice(0)})}}function f(k){if(M)if(oa(k)){k.preventDefault(); var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers.splice(m,1);break}a();I.end(t,{touches:pointers.slice(0),changedTouches:[]})}}function e(k){if(oa(k)){k.preventDefault();var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers[m]=k;break}a();I.moved(t,{touches:pointers.slice(0)})}}pointers=[];var i=jQuery.data(t,\"eobj\");if(i)if(window.PointerEvent){t.removeEventListener(\"pointerdown\",i.pointerDown);t.removeEventListener(\"pointerup\",i.pointerUp); t.removeEventListener(\"pointerout\",i.pointerUp);t.removeEventListener(\"pointermove\",i.pointerMove)}else{t.removeEventListener(\"MSPointerDown\",i.pointerDown);t.removeEventListener(\"MSPointerUp\",i.pointerUp);t.removeEventListener(\"MSPointerOut\",i.pointerUp);t.removeEventListener(\"MSPointerMove\",i.pointerMove)}jQuery.data(t,\"eobj\",{pointerDown:b,pointerUp:f,pointerMove:e});if(window.PointerEvent){t.addEventListener(\"pointerdown\",b);t.addEventListener(\"pointerup\",f);t.addEventListener(\"pointerout\",f); t.addEventListener(\"pointermove\",e)}else{t.addEventListener(\"MSPointerDown\",b);t.addEventListener(\"MSPointerUp\",f);t.addEventListener(\"MSPointerOut\",f);t.addEventListener(\"MSPointerMove\",e)}})();var xa=0.003,Ga=2.0E-4,la=0.07,ja=3,ua=0.001,Q=1.5,ya=0.02;jQuery.data(t,\"cobj\",this);var S=this,y=C.WT;S.config=d;var B=jQuery.data(t,\"oobj\"),u=null,P=true,D=null,w=[],V=false,T=false,F=null,va=null,wa=null,l={x:0,y:0},K=null,na=null;C=y.gfxUtils;var z=C.transform_mult,pa=C.transform_inverted,ma=C.transform_assign, p=C.rect_top,r=C.rect_bottom,o=C.rect_left,s=C.rect_right,da=false;v.combinedTransform=X;this.mouseMove=function(a,b){setTimeout(function(){if(!M){var f=y.widgetCoordinates(v.canvas,b);if(aa(f,d.area))if(W()&&P){u=[f.x,f.y];ga(sa)}}},0)};this.mouseDown=function(a,b){if(!M){a=y.widgetCoordinates(v.canvas,b);if(aa(a,d.area))D=a}};this.mouseUp=function(){M||(D=null)};this.mouseDrag=function(a,b){if(!M)if(D!==null){a=y.widgetCoordinates(v.canvas,b);if(aa(a,d.area)){y.buttons===1&&d.pan&&H({x:a.x-D.x, y:a.y-D.y});D=a}}};this.mouseWheel=function(a,b){a=d.wheelActions[(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey];if(a!==undefined){var f=y.widgetCoordinates(v.canvas,b);if(aa(f,d.area)){var e=y.normalizeWheel(b);if((a===4||a===5||a===6)&&d.pan){f=j(g)[4];var i=j(h)[5];if(a===6)H({x:-e.pixelX,y:-e.pixelY});else if(a===5)H({x:0,y:-e.pixelX-e.pixelY});else a===4&&H({x:-e.pixelX-e.pixelY,y:0});if(f!==j(g)[4]||i!==j(h)[5])y.cancelEvent(b)}else if(d.zoom){y.cancelEvent(b);b=-e.spinY;if(b===0)b= -e.spinX;if(a===1)ia(f,0,b);else if(a===0)ia(f,b,0);else if(a===2)ia(f,b,b);else if(a===3)e.pixelX!==0?ia(f,b,0):ia(f,0,b)}}}};I.start=function(a,b){V=b.touches.length===1;T=b.touches.length===2;if(V){da=false;a=y.widgetCoordinates(v.canvas,b.touches[0]);if(!aa(a,d.area))return;na=W()&&Fa(u,[a.x,a.y],30)?1:0;K=Date.now();D=a;y.capture(null);y.capture(v.canvas)}else if(T&&d.zoom){da=false;w=[y.widgetCoordinates(v.canvas,b.touches[0]),y.widgetCoordinates(v.canvas,b.touches[1])].map(function(e){return[e.x, e.y]});if(!w.every(function(e){return aa(e,d.area)})){T=null;return}y.capture(null);y.capture(v.canvas);F=Math.atan2(w[1][1]-w[0][1],w[1][0]-w[0][0]);va=[(w[0][0]+w[1][0])/2,(w[0][1]+w[1][1])/2];a=Math.abs(Math.sin(F));var f=Math.abs(Math.cos(F));F=a<Math.sin(0.125*Math.PI)?0:f<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(F)>0?Math.PI/4:-Math.PI/4;wa=Ea(F,va)}else return;b.preventDefault&&b.preventDefault()};I.end=function(a,b){var f=Array.prototype.slice.call(b.touches),e=f.length===0;V=f.length=== 1;T=f.length===2;e||function(){var i;for(i=0;i<b.changedTouches.length;++i)(function(){for(var k=b.changedTouches[i].identifier,m=0;m<f.length;++m)if(f[m].identifier===k){f.splice(m,1);return}})()}();e=f.length===0;V=f.length===1;T=f.length===2;if(e){if(na===0&&(isFinite(l.x)||isFinite(l.y))&&d.rubberBand){K=Date.now();da=true;ga(ta)}else{S.mouseUp(null,null);f=[];wa=va=F=null;if(K!=null){Date.now();K=null}}na=null}else if(V||T)I.start(a,b)};I.moved=function(a,b){if(V||T)if(V){if(D!==null){a=y.widgetCoordinates(v.canvas, b.touches[0]);var f=Date.now(),e={x:a.x-D.x,y:a.y-D.y},i=f-K;K=f;if(na===1){u[g]+=e.x;u[h]+=e.y;W()&&P&&ga(sa)}else if(d.pan){if(a.x<d.area[0]||a.x>d.area[0]+d.area[2]){l={x:0,y:0};return}if(a.y<d.area[1]||a.y>d.area[1]+d.area[3]){l={x:0,y:0};return}l.x=e.x/i;l.y=e.y/i;H(e,d.rubberBand?Aa:0)}b.preventDefault&&b.preventDefault();D=a}}else if(T&&d.zoom){b.preventDefault&&b.preventDefault();a=O(u);var k=(w[0][0]+w[1][0])/2,m=(w[0][1]+w[1][1])/2;b=[y.widgetCoordinates(v.canvas,b.touches[0]),y.widgetCoordinates(v.canvas, b.touches[1])].map(function(x){return F===0?[x.x,m]:F===Math.PI/2?[k,x.y]:z(wa,[x.x,x.y])});f=Math.abs(w[1][0]-w[0][0]);e=Math.abs(b[1][0]-b[0][0]);var n=f>0?e/f:1;if(e===f||F===Math.PI/2)n=1;var A=(b[0][0]+b[1][0])/2;f=Math.abs(w[1][1]-w[0][1]);e=Math.abs(b[1][1]-b[0][1]);var q=f?e/f:1;if(e===f||F===0)q=1;var G=(b[0][1]+b[1][1])/2;d.isHorizontal&&function(){var x=n;n=q;q=x;x=A;A=G;G=x;x=k;k=m;m=x}();if(j(g)[0]*n>d.maxZoom[g])n=d.maxZoom[g]/j(g)[0];if(j(h)[3]*q>d.maxZoom[h])q=d.maxZoom[h]/j(h)[3]; if(n!==1&&(n<1||j(g)[0]!==d.maxZoom[g]))ma(j(g),z([n,0,0,1,-n*k+A,0],j(g)));if(q!==1&&(q<1||j(h)[3]!==d.maxZoom[h]))ma(j(h),z([1,0,0,q,0,-q*m+G],j(h)));J();a=U(a);u[g]=a[g];u[h]=a[h];w=b;ka();Z();fa()}};this.setXRange=function(a,b,f){b=d.modelArea[0]+d.modelArea[2]*b;f=d.modelArea[0]+d.modelArea[2]*f;if(b<o(d.modelArea))b=o(d.modelArea);if(f>s(d.modelArea))f=s(d.modelArea);var e=d.series[a];if(e.length!==0){a=U([b,0],true);var i=U([f,0],true),k=d.isHorizontal?h:g,m=d.isHorizontal?g:h,n=qa(a[k],e); if(n<0)n=0;else{n++;if(e[n][2]===Y)n+=2}var A=qa(i[k],e),q,G,x=Infinity,N=-Infinity;for(q=n;q<=A&&q<e.length;++q)if(e[q][2]!==Y&&e[q][2]!==ra){if(e[q][m]<x)x=e[q][m];if(e[q][m]>N)N=e[q][m]}if(n>0){G=n-1;if(e[G][2]===ra)G-=2;q=(a[k]-e[G][k])/(e[n][k]-e[G][k]);n=e[G][m]+q*(e[n][m]-e[G][m]);if(n<x)x=n;if(n>N)N=n}if(A<e.length-1){n=A+1;if(e[n][2]===Y)n+=2;q=(i[k]-e[A][k])/(e[n][k]-e[A][k]);n=e[A][m]+q*(e[n][m]-e[A][m]);if(n<x)x=n;if(n>N)N=n}b=d.modelArea[2]/(f-b);e=d.isHorizontal?2:3;f=d.area[e]/(N-x); f=d.area[e]/(d.area[e]/f+20);if(f>d.maxZoom[m])f=d.maxZoom[m];a=d.isHorizontal?[a[h]-p(d.area),(x+N)/2-d.area[2]/f/2-o(d.area)]:[a[g]-o(d.area),-((x+N)/2+d.area[3]/f/2-r(d.area))];m=O(u);j(g)[0]=b;j(h)[3]=f;j(g)[4]=-a[g]*b;j(h)[5]=-a[h]*f;a=U(m);u[g]=a[g];u[h]=a[h];J();ka();Z();fa()}};this.getSeries=function(a){return d.series[a]};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))d[b]=a[b];Da();ka();Z();fa()};this.updateConfig({});if(window.TouchEvent&& !window.MSPointerEvent&&!window.PointerEvent){S.touchStart=I.start;S.touchEnd=I.end;S.touchMoved=I.moved}else{C=function(){};S.touchStart=C;S.touchEnd=C;S.touchMoved=C}}");
	}

	private static final int TICK_LENGTH = 5;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
