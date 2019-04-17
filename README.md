easy-deposit-properties
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-deposit-properties.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-deposit-properties)

<!-- Remove this comment and extend the descriptions below -->


SYNOPSIS
--------

    easy-deposit-properties (synopsis of command line parameters)
    easy-deposit-properties (... possibly multiple lines for subcommands)


DESCRIPTION
-----------

Service for keeping track of the deposit properties


ARGUMENTS
---------

    Options:

       -h, --help      Show help message
       -v, --version   Show version of this program

    Subcommand: run-service - Starts EASY Deposit Properties as a daemon that services HTTP requests
       -h, --help   Show help message
    ---

EXAMPLES
--------

    easy-deposit-properties -o value


GRAPHQL SCHEMA
--------------

To generate the latest version of the GraphQL schema for `easy-deposit-properties`:

    #install get-graphql-schema
    npm install -g get-graphql-schema
    
    # start the service
    
    get-graphql-schema http://<base-url>/graphql > docs/schema.graphql


INSTALLATION AND CONFIGURATION
------------------------------


1. Unzip the tarball to a directory of your choice, typically `/usr/local/`
2. A new directory called easy-deposit-properties-<version> will be created
3. Add the command script to your `PATH` environment variable by creating a symbolic link to it from a directory that is
   on the path, e.g. 
   
        ln -s /usr/local/easy-deposit-properties-<version>/bin/easy-deposit-properties /usr/bin



General configuration settings can be set in `cfg/application.properties` and logging can be configured
in `cfg/logback.xml`. The available settings are explained in comments in aforementioned files.


BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher

Steps:

        git clone https://github.com/DANS-KNAW/easy-deposit-properties.git
        cd easy-deposit-properties
        mvn install
