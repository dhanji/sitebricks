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
 * 
 */
@Singleton
public class JspTemplateCompiler implements TemplateCompiler {

	private final WidgetRegistry registry;
	private final PageBook pageBook;
	private final SystemMetrics metrics;

	@Inject
	Provider<HttpServletRequest> httpServletRequestProvider;

	@Inject
	Provider<HttpServletResponse> httpServletResponseProvider;

	// special widget types (built-in symbol table)
	private static final String REQUIRE_WIDGET = "@require";
	private static final String REPEAT_WIDGET = "repeat";
	private static final String CHOOSE_WIDGET = "choose";

	@Inject
	public JspTemplateCompiler(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
		this.registry = registry;
		this.pageBook = pageBook;
		this.metrics = metrics;
	}

	public Renderable compile(Class<?> page, final Template template) {

		Renderable renderable = new Renderable() {

			@Override
			public void render(Object bound, Respond respond) {
				HttpServletRequest request = httpServletRequestProvider.get();
				HttpServletResponse response = httpServletResponseProvider.get();
				RequestDispatcher requestDispatcher = request.getRequestDispatcher(template.getName());
				try {
					requestDispatcher.include(request, response);
				} catch (ServletException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not include the response for path=" + template.getName(), e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not include the response for path=" + template.getName(), e);
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
