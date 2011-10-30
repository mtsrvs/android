package ar.edu.itba.it.pdc.proxy.protocol;

public class JID {

		private String username;
		private String server;
		private String resource;
		
		public JID(String jid){
			String[] parts = jid.split("[@/]");
			this.username = parts[0];
			this.server = parts[1];
			this.resource = parts[2];
		}
		
		public JID(String username, String server, String resource){
			this.username = username;
			this.server = server;
			this.resource = resource;
		}

		public String getUsername() {
			return username;
		}

		public String getServer() {
			return server;
		}

		public String getResource() {
			return resource;
		}
		
		public String toString(){
			return this.username + "@" + this.server + "/" + this.resource;
		}
}
