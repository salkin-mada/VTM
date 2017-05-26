VTMAbstractDataManager {
	var name;
	var <context;
	var <items;//TEMP getter
	var oscInterface;
	var itemDeclarations;

	*dataClass{
		^this.subclassResponsibility(thisMethod);
	}

	//itemDeclarations is a VTMOrderedIdentityDictionary
	*new{arg context, itemDeclarations;
		^super.new.initAbstractDataManager(context, itemDeclarations);
	}

	//context is an instance of kind VTMContext or a symbol.
	//itemDeclarations is an array of Dictionaries.
	initAbstractDataManager{arg context_, itemDeclarations_;
		context = context_;
		itemDeclarations = itemDeclarations_;
		items = VTMOrderedIdentityDictionary.new;
		if(itemDeclarations_.notNil, {
			this.addItemsFromItemDeclarations(itemDeclarations_);
		});
	}

	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			newItem = this.class.dataClass.new(itemName, itemDeclaration, this);
			this.addItem(newItem);
		});
	}

	addItem{arg newItem;
		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			items.put(newItem.name, newItem);
		});
	}

	freeItem{arg itemName;
		if(this.hasItemNamed(itemName), {
			var removedItem;
			items[itemName].disable;//dissable actions and messages
			removedItem = items.removeAt(itemName);
			removedItem.free;
		});
	}

	hasItemNamed{arg key;
		^items.includesKey(key);
	}

	at{arg key;
		^items.at(key);
	}

	isEmpty{ ^items.isEmpty; }

	free{
		this.disableOSC;
		items.do(_.free);
		context = nil;
	}

	names{
		^items.collect(_.name);
	}

	name{ this.subclassResponsibility(thisMethod); }

	itemDeclarations{arg recursive;
		var result;
		if(recursive, {
			items.do({arg item;
				result = result.addAll([item.name, item.declaration]);
			});
		}, {
			items.do({arg item;
				result = result.addAll([item.name]);
			});
		});
		^result;
	}

	path{
		if(context.notNil, {
			if(context.isKindOf(VTMContext), {
				^context.fullPath;
			}, {
				"/%".format(context).asSymbol;
			});
		});
		^'/';
	}

	fullPath{
		^(this.path ++ this.leadingSeparator ++	this.name).asSymbol;
	}

	leadingSeparator{ ^':'; }

	enableOSC{
		//make OSC interface if not already created
		if(oscInterface.isNil, {
			oscInterface = VTMOSCInterface.new(this);
		});
		oscInterface.enable;
	}

	disableOSC{
		oscInterface.free;
		oscInterface = nil;
	}

	oscEnabled{
		^if(oscInterface.notNil, {
			oscInterface.enabled;
		}, {
			^nil;
		});
	}


	*makeDataManagerDeclaration{arg descriptions, valueDeclarations;
		var result = VTMOrderedIdentityDictionary[];
		descriptions.keysValuesDo({arg key, val;
			result.put(key, val);
			if(valueDeclarations.includesKey(key), {
				result[key].put(\value, valueDeclarations[key]);
			});
		});
		^result;
	}

}
