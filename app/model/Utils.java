package model;

import static play.Logger.debug;
import static play.Logger.warn;
import org.apache.commons.beanutils.BeanMap;


public class Utils {
	
	public static void describe(Object o) {
		debug("DESCRIBE "+o+" --------------- START");
		BeanMap m = new BeanMap(o);
		for(Object k: m.keySet()) {
			String key = (String) k;
			try {
				debug(key+ " = " + m.get(key)+"");
			} catch (Exception e) {
				warn(key + " retrieving error");
			}
		}
		debug("DESCRIBE "+o+" ---------------- END");
	}
}
