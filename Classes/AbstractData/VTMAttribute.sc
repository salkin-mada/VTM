VTMAttribute : VTMValueElement {
	*managerClass{ ^VTMAttributeManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initAttribute;
	}

	initAttribute{}

}
