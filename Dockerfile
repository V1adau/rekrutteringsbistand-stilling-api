FROM navikt/java:17
COPY nais/import-vault-token.sh /init-scripts
COPY /target/rekrutteringsbistand-stilling-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9501
