package org.apache.tika.parser.external;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PBCore;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToTextContentHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class WaitingExternalParserTest
{
    private String[] command = {"cat", 
        WaitingExternalParser.INPUT_FILE_TOKEN, 
        WaitingExternalParser.OUTPUT_FILE_TOKEN};
    private static HashMap<Pattern, String> extractionPatterns;
    
    @BeforeClass
    public static void setUpBeforeClass() 
    {
        extractionPatterns = new HashMap<Pattern, String>();
        extractionPatterns.put(
                Pattern.compile("Duration: (\\d+:\\d+:\\d+\\.?\\d?\\d?), "),
                PBCore.INSTANTIATION_DURATION.getName());
    }
    
    @Test
    public void testExternalParser() throws IOException, SAXException, TikaException
    {
        ExternalParser parser = new ExternalParser();
        parser.setCommand(command);
        parser.setMetadataExtractionPatterns(extractionPatterns);
        testParser(parser);
    }
    
    @Test
    public void testWaitingExternalParser() throws IOException, SAXException, TikaException
    {
        WaitingExternalParser parser = new WaitingExternalParser();
        parser.setCommand(command);
        parser.setMetadataExtractionPatterns(extractionPatterns);
        testParser(parser);
    }

    private void testParser(AbstractParser parser) throws IOException, SAXException, TikaException
    {
        /*
         * See comment: https://issues.apache.org/jira/browse/TIKA-634?focusedCommentId=14533184&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14533184
         */
        Metadata extractedMetadata = new Metadata();
        String testFilePath = "/test-documents/testTxt.txt";
        InputStream stream = this.getClass().getResourceAsStream(testFilePath);
        ToTextContentHandler handler = new ToTextContentHandler();
        parser.parse(stream, handler, extractedMetadata, new ParseContext());
        String[] names = extractedMetadata.names();
        String output = handler.toString();
        assertFalse(output, output.isEmpty());
        assertEquals(1, names.length);
        assertEquals(2, extractedMetadata.getValues(PBCore.INSTANTIATION_DURATION).length);
    }

}
