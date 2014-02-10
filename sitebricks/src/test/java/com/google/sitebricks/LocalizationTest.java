package com.google.sitebricks;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.sitebricks.i18n.Message;
import com.google.sitebricks.locale.LocaleProvider;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Unit test for the i18n binder utility.
 */
public class LocalizationTest {
  private static final String HELLO = "hello";
  private LocaleProvider localeProviderMock;

  @BeforeMethod
  public final void setup() {
    localeProviderMock = createNiceMock(LocaleProvider.class);

    expect(localeProviderMock.getLocale()).andReturn(Locale.ENGLISH);

    replay(localeProviderMock);
  }

  @Test
  public final void simpleLocalize() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(HELLO, "hello there!");

    String msg = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(Localized.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);

        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(Localized.class)
        .hello();

    assert resourceBundle.get(HELLO).equals(msg);
  }

  @Test(expectedExceptions = CreationException.class)
  public final void simpleLocalizeMissingEntry() {

    final Map<String, String> resourceBundle = Maps.newHashMap();

    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(Localized.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    });
  }

  @Test(expectedExceptions = CreationException.class)
  public final void simpleLocalizeMissingAnnotation() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "stuff");

    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedMissingAnnotation.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedMissingAnnotation.class);
  }

  @Test(expectedExceptions = CreationException.class)
  public final void simpleLocalizeWrongReturnType() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "stuff");

    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedWrongReturnType.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedWrongReturnType.class);
  }

  @Test(expectedExceptions = CreationException.class)
  public final void parameterizedLocalizeWrongArgAnnotation() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "stuff");

    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedWrongArgAnnotation.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedWrongArgAnnotation.class);
  }


  @Test(expectedExceptions = CreationException.class)
  public final void parameterizedLocalizeBrokenTemplate() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "stuff");

    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedBrokenTemplate.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedBrokenTemplate.class);
  }

  @Test
  public final void parameterizedLocalizeTemplate() {

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "hello ${name}");

    String msg = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedTemplate.class, Locale.ENGLISH, resourceBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedTemplate.class)
        .hello("Dude");

    assert "hello Dude".equals(msg);
  }


  @Test
  public final void parameterizedLocalizeTemplateMultipleLocales() {
    Locale.setDefault(Locale.ENGLISH);

    final Map<String, String> resourceBundle = Maps.newHashMap();
    resourceBundle.put(LocalizationTest.HELLO, "hello ${name}");

    final HashMap<String, String> japaneseBundle = Maps.newHashMap();
    japaneseBundle.put(LocalizationTest.HELLO, "konichiwa ${name} sama");

    final LocaleProvider customLocaleProviderMock = createNiceMock(LocaleProvider.class);
    expect(customLocaleProviderMock.getLocale()).andReturn(Locale.JAPANESE);
    replay(customLocaleProviderMock);

    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(new Localizer.Localization(LocalizedTemplate.class, Locale.ENGLISH, resourceBundle));
        locs.add(new Localizer.Localization(LocalizedTemplate.class, Locale.JAPANESE, japaneseBundle));

        Localizer.localizeAll(binder(), locs);
        bind(LocaleProvider.class).toInstance(customLocaleProviderMock);
      }
    });

    String msg = injector.getInstance(LocalizedTemplate.class).hello("Dude");
    assert "konichiwa Dude sama".equals(msg) : msg;

    // Now let's simulate english.
    EasyMock.reset(customLocaleProviderMock);
    expect(customLocaleProviderMock.getLocale()).andReturn(Locale.ENGLISH);
    replay(customLocaleProviderMock);

    msg = injector.getInstance(LocalizedTemplate.class).hello("Dude");
    assert "hello Dude".equals(msg);


    // Now let's simulate a totally different locale (should default to english).
    // Simulate an Accept-Language of French
    EasyMock.reset(customLocaleProviderMock);
    expect(customLocaleProviderMock.getLocale()).andReturn(Locale.FRENCH);
    replay(customLocaleProviderMock);

    // Assert that it uses the english locale (set as default above)
    msg = injector.getInstance(LocalizedTemplate.class).hello("Dude");
    assert "hello Dude".equals(msg);
  }


  @Test
  public final void parameterizedWithNoExternalBundle() {
    String msg = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        Set<Localizer.Localization> locs = Sets.newHashSet();
        locs.add(Localizer.defaultLocalizationFor(LocalizedTemplate.class));

        Localizer.localizeAll(binder(), locs);

        bind(LocaleProvider.class).toInstance(localeProviderMock);
      }
    }).getInstance(LocalizedTemplate.class)
        .hello("Dudette");

    assert "hello Dudette!".equals(msg);
  }


  public static interface Localized {
    @Message(message = "hello world!")
    String hello();
  }

  public static interface LocalizedMissingAnnotation {
    String hello();
  }

  public static interface LocalizedWrongReturnType {
    @Message(message = "hello world!")
    void hello();
  }

  public static interface LocalizedWrongArgAnnotation {
    @Message(message = "hello world!")
    String hello(String val);
  }

  public static interface LocalizedBrokenTemplate {
    @Message(message = "hello ${named}!")
    String hello(@Named("name") String val);
  }

  public static interface LocalizedTemplate {
    @Message(message = "hello ${name}!")
    String hello(@Named("name") String val);
  }
}
