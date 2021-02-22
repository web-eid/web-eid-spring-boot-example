## Deploy new version to web-eid.eu server

In the local machine:

1. Setup virtualenv and install [Fabric](https://www.fabfile.org/)

       python3 -m venv venv
       . venv/bin/activate  # . venv/Scripts/activate on Windows
       pip install -r requirements.txt

2. Check the project path, server hostname, user and port in `fab.sh`

4. Verify that Fabric is able to connect to the server by running `uname`

       ./fab.sh uname

7. Deploy the project

       ./fab.sh deploy

