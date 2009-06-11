package eu.webtoolkit.jwt.examples.style;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class StyleMain extends WtServlet {
	public StyleMain() {
		super();
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		app.setTitle("Style example");

		new StyleExample(app.getRoot());

		return app;
	}
}