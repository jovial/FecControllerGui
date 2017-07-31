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
import scalafx.scene.text.Text
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
import org.cowboycoders.ant.interfaces.AntTransceiver
import javafx.event.EventHandler
import javafx.stage.WindowEvent
import java.util.Timer
import java.util.TimerTask

import scalafx.beans.property.ObjectProperty
import scalafx.event.subscriptions.Subscription
import javafx.beans.value.ObservableNumberValue
import javafx.beans.binding.DoubleBinding

import scalafx.beans.binding.Bindings
import scalafx.beans.binding.ObjectBinding
import scalafx.beans.value.ObservableValue
import scalafx.collections.transformation.FilteredBuffer
import java.util.function.Predicate

import scalafx.collections.ObservableBuffer
import org.cowboycoders.ant.profiles.FecProfile
import javafx.scene.text.TextBoundsType

import HelloStageDemo.numCols

case class Cell(label: String, value: StringProperty)
case class GridCellGroup(enabled: Option[BooleanProperty], cells: Array[Cell])

object HelloStageDemo extends JFXApp {

  val turbo = new FecProfile()
  val antInterface = new AntTransceiver(1);
  val antNode = new org.cowboycoders.ant.Node(antInterface);

  val turboThread = new Thread() {
    override def run() = {
      println("Node starting")
      antNode.start()
      antNode.reset()
      turbo.start(antNode)
    }
  }
  turboThread.start()

  val timer = new Timer()

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

  val gradient = new StringProperty
  val coeffRolling = new StringProperty

  val bikeWeight = new StringProperty
  val userWeight = new StringProperty
  val userAge = new StringProperty
  val userHeight = new StringProperty
  val wheelDiameter = new StringProperty
  val windSpeed = new StringProperty
  val windCoeff = new StringProperty
  val draftingFactor = new StringProperty

  val powerLabel = "Power/w"
  val speedLabel = "Speed/kmh"
  val heartRateLabel = "Heart rate/bpm"
  val cadenceLabel = "Cadence/rpm"
  val distanceLabel = "Distance/km"
  val gearRatioLabel = "Gear ratio"
  val bikeWeightLabel = "Bike weight/kg"
  val userWeightLabel = "User weight/kg"
  val userAgeLabel = "User age/years"
  val userHeightLabel = "User height/cm"
  val wheelDiaLabel = "Wheel diameter/m"
  val coeffRollingLabel = "Coeff. rolling r."
  val gradientLabel = "Gradient/%"

  val windSpeedLabel = "Wind speed/m/s"
  val windCoeffLabel = "Wind coeff."
  val draftingFactorLabel = "Drafting factor"

  timer.scheduleAtFixedRate(new TimerTask() {
    override def run = {
      Platform.runLater {
        () =>
          {
//            power.value = turboModel.getPower.toString()
//            // we are doing conversion from m/s to km/h but we might use km/h internally later
//            speed.value = "%2.2f".format(turboModel.getSpeed.doubleValue() * 3.6)
//            hr.value = Option(turboModel.getHeartRate).getOrElse(0).toString()
//            cadence.value = turboModel.getCadence.toString
//            distance.value = "%2.2f".format((turboModel.getDistance / 1000.0))
//            gearRatio.value = "%2.2f".format(turboModel.getGearRatio)
//            bikeWeight.value = "%2.2f".format(turboModel.getBikeWeight)
//            val athlete = turboModel.getAthlete
//            userWeight.value = "%2.2f".format(athlete.getWeight)
//            userHeight.value = "%2.0f".format(athlete.getHeight)
//            userAge.value = "%d".format(athlete.getAge)
//            wheelDiameter.value = "%2.2f".format(turboModel.getWheelDiameter)
//            gradient.value = "%2.2f".format(turboModel.getTrackResistance.getGradient)
//            coeffRolling.value = "%2.2f".format(turboModel.getTrackResistance.getCoefficientRollingResistance)
//            val windResistance = turboModel.getWindResistance
//            windSpeed.value = "%d".format(windResistance.getWindSpeed)
//            draftingFactor.value = "%2.2f".format(windResistance.getDraftingFactor)
//            windCoeff.value = "%2.2f".format(windResistance.getWindResistanceCoefficent)

          }

      }
    }
  }, 2000, 500)

  val simulationCellsEnabled = new BooleanProperty {
    value = true
  }

  val telemetryCellsEnabled = new BooleanProperty {
    value = true
  }
  
  val testGrid = new GridPane() {
    hgap = 10
    vgap = 10
  }
  
  val telemetryCells = Array(
    GridCellGroup(None, Array(Cell(powerLabel, power),
      Cell(speedLabel, speed), Cell(heartRateLabel, hr),
      Cell(cadenceLabel, cadence), Cell(distanceLabel, distance), Cell(gearRatioLabel, gearRatio)))
  )

  val simulationCells =  Array(GridCellGroup(Some(simulationCellsEnabled), Array(
    Cell(bikeWeightLabel, bikeWeight), Cell(userWeightLabel, userWeight), Cell(userAgeLabel, userAge),
    Cell(userHeightLabel, userHeight), Cell(wheelDiaLabel, wheelDiameter), Cell(coeffRollingLabel, coeffRolling),
    Cell(gradientLabel, gradient), Cell(windSpeedLabel, windSpeed), Cell(windCoeffLabel, windCoeff),
    Cell(draftingFactorLabel, draftingFactor))))


  val numCols = new IntegerProperty() {
    value = 1
  }

  def genSplit: Parent = {


    val statsVBox = new VBox();

    statsVBox.vgrow = Priority.Always

    val scroll = new ScrollPane() {
      content = statsVBox
      fitToWidth = true
      fitToHeight = false
    }

    scroll.layoutBounds.onChange {
      (_, old, newV) =>
        {
          val size = newV.width
          var cols = 1
          if (size >= 600) {
            cols = 5
          } else if (size >= 380) {
            cols = 3
          } else if (size >= 200) {
            cols = 2
          }

          val actual = (size - 40 - 10 * cols) / cols

          numCols.value = cols

        }
    }

    def genGridTitle(title: String): Label = {
      val label = new Label(title) {
        style = "-fx-font-size:20"
      }
      label
    }

    statsVBox.children += genDecorated(telemetryCells, scroll.viewportBounds, telemetryCellsEnabled, genGridTitle("Common telemetry"))
    statsVBox.children += genDecorated(simulationCells, scroll.viewportBounds, simulationCellsEnabled, genGridTitle("Simulation data"))

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

    val simCellsButton = new Button {
      text = "Toggle simulation cells"
      onAction = { (e: ActionEvent) =>
        {
          simulationCellsEnabled.value = !simulationCellsEnabled.value
        }
      }
    }

    val reqCapsButtons = new Button {
      text = "Request capabilities"
      onAction = { (e: ActionEvent) => turbo.requestCapabilities() }
    }
    
    val disableGrid = new Button {
      text = "Clear grid"
      onAction = { (e: ActionEvent) => testGrid.requestLayout() }
    }
    
    val scaleBox = mkInput(
      StringProperty("vbox scale"),
      StringProperty("vbox scale"),
      (x) => true,
      getInvalidNumTxt,
      (v) => {
        println(v)
        })

    val rightSide = new TilePane {
      margin = Insets(10)
      prefColumns = 1
      children = Seq(powerInput, hrInput, simCellsButton, reqCapsButtons, disableGrid, scaleBox)
    }

    val border = new BorderPane
   

    val statusLight = new Rectangle {
      height = 30
      fill = Color.Green
      width = 30
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

    border.center = scroll
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


  // need object bindings to live as long as Node they are affecting
  var properties = List[ObjectBinding[Double]]()

  /**
    * Ensure this object lives as long as you want the scaling to occur. The binding doesn't keep this object alive.
    * garbage collected)
    * @param cells
    * @param parentBounds
    * @param enabled
    * @param header
    */
  def genDecorated(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], enabled: BooleanProperty, header: Node): Group = {

    //TODO: scale header separately?
    val commonTelemetry = new DynamicTable(cells, parentBounds, numCols)

    val decoratedCommonTele = new VBox {

      children = Seq(new Group {children = header}, new Group {
        children = commonTelemetry
      })
      alignment = Pos.Center
    }

    decoratedCommonTele.visible <== enabled


    val scaleBinding = Bindings.createObjectBinding(() => {
      val x = decoratedCommonTele.layoutBounds.get.getWidth
      val y = parentBounds.value.getWidth
      if (x == 0.0 || y == 0.0) 1 else
      {
        val ret = 0.99 * y / x
        ret
      }

    }, decoratedCommonTele.layoutBounds, parentBounds)

    scaleBinding.onChange {
      (_,o,n) => {
        decoratedCommonTele.scaleY = n
        decoratedCommonTele.scaleX = n
      }
    }

    //make sure object binding lives a long time
    properties = scaleBinding :: properties

    new Group { children = decoratedCommonTele}
  }


  class DynamicTable(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], numCols: IntegerProperty) {

    val grid = new GridPane {
      hgap = 10
      vgap = 10
    }

    numCols.onChange {
      (_,o,n) => update()
    }

    // regen to take into account current disabled/enabled value
    private def genNodes: Seq[VBox] =
      for {
        group <- cells
        if group.enabled.getOrElse(new BooleanProperty {
          value = true
        }).value
        cell <- group.cells
      } yield new VBox {
        children = Seq(
          new Label(cell.label),
          new Text {
            text <== cell.value
            style = "-fx-font-size: 200%"
          }
        );
        padding = Insets(20)
      }


    cells.map(_.enabled).distinct.foreach(_.foreach(_.onChange {
      (_,o,n) => update()
    }))

    update()

    def addThem(row: Int, toAdd: Seq[VBox], remaining: Seq[VBox]): Unit = {
      if (toAdd.length == 0) return
      for (i <- toAdd) {
      }
      grid.addRow(row, toAdd.map(_.delegate) : _*)
      val (l,r) = remaining.splitAt(numCols.value)
      addThem(row + 1, l, r)
    }

    def update() = {
      grid.children = Seq()
      val nodes = genNodes
      val (l, r) = nodes.splitAt(numCols.value)
      addThem(0,l,r)
    }


  }

  implicit def dynamicTable2(a: DynamicTable): Node = a.grid

  val split = genSplit

  val theScene = new Scene {
    fill = LightGreen
    root = split
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Controller"
    width = 600
    height = 450
    scene = theScene
  }

  // kill the background thread
  stage.onCloseRequest = new EventHandler[WindowEvent] {

    override def handle(e: WindowEvent): Unit = {
      Platform.exit();
      antNode.stop()
      System.exit(0);
    }

  }
}
