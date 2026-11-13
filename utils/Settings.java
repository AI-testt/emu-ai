package ains.utils;

import ains.world.users.Users;
import ains.network.sfs.User;

public class Settings {
	
    public static final String ANIMATION = "bWAnim";
    public static final String CLOAK = "bCloak";
    public static final String DUEL = "bDuel";
    public static final String FRIEND = "bFriend";
    public static final String GOTO = "bGoto";
    public static final String GUILD = "bGuild";
    public static final String HELM = "bHelm";
    public static final String MUSIC = "bMusicOn";
    public static final String PARTY = "bParty";
    public static final String PET = "bPet";
    public static final String SOUND = "bSoundOn";
    public static final String TOOLTIPS = "bTT";
    public static final String WHISPER = "bWhisper";
    public static final String TRADE = "bTrade";
    public static final String CLOAK_MESSAGE_OFF = "Cloak Visibility Off..";
    public static final String CLOAK_MESSAGE_ON = "Cloak Visibility On.";
    public static final String HELM_MESSAGE_OFF = "Helm Visibility On.";
    public static final String HELM_MESSAGE_ON = "Helm Visibility On.";
    public static final String PET_MESSAGE_OFF = "Pet Visibility On.";
    public static final String PET_MESSAGE_ON = "Pet Visibility On.";
    public static final String DUEL_MESSAGE_OFF = "Ignoring duel invites.";
    public static final String DUEL_MESSAGE_ON = "Accepting duel invites.";
    public static final String FRIEND_MESSAGE_OFF = "Ignoring Friend requests.";
    public static final String FRIEND_MESSAGE_ON = "Accepting Friend requests.";
    public static final String GOTO_MESSAGE_OFF = "Blocking goto requests.";
    public static final String GOTO_MESSAGE_ON = "Accepting goto requests.";
    public static final String GUILD_MESSAGE_OFF = "Ignoring guild invites.";
    public static final String GUILD_MESSAGE_ON = "Accepting guild invites.";
    public static final String PARTY_MESSAGE_OFF = "Ignoring party invites.";
    public static final String PARTY_MESSAGE_ON = "Accepting party invites.";
    public static final String TOOLTIPS_MESSAGE_OFF = "Ability ToolTips will not show on mouseover during combat.";
    public static final String TOOLTIPS_MESSAGE_ON = "Ability ToolTips will always show on mouseover.";
    public static final String WHISPER_MESSAGE_OFF = "Ignoring PMs.";
    public static final String WHISPER_MESSAGE_ON = "Accepting PMs.";
    public static final String TRADE_MESSAGE_OFF = "Ignoring trade invites.";
    public static final String TRADE_MESSAGE_ON = "Accepting trade invites.";
    public static final String WORLDBOSS_MESSAGE_OFF = "Ignoring world boss invites.";
    public static final String WORLDBOSS_MESSAGE_ON = "Accepting world boss invites.";

    private Settings() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    public static boolean isAllowed(String pref, User user, User client) {
        return (getPreferences(pref, client.properties.get(Users.SETTINGS))) || (user.isAdmin()) || (user.isModerator());
    }

    public static boolean getPreferences(String pref, Object setting) {
        return getPreferences(pref, Integer.parseInt(setting.toString()));
    }

    public static boolean getPreferences(String pref, int setting) {
        boolean value = false;

        switch (pref) {
		    case "bCloak":
			    value = Achievement.get(setting, 0) == 0;
				break;
		    case "bHelm":
			    value = Achievement.get(setting, 1) == 0;
				break;
		    case "bPet":
			    value = Achievement.get(setting, 2) == 0;
				break;
		    case "bWAnim":
			    value = Achievement.get(setting, 3) == 0;
				break;
		    case "bGoto":
			    value = Achievement.get(setting, 4) == 0;
				break;
		    case "bSoundOn":
			    value = Achievement.get(setting, 5) == 0;
				break;
		    case "bMusicOn":
			    value = Achievement.get(setting, 6) == 0;
				break;
		    case "bFriend":
			    value = Achievement.get(setting, 7) == 0;
				break;
		    case "bParty":
			    value = Achievement.get(setting, 8) == 0;
				break;
		    case "bGuild":
			    value = Achievement.get(setting, 9) == 0;
				break;
		    case "bWhisper":
			    value = Achievement.get(setting, 10) == 0;
				break;
		    case "bTT":
			    value = Achievement.get(setting, 11) == 0;
				break;
		    case "bFBShare":
			    value = Achievement.get(setting, 12) == 0;
				break;
		    case "bDuel":
			    value = Achievement.get(setting, 13) == 0;
				break;
		    case "bWorldBoss":
			    value = Achievement.get(setting, 19) == 0;
				break;
			case "bTrade":
				value = Achievement.get(setting, 20) == 0;
				break;
			default:
			    value = false;
			    break;
		}

        return value;
    }

    public static int setPreferences(String pref, Object test, boolean value) {
        return setPreferences(pref, Integer.parseInt(test.toString()), value);
    }

    public static int setPreferences(String pref, int intValue, boolean value) {
        int setting = value ? 0 : 1;
        int newInt = 0;
        switch (pref) {
		    case "bCloak":
			    newInt = Achievement.update(intValue, 0, setting);
				break;
		    case "bHelm":
			    newInt = Achievement.update(intValue, 1, setting);
				break;
		    case "bPet":
			    newInt = Achievement.update(intValue, 2, setting);
				break;
		    case "bWAnim":
			    newInt = Achievement.update(intValue, 3, setting);
				break;
		    case "bGoto":
			    newInt = Achievement.update(intValue, 4, setting);
				break;
		    case "bSoundOn":
			    newInt = Achievement.update(intValue, 5, setting);
				break;
		    case "bMusicOn":
			    newInt = Achievement.update(intValue, 6, setting);
				break;
		    case "bFriend":
			    newInt = Achievement.update(intValue, 7, setting);
				break;
		    case "bParty":
			    newInt = Achievement.update(intValue, 8, setting);
				break;
		    case "bGuild":
			    newInt = Achievement.update(intValue, 9, setting);
				break;
		    case "bWhisper":
			    newInt = Achievement.update(intValue, 10, setting);
				break;
		    case "bTT":
			    newInt = Achievement.update(intValue, 11, setting);
				break;
		    case "bFBShare":
			    newInt = Achievement.update(intValue, 12, setting);
				break;
		    case "bDuel":
			    newInt = Achievement.update(intValue, 13, setting);
				break;
		    case "bWorldBoss":
			    newInt = Achievement.update(intValue, 19, setting);
				break;
			case "bTrade":
				newInt = Achievement.update(intValue, 20, setting);
				break;
			default:
			    newInt = 1;
			    break;
		}

        return newInt;
    }
}
