package DeckTracker.Core;

import basemod.*;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.io.IOException;
import java.util.Properties;

public class DeckTrackerConfig implements PostInitializeSubscriber {
    private ModLabeledToggleButton tooltipButton, dynamicButton, dynamicTextButton, frozenEyeButton;
    private ModSlider dhSlider, dwSlider, dtSlider, dishSlider, distSlider, diswSlider;
    private static SpireConfig config;

    public DeckTrackerConfig() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
    }

    // Config file initialization and screen
    private static SpireConfig makeConfig() {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("extended-tooltip",  Boolean.toString(false));
        defaultProperties.setProperty("dynamic-update",  Boolean.toString(true));
        defaultProperties.setProperty("dynamic-text",  Boolean.toString(true));
        defaultProperties.setProperty("frozen-eye",  Boolean.toString(true));

        defaultProperties.setProperty("draw-width",  Integer.toString(200));
        defaultProperties.setProperty("draw-height", Integer.toString(28));
        defaultProperties.setProperty("draw-text-size", Float.toString(0.7F));
        defaultProperties.setProperty("draw-x", Float.toString(0));
        defaultProperties.setProperty("draw-y", Float.toString(Settings.HEIGHT - (140.0F * Settings.scale)));
        defaultProperties.setProperty("discard-width",  Integer.toString(130));
        defaultProperties.setProperty("discard-height", Integer.toString(28));
        defaultProperties.setProperty("discard-text-size", Float.toString(0.6F));
        defaultProperties.setProperty("discard-x", Float.toString(0));
        defaultProperties.setProperty("discard-y", Float.toString(Settings.HEIGHT - (140.0F * Settings.scale)));

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
                DeckTracker.extendedTooltips = entryB;
            } catch (Exception e) {
                DeckTracker.extendedTooltips = false;
            }
            try {
                entryB = getBoolean("dynamic-update");
                DeckTracker.dynamicUpdate = entryB;
            } catch (Exception e) {
                DeckTracker.dynamicUpdate = true;
            }
            try {
                entryB = getBoolean("dynamic-text");
                DeckTracker.dynamicText = entryB;
            } catch (Exception e) {
                DeckTracker.dynamicText = true;
            }
            try {
                entryB = getBoolean("frozen-eye");
                DeckTracker.frozenEye = entryB;
            } catch (Exception e) {
                DeckTracker.frozenEye = true;
            }
            try {
                entryI = getInt("draw-width");
                DeckTracker.drawWidth = entryI;
            } catch (Exception e) {
                DeckTracker.drawWidth = 200;
            }
            try {
                entryI = getInt("draw-height");
                DeckTracker.defaultDrawHeight = entryI;
            } catch (Exception e) {
                DeckTracker.defaultDrawHeight = 28;
            }
            try {
                entryF = getFloat("draw-text-size");
                DeckTracker.defaultDrawText = entryF;
            } catch (Exception e) {
                DeckTracker.defaultDrawText = 0.7F;
            }
            try {
                entryF = getFloat("draw-x");
                DeckTracker.xloc = DeckTracker.clamp(entryF, 0, (Settings.WIDTH - ((DeckTracker.drawWidth + DeckTracker.drawHeight*2) * Settings.scale)));
                DeckTracker.previousxloc  =DeckTracker.xloc;
            } catch (Exception e) {
                DeckTracker.xloc = 0;
            }
            try {
                entryF = getFloat("draw-y");
                DeckTracker.yOffset = DeckTracker.clamp(entryF, 200, Settings.HEIGHT - (140.0F * Settings.scale));
                DeckTracker.previousyOffset = DeckTracker.yOffset;
            } catch (Exception e) {
                DeckTracker.yOffset = Settings.HEIGHT - (140.0F * Settings.scale);
            }
            try {
                entryI = getInt("discard-width");
                DeckTracker.discardWidth = entryI;
            } catch (Exception e) {
                DeckTracker.discardWidth = 130;
            }
            try {
                entryI = getInt("discard-height");
                DeckTracker.defaultDiscardHeight = entryI;
            } catch (Exception e) {
                DeckTracker.defaultDiscardHeight = 28;
            }
            try {
                entryF = getFloat("discard-text-size");
                DeckTracker.defaultDiscardText = entryF;
            } catch (Exception e) {
                DeckTracker.defaultDiscardText = 0.7F;
            }
            try {
                entryF = getFloat("discard-x");
                DeckTracker.xlocDiscard = DeckTracker.clamp(entryF, 0, (Settings.WIDTH - ((DeckTracker.drawWidth + DeckTracker.drawHeight*2) * Settings.scale)));
                DeckTracker.previousxlocDiscard = DeckTracker.xlocDiscard;
            } catch (Exception e) {
                DeckTracker.xlocDiscard = 0;
            }
            try {
                entryF = getFloat("discard-y");
                DeckTracker.yOffsetDiscard = DeckTracker.clamp(entryF, 200, Settings.HEIGHT - (140.0F * Settings.scale));
                DeckTracker.previousyOffsetDiscard = DeckTracker.yOffsetDiscard;
            } catch (Exception e) {
                DeckTracker.yOffsetDiscard = Settings.HEIGHT - (140.0F * Settings.scale);
            }
        }
    }

    @Override
    public void receivePostInitialize() {
        config = makeConfig();
        setProperties();

        ModPanel settingsPanel = new ModPanel();
        ModButton defaultButton;
        ModLabel configLabel;
        float y = 750.0F;
        float x = 375.0F;
        defaultButton = new ModButton(1400.0F, y-(70* Settings.scale), ImageMaster.loadImage("img/DefaultButton.png"), settingsPanel, (button) -> {
            DeckTracker.extendedTooltips = false;
            DeckTracker.dynamicUpdate = true;
            DeckTracker.dynamicText = true;
            DeckTracker.frozenEye = true;
            DeckTracker.drawWidth = 200;
            DeckTracker.defaultDrawHeight = 28;
            DeckTracker.defaultDrawText = 0.7F;
            DeckTracker.xloc = 0;
            DeckTracker.yOffset = Settings.HEIGHT - (140.0F * Settings.scale);
            DeckTracker.defaultDiscardHeight = 28;
            DeckTracker.discardWidth = 130;
            DeckTracker.defaultDiscardText = 0.6F;
            DeckTracker.xlocDiscard = 0;
            DeckTracker.yOffsetDiscard = Settings.HEIGHT - (140.0F * Settings.scale);

            setBoolean("dynamic-update", false);
            setBoolean("extended-tooltip", true);
            setBoolean("dynamic-text", true);
            setBoolean("frozen-eye", true);
            setInt("draw-width", DeckTracker.drawWidth);
            setInt("draw-height", DeckTracker.defaultDrawHeight);
            setFloat("draw-text-size", DeckTracker.defaultDrawText);
            setFloat("draw-x", DeckTracker.xloc);
            setFloat("draw-y", DeckTracker.yOffset);
            setInt("discard-height", DeckTracker.defaultDiscardHeight);
            setInt("discard-width", DeckTracker.discardWidth);
            setFloat("discard-text-size", DeckTracker.defaultDiscardText);
            setFloat("discard-x", DeckTracker.xlocDiscard);
            setFloat("discard-y", DeckTracker.yOffsetDiscard);

            tooltipButton.toggle.enabled = false;
            dynamicButton.toggle.enabled = true;
            dynamicTextButton.toggle.enabled = true;
            frozenEyeButton.toggle.enabled = true;
            dwSlider.setValue(getInt("draw-width")/dwSlider.multiplier);
            dhSlider.setValue(getInt("draw-height")/dhSlider.multiplier);
            dtSlider.setValue(getFloat("draw-text-size"));
            diswSlider.setValue(getInt("discard-width")/diswSlider.multiplier);
            dishSlider.setValue(getInt("discard-height")/dishSlider.multiplier);
            distSlider.setValue(getFloat("discard-text-size"));
        });
        settingsPanel.addUIElement(defaultButton);
        tooltipButton = new ModLabeledToggleButton("Display full cards in tooltips.", x, y, Settings.CREAM_COLOR, FontHelper.charDescFont, DeckTracker.extendedTooltips, settingsPanel, (label) -> {
        }, (button) -> {
            DeckTracker.extendedTooltips = button.enabled;
            setBoolean("extended-tooltip", button.enabled);
        });
        settingsPanel.addUIElement(tooltipButton);
        y -= 40;
        dynamicButton = new ModLabeledToggleButton("Dynamically change height based on decksize.", x, y, Settings.CREAM_COLOR, FontHelper.charDescFont, DeckTracker.dynamicUpdate, settingsPanel, (label) -> {
        }, (button) -> {
            DeckTracker.dynamicUpdate = button.enabled;
            setBoolean("dynamic-update", button.enabled);
        });
        settingsPanel.addUIElement(dynamicButton);
        y -= 40;
        dynamicTextButton = new ModLabeledToggleButton("Change textsize based on height.", x, y, Settings.CREAM_COLOR, FontHelper.charDescFont, DeckTracker.dynamicText, settingsPanel, (label) -> {
        }, (button) -> {
            DeckTracker.dynamicText = button.enabled;
            setBoolean("dynamic-text", button.enabled);
        });
        settingsPanel.addUIElement(dynamicTextButton);
        y -= 40;
        frozenEyeButton = new ModLabeledToggleButton("Frozen Eye Support", x, y, Settings.CREAM_COLOR, FontHelper.charDescFont, DeckTracker.frozenEye, settingsPanel, (label) -> {
        }, (button) -> {
            DeckTracker.frozenEye = button.enabled;
            setBoolean("frozen-eye", button.enabled);
        });
        settingsPanel.addUIElement(frozenEyeButton);
        // DRAW DECK
        y -= 60;
        configLabel = new ModLabel("Draw Deck", x, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        y -= 40;
        configLabel = new ModLabel("Width", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        dwSlider = new ModSlider("", (x*1.5F)+100, y, 250.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            DeckTracker.drawWidth = val;
            setInt("draw-width", val);
        } );
        dwSlider.setValue(getInt("draw-width")/dwSlider.multiplier);
        settingsPanel.addUIElement(dwSlider);
        y -= 40;
        configLabel = new ModLabel("Height", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        dhSlider = new ModSlider("", (x*1.5F)+100, y, 50.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            DeckTracker.defaultDrawHeight = val;
            setInt("draw-height", val);
        } );
        dhSlider.setValue(getInt("draw-height")/dhSlider.multiplier);
        settingsPanel.addUIElement(dhSlider);
        y -= 40;
        configLabel = new ModLabel("Text Size", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        dtSlider = new ModSlider("", (x*1.5F)+100, y, 10.0F, "", settingsPanel, (slider) -> {
            final float val = Math.max(0.01F,slider.value);
            DeckTracker.defaultDrawText = val;
            setFloat("draw-text-size", val);
        } );
        dtSlider.setValue(getFloat("draw-text-size"));
        settingsPanel.addUIElement(dtSlider);

        //DISCARD DECK
        y -= 60;
        configLabel = new ModLabel("Discard Deck", x, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        y -= 40;
        configLabel = new ModLabel("Width", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        diswSlider = new ModSlider("", (x*1.5F)+100, y, 250.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            DeckTracker.discardWidth = val;
            setInt("discard-width", val);
        } );
        diswSlider.setValue(getInt("discard-width")/diswSlider.multiplier);
        settingsPanel.addUIElement(diswSlider);
        y -= 40;
        configLabel = new ModLabel("Height", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        dishSlider = new ModSlider("", (x*1.5F)+100, y, 50.0F, "", settingsPanel, (slider) -> {
            final int val = Math.max(1,Math.round(slider.value * slider.multiplier));
            DeckTracker.defaultDiscardHeight = val;
            setInt("discard-height", val);
        } );
        dishSlider.setValue(getInt("discard-height")/dishSlider.multiplier);
        settingsPanel.addUIElement(dishSlider);
        y -= 40;
        configLabel = new ModLabel("Text Size", x+100, y, Settings.CREAM_COLOR, settingsPanel, (label) -> {});
        settingsPanel.addUIElement(configLabel);
        distSlider = new ModSlider("", (x*1.5F)+100, y, 10.0F, "", settingsPanel, (slider) -> {
            final float val = Math.max(0.01F,slider.value);
            DeckTracker.defaultDiscardText = val;
            setFloat("discard-text-size", val);
        } );
        distSlider.setValue(getFloat("discard-text-size"));
        settingsPanel.addUIElement(distSlider);


        Texture badgeTexture = ImageMaster.loadImage("img/Decktracker-ModBadge.png");
        BaseMod.registerModBadge(badgeTexture, "StSDeckTracker", "Girogore", "Provides an in-game deck tracker", settingsPanel);
    }
}
