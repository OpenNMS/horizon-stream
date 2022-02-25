package org.opennms.core.ipc.grpc.heartbeat;

import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.ipc.sink.api.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Objects;

@XmlRootElement(name = "minion")
@XmlAccessorType(XmlAccessType.FIELD)
public class MinionIdentityDTO implements Message {

    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "timestamp")
    private Date timestamp;

    public MinionIdentityDTO() {
    }

    public MinionIdentityDTO(Identity identity) {
        this.id = identity.getId();
        location = identity.getLocation();
        timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MinionIdentityDTO other = (MinionIdentityDTO) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("MinionIdentityDTO[id=%s, location=%s, timestamp=%s]", id, location, timestamp);
    }
}
