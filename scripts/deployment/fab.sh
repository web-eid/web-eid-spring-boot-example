#!/bin/bash

set -eu

SERVER_ADDRESS=web-eid.eu
SERVER_USER=baron
SERVER_PORT=22

export WEBEID_DIR='web-eid/web-eid-springboot-example'
export WEBEID_DIR='web-eid/test-web-eid-springboot-example'

fab -e -H ${SERVER_USER}@${SERVER_ADDRESS}:${SERVER_PORT} "$@"
