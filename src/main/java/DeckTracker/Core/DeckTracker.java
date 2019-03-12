package DeckTracker.Core;

import basemod.*;
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
    public static int drawHeight, discardHeight;
    public static int drawWidth, discardWidth;
    public static float drawTextSize, discardTextSize;


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
            if (cardTypes>470/drawHeight && !discardDeck) break;
            if (cardTypes>470/discardHeight && discardDeck) break;
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

    // Config file initialization and screen
    private static SpireConfig makeConfig() {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("extended-tooltip",  Boolean.toString(false));

        defaultProperties.setProperty("draw-width",  Integer.toString(200));
        defaultProperties.setProperty("draw-height", Integer.toString(25));
        defaultProperties.setProperty("draw-text-size", Float.toString(0.7F));
        defaultProperties.setProperty("discard-width",  Integer.toString(130));
        defaultProperties.setProperty("discard-height", Integer.toString(25));
        defaultProperties.setProperty("discard-text-size", Float.toString(0.6F));

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

    private static int getInt(String key) {
        return config.getInt(key);
    }
    static void setInt(String key, int value) {
        config.setInt(key, value);

        try {
            config.save();
        } catch (IOException var3) {
            var3.printStackTrace();
        }
    }

    private static Float getFloat(String key) {
        return config.getFloat(key);
    }
    static void setFloat(String key, float value) {
        config.setFloat(key, value);

        try {
            config.save();
        } catch (IOException var3) {
            var3.printStackTrace();
        }
    }

    private static void setProperties() {
        if (config != null) {
            Boolean entryB;
            int entryI;
            String entryS;
            float entryF;
            try {
                entryB = getBoolean("extended-tooltip");
                extendedTooltips = entryB;
            } catch (Exception e) {
                extendedTooltips = false;
            }
            try {
                entryI = getInt("draw-width");
                drawWidth = entryI;
            } catch (Exception e) {
                drawWidth = 200;
            }
            try {
                entryI = getInt("draw-height");
                drawHeight = entryI;
            } catch (Exception e) {
                drawHeight = 25;
            }
            try {
                entryF = getFloat("draw-text-size");
                drawTextSize = entryF;
            } catch (Exception e) {
                drawTextSize = 0.7F;
            }
            try {
                entryI = getInt("discard-width");
                discardWidth = entryI;
            } catch (Exception e) {
                discardWidth = 130;
            }
            try {
                entryI = getInt("discard-height");
                discardHeight = entryI;
            } catch (Exception e) {
                discardHeight = 25;
            }
            try {
                entryF = getFloat("discard-text-size");
                discardTextSize = entryF;
            } catch (Exception e) {
                discardTextSize = 0.7F;
            }
        }
    }

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();
        ModLabeledToggleButton configButton;
        ModSlider configSlider;
        ModLabel configLabel;
        float y = 750.0F;
        float x = 375.0F;
        configButton = new ModLabeledToggleButton("Display full cards in tooltips.", x, y, Settings.CREAM_COLOR, FontHelper.charDescFont, extendedTooltips, settingsPanel, (label) -> {
        }, (button) -> {
            extendedTooltips = button.enabled;
            setBoolean("extended-tooltip", button.enabled);
        });
        settingsPanel.addUIElement(configButton);

        // DRAW DECK
        y -= 100;
        configLabel = new ModLabel("Draw Deck", x, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        y -= 40;
        configLabel = new ModLabel("Width", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 250.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            drawWidth = val;
            setInt("draw-width", val);
        } );
        configSlider.setValue(getInt("draw-width")/configSlider.multiplier);
        settingsPanel.addUIElement(configSlider);
        y -= 40;
        configLabel = new ModLabel("Height", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 50.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            drawHeight = val;
            setInt("draw-height", val);
        } );
        configSlider.setValue(getInt("draw-height")/configSlider.multiplier);
        settingsPanel.addUIElement(configSlider);
        y -= 40;
        configLabel = new ModLabel("Text Size", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 10.0F, "", settingsPanel, (slider) -> {
            final float val = Math.max(0.01F,slider.value);
            drawTextSize = val;
            setFloat("draw-text-size", val);
        } );
        configSlider.setValue(getFloat("draw-text-size"));
        settingsPanel.addUIElement(configSlider);

        //DISCARD DECK
        y -= 60;
        configLabel = new ModLabel("Discard Deck", x, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        y -= 40;
        configLabel = new ModLabel("Width", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 250.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            discardWidth = val;
            setInt("discard-width", val);
        } );
        configSlider.setValue(getInt("discard-width")/configSlider.multiplier);
        settingsPanel.addUIElement(configSlider);
        y -= 40;
        configLabel = new ModLabel("Height", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 50.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            discardHeight = val;
            setInt("discard-height", val);
        } );
        configSlider.setValue(getInt("discard-height")/configSlider.multiplier);
        settingsPanel.addUIElement(configSlider);
        y -= 40;
        configLabel = new ModLabel("Text Size", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        configSlider = new ModSlider("", (x*1.5F)+100, y, 10.0F, "", settingsPanel, (slider) -> {
            final float val = Math.max(0.01F,slider.value);
            discardTextSize = val;
            setFloat("discard-text-size", val);
        } );
        configSlider.setValue(getFloat("discard-text-size"));
        settingsPanel.addUIElement(configSlider);


        Texture badgeTexture = ImageMaster.loadImage("img/Decktracker-ModBadge.png");
        BaseMod.registerModBadge(badgeTexture, "StSDeckTracker", "Girogore", "Provides an in-game deck tracker", settingsPanel);
    }
}
