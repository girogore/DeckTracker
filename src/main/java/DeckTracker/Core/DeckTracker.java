package DeckTracker.Core;

import basemod.BaseMod;
import basemod.interfaces.PostDrawSubscriber;
import basemod.interfaces.StartGameSubscriber;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.OnCardUseSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDeathSubscriber;
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
import java.util.Map;
import java.util.TreeMap;

@SpireInitializer
public class DeckTracker implements PostDrawSubscriber, StartGameSubscriber, OnStartBattleSubscriber,
        OnCardUseSubscriber, PostBattleSubscriber, PostDeathSubscriber {
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

    @Override
    public void receivePostDeath() {
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
        catch (Exception e) { logger.error("You played a card while not in combat?"); return;}
        makeDeckList(false);
        makeDeckList(true);
    }

    private void makeDeckList(boolean discardDeck) {
        TreeMap<AbstractCard, Integer> ret = new TreeMap<>();
        ResetList(discardDeck);
        Texture COLORLESS_ORB = ImageMaster.loadImage("img/DeckTracker_colorless_orb.png");
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
            if (cardTypes>22) break; // temporary fix so it doesnt go over energy orb and deck
            int amount = entry.getValue();
            --y;
            AbstractCard card = entry.getKey();

            // Orbs
            switch (card.color) {
                case CURSE:
                case COLORLESS:
                    TROrb = new TextureRegion(COLORLESS_ORB, 0, 0, COLORLESS_ORB.getWidth(), COLORLESS_ORB.getHeight());
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
}
