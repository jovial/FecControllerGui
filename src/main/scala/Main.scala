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

object HelloStageDemo extends JFXApp {

  val test = StringProperty("boo")
  
  def genSplit: Parent = {
    
      val test = new TextArea {
        text = "hello mate hello matehello matehello matehello matehello matehello matehello matehello matehello matehello matehello matehello matehello matehello mate"
      }

    val leftSide = new BorderPane {
      center = test
    }
   
    
    val rightSide = new GridPane {
      children = Seq(
          new Button {
            text = "on the right"
          })
    }
    
    GridPane.setConstraints(leftSide, 0, 0)
    GridPane.setConstraints(rightSide, 1, 0)
    
    val res = new GridPane {
      
      padding = Insets(18)
      val constraintsColumnOne = new ColumnConstraints {percentWidth = 25}
      val constraintsColumnZero = new ColumnConstraints {percentWidth = 75}
      val rowConstr = new RowConstraints {percentHeight = 100}
      
      columnConstraints += constraintsColumnZero
      columnConstraints += constraintsColumnOne
      rowConstraints += rowConstr
      
      children = Seq(leftSide, rightSide)
    }
    
    val split = new SplitPane {
            padding = Insets(20)
            dividerPositions_=(0.75, 0.25)
            items ++= Seq(leftSide, rightSide);
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
