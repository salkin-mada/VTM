VTMValue {
	var enum;
	var <>action;
	var <selectedEnum;
	var scheduler;
	var properties;

	*prDefaultValueForType{
		^nil;
	}

	*typeToClass{arg val;
		^"VTM%Value".format(val.asString.capitalize).asSymbol.asClass;
	}

	*classToType{arg val;
		^val.name.asString.findRegexp("^VTM(.+)Value$")[1][1].toLower;
	}

	*type{
		\none;
	}

	type{
		^this.class.type;
	}

	*string{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*boolean{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*timecode{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*integer{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*decimal{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*array{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*dictionary{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*schema{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*list{arg properties; ^this.makeFromType(thisMethod.name, properties); }
	*tuple{arg properties; ^this.makeFromType(thisMethod.name, properties); }

	*makeFromProperties{arg properties;
		var dec = properties.deepCopy;
		var type = dec.removeAt(\type);
		^this.makeFromType(type, dec);
	}

	*makeFromType{arg type, properties;
		var class;
		class = this.typeToClass(type);
		if(class.notNil, {
			^class.new(properties);
		}, {
			^this.new(properties);
		});
	}

	*new{arg properties;
		^super.new.initValue(properties);
	}

	initValue{arg properties_;
		properties = VTMValueProperties.newFrom(properties_ ? []);
		if(properties.notEmpty, {
			if(properties.includesKey(\value), {
				this.value_(properties[\value]);
			});
			if(properties.includesKey(\defaultValue), {
				this.defaultValue_(properties[\defaultValue]);
			});
			if(properties.includesKey(\enum), {
				this.enum_(properties[\enum]);
			});
		});

	}

	//only non-abstract sub classes will implement this.
	isValidType{arg val;
		this.subclassResponsibility(thisMethod);
	}

	//set value to default
	reset{arg doActionUponReset = false;
		if(this.defaultValue.notNil, {
			this.value_(this.defaultValue);
			if(doActionUponReset, {
				this.doAction;
			});
		});
	}

	valueAction_{arg val;
		if(this.filterRepetitions, {
			var willDoAction = val != this.value; //check if new value is different
			this.value_(val);
			if(willDoAction, {
				this.doAction;
			});
		}, {
			this.value_(val);
			this.doAction;
		});
	}

	//Enabled by default.
	//Will enable action to be run
	enable{arg doActionWhenEnabled = false;
		this.enabled_(true);
		if(doActionWhenEnabled, {
			this.doAction;
		});
	}

	//Will disable action from being run
	disable{
		this.enabled_(false);
	}

	doAction{
		if(this.enabled, {
			action.value(this);
		});
	}

	ramp{arg val, time;
		if(scheduler.isPlaying, {
			scheduler.stop;
		});
		scheduler = fork{
			time.wait;
			this.valueAction_(val);
		};
	}

	addEnum{arg val, name, slot;
		enum.addItem(val, name, slot);
		this.changed(\enum);
	}

	removeEnum{arg slotOrName;
		if(enum.removedItem(slotOrName).notNil, {
			this.changed(\enum);
		});
	}

	moveEnum{arg slotOrName, toSlot;
		enum.moveItem(slotOrName, toSlot);
		this.changed(\enum);
	}

	setEnumName{arg slotOrName, itemName;
		enum.setItemName(slotOrName, itemName);
		this.changed(\enum);
	}

	changeEnum{arg slotOrName, newValue;
		enum.changeItem(slotOrName, newValue);
		this.changed(\enum);
	}

	getEnumValue{arg slotOrName;
		^enum[slotOrName];
	}

	*propertyKeys{
		^[\enabled, \filterRepetitions, \value, \defaultValue, \enum, \restrictValueToEnum];
	}

	set{arg key, val;
		properties.put(key, val);
		this.changed(\properties, key);
	}

	get{arg key;
		^properties[key];
	}

	properties{arg includeDefaultValues = true;
		var result = properties.deepCopy;
		if(includeDefaultValues, {
			this.class.propertyKeys.do({arg attrKey;
				var attrVal = this.perform(attrKey);
				//don't use the ones that are nil
				if(attrVal.notNil, {
					result.put(attrKey, attrVal);
				});
			});
		});

		^result;
	}

	//Attribute getters
	enabled{ ^this.get(\enabled) ? true; }
	enabled_{arg val; this.set(\enabled, val); }

	filterRepetitions{ ^this.get(\filterRepetitions) ? false; }
	filterRepetitions_{arg val; this.set(\filterRepetitions, val); }

	value{ ^this.get(\value) ? this.defaultValue; }
	value_{arg val;
		if(this.restrictValueToEnum, {
			if(this.enum.includes(val), {
				this.set(\value, val);
				this.changed(\value);
			});
		}, {
			this.set(\value, val);
			this.changed(\value);
		});
	}

	defaultValue{ ^this.get(\defaultValue) ? this.class.prDefaultValueForType; }
	defaultValue_{arg val; this.set(\defaultValue, val); }

	enum{ ^this.get(\enum); }
	enum_{arg val; this.set(\enum, val); }

	restrictValueToEnum{ ^this.get(\restrictValueToEnum) ? false; }
	restrictValueToEnum_{arg val; this.set(\restrictValueToEnum, val); }

}
