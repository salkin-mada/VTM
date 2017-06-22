TestVTMJSON : VTMUnitTest {

	test_SingleValues{
		[
			Float.makeRandom32Bits,
			Float.makeRandom64Bits,
			Integer.makeRandom32Bits
		].do({arg testValue;
			this.assertEquals(
				testValue,
				VTMJSON.parse( VTMJSON.stringify(testValue)),
				"VTMJSON - stringify and parse kept value %[%] intact.".format(testValue, testValue.class)
			);
		});
	}

	test_CollectionsOfSingleValues{
		[Array, List].do({arg class;
			[
				{Float.makeRandom32Bits},
				{Float.makeRandom64Bits},
				{Integer.makeRandom32Bits}
			].collect(_ ! 10).as(class).do({arg testValue;
				this.assertEquals(
					testValue,
					VTMJSON.parse( VTMJSON.stringify(testValue) ),
					"VTMJSON - stringify and parse kept value %[%] intact.".format(testValue, testValue.class)
				);
			});

		});
	}

	test_NetAddr{
		var testValue = NetAddr("127.0.0.1", 57120);
		this.assertEquals(
			testValue,
			VTMJSON.parse( VTMJSON.stringify(testValue) ),
			"VTMJSON - stringify and parse kept value %[%] intact.".format(testValue, testValue.class)
		);
	}
	test_Dictionaries{
	}

	// test_RawArrays{}
	//
	// test_OrderedDictionaries{}
	//
	// test_Symbols{}
	//
	// test_Strings{}
	//
	// test_StringArrays{}
	//
	// test_SymbolArrays{}
}
