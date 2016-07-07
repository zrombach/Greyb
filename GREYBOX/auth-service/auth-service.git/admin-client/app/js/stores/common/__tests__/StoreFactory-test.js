jest.dontMock('../StoreFactory.js');

var assign = require('object-assign');

function makeStore(ref, idProp) {
    var store = require('../StoreFactory').createSimpleStore(
        ref,
        require('reflux').createActions([
            'saved', 'fetched', 'deleted'
        ]),
        idProp
    );

    store.trigger = jest.genMockFunction();

    return store;
}

function makeRecord(idProp, val, otherStuff) {
    var record = {};
    record[idProp] = val;
    assign(record, otherStuff);

    return record;
}

describe('Simple Object Store', function () {
    it('emits the correct empty state', function () {
        var actual = makeStore('someRef', 'someProp').getInitialState();

        expect(actual).toEqual({'someRef': []});
    });

    it('emits the correct state when a collection is fetched', function () {
        var ref = 'someRef',
            idProp = 'someIdProp',
            store = makeStore(ref, idProp),
            collection = [
                makeRecord(idProp, 'one', { otherProp: 'foo' }),
                makeRecord(idProp, 'two', { otherProp: 'bar' }),
                makeRecord(idProp, 'three', { otherProp: 'blah' })
            ],
            expected = {};

        expected[ref] = collection;

        store.onFetched(collection);

        expect(store.trigger).lastCalledWith(expected);
    });

    it('emits the correct state when an object is saved', function () {
        var ref = 'someRef',
            idProp = 'someIdProp',
            store = makeStore(ref, idProp),
            collection = [
                makeRecord(idProp, 'one', { otherProp: 'foo' })
            ],
            newRecord = makeRecord(idProp, 'two', { otherProp: 'bar' }),
            expected = {};

        store.onFetched(collection);
        store.onSaved(newRecord);

        collection.push(newRecord);
        expected[ref] = collection;


        expect(store.trigger).lastCalledWith(expected);
    });

    it('emits the correct state when an object is updated', function () {
        var ref = 'someRef',
            idProp = 'someIdProp',
            store = makeStore(ref, idProp),
            collection = [
                makeRecord(idProp, 'one', { otherProp: 'foo' })
            ],
            newRecord = makeRecord(idProp, 'one', { otherProp: 'bar' }),
            expected = {};

        store.onFetched(collection);
        store.onSaved(newRecord);

        expected[ref] = [newRecord];

        expect(store.trigger).lastCalledWith(expected);
    });

    it('emits the correct state when an object is deleted', function () {
        var ref = 'someRef',
            idProp = 'someIdProp',
            store = makeStore(ref, idProp),
            record = makeRecord(idProp, 'one', { otherProp: 'foo' }),
            expected = {};

        store.onFetched([record]);
        store.onDeleted(record);

        expected[ref] = [];

        expect(store.trigger).lastCalledWith(expected);
    });
});
