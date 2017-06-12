VTMDefinitionLibrary : VTMElement {
	var <definitions; //TEMP getter

	*managerClass{ ^VTMDefinitionLibraryManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initDefinitionLibrary;
	}

	initDefinitionLibrary{
		definitions = this.readLibrary(this.get(\path));
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\path -> (type: \string, optional: false),
				\includedPaths -> (type: \array, itemType: \string),
				\excludedPaths -> (type: \array, itemType: \string)
			]
		);
	}

	*returnDescriptions{
		^super.returnDescriptions.putAll(
		   VTMOrderedIdentityDictionary[
			   \hasDefinition -> (type: \boolean)
		   ]
	   );
	}

	readLibrary{arg path;
		var result = VTMOrderedIdentityDictionary.new;
		var readEntry;
		readEntry = {arg entryPathName, res;
			case
			{entryPathName.isFile} {
				var defEnvir;
				if(".+_definition.scd$".matchRegexp(entryPathName.fileName), {
					var definitionName = entryPathName.fileName.findRegexp("(.+)_definition.scd$")[1][1].asSymbol;
					var loadedEnvir;
					try{
						loadedEnvir = File.loadEnvirFromFile(entryPathName.fullPath);
						if(loadedEnvir.isNil, {
							Error("Could not load environment from definition file: '%'".format(
								entryPathName
							)).throw;
						}, {
							res.put(definitionName, loadedEnvir);
						});
					} {
						"Could not compile definition file: '%'".format(entryPathName).warn;
					};
				});
			}
			{entryPathName.isFolder} {
				entryPathName.entries.do({arg item;
					readEntry.value(item, res);
				});
			};
		};
		if(File.exists(path), {
			PathName(path).entries.do({arg entry;
				readEntry.value(entry, result);
			});
		}, {
			Error("Did not find library folder: '%'".format(path).postln;).throw;
		});

		^result;
	}
}

