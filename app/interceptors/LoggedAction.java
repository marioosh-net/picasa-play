package interceptors;

import others.Role;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class LoggedAction extends Action<Logged> {

	@Override
	public Result call(Context paramContext) throws Throwable {
		Role role = configuration.value();
		if(paramContext.session().get("user") != null) {
			if(paramContext.session().get("role") != null && 
				paramContext.session().get("role").equals(role.name())) {
				return delegate.call(paramContext);
			} else {
				return forbidden("You need to be logged with role " + role.name() + " here!");
			}
		} else {
			return forbidden("You need to be logged here!");
		}
	}

}
