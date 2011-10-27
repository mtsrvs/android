package ar.edu.itba.it.pdc.config;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigCommandsProcessor {

	private ConfigLoader configLoader;
	private ObjectMapper mapper;
	private ConfigCommandsValidator validator;
	
	private static String OK = "{\"status\":\"OK\"}\n";
	private static String ERR = "{\"status\":\"ERR\"}\n";
		

	@Autowired
	public ConfigCommandsProcessor(ConfigLoader configLoader, ConfigCommandsValidator validator) {
		this.configLoader = configLoader;
		this.mapper = new ObjectMapper();
		this.validator = validator;
	}

	@SuppressWarnings("unchecked")
	public void process(SelectionKey key, ByteBuffer buf, String req) {
		HashMap<String, Object> request = new HashMap<String, Object>();

			try {
				request = mapper.readValue(req, new TypeReference<Map<String, Object>>() {});
				validator.validateBasics(request);
				if (authenticate(((List<String>) request.get("auth")))) {
					if (request.get("type").equals("query")) {
						validator.validateQuery(request);
						printQuery(key, buf, (String) request.get("parameter"));
					} else if (request.get("type").equals("assignation")) {
						if (request.containsKey("caccess")) {
							caccessCommand(key, buf, request);
						}
						if (request.containsKey("multiplex")) {
							multiplexCommand(key, buf, request);
						}
						if (request.containsKey("silence")) {
							silenceCommand(key, buf, request);
						}
						if (request.containsKey("filter")) {
							filterCommand(key, buf, request);
						}
						if (request.containsKey("blacklist")) {
							blacklistCommand(key, buf, request);
						}
					}
				} else {
					sendResponse(key, buf, "{\"status\":\"ERR\",\"data\":\"Wrong authentication.\"}\n");
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
				sendResponse(key, buf, ERR);
			} catch (Throwable e) {
				e.printStackTrace();
				sendResponse(key, buf, ERR);
			} 
	}

	public void sendResponse(SelectionKey key, ByteBuffer buf, String response) {
		if (response != "") {
			buf.clear();
			buf.put(response.getBytes());
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	private boolean authenticate(List<String> values) {
		String adminUsername = configLoader.getAdminUsername();
		String adminPassword = configLoader.getAdminPassword();
		
		if (values.get(0).equals(adminUsername) && values.get(1).equals(adminPassword)) {
			return true;
		}
		return false;
	}

	private void printQuery(SelectionKey key, ByteBuffer buf, String parameter) {
		String prop = configLoader.getProperty(parameter);
		String resp = "{\"status\":\"OK\",\"data\":" + prop + "}\n";
		sendResponse(key, buf, resp);
	}

	private void caccessCommand(SelectionKey key, ByteBuffer buf, HashMap<String, Object> request) {
		// {"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		validator.validateCaccessCommand(request);
		
		HashMap<String, String> currentCaccess = new HashMap<String, String>();
		
		try {
			String prop = configLoader.getProperty("caccess");
			if(prop != null) {
				currentCaccess = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			List<String> accessControl = (List<String>) request.get("caccess");
			currentCaccess.put(accessControl.get(0), accessControl.get(1));
			String newCaccess = mapper.writeValueAsString(currentCaccess);
			configLoader.setProperty("caccess", newCaccess);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}

	private void multiplexCommand(SelectionKey key, ByteBuffer buf, HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "multiplex":["jid","serverto"]}
		validator.validateMultiplexCommand(request);
		
		HashMap<String, String> currentMultiplex = new HashMap<String, String>();

		try {
			String prop = configLoader.getProperty("multiplex");
			if(prop != null) {
				currentMultiplex = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			List<String> multiplex = (List<String>) request.get("multiplex");
			currentMultiplex.put(multiplex.get(0), multiplex.get(1));
			String newMultiplex = mapper.writeValueAsString(currentMultiplex);
			configLoader.setProperty("multiplex", newMultiplex);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}

	private void silenceCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "silence":"user"}
		validator.validateSilenceCommand(request);
		
		List<String> currentSilence = new ArrayList<String>();

		try {
			String prop = configLoader.getProperty("silence");
			if(prop != null) {
				currentSilence = mapper.readValue(prop, new TypeReference<List<String>>() {});
			}
			String silence = (String) request.get("silence");
			currentSilence.add(silence);
			String newSilence = mapper.writeValueAsString(currentSilence);
			configLoader.setProperty("silence", newSilence);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);

	}

	private void filterCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "filter":["leet/hash", "user", "on/off"]}
		validator.validateFilterCommand(request);
		
		Map<String, String> currentFilter = new HashMap<String, String>(); 
		try {
			List<String> filter = (List<String>) request.get("filter");
			String prop = configLoader.getProperty(filter.get(0));
			if(prop != null) {
				currentFilter = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			currentFilter.put(filter.get(1), filter.get(2));
			String newFilter = mapper.writeValueAsString(currentFilter);
			configLoader.setProperty(filter.get(0), newFilter);
			
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}
	
	private void blacklistCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
		validator.validateBlacklistCommand(request);
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		if(blacklistCommand.get(0).equals("range")) {
			rangeBlacklist(key, buf, blacklistCommand);
		}
		if(blacklistCommand.get(0).equals("logins")) {
			loginsBlacklist(key, buf, blacklistCommand);
		}
		if(blacklistCommand.get(0).equals("ip")) {
			ipBlacklist(key, buf, blacklistCommand);
		}
		if(blacklistCommand.get(0).equals("net")) {
			netBlacklist(key, buf, blacklistCommand);
		}
	}

	private void rangeBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
//		rangeBlacklist = {"user":["from","to"]}
		HashMap<String, List<String>> currentRangeBlacklist = new HashMap<String, List<String>>();

		try {
			String prop = configLoader.getProperty("rangeBlacklist");
			if(prop != null) {
				currentRangeBlacklist = mapper.readValue(prop, new TypeReference<Map<String, List<String>>>() {});
			}
			List<String> userRange = new ArrayList<String>();
			userRange.add(blacklistCommand.get(2));
			userRange.add(blacklistCommand.get(3));
			currentRangeBlacklist.put(blacklistCommand.get(1), userRange);
			String newRangeBlacklist = mapper.writeValueAsString(currentRangeBlacklist);
			configLoader.setProperty("rangeBlacklist", newRangeBlacklist);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}
	
	private void loginsBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["logins","user","qty"]}
//		loginsBlacklist = {"user":"qty"}
		HashMap<String, String> currentLoginsBlacklist = new HashMap<String, String>();

		try {
			String prop = configLoader.getProperty("loginsBlacklist");
			if(prop != null) {
				currentLoginsBlacklist = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			currentLoginsBlacklist.put(blacklistCommand.get(1), blacklistCommand.get(2));
			String newLoginsBlacklist = mapper.writeValueAsString(currentLoginsBlacklist);
			configLoader.setProperty("loginsBlacklist", newLoginsBlacklist);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}
	
	private void ipBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["ip","dir"]}
//		ipBlacklist = ["ip"]
		List<String> currentIpBlacklist = new ArrayList<String>();

		try {
			String prop = configLoader.getProperty("ipBlacklist");
			if(prop != null) {
				currentIpBlacklist = mapper.readValue(prop, new TypeReference<List<String>>() {});
			}
			if(!currentIpBlacklist.contains(blacklistCommand.get(1))) {
				currentIpBlacklist.add(blacklistCommand.get(1));
			}
			String newIpBlacklist = mapper.writeValueAsString(currentIpBlacklist);
			configLoader.setProperty("ipBlacklist", newIpBlacklist);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}
	
	private void netBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["net","netid", "netmask"]}
//		netBlacklist = {["origin","port"]}
		boolean netIsNew = true;
		List<List<String>> currentNetBlacklist = new ArrayList<List<String>>();

		try {
			String prop = configLoader.getProperty("netBlacklist");
			if(prop != null) {
				currentNetBlacklist = mapper.readValue(prop, new TypeReference<List<List<String>>>() {});
			}
			List<String> newBlackNet = new ArrayList<String>();
			newBlackNet.add(blacklistCommand.get(1));
			newBlackNet.add(blacklistCommand.get(2));
			for(List<String> net : currentNetBlacklist) {
				if(net.get(0).equals(newBlackNet.get(0)) && net.get(1).equals(newBlackNet.get(1))) {
					netIsNew = false;
				}
			}
			if(netIsNew) {
				currentNetBlacklist.add(newBlackNet);
				String newNetBlacklist = mapper.writeValueAsString(currentNetBlacklist);
				configLoader.setProperty("netBlacklist", newNetBlacklist);
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(key, buf, ERR);
			return;
		}
		sendResponse(key, buf, OK);
	}
	
}
