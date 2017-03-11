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
import scalafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.scene.control.Dialog
import scalafx.stage.StageStyle
import scalafx.scene.control.ButtonType
import scalafx.beans.property.ReadOnlyProperty
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment
import scalafx.beans.property.BooleanProperty

object HelloStageDemo extends JFXApp {

  val power = new StringProperty() {
    value = "0"
  }

  val powerLabel = "Power/w"

  val telemetryProps = Array((powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power), (powerLabel, power));

  def genSplit: Parent = {

    val telemetryGrid = mkTelemetryGrid()

    val powerInput = mkInput(
      StringProperty("Power"),
      StringProperty("Set power (w)"),
      validateNumber,
      getInvalidNumTxt,
      (v) => println(v))

    val hrInput = mkInput(
      StringProperty("Heart rate"),
      StringProperty("Set heart rate (bpm)"),
      validateNumber,
      getInvalidNumTxt,
      (v) => println(v))

    val rightSide = new TilePane {
      margin = Insets(10)
      prefColumns = 1
      children = Seq(powerInput, hrInput)
    }

    val split = new SplitPane {
      padding = Insets(20)
      dividerPositions_=(0.8)
      items ++= Seq(telemetryGrid, rightSide);
    }

    SplitPane.setResizableWithParent(rightSide, false)

    val border = new BorderPane

    val statusLight = new Rectangle {
      height = 30
      fill = Color.Green
      width <== 30
    }

    val statusText = new Text {
      text = "hello"
    }

    val statusPane = new BorderPane {
      center = statusText
      hgrow = Priority.Always
      style = "-fx-background-color: white"
    }

    val status = new HBox {
      children = Seq(statusLight, statusPane)
      style = "-fx-border-width: 1; -fx-border-style: solid;"
    }

    border.center = telemetryGrid
    border.right = rightSide
    border.bottom = status

    border
  }

  def mkInput(labelTxt: StringProperty, promptTxt: StringProperty, validate: (scalafx.scene.control.TextField) => Boolean,
              getErrorTxt: (scalafx.scene.control.TextField) => String, onAccept: (String) => Unit) = {
    val inputField = new TextField() {
      promptText <== promptTxt
    }

    // validate an integer
    inputField.focused.onChange {
      ((
        s, o, n) => if (!n && !validate(inputField))
        // unfocused
        inputField.text = "")
    }

    inputField.onAction = (ae: ActionEvent) => {
      if (validate(inputField)) {
        onAccept(inputField.text.value)
        inputField.text = ""
      } else {
        val diag = new Dialog {
          title = "Error"
          contentText = getErrorTxt(inputField)
        }
        diag.initStyle(StageStyle.Utility)
        diag.dialogPane.value.getButtonTypes.add(ButtonType.OK);
        diag.showAndWait()
      }

    }

    val inputLabel = new Label {
      text <== labelTxt
      labelFor = inputField
    }

    val inputPair = new VBox {
      // margin = Insets(10)
      padding = Insets(5)
      children = Seq(inputLabel, inputField)
    }
    inputPair
  }

  def getInvalidNumTxt(field: scalafx.scene.control.TextField) = {
    field.text.value + " is invalid. You must enter a valid number"
  }

  def validateNumber(field: scalafx.scene.control.TextField) = field.text.value.matches("[0-9]+")

  def mkTelemetryGrid(): Node = {

    val telemetryGrid = new TilePane() {
      prefColumns = 3
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
    
    
    val theWidth = new DoubleProperty {
      value = 1
    }
    
    scroll.width.onChange {
      (_, old, newV) =>
        {
          val size = newV.doubleValue();
          var cols = 1
          if (size >= 600) {
            cols = 5
          } else if (size >= 380) {
            cols = 3
          } else if (size >= 200) {
            cols = 2
          }
           
          
        val actual = (size - 0.06 * size * cols) / cols
        
        theWidth.value = actual
        
        // should probably dynamically scale this
        for (x <- cells) {
          x.margin = Insets(0.03 * size)
        }

        }
    }
        for (x <- cells.flatMap(_.children)) {
          x.boundsInLocal.onChange {
            (_,o,n) => {
              val a = n.getWidth
              x.scaleX <== {theWidth / (a / 0.98)}
              x.scaleY <== {theWidth / (a / 0.98)}
            }
          }
          
        }     
    

          for (x <- cells) {
            for (y <- x.children) {
     
              y.boundsInParent.onChange {
                (_,o,n) => x.setPrefSize(n.getWidth, n.getHeight)
              }
            }

          }    
    
    
    scroll.height.onChange(
      (_, oldVal, newVal) => {
        val size = newVal.doubleValue();
        power.value = size.toString()
        val scale = size / 420 * 1.0;
        val actual = Math.max(scale, 1.0)
        for (x <- cells.flatMap(_.children)) {
          //x.setScaleX(actual)
          //x.setScaleY(actual)
        }
        // tile size is determined by largest prefWidth/Height
        for (x <- cells) {
          for (y <- x.children) {
            val b = y.boundsInParent.value
            //x.setPrefSize(b.getWidth, b.getHeight)
          }

          //x.margin = Insets(20 * actual)
        }

      })

    scroll

  }

  val split = genSplit

  val theScene = new Scene {
    fill = LightGreen
    root = split
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Sim"
    width = 600
    height = 450
    scene = theScene
  }
}
