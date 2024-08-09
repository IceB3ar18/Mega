package club.mega.gui.clicknew;

import club.mega.Mega;
import club.mega.module.Category;
import club.mega.module.impl.hud.ClickGui;

import java.awt.*;
import java.util.List;

public class CategoryHeader {
    private List<Category> categories;
    private Category selectedCategory;
    private double rectX, rectY, rectWidth;
    private int categorySpacing;

    public CategoryHeader(List<Category> categories, Category selectedCategory, double rectX, double rectY, double rectWidth, int categorySpacing) {
        this.categories = categories;
        this.selectedCategory = selectedCategory;
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
        this.categorySpacing = categorySpacing;
    }

    public void updatePosition(double rectX, double rectY, double rectWidth) {
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
    }

    public void drawHeader(int mouseX, int mouseY) {
        double startX = rectX + 65 + Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(Mega.INSTANCE.getName()) + 10;
        double startY = rectY + 7;

        for (Category category : categories) {
            int color = (category == selectedCategory) ? ClickGui.getInstance().color.getColor().getRGB() : new Color(157, 157, 157,255).getRGB();
            Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").drawStringWithShadow(category.getName().toUpperCase(), (int) startX, (int) startY, color);
            startX += Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(category.getName().toUpperCase()) + categorySpacing;
        }
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        double startX = rectX + 65 + Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(Mega.INSTANCE.getName().toUpperCase()) + 10;
        double startY = rectY + 10;

        for (Category category : categories) {
            if (mouseX >= startX && mouseX <= startX + Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(category.getName()) && mouseY >= startY && mouseY <= startY + 10) {
                selectedCategory = category;
                break;
            }
            startX += Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(category.getName().toUpperCase()) + categorySpacing;
        }
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public static double calculateTotalWidth(List<Category> categories, int categorySpacing) {
        double totalWidth = 0;
        for (Category category : categories) {
            totalWidth += Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(category.getName().toUpperCase()) + categorySpacing;
        }
        return totalWidth;
    }
}