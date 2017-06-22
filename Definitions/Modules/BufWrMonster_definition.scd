~prototypes = ['AudioSource'];

~prepare = {arg module, condition;
	~server;
	~source = {|magi, freq, unstabl, rate, grains, dur, lag|
        var buf, sines, sweeper, bufWr, bufRd, clean, modulo, sig;
        buf=LocalBuf(1e4,2).clear;
        sines=SinGrain.ar(Dust.ar(grains),dur.lag2(lag),freq.lag2(lag));
        sweeper=Sweep.ar(BufRd.ar(2,buf,Saw.ar(freq.lag2(lag),unstabl.lag2(lag),0)),rate.lag2(lag));
        bufWr=BufWr.ar(SinOsc.ar([SinOsc.ar(0.1*buf)*0.1,magi.lag2(lag)]),buf,sweeper);
        bufRd=BufRd.ar(2,buf,sweeper)/2;
        clean=Splay.ar(bufRd);
        modulo=Splay.ar(bufRd%sines)*0.2;
        sig = CombLP.ar(clean+modulo, gate: 1, maxdelaytime: 1, delaytime: 0.1, decaytime: 3, coef: 0.5, mul: 0.2);
		// stereo
        Splay.ar(sig);
	};
};

// is this needed?
~free = {arg module, condition;
    "freeing BufWrMonster".postln;
};

~initSynthArgs = [\magi, \freq, \unstabl, \rate, \dur, \lag];

~parameters = [
	(
		name: \magi,
		type: \decimal,
		action: {|p| ~output.set(\magi, p.value);
		},
		minVal: 0.01,
		maxVal: 20,
		defaultValue: 10,
	),
    (
		name: \freq,
		type: \decimal,
		action: {|p| ~output.set(\freq, p.value);
		},
		minVal: 0,
		maxVal: 2000,
		defaultValue: 1,
	),
    (
		name: \unstabl,
		type: \decimal,
		action: {|p| ~output.set(\unstabl, p.value);
		},
		minVal: 1,
		maxVal: 500,
		defaultValue: 1,
	),
    (
		name: \rate,
		type: \decimal,
		action: {|p| ~output.set(\rate, p.value);
		},
		minVal: 1,
		maxVal: 2e4,
		defaultValue: 200,
	),
    (
		name: \grains,
		type: \decimal,
		action: {|p| ~output.set(\grains, p.value);
		},
		minVal: 0.1,
		maxVal: 10,
		defaultValue: 5,
        clipmode: \both
	),
    (
		name: \dur,
		type: \decimal,
		action: {|p| ~output.set(\dur, p.value);
		},
		minVal: 0.01,
		maxVal: 5,
		defaultValue: 2,
        clipmode: \both
	),
    (
		name: \lag,
		type: \decimal,
		action: {|p| ~output.set(\lag, p.value);
		},
		minVal: 0.001,
		maxVal: 60,
		defaultValue: 5,
        clipmode: \low
	)
];
