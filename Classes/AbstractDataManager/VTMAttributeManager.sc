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

	set{arg key...args;
		items[key].valueAction_(*args);
	}

	get{arg key;
		^items[key].value;
	}
}
