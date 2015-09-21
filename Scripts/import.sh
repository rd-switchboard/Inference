#!/bin/bash

#rm *.txt

java -jar import_institutions-1.2.0.jar >import_institutions.txt 2>&1
java -jar import_patterns-1.2.0.jar >import_patterns.txt 2>&1
java -jar import_arc-1.2.0.jar >import_arc.txt 2>&1
java -jar import_nhmrc-1.2.0.jar >import_nhmrc.txt 2>&1
java -jar import_ands-1.2.0.jar >import_ands.txt 2>&1
java -jar import_dryad-1.2.0.jar >import_dryad.txt 2>&1
java -jar import_crossref-1.2.0.jar >import_crossref.txt 2>&1
java -jar import_orcid-1.2.0.jar >import_orcid.txt 2>&1
java -jar import_cern-1.2.0.jar >import_cern.txt 2>&1
java -jar import_openaire-1.2.0.jar >import_openaire.txt 2>&1
java -jar link_nodes-1.2.0.jar >link_nodes.txt 2>&1
java -jar link_web_researchers-1.2.0.jar >link_web_researchers.txt 2>&1
