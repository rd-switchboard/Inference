# Inference

This repository contains all Inference engine code of the RD-Switchboard, that is excutable on EC2 machines. 
The repository contains separate java applications as well as libraries, used by these applications. 

#### Requirements

Program requires Java 1.7 and Apache Maven 3.0.5. It will also require Neo4j 2.2.3.

Program has been tested on Ubuntu Linux 14.04 and should work on any other linux as well


#### Project structure

Inference enginge consists from a selection of different java modules united into one global Maven project, 
located in the main Repository folder. Part of these modules are libraries used by other modules and remaining 
modules are separate applications, designed to perform different taks. All applications will require one or two 
Neo4j instances to be avaliable and will use them in the exclusive mode, so no two applications can work at the 
same time. The Inefence Engine suppose to run them one by one in some batch process and such design allows 
to exclude some tasks from the batch to save time and resources.

Project modules and data are separated into different folders to make navigation between them easy.

* Build: Project Distribution module
* Data: Static data, incliding list of institutions, patterns, sevices, arc and nhmrc grants and test nodes
* Importers: All modules, designed to import data into Neo4j from different Data Sources 
* Importers/ANDS: [ANDS Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/ANDS/import_ands)
* Importers/ARC: [ARC Grants Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/ARC/import_arc)
* Importers/Cern: [CERN Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/Cern/import_cern)
* Importers/CrossRef: [CrossRef Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/CrossRef/import_crossref)
* Importers/DaRa: [Da-Ra Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/DaRa/import_dara)
* Importers/Dryad: [Dryad Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/Dryad/import_dryad)
* Importers/NHMRC: [NHMRC Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/NHMRC/import_nhmrc)
* Importers/ORCID: [ORCID Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/ORCID/import_orcid)
* Importers/OpenAIRE: [OpenAIRE Metadata Import Module](https://github.com/rd-switchboard/Inference/tree/master/Importers/OpenAIRE/import_openaire)
* Importers/Web: Static Metadata Import Module ([Institutions](https://github.com/rd-switchboard/Inference/tree/master/Importers/Web/import_institutions), [Patterns](https://github.com/rd-switchboard/Inference/tree/master/Importers/Web/import_patterns) and -Services-)
* Libraries: All Libraries, used by other Modules
* Libraries/CrossRef: CrossRef API Libarary
* Libraries/DDI: DDI Metadata Crosswalk (not used at this moment because of insufficient metadata)
* Libraries/DLI: DLI Metadata Crosswalk (used by OpenAIRE)
* Libraries/DaRa: DaRa Metadata Crosswalk (used by DaRa)
* Libraries/Fuzzy: Fuzzy Searh Library 
* Libraries/Google: Google CSE and Google Cache Libraries
* Libraries/Marc21: Marc21 Metadata Crosswalk (used by CERN)
* Libraries/Mets: Mets Metadata Crosswalk (used by Dryad)
* Libraries/ORCID: ORCID API Library
* Libraries/RIF_CS: RIF:CS Metadata Crosswalk (used by ANDS)
* Libraries/Scopus: Scopus API Library
* Libraries/graph_utils: [Graph Representation Library used in RD-Switchboard project](https://github.com/rd-switchboard/Inference/tree/master/Libraries/graph_utils)
* Libraries/neo4j_utils: [Neo4j Database Helper Library used in RD-Switchboard project](https://github.com/rd-switchboard/Inference/tree/master/Libraries/neo4j_utils)
* Linkers: Modules used to link nodes from different data sources
* Linkers/Google: Will link different data sources with Web:Researcher nodes, found by Google CSE
* Linkers/Static: Will link different data sources by existsing metadata (ODCID ID, Scopus ID, DOI etc)
* Obsolete: Code no longer used by Engine, but witch migth be used in the future
* Search: Search Engines Modules
* Search/Google: Module to search texts in Google CSE and generate Google cache
* Utils: Different Helper Applications
* Utils/Neo4j: Neo4j Applications
* Utils/Neo4j/copy_harmonized: Modle will generate Harmonized Neo4j Instance (Nexus) from Aggrigation Neo4j Instance
* Utils/Neo4j/delete_nodes: Module will delete orphant nodes (the nodes which do not have any connections to the other nodes
* Utils/Neo4j/export_graph_json: Module will export finished graphs into S3

#### Build and manage

To build the whole project simply run `mvn package` from the repository folder. The Maven will download all required dependancied and will build all existing modules. It will also generate distribution in the Build/distribution/target/inference-${project.version} folder and will produce gz and bz2 archives with this distribution. If archives or assemble folder are not required, it can be disabled in the assemble configuration located at Build/distribution/src/assembly/bin.xml

To install porject into your local maven repository, execute `mvn install` from the repository folder. After that you will be able to build any module separately by executint mvn package in the module folder, but, if one of depending modules has been changed, new installation of this module will be required.

You also can build single module without installing it, by executing Maven command `mvn install -pl :${module.name} -am` from the repository folder. For example, to build only ANDS import module, you can execute:

```
mvn install -pl import_ands -am
```

To change project version, execute `mvn versions:set -DgenerateBackupPoms=false` from the main repository folder and enter new version.

#### Distribute

If Distribute module has been compiled, the Maven will create global distribution with all modules and all dependacies located in Build/distribution/target/inference-${project.version}. It will also create bz2 and gz archives of this distribution. We recommend to upload whole archive on the server and unpack it there. The bz2 version is usally a bit smaller but will require more time to unpack. You can use either of them or create your own archive by zipping the distribution folder. 

#### Installation

You will need to install at least two neo4j databases - aggregator and nexus. You can download neo4j from the [official Neo4j Web site](http://neo4j.com/artifact.php?name=neo4j-community-2.2.3-unix.tar.gz)

Copy archive to the server and unpack it:

```
tar -xzvf neo4j-community-2.2.3-unix.tar.gz
cp neo4j-community-2.2.3-unix neo4j-aggregator
cp neo4j-community-2.2.3-unix neo4j-nexus
```

Next, unpack the Inference archive, by executing `tar -xzvf inference-${project.version}.tar.bz2` for bz2 or `tar -xjvf inference-${project.version}.tar.gz` for gzip. Replace ${project.version} with actual project version:

```
tar -xzvf inference-1.3.0.tar.gz
```

#### Configuration

The distribution will have `properties` folder where all properties files will be located. Each Module should have at lease one configuration file. Please refer to each module documentation to learn about possible configuration options and how you can modify them. You can have more that one properties file for each module with different configuration. To execute them, you can add path to a configuration file as parameter to the jar file.

#### Execution

All executable modules can be executed by calling java: `java -jar ${module.name}-${module.version} [${optinal.path.to.properties.file}]`. The output of the program can be directed to the log file and program itself could be run as a daemon, allows you to monitor the process without interfere to the program work. We recommend to add `nohup` keywoard before calling the Java, that will ensure, that program will finish its work even if your connection with server will be terminated. 

For example, to execute ANDS import with custom configuration file and without interruptions, you can use this command 

```
nohup java -jar import_ands-1.3.0.jar properties/import_ands/properties > logs/import_ands.txt 2>&1 &
``` 

We also recommend to combine all programs into some batch process so they would be called one by one. A shell script will be most siutable for that. A sample of such shell script is provided in the Scripts folder



