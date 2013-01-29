package com.google.sitebricks.acceptance;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.acceptance.util.SitebricksServiceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.example.RestfulWebServiceValidating.Person;

/**
 * 
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfulWebServiceValidatingAcceptanceTest extends SitebricksServiceTest {

    public void shouldTransportJsonWithoutTemplate() {

        Person person = new Person();
        person.setFirstName("firstName");
        person.setLastName("lastName");
        person.setAge(20);

        WebResponse response = createInjector().getInstance(Web.class)
                .clientOf(AcceptanceTest.baseUrl() + "/rest/validate")
                .transports(Person.class).over(Json.class).post(person);

        assert response.toString().contains(person.getLastName());

    }

    private Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(ConverterRegistry.class).toInstance(new StandardTypeConverter(ImmutableSet.<Converter> of()));
            }
        });
    }

}
