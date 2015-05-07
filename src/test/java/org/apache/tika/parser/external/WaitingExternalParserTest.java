/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Unit-tests ExternalParser, demonstrating the need to wait for the external parser before returning from {parse}.
 *
 * @author Ahmed Owian
 */
public class WaitingExternalParserTest
{
    private String[] commandWithOutputFile = {"cat", 
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
        extractionPatterns.put(
                        Pattern.compile("(a+)"),
                        PBCore.INSTANTIATION_DATA_RATE.getName());
    }
    
    @Test
    public void testExternalParserMetadataExtraction() throws IOException, SAXException, TikaException
    {
        ExternalParser parser = new ExternalParser();
        parser.setCommand(commandWithOutputFile);
        parser.setMetadataExtractionPatterns(extractionPatterns);
        testMetadataExtraction(parser);
    }
    
    @Test
    public void testWaitingExternalParserMetadataExtraction() throws IOException, SAXException, TikaException
    {
        WaitingExternalParser parser = new WaitingExternalParser();
        parser.setCommand(commandWithOutputFile);
        parser.setMetadataExtractionPatterns(extractionPatterns);
        testMetadataExtraction(parser);
    }

    private void testMetadataExtraction(AbstractParser parser) throws IOException, SAXException, TikaException
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
        assertEquals(2, names.length);
        assertEquals(2, extractedMetadata.getValues(PBCore.INSTANTIATION_DURATION).length);
        String[] values = extractedMetadata.getValues(PBCore.INSTANTIATION_DATA_RATE);
        assertEquals(311, values.length);
    }
    
    @Test
    public void testWaitingExternalParserOutputExtraction() throws IOException, SAXException, TikaException
    {
        WaitingExternalParser parser = new WaitingExternalParser();
        parser.setCommand("cat", WaitingExternalParser.INPUT_FILE_TOKEN);
        testOutputExtraction(parser);
    }
    
    @Test
    public void testExternalParserOutputExtraction() throws IOException, SAXException, TikaException
    {
        ExternalParser parser = new ExternalParser();
        parser.setCommand("cat", WaitingExternalParser.INPUT_FILE_TOKEN);
        testOutputExtraction(parser);
    }
    
    private void testOutputExtraction(AbstractParser parser) throws IOException, SAXException, TikaException
    {
        Metadata extractedMetadata = new Metadata();
        String testFilePath = "/test-documents/testTxt.txt";
        InputStream stream = this.getClass().getResourceAsStream(testFilePath);
        ToTextContentHandler handler = new ToTextContentHandler();
        parser.parse(stream, handler, extractedMetadata, new ParseContext());
        String[] names = extractedMetadata.names();
        String output = handler.toString();
        assertFalse(output, output.isEmpty());
        assertTrue(output.contains("a\r\naa"));
        assertTrue(output.contains("Duration: 00:00:01.0, \r\n"));
        assertEquals(0, names.length);
    }

}
