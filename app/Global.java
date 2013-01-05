import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import com.google.gdata.client.photos.PicasawebService;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Result;
import play.mvc.Http.RequestHeader;
import static play.Logger.error;
import static play.Logger.info;
import static play.mvc.Results.*;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		
		try {
			info("Loading services...");
			Properties p = new Properties();
			InputStream in =  Application.class.getResourceAsStream("/resources/accounts.properties");
			if(in != null) {
				p.load(in);
				in.close();
			
				Enumeration e = p.propertyNames();
				List<String[]> l = new ArrayList<String[]>();
				while(e.hasMoreElements()) {
					String k = (String) e.nextElement();
					l.add(new String[]{k+"", p.getProperty(k)});
					PicasawebService myService = new PicasawebService("testApp");			
					myService.setUserCredentials(k+"", p.getProperty(k));
					controllers.Application.myServices.add(myService);
				}
			
			} else {
				error("null inputstream");
			}
		
			Logger.info("Application has started");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}

	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

	@Override
	public Result onError(RequestHeader arg0, Throwable arg1) {
		return notFound("Page Not Found");
	}
	
	@Override
	public Result onHandlerNotFound(RequestHeader paramRequestHeader) {
		return notFound("Page Not Found");
	}
	
	@Override
	public Result onBadRequest(RequestHeader paramRequestHeader, String paramString) {
		return notFound("Page Not Found");
	}
}
