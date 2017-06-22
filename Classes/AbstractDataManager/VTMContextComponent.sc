VTMContextComponent : VTMElementComponent {
	addItem{arg newItem;
		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			var newItemName = newItem.name;
			//If the manager has already registered a context of this name then
			//we free the old context.
			//TODO: See if this need to be scheduled/synced in some way.
			if(this.hasItemNamed(newItemName), {
				"Freeing item: % from %".format(newItemName, this.fullPath).postln;
				this.freeItem(newItemName);
			});
			super.addItem(newItem);
		});
	}

}
