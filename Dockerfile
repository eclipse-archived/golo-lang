FROM azul/zulu-openjdk:8
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN apt-get update &&\
  apt-get -y upgrade &&\
  apt-get install -y maven rake &&\
  apt-get autoclean

ADD . /src
RUN cd /src &&\
  rake special:bootstrap &&\
  mkdir -p /opt/golo &&\
  cp -R /src/target/golo-*-distribution/golo-*/* /opt/golo &&\
  ln -s /opt/golo/bin/golo /usr/bin/golo &&\
  rm -rf /src ~/.m2
