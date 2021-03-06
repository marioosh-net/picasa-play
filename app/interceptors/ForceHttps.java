package interceptors;

import play.Play;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public class ForceHttps extends Action.Simple {
	
	private static String SSL_HEADER_CLOUD_FOUNDRY = "x-forwarded-proto";

	@Override
	public Result call(Context ctx) throws Throwable {
		if (!isHttpsRequest(ctx.request())) {
			return redirect("https://" + ctx.request().host()
					+ ctx.request().uri());
		}
		return delegate.call(ctx);
	}

	private boolean isHttpsRequest(Request request) {

		if (Play.isDev()) {
			return true;
		}

		if (request.getHeader(SSL_HEADER_CLOUD_FOUNDRY) != null
				&& request.getHeader(SSL_HEADER_CLOUD_FOUNDRY).contains("https")) {
				// && StringUtils.contains(request.getHeader(SSL_HEADER_CLOUD_FOUNDRY), "https")) {
			return true;
		}

		return false;
	}	
}