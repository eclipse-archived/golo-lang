FROM azul/zulu-openjdk-alpine:8
MAINTAINER Julien Ponge <julien.ponge@insa-lyon.fr>

RUN apk add --no-cache

ADD . /src

RUN cd /src &&\
  ./gradlew -v &&\
  ./gradlew installDist &&\
  mkdir -p /opt/golo &&\
  cp -R /src/build/install/golo/* /opt/golo &&\
  ln -s /opt/golo/bin/golo /usr/bin/golo &&\
  rm -rf /src /root/.gradle
