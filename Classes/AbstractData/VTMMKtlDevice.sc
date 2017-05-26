// declaration must pass in mktlName and mktlDescription

VTMMKtlDevice : VTMHardwareDevice {

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initMKtlDevice;
	}

	initMKtlDevice{
		this.envir.use({
			var attributes,returns, virtualMKtl;
			virtualMKtl = MKtl( ~self.get( \mktlName ), ~self.get( \mktlDescription ).asString, tryOpenDevice: false );

			returns = virtualMKtl.inputElements.keys.asArray.sort.collect{ |it|
				var el = virtualMKtl.inputElements.at( it );
				it -> (
					type: \float,  // mktl always converts to 0 - 1 range as floats
					minValue: 0.0, // or el.deviceSpec.minval
					maxValue: 1.0, // or el.deviceSpec.maxval
					defaultValue: el.defaultValue,
					value: el.value
				)
			};

			attributes = virtualMKtl.outputElements.keys.asArray.sort.collect{ |it|
				var el = virtualMKtl.outputElements.at( it );
				it -> (
					type: \float,  // mktl always converts to 0 - 1 range as floats
					minValue: 0.0, // or el.deviceSpec.minval
					maxValue: 1.0, // or el.deviceSpec.maxval
					defaultValue: el.defaultValue,
					value: el.value
				);
			};

			~returns.putAll( VTMOrderedIdentityDictionary.with( *returns ) );
			~attributes.putAll( VTMOrderedIdentityDictionary.with( *attributes )  );

			~commands.put( \makeGui, ( action: { ~mktl.gui } ) );

			virtualMKtl.free;
		});
		this.prInitComponentsWithContextDefinition; /// this seems to fix it!
		"VTMMKtlDevice initialized".postln;
	}

	prepare {
		envir.use({
			// open desired device
			~mktl = MKtl( ~self.get( \mktlName ), ~self.get( \mktlDescription ).asString; );
			// set actions for each control: here an interesting question: do we need to set actions for controls not currently used?
			~mktl.inputElements.do{ |it|
				~self.return( it.name, it.value );
				it.action = { |el|
					~self.return( it.name, el.value ); // inEnvir?
				}.inEnvir;
			};
			// create an output action
			~setMKtlCtl = { |name, val|
				~mktl.outputElements.at( name ).value_( val );
			};
		});
		super.prepare;
	}

	free{
		envir.use({
			~mktl.free;
		});
		super.free;
	}


	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\mktlName -> (type: \string, optional: false),
			//TODO: Change the type to symbol when it is implemented
			\mktlDescription -> (type: \string, optional: false, strictlyTyped: true)
	   	]); 
	}

}
