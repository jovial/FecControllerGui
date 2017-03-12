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
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.beans.property.ReadOnlyObjectProperty
import scalafx.geometry.Bounds

object HelloStageDemo extends JFXApp {

  val power = new StringProperty() {
    value = "0"
  }

  val speed = new StringProperty() {
    value = "10"
  }

  val hr = new StringProperty() {
    value = "75"
  }

  val cadence = new StringProperty() {
    value = "75"
  }

  val distance = new StringProperty() {
    value = "0.00"
  }

  val gearRatio = new StringProperty() {
    value = "4.00"
  }

  val powerLabel = "Power/w"
  val speedLabel = "Speed/kmh"
  val heartRateLabel = "Heart rate/bpm"
  val cadenceLabel = "Cadence/rpm"
  val distanceLabel = "Distance/km"
  val gearRatioLabel = "Gear ratio"

  val telemetryProps = Array((powerLabel, power), (speedLabel, speed), (heartRateLabel, hr),
    (cadenceLabel, cadence), (distanceLabel, distance), (gearRatioLabel, gearRatio));

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

        new Alert(AlertType.Error) {
          title = "Error"
          contentText = getErrorTxt(inputField)
        }.showAndWait()
      }

    }

    val inputLabel = new Label {
      text <== labelTxt
      labelFor = inputField
    }

    val inputPair = new VBox {
      //margin = Insets(10)
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

    val telemetryGrid = new TilePane {
      tileAlignment = Pos.CenterLeft
      padding = Insets(20)
      hgap = 10
    }

    var cells = List[Node]()

    val theWidth = new DoubleProperty {
      value = 1
    }

    var maxLength = 0;
    var maxPair: VBox = null

    val scaleFactor = new DoubleProperty

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
        padding = Insets(20)
      }

      if (label.text.length.get > maxLength) {
        maxLength = label.text.length.get
        maxPair = labelDataPair
      }

      labelDataPair.boundsInLocal.onChange {
        (_, o, n) =>
          {
            val a = n.getWidth
            labelDataPair.scaleX <== scaleFactor
            labelDataPair.scaleY <== scaleFactor
          }
      }

      // second group to take into account scaled bounds
      val gridCell = new Group {
        children = labelDataPair
      }

      cells = gridCell :: cells
      telemetryGrid.children += gridCell
    }

    maxPair.boundsInLocal.onChange {
      (_, o, n) =>
        {
          val a = n.getWidth
          scaleFactor <== theWidth / (a / 0.98)
        }
    }

    val scroll = new ScrollPane() {
      content = telemetryGrid
      fitToWidth = true
      fitToHeight = true
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

          val actual = (size - 40 - 10 * cols) / cols

          theWidth.value = actual

        }
    }

    scroll.height.onChange(
      (_, oldVal, newVal) => {
        val size = newVal.doubleValue();
        power.value = size.toString()

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
