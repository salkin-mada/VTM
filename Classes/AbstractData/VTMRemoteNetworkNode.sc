VTMRemoteNetworkNode : VTMElement {
	var <addr;

	*managerClass{ ^VTMNetworkNodeManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initRemoteNetworkNode;
	}

	initRemoteNetworkNode{
		addr = NetAddr.newFromIPString(this.get(\addr));
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\addr -> (type: \string, optional: false)
		]);
	}
}
