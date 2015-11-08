package com.exgress.exgress;

/**
 * Created by Not-A-Mac on 11/7/2015.
 */
public class Constants {
    //factions
    public static final String RedFaction = "Supremacist";
    public static final String BlueFaction = "Purist";

    //table names
    public static final String UserTableName = "user";
    public static final String NodeTableName = "node";
    public static final String StatusTableName = "status";

    //table columns
    public static final String UsernameColumn = "username";
    public static final String PasswordColumn = "password";
    public static final String SaltColumn = "salt";
    public static final String FactionColumn = "faction";
    public static final String EmailColumn = "email";

    public static final String StatusColumn = "status";
    public static final String ValueColumn = "value";

    public static final String LatitudeColumn = "latitude";
    public static final String LongitudeColumn = "longitude";
    public static final String HPColumn = "HP";
    public static final String NameColumn = "name";

    //status keys
    public static final String RedCount = "red_faction_count";
    public static final String BlueCount = "blue_faction_count";

    //response values
    public static final String SuccessResponse = "success";
    public static final String FailureResponse = "failure";
    public static final String UsernameTakenResponse = "The username is already taken";
    public static final String UserNotFoundResponse = "The user is not found";
    public static final String InvalidPasswordResponse = "The password is invalid";
    public static final String NodeTransformedResponse = "The node has transformed to your faction";

    //submission finalants
    public static final String DrainSubmission = "drain";
    public static final String ReinforceSubmission = "submission";
}
