VTMMapping : VTMElement {
	var source;
	var destination;
	*managerClass{ ^VTMMappingManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initMapping;
	}

	initMapping{
		source = VTMMappingSource.make(this.get(\source));
		destination = VTMMappingDestination.make(this.get(\destination));

		source.map(destination);
	}

	enable{
		switch(this.get(\type),
			\forwarding, {
				//send the data from the source to the destination
				source.addForwarding(destination);
			},
			\subscription, {
				//subscribe to the data from the destination
				source.addSubscription(destination);
			},
			\bind, {
				//both source and destination subscribes to eachother
				source.addForwarding(destination);
				source.addSubscriptions(destination);
			},
			\exclusiveBind, {
				//both source and destination subscribes to eachother exclusively
				source.addForwarding(destination, exclusive: true);
				source.addSubscriptions(destination, exclusive: true);
			}
		);
		super.enable;
	}

	disable{

		super.disable;
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\source -> (type: \string, optional: false),
				\destination -> (type: \string, optional: false),
				\type -> (type: \string, optional: true,
					default: \forwarding,
					enum: [\forwarding, \subscription, \bind, \exclusiveBind])
			]
		);
	}

	*attributeDescriptions{
		^super.attributeDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				(name: \enabled, type: \boolean, defaultValue: true, action: {
					arg attr, mapping;
					if(attr.value, {
						mapping.enable;
					}, {
						mapping.disable;
					});
				})
			]
		);
	}
}
