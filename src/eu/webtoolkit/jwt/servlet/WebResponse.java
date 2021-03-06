/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.servlet.WebRequest.ResponseType;

/**
 * A WebResponse which wraps the HttpServletResponse to support testing.
 * <p>
 * WebResponse is used instead of HttpServletRequest inside JWt's request handling,
 * and also in {@link WResource#handleRequest(WebRequest request, WebResponse response)}.
 * <p>
 * It augments the functionality of HttpServletResponse by having a constructor which
 * serializes the response to an arbitrary output stream, for testing purposes.
 * <p>
 * @see WebResponse
 */
public class WebResponse extends HttpServletResponseWrapper {
	private OutputStreamWriter outWriter;
	private HttpServletRequest request;
	private int id;
	private ServletOutputStream outputStream;
	private ResponseType responseType;

	/**
	 * Constructor which wraps a HttpServletResponse.
	 * <p>
	 * It also saves the corresponding request. This is for convenience, when wanting
	 * to change the rendering based on request information.
	 * 
	 * @param response The HttpSerlvetResponse
	 * @param request The HttpServletRequest
	 */
	public WebResponse(HttpServletResponse response, HttpServletRequest request) {
		super(response);

		this.request = request;

		try {
			outWriter = new OutputStreamWriter(getOutputStream(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct which uses a custom output stream.
	 * <p>
	 * This constructor is useful for testing purposes, for simulating a browser
	 * request and sending the output to e.g. a file.
	 * 
	 * @param out The custom output stream.
	 */
	public WebResponse(final OutputStream out) {
		super(WtServlet.getServletApi().getMockupHttpServletResponse());

		this.outputStream = new ServletOutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				out.write(arg0);
			}
		};

		try {
			outWriter = new OutputStreamWriter(outputStream, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the output stream.
	 * <p>
	 * Returns {@link HttpServletResponseWrapper#getOutputStream()} or the custom
	 * output stream passed to {@link #WebResponse(OutputStream)}.
	 * <p>
	 * You should only use the output stream to transmit binary information. Use
	 * {@link #getWriter()} for text output.
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (outputStream == null)
			return super.getOutputStream();
		else
			return outputStream;
	}

	/**
	 * Returns a text writer.
	 * <p>
	 * This returns a writer set on the output stream, which encodes text in UTF-8
	 * format.
	 * 
	 * @return a writer for streaming text.
	 */
	public Writer out() {
		return this.outWriter;
	}

	/**
	 * Sets an ID to the WebResponse (used by JWt).
	 * 
	 * @param i
	 */
	public void setId(int i) {
		id = i;
	}

	/**
	 * Returns the ID.
	 * <p>
	 * Returns the ID previously set using {@link #setId(int)}
	 * 
	 * @return the Id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Flushes the response.
	 * <p>
	 * This flushes the writer.
	 */
	public void flush() {
		try {
			outWriter.flush();
			getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception occurred when flushing the writer");
		} finally {
			if (request != null)
				WtServlet.getServletApi().completeAsyncContext(request);
		}
	}

	/**
	 * Returns the request path information.
	 * <p>
	 * This returns the path information that was passed in the request.
	 * 
	 * @return the request path information.
	 */
	public String getPathInfo() {
		String result = request.getPathInfo();

        // Jetty will report "/" as an internal path. Which totally makes no sense but is according
        // to the spec
        if (request.getServletPath().length() == 0)
        	if (result != null && result.equals("/"))
        		return "";

		return result == null ? "" : result;
	}

	/**
	 * Returns a request parameter value.
	 * @param string the parameter name
	 * @return the request parameter value, or the empty string if the parameter was not set.
	 */
	public String getParameter(String string) {
		String result = request.getParameter(string);
		return result == null ? "" : result;
	}

	/**
	 * Returns the request method.
	 * 
	 * @return the request method.
	 */
	public String getRequestMethod() {
		return request.getMethod();
	}

	/**
	 * Returns the request's parameter map.
	 * 
	 * @return the request's parameter map
	 */
	public Map<String, String[]> getParameterMap() {
		return ((WebRequest)request).getParameterMap();
	}

	/**
	 * Returns whether this request is a WebSocket request.
	 * 
	 * This is an internal JWt method.
	 */
	public boolean isWebSocketRequest() {
		return false;
	}

	/**
	 * Returns whether this request is a WebSocket message.
	 * 
	 * This is an internal JWt method.
	 */
	public boolean isWebSocketMessage() {
		return false;
	}

	/**
	 * Returns whether another WebSocket message is pending.
	 * 
	 * This is an internal JWt method.
	 */
	public boolean isWebSocketMessagePending() {
		return false;
	}
	
	/**
	 * Sets the response type.
	 * 
	 * This is an internal JWt method.
	 */
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}
	
	/**
	 * Returns the response type.
	 * 
	 * This is an internal JWt method.
	 */
	public ResponseType getResponseType() { 
		return this.responseType; 
	}
}
