#!/bin/sh

./gradlew \
  :lib:bidms-common-json:publish \
  :lib:bidms-app-common-conf:publish \
  :lib:bidms-rest-client:publish
