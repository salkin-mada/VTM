//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode : VTMAbstractDataManager {
	classvar <singleton;
	classvar <discoveryBroadcastPort = 57200;
	var <hostname;
	var <localNetworks;
	var discoveryReplyResponder;
	var remoteActivateResponder;
	var <networkNodeManager;
	var <hardwareSetup;
	var <moduleHost;
	var <sceneOwner;
	var <scoreManager;
	var <active = false;

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
		hostname = hostname.asSymbol;
		this.findLocalNetworks;
		NetAddr.broadcastFlag = true;
		StartUp.add({
			//Make remote activate responder
			remoteActivateResponder = OSCFunc({arg msg, time, addr, port;
				var hostnames = VTMJSON.parse(msg[1]);
				if(hostnames.detect({arg item;
					item == this.name;
				}).notNil, {
					"Remote VTM activation from: %".format(addr).postln;
					this.activate(doDiscovery: true);
				})
			}, '/activate', recvPort: this.class.discoveryBroadcastPort);
		});
	}

	activate{arg doDiscovery = false, remoteNetworkNodesToActivate;

		if(discoveryReplyResponder.isNil, {
			discoveryReplyResponder = OSCFunc({arg msg, time, resp, addr;
				var jsonData = VTMJSON.parse(msg[1]).changeScalarValuesToDataTypes;
				var senderHostname, senderAddr, registered = false;
				senderHostname = jsonData['hostname'].asSymbol;
				senderAddr = NetAddr.newFromIPString(jsonData['addr'].asString);
				// "We got a discovery message: % %".format(senderHostname, senderAddr).postln;

				if(localNetworks.any({arg item; item.addr == senderAddr;}), {
					// "IT WAS LOCAL, ignoring it!".postln;
				}, {
					//a remote network node sent discovery
					var isAlreadyRegistered;
					isAlreadyRegistered = networkNodeManager.hasItemNamed(senderHostname);
					if(isAlreadyRegistered.not, {
						var newNetworkNode;
						"Registering new network node: %".format([senderHostname, senderAddr]).postln;
						newNetworkNode = VTMRemoteNetworkNode(
							senderHostname,
							(
								addr: jsonData['addr'].asString,
								mac: jsonData['mac'].asString
							),
							networkNodeManager
						);
						newNetworkNode.discover;
					});
				});
			}, '/discovery', recvPort: this.class.discoveryBroadcastPort);
		});
		active = true;
		if(remoteNetworkNodesToActivate.notNil, {
			this.activateRemoteNetworkNodes(remoteNetworkNodesToActivate);
		});

		if(doDiscovery) { this.discover(); }

	}

	activateRemoteNetworkNodes{arg remoteHostnames;
		this.broadcastMsg('/activate', remoteHostnames);
	}

	deactivate{
		discoveryReplyResponder !? {discoveryReplyResponder.free;};
		active = false;
	}

	applications{ ^items; }

	findLocalNetworks{
		var lines, entries;

		//delete previous local networks
		localNetworks = [];
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
							).warn;
						});
					}, {
						"Did not find IP and broadcast for %".format(
							String.newFrom(entry.flat)).warn;
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
				"VTM has no local network method for Windows yet!".warn;
			}
		);



	}

	name{
		^this.hostname;
	}

	fullPath{
		^'/';
	}

	discover {arg targetHostname;
		//Broadcast discover to all network connections
		if(localNetworks.isNil, { ^this; });
		localNetworks.do({arg network;
			var data, targetAddr;

			data = (
				hostname: this.hostname,
				addr: network.addr.generateIPString,
				mac: network.mac
			);

			// if the method argument is nil, the message is broadcasted

			if(targetHostname.isNil, {
				targetAddr = NetAddr(
					network.broadcast,
					this.class.discoveryBroadcastPort
				);
			}, {
				targetAddr = NetAddr(targetHostname, this.class.discoveryBroadcastPort);
			});

			this.sendMsg(
				targetAddr.hostname, targetAddr.port, '/discovery', data
			);
		});
	}

	*leadingSeparator { ^$/; }

	sendMsg{arg targetHostname, port, path ...data;
		//sending eeeeverything as typed YAML for now.
		NetAddr(targetHostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
	}

	broadcastMsg{arg path ...data;
		if(localNetworks.notNil, {
			localNetworks.do({arg item;
				item.broadcastAddr.sendMsg(path, VTMJSON.stringify(data.unbubble));
			})
		});
	}

	findManagerForContextClass{arg class;
		var managerObj;
		case
		{class.isKindOf(VTMModule.class) } {managerObj =  moduleHost; }
		{class.isKindOf(VTMHardwareDevice.class) } {managerObj =  hardwareSetup; }
		{class.isKindOf(VTMScene.class) } {managerObj =  sceneOwner; }
		{class.isKindOf(VTMScore.class) } {managerObj =  scoreManager; };
		^managerObj;
	}
}
