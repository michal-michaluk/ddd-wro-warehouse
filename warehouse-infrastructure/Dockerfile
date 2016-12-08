FROM openjdk:8u111-alpine
MAINTAINER Michał Michaluk <michal.michaluk@bottega.com.pl>
ADD target/warehouse-rest.jar /lib/

EXPOSE 4567
CMD ["java", "-jar", "/lib/warehouse-rest.jar"]
