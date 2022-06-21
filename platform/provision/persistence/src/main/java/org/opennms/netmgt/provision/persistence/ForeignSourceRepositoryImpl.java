package org.opennms.netmgt.provision.persistence;

import lombok.RequiredArgsConstructor;
import org.joda.time.Duration;
import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;
import org.opennms.netmgt.provision.persistence.model.ForeignSourceRepository;

@RequiredArgsConstructor
public class ForeignSourceRepositoryImpl implements ForeignSourceRepository {


    @Override
    public ForeignSourceDTO getForeignSource(String id) {
        //TODO: use DAO to read from DB!!!!
        ForeignSourceDTO foreignSourceDTO = new ForeignSourceDTO();
        foreignSourceDTO.setScanInterval(new Duration(60000));
        return foreignSourceDTO;
    }
}
