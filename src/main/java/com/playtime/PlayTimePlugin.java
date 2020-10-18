package com.playtime;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.reportbutton.TimeStyle;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Slf4j
@PluginDescriptor(
	name = "Play Time"
)
public class PlayTimePlugin extends Plugin
{
	private static final ZoneId UTC = ZoneId.of("UTC");
	private static final ZoneId JAGEX = ZoneId.of("Europe/London");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM. dd, yyyy");

	private boolean ready = false;
	private int ticksSinceLogin = 0;
	private Instant loginTime;

	private PlayTimePanel panel;
	private NavigationButton navButton;

	@Inject
	private TimeRecordWriter writer;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PlayTimeConfig config;

	private ArrayList<PlayTimeRecord> records;
	private PlayTimeRecord record;

	@Override
	protected void startUp() throws Exception
	{
		panel = new PlayTimePanel(this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "pluginicon.png");

		navButton = NavigationButton.builder()
				.tooltip("Play Time")
				.priority(6)
				.icon(icon)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		if (client.getGameState().equals(GameState.LOGGED_IN) || client.getGameState().equals(GameState.LOADING))
		{
			writer.setPlayerUsername(client.getUsername());
			record = writer.loadPlayTimeRecord();
			if (record == null) {
				record = new PlayTimeRecord("", 0);
			}
		}
		//records = writer.loadPlayTimeRecords();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		writer.writePlayTimeFile(record);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		ticksSinceLogin++;

		record.setTime(ticksSinceLogin);

		if (ticksSinceLogin % 20 == 0) {
			writer.writePlayTimeFile(record);
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		long time = (long)(record.getTime() * 0.6) + Long.parseLong(config.startHours()) * 3600 + Long.parseLong(config.startMinutes()) * 60;

		long days = (long)Math.floor((double)time / (3600 * 24));
		time -= days * (3600 * 24);
		long hours = (long)Math.floor((double)time / 3600);
		time -= hours * 3600;
		long min = (long)Math.floor((double)time / 60);
		time -= min * 60;
		panel.updateTime(String.format("%d:%d:%d:%d", days, hours, min, time));
	}

	public void resetCounter() {
		ticksSinceLogin = 0;
		if (record != null) {
			record.setTime(0);
			writer.writePlayTimeFile(record);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();

		switch (state)
		{
			case LOGGING_IN:
			case HOPPING:
			case CONNECTION_LOST:
				ready = true;
				break;
			case LOGGED_IN:
				if (ready)
				{
					loginTime = Instant.now();
					writer.setPlayerUsername(client.getUsername());
					record = writer.loadPlayTimeRecord();
					if (record == null) {
						record = new PlayTimeRecord("", 0);
					}
					ticksSinceLogin = record.getTime();
					ready = false;
				}
				break;
		}
	}

	@Provides
	PlayTimeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayTimeConfig.class);
	}
}
