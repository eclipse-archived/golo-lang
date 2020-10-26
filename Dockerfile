FROM azul/zulu-openjdk:8 AS builder
ADD . /src
WORKDIR /src
RUN ./gradlew installDist

FROM azul/zulu-openjdk-alpine:8
COPY --from=builder /src/build/install/golo /opt/golo
ENV PATH=/opt/golo/bin:/opt/golo/share/shell-completion/:$PATH
ENV MANPATH=/opt/golo/man:$MANPATH
CMD [ "golo" ]
