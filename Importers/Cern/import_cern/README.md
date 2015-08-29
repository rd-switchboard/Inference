#Cern Importer

This tool is desinged to import Cern OAI:PMH/Marc21 XML files from provided S3 Bucket into Neo4j Database.

#### Requirements

Program requires Java 1.7 and Apache Maven 3.0.5. This will program also reuire an instance of Neo4j 2.2.3 Graph Database and access to AWS S3 Bucket with harvested Cern OAI:PMH/Marc21 XML files.

Program has been tested on Ubuntu Linux 14.04 and should work on any other linux as well

#### Build and ussage

To build the program run `mvn package -am` from the `Importers/Cern/import_cern` project forlder 
or run `mvn package -pl :import_cern -am` from the main repository folder. Or to build everything,
simple run `mvn package` from the main repository folder.

```
cd Importers/Cern/import_cern
mvn package -am
```

The compiled program will be avaliable in `Importers/Cern/import_cern/target/import_cern-${program.version}.jar`. 
All dependancies will be copied into `Importers/Cern/import_cern/target/jars`. 

To install, copy the main jar and depending jars into desired location.

To stat the program, execute `java -jar import_cern-${program.version} [${properties.file}]`. The properties file name
is optional, if ommited, the `properties/import_cern.properties` file will be used. The properties file is a simple text 
file with properties list, set as `property.name=property.value` and can be created using any text file edit program 
(for example `vi`)

List of supported properties are: 

* s3.bucket: Name of AWS S3 Bucket (for example xml.rd-switchboard)

* s3.prefix: A path prefix to the Cern XML files within a bucket (usually /cern/marcxml/)

* neo4j: Path to a neo4j folder what will be used for import the data. The Neo4j Database will be used in exclusive mode and can not be 
accesed from any other process during the import time.

We also recommed to start the import process as a batch process with other imports or to run it as a linux daemon process, so it will not 
be interrupted. For example:

```
nohup java -jar import_cern-1.3.0.jar properties/import_cern.properties >logs/import_cern.txt 2>&1 &
```



