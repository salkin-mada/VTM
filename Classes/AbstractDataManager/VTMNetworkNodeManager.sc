//a singleton class
VTMNetworkNodeManager : VTMAbstractDataManager {

	*dataClass{ ^VTMRemoteNetworkNode; }

	name{ ^\networkNodes; }

	*sendToAll{arg ...args;
	}
}
