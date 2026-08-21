# fhir-sandbox
#
# docker build -t octri.ohsu.edu/fhir-sandbox --rm=true --pull .

FROM octri.ohsu.edu/jarrunner:17
EXPOSE 8080
ENV SERVER_PORT=8080
COPY --chown=svcoctrikube:octrikube target/fhir-sandbox.jar /app.jar
USER svcoctrikube