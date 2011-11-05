package ar.edu.itba.it.pdc.proxy.parser.element;

import java.util.Map;

public class StartElement extends XMPPElement {

	private String prefix;
	private String name;
	private Map<String, String> namespaces;
	private Map<String, String> attributes;
	
	public StartElement(SimpleElement parent, String name, String prefix, Map<String, String> attributes, Map<String, String> namespaces) {
		super(parent);
		this.name = name;
		this.prefix = prefix;
		this.namespaces = namespaces;
		this.attributes = attributes;
	}

	public String getName() {
		if(this.prefix != null && !this.prefix.isEmpty()) {
			return this.prefix + ":" + this.name;
		} else {
			return this.name;
		}
	}

	@Override
	protected void appendDataToWrite(StringBuilder builder) {
		if(this.prefix != null && !this.prefix.isEmpty()) {
			builder.append("<" + this.prefix + ":" + this.name);
		}else{
			builder.append("<" + this.name);
		}
		for(String key : namespaces.keySet()) {
			builder.append(" " + key + "=\"" + namespaces.get(key) + "\"");
		}
		for(String key : attributes.keySet()) {
			builder.append(" " + key + "=\"" + attributes.get(key) + "\"");
		}
		builder.append(">");
	}
	
	public Map<String,String> getAttributes(){
		return this.attributes;
	}
	
	public Map<String,String> getNamespaces(){
		return this.namespaces;
	}
	
	@Override
	public boolean isStartElement(){
		return true;
	}
	
}
