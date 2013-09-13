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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class InformationRetriever {

	private static Logger log = LoggerFactory.getLogger(InformationRetriever.class);
	
	private static final Map<String, String> contentItems = new HashMap<String, String>();
	
	static {
		// All
		contentItems.put("artist-field", null);						// Artist
		contentItems.put("title-field", null);						// Title
		contentItems.put("location-field", null);					// Location
		contentItems.put("discoveryplace-field", null);             // Discoveryplace
		contentItems.put("material-field", null);					// Material
		contentItems.put("keyword-field", null);					// Material
		contentItems.put("description-field", null);				// Description
		contentItems.put("date-field", null);						// Date
		contentItems.put("epoch-field", null);						// Epoch
		contentItems.put("credits-field", null);					// Credits
		contentItems.put("location_building-field", null);			// Location Building
		contentItems.put("depository-field", null);					// Depository
		contentItems.put("discoverycontext-field", null);          // Discoverycontext
		contentItems.put("inventory_no-field", null);				// Inventory No
		contentItems.put("manufacture_place-field", null);			// Manufacture Place
		contentItems.put("rights_reproduction-field", null);		// Rights (Photograph/Reproduction)
		contentItems.put("size-field", null);						// Size
		contentItems.put("source-field", null);						// Database
		
		// Some
		contentItems.put("inscription-field", null);				// Inscription
		contentItems.put("technique-field", null);					// Technique

		// marburg_lba
		contentItems.put("beneficiary_of_charter-field", null);		// Beneficiary Of Charter
		contentItems.put("edition-field", null);					// Edition
		contentItems.put("issuer_of_charter-field", null);			// Issuer Of Charter
		contentItems.put("negative_id-field", null);				// Negative
		contentItems.put("number_of_preserved_seals-field", null);	// Number Of Preserved Seals
		contentItems.put("original_number_of_seals-field", null);	// Original Number Of Seals
		contentItems.put("record_id-field", null);					// Record
		contentItems.put("tradition-field", null);					// Tradition
		
		// giessen_lri
		contentItems.put("annotation-field", null);					// Annotation
		contentItems.put("image_code-field", null);					// Image Code
		contentItems.put("subtitle-field", null);					// Subtitle
		
		// ppo
		contentItems.put("description_source-field", null);			// Description Source
		contentItems.put("caption-field", null);					// Caption
		contentItems.put("keyword_general-field", null);			// Keyword General
		contentItems.put("pattern-field", null);					// Pattern		
	}

	public static Map<String, String> extractFrom(Document document) {
		collect(document);
		return contentItems;
	}
	
	private static void collect(Node node) {
		if (node == null)
			return;
		if (node.getNodeName().equals("td")) {
			Node classAttribute = node.getAttributes().getNamedItem("class");
			if (classAttribute == null)
				return;
			if (contentItems.containsKey(classAttribute.getNodeValue()))
					contentItems.put(classAttribute.getNodeValue(), node.getTextContent().trim());
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++ )
			collect(node.getChildNodes().item(i));
	}
	
	public static void printRetrievedItems() {
		log.info("------------------------------------------------------------------------");
		for (String key : contentItems.keySet())
			if (contentItems.get(key) != null)
				log.info(key + " - " + contentItems.get(key));
		log.info("------------------------------------------------------------------------");
	}
}
