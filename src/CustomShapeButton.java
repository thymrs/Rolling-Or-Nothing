
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker().darker());
        } else if (getModel().isRollover()) {
            g2.setColor(getBackground().darker());
        } else {
            g2.setColor(getBackground());
        }

        g2.fill(customShape);

        if (tileImage != null) {
            Shape oldClip = g2.getClip();
            g2.setClip(customShape);
            g2.drawImage(tileImage, 0, 0, getWidth(), getHeight(), this);
            g2.setClip(oldClip);
        }

        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        Rectangle bounds = customShape.getBounds();

        int cx = bounds.x + (bounds.width / 2);
        int cy = bounds.y + (bounds.height / 2);

        int tx = cx - (fm.stringWidth(getText()) / 2);
        int ty = cy - (fm.getHeight() / 2) + fm.getAscent();

        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (customShape == null)
            return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(customShape);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        if (customShape == null) {
            return super.contains(x, y);
        }
        return customShape.contains(x, y);
    }
}