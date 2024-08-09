package club.mega.gui.clicknew;

import club.mega.Mega;
import club.mega.file.ModuleSaver;
import club.mega.gui.click.ConfigGui;
import club.mega.gui.clicknew.components.*;
import club.mega.gui.clicknew.components.TextComponent;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.Setting;
import club.mega.module.setting.impl.*;
import club.mega.util.GuiBlurUtil;
import club.mega.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickGui extends GuiScreen {
    private double rectX, rectY, rectWidth, rectHeight;
    private boolean dragging, resizing, resizingRight, resizingBottom;
    private double dragOffsetX, dragOffsetY;

    private CategoryHeader categoryHeader;
    private ModuleComponent moduleComponent;
    private List<SettingComponent> settingComponents;
    private int scrollOffset;
    public static int settingScrollOffset;
    private static final int RESIZE_MARGIN = 5;
    private static final int HEADER_HEIGHT = 30;
    private static final int CATEGORY_SPACING = 40;
    private static final int MODULE_BUTTON = 120;
    private static final int TITLE_HEIGHT = 30;

    @Override
    public void initGui() {
        super.initGui();
        rectX = 50;
        rectY = 50;
        rectWidth = calculateMinWidth() + 200;
        rectHeight = 450;

        List<Category> categories = Arrays.asList(Category.values());
        categoryHeader = new CategoryHeader(categories, categories.get(0), rectX, rectY, rectWidth, CATEGORY_SPACING);

        settingComponents = new ArrayList<>();
        updateModules();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GuiBlurUtil.drawBlurredRect((float) rectX, (float) rectY, (float) rectWidth, (float) rectHeight, 5, new Color(30, 30, 30, 223));

        drawHeader();

        if (dragging) {
            rectX = mouseX - dragOffsetX;
            rectY = mouseY - dragOffsetY;
            categoryHeader.updatePosition(rectX, rectY, rectWidth);
        } else if (resizing) {
            if (resizingRight) rectWidth = Math.max(calculateMinWidth(), mouseX - rectX);
            if (resizingBottom) rectHeight = Math.max(100, mouseY - rectY);
            categoryHeader.updatePosition(rectX, rectY, rectWidth);
        }

        categoryHeader.drawHeader(mouseX, mouseY);
        moduleComponent.drawModules(mouseX, mouseY);

        updateModules();

        for (SettingComponent settingComponent : settingComponents) {
            settingComponent.drawComponent(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        categoryHeader.handleMouseClick(mouseX, mouseY);
        moduleComponent.handleMouseClick(mouseX, mouseY, mouseButton);

        for (SettingComponent settingComponent : settingComponents) {
            settingComponent.handleMouseClick(mouseX, mouseY, mouseButton);
        }

        if (mouseX >= rectX && mouseX <= rectX + rectWidth && mouseY >= rectY && mouseY <= rectY + HEADER_HEIGHT) {
            dragging = true;
            dragOffsetX = mouseX - rectX;
            dragOffsetY = mouseY - rectY;
        } else if (mouseX >= rectX + rectWidth - RESIZE_MARGIN && mouseX <= rectX + rectWidth
                && mouseY >= rectY + rectHeight - RESIZE_MARGIN && mouseY <= rectY + rectHeight) {
            resizing = true;
            resizingRight = true;
            resizingBottom = true;
        } else if (mouseX >= rectX + rectWidth - RESIZE_MARGIN && mouseX <= rectX + rectWidth
                && mouseY >= rectY && mouseY <= rectY + rectHeight) {
            resizing = true;
            resizingRight = true;
        } else if (mouseX >= rectX && mouseX <= rectX + rectWidth
                && mouseY >= rectY + rectHeight - RESIZE_MARGIN && mouseY <= rectY + rectHeight) {
            resizing = true;
            resizingBottom = true;
        }

    }

    private boolean isClickOnTitle(int mouseX, int mouseY) {
        int titleX = (int) (rectX + 7);
        int titleY = (int) (rectY + 7);
        int titleWidth = (int) Mega.INSTANCE.getFontManager().getFont("Roboto medium 25").getWidth(Mega.INSTANCE.getName());
        int titleHeight = 25; // Assuming the height of the title text is approximately 25

        return mouseX >= titleX && mouseX <= titleX + titleWidth && mouseY >= titleY && mouseY <= titleY + titleHeight;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        for (SettingComponent settingComponent : settingComponents) {
            settingComponent.handleMouseRelease(mouseX, mouseY, state); // Corrected method call
        }
        dragging = false;
        resizing = false;
        resizingRight = false;
        resizingBottom = false;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scrollAmount = Mouse.getEventDWheel();
        if (scrollAmount != 0) {
            if (isMouseInModuleArea(
                    Mouse.getEventX() * this.width / this.mc.displayWidth,
                    this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1)) {
                moduleComponent.handleMouseScroll(scrollAmount);
            } else {
                if (isMouseInSettingArea(
                        Mouse.getEventX() * this.width / this.mc.displayWidth,
                        this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1)) {
                    double totalHeight = calculateTotalHeight();
                    double visibleHeight = calculateVisibleSettingHeight();

                    // Only scroll if total height exceeds visible height
                    if (totalHeight > visibleHeight) {
                        if (scrollAmount > 0) {
                            settingScrollOffset -= 20;
                        } else {
                            settingScrollOffset += 20;
                        }

                        // Clamp scroll offset to ensure it stays within valid bounds
                        double maxOffset = Math.max(0, totalHeight - visibleHeight);
                        settingScrollOffset = (int) Math.max(0, Math.min(settingScrollOffset, maxOffset));
                    }
                }
            }
        }
    }

    private double calculateVisibleSettingHeight() {
        return rectHeight - HEADER_HEIGHT;
    }
    private double calculateTotalHeight() {
        double totalHeight = 0;
        Module expandedModule = null;

        // Find the expanded module
        for (Module module : Mega.INSTANCE.getModuleManager().getModules()) {
            if (module.isExpanded()) {
                expandedModule = module;
                break;
            }
        }

        // Calculate the total height of the settings of the expanded module
        if (expandedModule != null) {
            for (Setting setting : expandedModule.getSettings()) {
                if (setting.isVisible()) {
                    if (setting instanceof BooleanSetting || setting instanceof NumberSetting || setting instanceof ListSetting || setting instanceof RangeSetting) {
                        totalHeight += 18; // Default height for these components
                    } else if (setting instanceof ColorSetting) {
                        totalHeight += 18 * 3 + 7; // Height of ColorPickerComponent is 18 * 3 + 7
                    }
                }
            }
        }

        return totalHeight + TITLE_HEIGHT;
    }

    private boolean isMouseInSettingArea(int mouseX, int mouseY) {
        return mouseX >= rectX + 100 && mouseX <= rectX + rectWidth && mouseY >= rectY + HEADER_HEIGHT && mouseY <= rectY + rectHeight;
    }

    private boolean isMouseInModuleArea(int mouseX, int mouseY) {
        return mouseX >= rectX && mouseX <= rectX + 100 && mouseY >= rectY + HEADER_HEIGHT && mouseY <= rectY + rectHeight;
    }

    public static int getSettingScrollOffset() {
        return settingScrollOffset;
    }

    public static void setSettingScrollOffset(int settingScrollOffset) {
        ClickGui.settingScrollOffset = settingScrollOffset;
    }

    private void updateModules() {
        Category selectedCategory = categoryHeader.getSelectedCategory();
        List<Module> modules = Mega.INSTANCE.getModuleManager().getModules(selectedCategory);
        int maxVisibleModules = (int) ((rectHeight - HEADER_HEIGHT) / 20);

        if (moduleComponent != null) {
            scrollOffset = moduleComponent.getScrollOffset();
        }
        moduleComponent = new ModuleComponent(modules, rectX + 5, rectY + HEADER_HEIGHT, MODULE_BUTTON, rectHeight - HEADER_HEIGHT, maxVisibleModules);
        moduleComponent.setScrollOffset(scrollOffset);

        settingComponents.clear();

        double settingX = rectX + 120;
        double settingY = rectY + HEADER_HEIGHT - getSettingScrollOffset(); // Apply the scroll offset

        for (Module module : modules) {
            if (module.isExpanded()) {
                boolean isFirstSetting = true;
                for (Setting setting : module.getSettings()) {
                    if (setting.isVisible()) {
                        // Check if settingY is within visible bounds
                        if (settingY <= rectY + rectHeight - 20 && settingY >= rectY + HEADER_HEIGHT) {
                            // Add TitleComponent only once per module
                            if (isFirstSetting) {
                                settingComponents.add(new TitleComponent(module.getName(), module.getDescription(), module.getKey(), settingX, settingY, 200, TITLE_HEIGHT));
                                settingY += TITLE_HEIGHT + 15;
                                isFirstSetting = false;
                            }

                            // Add setting components
                            if (setting instanceof BooleanSetting) {
                                settingComponents.add(new BooleanComponent((BooleanSetting) setting, settingX, settingY, 100, 18));
                                settingY += 18;
                            } else if (setting instanceof NumberSetting) {
                                settingComponents.add(new NumberComponent((NumberSetting) setting, settingX, settingY, 100, 18));
                                settingY += 18;
                            } else if (setting instanceof ListSetting) {
                                settingComponents.add(new ListComponent((ListSetting) setting, settingX, settingY, 200, 18));
                                settingY += 18;
                            } else if (setting instanceof RangeSetting) {
                                settingComponents.add(new RangeComponent((RangeSetting) setting, settingX, settingY, 200, 18));
                                settingY += 18;
                            } else if (setting instanceof ColorSetting) {
                                settingComponents.add(new ColorPickerComponent((ColorSetting) setting, settingX, settingY, 200, 18 * 3));
                                settingY += 18 * 3 + 7; // Height of ColorPickerComponent is 18 * 3
                            } else if (setting instanceof TextSetting) {
                                settingComponents.add(new TextComponent((TextSetting) setting, settingX, settingY, 200, 18));
                                settingY += 18;
                            }
                        } else {
                            // Increment settingY even if the component is not visible
                            if (isFirstSetting) {
                                settingY += 18;
                                isFirstSetting = false;
                            } else {
                                settingY += (setting instanceof ColorSetting) ? (18 * 3 + 7) : 18;
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawHeader() {
        RenderUtil.drawPartialRoundedRect((float) rectX, (float) rectY, (float) rectWidth, (float) 25, 5, new Color(30, 30, 30, 147));
        RenderUtil.drawPartialRoundedRectBottom(rectX, rectY + 25, 100, rectHeight - 25, 5, new Color(30, 30, 30, 147));
        Mega.INSTANCE.getFontManager().getFont("Roboto medium 25").drawStringWithShadow(
                Mega.INSTANCE.getName(), (int) (rectX + 7), (int) (rectY + 7), 0xFFFFFFFF);
    }

    private double calculateMinWidth() {
        double clientNameWidth = Mega.INSTANCE.getFontManager().getFont("Roboto medium 25").getWidth(Mega.INSTANCE.getName()) + 40; // Client name width with padding
        double categoriesWidth = CategoryHeader.calculateTotalWidth(Arrays.asList(Category.values()), CATEGORY_SPACING); // Categories width with fixed spacing
        return Math.max(clientNameWidth + categoriesWidth, 300); // Ensure a reasonable minimum width
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Mega.INSTANCE.getModuleManager().getModule(club.mega.module.impl.hud.ClickGui.class).setToggled(false);
        ModuleSaver.save();
    }

    public static ClickGui getInstance() {
        return new ClickGui();
    }
}
