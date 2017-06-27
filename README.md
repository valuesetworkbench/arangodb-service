# ArangoDB CTS2 Service

A [CTS2-compliant](http://www.omg.org/spec/CTS2/) terminology service based on the [ArangoDB](https://www.arangodb.com/) graph database.

# Installation
## Via Docker Compose

The below Docker Compose file can be used to start the CTS2 service with its prerequisite services.

```YAML
# AraangoDB
arango:
  image: arangodb:2.8.7
  ports:
    - "8529:8529"
  environment:
    - ARANGO_NO_AUTH=1

# Elasticsearch
cts2-elasticsearch:
  image: elasticsearch:2.0.2
  ports:
  - "9201:9200"
  - "9301:9300"

# CTS2 Service
cts2:
  image: valuesetworkbench/arangodb-service
  container_name: cts2
  ports:
    - "9999:8080"
    - "62911:62911"
  links:
    - "arango:arango"
    - "cts2-elasticsearch:cts2-elasticsearch"
  environment:
    - SERVER_ROOT=http://your/server/name # <- CHANGE THIS
```
