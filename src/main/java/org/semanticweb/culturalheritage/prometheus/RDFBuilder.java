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

import java.net.URL;
import java.util.Map;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class RDFBuilder {
    
    private static Map<String, String> contentItems = null;
    private static OntModel ontModel = null;

    
    public static OntModel createOntModel(URL url, Map<String, String> contentItems, Dataset dataset) {

        RDFBuilder.contentItems = contentItems;

        ontModel = dataset == null ? ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
                : ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, dataset.getDefaultModel());
        ontModel.addSubModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
                .read("http://erlangen-crm.org/onto/ecrm/ecrm_current.owl"));

        Individual image = createInformationCarrier(url);
        Individual endurant = createEndurant(image);
        createSpatials(endurant);
        createTemporals(endurant);
        
        return ontModel;
    }

    private static Individual createInformationCarrier(URL url) {

        Individual carrier = 
                ontModel.getOntClass(CidocCRM.E84_Information_Carrier)
                .createIndividual(url.toExternalForm());
        
        if (contentItems.get("rights_reproduction-field") != null) {
            Individual right = 
                    ontModel.getOntClass(CidocCRM.E30_Right)
                    .createIndividual(contentItems.get("rights_reproduction-field"));
            carrier.addProperty(ontModel.getProperty(CidocCRM.P104_is_subject_to), right);
            right.addProperty(ontModel.getProperty(CidocCRM.P104i_applies_to), carrier);
        }

        if (contentItems.get("source-field") != null)
            addNote(carrier, "Database: " + contentItems.get("source-field"));

        if (contentItems.get("image_code-field") != null)
            addNote(carrier, "Image Code: " + contentItems.get("image_code-field"));
        
        if (contentItems.get("technique-field")  != null || contentItems.get("artist-field") != null) {
            
            Individual production = 
                    ontModel.getOntClass(CidocCRM.E12_Production)
                    .createIndividual();
            
            if (contentItems.get("technique-field") != null) {
                Individual technique = 
                        ontModel.getOntClass(CidocCRM.E29_Design_or_Procedure)
                        .createIndividual(contentItems.get("technique-field"));
                production.addProperty(ontModel.getProperty(CidocCRM.P33_used_specific_technique), technique);
                technique.addProperty(ontModel.getProperty(CidocCRM.P33i_was_used_by), production);                
            }

            if (contentItems.get("artist-field") != null) {
                Individual artist = 
                        ontModel.getOntClass(CidocCRM.E39_Actor)
                        .createIndividual(contentItems.get("artist-field"));
                production.addProperty(ontModel.getProperty(CidocCRM.P14_carried_out_by), artist);
                artist.addProperty(ontModel.getProperty(CidocCRM.P14i_performed), production);                
            }
        }
        
        Individual image = 
                ontModel.getOntClass(CidocCRM.E38_Image)
                .createIndividual("Image of " + contentItems.get("title-field"));
        carrier.addProperty(ontModel.getProperty(CidocCRM.P128_carries), image);
        image.addProperty(ontModel.getProperty(CidocCRM.P128i_is_carried_by), carrier);

        if (contentItems.get("keyword-field") != null)
            addNote(image, "Keyword: " + contentItems.get("keyword-field"));

        if (contentItems.get("description-field") != null)
            addNote(image, "Description: " + contentItems.get("description-field"));

        return image;
    }

    private static Individual createEndurant(Individual image) {

        Individual endurant = 
                ontModel.getOntClass(CidocCRM.E77_Persistent_Item)
                .createIndividual(contentItems.get("title-field"));
        image.addProperty(ontModel.getProperty(CidocCRM.P138_represents), endurant);
        endurant.addProperty(ontModel.getProperty(CidocCRM.P138i_has_representation), image);

        if (contentItems.get("material-field") != null) {
            Individual material = 
                    ontModel.getOntClass(CidocCRM.E57_Material)
                    .createIndividual(contentItems.get("material-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P45_consists_of), material);
            material.addProperty(ontModel.getProperty(CidocCRM.P45i_is_incorporated_in), endurant);                            
        }
        
        if (contentItems.get("size-field") != null) {
            Individual size = 
                    ontModel.getOntClass(CidocCRM.E54_Dimension)
                    .createIndividual(contentItems.get("size-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P43_has_dimension), size);
            size.addProperty(ontModel.getProperty(CidocCRM.P43i_is_dimension_of), endurant);
        }
        
        if (contentItems.get("credits-field") != null)
            addNote(endurant, "Credits: " + contentItems.get("credits-field"));
        
        if (contentItems.get("discoverycontext-field") != null)
            addNote(endurant, "Discoverycontext: " + contentItems.get("discoverycontext-field"));
        
        if (contentItems.get("inventory_no-field") != null)
            addNote(endurant, "Inventory No: " + contentItems.get("inventory_no-field"));

        if (contentItems.get("inscription-field") != null)
            addNote(endurant, "Inscription: " + contentItems.get("inscription-field"));

        if (contentItems.get("beneficiary_of_charter-field") != null)
            addNote(endurant, "Beneficiary Of Charter: " + contentItems.get("beneficiary_of_charter-field"));

        if (contentItems.get("edition-field") != null)
            addNote(endurant, "Edition: " + contentItems.get("edition-field"));

        if (contentItems.get("issuer_of_charter-field") != null)
            addNote(endurant, "Issuer Of Charter: " + contentItems.get("issuer_of_charter-field"));

        if (contentItems.get("negative_id-field") != null)
            addNote(endurant, "Negative: " + contentItems.get("negative_id-field"));

        if (contentItems.get("number_of_preserved_seals-field") != null)
            addNote(endurant, "Number Of Preserved Seals: " + contentItems.get("number_of_preserved_seals-field"));

        if (contentItems.get("original_number_of_seals-field") != null)
            addNote(endurant, "Original Number Of Seals: " + contentItems.get("original_number_of_seals-field"));

        if (contentItems.get("record_id-field") != null)
            addNote(endurant, "Record: " + contentItems.get("record_id-field"));

        if (contentItems.get("tradition-field") != null)
            addNote(endurant, "Tradition: " + contentItems.get("tradition-field"));

        if (contentItems.get("annotation-field") != null)
            addNote(endurant, "Annotation: " + contentItems.get("annotation-field"));

        if (contentItems.get("subtitle-field") != null)
            addNote(endurant, "Subtitle: " + contentItems.get("subtitle-field"));

        if (contentItems.get("description_source-field") != null)
            addNote(endurant, "Description Source: " + contentItems.get("description_source-field"));

        if (contentItems.get("caption-field") != null)
            addNote(endurant, "Caption: " + contentItems.get("caption-field"));

        if (contentItems.get("keyword_general-field") != null)
            addNote(endurant, "Keyword General: " + contentItems.get("keyword_general-field"));

        if (contentItems.get("pattern-field") != null)
            addNote(endurant, "Pattern: " + contentItems.get("pattern-field"));

        return endurant;
    }

    private static void createSpatials(Individual endurant) {

        if (contentItems.get("location-field") != null) {
            Individual location = 
                    ontModel.getOntClass(CidocCRM.E53_Place)
                    .createIndividual(contentItems.get("location-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P53_has_former_or_current_location), location);
            location.addProperty(ontModel.getProperty(CidocCRM.P53i_is_former_or_current_location_of), endurant);

            Individual type = 
                    ontModel.getOntClass(CidocCRM.E55_Type)
                    .createIndividual("location");
            location.addProperty(ontModel.getProperty(CidocCRM.P2_has_type), type);
            type.addProperty(ontModel.getProperty(CidocCRM.P2i_is_type_of), location);
        }

        if (contentItems.get("location_building-field") != null) {
            Individual location_building = 
                    ontModel.getOntClass(CidocCRM.E53_Place)
                    .createIndividual(contentItems.get("location_building-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P53_has_former_or_current_location), location_building);
            location_building.addProperty(ontModel.getProperty(CidocCRM.P53i_is_former_or_current_location_of), endurant);                

            Individual type = 
                    ontModel.getOntClass(CidocCRM.E55_Type)
                    .createIndividual("location building");
            location_building.addProperty(ontModel.getProperty(CidocCRM.P2_has_type), type);
            type.addProperty(ontModel.getProperty(CidocCRM.P2i_is_type_of), location_building);
        }

        if (contentItems.get("depository-field") != null) {
            Individual depository = 
                    ontModel.getOntClass(CidocCRM.E53_Place)
                    .createIndividual(contentItems.get("depository-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P53_has_former_or_current_location), depository);
            depository.addProperty(ontModel.getProperty(CidocCRM.P53i_is_former_or_current_location_of), endurant);                

            Individual type = 
                    ontModel.getOntClass(CidocCRM.E55_Type)
                    .createIndividual("depository");
            depository.addProperty(ontModel.getProperty(CidocCRM.P2_has_type), type);
            type.addProperty(ontModel.getProperty(CidocCRM.P2i_is_type_of), depository);
        }

        if (contentItems.get("discoveryplace-field") != null) {
            Individual discoveryplace = 
                    ontModel.getOntClass(CidocCRM.E53_Place)
                    .createIndividual(contentItems.get("discoveryplace-field"));
            endurant.addProperty(ontModel.getProperty(CidocCRM.P53_has_former_or_current_location), discoveryplace);
            discoveryplace.addProperty(ontModel.getProperty(CidocCRM.P53i_is_former_or_current_location_of), endurant);                

            Individual type = 
                    ontModel.getOntClass(CidocCRM.E55_Type)
                    .createIndividual("discoveryplace");
            discoveryplace.addProperty(ontModel.getProperty(CidocCRM.P2_has_type), type);
            type.addProperty(ontModel.getProperty(CidocCRM.P2i_is_type_of), discoveryplace);
        }
    }
    
    private static void createTemporals(Individual endurant) {
        
        if (contentItems.get("manufacture_place-field")  != null || contentItems.get("epoch-field") != null 
            || contentItems.get("date-field") != null) {
            
            Individual beginningOfExistence = 
                    ontModel.getOntClass(CidocCRM.E63_Beginning_of_Existence)
                    .createIndividual();
            beginningOfExistence.addProperty(ontModel.getProperty(CidocCRM.P92_brought_into_existence), endurant);
            endurant.addProperty(ontModel.getProperty(CidocCRM.P92i_was_brought_into_existence_by), beginningOfExistence);
            
            if (contentItems.get("manufacture_place-field") != null) {
                Individual manufacture_place = 
                        ontModel.getOntClass(CidocCRM.E53_Place)
                        .createIndividual(contentItems.get("manufacture_place-field"));
                endurant.addProperty(ontModel.getProperty(CidocCRM.P53_has_former_or_current_location), manufacture_place);
                manufacture_place.addProperty(ontModel.getProperty(CidocCRM.P53i_is_former_or_current_location_of), endurant);                
            }
            
            if (contentItems.get("epoch-field") != null || contentItems.get("date-field") != null) {

                Individual timespan = 
                        ontModel.getOntClass(CidocCRM.E52_Time_Span)
                        .createIndividual();
                beginningOfExistence.addProperty(ontModel.getProperty(CidocCRM.P4_has_time_span), timespan);
                timespan.addProperty(ontModel.getProperty(CidocCRM.P4i_is_time_span_of), beginningOfExistence);
                
                if (contentItems.get("epoch-field") != null) {
                    Individual epoch = 
                            ontModel.getOntClass(CidocCRM.E49_Time_Appellation)
                            .createIndividual(contentItems.get("epoch-field"));
                    timespan.addProperty(ontModel.getProperty(CidocCRM.P78_is_identified_by), epoch);
                    epoch.addProperty(ontModel.getProperty(CidocCRM.P78i_identifies), timespan);   
                }
                
                if (contentItems.get("date-field") != null) {
                    Individual date = 
                            ontModel.getOntClass(CidocCRM.E50_Date)
                            .createIndividual(contentItems.get("date-field"));
                    timespan.addProperty(ontModel.getProperty(CidocCRM.P78_is_identified_by), date);
                    date.addProperty(ontModel.getProperty(CidocCRM.P78i_identifies), timespan);              
                }
            }            
        }
    }
    
    private static void addNote(Individual individual, String text) {
        individual.addProperty(ontModel.getProperty(CidocCRM.P3_has_note), ontModel.createLiteral(text));
    }
}
