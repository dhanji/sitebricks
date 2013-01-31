package com.google.sitebricks.acceptance;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.example.model.Person;

/**
 * 
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfulWebServiceValidatingDaoAcceptanceTest {

    public void shouldValidateGet() {

        Person person = new Person();
        person.setFirstName("firstName");
        person.setLastName("lastName");
        person.setAge(20);

        WebResponse response = createInjector().getInstance(Web.class)
                .clientOf(AcceptanceTest.baseUrl() + "/restvalidatingdao")
                .transports(Person.class).over(Json.class).get();

        assert response.toString().contains(person.getLastName());

    }

    public void shouldValidatePost() {

        Person person = new Person();
        person.setFirstName("firstName");
        person.setLastName("lastName");
        person.setAge(20);

        WebResponse response = createInjector().getInstance(Web.class)
                .clientOf(AcceptanceTest.baseUrl() + "/restvalidatingdao")
                .transports(Person.class).over(Json.class).post(person);

        assert response.toString().contains(person.getLastName());

    }

    public void shouldReceiveExceptionValidatePost() {

        Person person = new Person();
        person.setFirstName(null);
        person.setLastName("lastName");
        person.setAge(20);

        WebResponse response = createInjector().getInstance(Web.class)
                .clientOf(AcceptanceTest.baseUrl() + "/restvalidatingdao")
                .transports(Person.class).over(Json.class).post(person);

        assert response.toString().contains("violation.null.firstName");

    }

    private Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(ConverterRegistry.class).toInstance(new StandardTypeConverter(ImmutableSet.<Converter> of()));
            }
        });
    }

}
