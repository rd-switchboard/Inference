#CrossRef Importer

This tool is desinged to search and import CrossRef metadata for a selection of DOI's. At this moment only DOI, located in `GraphUtils.PROPERTY_REFERENCE_BY` Node property will be quered, but this will be alteren in the future

#### Requirements

Program requires Java 1.7 and Apache Maven 3.0.5. This will program also reuire an instance of Neo4j 2.2.3 Graph Database and will generate a cache of requests on the local HDD.

Program has been tested on Ubuntu Linux 14.04 and should work on any other linux as well

#### Build and ussage

To build the program run `mvn package -am` from the `Importers/CrossRef/import_crossref` project forlder 
or run `mvn package -pl :import_crossref -am` from the main repository folder. Or to build everything,
simple run `mvn package` from the main repository folder.

```
cd Importers/CrossRef/import_crossref
mvn package -am
```

The compiled program will be avaliable in `Importers/CrossRef/import_crossref/target/import_crossref-${program.version}.jar`. 
All dependancies will be copied into `Importers/CrossRef/import_crossref/target/jars`. 

To install, copy the main jar and depending jars into desired location.

To stat the program, execute `java -jar import_crossref-${program.version} [${properties.file}]`. The properties file name
is optional, if ommited, the `properties/import_crossref.properties` file will be used. The properties file is a simple text 
file with properties list, set as `property.name=property.value` and can be created using any text file edit program 
(for example `vi`). 

List of supported properties are: 

* neo4j: Path to a neo4j folder what will be used for import the data. The Neo4j Database will be used in exclusive mode and can not be 
accesed from any other process during the import time.

* crossref: A path to a folder where CrossRef cache will be located. Default is: `crossref/cahce`

We also recommed to start the import process as a batch process with other imports or to run it as a linux daemon process, so it will not 
be interrupted. For example:

```
nohup java -jar import_crossref-1.3.0.jar properties/import_crossref.properties >logs/import_crossref.txt 2>&1 &
```



