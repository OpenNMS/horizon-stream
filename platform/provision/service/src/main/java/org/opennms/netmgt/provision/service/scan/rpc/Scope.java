/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.scan.rpc;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface Scope {
    Optional<ScopeValue> get(final ContextKey contextKey);
    Set<ContextKey> keys();

    public static class ScopeValue {
        public final ScopeName scopeName;
        public final String value;

        public ScopeValue(final ScopeName scopeName, final String value) {
            this.scopeName = Objects.requireNonNull(scopeName);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScopeValue that = (ScopeValue) o;
            return scopeName == that.scopeName && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scopeName, value);
        }

        @Override
        public String toString() {
            return "ScopeValue{" +
                    "scopeName=" + scopeName +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public enum ScopeName {
        DEFAULT, NODE, INTERFACE, SERVICE;
    }
}