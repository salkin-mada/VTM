VTMParameter : VTMValueElement {
	*managerClass{ ^VTMParameterManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initParameter;
	}

	initParameter{
		if(declaration.includesKey(\type), {
			this.prInitValueObject;
		},{
			Error("[%] - Value type for parameter not defined.".format(this.fullPath)).throw;
		});
	}

	*attributeDescriptions{
		^super.attributeDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\type -> (type: \string, optional: false)//overrides superclass description
			]
		);
	}

	action_{arg val;
		valueObj.action_(val);
	}

	value_{arg val;
		valueObj.value_(val);
	}

	valueAction_{arg val;
		valueObj.valueAction_(val);
	}
}
