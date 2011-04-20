package com.google.sitebricks.options;

import com.google.inject.Guice;
import org.testng.annotations.Test;

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
  public final void testTypedOptionsInterfaceWithNamespaceFromCommandLine() {

    String[] commandLine =
        ("--name=optimusprimer --score=0.8 --port=1034").split("[ ]+");

    MyTypedOpts opts = Guice.createInjector(new OptionsModule(commandLine).options(MyTypedOpts.class))
        .getInstance(MyTypedOpts.class);

    assert "optimusprimer".equals(opts.name());
    assert new Double(0.8).equals(opts.score());
    assert 1034 == opts.port();
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
}
