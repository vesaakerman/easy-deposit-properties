EASY-DEPOSIT-PROPERTIES
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-deposit-properties.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-deposit-properties)

SYNOPSIS
--------

    easy-deposit-properties load-props [--doUpdate] <properties-file>
    easy-deposit-properties run-service


DESCRIPTION
-----------

Service for keeping track of the deposit properties


ARGUMENTS
---------

    Options:

       -h, --help      Show help message
       -v, --version   Show version of this program

    Subcommand: load-props - Load a deposit.properties file and import it in the backend repository.
          --doUpdate   Without this argument the properties are not imported, the
                       default is a test mode that logs the intended changes
      -h, --help       Show help message
    
     trailing arguments:
      <properties-file> (required)   The deposit.properties file to be read.
    ---
    
    Subcommand: run-service - Starts EASY Deposit Properties as a daemon that services HTTP requests
       -h, --help   Show help message
    ---

EXAMPLES
--------

    easy-deposit-properties load-props <properties-file>
    easy-deposit-properties run-service


GRAPHQL INTERFACE
-----------------

1. build `easy-deposit-properties` using `mvn clean install`
2. make sure the [dans-dev-tools]({{ dans_dev_tools }}) are installed properly
3. call `run-reset-env.sh` from the root of the project
4. call `run-service.sh` from the root of the project
5. in your browser, go to http://localhost:20200/graphiql


GRAPHIQL TOOLS
--------------
To interact with the GraphQL servlet, use the internal http://localhost:20200/graphiql interface.
Alternatively, on Mac, use the [GraphiQL.app]({{ graphql_app }}).


GRAPHQL SCHEMA
--------------

To generate the latest version of the GraphQL schema for `easy-deposit-properties`:

    #install get-graphql-schema
    npm install -g get-graphql-schema
    
    # (re)start the service (after: mvn clean install -DskipTests=true): see GRAPHQL INTERFACE above
    
    get-graphql-schema http://<base-url>/graphql > docs/schema.graphql


INSTALLATION AND CONFIGURATION
------------------------------

Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/easy-deposit-properties` and the configuration files to `/etc/opt/dans.knaw.nl/easy-deposit-properties`. 

To install the module on systems that do not support RPM, you can copy and unarchive the tarball to the target host.
You will have to take care of placing the files in the correct locations for your system yourself. For instructions
on building the tarball, see next section.


BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

        git clone https://github.com/DANS-KNAW/easy-deposit-properties.git
        cd easy-deposit-properties
        mvn install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM 
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
