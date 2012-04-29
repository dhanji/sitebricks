package ${package};

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Main method kicks off the Jetty servlet container pointing at our Sitebricks webapp
 * in source code.
 * <p>
 * You should run this from the sitebricks-jetty-archetype directory or appropriately change
 * the directory specified ("src/main/resources" by default) below.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Main {
    private static final int PORT = 8080;

    public static void main(String... args) throws Exception {
        Server server = new Server(PORT);
        server.addHandler(new WebAppContext("src/main/resources", "/"));

        server.start();
        server.join();
    }
}