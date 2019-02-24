package DeckTracker.Core;

import basemod.BaseMod;
import basemod.abstracts.CustomPlayer;
import basemod.interfaces.PostDrawSubscriber;
import basemod.interfaces.StartGameSubscriber;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.OnCardUseSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import com.badlogic.gdx.graphics.g2d.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SpireInitializer
public class DeckTracker implements PostDrawSubscriber, StartGameSubscriber, OnStartBattleSubscriber, OnCardUseSubscriber, PostBattleSubscriber {
    public static final Logger logger = LogManager.getLogger(DeckTracker.class.getName());
    ArrayList<DeckTrackerCard> cardList;
    ArrayList<DeckTrackerCard> discardList;

    public DeckTracker() {
        cardList = new ArrayList<DeckTrackerCard>();
        discardList = new ArrayList<DeckTrackerCard>();
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        DeckTracker tracker = new DeckTracker();
    }


    @Override
    public void receiveStartGame() {
    }

    @Override
    public void receivePostDraw(AbstractCard c) {
        Update();
    }

    @Override
    public void receiveCardUsed(AbstractCard card) {
        Update();
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

    private void ResetList(boolean discardDeck) {
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

    public void Update() {
        try {AbstractDungeon.getCurrRoom();}
        catch (Exception e) { logger.error("You played a card while not in combat?");}
        makeDeckList(false);
        makeDeckList(true);
    }

    private void makeDeckList(boolean discardDeck) {
        TreeMap<AbstractCard, Integer> ret = new TreeMap<>();
        ResetList(discardDeck);
        Texture COLORLESS_ORB = ImageMaster.loadImage("img/DeckTracker_colorless_orb.png");
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

        for (Map.Entry<AbstractCard, Integer> entry :ret.entrySet()) {
            int amount = entry.getValue();
            --y;
            AbstractCard card = entry.getKey();

            // Orbs
            switch (card.color) {
                case RED:
                    TROrb = new TextureRegion(ImageMaster.RED_ORB, 0, 0, ImageMaster.RED_ORB.packedWidth, ImageMaster.RED_ORB.packedHeight);
                    break;
                case BLUE:
                    TROrb = new TextureRegion(ImageMaster.BLUE_ORB, 0, 0, ImageMaster.BLUE_ORB.packedWidth, ImageMaster.BLUE_ORB.packedHeight);
                    break;
                case GREEN:
                    TROrb = new TextureRegion(ImageMaster.GREEN_ORB, 0, 0, ImageMaster.GREEN_ORB.packedWidth, ImageMaster.GREEN_ORB.packedHeight);
                    break;
                case CURSE:
                case COLORLESS:
                    TROrb = new TextureRegion(COLORLESS_ORB, 0, 0, COLORLESS_ORB.getWidth(), COLORLESS_ORB.getHeight());
                    break;
                default:
                    TextureAtlas.AtlasRegion energyOrbAR = ((CustomPlayer) AbstractDungeon.player).getOrb();
                    TROrb = new TextureRegion(energyOrbAR, 0, 0, energyOrbAR.packedWidth, energyOrbAR.packedHeight);
                    break;
            }
            DeckTrackerCard dtCard = new DeckTrackerCard(card, TROrb, y, amount, discardDeck);
            if (discardDeck) discardList.add(dtCard);
            else cardList.add(dtCard);
        }
    }

    private TreeMap<AbstractCard, Integer> getDiscardList() {

        TreeMap<AbstractCard, Integer> ret = new TreeMap<>();
        for (AbstractCard card : AbstractDungeon.player.discardPile.group)  {
            ret.put(card, ret.getOrDefault(card, 0) + 1);
        }
        return ret;
    }
}
