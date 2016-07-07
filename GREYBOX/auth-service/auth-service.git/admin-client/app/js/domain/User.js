'use strict';

var t = require('tcomb-form');
var Classification = require('./Classification.js');

var citizenshipStatus = t.enums.of([
    'NATURALIZED US',
    'NOT INDICATED',
    'OTHER',
    'US'
]);

var country = t.enums.of([
    'AUS',
    'CAN',
    'GBR',
    'NZL',
    'UNK',
    'USA'
]);

var affiliation = t.enums.of([
    'CANDIDATE',
    'CONSULTANT',
    'CONTRACTOR',
    'CIVILIAN',
    'EMPLOYEE',
    'MILITARY',
    'NON-AGENCY',
    'NON-AGENCY CIVILIAN',
    'NON-AGENCY CONTRACTOR',
    'NON-AGENCY MILITARY'
]);

/**
 * Defines a user that may be entered into and/or retrieved from the authentication
 *system.
 */
var User = t.struct({
    dn: t.Str,
    firstName: t.maybe(t.Str),
    lastName: t.Str,
    fullName: t.Str,
    displayName:  t.maybe(t.Str),
    uid: t.Str,
    dutyOrg: t.maybe(t.Str),
    email: t.maybe(t.Str),
    employeeId: t.maybe(t.Str),
    personalTitle: t.maybe(t.Str),
    title: t.maybe(t.Str),
    secureTelephoneNumber: t.maybe(t.Str),
    telephoneNumber: t.maybe(t.Str),
    clearances: t.list(Classification),
    formalAccess: t.list(t.Str),
    coi: t.list(t.Str),
    briefing: t.list(t.Str),
    formalGroup: t.list(t.Str),
    citizenshipStatus: t.maybe(citizenshipStatus),
    country: t.maybe(country),
    grantBy: t.list(t.Str),
    organization: t.list(t.Str),
    affiliations: t.list(affiliation),
    dissemControl: t.list(t.Str),
    dissemTo: t.list(t.Str),
}, 'User');

module.exports = User;
