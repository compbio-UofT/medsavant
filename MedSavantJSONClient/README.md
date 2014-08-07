MedSavant JSON Client
=====================

## Usage

1. In this directory, run `mvn install`
2. Run MedSavant Server
3. Edit `./target/classes/config.properties` and enter the server's details
4. In this directory, run `mvn jetty:run-war`
5. Open the web browser to [http://localhost:8090/medsavant-json-client/testing](http://localhost:8090/medsavant-json-client/testing)

Open the browser's develope tools, and go to the Networking tab.
In the webpage, click "Go" or "demo", and watch the requests. That should give
you an idea of how to access the client.

