VTMAbstractData {
	var <name;
	var <manager;
	var parameters;
	var oscInterface;
	var path;
	var declaration;

	classvar viewClassSymbol = \VTMAbstractDataView;

	*managerClass{
		^this.subclassResponsibility(thisMethod);
	}

	*new{arg name_, declaration_, manager_;
		^super.new.initAbstractData(name_, declaration_, manager_);
	}

	*newFromDeclaration{arg declaration, manager;
		var dec = declaration.deepCopy;
		^this.new(dec.removeAt(\name), dec, manager);
	}

	initAbstractData{arg name_, declaration_, manager_;
		name = name_;
		manager = manager_;
		declaration = VTMDeclaration.newFrom(declaration_ ? []);
		this.prInitParameters;
		if(manager.notNil, {
			manager.addItem(this);
		});
	}

	prInitParameters{
		var tempAttr;
		this.class.parameterDescriptions.keysValuesDo({arg key, val;
			//check if parameter is defined in parameter values
			if(declaration.includesKey(key), {
				var checkType;
				var checkValue;
				var tempVal = VTMValue.makeFromProperties(val);
				//is type strict? true by default
				checkType = val[\strictType] ? true;
				if(checkType, {
					if(tempVal.isValidType(declaration[key]).not, {
						Error("Parameter value '%' must be of type '%'"
							.format(key, tempVal.type)).throw;
					});
				});
				//check if value is e.g. within described range.
				checkValue = val[\strictValid] ? false;
				if(checkValue, {
					if(tempVal.isValidValue(declaration[key]).not, {
						Error("Parameter value '%' is invalid"
							.format(key)).throw;
					});
				});
			}, {
				var optional;
				//if not check if it is optional, true by default
				optional = val[\optional] ? true;
				if(optional.not, {
					Error("Parameters is missing non-optional value '%'"
						.format(key)).throw;
				});
			});

		});
		parameters = VTMParameterManager.newFrom(declaration);
	}

	disable{
		this.disableForwarding;
		this.disableOSC;
	}

	enable{
		this.enableForwarding;
		this.enableOSC;
	}

	free{
		this.disableOSC;
		this.releaseDependants;
		this.release;
		parameters = nil;
		manager = nil;
	}

	addForwarding{arg key, addr, path, vtmJson = false;
		this.subclassResponsibility(thisMethod);
	}

	removeForwarding{arg key;
		this.subclassResponsibility(thisMethod);
	}

	removeAllForwardings{
		this.subclassResponsibility(thisMethod);
	}

	enableForwarding{
		this.subclassResponsibility(thisMethod);
	}

	disableForwarding{
		this.subclassResponsibility(thisMethod);
	}

	*parameterKeys{
		^this.parameterDescriptions.keys;
	}

	*parameterDescriptions{
		^VTMOrderedIdentityDictionary[
			\name -> (type: \string, optional: true),
			\path -> (type: \string, optional: true)
	   	];
	}

	parameters{
		^parameters.as(VTMParameters);
	}

	description{
		var result = VTMOrderedIdentityDictionary[
			\parameters -> this.class.parameterDescriptions,
		];
		^result;
	}

	declaration{
		this.subclassResponsibility(thisMethod);
	}

	makeView{arg parent, bounds, definition, settings;
		var viewClass = this.class.viewClassSymbol.asClass;
		//override class if defined in settings.
		if(settings.notNil, {
			if(settings.includesKey(\viewClass), {
				viewClass = settings[\viewClass];
			});
		});
		^viewClass.new(parent, bounds, definition, settings, this);
	}

	fullPath{
		^(this.path ++ this.leadingSeparator ++ this.name).asSymbol;
	}

	path{
		if(manager.isNil, {
			^parameters.at(\path);
		}, {
			^manager.fullPath;
		});
	}

	hasDerivedPath{
		^manager.notNil;
	}

	get{arg key;
		^parameters.at(key);
	}

	leadingSeparator{ ^'/'; }

	enableOSC {
		oscInterface !? { oscInterface.enable(); };
		oscInterface ?? { oscInterface = VTMOSCInterface(this).enable() };
	}


	disableOSC {
		oscInterface !? { oscInterface.free() };
		oscInterface = nil;
	}

	oscEnabled {
		^oscInterface.notNil();
	}
}
