<<<<<<< HEAD
public enum DinoType {
=======
import javax.swing.ImageIcon;
import java.net.URL;

public enum DinoType {

>>>>>>> main
    D("DinoSprites_doux.gif"),
    T("DinoSprites_tard.gif"),
    M("DinoSprites_mort.gif"),
    V("DinoSprites_vita.gif");

    private final String fileName;

    DinoType(String fileName) {
        this.fileName = fileName;
    }

    public ImageIcon getIcon() {
        URL resource = getClass().getResource("/gifs/" + fileName);
<<<<<<< HEAD
        if (resource == null) {
            throw new RuntimeException("Image not found: " + fileName);
        }
=======

        if (resource == null) {
            throw new RuntimeException("Image not found: " + fileName);
        }

>>>>>>> main
        return new ImageIcon(resource);
    }
}