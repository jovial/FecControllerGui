/**
 * Created by fluxoid on 17/02/17.
 */

import org.cowboycoders.ant.profiles.simulators._

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.SplitPane
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.HBox
import scalafx.scene.text.Text;
import scalafx.scene.Node
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.FlowPane
import scalafx.scene.control.TextArea
import scalafx.scene.layout.ColumnConstraints
import scalafx.scene.Parent
import scalafx.geometry.Insets
import scalafx.scene.layout.RowConstraints
import scalafx.scene.layout.Priority
import scalafx.scene.layout.TilePane
import scalafx.scene.control.TextField
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font
import javafx.beans.value.ChangeListener
import scalafx.scene.layout.AnchorPane
import scalafx.animation.Timeline
import scalafx.animation.KeyFrame
import javafx.util.Duration
import scalafx.geometry.Pos
import scalafx.scene.layout.Region
import scalafx.scene.control.Label
import scalafx.beans.property.DoubleProperty
import scalafx.scene.Group



object HelloStageDemo extends JFXApp {
  
  val power = new StringProperty(){
    value = "0"
  }
  
  val powerLabel = "Power/w"
  
  val telemetryProps = Array((powerLabel, power),(powerLabel, power),(powerLabel, power),(powerLabel, power),(powerLabel, power),(powerLabel, power));


  def genSplit: Parent = {

    val telemetryGrid = mkTelemetryGrid()

    val rightSide = new GridPane {
      children = Seq(
        new Button {
          text = "on the right"
        })
    }

    val split = new SplitPane {
      padding = Insets(20)
      dividerPositions_=(0.80, 0.20)
      items ++= Seq(telemetryGrid, rightSide);
    }
    
    split
  }
  

  def mkTelemetryGrid() = {
    val telemetryGrid = new GridPane() {
      padding = Insets(10)
      style = "-fx-background-color: red"
    }
      
    val columnConstraints = new ColumnConstraints() {
        hgrow = Priority.Always;
    }
    
    val nColumns = 3;
    
    for (a <- 1 to nColumns) {
      telemetryGrid.columnConstraints += columnConstraints
   }
    

    var cells = List[Node]()

    for (a <- 0 until telemetryProps.length) {

      
     val label = new Label {
            text = telemetryProps(a)._1
      }
     
     //label.setScaleX(10);
     //label.setScaleY(10);
     
       
     val value = new Text {
        text <== telemetryProps(a)._2
        style = "-fx-font-size: 200%"
      }
     
     val labelDataPair = new VBox() {
       children = Seq(label, value)
     }
     
     // group so that scaling children updates bounds
     val g = new Group() {
       children = Seq(labelDataPair)
       hgrow = Priority.Always
       vgrow = Priority.Always
     }
     
     val gridCell = new VBox{
         padding = Insets(10)
         hgrow = Priority.Always
         children = Seq(g)
         alignment = Pos.Center
     }
     
     
     telemetryGrid.height.onChange( {
       (_,_,newVal) => {
         val size = newVal.doubleValue();
         //vbox.setStyle("-fx-font-size:" + size)
         power.value = size.toString()
         val scale = size / 400 * 1.0;
         for (x <- g.children) {
           x.setScaleX(scale)
           x.setScaleY(scale)
         }
       }
     })
     
     telemetryGrid.boundsInParent.onChange( {
       (_, o, n) => {
         if (n.getMinX < 0.0) {
           telemetryGrid.requestLayout()
           println(telemetryGrid.boundsInParent.value)
           val len = telemetryGrid.columnConstraints.length
           if (len >= 1) {
           telemetryGrid.columnConstraints = telemetryGrid.getColumnConstraints.drop(1).foldRight(List[ColumnConstraints]())(_ :: _)
           var i = 0;
           for (cell <- cells) {
             GridPane.setRowIndex(cell, (i) / len)
             GridPane.setColumnIndex(cell, (i) % len)
             i += 1
           }
         }
         }
       }
     })
     
     cells ::= gridCell
    

     GridPane.setRowIndex(gridCell, (a) / nColumns)
     GridPane.setColumnIndex(gridCell, (a) % nColumns)

    
    telemetryGrid.children += gridCell
    }
    

    telemetryGrid

  }
  
  val theScene = new Scene {
      fill = LightGreen
      root = genSplit
    }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Sim"
    width = 600
    height = 450
    scene = theScene
  }
  
}
