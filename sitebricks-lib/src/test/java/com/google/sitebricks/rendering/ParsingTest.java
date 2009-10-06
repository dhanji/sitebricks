package com.google.sitebricks.rendering;

import com.google.sitebricks.compiler.Parsing;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class ParsingTest {
    private static final String XML_AND_FLAT_TEMPLATES = "XMLandFlats";

    @DataProvider(name = XML_AND_FLAT_TEMPLATES)
    public Object[][] get() {
        return new Object[][] {
            { "   @Meta() Hello world!", false },
            { "   <xml>@Meta() Hello world!</xml>", true },
            { "   @Meta() <Hello> world!</Hello>", false },
            { " <X  @Meta() Hello world!", true },
            { "  \n\n\n @Meta() Hello world!", false },
            { "  \n\n\n @Meta() Hello world!", false },
            { "  \n\t @Meta() Hello world!", false },
            { "  \n\r @Meta() Hello world!", false },
            { "  \n\r @Metas() Hello world!", true },
            { " \t \n\r @Meta) Hello world!", true },
            { " \t \n\r @Meta@Meta Hello world!", true },
            { " \t \n\r @@Meta Hello world!", true },
            { " \t \n\r @\n@Meta Hello world!", true },
            { "      \n\r @Meta Hello world!", false },
        };
    }

    @Test(dataProvider = XML_AND_FLAT_TEMPLATES)
    public final void isXmlTemplateOrNot(final String template, boolean is) {
        assert is == Parsing.treatAsXml(template);
    }


    @Test
    public final void isXmlTemplateFileOrNot() throws IOException {
        String flat = readFile("testFlatFile.js");
        String xml = readFile("testXmlFile.js");

        assert !Parsing.treatAsXml(flat);
        assert Parsing.treatAsXml(xml);
    }

    private static String readFile(final String s) throws IOException {
        return FileUtils.readFileToString(new File(ParsingTest.class.getResource(s).getFile()), null);
    }
}
