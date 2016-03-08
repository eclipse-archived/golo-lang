FROM azul/zulu-openjdk:8
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN apt-get update &&\
  apt-get install -y git &&\
  apt-get autoclean

ADD . /src
RUN cd /src && ./gradlew -v

RUN cd /src && ./gradlew installDist

RUN cp -R /src/build/install/golo-incubation /opt/golo &&\
  ln -s /opt/golo/bin/golo /usr/bin/golo &&\
  cd /src && ./gradlew clean
