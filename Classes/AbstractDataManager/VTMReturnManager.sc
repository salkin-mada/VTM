VTMReturnManager : VTMElementComponent{
	*dataClass{ ^VTMReturn; }
	name{ ^\returns; }

	return{arg key...args;
		items[key].valueAction_(*args);
	}

	query{arg key;
		^items[key].value;
	}
}
