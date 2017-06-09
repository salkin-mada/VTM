VTMElementComponent : VTMAbstractDataManager{
	//This method overrides the superclass
	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			var itemAction;
			//The action is a part of the ValueElements declaration
			//but only relevant to the Element it is declared in.
			//Therefor we extract the action here and wrap it into a function
			//that also includes the context as the second argument.
			itemAction = itemDeclaration.removeAt(\action);
			newItem = this.class.dataClass.new(itemName, itemDeclaration, this);
			"Made new item: %".format(newItem).postln;
			if(itemAction.notNil, {
				//If this object is in a context we bind the item action
				//to the context environment so that environment variables
				//can be used inside the defined action.
				if(context.notNil, {
					itemAction = context.prContextualizeFunction(itemAction);
				});
				newItem.action_({arg item;
					itemAction.value(newItem, context);
				});
			});
			this.addItem(newItem);
		});
	}
}
