VTMDefinitionLibraryManager : VTMContextComponent {
	classvar <global;
	classvar <system;

	*initClass{
		var sysDefPaths;
		Class.initClassTree(VTM);
		Class.initClassTree(VTMDefinitionLibrary);
		//TODO: Read and init global library
		global = VTMDefinitionLibrary(\global, (path: VTM.vtmPath +/+ "Definitions"));

		//TODO: Read and init system libraries
		if(VTM.systemConfiguration.includesKey('definitionPaths'), {
			sysDefPaths = VTM.systemConfiguration.at('definitionPaths');
			sysDefPaths.do({arg sysDefPath;

			});
		})
	}

	*dataClass{ ^VTMDefinitionLibrary; }
	name{ ^\libraries; }
}
