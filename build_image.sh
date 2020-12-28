#!/bin/bash
source settings.conf
echo "${IMG_NAME}"
docker build -t "${IMG_NAME}" .