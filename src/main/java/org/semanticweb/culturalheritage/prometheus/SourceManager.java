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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceManager {

	private static Logger log = LoggerFactory.getLogger(SourceManager.class);
	
	private static final Properties archives = new Properties();
	
	static {
	    try {
	        InputStream stream = SourceManager.class.getClassLoader().getResourceAsStream("prometheus-archives.properties");
	        archives.load(stream);
            stream.close();
        } catch (IOException ioe) {
            log.error("Error retrieving Prometheus archive list from properties file \"prometheus-archives.properties\"", ioe);
	    }
	}

	public static boolean containsRepository(String name) throws ClassNotFoundException {
		return archives.containsKey(name);
	}

	public static String getDescriptionFor(String name) throws ClassNotFoundException {
		return (String) (archives.containsKey(name) ? archives.get(name) : null);
	}

	public static void printArchiveList() {
		log.info("------------------------------------------------------------------------");
		for (Object key : archives.keySet())
			log.info(key + " - " + archives.get(key));
		log.info("------------------------------------------------------------------------");
	}
}
