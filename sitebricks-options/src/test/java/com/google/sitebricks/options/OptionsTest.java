package com.google.sitebricks.options;

import com.google.inject.Guice;
import org.testng.annotations.Test;

import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class OptionsTest {

  @Test
  public final void testOptionsInterfaceFromCommandLine() {
    String[] commandLine =
        "--host=optimusprime --domain=http://sitebricks.info --otherSetting=twoForOne!1"
            .split("[ ]+");

    MyOpts opts = Guice.createInjector(new OptionsModule(commandLine).options(MyOpts.class))
        .getInstance(MyOpts.class);

    assert "optimusprime".equals(opts.host());
    assert "http://sitebricks.info".equals(opts.domain());
    assert "twoForOne!1".equals(opts.otherSetting());
  }

  @Test
  public final void testOptionsInterfaceWithNamespaceFromCommandLine() {

    String[] commandLine =
        ("--sitebricks.host=optimusprime --sitebricks.domain=http://sitebricks.info" +
            " --sitebricks.otherSetting=twoForOne!1").split("[ ]+");

    MyOpts2 opts = Guice.createInjector(new OptionsModule(commandLine).options(MyOpts2.class))
        .getInstance(MyOpts2.class);

    assert "optimusprime".equals(opts.host());
    assert "http://sitebricks.info".equals(opts.domain());
    assert "twoForOne!1".equals(opts.otherSetting());
  }

  @Test
  public final void testTypedOptionsInterfaceFromCommandLine() {

    String[] commandLine =
        ("--name=optimusprimer --score=0.8 --port=1034").split("[ ]+");

    MyTypedOpts opts = Guice.createInjector(new OptionsModule(commandLine).options(MyTypedOpts.class))
        .getInstance(MyTypedOpts.class);

    assert "optimusprimer".equals(opts.name());
    assert new Double(0.8).equals(opts.score());
    assert 1034 == opts.port();
  }

  @Test
  public final void testTypedOptionsInterfaceWithNamespaceFromProperties() {
    Properties properties = new Properties();
    properties.put("name", "optimusprimer");
    properties.put("score", "0.7");
    properties.put("port", "65535");

    MyTypedOpts opts = Guice.createInjector(new OptionsModule(properties)
        .options(MyTypedOpts.class))
        .getInstance(MyTypedOpts.class);

    assert "optimusprimer".equals(opts.name());
    assert new Double(0.7).equals(opts.score());
    assert 65535 == opts.port();
  }

  @Test
  public final void testTypedOptionsInterfaceWithNamespaceFromPropertiesFile() {
    ResourceBundle bundle = ResourceBundle.getBundle(Options.class.getPackage().getName()
        + ".options");
    MyTypedOpts opts = Guice.createInjector(new OptionsModule(bundle)
        .options(MyTypedOpts.class))
        .getInstance(MyTypedOpts.class);

    assert "optimusprimer".equals(opts.name());
    assert new Double(0.7).equals(opts.score());
    assert 65534 == opts.port();
  }

  @Test
  public final void testTypedOptionsAbstractClassWithNamespaceFromPropertiesFile() {
    ResourceBundle bundle = ResourceBundle.getBundle(Options.class.getPackage().getName()
        + ".options");
    MyAbstractOpts opts = Guice.createInjector(new OptionsModule(bundle)
        .options(MyAbstractOpts.class))
        .getInstance(MyAbstractOpts.class);

    assert "optimusprimer".equals(opts.name());
    assert new Double(0.7).equals(opts.score());
    assert 65534 == opts.port(); // Default overridden.
    assert 22 == opts.code(); // Default.
  }

  @Options
  public static interface MyOpts {
    String host();

    String domain();

    String otherSetting();
  }

  @Options("sitebricks")
  public static interface MyOpts2 {
    String host();

    String domain();

    String otherSetting();
  }

  @Options
  public static interface MyTypedOpts {
    String name();

    Double score();

    int port();
  }

  @Options
  public static abstract class MyAbstractOpts {
    abstract String name();

    abstract Double score();

    int port() {
      return 22;
    }

    int code() {
      return 22;
    }
  }
}
