prometheus2crm
==============

Web API Java Application for the Extraction of CIDOC CRM based RDF from Prometheus Image Archive

The application can be used to extract information from the pages of the Prometheus Image Archive.
It takes as a command line parameter either a URL of a provided image page or an archive id and an item id 
(Run the application without parameters for details).

It then retrieves the page and extracts the image's metadata from the HTML using an XML parser.
The extracted information is then used to create an RDF description of the image and it's metadata.
The description uses the CIDOC CRM ontology for modelling (see http://www.cidoc-crm.org for details) 
and the application uses the OWL DL implementation of the CIDOC CRM from http://erlangen-crm.org/current/ 
for creating this description. A diagram showing the mapping is provided with the prometheus2crm.pdf document. 
The application will create a serialization for the RDF data using the N3 format. The user has the option 
to provide a Jena TDB store or a file name using command line options. Both tdb and file may be provided 
at the same time. If none is provided, the application will output the result to standard out. 

Building:
The project files retrieved from the repository contain the source as well as a pre-built package, 
so the program can be used immediately without building it first.
For building the program, install maven (http://maven.apache.org/) and run 'mvn clean package'.

Running:
To run the program, execute the run.sh script (Linux) or the run.bat script (Windows) accordingly.
If you do not provide any arguments, the application will show you a usage description.
