/**
  * Created by fluxoid on 17/02/17.
  */

import java.util.prefs.Preferences
import java.util.{Timer, TimerTask}
import javafx.stage.WindowEvent

import org.cowboycoders.ant.interfaces.AntTransceiver
import org.cowboycoders.ant.profiles.FecProfile
import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities

import scala.language.implicitConversions
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node, Parent, Scene}

case class Cell(label: String, value: StringProperty)

case class GridCellGroup(enabled: Option[BooleanProperty], cells: Array[Cell])

object HelloStageDemo extends JFXApp {

  private val SHOW_SIMULATION_PREF = "showSimulation"
  private val SHOW_COMMON_PREF = "showCommon"
  private val SHOW_CAPS_PREF = "showCaps"
  val pref = Preferences.userNodeForPackage(HelloStageDemo.getClass)

  val antInterface = new AntTransceiver(1)
  val antNode = new org.cowboycoders.ant.Node(antInterface)

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

  val unknownStr: String = "unknown"

  // start capabilites
  val supportBasicLabel = "Basic resistance support"
  val supportBasicState = new StringProperty(unknownStr)
  val supportSimulationLabel = "Simulation support"
  val supportSimulationState = new StringProperty(unknownStr)
  val supportPowerLabel = "Const. power support"
  val supportPowerState = new StringProperty(unknownStr)
  val maxResistanceLabel = "Max. resistance"
  val maxResistanceState = new StringProperty(unknownStr)

  def boolToStr(b: java.lang.Boolean): String = {
    val nonNull = Option(b).getOrElse(false)
    // TODO: internationalization
    nonNull.toString
  }


  private def updateCapabilites(caps: Capabilities) = {
    supportBasicState.value = boolToStr(caps.isBasicResistanceModeSupported)
    supportPowerState.value = boolToStr(caps.isTargetPowerModeSupported)
    supportSimulationState.value = boolToStr(caps.isSimulationModeSupported)
    maxResistanceState.value = Option(caps.getMaximumResistance).map(_.toString).getOrElse(unknownStr)

  }

  timer.scheduleAtFixedRate(new TimerTask() {
    override def run(): Unit = {
      Platform.runLater {
        () => {
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

  var allToggles = List[DisplayToggle]()

  case class DisplayToggle(key: String, desc: String) {
    val toggle: BooleanProperty = mkPersistentBool(key)
    allToggles = this :: allToggles
  }

  private def mkPersistentBool(prefId: String): BooleanProperty = {
    val ret = new BooleanProperty {
      value = pref.getBoolean(prefId, false)
    }
    ret.onChange{ (_, _, n) => {
      pref.putBoolean(prefId, n)}}
    ret
  }


  // order matters
  val simulationToggle = DisplayToggle(SHOW_SIMULATION_PREF, "Simulation data")
  val telemetryToggle = DisplayToggle(SHOW_COMMON_PREF, "Common Data")
  val capabilitesToggle = DisplayToggle(SHOW_CAPS_PREF, "Capabilities")

  val testGrid = new GridPane() {
    hgap = 10
    vgap = 10
  }

  val telemetryCells = Array(
    GridCellGroup(None, Array(Cell(powerLabel, power),
      Cell(speedLabel, speed), Cell(heartRateLabel, hr),
      Cell(cadenceLabel, cadence), Cell(distanceLabel, distance), Cell(gearRatioLabel, gearRatio)))
  )

  val simulationCells = Array(GridCellGroup(Some(simulationToggle.toggle), Array(
    Cell(bikeWeightLabel, bikeWeight), Cell(userWeightLabel, userWeight), Cell(userAgeLabel, userAge),
    Cell(userHeightLabel, userHeight), Cell(wheelDiaLabel, wheelDiameter), Cell(coeffRollingLabel, coeffRolling),
    Cell(gradientLabel, gradient), Cell(windSpeedLabel, windSpeed), Cell(windCoeffLabel, windCoeff),
    Cell(draftingFactorLabel, draftingFactor))))

  val capabilitiesCells = Array(GridCellGroup(None,
    Array(
      Cell(supportBasicLabel, supportBasicState),
      Cell(maxResistanceLabel, maxResistanceState),
      Cell(supportSimulationLabel, supportSimulationState),
      Cell(supportPowerLabel, supportPowerState)
    )))


  val numCols = new IntegerProperty() {
    value = 1
  }

  def genSplit: Parent = {


    val statsVBox = new VBox()

    statsVBox.vgrow = Priority.Always

    val scroll = new ScrollPane() {
      content = statsVBox
      fitToWidth = true
      fitToHeight = false
    }

    scroll.layoutBounds.onChange {
      (_, _, newV) => {
        val size = newV.width
        var cols = 1
        if (size >= 600) {
          cols = 5
        } else if (size >= 380) {
          cols = 3
        } else if (size >= 200) {
          cols = 2
        }

        numCols.value = cols

      }
    }

    def genGridTitle(title: String): Label = {
      val label = new Label(title) {
        style = "-fx-font-size:20"
      }
      label
    }

    statsVBox.children += genDecorated(telemetryCells, scroll.viewportBounds, telemetryToggle.toggle, genGridTitle("Common telemetry"))
    statsVBox.children += genDecorated(simulationCells, scroll.viewportBounds, simulationToggle.toggle, genGridTitle("Simulation data"))
    statsVBox.children += genDecorated(capabilitiesCells, scroll.viewportBounds, capabilitesToggle.toggle, genGridTitle("Capabilities"))

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

    def genToggle(displayToggle: DisplayToggle): CheckBox = {
      val box = new CheckBox {
        text = displayToggle.desc
        selected = displayToggle.toggle.value
      }
      displayToggle.toggle <==> box.selected
      box
    }

    val checkBoxContainer = new VBox() {
      children = new Label("data to display:") :: allToggles.map(genToggle)
    }


    val reqCapsButtons = new Button {
      text = "Request capabilities"
      onAction = { (_: ActionEvent) => turbo.requestCapabilities() }
    }

    val disableGrid = new Button {
      text = "Clear grid"
      onAction = { (_: ActionEvent) => testGrid.requestLayout() }
    }

    val scaleBox = mkInput(
      StringProperty("vbox scale"),
      StringProperty("vbox scale"),
      (_) => true,
      getInvalidNumTxt,
      (v) => {
        println(v)
      })

    val rightSide = new TilePane {
      margin = Insets(10)
      prefColumns = 1
      children = Seq(powerInput, hrInput, reqCapsButtons, disableGrid, scaleBox, checkBoxContainer)
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
              getErrorTxt: (scalafx.scene.control.TextField) => String, onAccept: (String) => Unit): Node = {
    val inputField = new TextField() {
      promptText <== promptTxt
    }

    // validate an integer
    inputField.focused.onChange {
      (
        _, _, n) =>
        if (!n && !validate(inputField))
        // unfocused
          inputField.text = ""
    }

    inputField.onAction = (_: ActionEvent) => {
      if (validate(inputField)) {
        onAccept(inputField.text.value)
        inputField.text = ""
      } else new Alert(AlertType.Error) {
        title = "Error"
        contentText = getErrorTxt(inputField)
      }.showAndWait()

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

  private def getInvalidNumTxt(field: scalafx.scene.control.TextField)= {
    field.text.value + " is invalid. You must enter a valid number"
  }

  private def validateNumber(field: scalafx.scene.control.TextField) = field.text.value.matches("[0-9]+")


  // need object bindings to live as long as Node they are affecting
  private var bindingKeepAlive = scala.collection.mutable.WeakHashMap[Node, ObjectBinding[_]]()

  /**
    * Ensure this object lives as long as you want the scaling to occur. The binding doesn't keep this object alive.
    * garbage collected)
    */
  def genDecorated(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], enabled: BooleanProperty, header: Node): Group = {

    //TODO: scale header separately?
    val commonTelemetry = new DynamicTable(cells, parentBounds, numCols)

    val decoratedCommonTele = new VBox {

      children = Seq(new Group {
        children = header
      }, new Group {
        children = commonTelemetry
      })
      alignment = Pos.Center
    }

    decoratedCommonTele.visible <== enabled


    val scaleBinding = Bindings.createObjectBinding(() => {
      val x = decoratedCommonTele.layoutBounds.get.getWidth
      val y = parentBounds.value.getWidth
      if (x == 0.0 || y == 0.0) 1 else {
        val ret = 0.99 * y / x
        ret
      }

    }, decoratedCommonTele.layoutBounds, parentBounds)

    scaleBinding.onChange {
      (_, _, n) => {
        decoratedCommonTele.scaleY = n
        decoratedCommonTele.scaleX = n
      }
    }

    val ret = new Group {
      children = decoratedCommonTele
    }

    //make sure object binding lives long enough
    bindingKeepAlive(ret) =  scaleBinding

    ret
  }


  class DynamicTable(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], numCols: IntegerProperty) {

    val grid = new GridPane {
      hgap = 10
      vgap = 10
    }

    numCols.onChange {
      (_, _, _) => update()
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
        )
        padding = Insets(20)
      }


    cells.map(_.enabled).distinct.foreach(_.foreach(_.onChange {
      (_,_, _) => update()
    }))

    update()

    def addThem(row: Int, toAdd: Seq[VBox], remaining: Seq[VBox]): Unit = {
      if (toAdd.isEmpty) return
      grid.addRow(row, toAdd.map(_.delegate): _*)
      val (l, r) = remaining.splitAt(numCols.value)
      addThem(row + 1, l, r)
    }

    def update(): Unit = {
      grid.children = Seq()
      val nodes = genNodes
      val (l, r) = nodes.splitAt(numCols.value)
      addThem(0, l, r)
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
  stage.onCloseRequest = (_: WindowEvent) => {
    Platform.exit()
    antNode.stop()
    System.exit(0)
  }

  // this stuff is last to ensure all variables are initialised

  val turbo = new FecProfile {
    def onCapabilitiesReceived(capabilities: Capabilities): Unit = {
      updateCapabilites(capabilities)
    }
  }

  val turboThread = new Thread() {
    override def run(): Unit = {
      println("Node starting")
      antNode.start()
      antNode.reset()
      turbo.start(antNode)
    }
  }
  turboThread.start()
}
