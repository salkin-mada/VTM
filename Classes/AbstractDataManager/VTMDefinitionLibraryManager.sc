VTMDefinitionLibraryManager : VTMContextComponent {
	classvar <global;
	classvar <system;

	*initClass{
		Class.initClassTree(VTMDefinitionLibrary);
		//TODO: Read and init global library
		global = VTMDefinitionLibrary(\global, (path: VTM.vtmPath +/+ "Definitions"));

		//TODO: Read and init system libraries
	}

	*dataClass{ ^VTMDefinitionLibrary; }
	name{ ^\libraries; }
}
