package club.mega.gui.clicknew;

public abstract class SettingComponent {
    protected double x, y, width, height;
    protected boolean dragging;
    protected double dragOffsetX, dragOffsetY;

    public SettingComponent(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dragging = false;
    }

    public abstract void drawComponent(int mouseX, int mouseY);

    public abstract void handleMouseClick(int mouseX, int mouseY, int mouseButton);


    public abstract void handleMouseRelease(int mouseX, int mouseY, int state);

    public void handleMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public abstract void handleKeyPress(char typedChar, int keyCode);
}
