//children may be Module
VTMModule : VTMComposableContext {

	*new{arg name, definition, attributes, manager;
		^super.new(name, definition, attributes, manager).initModule;
	}

	initModule{

		//it is only the "real" implementation classes that will know when it
		//has been properly initialized
		this.prChangeState(\initialized);
	}

	play{arg ...args;
		this.execute(\play, *args);
	} //temp for module definition hackaton

	stop{arg ...args;
		this.execute(\stop, *args);
	} //temp for module definition hackaton

}
