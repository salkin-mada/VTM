VTMRemoteNetworkNode : VTMElement {
	var <addr;

	*managerClass{ ^VTMNetworkNodeManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initRemoveNetworkNode;
	}

	initRemoveNetworkNode{
		addr = NetAddr.newFromIPString(this.get(\addr));
	}

	*parameterDescriptions{
		^VTMOrderedIdentityDictionary[
			\addr -> (type: \string, optional: false)
	   	];
	}
}
