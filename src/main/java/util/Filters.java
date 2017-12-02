package util;

import spark.*;

public class Filters {
	
	public static Filter handleLocaleChange = (Request request, Response response) -> {
        if (getQueryLocale(request) != null) {
            request.session().attribute("locale", getQueryLocale(request));
            System.out.println(getQueryLocale(request));
            response.redirect(request.pathInfo());
        }
    };
    
    private static String getQueryLocale(Request request) {
        return request.queryParams("locale");
    }

}
