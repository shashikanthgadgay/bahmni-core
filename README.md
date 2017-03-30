# OpenMRS module bahmnicore

[![Build Status](https://travis-ci.org/shashikanthgadgay/bahmni-core.svg?branch=master)](https://travis-ci.org/shashikanthgadgay/bahmni-core)

This module provides necessary services for running Bahmni

## Build

Clone the repository and build the omod
   
    git clone https://github.com/bahmni/bahmni-core
    cd bahmni-core
    mvn clean install
    
## Deploy

Copy ```bahmni-core/bahmnicore-omod/target/bahmnicore-omod-VERSION-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS
