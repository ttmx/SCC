FROM tomcat:9.0-jdk11-openjdk
WORKDIR /usr/local/tomcat
ADD ./target/scc2122-p1-1.0.war webapps
EXPOSE 8080
