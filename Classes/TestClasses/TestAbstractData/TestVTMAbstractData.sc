TestVTMAbstractData : VTMUnitTest {

	*classesForTesting{
		^[
			VTMAttribute,
			VTMCommand,
			VTMReturn,
//			VTMMapping,
//			VTMDefinitionLibrary,
//			VTMRemoteNetworkNode,
//			VTMApplication,
//			VTMCue,
//			VTMHardwareDevice,
//			VTMScore,
//			VTMModule,
//			VTMScene
		];
	}

	*makeRandomParameters{arg params;
		var result = VTMParameters[];
		this.findTestedClass.parameterKeys.do({arg attrKey;
			var attrParams, attrVal;
			if(params.notNil and: {params.includesKey(attrKey)}, {
				attrParams = params.at(attrKey);
			});
			attrVal = this.makeRandomParameter(attrKey, attrParams);
			if(attrVal.notNil, {
				result.put(attrKey, attrVal);
			});
		});
		^result;
	}

	*makeRandomParametersForObject{arg object;
		var testClass, class;
		var result = VTMParameters[];
		class = object.class;
		testClass = this.findTestClass(class);

		//omit name
		object.description.keysValuesDo({arg attrKey, attrVal;
			"Object attr '%' - %".format(attrKey, attrVal).postln;
		});
	}

	*makeRandomParameter{arg key, params;
		var result;
		result = switch(key,
			\name, {this.makeRandomSymbol},
			\path, {this.makeRandomPath}
		);
		^result;
	}

	*makeRandomManagerObject{
		var result;
		var managerClass;
		managerClass = this.findTestedClass.managerClass;

		result = managerClass.new();
		^result;
	}

	test_initAbstractDataParameters{
		var obj, testParameters, managerObj;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;
			var managerClass = class.managerClass;
			//managerClass shouldn not be nil
			this.assert(managerClass.notNil,
				"[%] - found manager class for test class".format(class)
			);
			managerObj = managerClass.new;
			this.assert(managerObj.notNil,
				"[%] - made manager obj for test class".format(class)
			);

			testParameters = testClass.makeRandomParameters;
			obj = class.new(
				testName,
				testParameters
				//managerObj
			);
			//
			// //check if name initialized
			// this.assertEquals(
			// 	obj.name,
			// 	testName,
			// 	"[%] - init 'name' correctly".format(class)
			// );
			//
			// //check parameters equal
			// this.assertEquals(
			// 	obj.parameters,
			// 	testParameters,
			// 	"[%] - init 'parameters' correctly".format(class)
			// );
			//
			// //the manager object must be identical
			// this.assert(
			// 	obj.manager === managerObj,
			// 	"[%] - init 'manager' correctly".format(class)
			// );
			//
			obj.free;
			// managerObj.free;
		});
	}

	test_parametersNil{
		var obj, testParameters, managerObj;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;
			var managerClass = class.managerClass;

			managerObj = managerClass.new;

			testParameters = nil;
			obj = class.new(
				testName,
				testParameters,
				managerObj
			);

			//Parameters should be empty VTMParameters
			this.assertEquals(
				obj.parameters,
				VTMParameters[],
				"[%] - init nil 'parameters' to empty array".format(class)
			);

			obj.free;
			managerObj.free;

		});
	}

	test_DefaultParameters{

	}

	test_ParametersGet{
		var obj;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;
			var appendString;

			//Should work with both initlialized uninitialized(nil) parameters.
			[
				[testClass.makeRandomParameters,	"[pre-initialized parameters]"],
				[nil,	"[parameters init to nil]"],
			].do({arg items, i;
				var testParameters, appendString;
				#testParameters, appendString = items;
				//All classes should implement get methods for
				//every Parameters.
				testParameters = nil;
				obj = class.new(
					testName,
					testParameters
				);

				class.parameterKeys.do({arg paramKey;
					var testVal, oldVal;
					//does it respond to getters for every parameters?
					this.assert(
						obj.respondsTo(paramKey),//test getter
						"[%] - responded to parameters getter '%'".format(
							class, paramKey) ++ appendString
					);
					//check if test class has implemented random generation method for it
					try{
						testVal = testClass.makeRandomParameter(paramKey);
					} {|err|
						this.failed(thisMethod,
							Error("[%] - Error making random parameters value for '%'".format(class, paramKey)).throw;
						);
					};
					this.assert(
						testVal.notNil,
						"[%] - test class generated non-nil random value for attr '%'".format(
							class, paramKey) ++ appendString
					);

				});
				obj.free;
			});
		});
	}
}
