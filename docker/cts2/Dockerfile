# Get the base image to Ubuntu
FROM tomcat:8.0.39-jre8

ADD cts2.war /usr/local/tomcat/webapps/

ADD setenv.sh /usr/local/tomcat/bin/

ADD config/* /root/.cts2/cts2/config/

CMD ["catalina.sh", "run"]