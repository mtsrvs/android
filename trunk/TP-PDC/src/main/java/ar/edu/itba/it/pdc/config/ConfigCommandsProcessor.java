package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigCommandsProcessor {

	private ConfigLoader configLoader;
	private ObjectMapper mapper;
	private ConfigCommandsValidator validator;
	
	protected static enum Msg {
		OK("{\"status\":\"OK\"}\n"), ERR("{\"status\":\"ERR\"}\n");
		private String value;
		private Msg(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	
	@Autowired
	public ConfigCommandsProcessor(ConfigLoader configLoader, ConfigCommandsValidator validator) {
		this.configLoader = configLoader;
		this.mapper = new ObjectMapper();
		this.validator = validator;
	}

	@SuppressWarnings("unchecked")
	public void process(SelectionKey key, ByteBuffer buf, String req) {
		Map<String, Object> request = new HashMap<String, Object>();
		boolean success = true;
			try {
				request = mapper.readValue(req, new TypeReference<Map<String, Object>>() {});
				validator.validateBasics(request);
				if (authenticate(((List<String>) request.get("auth")))) {
					if (request.get("type").equals("query")) {
						validator.validateQuery(request);
						printQuery(key, buf, (String) request.get("parameter"));
					} else if (request.get("type").equals("assignation") ||
							request.get("type").equals("delete")) {
						if (request.containsKey("caccess")) {
							success &= caccessCommand(key, buf, request);
						}
						if (request.containsKey("multiplex")) {
							success &= multiplexCommand(key, buf, request);
						}
						if (request.containsKey("silence")) {
							success &= silenceCommand(key, buf, request);
						}
						if (request.containsKey("filter")) {
							success &= filterCommand(key, buf, request);
						}
						if (request.containsKey("blacklist")) {
							success &= blacklistCommand(key, buf, request);
						}
					}
				} else {
					success = false;
//					sendResponse(key, buf, "{\"status\":\"ERR\",\"data\":\"Wrong authentication.\"}\n");
				}
			} catch (Throwable e) {
				success = false;
			} 
			
			if(success) {
				configLoader.commit();
				sendResponse(key, buf, Msg.OK.getValue());
			} else {
				configLoader.revert();
				sendResponse(key, buf, Msg.ERR.getValue());
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

	private boolean caccessCommand(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
		// {"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		validator.validateCaccessCommand(request);
		
		Map<String, Integer> currentCaccess = new HashMap<String, Integer>();
		
		try {
			currentCaccess = configLoader.getCaccess();
			if(request.get("type").equals("assignation")) {
				List<String> accessControl = (List<String>) request.get("caccess");
				currentCaccess.put(accessControl.get(0), Integer.valueOf(accessControl.get(1)));
			} else if(request.get("type").equals("delete")) {
				currentCaccess.remove((String) request.get("caccess"));
			}
			String newCaccess = mapper.writeValueAsString(currentCaccess);
			configLoader.setProperty("caccess", newCaccess);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean multiplexCommand(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "multiplex":["jid","serverto"]}
		validator.validateMultiplexCommand(request);
		
		Map<String, InetAddress> currentMultiplex = new HashMap<String, InetAddress>();

		try {
			currentMultiplex = configLoader.getMultiplex();
			if(request.get("type").equals("assignation")) {
				List<String> multiplex = (List<String>) request.get("multiplex");
				currentMultiplex.put(multiplex.get(0), InetAddress.getByName(multiplex.get(1)));
			} else if(request.get("type").equals("delete")) {
				currentMultiplex.remove((String) request.get("multiplex"));
			}
			String newMultiplex = mapper.writeValueAsString(currentMultiplex);
			configLoader.setProperty("multiplex", newMultiplex);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean silenceCommand(SelectionKey key, ByteBuffer buf,
			Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "silence":"user"}
		validator.validateSilenceCommand(request);
		
		List<String> currentSilence = new ArrayList<String>();

		try {
			currentSilence = configLoader.getSilence();
			if(request.get("type").equals("assignation")) {
				if(!currentSilence.contains((String) request.get("silence"))) {
					currentSilence.add((String) request.get("silence"));
				}
			} else if(request.get("type").equals("delete")) {
				currentSilence.remove((String) request.get("silence"));
			}
			String newSilence = mapper.writeValueAsString(currentSilence);
			configLoader.setProperty("silence", newSilence);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean filterCommand(SelectionKey key, ByteBuffer buf,
			Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "filter":["leet/hash", "user", "on/off"]}
		validator.validateFilterCommand(request);
		
		Map<String, String> currentFilter = new HashMap<String, String>(); 
		try {
			List<String> filter = (List<String>) request.get("filter");
			if(filter.get(0).equals("leet")) {
				currentFilter = configLoader.getLeet();
			} else if(filter.get(0).equals("hash")) {
				currentFilter = configLoader.getHash();
			}
			if(request.get("type").equals("assignation")) {
				currentFilter.put(filter.get(1), filter.get(2));
			} else if(request.get("type").equals("delete")) {
//		{"auth":["admin","admin"],"type":"delete", "filter":["leet/hash", "user"]}
				currentFilter.remove(filter.get(1));
			}
			String newFilter = mapper.writeValueAsString(currentFilter);
			configLoader.setProperty(filter.get(0), newFilter);
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean blacklistCommand(SelectionKey key, ByteBuffer buf,
			Map<String, Object> request) {
		boolean success = true;
		validator.validateBlacklistCommand(request);
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","server"]}
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		if(blacklistCommand.get(0).equals("range")) {
			success &= rangeBlacklist(key, buf, request);
		}
		if(blacklistCommand.get(0).equals("logins")) {
			success &= loginsBlacklist(key, buf, request);
		}
		if(blacklistCommand.get(0).equals("ip")) {
			success &= ipBlacklist(key, buf, request);
		}
		if(blacklistCommand.get(0).equals("net")) {
			success &= netBlacklist(key, buf, request);
		}
		return success;
	}

	private boolean rangeBlacklist(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","server"]}
//		rangeBlacklist = {"user":["from","server"]}
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		Map<String, List<String>> currentRangeBlacklist = new HashMap<String, List<String>>();

		try {
			//TODO levantar del configloader, perderia el from y el to?
			String prop = configLoader.getProperty("rangeBlacklist");
			if(prop != null) {
				currentRangeBlacklist = mapper.readValue(prop, new TypeReference<Map<String, List<String>>>() {});
			}
			if(request.get("type").equals("assignation")) {
				List<String> userRange = new ArrayList<String>();
				userRange.add(blacklistCommand.get(2));
				userRange.add(blacklistCommand.get(3));
				currentRangeBlacklist.put(blacklistCommand.get(1), userRange);
			} else if(request.get("type").equals("delete")) {
				currentRangeBlacklist.remove(blacklistCommand.get(1));
			}
			String newRangeBlacklist = mapper.writeValueAsString(currentRangeBlacklist);
			configLoader.setProperty("rangeBlacklist", newRangeBlacklist);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean loginsBlacklist(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["logins","user","qty"]}
//		loginsBlacklist = {"user":"qty"}
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		Map<String, String> currentLoginsBlacklist = new HashMap<String, String>();

		try {
			currentLoginsBlacklist = configLoader.getLoginsBlacklist();
			if(request.get("type").equals("assignation")) {
				currentLoginsBlacklist.put(blacklistCommand.get(1), blacklistCommand.get(2));
			} else if(request.get("type").equals("delete")) {
				currentLoginsBlacklist.remove(blacklistCommand.get(1));
			}
			String newLoginsBlacklist = mapper.writeValueAsString(currentLoginsBlacklist);
			configLoader.setProperty("loginsBlacklist", newLoginsBlacklist);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean ipBlacklist(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["ip","dir"]}
//		ipBlacklist = ["ip"]
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		List<InetAddress> currentIpBlacklist = new ArrayList<InetAddress>();

		try {
			currentIpBlacklist = configLoader.getIPBlacklist();
			if(request.get("type").equals("assignation")) {
				if(!currentIpBlacklist.contains(blacklistCommand.get(1))) {
					currentIpBlacklist.add(InetAddress.getByName(blacklistCommand.get(1)));
				}
			} else if(request.get("type").equals("delete")) {
				currentIpBlacklist.remove(InetAddress.getByName(blacklistCommand.get(1)));
			}
			String newIpBlacklist = mapper.writeValueAsString(currentIpBlacklist);
			configLoader.setProperty("ipBlacklist", newIpBlacklist);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean netBlacklist(SelectionKey key, ByteBuffer buf, Map<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["net","netid", "netmask"]}
//		netBlacklist = {["origin","port"]}
		List<String> blacklistCommand = (List<String>) request.get("blacklist");
		boolean netIsNew = true;
		List<List<String>> currentNetBlacklist = new ArrayList<List<String>>();

		try {
			//TODO obtener la currentNetBlacklist del configloader cuando se modifique el loader
			String prop = configLoader.getProperty("netBlacklist");
			if(prop != null) {
				currentNetBlacklist = mapper.readValue(prop, new TypeReference<List<List<String>>>() {});
			}
			List<String> newBlackNet = new ArrayList<String>();
			newBlackNet.add(blacklistCommand.get(1));
			newBlackNet.add(blacklistCommand.get(2));
			if(request.get("type").equals("assignation")) {
				for(List<String> net : currentNetBlacklist) {
					if(net.get(0).equals(newBlackNet.get(0)) && net.get(1).equals(newBlackNet.get(1))) {
						netIsNew = false;
					}
				}
				if(netIsNew) {
					currentNetBlacklist.add(newBlackNet);
				}
			} else if(request.get("type").equals("delete")) {
//	{"auth":["admin","admin"],"type":"delete", "blacklist":["net","netid","netmask"]}
				Iterator<List<String>> it = currentNetBlacklist.iterator();
				while(it.hasNext()) {
					List<String> next = it.next();
					if(next.get(0).equals(newBlackNet.get(0)) && next.get(1).equals(newBlackNet.get(1))) {
						it.remove();
					}
				}
			}
			if(netIsNew) {
				String newNetBlacklist = mapper.writeValueAsString(currentNetBlacklist);
				configLoader.setProperty("netBlacklist", newNetBlacklist);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
