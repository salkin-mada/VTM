VTMValueElement : VTMAbstractData {
	var valueObj;
	var forwardings;
	var forwarder;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initValueElement;
	}

	initValueElement{
		var valueClass = VTMValue.typeToClass(declaration[\type]) ? VTMValue;
		var valueProperties = VTMOrderedIdentityDictionary.new;
		//extract property values from declaration
		valueClass.propertyKeys.do({arg propKey;
			if(declaration.includesKey(propKey), {
				valueProperties.put(propKey, declaration[propKey]);
			});
		});
		valueObj = VTMValue.makeFromType(declaration[\type], valueProperties);
		forwardings = VTMOrderedIdentityDictionary.new;
		this.enableForwarding;
	}

	action_{arg func;
		valueObj.action_(func);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\type -> (type: \string, optional: true)
			]
		);
	}

	valueAction_{arg ...args;
		valueObj.valueAction_(*args);
	}

	value_{arg ...args;
		valueObj.value_(*args);
	}

	value{
		^valueObj.value;
	}

	free{
		forwardings.clear;
		forwarder.remove(\value);
		valueObj.release;
		valueObj = nil;
	}

	type{
		^this.get(\type);
	}

	declaration{
		^valueObj.properties.putAll(parameters);
	}

	//setting the value object properties.
	set{arg key...args;
		valueObj.set(key, *args);
	}

	//getting the vaue object properties, or if not found
	//try getting the Element parameters
	get{arg key;
		var result;
		result = valueObj.get(key);
		if(result.isNil, {
			result = super.get(key);
		});
		^result;
	}

	addForwarding{arg key, addr, path, vtmJson = false;
		//Observe value object for changng values
		forwardings.put(key, (addr: addr, path: path, vtmJson: vtmJson));
	}

	removeForwarding{arg key;
		forwardings.removeAt(key);
	}

	removeAllForwardings{
		forwardings.clear;
	}

	disableForwarding{
		forwarder.remove(\value);
		forwarder.clear;
		forwarder = nil;
	}

	enableForwarding{
		forwarder = SimpleController(valueObj).put(\value, {arg theChanged;
			forwardings.do({arg item;
				if(item[\vtmJson], {
					VTM.sendMsg(item[\addr].hostname, item[\addr].port, item[\path], this.value);
				}, {
					if(this.type==\dictionary, {
						"VTMValueElement, forwarding, dictionaries must be sent as JSON".warn;
					});
					item[\addr].sendMsg(item[\path], *this.value);
				});
			});
		});
	}

	disable{
		super.disable;
		this.disableForwarding;
		valueObj.disable;
	}

	enable{
		super.enable;
		this.enableForwarding;
		valueObj.enable;
	}

}
