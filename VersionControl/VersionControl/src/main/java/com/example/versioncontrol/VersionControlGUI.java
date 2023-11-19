package com.example.versioncontrol;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;
import java.util.function.Supplier;


public class VersionControlGUI extends Application {
    private String author;
    private String sourcepath;
    private Scene scene;
    private Canvas canvas;
    //private TextField messager;
    private NodeTree.Node selected, hovering, prevSel;
    NodeTree tree = new NodeTree(this);

    public int offsetX, offsetY, originX, originY, startX, startY;
    private boolean dragging = false;
    private static final int cx = 700, cy = 500;
    private GraphicsContext gc;
    private TextArea info;

    public static final double[] hx = new double[6], hy = new double[6];
    private static final double sqrt3 = Math.sqrt(3);
    private static final int stem = NodeTree.r + NodeTree.bw + 15;

    public void draw(){
        gc.setFill(Color.color(0.25, 0.25, 0.25, 1));
        gc.fillRect(0, 0, cx, cy);
        if(tree.root == null) return;//no drawing if no root
        gc.setLineWidth(8);
        tree.visitAll(tree.root, node -> {
            if (node.parent != null) {
                gc.setStroke(Color.color(0.15, 0.15, 0.15, 1));
                final int[] coords = new int[]{node.parent.animX, node.parent.animY, node.animX, node.animY};
                final double slope = 1.0 * (coords[1] - coords[3])/(coords[0] - coords[2]);
                if (slope == 0 || Math.abs(slope*slope - 3) < 1E-3) {//if 60º or 0º straight
                    gc.strokeLine(coords[0] + offsetX, coords[1] + offsetY, coords[2] + offsetX, coords[3] + offsetY);
                } else {//else we make a three segment connection
                    final double[] xyArr = new double[]{
                            node.parent.animX + stem, node.parent.animY,
                            node.parent.animX + stem / 2.0, node.parent.animY + stem * sqrt3 / 2,
                            node.parent.animX + stem / 2.0, node.parent.animY - stem * sqrt3 / 2
                    };
                    int minIndex = 0;
                    final double[] dists = new double[3];
                    for(int i = 0; i < 3; i++) dists[i] = Math.pow(xyArr[i*2] - node.animX - stem,2) + Math.pow(xyArr[1+i*2] - node.animY,2);
                    for(int i = 1; i < 3; i++) if (dists[i] < dists[minIndex]) minIndex = i;//horizontal reverse stem
                    gc.strokeLine(node.animX - stem + offsetX, node.animY + offsetY, node.animX + offsetX, node.animY + offsetY);
                    if(minIndex == 0){
                        gc.strokeLine(node.parent.animX + offsetX, node.parent.animY + offsetY, node.parent.animX + stem + offsetX, node.parent.animY + offsetY);
                        gc.strokeLine(node.parent.animX + stem + offsetX, node.parent.animY + offsetY, node.animX - stem + offsetX, node.animY + offsetY);
                    } else {
                        final boolean direction = node.parent.animY<node.animY;//true is +, - is below
                        gc.strokeLine(node.parent.animX + offsetX, node.parent.animY + offsetY, xyArr[2] + offsetX, direction?xyArr[3]:xyArr[5] + offsetY);
                        gc.strokeLine(xyArr[2] + offsetX, direction?xyArr[3]:xyArr[5] + offsetY, node.animX - stem + offsetX, node.animY + offsetY);
                    }
                }
            }
        });

        final double[] temp1 = new double[6], temp2 = new double[6];
        final int bww = 2;
        tree.visitAll(tree.root, node -> {
            if(node == selected) {//extra hexagon
                gc.setFill(node.equals(tree.root)?Color.LIGHTGRAY:Color.color(250/255.0, 204/255.0, 6/255.0, 1));
                for(int i = 0; i < 6; i++){
                    temp1[i] = hx[i]*(NodeTree.r + NodeTree.bw + bww) + node.animX + offsetX;
                    temp2[i] = hy[i]*(NodeTree.r + NodeTree.bw + bww) + node.animY + offsetY;
                }
                gc.fillPolygon(temp1, temp2, 6);
            }
            if(node == selected || node == hovering) gc.setFill(node.equals(tree.root)?Color.LIGHTGRAY:Color.color(1, 200/255.0, 90/255.0, 1));
            else gc.setFill(node.equals(tree.root)?Color.DARKGRAY:Color.color(240/255.0, 148/255.0, 46/255.0, 1));
            for(int i = 0; i < 6; i++){
                temp1[i] = hx[i]*(NodeTree.r + NodeTree.bw) + node.animX + offsetX;
                temp2[i] = hy[i]*(NodeTree.r + NodeTree.bw) + node.animY + offsetY;
            }
            gc.fillPolygon(temp1, temp2, 6);

            if(node == selected || node == hovering) gc.setFill(node.equals(tree.root)?Color.WHITE:Color.color(1, 1, 140/255.0, 1));
            else gc.setFill(node.equals(tree.root)?Color.LIGHTGRAY:Color.color(250/255.0, 204/255.0, 6/255.0, 1));
            for(int i = 0; i < 6; i++){
                temp1[i] = hx[i]*NodeTree.r + node.animX + offsetX;
                temp2[i] = hy[i]*NodeTree.r + node.animY + offsetY;
            }
            gc.fillPolygon(temp1, temp2, 6);
        });
    }

    public void start(Stage stage){
       for(int i = 0; i < 6; i++){
           hx[i] = Math.sin(Math.PI * i / 3.0);
           hy[i] = Math.cos(Math.PI * i / 3.0);
       }

        BorderPane root = new BorderPane();
        scene = new Scene(root);
        stage.setScene(scene);
        canvas = new Canvas(cx, cy);
        gc = canvas.getGraphicsContext2D();
        offsetX = cx/3;
        offsetY = cy/2;
        startX = cx/3;
        startY = cy/2;
        originX = cx/3;
        originY = cy/2;

        canvas.setOnMouseReleased(e -> {
            startX = offsetX;
            startY = offsetY;
            draw();
        });
        canvas.setOnMousePressed(e -> {
            originX = (int)e.getX();
            originY = (int)e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            if (selected != null && (Math.pow(selected.animX - e.getX(), 2) + Math.pow(selected.animY - e.getY(), 2)) <= (NodeTree.r + NodeTree.bw + 3)*(NodeTree.r + NodeTree.bw + 3)) dragging = true;
            offsetX = startX + (int)e.getX() - originX;
            offsetY = startY + (int)e.getY() - originY;
            draw();
        });
        HBox hbox = new HBox();
        //VBox sidebar = new VBox();
        VBox buttonBox = new VBox();
        VBox bigBox = new VBox();
        HBox fileSelection = new HBox();

        Button updateB = new Button("REFRESH"),
                backupB = new Button("BACKUP DIRECTORY"),
                restoreB = new Button("RESTORE FROM BACKUP"),
                commitB = new Button("COMMIT"),
                downloadB = new Button("DOWNLOAD"),
                openB = new Button("OPEN"),
                transposeB = new Button("TRANSPOSE"),
                deleteB = new Button("DELETE");//TODO: incorporate these two
        Button[] buttons = new Button[]{updateB, backupB, restoreB, commitB, downloadB, transposeB, deleteB, openB};
        for(Button button : buttons) {
            HBox.setHgrow(button, Priority.ALWAYS);
            button.setMaxWidth(Double.POSITIVE_INFINITY);
            button.setFocusTraversable(false);
        }
        openB.setOnAction(e -> open());
        updateB.setOnAction(e -> refresh(true));
        backupB.setOnAction(e -> backup());
        restoreB.setOnAction(e -> restore());
        commitB.setOnAction(e -> commit());
        downloadB.setOnAction(e -> download());

        Label label = new Label("  Working Directory:   ");
        Button plus = new Button("ADD"), minus = new Button("REMOVE");
        openB.setFocusTraversable(false);
        plus.setFocusTraversable(false);
        minus.setFocusTraversable(false);
        label.setFocusTraversable(false);
        ComboBox<String> comboBox = new ComboBox<>();
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        comboBox.setMaxWidth(Double.POSITIVE_INFINITY);
        comboBox.setFocusTraversable(false);

        comboBox.setOnAction(e -> {
            if(comboBox.getSelectionModel().isEmpty()) return;
            //if not empty
            backupB.setDisable(false);
            restoreB.setDisable(false);
            if(selected != null) {
                commitB.setDisable(false);
                downloadB.setDisable(false);
            }
            transposeB.setDisable(false);
            //deleteB.setDisable(false);
            minus.setDisable(false);
            openB.setDisable(false);
            sourcepath = comboBox.getSelectionModel().getSelectedItem();
        });
        plus.setOnAction(e -> {
            Stage TEMP = new Stage();
            DirectoryChooser myPicker = new DirectoryChooser();
            myPicker.setTitle("Select your directory to be added.");
            File temp = myPicker.showDialog(TEMP);
            if(temp == null) return;
            for(String item : comboBox.getItems()) if(temp.toString().equals(item)) return;
            try {
                FileWriter writer = new FileWriter(homedir + "\\info.txt", true);
                writer.write(temp + "\n");
                writer.flush();
                writer.close();
            } catch (IOException E) {
                System.out.println("Critical error!");
            }
            comboBox.getItems().add(temp.toString());
            TEMP.close();
        });
        minus.setOnAction(e -> {
            if(!comboBox.getItems().isEmpty()) {
                comboBox.getItems().remove(comboBox.getSelectionModel().getSelectedItem());
                backupB.setDisable(true);
                restoreB.setDisable(true);
                commitB.setDisable(true);
                downloadB.setDisable(true);
                transposeB.setDisable(true);
                //deleteB.setDisable(true);
                minus.setDisable(true);
                openB.setDisable(true);
                try {
                    BufferedReader tempReader = new BufferedReader(new FileReader(homedir + "\\info.txt"));
                    String temporary = tempReader.readLine() + "\n";
                    tempReader.close();
                    FileWriter writer = new FileWriter(homedir + "\\info.txt");
                    writer.write(temporary);
                    for (String item : comboBox.getItems()) writer.write(item + "\n");
                    writer.flush();
                    writer.close();
                } catch (IOException E) {
                    throw new RuntimeException();
                }
                comboBox.getSelectionModel().clearSelection();
            }
        });

        bigBox.setStyle("-fx-border-color: #303030 ; -fx-border-insets:0; -fx-border-radius:0; -fx-border-width: 10.0 10.0 10.0 10.0");
        bigBox.setAlignment(Pos.CENTER);
        info = new TextArea("Click REFRESH to load an up to date node-map!");//hoist it up here
        info.setStyle("-fx-border-color: #404040 ; -fx-border-insets:0; -fx-border-radius:0; -fx-border-width: 5.0 6.0 5.0 5.0");
        info.setEditable(false);
        info.setFocusTraversable(false);
        //sidebar.setStyle("-fx-border-width: 0 0 0 0; -fx-background-color: #282828");
        //String stylepath;
        //if(isWindows) stylepath = "file:" + homedir + "\\textarea.css";//\\src\\main\\scripts\\
        //else stylepath = "file:" + homedir + "/textarea.css";///src/main/script/
        //System.out.println(homedir + "\\textarea.css");
        info.getStylesheets().add(//"file:" + homedir + "\\textarea.css");//works
                "file:/C:/Users/vince/Desktop/VersionControl/VersionControl/src/main/scripts/textarea.css");
        root.setStyle("-fx-background-color: #404040; -fx-box-border: #404040; -fx-border-width: 0;");
        hbox.setStyle("-fx-background-color: #404040; -fx-box-border: #404040; -fx-border-width: 0;");
        label.setStyle("-fx-background-color: #FFFFFF; -fx-box-border: #FFFFFF; -fx-border-width: 0;");
        label.backgroundProperty().setValue(new Background(new BackgroundFill(Color.color(0.8, 0.8, 0.8, 1), CornerRadii.EMPTY, Insets.EMPTY)));
        //sidebar.getStylesheets().add(stylepath);
        //        scene.getStylesheets().add(getClass().getResource("style.css").toString());//works
        //info.setPrefColumnCount(70);
        //VBox.setVgrow(info, Priority.ALWAYS);
        HBox.setHgrow(info, Priority.ALWAYS);
        hbox.setMaxWidth(canvas.getWidth() - buttonBox.getWidth());
        //info.setMaxHeight(buttonBox.getHeight());
        //info.minHeight(buttonBox.getHeight());
        //info.setMaxWidth(Double.POSITIVE_INFINITY);
        info.setPrefRowCount(5);
        info.setPrefColumnCount(32);
        info.setWrapText(true);
        info.setFocusTraversable(false);

        minus.setDisable(true);
        backupB.setDisable(true);
        restoreB.setDisable(true);
        commitB.setDisable(true);
        downloadB.setDisable(true);
        transposeB.setDisable(true);
        deleteB.setDisable(true);
        openB.setDisable(true);

        fileSelection.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-weight: bold");
        VBox.setVgrow(label, Priority.ALWAYS);
        label.setMaxHeight(Double.POSITIVE_INFINITY);

        fileSelection.getChildren().addAll(label, comboBox, plus, minus);

        buttonBox.getChildren().addAll(updateB, backupB, restoreB, openB, commitB, downloadB);

        hbox.getChildren().addAll(buttonBox, info);
        bigBox.getChildren().addAll(fileSelection, hbox, canvas);
        root.setCenter(bigBox);

        stage.setResizable(false);
        stage.show();

        canvas.setOnMouseClicked(e -> {
            //System.out.println("click!");
            hovering = null;
            if(tree.root == null) return;
            else if(dragging) {
                dragging = false;
                return;
            }
            NodeTree.Node old = selected;
            //System.out.println(e.getX() + ", " + e.getY() + "  Threshold " + Math.pow(NodeTree.r+NodeTree.bw, 2));
            tree.visitAll(tree.root, node -> {
                //System.out.print("\t(" + (node.animX + offsetX) + ", " + (node.animY + offsetY) + ") ");
                //System.out.println("Distance of " + (Math.pow(e.getX() - node.animX - offsetX, 2) + Math.pow(e.getY() - node.animY - offsetY, 2)));
                if(Math.pow(NodeTree.r+NodeTree.bw, 2) >= Math.pow(e.getX() - node.animX - offsetX, 2) + Math.pow(e.getY() - node.animY - offsetY, 2))
                    selected = node;
            });
            //System.out.println(selected);
            if(selected == old || comboBox.getSelectionModel().isEmpty()) {
                selected = null;
                commitB.setDisable(true);
                downloadB.setDisable(true);
            } else {
                prevSel = selected;
                commitB.setDisable(false);
                downloadB.setDisable(false);
            }
            updateText();
            draw();
        });
        canvas.setOnMouseMoved(e -> {
            if(tree.root == null || selected != null) return;
            hovering = null;
            tree.visitAll(tree.root, node -> {
                if((NodeTree.r+NodeTree.bw)*(NodeTree.r+NodeTree.bw) >= (e.getX() - node.animX - offsetX)*(e.getX() - node.animX - offsetX) + (e.getY() - node.animY - offsetY)*(e.getY() - node.animY - offsetY))
                    hovering = node;
            });
            if(hovering == prevSel) hovering = null;
            else prevSel = null;
            updateText();
            draw();
        });

        //get author
        draw();
        initAuthorPath(comboBox);
        //timer.start();
    }


    //TODO: FILL FUNCTIONS
    //TODO: POPUP IF VAL IS FALSE
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().trim().startsWith("window");
    public static String pythonCommand = isWindows?"python":"python3";
    public void refresh(boolean fetch){
        File temp = new File(homedir + "\\request.txt");
        try{
            temp.createNewFile();
        }catch (IOException ignored){};

        if (fetch) runWillTerminate(pythonCommand, "send.py", homedir + "\\request.txt", "q");
        //python script queries host and gets back a manifest.txt file, each line is the description of a node.
        //final String path = isWindows?"src\\main\\scripts\\manifest.txt":"src/main/scripts/manifest.txt";
        final String path = homedir + "\\manifest.txt";
        try {
            BufferedReader myReader = new BufferedReader(new FileReader(path));
            tree = new NodeTree(this);
            tree.addRoot(new NodeTree.Node(null, "root", "superuser", "17/11/2023", "ROOT"));


            String next = myReader.readLine();
            String[] args;
            while(next != null){//ID parentID author date commitMessage
                args = next.split(" ", 5);
                tree.blindPush(args[0], args[1], args[2], args[3], args[4]);
                next = myReader.readLine();
            }
            tree.genCoords();
            //tree.printTree();

            //tree.updateWidth();
            //messager.setText("Successfully loaded nodemap!");
            myReader.close();
        } catch (IOException e){
            //messager.setText("Critical error, " + e.getMessage());
        }
        if(selected != null) selected = tree.getNodeFromID(selected.ID, tree.root);
        updateText();
        draw();
    }

    public void backup(){
        //calls python script that moves all files into scripts/backup after FIRST DELETING all files in scripts/backup
        runWillTerminate(pythonCommand, "clear_folder_(file).py", homedir + "\\backup");
        runWillTerminate(pythonCommand, "folder_copy_(file).py", sourcepath, homedir + "\\backup");
    }
    public void restore(){
        //calls python script that copies all scripts/backup items to project location
        //calls python script that DELETES all files in sourcepath before copying over all backup there!!
        runWillTerminate(pythonCommand, "clear_folder_(file).py", sourcepath);
        runWillTerminate(pythonCommand, "folder_copy_(file).py", homedir + "\\backup", sourcepath);
    }

    public void open(){
        //calls python script to open folder with solidworks
        runWillTerminate(pythonCommand, "open_sldprt_(file).py" ,sourcepath);
    }

    public void commit(){
        //USES 'selected' to determine where to branch off of!!
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().setMinWidth(300);
        dialog.setHeaderText("Enter a commit message!");
        dialog.setResizable(false);
        dialog.setTitle("");
        Optional<String> message = dialog.showAndWait();
        if(message.isEmpty() || message.get().trim().isEmpty()) {
            warning("You must enter a commit message!");
            return;
        }

        runWillTerminate(pythonCommand, "Prepare_for_commit_(file).py", sourcepath, homedir + "\\staging", selected.ID, author, message.get());
        //calls commit.py. Host then adds contents of scripts/staging to host.
        File staging = new File(homedir + "\\staging");
        String target = null;
        for(File file : staging.listFiles()) if(!file.getName().equals("update.txt")) target = file.getName();
        if(target == null) throw new RuntimeException("Vincent screwed up, check the prepcommit py script-no hash.zip created");
        System.out.println(target);

        runWillTerminate(pythonCommand, "send.py", homedir + "\\staging\\" + target, "q");
        //sends hash.zip then update.txt
        runWillTerminate(pythonCommand, "clear_folder_(file).py", homedir + "\\staging");
        refresh(false);
    }

    public void download(){//TODO: not always force backup?
        backup();
        //uses attribute 'selected'.ID and submits it as an argument to download.py
        //host sends back node (.zip and .txt) to scripts/staging
        File tbc = new File(homedir + "\\" + selected.ID + ".txt");
        try{
            tbc.createNewFile();
        } catch (IOException e){
            throw new RuntimeException("VINCENT WHYYY");
        }
        //System.out.println(tbc.getName());
        runWillTerminate(pythonCommand, "send.py", homedir +tbc.getName(), "q");
        runWillTerminate(pythonCommand, "Unpack_Node_(file).py", homedir + "\\staging", sourcepath);
    }

    public void warning(String input){
        Stage temp = new Stage();
        temp.setResizable(false);
        BorderPane temper = new BorderPane();
        Scene TEMP = new Scene(temper);
        temp.setScene(TEMP);
        String[] total = input.trim().split("\n");
        String tbc = "";
        for(int i = 0; i < total.length; i++) tbc += "   " + total[i] + "   \n";
        Label tempiest = new Label(tbc);
        tempiest.setFocusTraversable(false);
        tempiest.setStyle("-fx-text-fill: red");
        temper.setCenter(tempiest);
        temp.show();
    }

    /*public void transpose(NodeTree.Node origin, NodeTree.Node destination, String fileName){
        selected = origin;
        download(false);
        //extractTargetFile.py fileName to scripts/hotel
        //clear scripts/staging
        selected = destination;
        download(false);
        //move + overwrite anything in way, fileName from scripts/hotel to scripts.staging
        //clear scripts/hotel
        //move everything from scripts/staging to scripts/hotel
        //clear scripts/staging
        commit();
    }*/

    public void initAuthorPath(ComboBox<String> comboBox){
        try {//initialise sourcepath and author
            BufferedReader myReader = new BufferedReader(new FileReader(homedir + "\\info.txt"));
            String temp = myReader.readLine();
            if(temp == null){
                TextInputDialog dialog = new TextInputDialog();
                dialog.getDialogPane().setMinWidth(300);
                dialog.setHeaderText("Please choose a name!");
                dialog.setResizable(false);
                dialog.setTitle("");
                Button close = ((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL));
                close.setDefaultButton(false);
                close.setOnAction(e -> System.exit(1));
                //dialog.getDialogPane().lookupButton(ButtonType.OK).setOnMouseClicked(e -> {});
                Optional<String> value = dialog.showAndWait();

                value.ifPresentOrElse(e -> {
                    String VALUE = value.get().trim();
                    if(VALUE.isEmpty()) System.exit(1);
                    try {
                        //System.out.println(System.getProperty("user.dir"));
                        FileWriter writer = new FileWriter(homedir + "\\info.txt");
                        //System.out.println("\"VALUE\n\"");
                        writer.write(VALUE + "\n");
                        writer.flush();
                        writer.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }, () -> System.exit(1));
                return;
            }
            author = temp;//else there is a first line, it's the author!
            temp = myReader.readLine();
            if(temp != null) {
                sourcepath = temp;
                comboBox.getItems().add(temp);
                comboBox.getSelectionModel().select(0);
                temp = myReader.readLine();
                while(temp != null){
                    comboBox.getItems().add(temp);
                    temp = myReader.readLine();
                }
            }
            myReader.close();
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
    public static void main(String[] args) {
        if(1 > 2) launch();
        runWillTerminate(pythonCommand, "send.py", homedir + "\\staging\\request.txt", "q");
        //boolean val = runPython("python3", "folder_copy_(file).py", "hotel", "backup");
    }

    private void updateText(){
        if(hovering == null && selected == null) {
            info.setText("");
            return;
        }
        NodeTree.Node node = hovering;
        if(selected != null) node = selected;
        info.setText(node.commitMessage + "\n\n" + node.author + "\n" + node.date);
    }

    private static final String homedir = "C:\\Users\\vince\\Desktop\\VersionControl\\VersionControl\\src\\main\\scripts";//System.getProperty("user.dir") + (isWindows?"\\src\\main\\scripts":"/src/main/scripts");
    /*public static boolean runPython(String... command){
        if(timer.occupied()) return false;
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new File(homedir));
        try{
            Process process0 = processBuilder.start();
            timer.giveProcess(process0);
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }*/

    public static void runWillTerminate(String... command){
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new File(homedir));
        try {
            Process process0 = processBuilder.start();
            process0.waitFor();
            System.out.println("Exited process");
        } catch (IOException e){
            System.out.println("Failure to run " + command[0]);
        } catch (InterruptedException ignored){
            throw new RuntimeException("Vincent, this must be your fault again!");
        }
    }
    /*private static final newTimer timer = new newTimer(){
        private long programStart = -10000;
        private Supplier<Boolean> next = null;
        private static final int interval = 1000;//1000 millis
        private Process held = null;
        public void handle(long nanos){
            if(held != null && System.currentTimeMillis() - programStart > interval){
                held.destroy();
                held = null;
                if(next != null) {
                    System.out.println("Running second process! " + next);
                    next.get();
                    next = null;
                }
            }
        }
        public void giveProcess(Process input){
            this.held = input;
            this.programStart = System.currentTimeMillis();
        }
        public boolean occupied(){
            return held != null;
        }

        public void nextOnFinish(Supplier<Boolean> supplied) {
            next = supplied;
        }
    };
    private abstract static class newTimer extends AnimationTimer {
        private Supplier<Boolean> next;
        public abstract void giveProcess(Process input);
        public abstract boolean occupied();

        public abstract void nextOnFinish(Supplier<Boolean> supplied);
    }

    @Override
    public void stop() throws Exception {
        timer.stop();
        super.stop();
    }*/
}
