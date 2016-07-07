'use strict';

var Reflux = require('reflux');
var _ = require('lodash');
var { update } = require('react/addons').addons;

function createSimpleStore (listenables, idProp) {
    var cache = [];

    return Reflux.createStore({
        listenables: listenables,

        onFetched: function (data) {
            this.updateStore(data);
        },

        onSaved: function (data) {
            this.updateStore([data]);
        },

        onDeleted: function (data) {
            var i = _.findIndex(cache, y => y[idProp] === data[idProp]);
            if (i > -1) {
                cache = update(cache, { $splice: [[i, 1]]});
            }
            this.trigger(this.getState());
        },

        onFailedDelete: function(data) {
            console.log('Failed delete');
        },

        getInitialState: function () {
            return this.getState();
        },

        getState: function () {
            return cache;
        },

        updateStore: function (data) {
            data.forEach(x => {
                //replace if found, add if not
                var i = _.findIndex(cache, y => y[idProp] === x[idProp]);
                cache = i > -1 ?
                    update(cache, { $splice: [[i, 1, x]]}) :
                    update(cache, { $push: [x] });
            });

            this.trigger(this.getState());
        }
    });
}

module.exports = {
    createSimpleStore: createSimpleStore
};
