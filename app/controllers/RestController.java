package controllers;

import play.mvc.After;
import play.mvc.Controller;


public class RestController extends Controller {

    @After
    public static void afterEachRequest() {
       addCorsSupport();
    }

    private static void addCorsSupport() {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }


}
