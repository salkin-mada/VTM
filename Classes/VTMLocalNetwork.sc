//Represents a local area network
VTMLocalNetwork{
	var <ip;
	var <broadcast;
	var <addr;

	*new{arg ip, broadcast;
		^super.newCopyArgs(ip, broadcast).init;
	}

	init{
		addr = NetAddr(ip, NetAddr.localAddr.port);
	}
}