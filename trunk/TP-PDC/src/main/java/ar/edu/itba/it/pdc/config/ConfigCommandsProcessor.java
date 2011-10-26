package ar.edu.itba.it.pdc.config;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
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

	@Autowired
	public ConfigCommandsProcessor(ConfigLoader configLoader) {
		this.configLoader = configLoader;
		this.mapper = new ObjectMapper();
	}

	@SuppressWarnings("unchecked")
	public void process(SelectionKey key, ByteBuffer buf, String req) {
		HashMap<String, Object> request = new HashMap<String, Object>();

		try {
			request = mapper.readValue(req,
					new TypeReference<Map<String, Object>>() {
					});
			if (authenticate(((ArrayList<String>) request.get("auth")))) {
				if (request.get("type").equals("query")) {
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
					if (request.containsKey("server")) {
						serverCommand(key, buf, request);
					}
				}
			} else {
				sendResponse(key, buf, "ERR. Wrong authentication.\n");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			sendResponse(key, buf, "ERR\n");
		}
	}

	// public static void main(String[] args) {
	//		
	// String a =
	// "{\"auth\":[\"admin\",\"admin\"],\"type\":\"assignation\",\"command\":[\"filter\",\"l33t\",\"on\"]}";
	// String b =
	// "{\"user\":\"admin\",\"password\":\"admin\",\"type\":\"assignation\"}";
	// System.out.println(a);
	//		
	// HashMap<String, Object> map = new HashMap<String, Object>();
	//		
	// try {
	// map = mapper.readValue(a, new TypeReference<Map<String,Object>>() { });
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//		
	// System.out.println(((ArrayList<String>)map.get("auth")).get(0));
	// }

	public void sendResponse(SelectionKey key, ByteBuffer buf, String response) {
		if (response != "") {
			buf.clear();
			buf.put(response.getBytes());
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	private boolean authenticate(ArrayList<String> values) {
		String adminUsername = configLoader.getAdminUsername();
		String adminPassword = configLoader.getAdminPassword();

		if (values.get(0).equals(adminUsername)
				&& values.get(1).equals(adminPassword)) {
			return true;
		}
		return false;
	}

	private void printQuery(SelectionKey key, ByteBuffer buf, String parameter) {
		sendResponse(key, buf, configLoader.getProperty(parameter) + "\n");
	}

	private void caccessCommand(SelectionKey key, ByteBuffer buf, HashMap<String, Object> request) {
		// {"auth":["admin","admin"],"type":"assignation", "caccess":["user","qty"]}
		String response;
		HashMap<String, String> currentCaccess = new HashMap<String, String>();

		try {
			String prop = configLoader.getProperty("caccess");
			if(prop != null) {
				currentCaccess = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			List<String> accessControl = (ArrayList<String>) request.get("caccess");
			currentCaccess.put(accessControl.get(0), accessControl.get(1));
			String newCaccess = mapper.writeValueAsString(currentCaccess);
			configLoader.setProperty("caccess", newCaccess);
		} catch (Exception e) {
			e.printStackTrace();
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}

	private void multiplexCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {

//		{"auth":["admin","admin"],"type":"assignation", "multiplex":["jid","serverto"]}
		String response;
		HashMap<String, String> currentMultiplex = new HashMap<String, String>();

		try {
			String prop = configLoader.getProperty("multiplex");
			if(prop != null) {
				currentMultiplex = mapper.readValue(prop, new TypeReference<Map<String, String>>() {});
			}
			List<String> multiplex = (ArrayList<String>) request.get("multiplex");
			currentMultiplex.put(multiplex.get(0), multiplex.get(1));
			String newMultiplex = mapper.writeValueAsString(currentMultiplex);
			configLoader.setProperty("multiplex", newMultiplex);
		} catch (Exception e) {
			e.printStackTrace();
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}

	private void silenceCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "silence":"user"}
		String response;
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
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);

	}

	private void filterCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "filter":["type","on/off"]}
		List<String> filter = (ArrayList<String>) request.get("filter");
		String response;

		try {
			configLoader.setProperty(filter.get(0), filter.get(1));
		} catch (Exception e) {
			e.printStackTrace();
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}
	
	public void serverCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		String response;
//		HashMap<String, List<String>> oldServers = new HashMap<String, List<String>>();
//		// {"auth":["admin","admin"],"type":"assignation", "server":["origin","port"]}
//
//		List<String> server = (ArrayList<String>) request.get("server");
//		
//		try {
//			configLoader.setServer(server.get(0), server.get(1));
//		} catch (Exception e) {
//			e.printStackTrace();
//			response = "ERR\n";
//			sendResponse(key, buf, response);
//			return;
//		}
//		response = "OK\n";
//		sendResponse(key, buf, response);
	}

	private void blacklistCommand(SelectionKey key, ByteBuffer buf,
			HashMap<String, Object> request) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
		List<String> blacklistCommand = (ArrayList<String>) request.get("blacklist");
		if(blacklistCommand.get(0).equals("range")) {
			rangeBlacklist(key, buf, blacklistCommand);
		} else if(blacklistCommand.get(0).equals("logins")) {
			loginsBlacklist(key, buf, blacklistCommand);
		} else if(blacklistCommand.get(0).equals("ip")) {
			ipBlacklist(key, buf, blacklistCommand);
		} else if(blacklistCommand.get(0).equals("net")) {
			netBlacklist(key, buf, blacklistCommand);
		}
	}

	private void rangeBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["range","user","from","to"]}
//		rangeBlacklist = {"user":["from","to"]}
		String response;
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
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}
	
	private void loginsBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["logins","user","qty"]}
//		loginsBlacklist = {"user":"qty"}
		String response;
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
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}
	
	private void ipBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["ip","dir"]}
//		ipBlacklist = ["ip"]
		String response;
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
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
	}
	
	private void netBlacklist(SelectionKey key, ByteBuffer buf, List<String> blacklistCommand) {
//		{"auth":["admin","admin"],"type":"assignation", "blacklist":["net","netid", "netmask"]}
//		netBlacklist = {["origin","port"]}
		String response;
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
			response = "ERR\n";
			sendResponse(key, buf, response);
			return;
		}
		response = "OK\n";
		sendResponse(key, buf, response);
		
	}
}
