//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode : VTMAbstractDataManager {
	classvar <singleton;
	classvar <discoveryBroadcastPort = 57200;
	classvar <broadcastIPs;
	var <hostname;
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
		hostname = Pipe("hostname", "r").getLine();
		if(".local$".matchRegexp(hostname), {
			hostname = hostname.drop(-6);
		});
		this.findLocalNetworks;
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
					if(isAlreadyRegistered.not, {
						"Registering new network node: %".format([senderHostname, netAddr]).postln;
						networkNodeManager.addItemsFromItemDeclarations([
							senderHostname.asSymbol ->  (ip: senderIPString)
						]);
						this.discover(netAddr.port_(this.class.discoveryBroadcastPort));
					});

				});
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
				"ifconfig | grep broadcast | awk '{print $NF}'")
			},
			\windows, { unixCmdGetStdOut(
				"ifconfig | grep broadcast | awk '{print $NF}'")
			},
			\linux, { unixCmdGetStdOut(
				"/sbin/ifconfig | grep Bcast | awk 'BEGIN {FS = \"[ :]+\"}{print $6}'").stripWhiteSpace()
			}
		);
	}

	getLocalIp {

		// check BSD, may vary ...
		var line, lnet = false, lnet_ip;

		var addr_list = Platform.case(
			\osx, { Pipe(
				"ifconfig | grep '\<inet\>' | awk '{print $2}'", "r")
			},
			\linux, { Pipe (
				"/sbin/ifconfig | grep 'inet addr' | cut -d: -f2 | awk '{print $1}'","r")
			},
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

		Platform.case(
			\osx, {
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
					item.any({arg jtem;
						"status: inactive".matchRegexp(jtem);
					})
				});
				//get only the lines with IPV4 addresses and
				entries = entries.collect({arg item;
					var inetLine, hwLine;
					inetLine = item.detect({arg jtem;
						"\\<inet\\>".matchRegexp(jtem);
					});
					if(inetLine.notNil, {
						hwLine = item.detect({arg jtem;
							"\\<ether\\>".matchRegexp(jtem);
						})
					});
					[inetLine, hwLine];
				});
				//remove all that are nil
				entries = entries.reject({arg jtem; jtem.first.isNil; });

				//separate the addresses
				entries.collect({arg item;
					var ip, bcast, mac;
					var inetLine, hwLine;
					#inetLine, hwLine = item;

					ip = inetLine.copy.split(Char.space)[1];
					bcast = inetLine.findRegexp("broadcast (.+)");
					bcast = bcast !? {bcast[1][1];};
					mac = hwLine.findRegexp("ether (.+)");
					mac = mac !? {mac[1][1]};
					(
						ip: ip.stripWhiteSpace,
						broadcast: bcast.stripWhiteSpace,
						mac: mac.stripWhiteSpace
					)
				}).collect({arg item;
					localNetworks = localNetworks.add(VTMLocalNetwork.performWithEnvir(\new, item));
				});
			},
			\linux, {
				var result;
				//"No find local network method ifr Linux yet!".warn;
				//clump into separate network interface entries
				lines.collect({arg line;
					if(line.first != Char.space, {
						entries = entries.add([line]);
					}, {
						entries[entries.size - 1] = entries[entries.size - 1].add(line);
					});
				});
				//remove empty lines
				entries = entries.reject{arg entry;
					var lineIsEmpty = false;
					if(entry.size == 1, {
						lineIsEmpty = entry.first.isString and: {entry.first.isEmpty};
					});
					lineIsEmpty;
				};
				//remove loopback device
				entries = entries.reject{arg entry;
					"Loopback".matchRegexp(entry.first);
				};
				//select only entries with IPV4 addresses
				entries = entries.select{arg entry;
					entry.any{arg line;
						"\\<inet\\> .+".matchRegexp(line);
					};
				};
				//Get the MAC addresses
				entries.do{arg entry;
					var mac, ip, broadcast;
					var inetLine, entryData;
					entryData = ();
					mac = entry.first.findRegexp("HWaddr (.+)");
					if(mac.notNil, {
						entryData.put(\mac, mac[1][1].stripWhiteSpace);
					}, {
						"Did not find MAC for entry: %".format(entry).warn;
					});
					inetLine = entry.detect{arg line;
						"\\<inet\\> .+".matchRegexp(line);
					};
					if(inetLine.notNil, {
						var regx;
						regx = inetLine.findRegexp("addr:(.+) Bcast:(.+) .+");
						if(regx.notEmpty, {
							entryData.put(\ip, regx[1][1].stripWhiteSpace);
							entryData.put(\broadcast, regx[2][1].stripWhiteSpace);
						}, {
							"Could not parse inet line for %\n\t%".format(
								entry.first, inetLine
							).postln;
						});
					}, {
						"Did not find IP and broadcast for %".format(
							String.newFrom(entry.flat)).postln;
					});
					result = result.add(entryData);
				};
				if(result.notNil, {
					result.do({arg item;
						localNetworks = localNetworks.add(
							VTMLocalNetwork.performWithEnvir(\new, item)
						);
					});
				});
			},
			\windows, {
				"No find local network method for Windows yet!".warn;
			}
		);



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
				hostname: this.hostname,
				ip: network.addr
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

	sendMsg{arg targetHostname, port, path ...data;
		//sending eeeeverything as typed YAML for now.
		NetAddr(targetHostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
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

