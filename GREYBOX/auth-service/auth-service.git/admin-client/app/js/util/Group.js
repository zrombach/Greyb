/**
 * Utility functions to handle special rules applied to group names by the
 * authorization system.
 */
'use strict';

/**
 * Determine if a given group name contains a project name prefix.
 * @param {string} groupName Authorization group name.
 */
function hasProject(groupName) {
    return groupName.lastIndexOf('!') >= 0;
}

/**
 * Extract project name prefix from a given group name.
 * @param {string} groupName Authorization group name.
 */
function getProjectNameFromGroupName(groupName) {
    var index = groupName.lastIndexOf('!');
    var project;

    if (index > 0) {
        project = groupName.substring(0, index);
    }

    return project;
}

/**
 * Remove project name prefix from a given group name.
 * @param {string} groupName Authorization group name.
 */
function stripProjectNameFromGroupName(groupName) {
    var index = groupName.lastIndexOf('!');

    if (index >= 0) {
        groupName = groupName.substring(index + 1);
    }

    return groupName;
}

module.exports = {
    hasProject: hasProject,
    getProjectNameFromGroupName: getProjectNameFromGroupName,
    stripProjectNameFromGroupName: stripProjectNameFromGroupName
};
