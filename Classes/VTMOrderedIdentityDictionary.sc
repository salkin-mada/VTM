VTMOrderedIdentityDictionary : IdentityDictionary {
	var <order;

	put{arg key, value;
		if(this.includesKey(key).not, {
			order = order.add(key);
		});
		^super.put(key, value);
	}

	keysValuesArrayDo { arg argArray, function;
		var arr;
		if(this.isEmpty.not, {
			arr = [
				order,
				order.collect({arg item; this.at(item); })
			].lace;
			super.keysValuesArrayDo(arr, function);
		});
	}

	keys { arg species(Array);
		^super.keys(species);
	}

	values {
		var list = List.new(size);
		this.do({ arg value; list.add(value) });
		^list
	}

	sorted{
		var result = this.class.new(size);
		order.sort.do({arg item;
			result.put(item, this.at(item));
		});
		^result;
	}

	//adding additional check for equal order
	== {arg what;
		var result = super == what;
		if(result.not, { ^false; });
		if(order != what.order, {^false;});
		^true;
	}

	removeAt{arg key;
		if(order.includes(key), {
			order.remove(key);
		});
		super.removeAt(key);
	}
}