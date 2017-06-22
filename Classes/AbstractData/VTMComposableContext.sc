VTMComposableContext : VTMContext {
	var <children;

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initComposableContext;
	}

	initComposableContext{
		//TODO: init children here
	}

	free{
		children.do(_.free);
		super.free;
	}

	isSubcontext{
		if(manager.notNil, {
			^this.manager.isKindOf(this.class);
		});
		^false;
	}

	leadingSeparator{
		if(this.isSubcontext,
			{
				^'.';
			}, {
				^'/'
			}
		);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\exclusivelyOwned -> (type: \boolean, defaultValue: true)
		]);
	}

	*commandDescriptions{
		^super.commandDescriptions.putAll(VTMOrderedIdentityDictionary[
			\takeOwnership -> (type: \string), //which type to describe scene or application here?
			\releaseOwnership -> (type: \string)
		]);
	}

	*returnDescriptions{
		^super.returnDescriptions.putAll(VTMOrderedIdentityDictionary[
			\owner -> (type: \string)
		]);
	}
}
