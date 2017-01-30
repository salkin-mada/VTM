TestVTMContextParameterManager : VTMUnitTest {

	*makeRandomPresetForParameterAttributes{arg attributes;
		var result = IdentityDictionary.new;
		attributes.do({arg attr;
			result.put(
				attr[\name],
				this.testclassForType(attr[\type]).makeRandomValue
			);
		});
		^result.asKeyValuePairs;
	}

	*makeRandomPresetForContext{arg context;
		var result = IdentityDictionary.new;
		context.parameters.do({arg paramName;
			var param;
			param = context.getParameter(paramName);
			result.put(paramName, this.testclassForType(param.type).makeRandomValue);
		});
		^result.asKeyValuePairs;
	}

	*makeRandomPresetArrayForContext{arg context;
		var result = IdentityDictionary.new;
		rrand(3,8).do{arg i;
			result.put(this.makeRandomString, this.makeRandomPresetForContext(context));
		};
		^result.asKeyValuePairs;
	}

	*makeRandomPresetArrayForParameterAttributes{arg attributes;
		var result = IdentityDictionary.new;
		rrand(3,8).do{arg i;
			result.put(this.makeRandomString, this.makeRandomPresetForParameterAttributes(attributes));
		};
		^result.asKeyValuePairs;
	}


	test_AddingAndRemovingPresets{
		var context, testPresets;
		var testPresetNames;
		context = TestVTMContext.makeRandomContext;
		context.prepare;

		//make some random presets
		testPresets = this.class.makeRandomPresetArrayForContext(context);
		testPresetNames = testPresets.clump(2).flop[0];

		testPresets.pairsDo({arg presetName, presetData;
			context.addPreset(presetData, presetName);
		});

		this.assertEquals(
			context.presets, testPresetNames,
			"ContextParameterManager added preset names correctly"
		);

		this.assertEquals(
			context.presetAttributes, testPresets,
			"ContextParameterManager returned preset attributes correctly"
		);

		context.free;
	}

	test_InitContextWithPresets{}


}