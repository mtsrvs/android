package ar.edu.itba.it.pdc.proxy.protocol;

public class JID {

		private String username;
		private String server;
		private String resource;
		
		public JID(String jid){
			String[] parts = jid.split("[@/]");
			if (parts.length == 1)
				this.server = parts[0];
			else if (parts.length == 2){
				if (jid.indexOf("@") != -1){
					this.username = parts[0];
					this.server = parts[1];
				} else {
					this.server = parts[0];
					this.resource = parts[1];					
				}
			} else if (parts.length == 3){
				this.username = parts[0];
				this.server = parts[1];
				this.resource = parts[2];
			}
		}
		
		public JID(String username, String server, String resource){
			this.username = username;
			this.server = server;
			this.resource = resource;
		}
		
		public JID(String username, String server){
			this(username, server, null);
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
		
		public void setResource(String resource){
			this.resource = resource;
		}
		
		public String toString(){
			String str = this.server;
			if (this.username != null)
				str = this.username + "@" + str;
			if (this.resource != null)
				str = str + "/" + this.resource;
			return str;
		}
}
