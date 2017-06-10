//Represents a local area network
VTMLocalNetwork{
	var <ip;
	var <broadcast;
	var <mac;
	var <addr;

	*new{arg ip, broadcast, mac;
		^super.newCopyArgs(ip, broadcast, mac).init;
	}

	init{
		addr = NetAddr(ip, NetAddr.localAddr.port);
	}
}