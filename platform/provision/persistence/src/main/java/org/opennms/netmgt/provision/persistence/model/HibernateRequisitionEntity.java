package org.opennms.netmgt.provision.persistence.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;

@Entity
@Table(name="requisitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HibernateRequisitionEntity implements Serializable
{
    @Id
    private String requisitionName;

    // Because we are storing this as JSON, it makes to just inject the DTO object
    // which is the source of the JSON.
    @Column
    @Type(type = "RequisitionJsonType")
    private RequisitionDTO requisition;
}
