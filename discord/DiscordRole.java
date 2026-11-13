package ains.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public enum DiscordRole {

    ADMIN,
    STAFF,
    PLAYER;

    public static DiscordRole of(Member member) {
        if (member == null) return PLAYER;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return ADMIN;
        if (member.hasPermission(Permission.MESSAGE_MANAGE)) return STAFF;
        return PLAYER;
    }

    public boolean isStaffOrAbove() {
        return this != PLAYER;
    }
}
