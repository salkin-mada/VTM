//All classes that uses objects of this class must define a .makeOSCAPI
//classmethod returning getter and setter methods and functions for those.
//In order to define the OSC path the user class needs to define:
//- .path method.
//- .name method
//- .leadingSeparator
//- *makeOSCAPI(obj)
VTMOSCInterface {

	var parent;
	var <enabled = false;
	var responder;

	*new { |parent|

		if(parent.respondsTo(\fullPath).not, {
			NotYetImplementedError(
				"% has not implemented 'fullPath' method yet!"
				.format(parent.class)).throw;
		});

		postln(format("OSC Interface created for: %", parent.fullPath()));

		^super.newCopyArgs(parent);
	}

	makeResponderFromParent {

		responder = OSCFunc({|msg, time, addr, recvport|
			var path = msg[0], values;
			msg = msg.drop(1);
			values = msg;

			postln(format("OSC Message received at %, on port %, addressed to: %, with value: %",
				time, recvport, path, values));
		}, parent.fullPath, recvPort: NetAddr.localAddr.port());

	}

	*prMakeResponders{arg model;
		var result = IdentityDictionary.new;
		/*
		model.class.makeOSCAPI(model).keysValuesDo({arg cmdKey, cmdFunc;
		var responderFunc, lastCmdChar, responderPath;
		lastCmdChar = cmdKey.asString.last;
		responderPath = model.fullPath;
		switch(lastCmdChar,
		$!, {
		responderFunc = {arg msg, time, addr, port;
		cmdFunc.value(model);
		};
		},
		$?, {
		responderFunc = {arg msg, time, addr, port;
		var queryHost, queryPath, queryPort;
		if(msg.size == 4, {
		var replyData;
		queryHost = msg[1].asString;
		queryPort = msg[2];
		queryPath = msg[3];
		replyData = cmdFunc.value(model);
		if(replyData.notNil, {
		if(replyData.isArray, {
		NetAddr(queryHost, queryPort).sendMsg(
		queryPath.asSymbol,
		*replyData
		);
		}, {
		NetAddr(queryHost, queryPort).sendMsg(
		queryPath.asSymbol,
		replyData
		);
		});
		});
		}, {
		"% command '%' OSC missing query addr data".format(
		model.class,
		responderPath
		).warn
		});
		};
		},
		//the default case is a setter method
		{
		responderFunc = {arg msg, time, addr, port;
		cmdFunc.value(msg[1..]);
		};
		}
		);
		result.put(
		cmdKey,
		OSCFunc(responderFunc, responderPath);
		);

		});
		*/
		result.put(\setters, this.prMakeSetterResponders(model));
		result.put(\queries, this.prMakeQueryResponders(model));
		result.put(\commands, this.prMakeCommandResponders(model));
		^result;
	}

	enable {
		enabled = true;
		this.makeResponderFromParent();
	}

	disable{
		enabled = false;
	}

	free {
		this.disable;
		parent = nil;
	}
}
