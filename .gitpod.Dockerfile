FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y apt-utils && \
    #apt-get install -y gnupg2 && \
    apt install -y openjdk-8-jdk && \
    apt install -y maven && \
    apt install -y mercurial && \
    apt install -y git && \
    apt-get -y install locales  && \
    apt-get clean

# Set the locale
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# GOLO_HOME
RUN echo "export GOLO_HOME=/build/install/golo" >> ~/.bashrc  && \
    echo "export PATH=\$PATH:\$GOLO_HOME/bin" >> ~/.bashrc
