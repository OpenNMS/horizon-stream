## The gateway endpoints
* Create an event
```text
POST http://localhost:9090/events
accept: application/json
content-type: application/json
Authorization: Bearer eyJhbG...

{
  "uei": "uei.opennms.org/alarms/trigger",
  "time": "2022-01-12T17:12:22.000Z",
  "source": "asn-cli-script",
  "descr": "A problem has been triggered...",
  "creation-time": "2022-01-12T17:12:22.000Z",
  "logmsg": {
    "notify": true,
    "dest": "A problem has been triggered on //..."
  }
}
```
* List alarms
```text
GET http://localhost:9090/alarms
accept: application/json
Authorization: Bearer eyJhbGciOiJS
```