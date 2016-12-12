FROM openjdk:8u111-alpine
MAINTAINER Michał Michaluk <michal.michaluk@bottega.com.pl>

ARG db_url
ARG db_user
ARG db_pass
ENV db_url ${db_url}
ENV db_user ${db_user}
ENV db_pass ${db_pass}

ADD target/warehouse-app.jar /lib/
EXPOSE 4567
CMD java -jar /lib/warehouse-app.jar "${db_url}" "${db_user}" "${db_pass}"
