/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, Alexsuperfly <alexsuperfly@users.noreply.github.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.saltygg;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PluginDescriptor(
	name = "Salty GG Tracker",
	description = "Automatically updates your stats, drops and other achievements",
	tags = {"saltygg", "external", "integration"},
	enabledByDefault = true
)
@Slf4j
public class SaltyPlugin extends Plugin
{
	/**
	 * Amount of EXP that must be gained for an update to be submitted.
	 */
	private static final int XP_THRESHOLD = 0;
	public static ArrayList javaArrayListFromGSON;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SaltyOverlay saltyOverlay;

	@Inject
	private Client client;

	private String lastUsername;
	private boolean fetchXp;

	private long lastXp;


	@Inject
	private net.runelite.client.plugins.saltygg.SaltyConfig config;


	@Provides
	SaltyConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SaltyConfig.class);
	}


	@Override
	protected void startUp() throws Exception
	{
		fetchXp = true;
	}



	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState state = gameStateChanged.getGameState();

		if (state == GameState.LOGGED_IN)
		{
			Player local = client.getLocalPlayer();

			if (local == null)
			{
				return;
			}

			log.debug("Requesting list of Salty Members with {}", local.getName());

			updateActiveUserList();


			if (!Objects.equals(client.getUsername(), lastUsername))
			{
				lastUsername = client.getUsername();
				fetchXp = true;
			}
		}
		else if (state == GameState.LOGIN_SCREEN)
		{
			Player local = client.getLocalPlayer();
			if (local == null)
			{
				return;
			}


			long totalXp = client.getOverallExperience();
			// Don't submit update unless xp threshold is reached
			if (Math.abs(totalXp - lastXp) > XP_THRESHOLD)
			{
				log.debug("Submitting update for {}", local.getName());
				sendUpdateRequest(local.getName());
				lastXp = totalXp;
			}
		}
	}



	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (fetchXp)
		{
			lastXp = client.getOverallExperience();
			fetchXp = false;
		}


	}

	public void updateActiveUserList() {

		OkHttpClient httpClient = RuneLiteAPI.CLIENT;

		if (config.highlighter())
		{
			HttpUrl url = new HttpUrl.Builder()
					.scheme("https")
					.host("api.salty.gg")
					.addPathSegment("private")
					.addPathSegment("client")
					.addPathSegment("rsncache")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.build();

			httpClient.newCall(request).enqueue(new Callback()
			{


				private Array ResponseBody;

				@Override
				public void onFailure(Call call, IOException e)
				{
					log.warn("Error submitting salty.gg update, caused by {}.", e.getMessage());
				}


				@SneakyThrows
				@Override
				public void onResponse(Call call, Response response) throws IOException {

					// Convert JSON Array String into Java Array List
					Gson googleJson = new Gson();
					String arrayFromString = response.body().string();
					ArrayList javaArrayListFromGSON = googleJson.fromJson(arrayFromString, ArrayList.class);

					isSaltyArray(javaArrayListFromGSON);


				}



			});



		}

	}
//// THIS SPEWS OUT STUFF BUT I DONT UNDERSTAND WHAT NOW. Following PlayerIndicator plugin , i am still more confused as before. After working with this a while, i didnt get it to check the array all the time.
	private void isSaltyArray(ArrayList javaArrayListFromGSON) {

		Player local = client.getLocalPlayer();

		boolean saltyMember = SaltyPlugin.javaArrayListFromGSON.contains(local.getName());

		if (saltyMember)
			System.out.println("The list contains user" + saltyMember );
		else
			System.out.println("Fak off");


	}


	private void sendUpdateRequest(String username)
	{
		String reformedUsername = username.replace(" ", "_");
		OkHttpClient httpClient = RuneLiteAPI.CLIENT;

		if (config.track())
		{
			HttpUrl url = new HttpUrl.Builder()
					.scheme("https")
					.host("api.salty.gg")
					.addPathSegment("public")
					.addPathSegment("client")
					.addPathSegment("refresh")
					.addQueryParameter("rsn", reformedUsername)
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.build();

			httpClient.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					log.warn("Error submitting salty.gg update, caused by {}.", e.getMessage());
				}

				@Override
				public void onResponse(Call call, Response response)
				{
					response.close();
				}
			});
		}



	}


}
