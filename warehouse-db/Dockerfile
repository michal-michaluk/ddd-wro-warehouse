FROM postgres:10
MAINTAINER Michał Michaluk <michal.michaluk@bottega.com.pl>

ADD schema /schema
ADD initdb.sql /docker-entrypoint-initdb.d/
ADD start-and-migrate.sh /
RUN chmod +x /start-and-migrate.sh

ADD http://central.maven.org/maven2/org/liquibase/liquibase-core/3.4.2/liquibase-core-3.4.2.jar /lib/liquibase.jar
ADD http://central.maven.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar /lib/postgresql.jar
RUN chmod a+r /lib/liquibase.jar
RUN chmod a+r /lib/postgresql.jar

RUN apt-get update -y
RUN apt-get install openjdk-8-jre-headless -y

CMD /start-and-migrate.sh postgres
