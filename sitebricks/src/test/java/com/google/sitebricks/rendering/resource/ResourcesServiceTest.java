package com.google.sitebricks.rendering.resource;

import com.google.sitebricks.Respond;
import com.google.sitebricks.Export;

import org.apache.commons.io.IOUtils;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class ResourcesServiceTest {
    private static final String AN_URI = "/thing/my.xml";
    private static final String MY_XML = "my.xml";

    @Test
    public final void registerAndServeResource() throws IOException {
        Export export = createMock(Export.class);

        expect(export.at())
                .andReturn(AN_URI);

        expect(export.resource())
                .andReturn(MY_XML)
                .anyTimes();

        replay(export);

        final ResourcesService service = new ClasspathResourcesService();
        service.add(ResourcesServiceTest.class, export);

        final Respond respond = service.serve(AN_URI);
        assert null != respond : "Did not match uri properly";

        final List list = IOUtils.readLines(ResourcesServiceTest.class.getResourceAsStream(MY_XML));
        StringBuilder builder = new StringBuilder();
        for (Object o : list) {
            builder.append((String)o);
        }

        final String out = respond.toString();
        assert null == respond.getRedirect();
        assert builder.toString().equals(out) : "Did not respond with resource content: " + out;

        verify(export);
    }

    @Test(expectedExceptions = ResourceLoadingException.class)
    public final void registerAndServeResourceFailIfMissing() throws IOException {
        Export export = createMock(Export.class);

        expect(export.at())
                .andReturn(AN_URI);

        expect(export.resource())
                .andReturn("aoskdaoskdas")
                .anyTimes();

        replay(export);

        final ResourcesService service = new ClasspathResourcesService();
        service.add(ResourcesServiceTest.class, export);

        final Respond respond = service.serve(AN_URI);
        assert null != respond : "Did not match uri properly";

        final List list = IOUtils.readLines(ResourcesServiceTest.class.getResourceAsStream(MY_XML));
        StringBuilder builder = new StringBuilder();
        for (Object o : list) {
            builder.append((String)o);
        }

        final String out = respond.toString();
        assert builder.toString().equals(out) : "Did not respond with resource content: " + out;

        verify(export);
    }
}
