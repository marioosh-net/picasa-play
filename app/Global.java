import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Result;
import play.mvc.Http.RequestHeader;
import static play.mvc.Results.*;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		Logger.info("Application has started");
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
