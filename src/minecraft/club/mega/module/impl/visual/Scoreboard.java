package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventBlur;
import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.hud.ModuleList;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.Collection;
import java.util.List;

@Module.ModuleInfo(name = "Scoreboard", description = "nojgre", category = Category.VISUAL)
public class Scoreboard extends Module {

    public final BooleanSetting blur = new BooleanSetting("Blur", this, true);
    public final BooleanSetting rounded = new BooleanSetting("Rounded", this, true);
    public final NumberSetting radius = new NumberSetting("Radius", this, 1, 10, 5, 1, rounded::get);
    private final ListSetting mode = new ListSetting("Color Mode", this, new String[]{"Normal", "Fade"});
    private final NumberSetting speed = new NumberSetting("Fade Speed", this, 1, 50, 15, 0.1, () -> mode.is("Fade"));
    private final ColorSetting colornormal = new ColorSetting("Color", this, new Color(0, 0, 0, 173), () -> mode.is("Normal"));
    private final BooleanSetting thirdColor = new BooleanSetting("Third Color", this, true, () -> mode.is("Fade"));
    private final ColorSetting color1 = new ColorSetting("Start Color", this, new Color(0, 187, 255, 73), () -> mode.is("Fade"));
    private final ColorSetting color2 = new ColorSetting("Mid Color", this, new Color(0, 234, 255, 73), () -> mode.is("Fade") && thirdColor.get());
    private final ColorSetting color3 = new ColorSetting("End Color", this, new Color(0, 255, 196, 73), () -> mode.is("Fade"));
    public final ListSetting side = new ListSetting("Side", this, new String[]{"Right", "Left"});

    private int index = 0;
    public int x = 0;
    public int y= 0;
    public int width = 0;
    public int height = 0;

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(side.getCurrent());
    }

    @Handler
    public  void render2d(EventRender2D event) {
        renderScoreboardMain();
    }

    public void renderScoreboardMain() {
        ScaledResolution scaledresolution = new ScaledResolution(MC);
        net.minecraft.scoreboard.Scoreboard scoreboard = MC.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(MC.thePlayer.getName());

        if (scoreplayerteam != null) {
            int i1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (i1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            this.renderScoreboard(scoreobjective1, scaledresolution);
        }
    }
    public void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes) {
        net.minecraft.scoreboard.Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>() {
            public boolean apply(Score p_apply_1_) {
                return p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#");
            }
        }));

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        int i = MC.fontRendererObj.getStringWidth(objective.getDisplayName());
        int i1 = collection.size() * MC.fontRendererObj.FONT_HEIGHT;
        int k1 = 3;

        int scoreboardWidth = 0;
        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            int stringWidth = MC.fontRendererObj.getStringWidth(s);
            scoreboardWidth = Math.max(scoreboardWidth, stringWidth);
        }

        int j1;

        if (side.is("Right")) {
            j1 = Mega.INSTANCE.getModuleManager().isToggled(ModuleList.class) ? Mega.INSTANCE.getModuleManager().getModule(ModuleList.class).getHeight() + Mega.INSTANCE.getModuleManager().getModule(ModuleList.class).verticalOffset.getAsInt() + 20 + i1 : scaledRes.getScaledHeight() / 2 + i1 / 3;
        } else {
            j1 = scaledRes.getScaledHeight() / 2 + i1 / 3;
        }

        int l1;

        if (side.is("Right")) {
            l1 = scaledRes.getScaledWidth() - scoreboardWidth - k1;
        } else {
            l1 = k1 - 1;
        }

        int j = 0;
        int blurredRectHeight = collection.size() * MC.fontRendererObj.FONT_HEIGHT;

        int x = l1 - 2;
        int y = j1 - blurredRectHeight - MC.fontRendererObj.FONT_HEIGHT - 2;
        int width = scoreboardWidth + k1 + 4;
        int height123123 = blurredRectHeight + MC.fontRendererObj.FONT_HEIGHT + 4;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height123123;
        if(!rounded.get()) {

            RenderUtil.drawRect(x, y, width, height123123, getColor());
        } else {
            RenderUtil.drawRoundedRect(x, y, width, height123123, radius.getAsFloat() , getColor());
        }


        index += (int) 0.1;

        for (Score score1 : collection) {
            ++j;
            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
            String s2 = EnumChatFormatting.RED + "" + score1.getScorePoints();
            int k = j1 - j * MC.fontRendererObj.FONT_HEIGHT;
            int l;

            if (side.is("Right")) {
                l = scaledRes.getScaledWidth() - k1 + 2;
            } else {
                l = l1 + scoreboardWidth + 2;
            }

            MC.fontRendererObj.drawString(s1, l1, k, 553648127);
            MC.fontRendererObj.drawString(s2, l - MC.fontRendererObj.getStringWidth(s2), k, 553648127);

            if (j == collection.size()) {
                String s3 = objective.getDisplayName();
                MC.fontRendererObj.drawString(s3, l1 + scoreboardWidth / 2 - MC.fontRendererObj.getStringWidth(s3) / 2, k - MC.fontRendererObj.FONT_HEIGHT, 553648127);
            }
        }
    }

    @Handler
    public final void onBlur(EventBlur event) {
        if (blur.get()) {
            net.minecraft.scoreboard.Scoreboard scoreboard = MC.theWorld.getScoreboard();
            ScoreObjective scoreobjective = null;
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(MC.thePlayer.getName());

            if (scoreplayerteam != null) {
                int i1 = scoreplayerteam.getChatFormat().getColorIndex();

                if (i1 >= 0) {
                    scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
                }
            }

            ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

            if (scoreobjective1 != null) {
                if (!rounded.get()) {
                    RenderUtil.drawRect(x, y, width, height, -1);
                } else {
                    RenderUtil.drawRoundedRect(x, y, width, height, radius.getAsFloat(), Color.red);
                }
            }
        }
    }

    private Color getColor() {

        Color[] colors;
        if (mode.is("Normal")) {
            colors = new Color[]{colornormal.getColor()};
        } else if (mode.is("Fade") && !thirdColor.get()) {
            colors = new Color[]{color1.getColor(), color3.getColor()};
        } else {
            colors = new Color[]{color1.getColor(), color2.getColor(), color3.getColor()};

        }
        return ColorUtil.interpolateColorsBackAndForth(speed.getAsInt(), 0, colors, false);

    }

}