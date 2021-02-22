#!/bin/bash

set -eu

SERVER_ADDRESS=server
SERVER_USER=user
SERVER_PORT=22

export WEBEID_DIR='/path/'

fab -e -H ${SERVER_USER}@${SERVER_ADDRESS}:${SERVER_PORT} "$@"
