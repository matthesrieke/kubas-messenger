# enviroCar ArcGIS GeoEvent Processor for Server components

The enviroCar platform features HTTP pushing of newly uploaded tracks. This projects provides custom adapters
for ESRI ArcGIS GeoEvent Processors for Server. They can be used to transfer the data to e.g. an attached ArcGIS Server feature service.

## Building

`mvn clean install` will basically do the job.

For use in the GeoEventProcessor, you need to deploy the final .jar and its dependencies to the GEP `deploy` directory (e.g. `C:\Program Files\ArcGIS\Server\GeoEventProcessor\deploy`). All required dependencies are located at `track-components\track-adapter-inbound\target\<build-name>-binaries\deploy-to-gep` (or similar).

## GEP Setup

Once deployed both a new GeoEvent definition (`enviroCar-track-definition`) and an inbound adapter (`track-adapter-inbound`) are available in the GEP manager. You then need to define a new connector (e.g. at `https://localhost:6143/geoevent/manager/connectordefinition.html`). For "Adapter" select `track-adapter-inbound`, for "Transport" `HTTP`. You should define defaults for some properties:

 * Acceptable MIME Types (Server Mode): application/json
 * Mode: SERVER
 * HTTP Method: POST

Now you can create a new input using the newly created connector and define workflows with it.
