VTMError : Error{
	errorString {
		^"VTMERROR: " ++ what
	}
	errorPathString {
		^if(path.isNil) { "" } { "PATH:" + path ++ "\n" }
	}
}
