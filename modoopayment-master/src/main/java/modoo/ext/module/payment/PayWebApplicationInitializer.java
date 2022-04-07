package modoo.ext.module.payment;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class PayWebApplicationInitializer implements WebApplicationInitializer{

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		XmlWebApplicationContext rootContext = new XmlWebApplicationContext();
		rootContext.setConfigLocations(new String[] { "classpath*:spring/context-*.xml" });
		rootContext.refresh();
		rootContext.start();		
		servletContext.addListener(new ContextLoaderListener(rootContext));
	}
}
