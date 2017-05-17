VTMDefinitionLibrary : VTMElement {

	*managerClass{ ^VTMDefinitionLibraryManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initDefinitionLibrary;
	}

	initDefinitionLibrary{}

	*declarationKeys{
		^super.declarationKeys ++ [\includedPaths, \excludedPaths];
	}

	*attributeDescriptions{
		^super.attributeDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\includedPaths -> (type: \array, itemType: \string),
				\excludedPaths -> (type: \array, itemType: \string)
			]
		);
	}

	*queryDescriptions{
		^super.queryDescriptions.putAll(
		   VTMOrderedIdentityDictionary[
			   \hasDefinition -> (type: \boolean)
		   ]
	   );
	}

	//Attribute getters
	includedPaths{ ^this.get(\includedPaths); }
	includedPaths_{arg val; this.set(\includedPaths, val); }

	excludedPaths{ ^this.get(\excludedPaths); }
	excludedPaths_{arg val; this.set(\excludedPaths, val); }
}

