FROM azul/zulu-openjdk:8
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN apt-get update &&\
  apt-get -y upgrade &&\
  apt-get install -y git &&\
  apt-get autoclean

ADD . /src
RUN cd /src && ./gradlew -v

RUN cd /src && ./gradlew installDist

RUN mkdir -p /opt/golo &&\
  cp -R /src/build/install/golo /opt &&\
  ln -s /opt/golo/bin/golo /usr/bin/golo &&\
  cd /src && ./gradlew clean
