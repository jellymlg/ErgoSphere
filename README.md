# ErgoSphere - a personal Ergo ecosystem<img width="15%" align="right" src="https://user-images.githubusercontent.com/39315532/170884890-d7a10707-0ed6-4705-a71a-de148e12123e.svg">
ErgoSphere was created in the image of Umbrel (https://getumbrel.com), which allows the easy setup of self-hosted Ergo services.

## Services
An Ergo full node gets installed by default, which serves as the foundation for other services.

Currently the following services are available on ErgoSphere:
<ul>
  <li>Ergo explorer</li>
  <li>ErgoMixer</li>
  <li>ErgoDex offchain execution</li>
</ul>

This list is to be expanded with more services in the future.

## Dependencies
ErgoSphere is written in Java, and therefore can run on any system with a Java environment, however there are some additional dependencies:

<ul>
  <li>[Java >= 1.8]</li>
  <li>Docker</li>
  <li>Docker-compose (comes with Docker on Windows)</li>
  <li>SBT</li>
</ul>

## Running
ErgoSphere must be ran with administrative privilages. This is to make sure there won't be permission problems when interacting with Docker or PowerShell.

By default the web interface will be available on the local IP address of the host (e.g: 192.168.50.111), and all data will be stored in the "data" folder.

NOTE: all other services will be available on this address on their respective ports

These default can be overwritten with command line parameters:

    -address=<IP ADDRESS>
    -port=<PORT NUMBER>
    -storage=<DATA DIRECTORY>

An example for using custom parameters would look like this:

    java -jar ErgoSphere.jar -address=192.168.0.120 -port=5555 -storage=ESdata/

In this example the web interface will be accessible on http://192.168.0.120:5555, and all data will be stored in the "ESdata" folder.
