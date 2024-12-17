package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Controller {
    public ColorPicker colorPickerStroke;
    public ColorPicker colorPickerFill;
    public Slider sliderSetSize;
    public CheckBox fillCheckBox;
    @FXML
    private ToggleButton lineToggle;
    @FXML
    Canvas canvas;
    Model model;
    Points points;

    //Добавим переменные для хранения начальной и конечной точек линии:
    private boolean isDrawingLine = false;
    private double startX, startY;
    private double endX;
    private double endY;
    private Image snapshot;
    Image bgImage;
    double bgX, bgY, bgW = 300.0, bgH = 300.0;
    public void initialize(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        model = new Model();
        lineToggle.setSelected(false);
        initSliderWidth();
        initFillCheckBox();
        colorPickerStroke.setValue(Color.BLACK);
    }

    public void initSliderWidth() {
        sliderSetSize.setMin(0);
        sliderSetSize.setMax(20);
        sliderSetSize.setValue(1);
        sliderSetSize.setShowTickMarks(true);
        sliderSetSize.setShowTickLabels(true);
        sliderSetSize.setBlockIncrement(2.0);
        sliderSetSize.setMajorTickUnit(5.0);
        sliderSetSize.setMinorTickCount(4);
    }

    public void initFillCheckBox(){
        fillCheckBox.setSelected(false);
        fillCheckBox.setText("Заливка");
    }

    private void releaseEmpty(MouseEvent event) {
        //Ничего не делает лол, нужен для того, чтобы после переключения с фигуры больше ничего не происходило
        //В противном случае даже если ткнуть карандаш эта штука будет или ошибку выкидывать или параллельно с карандашом клепать многоугольники
    }
    //Карандаш
    public void setDrawPencil(){
        canvas.setOnMousePressed(this::startPencil);
        canvas.setOnMouseDragged(this::dragPencil);
        canvas.setOnMouseReleased(this::releaseEmpty);
    }
    public void startPencil(MouseEvent event){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        startX = event.getX();
        startY = event.getY();
        gc.setFill(colorPickerStroke.getValue());
        gc.fillOval(startX,startY, sliderSetSize.getValue(), sliderSetSize.getValue());
    }
    public void dragPencil(MouseEvent event){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        startX = event.getX();
        startY = event.getY();
        gc.setFill(colorPickerStroke.getValue());
        gc.fillOval(startX,startY, sliderSetSize.getValue(), sliderSetSize.getValue());
    }
    //Ластик
    public void setEraser(){
        canvas.setOnMousePressed(this::startEraser);
        canvas.setOnMouseDragged(this::dragEraser);
        canvas.setOnMouseReleased(this::releaseEmpty);
    }
    public void startEraser(MouseEvent event){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        startX = event.getX();
        startY = event.getY();
        gc.clearRect(startX,startY, sliderSetSize.getValue(), sliderSetSize.getValue());
    }
    public void dragEraser(MouseEvent event){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        startX = event.getX();
        startY = event.getY();
        gc.clearRect(startX,startY, sliderSetSize.getValue(), sliderSetSize.getValue());
    }
    //Пятиугольник
    public void setPentagon(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(this::startPentagon);
        canvas.setOnMouseDragged(event -> dragPentagon(event,gc));
        canvas.setOnMouseReleased(event -> releasePentagon(event,gc));
    }
    private void startPentagon(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        snapshot = canvas.snapshot(null,null);
    }

    private void dragPentagon(MouseEvent event, GraphicsContext gc) {
        endX = event.getX();
        endY = event.getY();
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.drawImage(snapshot,0,0);
        drawPentagon(gc, startX, startY, endX, endY);
    }

    private void releasePentagon(MouseEvent event, GraphicsContext gc) {
        endX = event.getX();
        endY = event.getY();
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.drawImage(snapshot,0,0);
        drawPentagon(gc, startX, startY, endX, endY);
    }
    private void drawPentagon(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;

        double radiusX = Math.abs(endX - startX) / 2;
        double radiusY = Math.abs(endY - startY) / 2;

        double[] xPoints = new double[5];
        double[] yPoints = new double[5];

        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(90 + i * 72);
            xPoints[i] = centerX + radiusX * Math.cos(angle);
            yPoints[i] = centerY - radiusY * Math.sin(angle);
        }

        gc.setStroke(colorPickerStroke.getValue());
        gc.setLineWidth(sliderSetSize.getValue());
        gc.setFill(colorPickerFill.getValue());
        boolean fill = fillCheckBox.isSelected();
        if (fill) {
            gc.fillPolygon(xPoints, yPoints, 5);
        }
        gc.strokePolygon(xPoints, yPoints, 5);
    }




    public void open(ActionEvent actionEvent) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        FileChooser fileChooser = new FileChooser();//класс работы с диалоговым окном
        fileChooser.setTitle("Выберите изображениe...");//заголовок диалога
//задает фильтр для указанного расшиерения
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Изображение", "*.jpg","*.png"),
                new FileChooser.ExtensionFilter("Изображение", "*.bmp"));

        File loadImageFile = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        if (loadImageFile != null) {
            //Open
            System.out.println("Процесс открытия файла");
            initDraw(gc, loadImageFile);
        }
    }







    private void initDraw(GraphicsContext gc, File file) {

        for (int i = 0; i < model.getPointCount(); i++) {
            //gc.setFill(cp.getValue());
            gc.fillOval(model.getPoint(i).getX(),model.getPoint(i).getY(),model.getPoint(i).getwP() ,model.getPoint(i).gethP());
        }
    }
    //Изменим метод print для рисования линии:
    public void print(MouseEvent mouseEvent) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (isDrawingLine) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // Сохраняем начальные координаты
                startX = mouseEvent.getX();
                startY = mouseEvent.getY();
            } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                // Очистить холст и перерисовать все точки
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                update(model); // Перерисовать точки
                // Цвет и толщина линии
                gc.setStroke(cp.getValue());
                gc.setLineWidth(sl.getValue());
                // Рисовать линию от начальной точки до текущей позиции мыши
                gc.strokeLine(startX, startY, mouseEvent.getX(), mouseEvent.getY());
            } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                // Завершить рисование линии
                gc.setStroke(cp.getValue());
                gc.setLineWidth(sl.getValue());
                gc.strokeLine(startX, startY, mouseEvent.getX(), mouseEvent.getY());
            }
        } else {
            // Логика для рисования точек
            Points points = new Points((int) mouseEvent.getX(), (int) mouseEvent.getY());
            points.setSizePoint(sl.getValue(), sl.getValue());
            model.addPoint(points);
            update(model);
        }
  // ====
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();


        bgImage = new Image(file.toURI().toString());
        gc.drawImage(bgImage,0,0);
    }


    public void save(ActionEvent actionEvent) throws IOException {

        WritableImage wim=new WritableImage((int)canvas.getWidth(),(int)canvas.getHeight());
        SnapshotParameters spa= new SnapshotParameters();
        canvas.snapshot(null,wim);
        File file=new File("Результат.png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
  
    @FXML
    public void toggleLine(ActionEvent event) {
        isDrawingLine = lineToggle.isSelected();
    }
}

