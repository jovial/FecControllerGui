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

object HelloStageDemo extends JFXApp {

  val test = StringProperty("boo")

  def genSplit: Parent = {

    val leftSide = new TilePane {
      padding = Insets(5)
      vgap = 5
      hgap = 5
      prefColumns = 3
    }
    
     val test = new GridPane() {
       padding = Insets(5)
       hgrow = Priority.Always
       vgrow = Priority.Always
       style = "-fx-background-color: red"
     }
     
   val columnConstraints = new ColumnConstraints() {
       hgrow = Priority.Always;
   }
   
   val nColumns = 3;
   
   for (a <- 1 to nColumns) {
     test.columnConstraints += columnConstraints
   }
     
    

    for (a <- 1 to 10) {

//      val test = new VBox {
//        hgrow = Priority.Always
//        padding = Insets(20)
//        spacing = 5
//      }

      
     val label = new Text("Power/w") {
          //  style = "-fx-font-size: 40%"
      }
       
     val value = new Text("300") {
        //    style = "-fx-font-size: 100%"
      }
     
     val vbox = new VBox{
         hgrow = Priority.Always
         vgrow = Priority.Always
         children = Seq(label, value)
         maxHeight = 1000
         alignment = Pos.Center
     }
     
     
     vbox.height.onChange {
       (s,o,n) => {
         label.font = new Font(n.doubleValue() * 0.1)
         value.font = new Font(n.doubleValue() * 0.2)
       }
     }

     GridPane.setRowIndex(vbox, (a-1) / nColumns)
     GridPane.setColumnIndex(vbox, (a-1) % nColumns)

    
    test.children += vbox
    }

    val rightSide = new GridPane {
      children = Seq(
        new Button {
          text = "on the right"
        })
    }

    val split = new SplitPane {
      padding = Insets(20)
      dividerPositions_=(0.80, 0.20)
      items ++= Seq(test, rightSide);
    }

    split
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Sim"
    width = 600
    height = 450
    scene = new Scene {
      fill = LightGreen
      root = genSplit
    }
  }
}
