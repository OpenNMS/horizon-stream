/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.gatling;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class BasicSimulation extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http
          // Here is the root for all relative URLs
          .baseUrl("http://localhost:8181/cxf")
          .authorizationHeader("Basic YWRtaW46YWRtaW4=")
          .contentTypeHeader("application/xml");

  // A scenario is a chain of requests and pauses
  ScenarioBuilder scn =
      scenario("Post Events")
          .exec(
              http("post_events")
                  // Here's an example of a POST request
                  .post("/events")
                  // Note the triple double quotes: used in Scala for protecting a whole chain of
                  // characters (no need for backslash)
                  .body(RawFileBody("alarm-event.xml")));

  {
    setUp(scn.injectOpen(rampUsersPerSec(1).to(1000).during(30)).protocols(httpProtocol));
  }
}
