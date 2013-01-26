package com.google.sitebricks.compiler;

import java.io.IOException;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.Template;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * Class that delegates the JSP compilation to the provided WEB container compiler
 * and let the {@link RequestDispatcher} write the result into the response.
 * 
 * The {@link Respond} is not used to write the result.
 * 
 * An pageFlow attribute is added to the request so the accessors available in the 
 * page object can be used from the JSP page.
 */
@Singleton
public class JspTemplateCompiler implements TemplateCompiler {

	@Inject
	Provider<HttpServletRequest> httpServletRequestProvider;

	@Inject
	Provider<HttpServletResponse> httpServletResponseProvider;

	@Inject
	public JspTemplateCompiler(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
	}

	public Renderable compile(Class<?> page, final Template template) {

		Renderable renderable = new Renderable() {

			@Override
			public void render(Object bound, Respond respond) {
				HttpServletRequest request = httpServletRequestProvider.get();
				HttpServletResponse response = httpServletResponseProvider.get();
				RequestDispatcher requestDispatcher = request.getRequestDispatcher(template.getName());
				request.setAttribute("pageFlow", bound);
				try {
					requestDispatcher.include(request, response);
				} catch (ServletException e) {
					throw new RuntimeException("Could not include the JSP response for path=" + template.getName(), e);
				} catch (IOException e) {
					throw new RuntimeException("Could not include the JSP response for path=" + template.getName(), e);
				}
			}

			@Override
			public <T extends Renderable> Set<T> collect(Class<T> clazz) {
				throw new IllegalStateException(
				        "The collect method should not be called while rendering a JSP template.");
			}
		};

		return renderable;
	}

}
