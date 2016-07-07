jest.dontMock('../User.js')
	.dontMock("../../../../node_modules/tcomb-form/node_modules/tcomb-validation")
	.dontMock("../../stores/common/StoreFactory.js")



function buildXMLHttpResponse (responseCode, data) {
	var deferred = new jQuery.Deferred();

	var FakeRequest = require("fakexmlhttprequest");
	
	var req = new FakeRequest();
	req.respond(responseCode);

	return req;
}

// current state:  UserAPI actually returns jqXHR, which supports then and failed (deferred) - not sure how to mock our response to handle...
xdescribe('UserActions', function () {

	var UserActions, UserStore, StoreFactory, $, user, UserAPI;

	beforeEach(function () {
		//$ = require('jquery'); // this one's mocked
		UserAPI = require('../../webapi/User.js');
		user = require('../../domain/User.js')


		UserActions = require('../User.js');

		/**
			Not mocking UserStore - maybe should...

			Instead went up a parent level...  
			Not mocking StoreFactory, because of how it returns an object - jest can't see it.
			  So instead, mocking the function we're checking
		**/
		//UserStore = require('../../stores/GlobalUserStore.js');
		StoreFactory = require('../../stores/common/StoreFactory.js').createSimpleStore('users');
		StoreFactory.onDeleted = jest.genMockFunction();
		//spyOn(StoreFactory, "onDeleted");		// can't spyOn, since it's not a true full mock.  

	})

	// current state:  UserAPI actually returns jqXHR, which supports then and failed (deferred) - not sure how to mock our response to handle...
	it('handle success for delete', function () {	

		UserAPI.deleteUser = jest.genMockFunction();
		UserAPI.deleteUser.mockReturnValueOnce(buildXMLHttpResponse(200));
		UserActions.delete.trigger(user);		// trigger synchronously.., rather than our default of asynchronous

		expect(StoreFactory.onDeleted).toBeCalled();


	});
	xit('handle response of not found for delete', function () {
		
		$.ajax.mockReturnValueOnce(buildXMLHttpResponse(404));
		UserActions.delete(user)
		
		expect(StoreFactory.onDeleted).not.toHaveBeenCalled();

	});

	xit('handle response of server error for delete', function() {

	});

});
