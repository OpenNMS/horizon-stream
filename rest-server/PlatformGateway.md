## The gateway endpoints
* Create an event: return status code 204
```text
POST http://localhost:9090/events
accept: application/json
content-type: application/json
Authorization: Bearer eyJhbG...

post data
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
* List alarms: 
```text
GET http://localhost:9090/alarms
accept: application/json
Authorization: Bearer eyJhbGciOiJS

----
sample result 
{
  "alarm": [
    {
      "id": 1,
      "uei": "uei.opennms.org/alarms/trigger",
      "location": "Default",
      "nodeId": null,
      "nodeLabel": null,
      "ipAddress": null,
      "serviceType": null,
      "reductionKey": "uei.opennms.org/alarms/trigger:::",
      "type": 1,
      "count": 1,
      "severity": "WARNING",
      "firstEventTime": 1652192242000,
      "description": "A problem has been triggered...",
      "logMessage": "A problem has been triggered on //.",
      "operatorInstructions": null,
      "troubleTicket": null,
      "troubleTicketState": null,
      "troubleTicketLink": null,
      "mouseOverText": null,
      "suppressedUntil": 1652192242000,
      "suppressedBy": null,
      "suppressedTime": 1652192242000,
      "ackUser": null,
      "ackTime": null,
      "clearKey": null,
      "lastEvent": {
        "id": 1,
        "uei": "uei.opennms.org/alarms/trigger",
        "label": "Alarm: Generic Trigger",
        "time": 1652192242000,
        "host": null,
        "source": "asn-cli-script",
        "ipAddress": null,
        "snmpHost": null,
        "serviceType": null,
        "snmp": null,
        "parameter": [],
        "createTime": 1652227116354,
        "description": "A problem has been triggered...",
        "logGroup": null,
        "logMessage": "A problem has been triggered on //.",
        "severity": "WARNING",
        "pathOutage": null,
        "correlation": null,
        "suppressedCount": null,
        "operatorInstructions": null,
        "autoAction": null,
        "operatorAction": null,
        "operationActionMenuText": null,
        "notification": null,
        "troubleTicket": null,
        "troubleTicketState": null,
        "mouseOverText": null,
        "log": "Y",
        "display": "Y",
        "ackUser": null,
        "ackTime": null,
        "nodeId": null,
        "nodeLabel": null,
        "ifIndex": null,
        "location": "Default"
      },
      "parameter": [],
      "lastEventTime": 1652192242000,
      "applicationDN": null,
      "managedObjectInstance": null,
      "managedObjectType": null,
      "ossPrimaryKey": null,
      "x733AlarmType": null,
      "x733ProbableCause": 0,
      "qosAlarmState": null,
      "firstAutomationTime": null,
      "lastAutomationTime": null,
      "ifIndex": null,
      "reductionKeyMemo": null,
      "stickyMemo": null,
      "relatedAlarms": null,
      "affectedNodeCount": 0
    }
  ],
  "count": 1,
  "totalCount": 1,
  "offset": 0
}
```

* Ack alarm
```
POST http://localhost:9090/alarms/5/ack
accept: application/json
content-type: application/json
Authorization: Bearer

{
  "user": "admin"
}

repoonse noncontent
```

* Un-Ack alarm
```
DELETE http://localhost:9090/alarms/5/ack
accept: application/json
Authorization: Bearer

reponse nocontent
```

* Clear alarm
```text
POST http://localhost:9090/alarms/4/clear
accept: application/json
content-type: application/json
Authorization: Bearer
```

