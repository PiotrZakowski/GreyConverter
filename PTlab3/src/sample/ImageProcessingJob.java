package sample;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageProcessingJob {

    File file;

    SimpleStringProperty status;

    DoubleProperty progress;

    public final static String STATUS_WAITING = "waiting...";
    public final static String STATUS_PROCESSING = "processing...";
    public final static String STATUS_DONE = "done.";

    public ImageProcessingJob(File fileGiven)
    {
        this.file = fileGiven;
        this.status= new SimpleStringProperty(this.STATUS_WAITING);
        this.progress= new SimpleDoubleProperty(0);
    }

    public File getFile()
    {
        return this.file;
    }

    public SimpleStringProperty getStatusProperty()
    {
        return this.status;
    }

    public void setStatusProperty( SimpleStringProperty statusGiven)
    {
        this.status=statusGiven;
    }

    public DoubleProperty getProgressProperty()
    {
        return this.progress;
    }

    public void setProgressProperty(DoubleProperty progressGiven)
    {
        this.progress=progressGiven;
    }

    public static void convertToGrayscale(
            File originalFile, //oryginalny plik graficzny
            File outputDir, //katalog docelowy
            DoubleProperty progressProp,//własność określająca postęp operacji
            SimpleStringProperty statusProp
    )
    {
        try {
            Platform.runLater(() -> statusProp.set(STATUS_PROCESSING));
            //wczytanie oryginalnego pliku do pamięci
            BufferedImage original = ImageIO.read(originalFile);
            //przygotowanie bufora na grafikę w skali szarości
            BufferedImage grayscale = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
            //przetwarzanie piksel po pikselu
            for (int i = 0; i < original.getWidth(); i++)
            {
                for (int j = 0; j < original.getHeight(); j++)
                {
                    //pobranie składowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasności piksela dla obrazu w skali szarości
                    int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
                    //przygotowanie wartości koloru w oparciu o obliczoną jaskość
                    int newPixel = new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postępu przetwarzania jako liczby z przedziału [0, 1]
                double progress = (1.0 + i) / original.getWidth();
                //aktualizacja własności zbindowanej z paskiem postępu w tabeli
                Platform.runLater(() -> progressProp.set(progress));
            }
            Platform.runLater(() -> statusProp.set(STATUS_DONE));
            //przygotowanie ścieżki wskazującej na plik wynikowy
            Path outputPath = Paths.get(outputDir.getAbsolutePath(), originalFile.getName());
            //zapisanie zawartości bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());

        } catch (IOException ex) {
            //translacja wyjątku
            throw new RuntimeException(ex);
        }
    }
}
