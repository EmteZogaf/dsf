#!/bin/bash

echo datasharingframework/bpe ...
docker build --pull -t datasharingframework/bpe ../dsf-bpe/dsf-bpe-server-jetty/docker

echo datasharingframework/fhir ...
docker build --pull -t datasharingframework/fhir ../dsf-fhir/dsf-fhir-server-jetty/docker

echo datasharingframework/fhir_proxy ...
docker build --pull -t datasharingframework/fhir_proxy ../dsf-docker/fhir_proxy
