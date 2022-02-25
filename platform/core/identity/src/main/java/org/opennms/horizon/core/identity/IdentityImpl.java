package org.opennms.horizon.core.identity;

public class IdentityImpl implements Identity {

    private final String id;
    private final String location;
    private final String type;

    public IdentityImpl(String id, String location, String type) {
        this.id = id;
        this.location = location;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getType() {
        return type;
    }
}
