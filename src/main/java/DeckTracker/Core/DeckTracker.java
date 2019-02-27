package DeckTracker.Core;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.StartGameSubscriber;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDeathSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.megacrit.cardcrawl.cards.AbstractCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@SpireInitializer
public class DeckTracker implements StartGameSubscriber, OnStartBattleSubscriber,
        PostBattleSubscriber, PostDeathSubscriber, PostInitializeSubscriber{
    public static final Logger logger = LogManager.getLogger(DeckTracker.class.getName());
    static ArrayList<DeckTrackerCard> cardList;
    static ArrayList<DeckTrackerCard> discardList;

    private static SpireConfig config;
    public static boolean extendedTooltips;


    public DeckTracker() {
        cardList = new ArrayList<DeckTrackerCard>();
        discardList = new ArrayList<DeckTrackerCard>();
        BaseMod.subscribe(this);

    }

    public static void initialize() {
        DeckTracker tracker = new DeckTracker();
        config = makeConfig();
        setProperties();
    }


    @SpirePatch(
            clz = AbstractPlayer.class,
            method = "onCardDrawOrDiscard"
    )
    public static class PostCardResolve {
        public static void Postfix(AbstractPlayer __instance) {
            Update();
        }
    }

    @Override
    public void receiveStartGame() {
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        Update();
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        ResetList(true);
        ResetList(false);
    }

    @Override
    public void receivePostDeath() {
        ResetList(true);
        ResetList(false);
    }

    private static void ResetList(boolean discardDeck) {
        if (discardDeck) {
            for (DeckTrackerCard dt : discardList) {
                dt.Remove();
            }
            discardList.clear();
        }
        else {
            for (DeckTrackerCard dt : cardList) {
                dt.Remove();
            }
            cardList.clear();
        }
    }

    public static void Update() {
        try {AbstractDungeon.getCurrRoom();}
        catch (Exception e) { logger.error("You played a card while not in combat?"); return;}
        makeDeckList(false);
        makeDeckList(true);
    }

    private static void makeDeckList(boolean discardDeck) {
        TreeMap<AbstractCard, Integer> ret = new TreeMap<>();
        ResetList(discardDeck);
        TextureAtlas.AtlasRegion energyOrbAR = AbstractDungeon.player.getOrb();
        int y = 0;
        TextureRegion TROrb = null;
        y=0;
        if (discardDeck) {
            for (AbstractCard card : AbstractDungeon.player.discardPile.group)  {
                ret.put(card, ret.getOrDefault(card, 0) + 1);
            }
        }
        else {
            for (AbstractCard card : AbstractDungeon.player.drawPile.group)  {
                ret.put(card, ret.getOrDefault(card, 0) + 1);
            }
        }
        int cardTypes = -1;
        for (Map.Entry<AbstractCard, Integer> entry :ret.entrySet()) {
            cardTypes++;
            if (cardTypes>20) break; // temporary fix so it doesnt go over energy orb and deck
            int amount = entry.getValue();
            --y;
            AbstractCard card = entry.getKey();
            // Orbs
            switch (card.color) {
                case CURSE:
                case COLORLESS:
                    TROrb = new TextureRegion(ImageMaster.CARD_GRAY_ORB_L, 0, 0, ImageMaster.CARD_GRAY_ORB_L.getWidth(), ImageMaster.CARD_GRAY_ORB_L.getHeight());
                    break;
                default:
                    TROrb = new TextureRegion(energyOrbAR, 0, 0, energyOrbAR.packedWidth, energyOrbAR.packedHeight);
                    break;
            }
            DeckTrackerCard dtCard = new DeckTrackerCard(card, TROrb, y, amount, discardDeck);
            if (discardDeck) discardList.add(dtCard);
            else cardList.add(dtCard);
        }
    }

    public enum TooltipTypes {BothDefaultSimple, BothDefaultExtended, Simple, Extended }

    // Config file initialization and screen
    private static SpireConfig makeConfig() {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("extended-tooltip",  Boolean.toString(false));

        try {
            SpireConfig retConfig = new SpireConfig("StSDeckTracker", "StSDeckTracker-config", defaultProperties);
            return retConfig;
        } catch (IOException var2) {
            return null;
        }
    }

    private static Boolean getBoolean(String key) {
        return config.getBool(key);
    }
    static void setBoolean(String key, Boolean value) {
        config.setBool(key, value);

        try {
            config.save();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    private static void setProperties() {
        if (config != null) {
            Boolean tooltip = getBoolean("extended-tooltip");
            if (tooltip != null) {
                extendedTooltips = tooltip;
            }
        }
    }

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();

        ModLabeledToggleButton extendedTooltip = new ModLabeledToggleButton("Display full cards in tooltips", 350.0F, 700.0F, Settings.CREAM_COLOR, FontHelper.charDescFont, extendedTooltips, settingsPanel, (label) -> {
        }, (button) -> {
            extendedTooltips = button.enabled;
            setBoolean("extended-tooltip", button.enabled);
        });
        settingsPanel.addUIElement(extendedTooltip);

        Texture badgeTexture = ImageMaster.loadImage("img/Decktracker-ModBadge.png");
        BaseMod.registerModBadge(badgeTexture, "StSDeckTracker", "Girogore", "Provides an in-game deck tracker", settingsPanel);

    }

}
