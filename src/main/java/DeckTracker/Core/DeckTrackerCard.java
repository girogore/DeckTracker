package DeckTracker.Core;

import basemod.BaseMod;
import basemod.interfaces.PreUpdateSubscriber;
import basemod.interfaces.RenderSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import basemod.abstracts.DynamicVariable;

public class DeckTrackerCard implements RenderSubscriber, PreUpdateSubscriber {
    AbstractCard card;
    Hitbox hb;
    boolean extendedTooltips; //Toggles on hitbox click
    float index;
    float cardSizeWidth,cardSizeHeight;

    int amount;
    float OFFSET = (float) Settings.HEIGHT - (160.0F * Settings.scale);
    private static int rowHeight = 25;
    private static int rowWidth = 200;
    private float xloc,width,height;
    TextureRegion orbTexture;
    String cost;
    String name;
    String description;
    BitmapFont titleFont;
    boolean discardDeck;
    public static final Logger logger = LogManager.getLogger(DeckTracker.class.getName());

    public DeckTrackerCard(AbstractCard card, TextureRegion orbTR, int y, int amount, boolean discard) {
        this.card = card.makeStatEquivalentCopy();
        this.card.drawScale = 0.7F;
        this.discardDeck = discard;

        if (discardDeck) width = (rowWidth*0.65F) * Settings.scale;
        else width = rowWidth * Settings.scale;

        height = rowHeight * Settings.scale;
        index = (y * rowHeight * 1.1F * Settings.scale) + (OFFSET);
        this.amount = amount;
        orbTexture = orbTR;
        if (discardDeck)
            xloc = Settings.WIDTH - (width+height);
        else
            xloc = 0;
        if (card.cost ==-1) cost = "X";
        else if (card.cost < 0) cost = "-";
        else cost = Integer.toString(card.cost);
        if (card.name.length() > 12 && discardDeck)
            name = card.name.substring(0,12);
        else if (card.name.length() > 17)
            name = card.name.substring(0,17);
        else
            name = card.name;
        titleFont = FontHelper.cardTitleFont_N;
        FontHelper.menuBannerFont.getData().setScale(0.7F);

        hb = new Hitbox(xloc, index, width, height);

        extendedTooltips = DeckTracker.extendedTooltips;

        cardSizeWidth = this.card.hb.width/this.card.drawScale;
        cardSizeHeight = this.card.hb.height/this.card.drawScale;
        BaseMod.subscribe(this);
    }

    public void Remove()
    {
        BaseMod.unsubscribe(this);
    }


    @Override
    public void receivePreUpdate() {
        // updating the hitbox
        this.hb.update();
        if (this.hb.justHovered) {
            try {
                UpdateDescription();
            } catch (Exception e) {
                description = card.rawDescription;
            }
        }

        //Swaps between full card view and tooltip
        if (this.hb.hovered) {
            if (InputHelper.justClickedLeft) {
                logger.info(this.card.name + " start clicked");
                this.hb.clickStarted = true;
            }
            if (this.hb.clicked) {
                this.hb.clicked = false;
                extendedTooltips = !extendedTooltips;
                logger.info(this.card.name + " clicked");
            }
        }
        else {
            extendedTooltips = DeckTracker.extendedTooltips;
        }
    }


    @Override
    public void receiveRender(SpriteBatch sb) {
        try {
            AbstractDungeon.getCurrRoom();
        }
        catch (Exception e) {
            BaseMod.unsubscribeLater(this);
            return;
        }
        //
        if (this.hb.hovered) {
            float tooltipX;
            logger.info("Config:: " + DeckTracker.extendedTooltips + "  Current :: " + extendedTooltips);
            if (discardDeck) { tooltipX = xloc - 205.0F; }
            else { tooltipX = width + (height/2) + 15.0F; }
            if (extendedTooltips)
            {
                if (discardDeck) this.card.current_x = xloc-(((this.card.drawScale*cardSizeWidth)/2) + (height/2)); // Half a card size over from start of line
                else this.card.current_x = tooltipX+((this.card.drawScale*cardSizeWidth)/2); // Half a card size over from the normal tooltip
                this.card.current_y = index-((this.card.drawScale*cardSizeHeight)/2)+height; // Half a card size up from the BOTTOM of the line.
                this.card.render(sb);
            }
            else {
                TipHelper.renderGenericTip(tooltipX, index, card.name, description);
            }
        }

        sb.setColor(Color.WHITE.cpy()); // updating the sb alpha so it actually draws..
        // Draw the orb/image
        try {
            sb.draw(orbTexture, xloc+width, index, height, height);
            TextureAtlas.AtlasRegion AR = card.portrait;
            TextureRegion TR = new TextureRegion(AR, 0, (AR.packedHeight / 3), AR.packedWidth, AR.packedHeight / 3);
            sb.draw(TR, xloc, index, width, height);
        }
        catch (Exception e) {
            logger.error("Decktracker:: card - " + name + " failed to draw.");
        }

        // Draw the text
        titleFont.getData().setScale(1.0F);
        FontHelper.renderFont(sb, FontHelper.menuBannerFont, Integer.toString(amount), xloc+3.0F, index+(2.0F * Settings.scale)+(height*0.8F), Color.GOLD);
        if (discardDeck)
            titleFont.getData().setScale(0.6F);
        else
            titleFont.getData().setScale(0.7F);
        FontHelper.renderFont(sb, titleFont, name, xloc+20, index+(height*0.7F), Color.WHITE);
        titleFont.getData().setScale(0.8F);
        if (card.cost == 1) // 1 is centered weird in this font.
            FontHelper.renderFont(sb, titleFont, cost, (xloc+width+(height*0.35F)), index+(height*0.70F), Color.WHITE);
        else
            FontHelper.renderFont(sb, titleFont, cost, xloc+width+(height*0.35F)-2.0F, index+(height*0.7F), Color.WHITE);
    }


    //https://github.com/bug-sniper/card-channeler-mod/blob/master/src/main/java/cardchanneler/orbs/ChanneledCard.java
    //Author::bug-sniper
    private String getDynamicValue(final String key) {
        String value = null;
        if (key.length() == 1){
            switch (key.charAt(0)) {
                case 'B': {
                    if (!card.isBlockModified) {
                        return Integer.toString(card.baseBlock);
                    }
                    if (card.block >= card.baseBlock) {
                        return "[#7fff00]" + Integer.toString(card.block) + "[]";
                    }
                    return "[#ff6563]" + Integer.toString(card.block) + "[]";
                }
                case 'D': {
                    if (!card.isDamageModified) {
                        return Integer.toString(card.baseDamage);
                    }
                    if (card.damage >= card.baseDamage) {
                        return "[#7fff00]" + Integer.toString(card.damage) + "[]";
                    }
                    return "[#ff6563]" + Integer.toString(card.damage) + "[]";
                }
                case 'M': {
                    if (!card.isMagicNumberModified) {
                        return Integer.toString(card.baseMagicNumber);
                    }
                    if (card.magicNumber >= card.baseMagicNumber) {
                        return "[#7fff00]" + Integer.toString(card.magicNumber) + "[]";
                    }
                    return "[#ff6563]" + Integer.toString(card.magicNumber) + "[]";
                }
                default: {
                    logger.info("KEY: " + key);
                    return Integer.toString(-99);
                }
            }
        }
        else {
            DynamicVariable dv = BaseMod.cardDynamicVariableMap.get(key);
            if (dv != null) {
                if (dv.isModified(card)) {
                    if (dv.value(card) >= dv.baseValue(card)) {
                        value = "[#" + dv.getIncreasedValueColor().toString() + "]" + Integer.toString(dv.value(card)) + "[]";
                    } else {
                        value = "[#" + dv.getDecreasedValueColor().toString() + "]" + Integer.toString(dv.value(card)) + "[]";
                    }
                } else {
                    value = Integer.toString(dv.baseValue(card));
                }
            }
            logger.info(key + " is " + value);
            return (String) value;
        }
    }

    //https://github.com/bug-sniper/card-channeler-mod/blob/master/src/main/java/cardchanneler/orbs/ChanneledCard.java
    //Author::bug-sniper
    // Set the on-hover description of the card
    public void UpdateDescription() {
        description = "";
        boolean firstWord = true;
        card.initializeDescription();
        String descriptionFragment = "";
        for (int i=0; i<card.description.size(); i++){
            descriptionFragment = card.description.get(i).text;
            for (String word : descriptionFragment.split(" ")) {
                if (firstWord){
                    firstWord = false;
                }else{
                    description += " ";
                }
                if (word.length() > 0 && word.charAt(0) == '*') {
                    word = word.substring(1);
                    String punctuation = "";
                    if (word.length() > 1 && !Character.isLetter(word.charAt(word.length() - 2))) {
                        punctuation += word.charAt(word.length() - 2);
                        word = word.substring(0, word.length() - 2);
                        punctuation += ' ';
                    }
                    description += word;
                    description += punctuation;
                }
                else if (word.length() > 0 && word.charAt(0) == '!') {
                    String key = "";
                    for (int j=1; j<word.length(); j++){
                        if (word.charAt(j) == '!'){
                            description += getDynamicValue(key);
                            description += word.substring(j+1);
                        }
                        else {
                            key += word.charAt(j);
                        }
                    }
                }
                else{
                    description += word;
                }
            }
        }
    }
}
