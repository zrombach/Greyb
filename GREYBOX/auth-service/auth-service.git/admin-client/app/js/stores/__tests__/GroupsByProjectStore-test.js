jest
    .dontMock('../GroupsByProjectStore.js')
    .dontMock('../../util/Group.js')
    .dontMock('../common/StoreFactory.js')
    .dontMock('../GlobalGroupStore.js');

function makeAStore (project) {
    var store = require('../GroupsByProjectStore.js');

    store.trigger = jest.genMockFunction();
    store.onSetActiveProject(project);

    return store;
}

describe('Group Store', function () {
    it('emits the correct empty state', function () {
        var state = require('../GroupsByProjectStore.js').getInitialState();
        expect(state).toEqual({ projectGroups: [], activeProject: null });
    });

    it('emits the correct state when activeProject is set', function () {
        var project = { name: 'A Project'};
        var store = makeAStore(project);

        expect(store.trigger).lastCalledWith({
            activeProject: project,
            projectGroups: []
        });
    });


    it('emits the correct state when the group cache updates', function () {
        var project = { name: 'A Project'};
        var store = makeAStore(project);
        var groups = [
            { name: 'A Project!A Group' },
            { name: 'A Project!Another Group'}
        ];

        var anotherGroup = { name: 'Another Project!A Group' }
        var groupsPlusOne = groups.concat(anotherGroup);

        store.onGroupCacheUpdate({ groups: groupsPlusOne });

        //should emit the active project and matching groups
        expect(store.trigger).lastCalledWith({
            activeProject: project,
            projectGroups: groups
        });

        var anotherProject = { name: 'Another Project' }
        store.onSetActiveProject(anotherProject);

        expect(store.trigger).lastCalledWith({
            activeProject: anotherProject,
            projectGroups: [anotherGroup]
        })
    });

    it('emits the correct state when the project cache updates', function () {
        var project = { name: 'A Project'};
        var store = makeAStore(project);
        var groups = [ { name: 'A Project!A Group' }];

        store.onGroupCacheUpdate({ groups: groups });

        var newProject = { name: 'A Project', foo: 'bar' };

        store.onProjectCacheUpdate({ projects: [newProject]});

        expect(store.trigger).lastCalledWith({
            activeProject: newProject,
            projectGroups: groups
        });
    });
});
