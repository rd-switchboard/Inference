#Institutions Importer

This tool is desinged to import Web:Institution Nodes from a provided CSV file.

#### Requirements

Program requires Java 1.7 and Apache Maven 3.0.5. This will program also reuire an instance of Neo4j 2.2.3 Graph Database and a CSV file with Web:Institution Nodes information.

Program has been tested on Ubuntu Linux 14.04 and should work on any other linux as well

#### Build and ussage

To build the program run `mvn package -am` from the `Importers/Web/import_institutions` project forlder 
or run `mvn package -pl :import_institutions -am` from the main repository folder. Or to build everything,
simple run `mvn package` from the main repository folder.

```
cd Importers/Web/import_institutions
mvn package -am
```

The compiled program will be avaliable in `Importers/Web/import_institutions/target/import_institutions-${program.version}.jar`. 
All dependancies will be copied into `Importers/Web/import_institutions/target/jars`. 

To install, copy the main jar and depending jars into desired location.

To stat the program, execute `java -jar import_institutions-${program.version} [${properties.file}]`. The properties file name
is optional, if ommited, the `properties/import_institutions.properties` file will be used. The properties file is a simple text 
file with properties list, set as `property.name=property.value` and can be created using any text file edit program 
(for example `vi`)

List of supported properties are: 

* neo4j: Path to a neo4j folder what will be used for import the data. The Neo4j Database will be used in exclusive mode and can not be 
accesed from any other process during the import time.

* institutions: Path to a CSV file with Web:Institution Nodes information. Default is `data/institutions.csv`

We also recommed to start the import process as a batch process with other imports or to run it as a linux daemon process, so it will not 
be interrupted. For example:

```
nohup java -jar import_institutions-1.3.0.jar properties/import_institutions.properties >logs/import_institutions.txt 2>&1 &
```



