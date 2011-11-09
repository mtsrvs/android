package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.util.SubnetUtils;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.CommandValidationException;

@Component
public class ConfigCommandsValidator {

	public void validateBasics(Map<String, Object> command) {
//	{"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		List<String> validTypes = new ArrayList<String>();
		validTypes.add("assignation");
		validTypes.add("query");
		validTypes.add("delete");
		
		@SuppressWarnings("unchecked")
		boolean authIsWrong = !command.containsKey("auth") || ((List<String>)command.get("auth")).size() != 2;
		boolean typeIsWrong = !command.containsKey("type") || !validTypes.contains(command.get("type"));
		boolean quantityIsWrong = command.size() <= 2;
		
		if(authIsWrong || typeIsWrong || quantityIsWrong) {
			throw new CommandValidationException();
		}
	}
	
	public void validateQuery(Map<String, Object> command) {
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
	
	@SuppressWarnings("unchecked")
	public void validateCaccessCommand(Map<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		if(command.get("type").equals("assignation")) {
			List<String> caccess = (List<String>)command.get("caccess");
			Integer.valueOf(caccess.get(1));
			if(caccess.size() != 2 || jidIsWrong(caccess.get(0))) {
				throw new CommandValidationException();
			}
		} else if(command.get("type").equals("delete")) {
//		{"auth":["admin","admin"],"type":"delete", "caccess":"user"}
			String caccess = (String)command.get("caccess");
			if(jidIsWrong(caccess)) {
				throw new CommandValidationException();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void validateMultiplexCommand(Map<String, Object> command) {
		if(command.get("type").equals("assignation")) {
//		{"auth":["admin","admin"],"type":"assignation", "multiplex":["jid","serverto","port"]}
			List<String> multiplex = (List<String>)command.get("multiplex");
			validatePort(multiplex.get(2));
			if(multiplex.size() != 3 || jidIsWrong(multiplex.get(0))) {
				throw new CommandValidationException();
			}
		} else if(command.get("type").equals("delete")) {
//		{"auth":["admin","admin"],"type":"assignation", "multiplex":"jid"}
			String multiplex = (String)command.get("multiplex");
			if(jidIsWrong(multiplex)) {
				throw new CommandValidationException();
			}
		}
	}
	
	private void validatePort(String portStr) {
		try {
			int port = Integer.valueOf(portStr);
			if(port < 0 || port > 65535) {
				throw new CommandValidationException();
			}
		} catch (NumberFormatException e) {
			throw new CommandValidationException();
		}
	}
	
	public void validateSilenceCommand(Map<String, Object> command) {
		if(command.get("type").equals("assignation")) {
//		{"auth":["admin","admin"],"type":"assignation", "silence":"user"}
			if(jidIsWrong((String) command.get("silence"))) {
				throw new CommandValidationException();
			}
		} else if(command.get("type").equals("delete")) {
//		{"auth":["admin","admin"],"type":"delete", "silence":"user"}
			if(jidIsWrong((String) command.get("silence"))) {
				throw new CommandValidationException();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void validateFilterCommand(Map<String, Object> command) {
		List<String> filter = (List<String>) command.get("filter");
		if(command.get("type").equals("assignation")) {
//		{"auth":["admin","admin"],"type":"assignation", "filter":["leet/hash", "user", "on/off"]}
			if(!getValidFilters().contains(filter.get(0)) || jidIsWrong(filter.get(1)) ||
					!getValidStatus().contains(filter.get(2))) {
				throw new CommandValidationException();
			}
		} else if(command.get("type").equals("delete")) {
//		{"auth":["admin","admin"],"type":"delete", "filter":["leet/hash", "user"]}
			if(!getValidFilters().contains(filter.get(0)) || jidIsWrong(filter.get(1))) {
				throw new CommandValidationException();
			}
		}
	}
	
	private List<String> getValidFilters() {
		List<String> validFilters = new ArrayList<String>();
		validFilters.add("leet");
		validFilters.add("hash");
		return validFilters;
	}
	
	private List<String> getValidStatus() {
		List<String> validStatus = new ArrayList<String>();
		validStatus.add("on");
		validStatus.add("off");
		return validStatus;
	}
	
	@SuppressWarnings("unchecked")
	public void validateBlacklistCommand(Map<String, Object> command) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","server"]}
		List<String> blacklistCommand = (List<String>) command.get("blacklist");
		
		if(command.get("type").equals("assignation")) {
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
		} else if(command.get("type").equals("delete")) {
//			{"auth":["admin","admin"],"type":"delete", "blacklist":["range","user"]}
			if(blacklistCommand.get(0).equals("range") || blacklistCommand.get(0).equals("logins")) {
				validateRangeAndLoginsBlacklistDelete(blacklistCommand);
			} else if(blacklistCommand.get(0).equals("ip")) {
				validateIpBlacklist(blacklistCommand);
			} else if(blacklistCommand.get(0).equals("net")) {
				validateNetBlacklist(blacklistCommand);
			} else {
				throw new CommandValidationException();
			}
		}
	}
	
	private void validateRangeBlacklist(List<String> command) {
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","server"]}
		
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
//		- {"auth":["admin","admin"],"type":"assignation", "blacklist":["net","netid/netmask"]}
		try {
			new SubnetUtils(command.get(1));
		} catch (Exception e) {
			throw new CommandValidationException();
		}
		if(command.size() != 2) {
			throw new CommandValidationException();
		}
	}
	
	private void validateRangeAndLoginsBlacklistDelete(List<String> command) {
//		{"auth":["admin","admin"],"type":"delete", "blacklist":["range","user"]}
//		{"auth":["admin","admin"],"type":"delete", "blacklist":["logins","user"]}
		if(command.size() != 2 || jidIsWrong(command.get(1))) {
			throw new CommandValidationException();
		}
	}
	
	private boolean jidIsWrong(String jid) {
		return validateJid(jid);
	}
	
	private boolean validateJid(String jid) {
		int domainBeggining = 0;
		int domainEnd = jid.length();
		
		if(jid.contains("@")) {
			validatePartLength(jid.substring(0, jid.indexOf("@")));
			domainBeggining = jid.indexOf("@") + 1;
		}
		if(jid.contains("/")) {
			validatePartLength(jid.substring(jid.indexOf("/") + 1, jid.length()));
			domainEnd = jid.indexOf("/");
		}
		validatePartLength(jid.substring(domainBeggining, domainEnd));
		return false;
	}
	
	private void validatePartLength(String part) {
		if(part.length() <= 0 || part.length() > 1023) {
			throw new CommandValidationException();
		}
	}
	
}
