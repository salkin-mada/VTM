//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode : VTMAbstractDataManager {
	classvar <singleton;
	classvar <hostname;
	classvar <discoveryBroadcastPort = 57200;
	classvar <remoteNetworkNodes;
	var discoveryReplyResponder;
	var <networkNodeManager;
	var <hardwareSetup;
	var <moduleHost;
	var <sceneOwner;
	var <scoreManager;

	*dataClass{ ^VTMApplication; }

	*initClass{
		Class.initClassTree(VTMAbstractData);
		Class.initClassTree(VTMNetworkNodeManager);
		hostname = Pipe("hostname", "r").getLine();
		singleton = super.new.initLocalNetworkNode;
	}

	*new{
		^singleton;
	}

	initLocalNetworkNode{
		networkNodeManager = VTMNetworkNodeManager.new(this);
		hardwareSetup = VTMHardwareSetup.new(this);
		moduleHost = VTMModuleHost.new(this);
		sceneOwner = VTMSceneOwner.new(this);
		scoreManager = VTMScoreManager.new(this);

		NetAddr.broadcastFlag = true;
	}

	activate{arg val, discovery = false;

		if(discoveryReplyResponder.isNil, {
			discoveryReplyResponder = OSCFunc({arg msg, time, resp, addr;
				var jsonData = VTMJSON.parse(msg[1]);
				var senderHostName, netAddr, registered = false;
				senderHostName = jsonData["hostname"];
				topEnvironment[\jsonData] = jsonData;
				netAddr = NetAddr.newFromIPString(jsonData["addr"].asString);
				"We got a discovery message: % %".format(senderHostName, netAddr).postln;
				"The local Addr: %".format(this.getLocalAddr).postln;

				if(netAddr != this.getLocalAddr, {
					var registered;
					registered = this.class.remoteNetworkNodes.includesKey(senderHostName);
					if(registered.not)
					{
						"Registering new network node: %".format([senderHostName, netAddr]).postln;
						networkNodeManager.addItemsFromItemDeclarations([
							netAddr.generateIPString.asSymbol -> (hostname: hostname)
						]);
						this.discover(netAddr);
					};
				}, {
					"Got broadcastfrom local network node: %".format(this.getLocalAddr).postln;
				});

			}, '/discovery', recvPort: this.class.discoveryBroadcastPort);

		});

		if(discovery) { this.discover(); }
	}

	deactivate{
		discoveryReplyResponder !? {discoveryReplyResponder.free;};
	}

	applications{ ^items; }

	getBroadcastIp {
		^Platform.case(
			\osx, { unixCmdGetStdOut(
				"ifconfig | grep broadcast | awk '{print $NF}'") },
			\windows, { unixCmdGetStdOut(
				"ifconfig | grep broadcast | awk '{print $NF}'") },
			\linux, { unixCmdGetStdOut(
				"/sbin/ifconfig | grep Bcast | awk 'BEGIN {FS = \"[ :]+\"}{print $6}'").stripWhiteSpace()}
		);
	}

	getLocalIp {

		// check BSD, may vary ...
		var line, lnet = false, lnet_ip;

		var addr_list = Platform.case(
			\osx, { Pipe(
				"ifconfig | grep 'inet' | awk '{print $2}'", "r") },
			\linux, { Pipe (
				"/sbin/ifconfig | grep 'inet addr' | cut -d: -f2 | awk '{print $1}'","r")},
			\windows, {}
		);

		var data;
		var targetAddr;

		line = addr_list.getLine();

		while({line.notNil()})
		{
			// check ipv4 valid address patterns
			lnet = "[0-9]{2,}\.[0-9]{1,}\.[0-9]{1,}\.[1-9]{1,}"
			.matchRegexp(line);

			// if valid, check whether localhost or lnet
			if(lnet)
			{
				if(line != "127.0.0.1")
				{ lnet_ip = line; }
			};

			lnet_ip ?? { line = addr_list.getLine(); };
			lnet_ip !? { line = nil }
		};

		lnet_ip !? { ^lnet_ip };
		lnet_ip ?? { ^"127.0.0.1" };

	}

	getLocalAddr{
		^NetAddr(this.getLocalIp, NetAddr.localAddr.port);
	}

	name{
		^this.getLocalAddr.generateIPString;
	}

	fullPath{
		^'/';
	}

	discover {arg destinationAddr;

		var data, targetAddr;

		data = (
			hostname: hostname,
			addr: NetAddr(this.getLocalIp, NetAddr.localAddr.port).generateIPString
		);

		// if the method argument is nil, the message is broadcasted

		if(destinationAddr.isNil(), {
			targetAddr = NetAddr(
				this.getBroadcastIp,
				this.class.discoveryBroadcastPort
			);
		}, {
			targetAddr = destinationAddr;
		});

		//Makes the responder if not already made
		discoveryReplyResponder.value;
		this.sendMsg(
			targetAddr.hostname, this.class.discoveryBroadcastPort, '/discovery', data
		);
		postln([targetAddr.hostname, targetAddr.port, '/discovery', data]);
	}

	*leadingSeparator { ^$/; }

	sendMsg{arg hostname, port, path ...data;
		//sending eeeeverything as typed YAML for now.
		NetAddr(hostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
	}

	findManagerForContextClass{arg class;
		var managerObj;
		case
		{class.isKindOf(VTMModule.class) } {managerObj =  moduleHost; }
		{class.isKindOf(VTMHardwareDevice.class) } {managerObj =  hardwareSetup; }
		{class.isKindOf(VTMScene.class) } {managerObj =  sceneOwner; }
		{class.isKindOf(VTMScore.class) } {managerObj =  scoreManager; };
		"DID I Find: % \n\t%".format(managerObj, class).postln;
		^managerObj;
	}
}

