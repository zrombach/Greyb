// note: without the node_modules dontMock, even with tcomb-form listed in unmockedModulePathPatterns in package.json, will be unable to execute.
jest.dontMock("../Classification.js")
	.dontMock("../../../../node_modules/tcomb-form/node_modules/tcomb-validation")

describe('Classification tests', function () {

		it('testing infrastructure of something that needs tcomb-form', function () {
			var Classification = require("../Classification.js");
		});
});
