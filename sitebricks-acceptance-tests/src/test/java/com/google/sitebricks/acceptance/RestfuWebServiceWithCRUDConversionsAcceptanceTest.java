package com.google.sitebricks.acceptance;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.example.RestfulWebServiceWithCRUDConversions;
import com.google.sitebricks.example.RestfulWebServiceWithCRUDConversions.Widget;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithCRUDConversionsAcceptanceTest {
	private Widget testWidget = new Widget(100, "Widget 100", new Date(), 1.50);
	private Widget widgetOne = RestfulWebServiceWithCRUDConversions.findWidget(1).clone();

	public void create() {
		String url = AcceptanceTest.BASE_URL + RestfulWebServiceWithCRUDConversions.AT_ME;
		System.out.println("POST " + url);

		WebResponse response = createInjector()
			.getInstance(Web.class)
			.clientOf(url)
			.transports(Widget.class)
			.over(Json.class)
			.post(testWidget);

		Widget result = response.to(Widget.class).using(Json.class);
		assert result.equals(testWidget);
	}

	public void readCollection() {
		String url = AcceptanceTest.BASE_URL + RestfulWebServiceWithCRUDConversions.AT_ME;
		System.out.println("GET " + url);
		WebResponse response = createInjector()
			.getInstance(Web.class)
			.clientOf(url)
			.transports(String.class)
			.over(Json.class).get();

    @SuppressWarnings("unchecked")
		List<Widget> result = response.to(List.class).using(Json.class);

		assert result.size() == RestfulWebServiceWithCRUDConversions.widgets.size();
	}

	private Injector createInjector() {
		return Guice.createInjector(new AbstractModule() {
	      protected void configure() {
	        bind(ConverterRegistry.class).toInstance(new StandardTypeConverter());
	      }
	    });
	}

	public void readIndividual() {
		String url = AcceptanceTest.BASE_URL + RestfulWebServiceWithCRUDConversions.AT_ME + "/" + widgetOne.getId();
		System.out.println("GET " + url);
		WebResponse response = createInjector()
			.getInstance(Web.class)
			.clientOf(url)
			.transports(String.class)
			.over(Json.class)
			.get();

		Widget result = response.to(Widget.class).using(Json.class);
		assert result.equals(widgetOne);
	}

	public void update() {
		String url = AcceptanceTest.BASE_URL + RestfulWebServiceWithCRUDConversions.AT_ME;

		widgetOne.setPrice(5.50);
		System.out.println("PUT " + url);

		WebResponse response = createInjector()
			.getInstance(Web.class)
			.clientOf(url)
			.transports(Widget.class)
			.over(Json.class)
			.put(widgetOne);

		Widget result = response.to(Widget.class).using(Json.class);
		assert result.equals(widgetOne);
	}


	public void delete() {
		String url = AcceptanceTest.BASE_URL + RestfulWebServiceWithCRUDConversions.AT_ME + "/" + testWidget.getId();
		System.out.println("DELETE " + url);
		WebResponse response = createInjector()
			.getInstance(Web.class)
			.clientOf(url)
			.transports(String.class)
			.over(Json.class)
			.delete();

		Widget result = response.to(Widget.class).using(Json.class);
		assert result.equals(testWidget);
	}

}
