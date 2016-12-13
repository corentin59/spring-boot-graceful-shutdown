Spring-Boot-Graceful-Shutdown-Starter
===================


This starter expose endpoints on a spring-boot project to perform graceful shutdown.
At this time, the actuator starter expose a shutdown endpoint, this shutdown is ok to destroy graceful the spring-context, but not for currents HTTP connections.

An issue is open on github : https://github.com/spring-projects/spring-boot/issues/4657
To gain time, I have written a new Spring-Boot starter based on Spring-Boot actuator. When we hit a shutdown, the starter denied new http connections and wait end of currents connections.

Endpoints exposed :
- HTTP : REST API /gracefulshutdown (with spring-boot-starter-actuator)
- JMX (with spring-boot-starter-actuator)
- SSH (with spring-boot-starter-remote-shell)

> **Note:**
> Support only Undertow and Tomcat
> For security see actuator

Servlets engine supported
-------------
We support currently : Undertow and Tomcat

How it works
-------------

![workflow](https://raw.githubusercontent.com/corentin59/spring-boot-graceful-shutdown/master/docs/images/workflow.png)

How to for ops
-------------

Setup
-----
3 properties in application.properties (or application.yml)

| Property  | Default | Description |
| ------------- | ------------- | ------------- |
| endpoints.shutdown.graceful.enabled  | false  | Activate the starter and expose endpoints |
| endpoints.shutdown.graceful.timeout | 30 | Wait "30" seconds before make a force shutdown |
| endpoints.shutdown.graceful.wait| 30 | The time before launch graceful shutdown, the health checker return OUT_OF_SERVICE |

Perform shutdown (HTTP REST API)
-----
Call /shutdowngraceful (in GET), the endpoint return the HTTP Response code 200 with a message.

Perform shutdown (JMX)
-----
Must be documented

Perform shutdown (SSH)
-----
If the spring-boot-starter-remote-shell is in dependencies, you can type the following command

    endpoint invoke gracefulShutdownEndpoint

![crash](https://raw.githubusercontent.com/corentin59/spring-boot-graceful-shutdown/master/docs/images/ssh.png)

Please refer to [remote shell manual](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-remote-shell.html) for setup and security.

Logs
-----
| Level | Sample | Description |
| ------------- | ------------- | ------------- |
| INFO | Mapped "{[/shutdowngraceful..." | 	If graceful shutdown starter is enabled |
| INFO | Shutdown performed in ?? second(s) | When the shutdown is performed |
| INFO | Graceful shutdown in progress.. We don't accept new connection... Wait after latest connections (max : ?? seconds) | When we start a graceful shutdown |
| INFO | Thread pool is empty, we stop now | No active HTTP connection, we can kill |
| INFO | We are now in OUT_OF_SERVICE mode, please wait ?? second(s) | App is always alive, but the health checker return OUT_OF_SERVICE |
| WARN | Thread pool did not shut down gracefully within ?? second(s). Proceeding with force shutdown | Few HTTP connections are actives, but the timeout is exceeded, we perform a force shutdown |
| ERROR | The await termination has been interrupted | A force shutdown has been received before graceful shutdown |

Health checker (HTTP RESP API)
-----
An health checker endpoint is available to see the state of the app. The endpoint can be prefixed, see actuator doc. If the app is out of service, the response will be :

    {
	    "status": "OUT_OF_SERVICE",
	    "gracefulHealth" : {
		    "status" : "OUT_OF_SERVICE"
		}
	}
The HTTP code is **503** : Service unavailable. If all is good, you must be have a **200**.

Security
-----
The security is maintened by two points :

 - Isolate Path : /management
 - Isolate management opération on a specific port : please use management.port setting

How to for developers
-------------

Add spring-boot-starter-actuator in your pom.xml
-----

    <dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-actuator</artifactId>
	</dependency>

Add spring-boot-starter-graceful-shutdown in your pom.xml
-----

    <dependency>
	   <groupId>com.nordnet</groupId>
	   <artifactId>spring-boot-starter-graceful-shutdown</artifactId>
	   <version>X.X.X</version>
	</dependency>
Check the latest version on repository.

Add spring-boot-starter-remote-shell in your pom.xml
-----
It's optionnal

    <dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-remote-shell</artifactId>
	</dependency>

Setup the started
-----
In application.properties

    # Enable the endpoint (mandatory)
	endpoints.shutdown.graceful.enabled=true
 
	# Specify the timeout before perform a force shutdown (optional)
	endpoints.shutdown.graceful.timeout=15
  
	# The timer before launch graceful shutdown, the health checker return OUT_OF_SERVICE (optional)
	endpoints.shutdown.graceful.wait=15

Or application.yml

    endpoints:
    shutdown:
        graceful:
            enabled: true
            timeout: 15
            wait: 15