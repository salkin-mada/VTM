VTMAttributeManager : VTMElementComponent {
	var presets;

	*dataClass{ ^VTMAttribute; }
	name{ ^\attributes; }

	*new{arg context, itemDeclarations;
		^super.new(context, itemDeclarations).initAttributeManager;
	}

	initAttributeManager{
		//presets = VTMPresetManager(itemDeclarations[\presets]);
	}
}
