package wjhk.jupload2.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Icon implementation, to control and indicate the current sort order, into the
 * {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class SortArrowIcon implements Icon {
  public static final int NONE = 0;
  public static final int DECENDING = 1;
  public static final int ASCENDING = 2;

  protected int direction;
  protected int width = 8;
  protected int height = 8;

  public SortArrowIcon(int direction) {
    this.direction = direction;
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color bg = c.getBackground();
    Color light = bg.brighter().brighter();
    Color shade = bg.darker().darker();

    int w = width;
    int h = height;
    int m = w / 2;
    if (direction == ASCENDING) {
      g.setColor(shade);
      g.drawLine(x, y, x + w, y);
      g.drawLine(x, y, x + m, y + h);
      g.setColor(light);
      g.drawLine(x + w, y, x + m, y + h);
    }
    if (direction == DECENDING) {
      g.setColor(shade);
      g.drawLine(x + m, y, x, y + h);
      g.setColor(light);
      g.drawLine(x, y + h, x + w, y + h);
      g.drawLine(x + m, y, x + w, y + h);
    }
  }
}
