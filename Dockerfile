FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.3.2

USER root

# Copy certs from your project directory to the shared directory within the container
COPY certs/ /usr/local/share/ca-certificates/

# Import certs into the Java keystore for Sonar CLI
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_1 -file /usr/local/share/ca-certificates/Salesforce_Internal_GIA_Root_CA_1.pem -cacerts -storepass changeit -noprompt
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_4 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_4.pem -cacerts -storepass changeit -noprompt
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_3 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_3.pem -cacerts -storepass changeit -noprompt

# Update CA certificates for general system use
RUN update-ca-certificates

USER jenkins
WORKDIR /home/jenkins
