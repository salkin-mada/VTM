~prototypes =  [\MIDIDevice];
~prepare = {arg device, cond;
	"Preparing LPD8".postln;
	(1..4).collect({arg progNum;
		var result;
		(1..8).do({arg chanNum, i;
			result = result.add(
				"prog.%/knob.%".format(progNum, chanNum).asSymbol -> (
					number: (9..16).at(i), chan: progNum - 1, msgType: \control
				)
			);
			result = result.add(
				"prog.%/ccpad.%".format(progNum, chanNum).asSymbol -> (
					number: (1..8).at(i), chan: progNum - 1, msgType: \control
				)
			);
			result = result.addAll([
				"prog.%/pad.%/on".format(progNum, chanNum).asSymbol -> (
					number: [64, 66, 68, 69, 57, 59, 60, 62].at(i), chan: progNum - 1, msgType: \noteOn
				),
				"prog.%/pad.%/off".format(progNum, chanNum).asSymbol -> (
					number: [64, 66, 68, 69, 57, 59, 60, 62].at(i), chan: progNum - 1, msgType: \noteOff
				)
			]);
		});
		result;
	}).flat.do({arg item;
		var data = item.value;
		~device.addComponent(item.key, data[\chan], data[\number], data[\msgType]);
	});

	// ~device.trace(true);
};
