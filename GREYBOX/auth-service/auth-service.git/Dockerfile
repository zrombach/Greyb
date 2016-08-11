FROM java:8

EXPOSE 8080 8443 8081

WORKDIR /usr/share/authport


COPY build_output/authViaREST-0.0.1-SNAPSHOT.jar /usr/share/authport/bin/authViaREST-0.0.1-SNAPSHOT.jar

COPY authViaREST/src/main/resources/authServiceConfig.yml 
/usr/share/authport/config/authServiceConfig.yml

COPY authVia
REST/testcerts/keystore.jks  /usr/share/authport/testcerts/keystore.jks


ENTRYPOINT ["java", "-jar", "bin/authViaREST-0.0.1-SNAPSHOT.jar"]


CMD ["server", "config/authServiceConfig.yml"]
