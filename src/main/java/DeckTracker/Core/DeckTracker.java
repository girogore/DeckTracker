package DeckTracker.Core;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

@SpireInitializer
public class DeckTracker implements PostInitializeSubscriber, OnStartBattleSubscriber,
        PostBattleSubscriber, PostDeathSubscriber {
    public static final Logger logger = LogManager.getLogger(DeckTracker.class.getName());
    static ArrayList<DeckTrackerCard> drawList;
    static ArrayList<DeckTrackerCard> discardList;

    public static float RELICLINE;
    public static boolean extendedTooltips, dynamicUpdate, dynamicText;
    public static int drawHeight, discardHeight;
    public static int defaultDrawHeight, defaultDiscardHeight;
    public static int drawWidth, discardWidth;
    public static float drawTextSize, discardTextSize;
    public static float defaultDiscardText, defaultDrawText;
    public static float xloc, xlocDiscard;
    public static float yOffset, yOffsetDiscard;
    public static final float screenArea = 650.0F;

    public static float previousxloc, previousyOffset, previousxlocDiscard, previousyOffsetDiscard;

    public DeckTracker() {
        drawList = new ArrayList<DeckTrackerCard>();
        discardList = new ArrayList<DeckTrackerCard>();
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        DeckTracker tracker = new DeckTracker();
        DeckTrackerConfig trackerconfig = new DeckTrackerConfig();
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
    public void receivePostInitialize() {
        RELICLINE = Settings.HEIGHT - (140.0F * Settings.scale);
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        Update();
        if (xloc != previousxloc) {
            DeckTrackerConfig.setFloat("draw-x", xloc);
            previousxloc = xloc;
        }
        if (yOffset != previousyOffset) {
            DeckTrackerConfig.setFloat("draw-y", yOffset);
            previousyOffset = yOffset;
        }
        if (xlocDiscard != previousxlocDiscard) {
            DeckTrackerConfig.setFloat("discard-x", xlocDiscard);
            previousxlocDiscard = xlocDiscard;
        }
        if (yOffsetDiscard != previousyOffsetDiscard) {
            DeckTrackerConfig.setFloat("discard-y", yOffsetDiscard);
            previousyOffsetDiscard = yOffsetDiscard;
        }
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
        } else {
            for (DeckTrackerCard dt : drawList) {
                dt.Remove();
            }
            drawList.clear();
        }
    }

    public static void Update() {
        try {
            AbstractDungeon.getCurrRoom();
        } catch (Exception e) {
            logger.error("You played a card while not in combat?");
            return;
        }
        makeDeckList(false);
        makeDeckList(true);
    }

    private static void makeDeckList(boolean discardDeck) {
        TreeMap<String, MutablePair<AbstractCard, Integer>> ret;
        ResetList(discardDeck);
        TextureAtlas.AtlasRegion energyOrbAR = AbstractDungeon.player.getOrb();
        int screenSpace;
        if (discardDeck) screenSpace = (int)((DeckTracker.screenArea*Settings.scale)-(RELICLINE - yOffsetDiscard));
        else screenSpace = (int)((DeckTracker.screenArea*Settings.scale)-(RELICLINE - yOffset));
        int currentHeight;
        float currentText;
        if (discardDeck) {
            currentHeight = defaultDiscardHeight;
            currentText = defaultDiscardText;
        }
        else {
            currentHeight = defaultDrawHeight;
            currentText = defaultDrawText;
        }

        if (discardDeck)  ret = GetCards(AbstractDungeon.player.discardPile.group);
        else ret = GetCards(AbstractDungeon.player.drawPile.group);

        int cardTypes = ret.entrySet().size();

        if (dynamicUpdate) {
            while ((cardTypes > screenSpace / currentHeight) && currentHeight >= 18) {
                currentHeight--;
            }
        }

        if (dynamicText) {
            currentText = GetTextSize(currentHeight);
        }

        if (discardDeck) {
            discardHeight = currentHeight;
            discardTextSize = currentText;
        }
        else {
            drawHeight = currentHeight;
            drawTextSize = currentText;
        }

        cardTypes = -1;
        int y = 0;
        TextureRegion TROrb;
        for (Map.Entry<String, MutablePair<AbstractCard, Integer>> entry : ret.entrySet()) {
            cardTypes++;
            if (cardTypes > screenSpace / currentHeight && cardTypes != 0) break;
            int amount = entry.getValue().getRight();
            --y;
            AbstractCard card = entry.getValue().getLeft();
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
            DeckTrackerCard dtCard;
            float yloc;
            if (discardDeck) {
                yloc = (y * discardHeight * (1.15F* Settings.scale)) + (yOffsetDiscard);
                float x = (Settings.WIDTH - ((discardWidth+discardHeight) * Settings.scale) - xlocDiscard);
                dtCard = new DeckTrackerCard(card, TROrb, x, yloc, amount, discardDeck);
                discardList.add(dtCard);
            }
            else {
                yloc = (y * drawHeight * (1.15F* Settings.scale)) + (yOffset);
                dtCard = new DeckTrackerCard(card, TROrb, xloc, yloc, amount, discardDeck);
                drawList.add(dtCard);
            }

        }
    }

    static TreeMap<String, MutablePair<AbstractCard, Integer>> GetCards(ArrayList<AbstractCard> deck) {
        TreeMap<String, MutablePair<AbstractCard, Integer>> ret = new TreeMap<>();
        for (AbstractCard card : deck) {
            String name = card.name;
            if (ret.containsKey(name))
                ret.get(name).setRight(ret.get(name).getRight()+1);
            else
                ret.put(name, new MutablePair<>(card, 1));
        }
        return ret;
    }
    public static void MoveAll(boolean discard) {
        int y = 0;
        if (discard) {
            for (DeckTrackerCard dtCard: discardList) {
                y--;
                float x = (Settings.WIDTH - ((discardWidth+discardHeight) * Settings.scale) - xlocDiscard);
                dtCard.Move(x, (y * discardHeight * (1.15F* Settings.scale)) + (yOffsetDiscard));
            }
        } else {
            for (DeckTrackerCard dtCard: drawList) {
                y--;
                dtCard.Move(xloc, (y * drawHeight * (1.15F* Settings.scale)) + (yOffset));
            }
        }

    }
    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
    static float GetTextSize(int currentHeight) {
        if (currentHeight < 10) return 0.2F;
        else if (currentHeight < 13) return 0.3F;
        else if (currentHeight < 14) return 0.4F;
        else if (currentHeight < 16) return 0.5F;
        else if (currentHeight < 18) return 0.6F;
        else if (currentHeight < 26) return 0.7F;
        else if (currentHeight < 31) return 0.8F;
        else if (currentHeight < 40) return 0.9F;
        else return 1.0F;
    }


}