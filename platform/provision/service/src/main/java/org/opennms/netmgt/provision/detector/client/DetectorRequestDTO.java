/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.client;

import io.opentracing.Span;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opennms.core.xml.InetAddressXmlAdapter;
import org.opennms.horizon.ipc.rpc.api.RpcRequest;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.PreDetectCallback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor

@XmlRootElement(name = "detector-request")
@XmlAccessorType(XmlAccessType.NONE)
//TODO lombok
public class DetectorRequestDTO implements DetectRequest, RpcRequest {

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress address;

    @XmlElement(name = "detector-attribute")
    private List<DetectorAttributeDTO> detectorAttributes = new ArrayList<>();

    @XmlElement(name = "runtime-attribute")
    private List<DetectorAttributeDTO> runtimeAttributes = new ArrayList<>();

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

    private Span span;

    private PreDetectCallback preDetectCallback;

    public void addDetectorAttribute(String key, String value) {
        detectorAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addDetectorAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addDetectorAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, String> getAttributeMap() {
        return detectorAttributes.stream().collect(Collectors.toMap(DetectorAttributeDTO::getKey,
                DetectorAttributeDTO::getValue));
    }

    public void addRuntimeAttribute(String key, String value) {
        runtimeAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addRuntimeAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addRuntimeAttribute(e.getKey(), e.getValue()));
    }

    @Override
    public Map<String, String> getRuntimeAttributes() {
        Map<String, String> runtimeAttributeMap = new HashMap<String, String>();
        for (DetectorAttributeDTO agentAttribute : runtimeAttributes) {
            runtimeAttributeMap.put(agentAttribute.getKey(), agentAttribute.getValue());
        }
        return runtimeAttributeMap;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
    }

    public void setPreDetectCallback(PreDetectCallback preDetectCallback) {
        this.preDetectCallback = preDetectCallback;
    }

    @Override
    public void preDetect() {
        if(preDetectCallback != null) {
            preDetectCallback.preDetect();
        }
    }

    //TODO: does lombok generated methods give same answer?
//    @Override
//    public int hashCode() {
//        return Objects.hash(location, systemId, detectorAttributes, runtimeAttributes,
//                className, address);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final DetectorRequestDTO other = (DetectorRequestDTO) obj;
//        return Objects.equals(this.location, other.location)
//                && Objects.equals(this.systemId, other.systemId)
//                && Objects.equals(this.detectorAttributes, other.detectorAttributes)
//                && Objects.equals(this.runtimeAttributes, other.runtimeAttributes)
//                && Objects.equals(this.className, other.className)
//                && Objects.equals(this.address, other.address);
//    }

}
