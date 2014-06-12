FROM ubuntu:14.04
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get -y install software-properties-common
RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-get update

RUN (echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections)
RUN apt-get install -y oracle-java8-installer oracle-java8-set-default

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV PATH $JAVA_HOME/bin:$PATH
RUN apt-get install -y maven rake
RUN apt-get autoclean

ADD . /src
RUN cd /src && rake special:bootstrap
RUN mkdir -p /opt/golo && cp -R /src/target/golo-*-distribution/golo-*/* /opt/golo && ln -s /opt/golo/bin/golo /usr/bin/golo
RUN rm -rf /src ~/.m2
