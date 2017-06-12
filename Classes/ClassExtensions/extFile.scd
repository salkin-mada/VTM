+ File {
	*loadEnvirFromFile{arg pathName;
		var defFunc;
		if(this.exists(pathName).not, {
			Error("Filename not found").throw;
		});

		defFunc = thisProcess.interpreter.compileFile(pathName.asAbsolutePath);
		if(defFunc.isNil, { Error("Could not compile envir from file").throw; });
		^Environment.make(defFunc);
	}
}