package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.CommandValidationException;

@Component
public class ConfigCommandsValidator {

	public void validateBasics(HashMap<String, Object> command) {
//	{"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		List<String> validTypes = new ArrayList<String>();
		validTypes.add("assignation");
		validTypes.add("query");
		
		boolean authIsWrong = !command.containsKey("auth") || ((List<String>)command.get("auth")).size() != 2;
		boolean typeIsWrong = !command.containsKey("type") || !validTypes.contains(command.get("type"));
		boolean quantityIsWrong = command.size() <= 2;
		
		if(authIsWrong || typeIsWrong || quantityIsWrong) {
			throw new CommandValidationException();
		}
	}
	
	public void validateQuery(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"query", "parameter":"caccess"}
		List<String> validCommands = new ArrayList<String>();
		validCommands.add("caccess");
		validCommands.add("multiplex");
		validCommands.add("silence");
		validCommands.add("leet");
		validCommands.add("hash");
		validCommands.add("rangeBlacklist");
		validCommands.add("loginsBlacklist");
		validCommands.add("ipBlacklist");
		validCommands.add("netBlacklist");
		
		if(!command.containsKey("parameter") || !validCommands.contains(command.get("parameter"))) {
			throw new CommandValidationException();
		}
	}
	
	public void validateCaccessCommand(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		List<String> caccess = (List<String>)command.get("caccess");
		Integer.valueOf(caccess.get(1));
		if(caccess.size() != 2 || jidIsWrong(caccess.get(0))) {
			throw new CommandValidationException();
		}
	}
	
	public void validateMultiplexCommand(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "multiplex":["jid","serverto"]}
		List<String> multiplex = (List<String>)command.get("multiplex");
		if(multiplex.size() != 2 || jidIsWrong(multiplex.get(0))) {
			throw new CommandValidationException();
		}
	}

	public void validateSilenceCommand(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "silence":"user"}
		if(jidIsWrong((String) command.get("silence"))) {
			throw new CommandValidationException();
		}
	}

	public void validateFilterCommand(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "filter":["leet/hash", "user", "on/off"]}
		List<String> validFilters = new ArrayList<String>();
		validFilters.add("leet");
		validFilters.add("hash");
		
		List<String> validStatus = new ArrayList<String>();
		validStatus.add("on");
		validStatus.add("off");
		
		List<String> filter = (List<String>) command.get("filter");
		
		if(!validFilters.contains(filter.get(0)) || jidIsWrong(filter.get(1)) ||
				!validStatus.contains(filter.get(2))) {
			throw new CommandValidationException();
		}
	}
	
	public void validateBlacklistCommand(HashMap<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
		List<String> blacklistCommand = (List<String>) command.get("blacklist");
		
		if(blacklistCommand.get(0).equals("range")) {
			validateRangeBlacklist(blacklistCommand);
		} else if(blacklistCommand.get(0).equals("logins")) {
			validateLoginsBlacklist(blacklistCommand);
		} else if(blacklistCommand.get(0).equals("ip")) {
			validateIpBlacklist(blacklistCommand);
		} else if(blacklistCommand.get(0).equals("net")) {
			validateNetBlacklist(blacklistCommand);
		} else {
			throw new CommandValidationException();
		}
	}
	
	private void validateRangeBlacklist(List<String> command) {
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
		
		LocalTime.parse(command.get(2));
		LocalTime.parse(command.get(3));
		if(command.size() != 4 || jidIsWrong(command.get(1))) {
			throw new CommandValidationException();
		}
	}
	
	private void validateLoginsBlacklist(List<String> command) {
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["logins","user","qty"]}
		Integer.valueOf(command.get(2));
		if(command.size() != 3 || jidIsWrong(command.get(1))) {
			throw new CommandValidationException();
		}
	}
	
	private void validateIpBlacklist(List<String> command) {
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["ip","dir"]}
		try {
			InetAddress.getByName(command.get(1));
		} catch (UnknownHostException e) {
			throw new CommandValidationException();
		}
		if(command.size() != 2) {
			throw new CommandValidationException();
		}
	}
	
	private void validateNetBlacklist(List<String> command) {
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["net","netid", "netmask"]}
		try {
			InetAddress.getByName(command.get(1));
		} catch (UnknownHostException e) {
			throw new CommandValidationException();
		}
		Integer netmask = Integer.valueOf(command.get(2));
		if(command.size() != 3 || (netmask < 0 || netmask > 255)) {
			throw new CommandValidationException();
		}
	}
	
	private boolean jidIsWrong(String jid) {
		//TODO validarlo
		return false;
	}
	
}
