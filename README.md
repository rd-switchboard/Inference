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
* Importers/Web: Static Metadata Import Module ([Institutions](https://github.com/rd-switchboard/Inference/tree/master/Importers/Web/import_institutions), [Patterns](https://github.com/rd-switchboard/Inference/tree/master/Importers/Web/import_patterns) and --Services--)
* Libraries: All Libraries, used by other Modules
* Libraries/CrossRef: [CrossRef API Libarary](https://github.com/rd-switchboard/Inference/tree/master/Libraries/CrossRef/crossref_utils)
* Libraries/DDI: [DDI Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/DDI/crosswalk_ddi) (not used at this moment because of insufficient metadata)
* Libraries/DLI: [DLI Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/DLI/crosswalk_dli) (used by OpenAIRE)
* Libraries/DaRa: [DaRa Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/DaRa/crosswalk_dara) (used by DaRa)
* Libraries/Fuzzy: [Fuzzy Searh Library](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Fuzzy/fuzzy_search)
* Libraries/Google: [Google CSE](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Google/google_cse) and [Google Cache](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Google/google_cache) Libraries
* Libraries/Marc21: [Marc21 Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Marc21/crosswalk_marc21) (used by CERN)
* Libraries/Mets: [Mets Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Mets/crosswalk_mets) (used by Dryad)
* Libraries/ORCID: [ORCID API Library](https://github.com/rd-switchboard/Inference/tree/master/Libraries/ORCID/orcid_utils)
* Libraries/RIF_CS: [RIF:CS Metadata Crosswalk](https://github.com/rd-switchboard/Inference/tree/master/Libraries/RIF_CS/crosswalk_rif_cs) (used by ANDS)
* Libraries/Scopus: [Scopus API Library](https://github.com/rd-switchboard/Inference/tree/master/Libraries/Scopus/scopus_utils)
* Libraries/graph_utils: [Graph Representation Library used in RD-Switchboard project](https://github.com/rd-switchboard/Inference/tree/master/Libraries/graph_utils)
* Libraries/neo4j_utils: [Neo4j Database Helper Library used in RD-Switchboard project](https://github.com/rd-switchboard/Inference/tree/master/Libraries/neo4j_utils)
* Linkers: Modules used to link nodes from different data sources
* Linkers/Google: [Web Researcher Linker](https://github.com/rd-switchboard/Inference/tree/master/Linkers/Google/link_web_researchers) Will link different data sources with Web:Researcher nodes, found by Google CSE
* Linkers/Static: [Node Linker](https://github.com/rd-switchboard/Inference/tree/master/Linkers/Static/link_nodes) Will link different data sources by existsing metadata (ODCID ID, Scopus ID, DOI etc)
* Obsolete: Code no longer used by Engine, but witch migth be used in the future
* Search: Search Engines Modules
* Search/Google: [Google Search](https://github.com/rd-switchboard/Inference/tree/master/Search/Google/google_search) Module to search texts in Google CSE and generate Google cache
* Scripts: Folder will contain Inference scripts samples.
* Utils: Different Helper Applications
* Utils/Neo4j: Neo4j Applications
* Utils/Neo4j/copy_harmonized: [Copy Harmonized](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/copy_harmonyzed) Modle will generate Harmonized Neo4j Instance (Nexus) from Aggrigation Neo4j Instance
* Utils/Neo4j/delete_nodes: [Delete Orphant Nodes](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/delete_nodes) Module will delete orphant nodes (the nodes which do not have any connections to the other nodes
* Utils/Neo4j/export_graph_json: [Export Graph JSON](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/export_graph_json) Module will export finished graphs into S3
* Utils/Neo4j/export_keys: [Export Keys](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/export_keys) Module will export all existing keys, used to synchronize RDS records with graphs
* Utils/Neo4j/replace_source: [Replace Source](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/replace_source) Module will replace source name with another
* Utils/Neo4j/test_connections: [Test connections](https://github.com/rd-switchboard/Inference/tree/master/Utils/Neo4j/test_connections) Module will test existing connections and generate report



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

#### Execution Order

Some tasks must be executed in a specyfed order. We recommend to run all import applicatios first, then run search application, linking applications and export application.

Suggested run order will be:

* import_institutions : To import predefined institutions nodes
* import_patterns: To import predefined search patterns
* import_arc: To import ARC grants
* import_nhmrc: To import NHMRC grants
* import_ands: To import ANDS records
* import_dryad: To import Dryad records
* import_cern: To import CERN records
* import_orcid: To import ORCID records
* import_dara: To import DaRa records
* import_openaire: To import DLI records
* import_crossref: To search and import crossref records
* google_search: To search ANDS grant titles in Google
* link_nodes: To link all existing nodes by DOI, ORCID ID, etc
* link_web_researchers: To link nodes with Web:Researcher nodes
* copy_harmonyzed: To copy harmonyzed data into Nexus Neo4j
* delete_nodes: To delete orpant nodes from Nexus Neo4j
* test_connections : To test Nexus Neo4j existing connections numbers
* export_graph_json: To export final graphs
* export_keys: To export existing keys

Please be aware, what all this programs will require an explict access to the Neo4j database, therefore no two programs can run at the same time on the same database. The google_search software will only be able to process 10000 requests per day due to Google CSE limitations. The google_search is the only program who will access Neo4j in a read-only mode and will not make any changed in the Neo4j database, storing all found information into a Cache instead. Therefore google_search can use a copy of the Neo4j database and run as soon as you have imported data sources, selected for a Google Search. That will allow you to build the rest of the database while Google Search will be executed and save a dicent ammount of time. 



