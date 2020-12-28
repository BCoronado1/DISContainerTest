#!/bin/bash
source settings.conf
docker run --network host --name "${CONTAINER_NAME}" "${IMG_NAME}"