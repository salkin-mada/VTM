VTM{
	classvar <systemConfiguration;

	*initClass{
		var configFilePath = "~/.vtm.conf.yaml".standardizePath;
		if(File.exists(configFilePath), {
			try{
				systemConfiguration = configFilePath.parseYAMLFile.changeScalarValuesToDataTypes;
			} {
				"Error reading VTM config file".warn;
			}
		}, {
			systemConfiguration = IdentityDictionary.new;
		});

	}

	*local{
		^VTMLocalNetworkNode.singleton;
	}

	*sendMsg{arg hostname, port, path ...data;
		this.local.sendMsg(hostname, port, path, *data);
	}

	*sendLocalMsg{arg path ...data;
		this.sendMsg(
			NetAddr.localAddr.hostname, NetAddr.localAddr.port,
			path, *data
		);
	}

	*activate{arg discovery = false, remoteNetworkNodesToActivate;
		this.local.activate(discovery, remoteNetworkNodesToActivate);
	}

	*deactivate{
		this.local.deactivate;
	}

	*discover{
		this.local.discover;
	}

	*vtmPath{ ^PathName(PathName(this.filenameSymbol.asString).parentPath).parentPath; }
}
