import javax.swing.ImageIcon;

public enum DinoType {
    D("DinoSprites_doux.gif"),
    T("DinoSprites_tard.gif"),
    M("DinoSprites_mort.gif");
    private final String fileName;

    DinoType(String fileName) {
        this.fileName = fileName;
    }

    public ImageIcon getIcon() {
    URL resource = DinoType.class.getResource("/gifs/" + fileName);

    if (resource == null) {
        System.err.println("Image not found: /gifs/" + fileName);
        return null;
    }

    return new ImageIcon(resource);
}
}