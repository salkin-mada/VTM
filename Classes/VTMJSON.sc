VTMJSON : JSON {

	*stringify { arg obj;
		var out;

		if(obj.isString, {
			^"<string> " ++ obj.asCompileString.reject(_.isControl).replace("\n", JSON.nl).replace("\t", JSON.tab);
		});
		if(obj.class === Symbol, {
			^"<symbol> %".format(obj.asString);
		});

		if(obj.isKindOf(Dictionary), {
			out = List.new;
			obj.keysValuesDo({ arg key, value;
				out.add( key.asString.asCompileString ++ ":" + VTMJSON.stringify(value) );
			});
			^("{" ++ (out.join(", ")) ++ "}");
		});

		if(obj.isNil, {
			^"null"
		});
		if(obj === true, {
			^"<bool> true"
		});
		if(obj === false, {
			^"<bool> false"
		});
		if(obj.isNumber, {
			if(obj.isNaN, {
				^"null"
			});
			if(obj === inf, {
				^"null"
			});
			if(obj === (-inf), {
				^"null"
			});
			if(obj.isFloat, {
				//using angle bracket for denoting the data type, 64 bit float
				^"<float> %%".format(*[obj.high32Bits, obj.low32Bits].collect(_.asHexString));
			});
			if(obj.isInteger, {
				^"<int> %".format(obj);
			});

			^obj.asString
		});
		if(obj.isKindOf(SequenceableCollection), {
			^"[" ++ obj.collect({ arg sub;
				VTMJSON.stringify(sub)
			}).join(", ")
			++ "]";
		});
		// if(obj.isKindOf(NetAddr), {
		// 	^"<netaddr> %".format(obj.generateIPString);
		// });

		// obj.asDictionary -> key value all of its members

		// datetime
		// "2010-04-20T20:08:21.634121"
		// http://en.wikipedia.org/wiki/ISO_8601

		("No JSON conversion for object" + obj).warn;
		^VTMJSON.stringify(obj.asCompileString)
	}

	*parse{arg str;
		var result;
		result = str;
		if(result.isKindOf(Symbol), {
			result = result.asString;
		});
		result = result.parseYAML;
		case
		{result.isString;} {result = this.parseYAMLValue(result);}
		{result.isKindOf(Collection)} {result = result.collect({arg item; this.parse(item);})}
		^result;
	}

	*parseYAMLValue{arg str;
		var result = str;
		case
		{"^<int> -?[0-9]+$".matchRegexp(str)}
		{
			result = str.drop(6).interpret;
		}
		{"^<float> [0-9a-fA-F]{16}$".matchRegexp(str)}
		{
			result = str.drop(8).clump(8).collect({arg it; "0x%".format(it).interpret});
			result = Float.from64Bits(*result);
		}
		{"^<float> [0-9a-fA-F]{8}$".matchRegexp(str)}
		{
			result = "0x%".format(str.drop(8)).interpret;
			result = Float.from32Bits(result);
		}
		{"^<symbol> .+$".matchRegexp(str)} {result = str.drop(9).asSymbol; }
		{"^<string> .+$".matchRegexp(str)} {result = str.drop(9).drop(1).drop(-1); }
		{"^<bool> true$".matchRegexp(str)} {result = true; }
		{"^<bool> false$".matchRegexp(str)} {result = false; }
		// {"^<netaddr> .+$".matchRegexp(str)} {result = NetAddr.newFromIPString(str.drop(10)); }
		{"^-?[0-9]+(?:\.[0-9]+)?$".matchRegexp(str)}//if number
		{
			if(str.asFloat == str.asInteger,
				{
					result = str.asInteger;
				}, {
					result = str.asFloat;
				}
			);
		}
		{"^0[xX][0-9a-fA-F]+$".matchRegexp(str)} {result = str.interpret; } //hex notation
		{"^true$".matchRegexp(str)} { result = true; }// yaml1.2 /json compatible booleans
		{"^false$".matchRegexp(str)} { result = false; }
		{ result = str.asString; };//convert to symbol by default
		^result;
	}
}
