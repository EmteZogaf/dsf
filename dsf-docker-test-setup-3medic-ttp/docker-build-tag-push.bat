@echo off

REM For installing / starting local registry at registry:5000 see test_setup.txt
REM See https://docs.docker.com/registry/insecure/ for infos on pushing to / pulling from a local insecure registry

echo highmed/bpe ...
docker build --pull -t highmed/bpe ..\dsf-bpe\dsf-bpe-server-jetty\docker
docker tag highmed/bpe:latest registry:5000/highmed/bpe:latest
docker push registry:5000/highmed/bpe

echo highmed/fhir ...
docker build --pull -t highmed/fhir ..\dsf-fhir\dsf-fhir-server-jetty\docker
docker tag highmed/fhir:latest registry:5000/highmed/fhir:latest
docker push registry:5000/highmed/fhir

echo highmed/fhir_proxy ...
docker build --pull -t highmed/fhir_proxy ..\dsf-docker\fhir_proxy
docker tag highmed/fhir_proxy:latest registry:5000/highmed/fhir_proxy:latest
docker push registry:5000/highmed/fhir_proxy
