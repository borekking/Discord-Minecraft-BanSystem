package de.borekking.banSystem.permission;

import de.borekking.banSystem.util.JavaUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PermissionUtil {

    /*
     * Each command while have permission:
     *    e.g.:
     *       1. command.group.subCommand   <- Only the subCommand
     *       2. command.group.*            <- All subCommands of group
     *       3. command.*                  <- All groups of command
     *       4. *                          <- All Commands
     *
     *       Note that 4 contains 1, 2 and 3, while 3 contains 2 and 1, etc.
     */

    private PermissionUtil() {
    }

    public static String PERMISSION_SEPARATOR = ";", STAR = "*", GROUP_SEPARATOR = ".";

    public static List<String> getPermissionsList(String permissions) {
        return JavaUtils.getAsList(permissions.split(PERMISSION_SEPARATOR));
    }

    public static boolean userPermissionContainsPermission(String userPermissions, String neededPermission) {
        List<String> permissions = getPermissionsList(userPermissions);
        return permissions.stream().anyMatch(perm -> containsPermission(perm, neededPermission));
    }

    // Return if givenPermission contains neededPermission
    private static boolean containsPermission(String givenPermission, String neededPermission) {
        if (givenPermission.equals(neededPermission)) return true;

        String[] givenArray = givenPermission.split("\\" + GROUP_SEPARATOR);
        String[] neededArray = neededPermission.split("\\" + GROUP_SEPARATOR);

        for (int i = 0; i < givenArray.length; i++) {
            if (givenArray[i].equals(STAR)) return true;

            if (!givenArray[i].equals(neededArray[i])) return false;
        }

        return false;
    }

    public static String getPermissions(List<String> permissions) {
        return permissions.stream().collect(Collectors.joining(PERMISSION_SEPARATOR));
    }

    public static String mergePermissions(String... permissions) {
        return JavaUtils.getAsList(permissions).stream().collect(Collectors.joining(GROUP_SEPARATOR));
    }

    public static String removePermissions(String permissions, String permissionsRemoved) {
        List<String> permissionsList = getPermissionsList(permissions);
        List<String> permissionsRemovedList = getPermissionsList(permissionsRemoved);

        permissionsList.removeAll(permissionsRemovedList);
        return getPermissions(permissionsList);
    }
}
