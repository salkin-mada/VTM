VTMCommandManager : VTMElementComponent {
	*dataClass{ ^VTMCommand; }
	name{ ^\commands; }

	doCommand{arg key...args;
		items[key].valueAction_(*args);
	}
}
