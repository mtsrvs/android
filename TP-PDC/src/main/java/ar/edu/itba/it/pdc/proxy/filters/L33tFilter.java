package ar.edu.itba.it.pdc.proxy.filters;

public class L33tFilter {
	public static String transform(String str){
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i < str.length(); i++){
			char c = str.charAt(i);
			switch(c){
			case 'A': case 'a':
				sb.append('4');
				break;
			case 'E': case 'e':
				sb.append('3');
				break;
			case 'I': case 'i':
				sb.append('1');
				break;
			case 'O': case 'o':
				sb.append('0');
				break;
			default:
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
}
