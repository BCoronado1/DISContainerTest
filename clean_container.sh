#!/bin/bash
source settings.conf

docker stop "${CONTAINER_NAME}" && docker rm "${CONTAINER_NAME}"