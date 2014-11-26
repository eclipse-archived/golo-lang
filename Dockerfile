FROM azul/zulu-openjdk:8
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get install -y maven rake
RUN apt-get autoclean

ADD . /src
RUN cd /src && rake special:bootstrap
RUN mkdir -p /opt/golo && cp -R /src/target/golo-*-distribution/golo-*/* /opt/golo && ln -s /opt/golo/bin/golo /usr/bin/golo
RUN rm -rf /src ~/.m2
