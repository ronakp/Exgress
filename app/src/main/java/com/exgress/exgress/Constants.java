package com.exgress.exgress;

/**
 * Created by Not-A-Mac on 11/7/2015.
 */
public class Constants {
    //factions
    public static final String RedFaction = "Supremacist";
    public static final String BlueFaction = "Purist";

    //table names
    public static final String UserTableName = "User";
    public static final String NodeTableName = "Node";
    public static final String StatusTableName = "Status";

    //table columns
    public static final String UsernameColumn = "Username";
    public static final String PasswordColumn = "Password";
    public static final String SaltColumn = "Salt";
    public static final String FactionColumn = "Faction";
    public static final String EmailColumn = "Email";

    public static final String StatusColumn = "Status";
    public static final String ValueColumn = "Value";

    public static final String LatitudeColumn = "Latitude";
    public static final String LongitudeColumn = "Longitude";
    public static final String HPColumn = "HP";
    public static final String NameColumn = "Name";

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
