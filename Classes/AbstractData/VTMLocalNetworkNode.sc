//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode : VTMAbstractDataManager {
	classvar <singleton;
	classvar <hostname;
	classvar <discoveryBroadcastPort = 57200;
	classvar <broadcastIPs;
	var <localNetworks;
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

	activate{arg discovery = false;

		if(discoveryReplyResponder.isNil, {
			discoveryReplyResponder = OSCFunc({arg msg, time, resp, addr;
				var jsonData = VTMJSON.parse(msg[1]);
				var senderHostname, netAddr, registered = false;
				senderHostname = jsonData["hostname"];
				topEnvironment[\jsonData] = jsonData;
				netAddr = NetAddr.newFromIPString(jsonData["addr"].asString);
				"We got a discovery message: % %".format(senderHostname, netAddr).postln;

				if(localNetworks.any({arg item; item.addr == netAddr;}), {
					"IT WAS LOCAL, ignoring it!".postln;
				}, {
					//a remote network node sent discovery
					var isAlreadyRegistered;
					var senderIPString = netAddr.generateIPString.asSymbol;
					isAlreadyRegistered = networkNodeManager.hasItemNamed(senderIPString);
					if(isAlreadyRegistered.not)
					{
						"Registering new network node: %".format([senderHostname, netAddr]).postln;
						networkNodeManager.addItemsFromItemDeclarations([
							senderIPString.asSymbol -> (hostname: senderHostname)
						]);
						this.discover(netAddr.port_(this.class.discoveryBroadcastPort));
					};

				});
				// }, {
				// "Got broadcastfrom local network node: %".format(this.getLocalAddr).postln;
				// });

			}, '/discovery', recvPort: this.class.discoveryBroadcastPort);

		});
		this.findLocalNetworks;

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
				"ifconfig | grep '\<inet\>' | awk '{print $2}'", "r") },
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

	findLocalNetworks{
		var lines, entries;
		lines = "ifconfig".unixCmdGetStdOutLines;
		//clump into separate network interface entries
		lines.collect({arg line;
			if(line.first != Char.tab, {
				entries = entries.add([line]);
			}, {
				entries[entries.size - 1] = entries[entries.size - 1].add(line);
			});
		});
		//remove the entries that don't have any extra information
		entries = entries.reject({arg item; item.size == 1});
		//remove the LOOPBACK entry(ies)
		entries = entries.reject({arg item;
			"[,<]?LOOPBACK[,>]?".matchRegexp(item.first);
		});
		//get only the active entries
		entries = entries.reject({arg item;
			item.any{arg jtem;
				"status: inactive".matchRegexp(jtem);
			}
		});
		//get only the lines with IPV4 addresses
		entries = entries.collect({arg item;
			item.detect{arg jtem;
				"\\<inet\\>".matchRegexp(jtem);
			}
		});
		//remove all that are nil
		entries = entries.reject(_.isNil);

		//separate the addresses
		entries.collect({arg item;
			var ip, bcast;

			ip = item.copy.split(Char.space)[1];
			bcast = item.findRegexp("broadcast (.+)");
			bcast = bcast !? {bcast[1][1];};
			(
				ip: ip,
				broadcast: bcast
			)
		}).collect({arg item;
			localNetworks = localNetworks.add(VTMLocalNetwork.performWithEnvir(\new, item));
		});

	}

	name{
		^this.getLocalAddr.generateIPString;
	}

	fullPath{
		^'/';
	}

	discover {arg destinationAddr;
		//Broadcast discover to all network connections
		if(localNetworks.isNil, { ^this; });
		localNetworks.do({arg network;
			var data, targetAddr;

			data = (
				hostname: hostname,
				addr: NetAddr(network.ip, NetAddr.localAddr.port).generateIPString
			);

			// if the method argument is nil, the message is broadcasted

			if(destinationAddr.isNil, {
				targetAddr = NetAddr(
					network.broadcast,
					this.class.discoveryBroadcastPort
				);
			}, {
				targetAddr = destinationAddr;
			});

			//Makes the responder if not already made
			discoveryReplyResponder.value;
			this.sendMsg(
				targetAddr.hostname, targetAddr.port, '/discovery', data
			);
			postln([targetAddr.hostname, targetAddr.port, '/discovery', data]);
		});
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

