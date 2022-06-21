package org.opennms.horizon.provision.rest;

import com.google.gson.Gson;
import java.util.Optional;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.service.Provisioner;

@RequiredArgsConstructor
@Slf4j
public class ProvisionRestServiceImpl implements ProvisionRestService {
    private final Provisioner provisioner;
    private final SessionUtils sessionUtils;
    private Gson gson = new Gson();

    @Override
    public Response publishRequisition(String requisition) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                RequisitionDTO requisitionDTO = gson.fromJson(requisition, RequisitionDTO.class);
                String id = provisioner.publish(requisitionDTO);
                return Response.ok().entity(id).build();
            } catch (Exception e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        });
    }

    @Override
    public Response getRequisition(String requisitionName) {
        return this.sessionUtils.withReadOnlyTransaction(() -> {
            Optional<RequisitionDTO> data = provisioner.read(requisitionName);
            if (data.isPresent()) {
                log.info("Found the req {}", data);
                return Response.ok().entity(gson.toJson(data.get())).build();
            }
            else {
                return Response.status(Status.NOT_FOUND).build();
            }
        });
    }

    @Override
    public Response updateRequisition(String requisition) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                RequisitionDTO requisitionDTO = gson.fromJson(requisition, RequisitionDTO.class);
                String id = provisioner.update(requisitionDTO);
                return Response.ok().entity(id).build();
            } catch (Exception e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        });
    }

    @Override
    public Response deleteRequisition(String requisitionName) {
        return this.sessionUtils.withTransaction(() -> {
            try {
                provisioner.delete(requisitionName);
                return Response.ok().build();
            }
            catch (Exception e) {
                return Response.noContent().build();
            }
        });
    }

    @Override
    public Response scanNodes() {
        return this.sessionUtils.withReadOnlyTransaction(() -> {
            provisioner.performNodeScan();
            return Response.ok().build();
        });
    }
}
