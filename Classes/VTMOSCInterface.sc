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
	var responder, compliant_responder;

	*new { |parent|

		if(parent.respondsTo(\fullPath).not, {
			NotYetImplementedError(
				"% has not implemented 'fullPath' method yet!"
				.format(parent.class)).throw;
		});

		postln(format("OSC Interface created for: %", parent.fullPath()));
		^super.newCopyArgs(parent);
	}

	*makeOSCPathCompliant { |path|
		var res = path.asString().replace("/:", "/");
		if(res.contains(":")) { res = res.replace(":", "/") };
		^res
	}

	makeResponderFromParent {

		responder = OSCFunc({|msg, time, addr, recvport|
			var path = msg[0];
			msg = msg.drop(1);
			postln(format("OSC Message received at %, on port %, addressed to: %, with value: %",
				time, recvport, path, msg));
		}, parent.fullPath, recvPort: NetAddr.localAddr.port());

		compliant_responder = OSCFunc({|msg, time, addr, recvport|
			var path = msg[0];
			msg = msg.drop(1);
			postln(format("OSC Message received at %, on port %, addressed to: %, with value: %",
				time, recvport, path, msg));
		}, VTMOSCInterface.makeOSCPathCompliant(parent.fullPath.asString()),
		recvPort: NetAddr.localAddr.port());
	}

	enable {
		enabled = true;
		this.makeResponderFromParent();
	}

	disable {
		enabled = false;
	}

	free {
		this.disable;
		parent = nil;
	}
}
