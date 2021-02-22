import os

from fabric import task


@task
def uname(c):
    c.run('uname -a')


@task
def deploy(c):
    with c.cd(os.getenv('WEBEID_DIR')):
        c.run('git pull')
        c.run('mvn clean package com.google.cloud.tools:jib-maven-plugin:dockerBuild')
        c.run('docker-compose down')
        c.run('docker-compose up -d')

