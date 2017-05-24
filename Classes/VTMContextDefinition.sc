VTMContextDefinition {
	var definition;
	var context;

	*new{arg env, context;
		^super.new.initContextDefinition(env, context);
	}

	initContextDefinition{arg env_, context_;
		definition = env_ ? Environment[
			\parameters -> VTMOrderedIdentityDictionary.new,
			\attributes -> VTMOrderedIdentityDictionary.new,
			\commands -> VTMOrderedIdentityDictionary.new,
			\presets -> VTMOrderedIdentityDictionary.new,
			\cues -> VTMOrderedIdentityDictionary.new,
			\mappings -> VTMOrderedIdentityDictionary.new,
			\scores -> VTMOrderedIdentityDictionary.new
		];
		context = context_;
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
