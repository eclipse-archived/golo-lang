FROM azul/zulu-openjdk:8 AS builder
ADD . /src
WORKDIR /src
RUN ./gradlew installDist

FROM azul/zulu-openjdk-alpine:8
COPY --from=builder /src/build/install/golo /opt/golo
RUN ln -s /opt/golo/bin/golo /usr/bin/golo
CMD [ "golo" ]
