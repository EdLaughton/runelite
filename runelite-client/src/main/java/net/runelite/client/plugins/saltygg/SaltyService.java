/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.plugins.playerindicators.PlayerIndicatorsConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.BiConsumer;

@Singleton
public class SaltyService
{

    private final Client client;
    private final PlayerIndicatorsConfig piConfig;


    @Inject
    private SaltyService( Client client, PlayerIndicatorsConfig config)
    {
        this.piConfig = config;
        this.client = client;

    }



    public void forEachPlayer(final BiConsumer<Player, Color> consumer)
    {
        if (!piConfig.highlightOwnPlayer() && !piConfig.drawClanMemberNames()
                && !piConfig.highlightFriends() && !piConfig.highlightNonClanMembers())
        {
            return;
        }

        final Player localPlayer = client.getLocalPlayer();

        for (Player player : client.getPlayers())
        {

//Remnants of trying to get the array check into service
                boolean saltyMember = SaltyPlugin.javaArrayListFromGSON.contains(player.getName());

            System.out.println(SaltyPlugin.javaArrayListFromGSON);

                if (SaltyPlugin.javaArrayListFromGSON.contains(localPlayer))
                    System.out.println("Contains LocalPlayer");
                else
                    System.out.println("Doesn't Contain");

                if (saltyMember)
                    System.out.println("The list contains user" + saltyMember );
                else
                    System.out.println("Fak off");


            if (player == null || player.getName() == null)
            {
                continue;
            }

            boolean isClanMember = player.isClanMember();

            if (player == localPlayer)
            {
                if (piConfig.highlightOwnPlayer())
                {
                    consumer.accept(player, piConfig.getOwnPlayerColor());
                }
            }
            else if (piConfig.highlightFriends() && player.isFriend())
            {
                consumer.accept(player, piConfig.getFriendColor());
            }
            else if (piConfig.drawClanMemberNames() && isClanMember)
            {
                consumer.accept(player, piConfig.getClanMemberColor());
            }
            else if (piConfig.highlightTeamMembers() && localPlayer.getTeam() > 0 && localPlayer.getTeam() == player.getTeam())
            {
                consumer.accept(player, piConfig.getTeamMemberColor());
            }
            else if (piConfig.highlightNonClanMembers() && !isClanMember)
            {
                consumer.accept(player, piConfig.getNonClanMemberColor());
            }
        }
    }
}
