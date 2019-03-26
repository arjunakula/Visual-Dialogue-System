Query Engine Template
=====================

This eclipse project is a template to develop a Query Engine that can connect
to the MSEE Framework.

The following contents are included in this package:

    lib/    -- MSEE-SDK and all dependencies packed in jar files.
        msee-sdk-java-20131217.jar  -- The MSEE-SDK.
    src/
        QueryEngineImpl.java    -- To implementation the interface here.
        QueryEngineServer.java  -- The executable entrance at the final deployment.
    msee_conf.xml    -- A configuration file used to connect to other services.

Online computation access is not included in the current SDK.

Later when the SDK is updated, you only need to put the new jar file under lib
folder and reconfigure the classpath to use the new jar.
