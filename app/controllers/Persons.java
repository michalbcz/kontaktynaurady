package controllers;

import play.mvc.With;

/**
 * Created by michal on 2015-01-28.
 */
@With(Secure.class)
public class Persons extends CRUD {
}
