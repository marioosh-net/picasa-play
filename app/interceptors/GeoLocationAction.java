package interceptors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * IP location by ipinfodb.com API
 * 
 * @author marioosh
 *
 */
public class GeoLocationAction extends Action<Geo> {

    private static final HttpClient HTTP_CLIENT = new DefaultHttpClient();
	
	@Override
	public Result call(Context paramContext) throws Throwable {
		String ip = paramContext.request().remoteAddress();
		ip = getIp(ip);
		try {
			Logger.info("GEO: "+geo(ip));
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return delegate.call(paramContext);
	}
	
	private String geo(String ip) throws Exception {
        String format = ""; // "json";
		String mode = "ip-city";
        String apiKey = "647f19cd2dcc198ca5eb21fb5c55fd8ee2a0a244200c712081ac0bb4183d9f22";
        String url = "http://api.ipinfodb.com/v3/" + mode + "/?format="+format+"&key=" + apiKey + "&ip=" + ip;
        
        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = HTTP_CLIENT.execute(request, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new Exception("IpInfoDb response is " + response.getStatusLine());
            }
            return EntityUtils.toString(response.getEntity());
        } finally {
            // HTTP_CLIENT.getConnectionManager().shutdown();
        }
	}
	
	public static String getIp(String s) {
		Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		Matcher m = p.matcher(s);
		while(m.find()) {
			return m.group();
		}
		return null;
	}	
}
