package net.runelite.client.plugins.saltygg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ClanMemberRank;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.game.ClanManager;
import net.runelite.client.plugins.playerindicators.PlayerIndicatorsConfig;
import net.runelite.client.plugins.playerindicators.PlayerIndicatorsService;
import net.runelite.client.plugins.playerindicators.PlayerNameLocation;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
public class SaltyOverlay extends Overlay{




        private static final int ACTOR_OVERHEAD_TEXT_MARGIN = 40;
        private static final int ACTOR_HORIZONTAL_TEXT_MARGIN = 10;

        private final PlayerIndicatorsService playerIndicatorsService;
        private final PlayerIndicatorsConfig config;


        @Inject
        private SaltyOverlay(PlayerIndicatorsConfig config, PlayerIndicatorsService playerIndicatorsService,
                                        ClanManager clanManager)
        {
            this.config = config;
            this.playerIndicatorsService = playerIndicatorsService;

            setPosition(OverlayPosition.DYNAMIC);
            setPriority(OverlayPriority.MED);
        }


    @Override
        public Dimension render(Graphics2D graphics)
        {
            playerIndicatorsService.forEachPlayer((player, color) -> renderPlayerOverlay(graphics, player, color));
            return null;
        }

        private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color)
        {
            final PlayerNameLocation drawPlayerNamesConfig = config.playerNamePosition();
            if (drawPlayerNamesConfig == PlayerNameLocation.DISABLED)
            {
                return;
            }

            final int zOffset;
            switch (drawPlayerNamesConfig)
            {
                case MODEL_CENTER:
                case MODEL_RIGHT:
                    zOffset = actor.getLogicalHeight() / 2;
                    break;
                default:
                    zOffset = actor.getLogicalHeight() + ACTOR_OVERHEAD_TEXT_MARGIN;
            }

            final String name = Text.sanitize(actor.getName());
            Point textLocation = actor.getCanvasTextLocation(graphics, name, zOffset);

            if (drawPlayerNamesConfig == PlayerNameLocation.MODEL_RIGHT)
            {
                textLocation = actor.getCanvasTextLocation(graphics, "", zOffset);

                if (textLocation == null)
                {
                    return;
                }

                textLocation = new Point(textLocation.getX() + ACTOR_HORIZONTAL_TEXT_MARGIN, textLocation.getY());
            }

            if (textLocation == null)
            {
                return;
            }


            OverlayUtil.renderTextLocation(graphics, textLocation, name, color);
        }

}
