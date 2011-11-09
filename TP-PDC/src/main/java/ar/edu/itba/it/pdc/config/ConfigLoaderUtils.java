package ar.edu.itba.it.pdc.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.net.util.SubnetUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.pdc.exception.ConfigurationFileException;

@Component
public class ConfigLoaderUtils {
	
	private ObjectMapper mapper;
	
	public ConfigLoaderUtils() {
		this.mapper = new ObjectMapper();
	}

	public List<String> getStringList(String property){
		try {
			if(property != null) {
				return mapper.readValue(property, new TypeReference<List<String>>() {});
			} else {
				return new ArrayList<String>();
			}
		} catch (Exception e) {
			throw new ConfigurationFileException();
		}
	}
	
	public List<InetAddress> getInetAddressList(String property){
		try {
			if(property != null) {
				return mapper.readValue(property, new TypeReference<List<InetAddress>>() {});
			} else {
				return new ArrayList<InetAddress>();
			}
		} catch (Exception e) {
			throw new ConfigurationFileException();
		}
	}
	
	public Map<String,TimeRange> getTimeRangesMap(String property){
		Map<String,TimeRange> timeRanges = new HashMap<String, TimeRange>();
		if(property != null) {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			try {
				map =  mapper.readValue(property, new TypeReference<Map<String, List<String>>>() {});
				for(Entry<String, List<String>> entry : map.entrySet()) {
					LocalTime from = LocalTime.parse(entry.getValue().get(0));
					LocalTime to = LocalTime.parse(entry.getValue().get(1));
					TimeRange range = new TimeRange(from.getHourOfDay(), from.getMinuteOfHour(), from.getSecondOfMinute(),
							to.getHourOfDay(), to.getMinuteOfHour(), to.getSecondOfMinute());
					timeRanges.put(entry.getKey(), range);
				}
			} catch (Exception e) {
				throw new ConfigurationFileException();
			}
		}
		return timeRanges;
	}

	public Map<String, String> getStringStringMap(String property) {
//	{"user"\:"on","userb"\:"off"}
		Map<String, String> map = new HashMap<String, String>();
		try {
			if(property != null) {
				map =  mapper.readValue(property, new TypeReference<Map<String, String>>() {});
			}
		} catch (Exception e) {
			throw new ConfigurationFileException();
		}
		return map;
	}
	
	public Map<String, InetSocketAddress> getStringInetMap(String property) {
//		{"jid"\:"server","foo"\:"localhost"}
		Map<String, InetSocketAddress> multiplex = new HashMap<String, InetSocketAddress>();
		if(property != null) {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			try {
				map =  mapper.readValue(property, new TypeReference<Map<String, List<String>>>() {});
				for(Entry<String, List<String>> entry : map.entrySet()) {
					int port = Integer.valueOf(entry.getValue().get(1));
					InetSocketAddress address = new InetSocketAddress(entry.getValue().get(0), port);
					multiplex.put(entry.getKey(), address);
				}
			} catch (Exception e) {
				throw new ConfigurationFileException();
			}
		}
		return multiplex;
	}
	
	public Map<String, Integer> getStringIntegerMap(String property) {
//		{"jid"\:"server","foo"\:"localhost"}
		Map<String, Integer> multiplex = new HashMap<String, Integer>();
		if(property != null) {
			Map<String, String> map = new HashMap<String, String>();
			try {
				map =  mapper.readValue(property, new TypeReference<Map<String, String>>() {});
				for(Entry<String, String> entry : map.entrySet()) {
					multiplex.put(entry.getKey(), Integer.valueOf(entry.getValue()));
				}
			} catch (Exception e) {
				throw new ConfigurationFileException();
			}
		}
		return multiplex;
	}
	
	public List<SubnetUtils> getNetList(String property) {
		List<SubnetUtils> nets = new ArrayList<SubnetUtils>();
		List<String> currentNets = new ArrayList<String>();
		if(property != null) {
			try {
				currentNets = mapper.readValue(property, new TypeReference<List<String>>() {});
				for(String net : currentNets) {
					nets.add(new SubnetUtils(net));
				}
			} catch (Exception e) {
				throw new ConfigurationFileException();
			}
		}
		return nets;
	}
	
}
