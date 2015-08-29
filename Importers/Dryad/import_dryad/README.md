#Dryad Importer

This tool is desinged to import Dryad XML filed from provided S3 Bucket into Neo4j Database.

#### Requirements

Program requires Java 1.7 and Apache Maven 3.0.5. This will program also reuire an instance of Neo4j 2.2.3 Graph Database and access to AWS S3 Bucket with harvested Dryad OAI:PMH/Mets XML files.

Program has been tested on Ubuntu Linux 14.04 and should work on any other linux as well

#### Build and ussage

To build the program run `mvn package -am` from the `Importers/Dryad/import_dryad` project forlder 
or run `mvn package -pl :import_dryad -am` from the main repository folder:

```
cd Importers/Dryad/import_dryad
mvn package -am
```

The compiled program will be avaliable in `Importers/Dryad/import_dryad/target/import_dryad-${program.version}.jar`. 
All dependancies will be copied into `Importers/Dryad/import_dryad/target/jars`. 

To install, copy the main jar and depending jars into desired location.

To stat the program, execute `java -jar import_dryad-${program.version} [${properties.file}]`. The properties file name
is optional, if ommited, the `properties/import_dryad.properties` file will be used. The properties file is a simple text 
file with properties list, set as `property.name=property.value` and can be created using any text file edit program 
(for example `vi`)

List of supported properties are: 

* s3.bucket: Name of AWS S3 Bucket (for example xml.rd-switchboard)

* s3.prefix: A path prefix to the Dryad XML files within a bucket (usually /dryad/mets/)

* neo4j: Path to a neo4j folder what will be used for import the data. The Neo4j Database will be used in exclusive mode and can not be 
accesed from any other process during the import time.

We also recommed to start the import process as a batch process with other imports or to run it as a linux daemon process, so it will not 
be interrupted. For example:

```
nohup java -jar import_dryad-1.3.0.jar properties/import_dryad.properties >logs/import_dryad.txt 2>&1 &
```



