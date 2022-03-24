package org.opennms.rest.jaxrs.jwt.impl;

import org.opennms.rest.jaxrs.jwt.RoleAssignmentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * RoleAssignmentProvider which loads assignments from users.properties file in the same format used by Karaf.
 *
 * NOTE: does NOT support dynamic updates at this time.
 */
public class UserPropertiesRoleAssignmentProvider implements RoleAssignmentProvider {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(UserPropertiesRoleAssignmentProvider.class);

    private Logger log = DEFAULT_LOGGER;

    private String path = "etc/users.properties";

    private Map<String, List<String>> userToRoleMap;

//========================================
// Getters and Setters
//----------------------------------------

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        userToRoleMap = loadFromFile(path);
    }

//========================================
// RoleAssignmentProvider API
//----------------------------------------

    @Override
    public List<String> lookupUserRoles(String username) {
        if (userToRoleMap == null) {
            log.error("Not initialized; make sure the init() method is called at startup");
            throw new RuntimeException("role lookup failed");
        }

        return userToRoleMap.get(username);
    }

//========================================
//
//----------------------------------------

    private Map<String, List<String>> loadFromFile(String path) {
        Map<String, List<String>> roleAssignments = new HashMap<>();
        Map<String, List<String>> groupDefinitions = new HashMap<>();

        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);

            //
            // Process all of the groups first
            //
            for (String oneKey : properties.stringPropertyNames()) {
                if (oneKey.startsWith("_g_:")) {
                    processGroupDefinition(properties, groupDefinitions, oneKey);
                }
            }

            //
            // Now process the user entries
            //
            for (String oneKey : properties.stringPropertyNames()) {
                if (! oneKey.startsWith("_g_:")) {
                    processUserEntry(properties, groupDefinitions, roleAssignments, oneKey);
                }
            }
        } catch (Exception exc) {
            throw new RuntimeException("failed to load role assignments from file \"" + path + "\"", exc);
        }

        return roleAssignments;
    }

    private void processGroupDefinition(Properties properties, Map<String, List<String>> groupDefinitions, String groupKey) {
        //
        // The group starts with prefix "_g_:", so remove it
        //
        String groupName = groupKey.substring("_g_:".length());

        String roleNameText = properties.getProperty(groupKey);

        //
        // Split the comma-separated list
        //
        if (roleNameText != null) {
            String[] roleNames = roleNameText.split(",");

            Set<String> roleList = new TreeSet<>();

            for (String oneRawRoleName : roleNames) {
                String trimmedRoleName = oneRawRoleName.trim();
                if (! trimmedRoleName.isEmpty()) {
                    roleList.add(trimmedRoleName);
                }
            }

            groupDefinitions.put(groupName, new LinkedList<>(roleList));
        }
    }

    private void processUserEntry(
            Properties properties,
            Map<String, List<String>> groupDefinitions,
            Map<String, List<String>> roleAssignments,
            String username) {

        String assignmentText = properties.getProperty(username);

        //
        // Split the comma-separated list
        //
        if (assignmentText != null) {
            String[] assignmentEntries = assignmentText.split(",");

            Set<String> roleSet = new TreeSet<>();

            boolean first = true;

            //
            // Iterate over all of the entries.  Add individual role names as well as the roles from group assignments.
            //
            for (String oneRawAssignment : assignmentEntries) {
                // Skip the first entry, that's the password
                if (first) {
                    first = false;
                } else {
                    String trimmedAssignment = oneRawAssignment.trim();

                    if (trimmedAssignment.startsWith("_g_:")) {
                        // Group assignment
                        String groupName = trimmedAssignment.substring("_g_:".length());
                        List<String> groupAssignments = groupDefinitions.get(groupName);

                        // If the group has a list of assignments, add them all to this user
                        if (groupAssignments != null) {
                            roleSet.addAll(groupAssignments);
                        }
                    } else {
                        // Not a group, just a role name.
                        if (! trimmedAssignment.isEmpty()) {
                            roleSet.add(trimmedAssignment);
                        }
                    }
                }
            }

            List<String> roleList = new LinkedList<>(roleSet);
            roleAssignments.put(username, roleList);
        }
    }

}
