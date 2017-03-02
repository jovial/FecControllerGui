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
import scalafx.beans.property.IntegerProperty
import scalafx.scene.control.ScrollPane

object HelloStageDemo extends JFXApp {

  val power = new StringProperty() {
    value = "0"
  }

  val powerLabel = "Power/w"

  val telemetryProps = Array((powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power));

  def genSplit: Parent = {

    val telemetryGrid = mkTelemetryGrid()

    val rightSide = new GridPane {
      children = Seq(
        new Button {
          text = "on the right"
        },
        new Button {
          text = "on the right"
          margin = Insets(10)
        })

    }

    val split = new SplitPane {
      padding = Insets(20)
      dividerPositions_=(0.80, 0.20)
      items ++= Seq(telemetryGrid, rightSide);
    }

    split
  }

  val columnN = new IntegerProperty() {
    value = 3
  }

  def mkTelemetryGrid(): Node = {

    val telemetryGrid = new TilePane() {
      prefColumns <== columnN
    }

    var cells = List[VBox]()

    for (a <- 0 until telemetryProps.length) {

      val label = new Label {
        text = telemetryProps(a)._1
      }

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
      }

      val gridCell = new VBox {
        margin = Insets(10)
        children = Seq(g)
        alignment = Pos.Center
      }

      cells = gridCell :: cells
      telemetryGrid.children += gridCell
    }

    val scroll = new ScrollPane() {
      content = telemetryGrid
      fitToWidth = true
      fitToHeight = true
    }

    scroll.height.onChange(
      (_, oldVal, newVal) => {
        val size = newVal.doubleValue();
        power.value = size.toString()
        val scale = size / 420 * 1.0;
        for (x <- cells.flatMap(_.children)) {
          x.setScaleX(scale)
          x.setScaleY(scale)
        }
        // tile size is determined by largest prefWidth/Height
        for (x <- cells) {
          for (y <- x.children) {
            val b = y.boundsInParent.value
            x.setPrefSize(b.getWidth, b.getHeight)
          }

          x.margin = Insets(20 * scale)
        }

      })

    scroll

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
