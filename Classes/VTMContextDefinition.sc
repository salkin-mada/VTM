VTMContextDefinition {
	var definition;
	var context;

	*new{arg env, context;
		^super.new.initContextDefinition(env, context);
	}

	initContextDefinition{arg env_, context_;
		context = context_;
		definition = Environment[
			\parameters -> VTMOrderedIdentityDictionary.new,
			\attributes -> VTMOrderedIdentityDictionary.new,
			\commands -> VTMOrderedIdentityDictionary.new,
			\presets -> VTMOrderedIdentityDictionary.new,
			\returns -> VTMOrderedIdentityDictionary.new,
			\signals -> VTMOrderedIdentityDictionary.new,
			\cues -> VTMOrderedIdentityDictionary.new,
			\mappings -> VTMOrderedIdentityDictionary.new,
			\scores -> VTMOrderedIdentityDictionary.new
		];
		if(env_.notNil, {
			var envToLoad = env_.deepCopy;
			//Turn any arrays of Associations into OrderedIdentityDictionaries
			//Get the names of the components our context consists of.
			definition.keys.do({arg compName;
				if(envToLoad.includesKey(compName), {
					var itemDeclarations;
					//Remove the itemDeclarations for this component key
					//so that we can add the remaing ones after changes has been made.
					itemDeclarations = envToLoad.removeAt(compName);
					//If it is an array of associations we change it to OrderedeIdentotitDictionary
					if(itemDeclarations.isArray and: {itemDeclarations.every{arg it; it.isKindOf(Association); } }, {
						itemDeclarations.do({arg itemDeclaration;
							definition[compName].put(
								itemDeclaration.key,
							   	itemDeclaration.value
							);
						});
					}, {
						//otherwise we assume it is a kind of dictionary.
						//TODO: Handle dicationaries as arguments
					});
				});
			});
			definition.putAll(envToLoad);
		});
	}
	
	makeEnvir{
		^definition.deepCopy.put(\self, context);
	}

	parameters{
		^definition[\parameters];
	}
	attributes{
		^definition[\attributes];
	}
	commands{
		^definition[\command];
	}
	presets{
		^definition[\presets];
	}
	cues{
		^definition[\cues];
	}
	mappings{
		^definition[\mappings];
	}
	scores{
		^definition[\scores];
	}
}
