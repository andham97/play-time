package com.playtime;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface PlayTimeConfig extends Config
{
	@ConfigItem(
			keyName = "startHours",
			name = "Current in-game hours",
			description = "Hours from Hans to offset the counter"
	)
	default String startHours()
	{
		return "0";
	}

	@ConfigItem(
			keyName = "startMinutes",
			name = "Current in-game minutes",
			description = "Minutes from Hans to offset the counter"
	)
	default String startMinutes()
	{
		return "0";
	}
}
