/*
 * Author:  Robert David (9427084@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package org.semanticweb.culturalheritage.prometheus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Web API Java Application for the Extraction of CIDOC CRM based RDF from Prometheus Image Archive
 *
 */
public class Prometheus2CRM  {
	
	private static Logger log = LoggerFactory.getLogger(Prometheus2CRM.class);
	
	private static final String baseUri = "http://prometheus.uni-koeln.de/pandora/image/show/";	
	
    public static void main( String[] args ) {

        Dataset dataset = null;
    	try {
    		if (args.length == 0 || args.length % 2 == 1)
    		    printUsage();

		    URL url = null;
		    String archiveId = null;
		    String itemId = null;
		    String tdb = null;
		    String file = null;
		    
            try {                    
                for (int i = 0; i < args.length; i=i+2) {
                    if (args[i].equals("-u") || args[i].equals("--url"))
                        url = new URL(args[i+1]);
                    else if (args[i].equals("-a") || args[i].equals("--archiveId"))
                        archiveId = args[i+1];
                    else if (args[i].equals("-i") || args[i].equals("--itemId"))
                        itemId = args[i+1];
                    else if (args[i].equals("-t") || args[i].equals("--tdb"))
                        tdb = args[i+1];
                    else if (args[i].equals("-f") || args[i].equals("--file"))
                        file = args[i+1];
                    else throw new Exception("Illegal option");
                }
            } catch (Exception e) {
                printUsage();
            }

            // if url given, no archiveId or itemId may be given
            // if url not given, both archiveId and itemId must be given
            if ((url != null && (archiveId != null || itemId != null))
                    || (url == null && (archiveId == null || itemId == null)))
                printUsage();                
		    
            if (archiveId != null)
                if (!SourceManager.containsRepository(archiveId)) {
                    log.error("For <archiveId> select one of:");
                    SourceManager.printArchiveList();
                    return;
                } else {
                    url = new URL(baseUri + archiveId + "-" + itemId);
                }

            if (file != null)
                try {
                    File f = new File(file);
                    if (f.exists()) {
                        log.error("The <file> " + file + " already exists");
                        return;
                    }
                } catch (Exception e) {
                    log.error("Error creating file " + file, e);
                    printUsage();
                }

            if (tdb != null)
                try {
                    dataset = tdb.endsWith(".ttl") ? 
                            TDBFactory.assembleDataset(tdb) : TDBFactory.createDataset(tdb);
                } catch (Exception e) {
                    log.error("Error creating tdb store " + tdb, e);
                    printUsage();
                }
            
			log.info("Requesting " + url);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				log.info("Status OK: " + connection.getResponseCode());
			} else {
				log.error(connection.getResponseCode() + ": Failed to fetch content for URL " + url);
				System.exit(-1);	
			}
		    
			log.info("Parsing content");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// retrieve the document first as a byte array
		    InputStream is = connection.getInputStream();
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    int next = is.read();
		    while (next > -1) {
		        bos.write(next);
		        next = is.read();
		    }
		    bos.flush();

			// we have the document as a byte array
		    // now we can make a string with the content and replace invalid characters
		    // needed because the document may not be valid xml and the sax parser demands it so
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document document =  builder.parse(
		    		new InputSource(new StringReader(
		    				new String(bos.toByteArray()).replaceAll( "&", "&amp;" ))));

			log.info("Extracting information");
			Map<String, String> contentItems = InformationRetriever.extractFrom(document);

		    log.info("Building RDF data");
		    if (dataset != null) {
		        log.info("Writing RDF data to tdb " + tdb);
		        RDFBuilder.createOntModel(url, contentItems, dataset).close();
	            dataset.close();
		    }

		    OntModel model = null;
		    if (file != null || dataset == null)
		        model = RDFBuilder.createOntModel(url, contentItems, null);
		    
            if (file != null) {
                FileWriter fileWriter = null;
                try {
                    log.info("Writing RDF data to file " + file);
                    fileWriter = new FileWriter(file);
                    model.write(fileWriter, "N3");
                } catch (Exception e) {
                    log.error("Error creating file", e);
                    printUsage();
                } finally {
                    try {
                        if (file != null)
                            fileWriter.close();
                    } catch (Exception ignore) {}
                }
            }
            
            if (file == null && dataset == null)
                model.write(System.out, "N3");
            
            if (model != null)
                model.close();
		    
		    log.info("Done");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		    if (dataset != null)
		        dataset.close();
		}
    }
    
    private static void printUsage() {

        log.info("------------------------------------------------------------------------");
        log.info("Usage: ./run.sh (URL|IDS>) [OPTIONS]");
        log.info("");
        log.info("URL");
        log.info("       -u (--url) <url>");
        log.info("              retrieve the information from the url");
        log.info("       For example: ./run.sh -u http://prometheus.uni-koeln.de/pandora/image/show/heidicon_aa-030fe7e5ee5b0ddcc11687e484a044d6181914f8");
        log.info("       If url is given, none of archiveId or itemId may be specified");
        log.info("");
        log.info("IDS");
        log.info("       -a (--archiveId) <archiveId>");
        log.info("              specify a Prometheus image archive");
        log.info("       -i (--itemId) <itemId>");
        log.info("              specify an item within the given image archive");
        log.info("       For example: ./run.sh -a heidicon_aa -i 030fe7e5ee5b0ddcc11687e484a044d6181914f8");
        log.info("       Both archiveId and itemId must be given together, and no url may be specified");
        log.info("");
        log.info("OPTIONS");
        log.info("       -t (--tdb) <tdb>");
        log.info("              specify a tdb dataset to store the retrieved information");
        log.info("              tdb is taken as a pathname. If it ends with .ttl, it is used as a tdb assembler filename");
        log.info("       -f (--file) <filename>");
        log.info("              specify a file to store the retrieved information");
        log.info("------------------------------------------------------------------------");
        System.exit(0);
    }
}
