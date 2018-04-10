package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.ForkJoinPool.commonPool;
import static sample.ImageProcessingJob.convertToGrayscale;

public class Controller {

    File selectedDirectory=null;

    List<File> selectedFiles;

    ObservableList<ImageProcessingJob> selectedImages = FXCollections.observableArrayList();

    public int threadNumber = ForkJoinPool.getCommonPoolParallelism();
    //public int threadNumber = commonPool().getParallelism();

    public long start;

    public long end;

    public long duration;

    @FXML
    private TableView<ImageProcessingJob> filesTable;

    @FXML
    private TableColumn<ImageProcessingJob, String> imageNameColumn;

    @FXML
    private TableColumn<ImageProcessingJob, Double> progressColumn;

    @FXML
    private TableColumn<ImageProcessingJob, String> statusColumn;

    @FXML
    private Label timeLabel;

    @FXML
    private Label threadLabel;


    public void initialize() {
        imageNameColumn.setCellValueFactory( p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory(  p -> p.getValue().getStatusProperty());
        progressColumn.setCellFactory( ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressColumn.setCellValueFactory( p -> p.getValue().getProgressProperty().asObject());

        threadLabel.setText(""+this.threadNumber);
    }

    @FXML
    void sequenceProcess(ActionEvent event) {
        if(selectedDirectory==null)
            addDirectory(event);

        this.start = System.currentTimeMillis(); //zwraca aktualny czas [ms]

        new Thread(this::backgroundJob).start();
    }

    @FXML
    void parallelProcess(ActionEvent event) {
        if(selectedDirectory==null)
            addDirectory(event);

        this.start = System.currentTimeMillis(); //zwraca aktualny czas [ms]

        ForkJoinPool pool = new ForkJoinPool(this.threadNumber);
        //pool.submit(this::backgroundJob);
        pool.submit(() -> this.selectedImages.parallelStream().forEach(this::transform));

        /*
        end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        duration = end-start; //czas przetwarzania [ms]
        //this.timeLabel.setText("Time: "+duration+"ms");
        Platform.runLater(() -> this.timeLabel.setText("Time: "+duration+"ms"));
        */
    }

    @FXML
    void addDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        this.selectedDirectory=directoryChooser.showDialog(null);
    }

    @FXML
    void addFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        this.selectedFiles = fileChooser.showOpenMultipleDialog(null);

        for(File file : this.selectedFiles)
        {
            this.selectedImages.add(new ImageProcessingJob(file));
        }

        this.filesTable.setItems(selectedImages);
    }

    @FXML
    void clear(ActionEvent event) {
        this.selectedFiles=null;
        //this.selectedImages.clear();
        this.selectedImages.removeAll(selectedImages);
        this.filesTable.setItems(selectedImages);
    }

    @FXML
    void lessThreads(ActionEvent event) {
        if(this.threadNumber>1)
        {
            this.threadNumber=this.threadNumber-1;
            threadLabel.setText(""+this.threadNumber);
        }
    }

    @FXML
    void moreThreads(ActionEvent event) {
        this.threadNumber=this.threadNumber+1;
        threadLabel.setText(""+this.threadNumber);
    }

    //metoda uruchamiana w tle (w tej samej klasie)
    private void backgroundJob()
    {
        //...operacje w tle...
        this.selectedImages.stream().forEach(this::transform);

    }

    private void transform(ImageProcessingJob job)
    {
        convertToGrayscale(job.getFile(),this.selectedDirectory,job.getProgressProperty(),job.getStatusProperty());

        end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        duration = end-start; //czas przetwarzania [ms]
        //this.timeLabel.setText("Time: "+duration+"ms");
        Platform.runLater(() -> this.timeLabel.setText("Time: "+duration+"ms"));

    }
}
