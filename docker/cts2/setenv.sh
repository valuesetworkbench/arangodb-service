#! /bin/sh

export CATALINA_OPTS="$CATALINA_OPTS -Xms64m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx1024m"

export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=512m"

export CATALINA_OPTS="$CATALINA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=62911,server=y,suspend=n"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote="
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=1898 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
export CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=10.8.1.106"