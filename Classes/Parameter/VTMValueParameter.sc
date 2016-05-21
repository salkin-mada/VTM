VTMValueParameter : VTMParameter {
	classvar <defaultValue;
	var <value;
	var <>typecheck = true;//for checking type when value is set, adds overhead and safety.
	var <>filterRepetitions = false;//only perform action when incoming value is unequal to current value.
	var <>defaultValue;

	//This is an abstract class, not to be used directly.
	//Use GenericParameter for parameters that can receive any value type.
	*new{arg name, description;
		^super.new(name, description).initValueParameter;
	}

	//this class will accept any type
	*isValidType{arg val; ^true; }

	initValueParameter{
		if(description.notNil, {
			if(description.includesKey(\defaultValue), {
				defaultValue = description[\defaultValue];
			});
			if(description.includesKey(\value), {
				value = description[\value];
			});
			if(description.includesKey(\filterRepetitions), {
				filterRepetitions = description[\filterRepetitions];
			});
		});
		if(defaultValue.isNil, {
			defaultValue = this.class.defaultValue.deepCopy;
		});
		if(value.isNil, {
			value = defaultValue;
		});
	}

	//set value to default
	reset{arg doActionUponReset = false;
		if(defaultValue.notNil, {
			this.value_(defaultValue);
			if(doActionUponReset, {
				this.doAction;
			});
		});
	}

	value_{arg val, omitTypecheck = false; //don't do typecheck if already performed in subclass
		if(typecheck or: {omitTypecheck.not}, {
			if(this.class.isValidType(val), {
				value = val;
			}, {
				"ValueParameter:value_ '%' - ignoring val because of invalid type: '%[%]'".format(
					this.fullPath, val, val.class
				).warn;
			});
		}, {
			value = val;
		});
	}

	valueAction_{arg val;
		if(filterRepetitions, {
			var willDoAction = val != value; //check if new value is different
			this.value_(val);
			if(willDoAction, {
				this.doAction;
			});
		}, {
			this.value_(val);
			this.doAction;
		});
	}

	free{
		value = nil;
		defaultValue = nil;
		super.free;
	}

	attributes{
		^super.attributes.putAll(IdentityDictionary[
			\value -> this.value,
			\defaultValue -> this.defaultValue,
			\filterRepetitions -> this.filterRepetitions,
			\typecheck -> this.typecheck
		]);
	}
}