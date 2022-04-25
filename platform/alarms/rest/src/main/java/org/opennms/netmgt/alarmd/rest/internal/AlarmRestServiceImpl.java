/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.rest.internal;


import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.opennms.horizon.core.lib.SystemProperties;
import org.opennms.horizon.db.dao.api.AlarmDao;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.horizon.db.model.OnmsAlarm;
import org.opennms.horizon.db.model.dto.AlarmCollectionDTO;
import org.opennms.horizon.db.model.dto.AlarmDTO;
import org.opennms.horizon.db.model.mapper.AlarmMapper;
import org.opennms.netmgt.alarmd.rest.AlarmRestService;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Basic Web Service using REST for {@link OnmsAlarm} entity, but from Karaf container.
 * ...based of of v2 of the currently existing rest webservice for alarmD
 *
 * @author Mark Bordelon
 */
@Path("/alarms")
public class AlarmRestServiceImpl implements AlarmRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmRestServiceImpl.class);

    private AlarmDao alarmDao;

    private AlarmMapper m_alarmMapper;

    //private AlarmRepository alarmRepository;

    //private TroubleTicketProxy troubleTicketProxy;

    private SessionUtils sessionUtils;

//========================================
// Getters and Setters
//========================================


    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

//    public void setTroubleTicketProxy(TroubleTicketProxy troubleTicketProxy) {
//        this.troubleTicketProxy = troubleTicketProxy;
//    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public void setAlarmMapper(AlarmMapper m_alarmMapper) {
        this.m_alarmMapper = m_alarmMapper;
    }

//========================================
//
//========================================

    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = alarmDao.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<OnmsAlarm> criteriaQuery = builder.createQuery(OnmsAlarm.class);
        Root<OnmsAlarm> alarmRoot = criteriaQuery.from(OnmsAlarm.class);
        alarmRoot.fetch("lastEvent", JoinType.LEFT);

        // FIXME: OOps
//        // 1st level JOINs
//        builder.alias("lastEvent", "lastEvent", JoinType.LEFT_JOIN);
//        builder.alias("distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
//        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
//        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);
//
//        // 2nd level JOINs
//        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
//        // Left joins on a toMany relationship need a join condition so that only one row is returned
//        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.ipInterface.prop("ipAddress"), Aliases.alarm.prop("ipAddr")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
//        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
//        // Left joins on a toMany relationship need a join condition so that only one row is returned
//        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.alarm.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));
//
//        builder.orderBy("lastEventTime").desc(); // order by last event time by default

        return builder;
    }

    protected Class<OnmsAlarm> getDaoClass() {
        return OnmsAlarm.class;
    }

    protected WebApplicationException getException(final Status status, String msg, String... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    private boolean isTicketerPluginEnabled() {
        return SystemProperties.getBooleanWithDefaultAsTrue("opennms.alarmTroubleTicketEnabled");
    }

    private Response runIfTicketerPluginIsEnabled(Callable<Response> callable) throws Exception {
        if (!isTicketerPluginEnabled()) {
            return Response.status(Status.NOT_IMPLEMENTED).entity("AlarmTroubleTicketer is not enabled. Cannot perform operation").build();
        }
        Objects.requireNonNull(callable);
        final Response response = callable.call();
        return response;
    }


//========================================
// Interface
//========================================

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Retrieve the list of alarms"
    )
    public Response getAlarms(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo) {
        // replace the next line with @RolesAllowed("")
        //SecurityHelper.assertUserReadCredentials(securityContext);

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            //CriteriaBuilder builder = getCriteriaBuilder(uriInfo);
            //builder.distinct();

            List<OnmsAlarm> matchingAlarms = this.alarmDao.findAll(); //(builder.createQuery());

            List<AlarmDTO> dtoAlarmList =
                    matchingAlarms
                            .stream()
                            .map(this.m_alarmMapper::alarmToAlarmDTO)
                            .collect(Collectors.toList());

            AlarmCollectionDTO alarmsCollection = new AlarmCollectionDTO(dtoAlarmList);
            alarmsCollection.setTotalCount(dtoAlarmList.size());

            return Response.status(Status.OK).entity(alarmsCollection).build();
        });

    }

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Update the memo for an Alarm"
    )
    public Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        // replace the next two lines with @RolesAllowed("")
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);

        return this.sessionUtils.withTransaction(() -> {
            final String body = params.getFirst("body");
            if (body == null) {
                throw getException(Status.BAD_REQUEST, "Body cannot be null.");
            }
            //alarmRepository.updateStickyMemo(alarmId, body, user); // TODO doing anything??
            return Response.noContent().build();
        });
    }

    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Update the journal for an Alarm"
    )
    public Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        return this.sessionUtils.withTransaction(() -> {
            final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
            // SecurityHelper.assertUserEditCredentials(securityContext, user);
            final String body = params.getFirst("body");
            if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
            //alarmRepository.updateReductionKeyMemo(alarmId, body, user); // TODO doing anything??
            return Response.noContent().build();
        });
    }

    @DELETE
    @Path("{id}/memo")
    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Remove the memo for an Alarm"
    )
    public Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) {
        //SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        try {
            return runIfTicketerPluginIsEnabled(() -> {
                return this.sessionUtils.withTransaction(() -> {
                   // alarmRepository.removeStickyMemo(alarmId); // TODO doing anything??
                    return Response.noContent().build();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
